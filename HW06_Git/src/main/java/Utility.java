import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


import static java.util.stream.Collectors.toList;


public class Utility {

    private String pathObj;

    Utility(String objDirName) {
        pathObj = objDirName;
    }

    public static String getCurrentTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss.SSS");
        Date date = new Date();
        return dateFormat.format(date);
    }


    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String generateHash(String filename) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((filename + getCurrentTime()).getBytes(StandardCharsets.UTF_8));

            return new String(bytesToHex(hash));
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String generateRandomHash() {
        String hashOne = Long.toHexString(Double.doubleToLongBits(Math.random()));
        String hashTwo = Long.toHexString(Double.doubleToLongBits(Math.random()));
        return hashOne + hashTwo;
    }


    public String getCommitTree(String commit) throws IOException, org.json.simple.parser.ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(pathObj + File.separator + commit));
        JSONObject jsoncommit = (JSONObject) obj;
        String hash = (String) jsoncommit.get("tree");
        return hash;
    }

    public JSONObject getJsonInfo(String hash) throws IOException, org.json.simple.parser.ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(new FileReader(pathObj + File.separator + hash));
        JSONObject jsonObject = (JSONObject) obj;
        return jsonObject;
    }

    public static boolean equalHash(String a, String b) throws NoSuchAlgorithmException, IOException {
        byte[] bA = Files.readAllBytes(Paths.get(a));
        byte[] hashA = MessageDigest.getInstance("MD5").digest(bA);

        byte[] bB = Files.readAllBytes(Paths.get(b));
        byte[] hashB = MessageDigest.getInstance("MD5").digest(bB);

        return bytesToHex(hashA).equals(bytesToHex(hashB));
    }

    public static String getMD5(String filePath) throws IOException, NoSuchAlgorithmException {
        byte[] b = Files.readAllBytes(Paths.get(filePath));
        byte[] hash = MessageDigest.getInstance("MD5").digest(b);
        return bytesToHex(hash);
    }

    public String createBlob(String source) throws IOException {
        String filename = generateHash(source);
        File dest = new File(pathObj + File.separator + filename);
        dest.getParentFile().mkdirs();
        Files.copy(Paths.get(source), dest.toPath());
        return filename;

    }

    public String createTree(String source) {
        String filename = generateHash(source);
        File dest = new File(pathObj + File.separator + filename);
        dest.getParentFile().mkdirs();
        try {
            dest.createNewFile();
            return filename;
        } catch (IOException e) {
            //System.out.println("File: " + source.getName() + " wiil be not indexed!");
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> indexDir(String source) {
        File folder = new File(source);
        File[] listOfFiles = folder.listFiles();
        ArrayList<String> files = new ArrayList<>();

        for (File file : listOfFiles) {
            if (file.isDirectory() && !(file.getName()).equals("_git")) {
                files.addAll(indexDir(source + File.separator + file.getName()));
            }
        }

        for (File file : listOfFiles) {
            if (file.isFile()) {
                files.add(file.toString());
            }
        }
        return files;
    }

    public static String normalizePath(String path) {
        String p = new File(path).getPath();
        Path nPath = Paths.get(p).normalize();
        return nPath.toString();
    }


}
