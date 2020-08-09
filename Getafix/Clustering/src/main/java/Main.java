import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Main {
    public static void main(String args[]) throws Exception {
        List<Integer> intList = new ArrayList<>();
        for(int i=0;i<=561;i++){
            intList.add(i);
        }
        Collections.shuffle(intList);


        String directory = "Edit Trees";
        int count = 500;

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
        Fixer.fixer(count,intList);
    }
}
