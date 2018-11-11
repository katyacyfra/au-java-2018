import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * priority -- when priority is 0 it needs user interaction for solving conflicts
 * -- 1 - if strings are totally different we choose variant from first string
 * -- 2 - if strings are totally different we choose variant from second string
 * -- other -- behaves like priority is 0
 * -- options 1 and 2 are used for tests
 */

public class FileMerger {


    private String filename;


    private List<String> first = new ArrayList<String>();
    private List<String> second = new ArrayList<String>();

    FileMerger(String file, String firstPath, String secondPath) throws FileNotFoundException {
        filename = file;
        Scanner f = new Scanner(new File(firstPath));
        while (f.hasNextLine()){
            first.add(f.nextLine());
        }

        Scanner s = new Scanner(new File(secondPath));
        while (s.hasNextLine()){
            second.add(s.nextLine());
        }
    }

    public static boolean matchSubarrays(List<String> first, List<String> second, int toffset, int ooffset, int len) {
        List<String> one = first.subList(toffset, toffset + len);
        List<String> two = second.subList(ooffset, ooffset + len);
        boolean result = true;
        for (int i = 0; i < len; i++) {
            if (!one.get(i).equals(two.get(i))) {
                result = false;
                break;
            }
        }
        return result;
    }

    public List<String> runMerge() {
        return mergeLists(first, second);
    }

    public static List<String> longestCommonSubsequence(List<String> first, List<String> second) {
        if (first.size() > second.size())
            return longestCommonSubsequence(second, first);

        List<String> res = new ArrayList<>();
        for (int ai = 0; ai < first.size(); ai++) {
            for (int len = first.size() - ai; len > 0; len--) {

                for (int bi = 0; bi < second.size() - len; bi++) {

                    if (matchSubarrays(first, second, ai, bi, len) && len > res.size()) {
                        res = first.subList(ai, ai + len);
                    }
                }
            }
        }
        return res;
    }

    private static List<String> writeConflict(List<String> first, List<String> second) {
        List<String> result = new ArrayList<>();
        result.add("<<<<<<< HEAD");
        result.addAll(first);
        result.add("=======");
        result.addAll(second);
        result.add(">>>>>>> new branch to commit");
        return result;
    }


    public static List<String> mergeLists(List<String> first, List<String> second) {
        List<String> result = new ArrayList<>();
        if (first.isEmpty() && second.isEmpty()) {
            return result;
        } else if (first.isEmpty() && !second.isEmpty()) {
            return writeConflict(first, second);
        } else if (second.isEmpty() && !first.isEmpty()) {
            return writeConflict(first, second);
        } else {
            List<String> common = longestCommonSubsequence(first, second);
            if (common.isEmpty()) { //totally different
                return writeConflict(first, second);

            } else if (common.equals(first)) {
                return writeConflict(first, second);
            } else if (common.equals(second)) {
                return writeConflict(first, second);
            } else {
                int firstIndex = Collections.indexOfSubList(first, common);
                int secondIndex = Collections.indexOfSubList(second, common);
                List<String> firstOne = first.subList(0, firstIndex);
                List<String> secondOne = second.subList(0, secondIndex);


                List<String> firstTwo = first.subList(firstIndex + common.size(), first.size());
                List<String> secondTwo = second.subList(secondIndex + common.size(), second.size());


                result.addAll(mergeLists(firstOne, secondOne));
                result.addAll(common);
                result.addAll(mergeLists(firstTwo, secondTwo));
                return result;
            }
        }
    }
}
