
import com.github.javaparser.JavaParser;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
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

    static int flag;

/// use to list all method in a directory
    public static void listMethodDeclaration(File projectDir) throws FileNotFoundException, UnsupportedEncodingException {
        JavaParser.getStaticConfiguration().setAttributeComments(false);

        new Director((level, path, file) -> path.endsWith(".java"), (int level, String path, File file) -> {


         //   System.out.println(path);//print the path
            if(path.contains("code")){
                flag=0;
            }
            else flag=1;
            System.out.println(path+" "+flag);
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(MethodDeclaration n, Object arg) {
                        super.visit(n, arg);
                        JsonPrinter jprinter=new JsonPrinter(true);





                        PrintWriter writer = null;
                        try {
                            if(flag==0)
                            writer = new PrintWriter(projectDir.toString()+"\\ast.json", "UTF-8");
                            else writer = new PrintWriter(projectDir.toString()+"\\solution_ast.json", "UTF-8");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        writer.println(toPrettyFormat(jprinter.output(n)));
                        writer.close();


                        try {
                            if(flag==0)
                            writer = new PrintWriter(projectDir+"\\code.java", "UTF-8");
                            else writer = new PrintWriter(projectDir+"\\solution.java", "UTF-8");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        writer.println("class Dummy{\n"+n.toString()+"}\n");
                        writer.close();






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

    public static void parseString(File projectDir) throws FileNotFoundException, UnsupportedEncodingException {
        JavaParser.getStaticConfiguration().setAttributeComments(false);


        new Director((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {

            if(path.contains("code")){
                flag=0;
            }
            else flag=1;
            System.out.println(path+" "+flag);
            PrintWriter writer;
            if(flag==0)
                writer= new PrintWriter(projectDir+"//String.txt", "UTF-8");
            else writer= new PrintWriter(projectDir+"//solution_String.txt", "UTF-8");


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
                writer.close();
            } catch (RuntimeException e) {
                new RuntimeException(e);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }).explore(projectDir);



        new Director((level, path, file) -> path.endsWith("java"), (level, path, file) -> {

            if(path.contains("code")){
                flag=0;
            }
            else flag=1;
            System.out.println(path+" "+flag);
            PrintWriter writer;
            if(flag==0)
                writer= new PrintWriter(projectDir+"//String.txt", "UTF-8");
            else writer= new PrintWriter(projectDir+"//solution_String.txt", "UTF-8");

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
                writer.close();
            } catch (RuntimeException e) {
                new RuntimeException(e);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }).explore(projectDir);
    }

    public static void main(String[] args) throws IOException {


        File file = new File("D:\\Thesis\\Mined\\Dataset");
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        for(String x: directories){
          //  File projectDir = new File("D:\\Thesis\\Mined\\Dataset\\"+x);
           // listMethodDeclaration(projectDir);
           // System.out.println(x);
        }

        for(String x: directories){
           // File projectDir = new File("D:\\Thesis\\Mined\\Dataset\\"+x);
          //  listMethodDeclaration(projectDir);
            // System.out.println(x);
            parseString(new File("D:\\Thesis\\Mined\\Dataset\\"+x));
        }

//       File projectDir = new File("D:\\Thesis\\Mined\\Input for test run");
//       listMethodDeclaration(projectDir);
//       Parse();
////      JsonParsing();
//      String dir="D:\\Thesis\\Mined\\Input_From_Test_Run\\Test_";
//       for(int i=1;i<=8014;i++){// depends on number of file
//           parseString(new File(dir+Integer.toString(i)));
//        }
      //File stringDir=new File("D:\\Thesis\\Undergraduate-Thesis\\Java_parser");
     // parseString(stringDir);
      //  listMethodDeclaration(stringDir);

    }
}
