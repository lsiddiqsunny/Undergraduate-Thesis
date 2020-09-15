import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PatternMatcher {
    HashMap<String, String> holeMapping;
    ArrayList<EditPattern> clusters;
    int holeCount;
    int unmapped_hole_count;
    boolean isAfter;
    int un_resolved_holes;
    int hole_impact;

    Gson gson;

    PatternMatcher()
    {
        gson = new Gson();
        holeMapping = new HashMap<String, String>();
        holeCount = 0;
        un_resolved_holes = 0;
        hole_impact = 0;
    }

    public void Init(String dir, int count,List<Integer> test_list ) {
        Gson gson = new Gson();
        clusters = new ArrayList<>();

        System.out.println("Initializing concrete edits.");

        for(int i=1; i<=count; i++) {
            if(test_list.contains(i)){
                //continue;
            }
            String filename = dir + "/" + i + "/editTree.json";

            JsonObject root;
            try {
                root = gson.fromJson(new FileReader(filename), JsonObject.class);
            } catch (FileNotFoundException e) {
                System.out.println(i+"th File not found");
                continue;
            }

            String beforeCode = root.get("before_code").getAsString();
            if(beforeCode.length()==0){
                System.out.println(i+": Missing value in dataset");
                continue;
            }

            EditPattern edit = new EditPattern(root, i);
            clusters.add(edit);
        }

        System.out.println(clusters.size()+" concrete edits initialized.");
        System.out.println();
        //EditPattern ep = clusters.get(0);
        //EditPattern ep2 = clusters.get(1);
        //cluster();
    }

    public EditPattern cluster()  //Nearest neighbor chaining used
    {
        ArrayList<EditPattern> chain = new ArrayList<EditPattern>();

        chain.add(clusters.get(0));

        while (clusters.size()> 1){

            //if(chain.isEmpty()) chain.add(clusters.get(0));
            System.out.println("chain size: "+chain.size());
            EditPattern tmp = chain.get(chain.size()-1);
            System.out.println("last:"+tmp.id);

            EditPattern best_neighbor = null;
            EditPattern best_merged = null;

            for(int i=0; i<clusters.size(); i++)
            {
                EditPattern neighbor = clusters.get(i);
                if(neighbor.equals(tmp))
                {
                    continue;
                }
                else {
                    EditPattern merged = match(tmp, neighbor, true);

                    if (best_merged == null || merged.isBetter(best_merged)) {
                        best_merged = merged;
                        best_neighbor = neighbor;
                    }
                }
            }

            System.out.println("Best Neighbor:" + best_neighbor.id);

            if(chain.size()==1) chain.add(best_neighbor);
            else if(chain.get(chain.size()-2).equals(best_neighbor))
            {
                EditPattern merged = match(tmp, best_neighbor, true);

//                System.out.println("b1: "+tmp.beforePattern);
//                System.out.println("b2: "+best_neighbor.beforePattern);
//                System.out.println("mb: "+merged.beforePattern+"\n");
//
//                System.out.println("a1: "+tmp.afterPattern);
//                System.out.println("a2: "+best_neighbor.afterPattern);
//                System.out.println("ma: "+merged.afterPattern);
                System.out.println(merged.id +"  Score:  "+merged.node_count_B+"  "+(merged.holes-unmapped_hole_count));
                System.out.println();

                chain.remove(best_neighbor);
                chain.remove(tmp);
                chain.add(merged);
                clusters.remove(best_neighbor);
                clusters.remove(tmp);
                clusters.add(merged);
            }
            else chain.add(best_neighbor);

        }
        if(clusters.size()==1) return clusters.get(0);
        return null;
    }

    public EditPattern match(EditPattern edit1, EditPattern edit2, boolean merge)
    {
        holeMapping = new HashMap<String, String>();
        holeCount = 0;
        un_resolved_holes = 0;
        hole_impact = 0;

        JsonObject beforeTree1 = edit1.beforePattern;
        JsonObject afterTree1 = edit1.afterPattern;

        JsonObject beforeTree2 = edit2.beforePattern;
        JsonObject afterTree2 = edit2.afterPattern;

        isAfter = false;
        JsonObject beforePattern = antiUnify(beforeTree1, beforeTree2,false);
        isAfter = true;
        unmapped_hole_count = 0;
        JsonObject afterPattern = antiUnify(afterTree1, afterTree2,false);

        EditPattern ep = new EditPattern(beforePattern, afterPattern);
        ep.level = Math.max(edit1.level, edit2.level) + 1;
        ep.holes = holeCount;
        ep.un_mapped_holes = unmapped_hole_count;
        ep.un_resolved_holes = un_resolved_holes;
        ep.node_count_B = countTotalNodes(beforePattern);
        ep.node_count_A = countTotalNodes(beforePattern);
        //ep.hole_impact += edit1.hole_impact;
        //ep.hole_impact += edit2.hole_impact;

        if(merge)
        {
            edit1.parent = ep;
            edit2.parent = ep;
            ep.parent = null;
            ep.child1 = edit1;
            ep.child2 = edit2;
            ep.id = edit1.id +","+edit2.id;
        }


        /*for(String key: holeMapping.keySet())
        {
            System.out.println(holeMapping.get(key));
            System.out.println(key);
        }*/

        return ep;
    }

    public JsonObject antiUnify(JsonObject ob1, JsonObject ob2,boolean isAdd)
    {
        JsonObject unified = new JsonObject();
        JsonArray children = new JsonArray();

        String type1 = ob1.get("type").getAsString();
        String type2 = ob2.get("type").getAsString();

        String label1 = ob1.get("label").getAsString();
        String label2 = ob2.get("label").getAsString();

        //System.out.println("1***Type: "+type1 + "  Label: " + label1);
        //System.out.println("2***Type: "+type2 + "  Label: " + label2);


        JsonArray children1 = ob1.getAsJsonArray("children");
        JsonArray children2 = ob2.getAsJsonArray("children");

        //System.out.println("***1***Size: "+children1.size() + " Child: "+children1.toString());
        //System.out.println("***2***Size: "+children2.size() + " Child: "+children2.toString());

        if(type1.equals(type2) && label1.equals(label2) && children1.size()==children2.size()) {

            unified.addProperty("label", label1);
            unified.addProperty("type",type1);


            for (int i = 0; i < children1.size(); i++) {
                JsonObject ch1 = children1.get(i).getAsJsonObject();
                JsonObject ch2 = children2.get(i).getAsJsonObject();
                children.add(antiUnify(ch1, ch2,isAdd));
            }
        }

        else {
            String hole = getHole(ob1, ob2);
            unified.addProperty("label", hole);
            ReplaceSuggestion replaceSuggestion = new ReplaceSuggestion(ob1.get("label"),ob2.get("label"));
            if(isAdd)
            Fixer.replaceSuggestions.add(replaceSuggestion);
           //System.out.println("hole: "+hole+"  target: "+ob1.get("label")+" source: "+ob2.get("label"));

            if(type1.equals(type2)) unified.addProperty("type",type1);
            else {
                unified.addProperty("type","?");
                un_resolved_holes++;
            }
        }
        unified.add("children", children);
        //System.out.println(unified.toString());
        return unified;
    }

    public String getHole(JsonObject ob1, JsonObject ob2)
    {
        String label1 = ob1.get("label").toString()+"1";
        String label2 = ob2.get("label").toString()+"2";

        if(holeMapping.containsKey(label1+label2))
        {
            return holeMapping.get(label1+label2);
        }
        else
        {
            if(isAfter) unmapped_hole_count++;
            if(!isAfter) {
                hole_impact += countTotalNodes(ob1);
                hole_impact += countTotalNodes(ob2);
            }

            String hole = "hole_"+holeCount;
            holeCount++;

            holeMapping.put(label1+label2,hole);

            return hole;

            /*String type1 = ob1.get("type").getAsString();
            String type2 = ob2.get("type").getAsString();

            if(type1.equals(type2)) //&& !type1.equals("dummy")
            {
                hole.addProperty("type", type1);
            }

            else
            {
                hole.addProperty("type", "?");
                un_resolved_holes++;
            }

            JsonArray children = new JsonArray();
            hole.add("children", children);  //Children is empty

            holeMapping.put(key, hole);

            return hole;*/
        }
    }

    public int countTotalNodes(JsonObject obj)
    {
        int count = 1;

        JsonArray children = obj.getAsJsonArray("children");

        for(int i=0; i<children.size(); i++)
        {
            count = count + countTotalNodes(children.get(i).getAsJsonObject());
        }

        return count;
    }
}
