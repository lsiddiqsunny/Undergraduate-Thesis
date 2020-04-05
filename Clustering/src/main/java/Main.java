import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


public class Main {
    public static void main(String args[]) throws Exception {
        String directory = "Edit Trees"; // got from gumtree
        int count = 200;

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        PatternMatcher pm = new PatternMatcher(); // create pattern matcher class
        pm.Init(directory, count); // initiate with the directory
        EditPattern editClusters = pm.cluster(); // call the cluster function
        JsonObject dendogram = editClusters.convert2Json(); // convert it to json
        String data = gson.toJson(dendogram);

        //FileWriter writer = new FileWriter("target/dendogram.json");
        //writer.write(data);
        //writer.flush();
        //writer.close();
    }
}
