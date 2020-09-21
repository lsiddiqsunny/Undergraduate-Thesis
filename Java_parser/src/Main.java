
import com.github.javaparser.JavaParser;


import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


import com.github.javaparser.ast.expr.StringLiteralExpr;


import com.github.javaparser.printer.JsonPrinter;
import com.google.gson.*;


import java.io.*;


public class Main {

/// use to format json type file
    public static String toPrettyFormat(String jsonString)
    {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

public static void Parser(File file){
        System.out.println("Variable declaration:");
    try {
        new VoidVisitorAdapter<Object>() {
            @Override

            public void visit(VariableDeclarator n, Object arg) {
                super.visit(n, arg);

                if(n.getInitializer().isPresent()){
                    System.out.println(n.getInitializer().get());
                }


            }
        }.visit(JavaParser.parse(file), null);
        // System.out.println(); // empty line
    } catch (RuntimeException e) {
        new RuntimeException(e);
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }
    System.out.println("Assignment:");
    try {
        new VoidVisitorAdapter<Object>() {
            @Override

            public void visit(AssignExpr n, Object arg) {
                super.visit(n, arg);

                    System.out.println(n.getValue());



            }
        }.visit(JavaParser.parse(file), null);
        // System.out.println(); // empty line
    } catch (RuntimeException e) {
        new RuntimeException(e);
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }
    System.out.println("Method call:");
    try {
        new VoidVisitorAdapter<Object>() {
            @Override

            public void visit(MethodCallExpr n, Object arg) {
                super.visit(n, arg);
                if(n.getArguments().isNonEmpty())
                System.out.println(n.getArguments());



            }
        }.visit(JavaParser.parse(file), null);
        // System.out.println(); // empty line
    } catch (RuntimeException e) {
        new RuntimeException(e);
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }
}
    public static void main(String[] args) throws IOException {
        File file = new File("D:\\Thesis\\Undergraduate-Thesis\\Java_parser\\Data\\before_1.java");
        Parser(file);
    }
}
