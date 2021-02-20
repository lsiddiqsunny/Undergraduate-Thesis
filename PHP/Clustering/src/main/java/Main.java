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
        List<Integer> test_list = new ArrayList<>();
                try {

           BufferedReader reader = new BufferedReader(new FileReader("test_list.txt"));
            String line = reader.readLine();
            while (line != null) {
                if(line.charAt(line.length()-1)==' ')
                test_list.add(Integer.parseInt(line.substring(0,line.length()-1)));
                else
                    test_list.add(Integer.parseInt(line));
                //System.out.println(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(test_list.size());

        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        String directory = "D:\\Thesis\\Undergraduate-Thesis\\PHP\\All Codes\\Dataset";
        int count = 500;
        PatternMatcher pm = new PatternMatcher();
        pm.Init(directory, count, test_list);
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

