import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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

            return bytesToHex(hash);
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


    public static String getMD5(String filePath) throws IOException, NoSuchAlgorithmException {

        try(FileInputStream inputStream = new FileInputStream(filePath))
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileChannel channel = inputStream.getChannel();
            ByteBuffer buff = ByteBuffer.allocate(2048);
            while (channel.read(buff) != -1) {
                buff.flip();
                md.update(buff);
                buff.clear();
            }
            byte[] hashValue = md.digest();
            return bytesToHex(hashValue);
        }
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


    public String LCA(String commitA, String commitB) throws IOException, org.json.simple.parser.ParseException {
        JsonWrapper first = new JsonWrapper(pathObj + File.separator + commitA);
        JsonWrapper second = new JsonWrapper(pathObj + File.separator + commitB);
        String firstCommit = "";
        String parent = first.getItemSingle("parent");

        String lowestAncestor = "";


        HashSet<String> nodes = new HashSet<>();
        nodes.add(commitA);

        while (!parent.isEmpty()) {
            nodes.add(parent);
            JsonWrapper next = new JsonWrapper(pathObj + File.separator + parent);
            parent = next.getItemSingle("parent");
        }


        parent = second.getItemSingle("parent");


        while (!parent.isEmpty()) {
            if (nodes.contains(parent)) {
                lowestAncestor = parent;
                break;
            }
            JsonWrapper next = new JsonWrapper(pathObj + File.separator + parent);
            parent = next.getItemSingle("parent");
        }
        return lowestAncestor;

    }

    public static String longestCommonString(String first, String second) {
        if (first.length() > second.length())
            return longestCommonString(second, first);

        String res = "";
        for (int ai = 0; ai < first.length(); ai++) {
            for (int len = first.length() - ai; len > 0; len--) {


                for (int bi = 0; bi < second.length() - len; bi++) {

                    if (first.regionMatches(ai, second, bi, len) && len > res.length()) {
                        res = first.substring(ai, ai + len);
                    }
                }
            }
        }
        return res;
    }


    /**
     * @param first
     * @param second
     * @param priority -- when priority is 0 it needs user interaction for solving conflicts
     *                 -- 1 - if strings are totally different we choose variant from first string
     *                 -- 2 - if strings are totally different we choose variant from second string
     *                 -- other -- behaves like priority is 0
     *                 -- options 1 and 2 are used for tests
     * @return
     */
    public static String mergeStrings(String first, String second, int priority) {
        if (first.isEmpty() && second.isEmpty()) {
            return "";
        } else if (first.isEmpty() && !second.isEmpty()) {
            return second;
        } else if (second.isEmpty() && !first.isEmpty()) {
            return first;
        } else {
            String common = longestCommonString(first, second);
            if (common.isEmpty()) { //totally different
                if (priority == 1) {
                    return first;
                }
                else if (priority == 2) {
                    return second;
                }
                else {
                    System.out.println("Different !!");
                    System.out.println(first);
                    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    System.out.println(second);
                    return first;
                }

            } else if (common.equals(first)) {
                return second;
            } else if (common.equals(second)) {
                return first;
            } else {
                int firstIndex = first.indexOf(common);
                int secondIndex = second.indexOf(common);
                System.out.println("Common: " + common);
                String firstOne = first.substring(0, firstIndex);
                String secondOne = second.substring(0, secondIndex);


                String firstTwo = first.substring(firstIndex + common.length());
                String secondTwo = second.substring(firstIndex + common.length());

                System.out.println("F1 : " + firstOne);
                System.out.println("S1: " + secondOne);
                System.out.println("F2: " + firstTwo);
                System.out.println("S2: " + secondTwo);

                return mergeStrings(firstOne, secondOne, priority) + common +
                        mergeStrings(firstTwo, secondTwo, priority);
            }

        }

    }


}
