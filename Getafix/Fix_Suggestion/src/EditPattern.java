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
    int node_count_B;
    int node_count_A;

    EditPattern(JsonObject root, int id)
    {
        beforePattern = root.getAsJsonObject("before_pattern");
        afterPattern = root.getAsJsonObject("after_pattern");
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

    EditPattern(JsonObject beforePattern, JsonObject afterPattern)
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

    boolean isBetter(EditPattern other) //Focused on Before Trees only
    {
        //(holes - un_mapped_holes) ---> number of holes in marge of before trees
        //node_count_B ---> number of nodes in marge of before trees
        //node_count_A ---> number of nodes in marge of after trees

        double lengthRatio1 = (this.holes-this.un_mapped_holes)*10 + this.node_count_B*10 + this.node_count_A ;
        double lengthRatio2 = (other.holes-other.un_mapped_holes)*10 + other.node_count_B*10 + other.node_count_A;

        if(lengthRatio1 > lengthRatio2) return true;
        if(lengthRatio1 < lengthRatio2) return false;

        if(this.node_count_B > other.node_count_B) return true;
        if(this.node_count_B < other.node_count_B) return false;

        if(this.holes-this.un_mapped_holes > other.holes-other.un_mapped_holes) return true;
        if(this.holes-this.un_mapped_holes < other.holes-other.un_mapped_holes) return false;

        if(this.holes > other.holes) return true;
        if(this.holes < other.holes) return false;

        /// These are codes from Towfiq vai

        double p1_un_mapped = this.un_mapped_holes*1.0 / this.total_nodes;
        double p2_un_mapped = other.un_mapped_holes*1.0 / other.total_nodes;

        if(p1_un_mapped < p2_un_mapped) return true;
        if(p1_un_mapped > p2_un_mapped) return false;

        if(this.un_resolved_holes < other.holes) return true;
        if(this.un_resolved_holes > other.holes) return false;

        //if(this.hole_impact < other.hole_impact) return true;
        //if(this.hole_impact > other.hole_impact) return false;

        if(this.level < other.level) return true;
        if(this.level > other.level) return false;

        if(this.total_nodes > other.total_nodes) return true;
        return false;
    }


    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Before Pattern:\n");
        sb.append(Main.toPrettyFormat(this.beforePattern.toString()));
        sb.append("\n");
        sb.append("After Pattern:\n");
        sb.append(Main.toPrettyFormat(this.afterPattern.toString()));
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
