import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//https://allenkim67.github.io/programming/2016/05/04/how-to-make-your-own-bittorrent-client.html#pieces

public class ClientAsClient {

    private static ExecutorService executor = Executors.newFixedThreadPool(5);

    public static int upload(String filePath) throws IOException {
        File file = new File(filePath);
        Socket query = new Socket("localhost", 8081);
        DataOutputStream os = new DataOutputStream(query.getOutputStream());
        os.writeByte(2);
        os.writeUTF(file.getName());
        os.writeLong(file.length());
        os.flush();

        DataInputStream is = new DataInputStream(query.getInputStream());
        int result = is.readInt();

        os.close();
        is.close();
        query.close();
        return result;
    }

    public static void exit() {
        executor.shutdown();
    }

    private static String getFileName(int fileId) throws IOException {
        Socket query = new Socket("localhost", 8081);
        DataOutputStream os = new DataOutputStream(query.getOutputStream());
        os.writeByte(6);
        os.writeInt(fileId);
        os.flush();

        DataInputStream is = new DataInputStream(query.getInputStream());
        String result = is.readUTF();

        os.close();
        is.close();
        query.close();
        return result;

    }

    public static List<String> source(int fileId) throws IOException {
        List<String> seeds = new ArrayList<>();
        Socket query = new Socket("localhost", 8081);
        DataOutputStream os = new DataOutputStream(query.getOutputStream());
        os.writeByte(3);
        os.writeInt(fileId);
        os.flush();

        DataInputStream is = new DataInputStream(query.getInputStream());
        int count = is.readInt();
        int i = 0;
        while (i < count) {
            StringBuilder sb = new StringBuilder();
            sb.append(is.readByte());
            sb.append(".");
            sb.append(is.readByte());
            sb.append(".");
            sb.append(is.readByte());
            sb.append(".");
            sb.append(is.readByte());
            sb.append(":");
            sb.append(is.readShort());
            seeds.add(sb.toString());
            i++;
        }
        os.close();
        is.close();
        query.close();
        return seeds;

    }

    public static void list() throws IOException {
        Socket query = new Socket("localhost", 8081);
        DataOutputStream os = new DataOutputStream(query.getOutputStream());
        os.writeByte(1);
        os.flush();

        DataInputStream is = new DataInputStream(query.getInputStream());
        int count = is.readInt();
        System.out.println("Total " + count + " files:");
        System.out.println("ID    Name    Size");
        int i = 0;
        while (i < count) {
            System.out.println(is.readInt() + "    " + is.readUTF() + "    " + is.readLong());
            i++;
        }
        os.close();
        is.close();
        query.close();

    }

    private static int partsCount(long size) {
        long number = size/FileUtility.BLOCK_SIZE;
        if (size % FileUtility.BLOCK_SIZE != 0) {
            number++;
        }
        return (int) number;
    }


    public static JSONObject download(int fileID) throws IOException {
        long size = getSize(fileID);
        if (size == 0) {
            System.out.println("No such file!");
            return null;
        }
        int partsNumber = partsCount(size);

        boolean[] requested = new boolean[partsNumber];
        boolean[] downloaded = new boolean[partsNumber];

        List<String> available = source(fileID);
        if (available.size() == 0) {
            System.out.println("No available peers!");
            return null;
        }
        //create new file
        String filePath = getFileName(fileID);
        RandomAccessFile f = new RandomAccessFile(filePath, "rw");
        f.setLength(size);
        f.close();

        JSONObject obj = new JSONObject();
        obj.put("file_path", filePath);

        obj.put("parts", new JSONArray());
        JSONArray partsInfo = new JSONArray();


        List<DownloadTask> tasks = new ArrayList<>();


        for (String a : available) {
            tasks.add(new DownloadTask(a, requested, downloaded, fileID, filePath));
            //executor.execute(new DownloadTask(a, requested, downloaded, fileID, filePath));
        }
        try {
            executor.invokeAll(tasks);
            boolean success = true;
            long i = 0;
            long done = 0;
            long partLength = FileUtility.BLOCK_SIZE;
            while (i < partsNumber) {
                if (requested[(int) i]) {
                    JSONObject p = new JSONObject();
                    p.put("id", String.valueOf(i));
                    p.put("start", (long) FileUtility.BLOCK_SIZE*i);
                    p.put("length", partLength);
                    partsInfo.add(p);
                }
                else {
                    success = false;
                }

                if (done + FileUtility.BLOCK_SIZE > size ) {
                    partLength = size - done;
                }
                else {
                    done = done + FileUtility.BLOCK_SIZE;
                }
                i++;
            }
            if (success) {
                System.out.println(filePath + " was successfully downloaded");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Download was interrupted!");
            return obj;
        }
        finally {
            obj.put("parts", partsInfo);
        }
        return obj;
    }

    private static long getSize(int fileId) throws IOException {
        Socket query = new Socket("localhost", 8081);
        DataOutputStream os = new DataOutputStream(query.getOutputStream());
        os.writeByte(5);
        os.writeInt(fileId);
        os.flush();

        DataInputStream is = new DataInputStream(query.getInputStream());
        long result = is.readLong();

        os.close();
        is.close();
        query.close();
        return result;
    }

    private static byte[] get(int fileId, int part, String ip, short port) throws IOException {
        Socket query = new Socket(ip, port);
        DataOutputStream os = new DataOutputStream(query.getOutputStream());
        os.writeByte(2);
        os.writeInt(fileId);
        os.writeInt(part);
        os.flush();

        DataInputStream is = new DataInputStream(query.getInputStream());
        long size = is.readLong();
        byte[] result = new byte[(int) size];
        is.read(result);
        os.close();
        is.close();
        query.close();
        return result;
    }

    private static Queue<Integer> stat(int fileId, String ip, short port) throws IOException {
        Queue<Integer> result = new PriorityQueue<>();
        Socket query = new Socket(ip, port);
        DataOutputStream os = new DataOutputStream(query.getOutputStream());
        os.writeByte(1);
        os.writeInt(fileId);
        os.flush();

        DataInputStream is = new DataInputStream(query.getInputStream());
        int partsNumber = is.readInt();
        int i = 0;
        while (i < partsNumber) {
            result.add(is.readInt());
            i++;
        }

        os.close();
        is.close();
        query.close();
        return result;
    }

    static class DownloadTask implements Callable<Boolean> {
        boolean[] requested;
        boolean[] ready;
        String ip;
        Short port;
        int fileId;
        String filePath;

        DownloadTask(String peer, boolean[] req, boolean[] downloaded, int file, String fPath) {
            requested = req;
            String[] addr = peer.split(":");
            ip = addr[0];
            port = Short.parseShort(addr[1]);
            ready = downloaded;
            fileId = file;
            filePath = fPath;
        }

        @Override
        public Boolean call() {
            try {
                Queue<Integer> availableParts = stat(fileId, ip, port);
                while (!availableParts.isEmpty()) {
                    int part = availableParts.poll();

                    boolean willDownload = false;
                    synchronized (requested) {
                        if (!requested[part]) {
                          requested[part] = true;
                          willDownload = true;
                        }
                    }
                    if (willDownload) {
                        try {
                            //System.out.println("write part " + part + " by " + port);
                            byte[] content = get(fileId, part, ip, port);
                            FileUtility.writePart(filePath, part*FileUtility.BLOCK_SIZE, content);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                            synchronized (requested) {
                                requested[part] = false;
                            }
                        }

                    }

                }
                return true;

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }


        }
    }



}
