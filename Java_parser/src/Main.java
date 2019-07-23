

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

        PrintWriter writer1 = new PrintWriter("outjsonfrom2652019.json", "UTF-8");
        PrintWriter writer2 = new PrintWriter("outjavafrom2652019.java", "UTF-8");
        new Director((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {

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
                        writer1.println(toPrettyFormat(jprinter.output(n))+"\n\n######\n\n");
                        writer2.println(n.toString()+"\n\n######\n\n");
                        // System.out.println(printer.output(n));

                      //  writer.println(toPrettyFormat(jprinter.output(n))+"\n#######\n");
                     //  String data=jprinter.output(n);
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
        writer1.close();
        writer2.close();
    }
    public static void Parse() throws IOException {

        InputStream is = new FileInputStream("D:\\Thesis\\Undergraduate-Thesis\\Java_parser\\outjsonfrom2652019.json");
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();
        while(line != null){ sb.append(line).append("\n"); line = buf.readLine(); }
        String fileAsString = sb.toString();

        String [] data=fileAsString.split("######");


        InputStream is1 = new FileInputStream("D:\\Thesis\\Undergraduate-Thesis\\Java_parser\\outjavafrom2652019.java");
        BufferedReader buf1 = new BufferedReader(new InputStreamReader(is1));
        String line1 = buf1.readLine();
        StringBuilder sb1 = new StringBuilder();
        while(line1 != null){ sb1.append(line1).append("\n"); line1 = buf1.readLine(); }
        String fileAsString1 = sb1.toString();

        String [] data1=fileAsString1.split("######");

        int i=0;
        String dir="D:\\Thesis\\Mined\\Output_from_28_5_2019\\28_5_2019_";
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
        String dir1="D:\\Thesis\\Mined\\Output_from_28_5_2019\\28_5_2019_";
        String ext1=".java";

        for(String s: data1){
            PrintWriter writer = new PrintWriter(dir1+Integer.toString(i+1)+"\\code"+ext1, "UTF-8");
            writer.println(s);
            writer.close();


            i++;
        }




    }


    public static void main(String[] args) throws IOException {
      //  CompilationUnit cu = StaticJavaParser.parse(new File(FILE_PATH));
      ///  System.out.println(cu);


    // Now comes the inspection code:
     //   YamlPrinter printer = new YamlPrinter(true);
       // System.out.println(printer.output(cu));

       // List<Comment> comments = cu.getAllContainedComments();
        //System.out.println("******\n"+cu.toString()+"\n******");

       // Iterator iterator = comments.iterator();
        //while(iterator.hasNext()) {
            //System.out.println(iterator.next().toString());
        //}

     //   File projectDir = new File("D:\\Thesis\\Mined\\output from  28-5-2019");
     //   listMethodDeclaration(projectDir);
        Parse();
    }
}
