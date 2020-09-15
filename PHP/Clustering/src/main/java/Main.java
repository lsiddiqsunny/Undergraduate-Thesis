import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Main {
    public static void main(String args[]) throws Exception {
        BufferedReader reader;
        List<Integer> test_list = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(
                    "test_list.txt"));
            String line = reader.readLine();
            while (line != null) {
                test_list.add(Integer.parseInt(line));
                //System.out.println(line);
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(test_list.size());
/*


*/
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String directory = "Dataset";
        int count = 10;
        PatternMatcher pm = new PatternMatcher();
        pm.Init(directory, count,test_list);
        EditPattern editClusters = pm.cluster();
        JsonObject dendogram = editClusters.convert2Json();
        String data = gson.toJson(dendogram);

        FileWriter writer = new FileWriter("target/dendogram.json");
        writer.write(data);
        writer.flush();
        writer.close();
        Fixer.fixer(test_list);
    }
}
