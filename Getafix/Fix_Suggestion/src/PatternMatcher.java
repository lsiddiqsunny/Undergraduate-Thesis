import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class PatternMatcher {
    HashMap<String, String> holeMapping;
    ArrayList<EditPattern> clusters;
    int holeCount;
    int unmapped_hole_count;
    boolean isAfter;
    int un_resolved_holes;
    int hole_impact;

    Gson gson;


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
        JsonObject beforePattern = antiUnify(beforeTree1, beforeTree2);
        isAfter = true;
        unmapped_hole_count = 0;
        //JsonObject afterPattern = antiUnify(afterTree1, afterTree2);

        EditPattern ep = new EditPattern(beforePattern, afterTree1);
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

    public JsonObject antiUnify(JsonObject ob1, JsonObject ob2)
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
                children.add(antiUnify(ch1, ch2));
            }
        }

        else {
            String hole = getHole(ob1, ob2);
            unified.addProperty("label", hole);
            if(type1.equals(type2)) unified.addProperty("type",type1);
            else {
                unified.addProperty("type","?");
                un_resolved_holes++;
            }
        }
        unified.add("children", children);
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
