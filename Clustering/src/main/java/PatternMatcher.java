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
    int mapped_hole_count;
    boolean isAfter;
    int un_resolved_holes;
    int hole_impact;

    Gson gson;

    PatternMatcher()
    {
        gson = new Gson(); // json object
        holeMapping = null; //holemapping hashmap
        holeCount = 0; // hole count
    }

    public void Init(String dir, int count) throws Exception {
        Gson gson = new Gson(); // create a json object
        clusters = new ArrayList<EditPattern>(); // for keep the cluster ,create an array of editpattern

        System.out.println("Initializing concrete edits.");
        for(int i=0; i<count; i++) {
            String filename = dir + "/change_pair" + (i+1) + ".json";
            try {
                JsonObject root = gson.fromJson(new FileReader(filename), JsonObject.class);
                EditPattern edit = new EditPattern(root, i + 1); // create editpattern object for each code pair
                clusters.add(edit); // add it to the cluster object, initially all at leaf node
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        System.out.println(clusters.size()+" concrete edits initialized.");
        System.out.println();

    }

    public EditPattern cluster()
    {
        ArrayList<EditPattern> chain = new ArrayList<EditPattern>();
        int itr = 0;

        while (clusters.size()> 1){// until merged in one cluster
            itr++;

            if(chain.isEmpty()) chain.add(clusters.get(0)); //if the chain is empty add,the first one
            EditPattern tmp = chain.get(chain.size()-1); // get the last one from the chain

            System.out.println("last:"+tmp.id);

            EditPattern best_neighbor = null;
            EditPattern best_merged = null;

            for(int i=0; i<clusters.size(); i++)
            {
                EditPattern neighbor = clusters.get(i);// take the neighbour
                if(neighbor.equals(tmp))
                {
                    continue;
                }
                else {
                    EditPattern merged = match(tmp, neighbor, false);// try to merged

                    if (best_merged == null || merged.isBetter(best_merged)) {// is it the best merge?
                        best_merged = merged;
                        best_neighbor = neighbor;
                    }
                }
            }

            System.out.println("Best Neighbor:" + best_neighbor.id);

            if(chain.size()==1) chain.add(best_neighbor); // add the best one in the chain
            else if(chain.get(chain.size()-2).equals(best_neighbor))
            {
                EditPattern merged = match(tmp, best_neighbor, true); // merged last two in the chain
                chain.remove(best_neighbor);
                chain.remove(tmp);
                clusters.remove(best_neighbor);
                clusters.remove(tmp);
                clusters.add(merged);

                System.out.println(merged.beforePattern);
                System.out.println(merged.afterPattern);
                System.out.println(merged.id);
                System.out.println();
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
        JsonObject beforePattern = antiUnify(beforeTree1, beforeTree2);
        isAfter = true;
        mapped_hole_count = 0;
        JsonObject afterPattern = antiUnify(afterTree1, afterTree2);

        EditPattern ep = new EditPattern(beforePattern, afterPattern);
        ep.level = Math.max(edit1.level, edit2.level) + 1;
        ep.holes = holeCount;
        ep.un_mapped_holes = holeCount - mapped_hole_count;
        ep.un_resolved_holes = un_resolved_holes;
        ep.hole_impact = hole_impact;
        ep.hole_impact += edit1.hole_impact;
        ep.hole_impact += edit2.hole_impact;

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

    public JsonObject getHole(JsonObject ob1, JsonObject ob2)
    {
        //cant understand this part why node1 and node2
        JsonObject obj = new JsonObject();
        obj.add("node_1",ob1);
        obj.add("node_2",ob2);

        String key = obj.toString();

        if(holeMapping.containsKey(key))
        {
            if(isAfter) mapped_hole_count++;
            return gson.fromJson(holeMapping.get(key), JsonObject.class);
        }
        else
        {
            hole_impact += countTotalNodes(ob1);
            hole_impact += countTotalNodes(ob2);

            JsonObject hole = new JsonObject();
            String hole_id = "hole_"+holeCount;
            holeCount++;
            JsonArray children = new JsonArray();

            hole.addProperty("label", hole_id);

            String type1 = ob1.get("type").getAsString();
            String type2 = ob2.get("type").getAsString();

            if(type1.equals(type2) && !type1.equals("dummy")) //case 2
            {
               hole.addProperty("type", type1);
            }

            else
            {
                hole.addProperty("type", "?");//case 3
                un_resolved_holes++;
            }

            hole.add("children", children);

            holeMapping.put(key, hole.toString());

            return hole;
        }
    }

    public JsonObject antiUnify(JsonObject ob1, JsonObject ob2)
    {
        JsonObject unified;

        String type1 = ob1.get("type").getAsString();
        String type2 = ob2.get("type").getAsString();

        String label1 = ob1.get("label").getAsString();
        String label2 = ob2.get("label").getAsString();


        JsonArray children1 = ob1.getAsJsonArray("children");
        JsonArray children2 = ob2.getAsJsonArray("children");

        if(type1.equals(type2) && label1.equals(label2))// case 1 and 2
        {
            unified = new JsonObject();

            unified.addProperty("label", label1);
            unified.addProperty("type",type1);

            JsonArray children = new JsonArray();

            if(children1.size()==children2.size()) {
                for (int i = 0; i < children1.size(); i++) {
                    JsonObject ch1 = children1.get(i).getAsJsonObject();
                    JsonObject ch2 = children2.get(i).getAsJsonObject();
                    children.add(antiUnify(ch1, ch2));// ci = antiunify(ai,bi) // case 1
                }
            }
            else{
                JsonObject tmp1 = new JsonObject();
                JsonObject tmp2 = new JsonObject();

                tmp1.addProperty("label","dummy");
                tmp2.addProperty("label","dummy");
                tmp1.addProperty("type","dummy");
                tmp2.addProperty("type","dummy");
                tmp1.add("children", children1);
                tmp2.add("children", children2);
                children.add(getHole(tmp1,tmp2));//case 2: get hole
            }
            unified.add("children", children);
        }
        else
        {
            unified = getHole(ob1, ob2);// case 3
        }
        return unified;
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
