import java.io.*;
import java.net.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Tracker {
    private final static String META_INFO = "./_info";

    private static Map<String, List<Integer>> activeSeeds = new HashMap<>();
    private static Map<String, LocalDateTime> updatedSeeds = new HashMap<>();

    private static ExecutorService executor = Executors.newFixedThreadPool(5);

    private static ExecutorService executorUpdate = Executors.newSingleThreadExecutor();

    private static ExecutorService executorQuery = Executors.newFixedThreadPool(5);

    private static JSONObject info;

    private static ServerSocket serverSocket;



    private static void writeMeta(JSONObject obj) throws IOException {
        try (FileWriter file = new FileWriter(META_INFO)) {
            file.write(obj.toJSONString());
            file.flush();
        }
    }

    public static void exit() throws IOException {
        serverSocket.close();

    }

    private static void init() throws IOException {
        //directories, metas
        File f = new File(META_INFO);
        if(!f.exists()) {
            f.createNewFile();
            JSONObject empty = new JSONObject();
            writeMeta(empty);
        }
    }

    public static void main(String[] arg) throws IOException, ParseException {
        serverSocket = new ServerSocket(8081);
        JSONParser parser = new JSONParser();
        init();
        Object obj = parser.parse(new FileReader(META_INFO));
        info = (JSONObject) obj;
        System.out.println("Tracker is ready!");
        Socket socket = null;
        try {
            while (true) {
                socket = serverSocket.accept();
                executor.execute(new TrackerTask(socket));
            }
        } finally {
            System.out.println("Bye!");
            if (serverSocket !=null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (socket !=null && !socket.isClosed()) {
            socket.close();
            }
            executor.shutdown();
            executorUpdate.shutdown();
            executorQuery.shutdown();
        }
    }


    static private void deleteInactive() {
        List<String> toDelete = new ArrayList<>();
        synchronized (updatedSeeds) {
            Set<String> keys = updatedSeeds.keySet();
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String key = it.next();
                if (Duration.between(LocalDateTime.now(), updatedSeeds.get(key)).abs().getSeconds()
                        > Client.MINUTES_TO_UPDATE*60) {
                    it.remove();
                    toDelete.add(key);
                }
            }
        }

        synchronized (activeSeeds) {
            for (String s : toDelete) {
                if (activeSeeds.containsKey(s)) {
                    activeSeeds.remove(s);
                }
            }

        }

    }

    static private byte[] addressToBytes(String ipAd) throws UnknownHostException {
        InetAddress ip = InetAddress.getByName(ipAd);
        return ip.getAddress();

    }


    static class TrackerTask implements Runnable {
        Socket clientSocket;

        TrackerTask(Socket client) {
            clientSocket = client;
        }

        @Override
        public void run() {
            try {
                DataInputStream is = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream os = new DataOutputStream(clientSocket.getOutputStream());
                byte messageType = is.readByte();
                switch (messageType) {
                    case 1:
                        Set<String> keys = info.keySet();//assuming that we can not delete file from tracker
                        os.writeInt(keys.size());
                        Iterator<String> it = keys.iterator();
                        while (it.hasNext()) {
                            String key = it.next();
                            os.writeInt(Integer.parseInt(key));
                            JSONObject obj = (JSONObject) info.get(key);
                            os.writeUTF((String) obj.get("name"));
                            os.writeLong((Long) obj.get("size"));
                        }
                        os.flush();
                        break;
                    case 2:
                        String fileName = is.readUTF();
                        long size = is.readLong();
                        //
                        Future<Integer> res = executorUpdate.submit(new UploadTask(fileName, size));
                        os.writeInt(res.get());
                        os.flush();
                        break;
                    case 3:
                        int fid = is.readInt();
                        deleteInactive();
                        List<String> source = new ArrayList<>();
                        synchronized (activeSeeds) {
                            for (Map.Entry<String, List<Integer>> entry : activeSeeds.entrySet()) {
                                if (entry.getValue().contains(fid)) {
                                    source.add(entry.getKey());
                                }
                            }
                        }
                        os.writeInt(source.size());
                        for (String s : source) {
                            String[] parts = s.split(":");
                            byte[] bytes = addressToBytes(parts[0]);
                            for (byte b : bytes) {
                                os.writeByte(b & 0xFF);
                            }
                            os.writeShort(Short.parseShort(parts[1]));
                        }
                        os.flush();
                        break;
                    case 4:
                        boolean status;
                        short port = is.readShort();
                        int amount = is.readInt();
                        int i = 0;
                        List<Integer> fileIDs = new ArrayList<>();
                        String ip = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/","");
                        while (i < amount) {
                            int id = is.readInt();
                            fileIDs.add(id);
                            i++;
                        }
                        Future<Boolean> answer = executorUpdate.submit(new UpdateTask(port, ip, fileIDs));
                        status = answer.get();
                        os.writeBoolean(status);
                        os.flush();
                        LocalDateTime now = LocalDateTime.now();
                        updatedSeeds.put(ip + ":" + port, now);
                        break;
                    case 5://get size by id
                        int fileId = is.readInt();
                        JSONObject obj = (JSONObject) info.get(String.valueOf(fileId));
                        if (obj != null) {
                            os.writeLong((long) obj.get("size"));
                        }
                        else {
                            os.writeLong(0);
                        }
                        os.flush();
                        break;
                    case 6://get name by id
                        int file = is.readInt();
                        JSONObject fInfo = (JSONObject) info.get(String.valueOf(file));
                        if (fInfo != null) {
                            os.writeUTF((String) fInfo.get("name"));
                        }
                        else {
                            os.writeUTF("");
                        }
                        os.flush();
                        break;

                    default:
                        throw new IllegalArgumentException("No such message type");
                }
                is.close();
                os.close();

            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    static class UploadTask implements Callable<Integer> {
        private String fileName;
        private long size;

        UploadTask(String name, long size) {
            fileName = name;
            this.size = size;
        }

        @Override
        public Integer call() {
            int result = 0;
                JSONObject fileInfo = new JSONObject();
                fileInfo.put("name", fileName);
                fileInfo.put("size", size);

            synchronized (info) {
                info.put(String.valueOf(info.size() + 1), fileInfo);
                try {
                    Tracker.writeMeta(info);
                    result = info.size();
                } catch (IOException e) {
                    e.printStackTrace();
                    result = 0; // error while update
                }
            }
            return result;
        }
    }

    static class UpdateTask implements Callable<Boolean> {
        private short port;
        private String ip;
        private List<Integer> ids;

        UpdateTask(short clientPort, String clientIP, List<Integer> IDs) {
            port = clientPort;
            ip = clientIP;
            ids = IDs;
        }

        @Override
        public Boolean call() {
            String ipAndPort = ip + ":" + String.valueOf(port);
            synchronized (activeSeeds) {
                activeSeeds.put(ipAndPort, ids);
            }
            return true;
        }
    }
}
