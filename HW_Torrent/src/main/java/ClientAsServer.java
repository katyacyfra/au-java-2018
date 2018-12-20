import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientAsServer implements Runnable {
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);
    private JSONObject filesInfo;
    private short port;
    private ServerSocket serverSocket;

    ClientAsServer(short serverPort, JSONObject info) throws IOException {
        filesInfo = info;
        port = serverPort;
        serverSocket = new ServerSocket(port);
    }




    private void launch() throws IOException {
        Socket socket = null;
        try {
            while (true) {
                socket = serverSocket.accept();
                executor.execute(new SeederTask(socket));
            }
        }
            finally{
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
            executor.shutdown();
        }
    }




    public void exit() throws IOException {
        serverSocket.close();
    }

    @Override
    public void run() {
        try {
            launch();
        } catch (IOException e) {
        }

    }


    class SeederTask implements Runnable {
        Socket clientSocket;

        SeederTask(Socket socket) {
            clientSocket = socket;

        }

        @Override
        public void run() {
            try {
                DataInputStream is = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream os = new DataOutputStream(clientSocket.getOutputStream());
                byte messageType = is.readByte();
                switch (messageType) {
                    case 1://stat
                        int fileId = is.readInt();
                        if (filesInfo.containsKey(String.valueOf(fileId))) {
                            JSONObject fileInfo = (JSONObject) filesInfo.get(String.valueOf(fileId));
                            JSONArray parts  = (JSONArray) fileInfo.get("parts");
                            os.writeInt(parts.size());
                            for (Object obj : parts) {
                                JSONObject ob = (JSONObject) obj;
                                os.writeInt(Integer.parseInt((String) ob.get("id")));
                            }
                        }
                        else {
                            os.writeInt(0);
                        }
                        os.flush();
                        break;
                    case 2:
                        int file = is.readInt();
                        int partNumber = is.readInt();
                        JSONObject fileInfo = (JSONObject) filesInfo.get(String.valueOf(file));
                        JSONArray parts  = (JSONArray) fileInfo.get("parts");
                        String filePath = (String) fileInfo.get("file_path");
                        JSONObject part = (JSONObject) parts.get(partNumber);
                        long partLength = (long) part.get("length");

                        byte[] content = FileUtility.readFromPosition(filePath,
                                (long) part.get("start"),
                                partLength);
                        os.writeLong(partLength);
                        os.write(content);
                        os.flush();
                        break;
                    case 3://get checksum
                        int fileID = is.readInt();
                        if (filesInfo.containsKey(String.valueOf(fileID))) {
                            JSONObject fileInf = (JSONObject) filesInfo.get(String.valueOf(fileID));
                            JSONArray partsTotal  = (JSONArray) fileInf.get("parts");
                            os.writeInt(partsTotal.size());
                            for (Object obj : partsTotal) {
                                JSONObject ob = (JSONObject) obj;
                                os.writeInt(Integer.parseInt((String) ob.get("id")));
                                os.writeUTF((String) ob.get("checkSum"));
                            }
                        }
                        else {
                            os.writeInt(0);
                        }
                        os.flush();
                        break;
                    default:
                        throw new IllegalArgumentException("No such message type");
                }
                is.close();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}


