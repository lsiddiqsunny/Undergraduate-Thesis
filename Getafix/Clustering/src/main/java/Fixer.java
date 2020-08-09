import com.google.gson.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;


public class Fixer {
    public static String toPrettyFormat(String jsonString)
    {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

    static ArrayList<FixerEditPattern> patterns;

    public static void fixer(int start,List<Integer> inList) throws Exception {
/*
        File file = new File("test.java");
        ArrayList<Integer> lineNum = new ArrayList<>();
        lineNum.add(3);

        String json = new AstBuilder(file,lineNum).getTargetAst();
        //System.out.println(toPrettyFormat(json));
*/
        //JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        int count=  0;
        for(int i=start;i<inList.size();i++) {
            String filename = "target\\dendogram.json";
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(new FileReader("Edit Trees\\change_pair"+inList.get(i)+ ".json"), JsonObject.class);
            FixerEditPattern target = new FixerEditPattern(jsonObject.getAsJsonObject("before_tree"), jsonObject.getAsJsonObject("after_tree"));
            try {
                JsonObject root = gson.fromJson(new FileReader(filename), JsonObject.class);
                patterns = new ArrayList<>();
                getEditPatterns(root, target);
                JsonObject obj = new JsonObject();
                obj.add("target", target.beforePattern);

                for (int j = 0; j < patterns.size(); j++) {
                    obj.add("suggestion" + j, patterns.get(j).afterPattern);
                }

                System.out.println(toPrettyFormat(obj.toString()));
                System.out.println(patterns.size());
                if(patterns.size()!=0){
                    count++;
                }
                File solution = new File("output\\output"+i+".json");
                FileWriter fileWriter = new FileWriter(solution);
                fileWriter.write(toPrettyFormat(obj.toString()));
                fileWriter.flush();
                fileWriter.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        System.out.println("Solution found for: "+count+"\nSolution found rate: "+((count*100.0)/61));
    }

    public static FixerEditPattern getEditPatterns(JsonObject obj,FixerEditPattern target)
    {

        //should have done a null check and return zero otherwise sum of (1+nodes in child(i) for all i )
        JsonArray children = obj.getAsJsonArray("children");
        if (children.size() != 0) {
            for (int i = 0; i < children.size(); i++) {
                if (Match(children.get(i).getAsJsonObject().get("before_pattern").getAsJsonObject(), target.getBeforePattern())) {
                    //System.out.println("**Match:"+children.get(i).getAsJsonObject().get("before_pattern").toString());
                    getEditPatterns(children.get(i).getAsJsonObject(), target);
                }
                else {
                    //System.out.println("noMatch"+children.get(i).getAsJsonObject().get("before_pattern").toString());
                }
            }
        }
        if(children.size()==0){
            patterns.add(new FixerEditPattern(obj,0));
            System.out.println("********  "+obj.get("pattern_id").toString());
        }
        return new FixerEditPattern(obj,0);
    }

    public static boolean Match(JsonObject suggestionBT, JsonObject targetBT){
        //System.out.println(suggestionBT);
        String labelS = suggestionBT.get("label").getAsString();
        String labelT = targetBT.get("label").getAsString();
        String typeS = suggestionBT.get("type").getAsString();
        String typeT = targetBT.get("type").getAsString();
        JsonArray childrenS = suggestionBT.getAsJsonArray("children");
        JsonArray childrenT = targetBT.getAsJsonArray("children");

        /*if(map.containsKey(labelS)) {
            if (!map.get(labelS).equals(labelT)) return false;
        }
        else{
            if(map.containsValue(labelT)) return false;
            map.put(labelS,labelT);
        }*/

        if(!typeS.equals("?") && !typeS.equals(typeT)) return false;

        if(labelS.contains("hole_")) return true;

        if(childrenS.size() != childrenT.size()) return false;
        else{
            for(int i = 0; i < childrenS.size(); i++){
                JsonObject chS = childrenS.get(i).getAsJsonObject();
                JsonObject chT = childrenT.get(i).getAsJsonObject();
                if(!Match(chS, chT)) return false;
            }
        }

        return true;
    }
}
