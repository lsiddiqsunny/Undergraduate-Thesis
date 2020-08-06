import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gumtree.spoon.AstComparator;
import gumtree.spoon.builder.Json4SpoonGenerator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import org.apache.commons.io.FileUtils;
import spoon.reflect.declaration.CtElement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AstBuilder {
    File file;
    ArrayList<Integer> lineNum;
    AstBuilder(File file, ArrayList<Integer> lineNum){
        this.file = file;
        this.lineNum = lineNum;
    }
    String getTargetAst() throws Exception {
        List<CtElement> before = new ArrayList<>();
        List<CtElement> after = new ArrayList<>();
        for(Integer x: lineNum){
            String line = (String) FileUtils.readLines(file).get(x-1);
            System.out.println(line);
            Diff result = new AstComparator().compare("class dummy{void f(){"+line+"}}" ,"class dummy{void f(){}}");
            final List<Operation> allOps = result.getRootOperations();


            for(Operation op: allOps) {
                before.add(op.getSrcNode());
            }
        }

        return convert2Json(before,after);
    }
    public  String convert2Json(List<CtElement> before, List<CtElement> after) throws Exception {
        Json4SpoonGenerator jsonGen = new Json4SpoonGenerator();

        JsonObject root = new JsonObject();
        JsonArray beforeEdits = new JsonArray();
        JsonArray afterEdits = new JsonArray();
        JsonObject beforeTree = new JsonObject();
        JsonObject afterTree = new JsonObject();
        String beforeCode = "";
        String afterCode = "";

        beforeTree.addProperty("label", "edit_root");
        afterTree.addProperty("label", "edit_root");

        beforeTree.addProperty("type", "edit_unit");
        afterTree.addProperty("type", "edit_unit");

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
        root.add("before_tree",beforeTree);
        root.add("after_tree",afterTree);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonStr = gson.toJson(root);

        return jsonStr;
    }

}
