import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


public class FileUtility {
    final static int BLOCK_SIZE = 2048;

    static JSONArray splitFile(String filePath, int fileId) throws IOException {
        JSONArray parts = new JSONArray();
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            FileChannel channel = inputStream.getChannel();
            ByteBuffer buff = ByteBuffer.allocate(BLOCK_SIZE);
            long bytesToRead = 0;
            long currentPos = 0;
            int i = 0;
            while (bytesToRead != -1) {
                bytesToRead = channel.read(buff);
                if (bytesToRead != -1) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", String.valueOf(i));
                    obj.put("start", currentPos);
                    obj.put("length", bytesToRead);
                    currentPos = currentPos + bytesToRead;
                    buff.flip();
                    //MessageDigest md = MessageDigest.getInstance("MD5");
                    //md.update(buff);
                    //byte[] hashValue = md.digest();
                    //obj.put("checkSum", bytesToHex(hashValue));
                    buff.clear();
                    parts.add(obj);
                    i++;
                }

            }
        }
        return parts;
    }

    static void writePart(String filePath, long start, byte[] contents) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
        long startPositon = Byte.SIZE*start / Byte.SIZE;
        raf.seek(startPositon);
        raf.write(contents);
        raf.close();
    }

    static byte[] readFromPosition(String filePath, long start, long length) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(filePath, "r");
        //long endPositon = raf.length() - Long.SIZE / Byte.SIZE;

        long startPositon = Byte.SIZE*start / Byte.SIZE;
        raf.seek(startPositon);
        byte[] bytes = new byte[(int)length];

        raf.read(bytes);
        raf.close();

        //String appendedData = new String(bytes);
        //System.out.println(appendedData);
        return bytes;

    }



}
