import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;

public class FixerEditPattern {

    JsonObject beforePattern;
    JsonObject afterPattern;
    String beforeCode, afterCode;
    HashMap<String, String>  holeMapping;
    FixerEditPattern parent;
    FixerEditPattern child1;
    FixerEditPattern child2;
    int level;
    String id;
    int total_nodes;
    int holes;
    int un_mapped_holes;
    int un_resolved_holes;
    int node_count_B;
    int node_count_A;

    FixerEditPattern(JsonObject root, int id)
    {

        beforePattern = root.getAsJsonObject("before_pattern");
        afterPattern = root.getAsJsonObject("after_pattern");
        beforeCode = root.get("before_code")!=null?root.get("before_code").toString():null;
        afterCode = root.get("after_code")!=null?root.get("after_code").toString():null;
        holeMapping = null;
        level = 1;
        this.id = ""+id;
        total_nodes = countTotalNodes(beforePattern);
        //total_nodes += countTotalNodes(afterPattern);
        holes = 0;
        un_mapped_holes = 0;
        un_resolved_holes = 0;
        node_count_B = node_count_A = 0;
        parent = null;
        child1 = null;
        child2 = null;
    }

    FixerEditPattern(JsonObject beforePattern, JsonObject afterPattern)
    {
        this.beforePattern = beforePattern;
        this.afterPattern = afterPattern;
        this.holeMapping = null;
        total_nodes = countTotalNodes(beforePattern);
        //total_nodes += countTotalNodes(afterPattern);
        holes = 0;
        un_mapped_holes = 0;
        un_resolved_holes = 0;
        node_count_B = node_count_A = 0;
        parent = null;
        child1 = null;
        child2 = null;
    }

    public JsonObject getBeforePattern(){
        return beforePattern;
    }
    public JsonObject getAfterPattern(){
        return afterPattern;
    }
    public int countTotalNodes(JsonObject obj)
    {
        int count = 1;
        //shoud have done a null check and return zero otherwise sum of (1+nodes in child(i) for all i )
        JsonArray children = obj.getAsJsonArray("children");
        for(int i=0; i<children.size(); i++)
        {
            count = count + countTotalNodes(children.get(i).getAsJsonObject());
        }

        return count;
    }


    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Before Pattern:\n");
        sb.append(Fixer.toPrettyFormat(this.beforePattern.toString()));
        sb.append("\n");
        sb.append("After Pattern:\n");
        sb.append(Fixer.toPrettyFormat(this.afterPattern.toString()));
        sb.append("\n");
        sb.append("Pattern ID:\n");
        sb.append(this.id);
        sb.append("\n\n");
        return sb.toString();
    }

    public JsonObject convert2Json()
    {
        JsonObject obj = new JsonObject();
        obj.add("before_pattern",this.beforePattern);
        obj.add("after_pattern",this.afterPattern);
        obj.addProperty("before_code",this.beforeCode);
        //System.out.println(this.beforeCode);
        obj.addProperty("after_code",this.afterCode);
        obj.addProperty("pattern_id",this.id);
        JsonArray children = new JsonArray();

        if(this.child1 != null)
        {
            children.add(this.child1.convert2Json());
        }
        if(this.child2 != null)
        {
            children.add(this.child2.convert2Json());
        }

        obj.add("children",children);
        return obj;
    }

}
