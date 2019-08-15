

import com.github.javaparser.JavaParser;
<<<<<<< HEAD

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.JsonPrinter;
=======
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.JsonPrinter;
import com.github.javaparser.printer.YamlPrinter;
>>>>>>> 6bc91c01667b80d78e1998ceef4516381f6f35f8
import com.google.gson.*;


import java.io.*;
<<<<<<< HEAD
=======
import java.util.*;
>>>>>>> 6bc91c01667b80d78e1998ceef4516381f6f35f8

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
<<<<<<< HEAD
    static int i=0;
=======

>>>>>>> 6bc91c01667b80d78e1998ceef4516381f6f35f8
/// use to list all method in a directory
    public static void listMethodDeclaration(File projectDir) throws FileNotFoundException, UnsupportedEncodingException {
        JavaParser.getStaticConfiguration().setAttributeComments(false);

<<<<<<< HEAD

        String dir="D:\\Thesis\\Mined\\Input_From_Test_Run\\Test_";///change with respect to data folder
        new Director((level, path, file) -> path.endsWith(".java"), (int level, String path, File file) -> {
=======
        PrintWriter writer1 = new PrintWriter("outjson.json", "UTF-8");//get all json from the specific directory
        PrintWriter writer2 = new PrintWriter("outjava.java", "UTF-8");// get all java function from the specific directory
        new Director((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
>>>>>>> 6bc91c01667b80d78e1998ceef4516381f6f35f8

            System.out.println(path);//print the path
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(MethodDeclaration n, Object arg) {
                        super.visit(n, arg);
                        JsonPrinter jprinter=new  JsonPrinter(true);
<<<<<<< HEAD

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



=======
                        writer1.println(toPrettyFormat(jprinter.output(n))+"\n\n######\n\n");///dump json in the output json
                        writer2.println("class Dummy{\n"+n.toString()+"\n}\n######\n\n");/// add dummy class to revisit the function
                    }
                }.visit(JavaParser.parse(file), null);
                System.out.println(); // empty line
            } catch (RuntimeException e) {
                new RuntimeException(e);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }).explore(projectDir);
        writer1.close();
        writer2.close();
    }

///use to parse two output file separated by #####
    public static void Parse() throws IOException {

        InputStream is = new FileInputStream("D:\\Thesis\\Undergraduate-Thesis\\Java_parser\\outjson.json");
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();
        while(line != null){ sb.append(line).append("\n"); line = buf.readLine(); }
        String fileAsString = sb.toString();

        String [] data=fileAsString.split("######");


        InputStream is1 = new FileInputStream("D:\\Thesis\\Undergraduate-Thesis\\Java_parser\\outjava.java");
        BufferedReader buf1 = new BufferedReader(new InputStreamReader(is1));
        String line1 = buf1.readLine();
        StringBuilder sb1 = new StringBuilder();
        while(line1 != null){ sb1.append(line1).append("\n"); line1 = buf1.readLine(); }
        String fileAsString1 = sb1.toString();

        String [] data1=fileAsString1.split("######");

        int i=0;
        String dir="D:\\Thesis\\Mined\\Input_From_Test_Run\\Test_";///change with respect to data folder
        String ext=".json";

        for(String s: data){
            File directory = new File(dir+Integer.toString(i+1));
            if (! directory.exists()){
                directory.mkdir();

            }


            PrintWriter writer = new PrintWriter(dir+Integer.toString(i+1)+"\\ast"+ext, "UTF-8");
            writer.println(s);
            writer.close();


            i++;
        }

        i=0;
        String dir1="D:\\Thesis\\Mined\\Input_From_Test_Run\\Test_";
        String ext1=".java";

        for(String s: data1){
            PrintWriter writer = new PrintWriter(dir1+Integer.toString(i+1)+"\\code"+ext1, "UTF-8");
            writer.println(s);
            writer.close();


            i++;
        }




    }


//use to get string from a java function
    public static void listString(File projectDir,String dir) throws FileNotFoundException, UnsupportedEncodingException {
        JavaParser.getStaticConfiguration().setAttributeComments(false);

        PrintWriter writer = new PrintWriter(dir+"//string.txt", "UTF-8");

        new Director((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {

            System.out.println(path);
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(StringLiteralExpr n, Object arg) {
                        super.visit(n, arg);

                        writer.println(n.toString()+"\n\n");
>>>>>>> 6bc91c01667b80d78e1998ceef4516381f6f35f8
                    }
                }.visit(JavaParser.parse(file), null);
                System.out.println(); // empty line
            } catch (RuntimeException e) {
                new RuntimeException(e);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }).explore(projectDir);
<<<<<<< HEAD
    }



=======
        writer.close();
    }

>>>>>>> 6bc91c01667b80d78e1998ceef4516381f6f35f8
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
   

<<<<<<< HEAD
    //   File projectDir = new File("D:\\Thesis\\Mined\\Input for test run");
      // listMethodDeclaration(projectDir);
      String dir="D:\\Thesis\\Mined\\Input_From_Test_Run\\Test_";
       for(int i=1;i<=21666;i++){// depends on number of file
           parseString(new File(dir+Integer.toString(i)));
        }

=======
       File projectDir = new File("D:\\Thesis\\Mined\\Input for test run");
       listMethodDeclaration(projectDir);
       Parse();
//      JsonParsing();
      String dir="D:\\Thesis\\Mined\\Input_From_Test_Run\\Test_";
       for(int i=1;i<=8014;i++){// depends on number of file
           parseString(new File(dir+Integer.toString(i)));
        }
      //File stringDir=new File("D:\\Thesis\\Undergraduate-Thesis\\Java_parser");
     // parseString(stringDir);
      //  listMethodDeclaration(stringDir);
>>>>>>> 6bc91c01667b80d78e1998ceef4516381f6f35f8



    }
}
