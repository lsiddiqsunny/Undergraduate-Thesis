

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.JsonPrinter;
import com.github.javaparser.printer.YamlPrinter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;



import java.io.*;

public class Main {

    private static final String FILE_PATH = "D:\\Thesis\\Undergraduate-Thesis\\Java_parser\\src\\test.java";

    public static void listClasses(File projectDir) {
        new Director((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
            System.out.println(path);
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(ClassOrInterfaceDeclaration n, Object arg) {
                        super.visit(n, arg);
                        System.out.println(" * " + n.getName());
                    }
                }.visit(JavaParser.parse(file), null);
                System.out.println(); // empty line
            } catch (IOException e) {
                new RuntimeException(e);
            }
        }).explore(projectDir);
    }


    public static void listMethodCalls(File projectDir) {
       new Director((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {

            System.out.println(path);
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(MethodCallExpr n, Object arg) {
                        super.visit(n, arg);
                        System.out.println(" [L " + n.getName() + "] " + n);
                    }
                }.visit(JavaParser.parse(file), null);
                System.out.println(); // empty line
            } catch (IOException e) {
                new RuntimeException(e);
            }
        }).explore(projectDir);
    }
    public static String toPrettyFormat(String jsonString)
    {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }


    public static void listMethodDeclaration(File projectDir) throws FileNotFoundException, UnsupportedEncodingException {
        JavaParser.getStaticConfiguration().setAttributeComments(false);

        PrintWriter writer = new PrintWriter("testout.json", "UTF-8");
        new Director((level, path, file) -> path.endsWith(".txt"), (level, path, file) -> {

            System.out.println(path);
            //System.out.println(Strings.repeat("=", path.length()));
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(MethodDeclaration n, Object arg) {
                        super.visit(n, arg);
                      //  System.out.println(n + "\n");
                        YamlPrinter printer = new YamlPrinter(true);
                        JsonPrinter jprinter=new  JsonPrinter(true);

                        // System.out.println(printer.output(n));

                        writer.println(toPrettyFormat(jprinter.output(n)));
                       String data=jprinter.output(n);
//
                      //  System.out.println(data);
                   //   System.out.println(toPrettyFormat(data));



                    }
                }.visit(JavaParser.parse(file), null);
                System.out.println(); // empty line
            } catch (RuntimeException e) {
                new RuntimeException(e);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }).explore(projectDir);
        writer.close();
    }


    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
      //  CompilationUnit cu = StaticJavaParser.parse(new File(FILE_PATH));
      ///  System.out.println(cu);


    // Now comes the inspection code:
        YamlPrinter printer = new YamlPrinter(true);
       // System.out.println(printer.output(cu));

       // List<Comment> comments = cu.getAllContainedComments();
        //System.out.println("******\n"+cu.toString()+"\n******");

       // Iterator iterator = comments.iterator();
        //while(iterator.hasNext()) {
            //System.out.println(iterator.next().toString());
        //}

        File projectDir = new File("D:\\Thesis\\Undergraduate-Thesis\\Java_parser");
        listMethodDeclaration(projectDir);
    }
}
