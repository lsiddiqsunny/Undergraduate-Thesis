import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.HashMap;

public class EditPattern {
    JsonObject beforePattern;
    JsonObject afterPattern;
    HashMap<String, String>  holeMapping;
    EditPattern parent;
    EditPattern child1;
    EditPattern child2;
    int level;
    String id;
    int total_nodes;
    int holes;
    int un_mapped_holes;
    int un_resolved_holes;
    int hole_impact;

    EditPattern(JsonObject root, int id)
    {
        beforePattern = root.getAsJsonObject("before_tree");
        afterPattern = root.getAsJsonObject("after_tree");
        holeMapping = null;
        level = 1;
        this.id = ""+id;
        total_nodes = countTotalNodes(beforePattern);
        total_nodes += countTotalNodes(afterPattern);
        holes = 0;
        un_mapped_holes = 0;
        un_resolved_holes = 0;
        hole_impact = 0;
        parent = null;
        child1 = null;
        child2 = null;
    }

    EditPattern(JsonObject beforePattern, JsonObject afterPattern)
    {
        this.beforePattern = beforePattern;
        this.afterPattern = afterPattern;
        this.holeMapping = null;
        total_nodes = countTotalNodes(beforePattern);
        total_nodes += countTotalNodes(afterPattern);
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

    boolean isBetter(EditPattern other)
    {
        double p1_un_mapped = this.un_mapped_holes*1.0 / this.total_nodes;
        double p2_un_mapped = other.un_mapped_holes*1.0 / other.total_nodes;

        //if(this.un_mapped_holes < other.un_mapped_holes) return true;
        //if(this.un_mapped_holes > other.un_mapped_holes) return false;

        if(p1_un_mapped < p2_un_mapped) return true;
        if(p1_un_mapped > p2_un_mapped) return false;

        if(this.hole_impact < other.hole_impact) return true;
        if(this.hole_impact > other.hole_impact) return false;

        if(this.holes < other.holes) return true;
        if(this.holes > other.holes) return false;

        if(this.un_resolved_holes < other.holes) return true;
        if(this.un_resolved_holes > other.holes) return false;

        if(this.level < other.level) return true;
        if(this.level > other.level) return false;

        if(this.total_nodes > other.total_nodes) return true;
        if(this.total_nodes < other.total_nodes) return false;
        return false;
    }


    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Before Pattern:\n");
        sb.append(this.beforePattern.toString());
        sb.append("\n");
        sb.append("After Pattern:\n");
        sb.append(this.afterPattern.toString());
        sb.append("\n");
        sb.append("Pattern ID:\n");
        sb.append(this.id);
        sb.append("\n\n");
        return sb.toString();
    }


    public void Print()
    {
        System.out.println(this.toString());
    }

    public void deepPrint()
    {
        System.out.println(this.toString());
        if(this.child1 != null) this.child1.deepPrint();
        if(this.child2 != null) this.child2.deepPrint();
    }

    public JsonObject convert2Json()
    {
        JsonObject obj = new JsonObject();
        obj.add("before_pattern",this.beforePattern);
        obj.add("after_pattern",this.afterPattern);
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
