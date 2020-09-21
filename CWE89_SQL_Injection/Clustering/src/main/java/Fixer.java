import com.google.gson.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class Fixer {
    public static String toPrettyFormat(String jsonString) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

    static ArrayList<FixerEditPattern> patterns;
    static ArrayList<ReplaceSuggestion> replaceSuggestions;

    public static void fixer(List<Integer> test_list) throws IOException {
        String filename = "target\\dendogram.json";
        int total = 0;
        int found = 0;
        for(int l=1;l<=4;l++) {
            String directory = "D:\\Thesis\\CWE89_SQL_Injection\\Target\\s0"+l+"\\";
            File directoryPath = new File(directory);
            String[] contents = directoryPath.list();
            System.out.println("List of files and directories in the specified directory:");

            for (int i = 0; i < contents.length; i++) {
                File file = new File(directory + contents[i]);
                if (file.isDirectory()) {
                    String[] fileContents = file.list();
                    int j = 0;
                    //System.out.println(file.getName());
                    boolean ok = false;
                    for (String fileName : fileContents) {
                        if (fileName.contains("_") && fileName.contains(".sql.json")) {
                            ok = true;
                            break;
                        }
                    }
                    if (!ok)
                        continue;
                    for (String fileName : fileContents) {
                        if (fileName.contains(".json") && !fileName.contains(".sql.json")) {
                            total++;
                            System.out.println(directory + contents[i] +"\\"+ fileName);
                            Gson gson = new Gson();
                            JsonObject jsonObject;
                            try {
                                jsonObject = gson.fromJson(new FileReader(directory + contents[i] + "\\" + fileName), JsonObject.class);
                            } catch (FileNotFoundException e) {
                                System.out.println("File not found");
                                continue;
                            }
                            FixerEditPattern target = new FixerEditPattern(jsonObject.getAsJsonObject("before_tree"), jsonObject.getAsJsonObject("after_tree"));
                            JsonObject root = null;
                            try {
                                root = gson.fromJson(new FileReader(filename), JsonObject.class);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            patterns = new ArrayList<>();
                            getEditPatterns(root, target);

                            JsonObject obj = new JsonObject();
                            obj.add("target", target.beforePattern);
                            System.out.println(patterns.size());
                            for (int k = 0; k < patterns.size(); k++) {

                                PatternMatcher pm = new PatternMatcher();
                                replaceSuggestions = new ArrayList<>();
                                pm.antiUnify(target.beforePattern, patterns.get(k).beforePattern, true);

                                String afterString = patterns.get(k).afterPattern.toString();
                                String afterCode = patterns.get(k).afterCode;

                                for (ReplaceSuggestion replaceSuggestion : replaceSuggestions) {
                                    afterString = afterString.replace(replaceSuggestion.before.toString().replace("\"", ""), replaceSuggestion.after.toString().replace("\"", ""));
                                    afterCode = afterCode.replace(replaceSuggestion.before.toString().replace("\"", ""), replaceSuggestion.after.toString().replace("\"", ""));

                                }

                                JsonParser jsonParser = new JsonParser();
                                patterns.get(j).afterPattern = (JsonObject) jsonParser.parse(afterString);
                                patterns.get(j).afterCode = afterCode;
                                obj.add("suggestion" + j, patterns.get(j).convert2Json());
                            }
                            if (patterns.size() != 0) {
                                found++;
                            }
                        File solution = new File("output\\s0"+l+"\\"+contents[i]+ fileName+".json");
                        FileWriter fileWriter = new FileWriter(solution);
                            fileWriter.write(toPrettyFormat(obj.toString()));
                            fileWriter.flush();
                            fileWriter.close();
//                        try {
//                            fileWriter = ;
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        try {
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        try {
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        try {
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }

                        }

                        //break;
                    }
                    //break;
                }
            }
        }
        System.out.println(total + " " + found+ " "+(found*100.0/total));

    }

    public static FixerEditPattern getEditPatterns(JsonObject obj, FixerEditPattern target) {

        //should have done a null check and return zero otherwise sum of (1+nodes in child(i) for all i )
        JsonArray children = obj.getAsJsonArray("children");
        if (children.size() != 0) {
            for (int i = 0; i < children.size(); i++) {
                if (Match(children.get(i).getAsJsonObject().get("before_pattern").getAsJsonObject(), target.beforePattern)) {
                    //System.out.println("**Match:"+children.get(i).getAsJsonObject().get("before_pattern").toString());

                    getEditPatterns(children.get(i).getAsJsonObject(), target);
                }
//                else {
//                    //System.out.println("noMatch"+children.get(i).getAsJsonObject().get("before_pattern").toString());
//                }
            }
        }
        if (children.size() == 0) {
            patterns.add(new FixerEditPattern(obj, 0));

        }

        return new FixerEditPattern(obj, 0);
    }

    public static boolean Match(JsonObject suggestionBT, JsonObject targetBT) {
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

        if (!typeS.equals("?") && !typeS.equals(typeT)) return false;

        if (labelS.contains("hole_")) return true;

        if (childrenS.size() != childrenT.size()) return false;
        else {
            for (int i = 0; i < childrenS.size(); i++) {
                JsonObject chS = childrenS.get(i).getAsJsonObject();
                JsonObject chT = childrenT.get(i).getAsJsonObject();
                if (!Match(chS, chT)) return false;
            }
        }

        return true;
    }
}
