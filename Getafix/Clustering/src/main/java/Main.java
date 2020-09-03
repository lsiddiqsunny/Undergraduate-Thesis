import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Main {
    public static void main(String args[]) throws Exception {
        List<Integer> intList = new ArrayList<>();

        String directory = "Train_Edit Trees";
        int count = 561;

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        PatternMatcher pm = new PatternMatcher();
        pm.Init(directory, count,intList);
        EditPattern editClusters = pm.cluster();
        JsonObject dendogram = editClusters.convert2Json();
        String data = gson.toJson(dendogram);

        FileWriter writer = new FileWriter("target/dendogram.json");
        writer.write(data);
        writer.flush();
        writer.close();
        Fixer.fixer(561);
    }
}
