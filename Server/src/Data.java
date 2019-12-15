import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Data {

    public static String[] readConfig() {
        Properties prop = new Properties();
        String[] settings = null;
        try {
            settings = new String[5];
            prop.load(new FileInputStream("server_config.properties"));
            settings[0] = prop.getProperty("ip");
            settings[1] = prop.getProperty("port");
            settings[2] = prop.getProperty("service_name");
            settings[3] = prop.getProperty("max_cells");
            settings[4] = prop.getProperty("tag_size");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return settings;

    }

    public static void resetBoxes() {
        try {
            File file = new File("boxes.json");
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveBoxes(List<Map<String, byte[]>> boxes) {
        Gson gson = new Gson();
        try {
            if (boxes != null) {
                Type listType = new TypeToken<List<Map<String, byte[]>>>() {
                }.getType();
                String json = gson.toJson(boxes, listType);
                JsonWriter jsonWriter = new JsonWriter(new FileWriter("boxes.json"));
                jsonWriter.jsonValue(json);
                jsonWriter.flush();
                jsonWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Map<String, byte[]>> loadBoxes() {
        Gson gson = new Gson();
        try {
            JsonReader jsonReader = new JsonReader(new FileReader("boxes.json"));
            Type listType = new TypeToken<List<Map<String, byte[]>>>() {
            }.getType();
            List<Map<String, byte[]>> boxes = gson.fromJson(jsonReader, listType);
            jsonReader.close();
            return boxes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
