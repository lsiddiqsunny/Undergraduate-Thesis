import com.google.gson.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class Fixer {
    public static String toPrettyFormat(String jsonString)
    {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }


    static ArrayList<FixerEditPattern> patterns;
    static ArrayList<ReplaceSuggestion> replaceSuggestions;
    public static void fixer(List<Integer> test_list)  {

        int total = 0;
        int count=  0;
        Collections.sort(test_list);
        for(int i: test_list) {

            String filename = "target\\dendogram.json";
            Gson gson = new Gson();
            JsonObject jsonObject = null;

            try {
                total++;
                jsonObject = gson.fromJson(new FileReader("Dataset\\"+i+"\\editTree.json"), JsonObject.class);
            } catch (FileNotFoundException e) {
                System.out.println("File not found");
                continue;
            }
            System.out.println("for "+i+":");

            FixerEditPattern target = new FixerEditPattern(jsonObject.getAsJsonObject("before_tree"), jsonObject.getAsJsonObject("after_tree"),jsonObject.get("before_code").getAsString());
            try {

                    JsonObject root = gson.fromJson(new FileReader(filename), JsonObject.class);
                    patterns = new ArrayList<>();
                    getEditPatterns(root, target);

                    JsonObject obj = new JsonObject();
                    obj.addProperty("target", target.beforeCode);
                    int now=0;
                    if(patterns.size()==0){
                        System.out.println("no solution");
                    }
                    for (int j = 0; j < patterns.size(); j++) {
                        //System.out.println("From suggestion "+j+":("+patterns.get(j).root.get("pattern_id").getAsString()+")");

                        PatternMatcher pm = new PatternMatcher();
                        replaceSuggestions = new ArrayList<>();
                        pm.antiUnify2(target.beforePattern,patterns.get(j).beforePattern,true);

                        String afterCode = patterns.get(j).afterCode;
                        System.out.println("After code:\n"+afterCode);
                        for(ReplaceSuggestion replaceSuggestion: replaceSuggestions){
                            String before = replaceSuggestion.before.getAsString();
                            String after = replaceSuggestion.after.getAsString();

                            System.out.println("After: "+after+" Before: "+before);
                            before = before.replaceAll("[*]", "[*]");
                            //before = before.replaceAll("\\[", "");
                            //before = before.replaceAll("\\]", "");
                            before = before.replaceAll("[{]", "");
                            before = before.replaceAll("[}]", "");
                            before = before.replace("\\", "");
                            after = after.replaceAll("[{]", "");
                            after = after.replaceAll("[}]", "");
                            after = after.replace("\\", "");
                            //after = after.replace("\\", "");
                            //before = before.replaceAll("[']", "");
                            //after = after.replaceAll("[']", "");
                            if(before.charAt(0)=='\''){
                                before = before.substring(1,before.length()-1);
                            }
                            if(after.charAt(0)=='\''){
                                after = after.substring(1,after.length()-1);
                            }

                            System.out.println("After: "+after+" Before: "+before);

                            try{
                            afterCode=afterCode.replace(before,after);}
                            catch(Exception e){
                                System.out.println("Problem in replacing");

                                continue;
                            }

                        }
                        System.out.println("After code:\n"+afterCode);
                        now++;
                        patterns.get(j).afterCode=afterCode;
                        obj.addProperty("suggestion" + j, patterns.get(j).afterCode);
                    }

                    //System.out.println(toPrettyFormat(obj.toString()));
                    //System.out.println(patterns.size());
                    if(patterns.size()!=0 && now!=0){
                        count++;
                    }
                    if(patterns.size() > 0 && now!=0) {
                        File solution = new File("output\\" + i+".json");
                        FileWriter fileWriter = new FileWriter(solution);
                        fileWriter.write(toPrettyFormat(obj.toString()));
                        fileWriter.flush();
                        fileWriter.close();
                    }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Solution found for: "+count+"\nSolution found rate: "+((count*100.0)/total));
    }

    public static FixerEditPattern getEditPatterns(JsonObject obj,FixerEditPattern target)
    {

        //should have done a null check and return zero otherwise sum of (1+nodes in child(i) for all i )
        JsonArray children = obj.getAsJsonArray("children");
        if (children.size() != 0) {
            for (int i = 0; i < children.size(); i++) {
                if (Match(children.get(i).getAsJsonObject().get("before_pattern").getAsJsonObject(), target.beforePattern)) {
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
//            Set<Map.Entry<String, JsonElement>> entries = obj.entrySet();//will return members of your object
//            for (Map.Entry<String, JsonElement> entry: entries) {
//                System.out.println(entry.getKey());
//            }
            //System.out.println("*** Suggestion:  "+obj.get("pattern_id").toString());
        }

        return new FixerEditPattern(obj,0);
    }

    public static boolean Match(JsonObject suggestionBT, JsonObject targetBT){
        //suggestionBT -> Suggestion Before Tree
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
