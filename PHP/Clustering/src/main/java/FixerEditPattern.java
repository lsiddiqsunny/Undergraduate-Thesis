import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;

public class FixerEditPattern {
    JsonObject root;
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
        this.root=root;

        beforePattern = root.getAsJsonObject("before_pattern");
        afterPattern = root.getAsJsonObject("after_pattern");
        beforeCode = root.get("before_code")!=null?root.get("before_code").getAsString():null;
        afterCode = root.get("after_code")!=null?root.get("after_code").getAsString():null;
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

    FixerEditPattern(JsonObject beforePattern, JsonObject afterPattern, String beforeCode)
    {
        this.beforePattern = beforePattern;
        this.afterPattern = afterPattern;
        this.beforeCode = beforeCode;
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



}
