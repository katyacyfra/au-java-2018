import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.Iterator;
import java.util.List;

public class JsonWrapper {

    private JSONObject jsonObject;
    private String filename;

    public JsonWrapper(String filePath) throws IOException, org.json.simple.parser.ParseException {
        filename = filePath;
        //check emptiness of file
        BufferedReader br = new BufferedReader(new FileReader(filename));
        if (br.readLine() == null) {
            jsonObject = new JSONObject();
        } else {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filename));
            jsonObject = (JSONObject) obj;
        }
    }

    public boolean hasKey(String key) {
        return jsonObject.containsKey(key);
    }

    public Iterator<String> getKeyIterator() {
        return jsonObject.keySet().iterator();
    }

    public void addItemSingle(String key, String value) {
        jsonObject.put(key, value);
    }

    public JSONArray getArrayItem(String key) {
        return (JSONArray) jsonObject.get(key);
    }

    public String getItemSingle(String key) {
        return (String) jsonObject.get(key);
    }

    public void addItemArray(String key, List<String> values) {
        JSONArray list = new JSONArray();
        Iterator<String> it = values.iterator();
        while (it.hasNext()) {
            list.add(it.next());
        }
        jsonObject.put(key, list);
    }

    public void deleteItem(String key) {
        jsonObject.remove(key);

    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void write() throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(new File(filename))) {
            writer.print(jsonObject.toJSONString());
        }
    }

    public void clear() throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(new File(filename))) {
            writer.print((new JSONObject()).toJSONString());
        }
    }


}
