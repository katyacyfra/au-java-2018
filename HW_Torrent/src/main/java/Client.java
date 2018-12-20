import asg.cliche.Command;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//https://stackoverflow.com/questions/5680259/using-sockets-to-send-and-receive-data

public class Client {

    private static ScheduledExecutorService executorUpdate = Executors.newSingleThreadScheduledExecutor();
    //private static ExecutorService executor = Executors.newFixedThreadPool(5);
    private static short port;
    private static ClientAsClient clientWorker;
    private static ClientAsServer serverWorker;
    public final static String META_INFO = "./_fileInfo";
    private static JSONObject info;
    public static final int MINUTES_TO_UPDATE = 1;

    private static void saveInfo(JSONObject obj) throws IOException {
        try (FileWriter file = new FileWriter(META_INFO)) {
            file.write(obj.toJSONString());
            file.flush();
        }

    }

    Client(short portForOthers) throws IOException, ParseException {
        File f = new File(META_INFO);
        if(!f.exists()) {
            f.createNewFile();
            JSONObject empty = new JSONObject();
            saveInfo(empty);
        }
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(META_INFO));
        info = (JSONObject) obj;
        port = portForOthers;
        clientWorker = new ClientAsClient();
        serverWorker = new ClientAsServer(port, info);
        Thread serverThread = new Thread(serverWorker);
        serverThread.start();

        executorUpdate.scheduleAtFixedRate(new Updater(), 0, MINUTES_TO_UPDATE, TimeUnit.MINUTES);
    }

    @Command
    public static void upload(String filePath) throws IOException, NoSuchAlgorithmException {
        File f = new File(filePath);
        if(f.exists() && !f.isDirectory()) {
            int fileId = clientWorker.upload(filePath);
            JSONArray partsInfo = FileUtility.splitFile(filePath, fileId);
            JSONObject fileInfo = new JSONObject();
            fileInfo.put("file_path", filePath);
            fileInfo.put("parts", FileUtility.splitFile(filePath, fileId));
            synchronized (info) {
                info.put(String.valueOf(fileId), fileInfo);
                saveInfo(info);
            }
        }
        else {
            System.out.println("No such file!");
        }
    }

    @Command
    public static void list() throws IOException {
        clientWorker.list();

    }
    @Command
    public static void exit() throws IOException {
        executorUpdate.shutdown();
        serverWorker.exit();
        clientWorker.exit();
    }


    @Command
    public static void download(int fileId) throws IOException {
        JSONObject obj = clientWorker.download(fileId);
        if (obj != null) {
            synchronized (info) {
                info.put(String.valueOf(fileId), obj);
                saveInfo(info);
            }
        }
    }

    @Command
    public static void source(int fileId) throws IOException {
        List<String> result = clientWorker.source(fileId);
        for (String addr : result) {
            System.out.println(addr);
        }

    }

    class Updater implements Runnable {
        @Override
        public void run() {
            try {
                Set<String> keys = info.keySet();
                Socket query = new Socket("localhost", 8081);
                DataOutputStream os = new DataOutputStream(query.getOutputStream());
                os.writeByte(4);
                os.writeShort(port);

                //check unavailable files
                List<String> toRemove = new ArrayList<>();
                synchronized (info) {
                    for (String key : keys) {
                        JSONObject fInfo = (JSONObject) info.get(key);
                        String filename = (String) fInfo.get("file_path");
                        File f = new File(filename);
                        JSONArray parts = (JSONArray) fInfo.get("parts");
                        if (!f.exists() || parts.size() == 0) {
                            toRemove.add(key);
                        }
                    }
                }
                synchronized (info) {
                    for (String key : toRemove) {
                        info.remove(key);
                    }
                    saveInfo(info);
                    keys = info.keySet();
                }


                os.writeInt(keys.size());
                Iterator<String> it = keys.iterator();

                while (it.hasNext()) {
                    os.writeInt(Integer.parseInt(it.next()));
                }

                DataInputStream is = new DataInputStream(query.getInputStream());
                boolean answer = is.readBoolean();
                if (!answer) {
                    System.out.println("Update failed!");
                }
                os.flush();
                os.close();
                is.close();
                query.close();
            } catch (IOException e) {
                e.printStackTrace();
            }



        }
    }

}
