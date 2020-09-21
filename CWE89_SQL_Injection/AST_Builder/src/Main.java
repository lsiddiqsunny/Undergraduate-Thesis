import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gumtree.spoon.AstComparator;
import gumtree.spoon.builder.Json4SpoonGenerator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.DeleteOperation;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.declaration.CtElement;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void   convert2Json(List<CtElement> before, List<CtElement> after, List<Integer>beforeLine,
                                      List<Integer>afterLine,String path) throws Exception {
        Json4SpoonGenerator jsonGen = new Json4SpoonGenerator();

        JsonObject root = new JsonObject();
        JsonArray beforeEdits = new JsonArray();
        JsonArray afterEdits = new JsonArray();
        JsonArray beforeLines = new JsonArray();
        JsonArray afterLines = new JsonArray();
        JsonObject beforeTree = new JsonObject();
        JsonObject afterTree = new JsonObject();
        String beforeCode = "";
        String afterCode = "";

        beforeTree.addProperty("label", "edit_root");
        afterTree.addProperty("label", "edit_root");

        beforeTree.addProperty("type", "edit_unit");
        afterTree.addProperty("type", "edit_unit");
        for(int x: beforeLine){
            beforeLines.add(x);
        }
        for(int x: afterLine){
            afterLines.add(x);
        }

        for(CtElement ct: before)
        {
            JsonObject ctJson = jsonGen.getJSONasJsonObject(ct);

            if(ctJson.getAsJsonArray("children").size() == 1)
            {
                JsonObject child = ctJson.getAsJsonArray("children").get(0).getAsJsonObject();
                beforeEdits.add(child);
            }
            else
            {
                beforeEdits.add(ctJson);
                System.out.println("Anomaly-----------?");
            }


            beforeCode += ct.toString();
            beforeCode += "\n";

        }

        for(CtElement ct: after)
        {
            JsonObject ctJson = jsonGen.getJSONasJsonObject(ct);

            if(ctJson.getAsJsonArray("children").size() == 1)
            {
                JsonObject child = ctJson.getAsJsonArray("children").get(0).getAsJsonObject();
                afterEdits.add(child);
            }
            else
            {
                afterEdits.add(ctJson);
                System.out.println("Anomaly-----------?");
            }

            afterCode += ct.toString();
            afterCode += "\n";
        }


        beforeTree.add("children", beforeEdits);
        afterTree.add("children", afterEdits);

        root.addProperty("before_code",beforeCode);
        root.addProperty("after_code", afterCode);
        root.add("before_line",beforeLines);
        root.add("after_line",afterLines);
        root.add("before_tree",beforeTree);
        root.add("after_tree",afterTree);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonStr = gson.toJson(root);

        File jsonFile = new File(path+".json");
        jsonFile.createNewFile();
        FileWriter writer = new FileWriter(jsonFile);
        writer.write(jsonStr);
        writer.flush();
        writer.close();
        return;
    }
    public static void main(String[] args) throws Exception {
        int total = 0;
        for(int i=1;i<=4;i++){
            String[] pathnames;

            File f = new File("D:\\Thesis\\CWE89_SQL_Injection\\Target\\s0"+i+"\\");
            pathnames = f.list();
            if(pathnames==null){
                continue;
            }
            for(String pathname:pathnames){

                String basepath = "D:\\Thesis\\CWE89_SQL_Injection\\Target\\s0"+i+"\\"+pathname+"\\target.txt";


                            InputStream is = new FileInputStream(basepath);

                            BufferedReader buf = new BufferedReader(new InputStreamReader(is));

                            String line = buf.readLine();

                            String lines = "";
                            while(line != null){
                                lines+=line;

                                line = buf.readLine();
                            }
                            if(lines.length()==0){
                                continue;
                            }
                            String[] targetLine = lines.split("##########");
                            int co=1;
                            for(String target:targetLine){
                                List<CtElement> ctElements = new ArrayList<>();

                                for(String x: target.split("#")){
                                    if(x.length()==0){
                                        continue;
                                    }
                                    if(x.contains("\"")){
                                        FileWriter fr = new FileWriter(new File("D:\\Thesis\\CWE89_SQL_Injection\\Target\\s0"+i+"\\"+pathname+"\\String"+co+".txt"));
                                        fr.write(x);
                                        fr.close();
                                    }
                                    //System.out.println(x.trim());
                                    Diff diff =  new AstComparator().compare("class dummy{void f(){"+x.trim()+"}}", "class dummy{void f(){}}");
                                    try {
                                        ctElements.add(diff.getAllOperations().get(diff.getAllOperations().size() - 1).getSrcNode());
                                    }catch (Exception e){
                                        e.printStackTrace();
                                        continue;
                                    }

                                }
                                //System.out.println(ctElements.size());
                                if(ctElements.size()==0){
                                    continue;
                                }

                                total++;
                                System.out.println("done: "+pathname+" "+co);
                               convert2Json(ctElements, new ArrayList<>(),new ArrayList<>(), new ArrayList<>(),"D:\\Thesis\\CWE89_SQL_Injection\\Target\\s0"+i+"\\"+pathname+"\\"+co);
                                co++;
                            }
                            //break;
                            /*
                            */
                        }
                    }
                System.out.println("total: "+total);
            }
        }





