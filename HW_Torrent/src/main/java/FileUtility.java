import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class FileUtility {
    final static int BLOCK_SIZE = 2048;

    public static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String hashBytes(byte[] content) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(content);
        byte[] hashValue = md.digest();
        return bytesToHex(hashValue);

    }

    public static JSONArray splitFile(String filePath, int fileId) throws IOException, NoSuchAlgorithmException {
        JSONArray parts = new JSONArray();
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            DataInputStream is = new DataInputStream(inputStream);
            File f = new File(filePath);
            long size = f.length();

            long bytesToRead;
            long currentPos = 0;
            int i = 0;
            byte[] buff;
            if (size < BLOCK_SIZE) {
                buff = new byte[(int) size];
            }
            else {
                buff = new byte[BLOCK_SIZE];
            }
            while ((bytesToRead = is.read(buff)) > 0) {
                if (bytesToRead > 0 ) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", String.valueOf(i));
                    obj.put("start", currentPos);
                    obj.put("length", bytesToRead);
                    currentPos = currentPos + bytesToRead;
                    obj.put("checkSum", hashBytes(buff));
                    if (currentPos + BLOCK_SIZE > size) {
                        buff = new byte[((int) (size - currentPos))];
                    }
                    parts.add(obj);
                    i++;
                }

            }
        }
        return parts;
    }

    public static void writePart(String filePath, long start, byte[] contents) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
        long startPositon = Byte.SIZE*start / Byte.SIZE;
        raf.seek(startPositon);
        raf.write(contents);
        raf.close();
    }

    public static byte[] readFromPosition(String filePath, long start, long length) throws IOException {
        File f = new File(filePath);
        if (!f.exists()) {
            return new byte[(int) length];
        }
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
