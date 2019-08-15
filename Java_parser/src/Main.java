

import com.github.javaparser.JavaParser;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
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
    static int i=0;
/// use to list all method in a directory
    public static void listMethodDeclaration(File projectDir) throws FileNotFoundException, UnsupportedEncodingException {
        JavaParser.getStaticConfiguration().setAttributeComments(false);


        String dir="D:\\Thesis\\Mined\\Input_From_Test_Run\\Test_";///change with respect to data folder
        new Director((level, path, file) -> path.endsWith(".java"), (int level, String path, File file) -> {

            System.out.println(path);//print the path
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(MethodDeclaration n, Object arg) {
                        super.visit(n, arg);
                        JsonPrinter jprinter=new  JsonPrinter(true);

                        File directory = new File(dir+Integer.toString(i+1));
                        if (! directory.exists()){
                            directory.mkdir();

                        }


                        PrintWriter writer = null;
                        try {
                            writer = new PrintWriter(dir+Integer.toString(i+1)+"\\ast.json", "UTF-8");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        writer.println(toPrettyFormat(jprinter.output(n)));
                        writer.close();


                        try {
                            writer = new PrintWriter(dir+Integer.toString(i+1)+"\\code.java", "UTF-8");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        writer.println("class Dummy{\n"+n.toString()+"}\n");
                        writer.close();



                        i++;



                    }
                }.visit(JavaParser.parse(file), null);
                System.out.println(); // empty line
            } catch (RuntimeException e) {
                new RuntimeException(e);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }).explore(projectDir);
    }



    public static void parseString(File projectDir) throws FileNotFoundException, UnsupportedEncodingException {
        JavaParser.getStaticConfiguration().setAttributeComments(false);

        PrintWriter writer = new PrintWriter(projectDir+"//String.txt", "UTF-8");

        new Director((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {

            System.out.println(path);
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(AssignExpr n, Object arg) {
                        super.visit(n, arg);

                        n.getValue().getTokenRange().get().forEach(t -> writer.print(t.getText()+" "));
                        writer.println();


                    }
                }.visit(JavaParser.parse(file), null);
               // System.out.println(); // empty line
            } catch (RuntimeException e) {
                new RuntimeException(e);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }).explore(projectDir);


        new Director((level, path, file) -> path.endsWith("test.java"), (level, path, file) -> {

            System.out.println(path);
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(VariableDeclarationExpr n, Object arg) {
                        super.visit(n, arg);


                        for(Node x: n.getChildNodes()){

                           // writer.println(x+" ");

                            x.getTokenRange().get().forEach(t -> writer.print(t.getText()+" "));
                        }
                        writer.println();
                    }
                }.visit(JavaParser.parse(file), null);
              //  System.out.println(); // empty line
            } catch (RuntimeException e) {
                new RuntimeException(e);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }).explore(projectDir);
        writer.close();
    }

    public static void main(String[] args) throws IOException {
   

    //   File projectDir = new File("D:\\Thesis\\Mined\\Input for test run");
      // listMethodDeclaration(projectDir);
      String dir="D:\\Thesis\\Mined\\Input_From_Test_Run\\Test_";
       for(int i=1;i<=21666;i++){// depends on number of file
           parseString(new File(dir+Integer.toString(i)));
        }




    }
}
