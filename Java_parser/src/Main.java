

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.JsonPrinter;
import com.github.javaparser.printer.YamlPrinter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;



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

/// use to list all method in a directory
    public static void listMethodDeclaration(File projectDir) throws FileNotFoundException, UnsupportedEncodingException {
        JavaParser.getStaticConfiguration().setAttributeComments(false);

        PrintWriter writer1 = new PrintWriter("outjson.json", "UTF-8");//get all json from the specific directory
        PrintWriter writer2 = new PrintWriter("outjava.java", "UTF-8");// get all java function from the specific directory
        new Director((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {

            System.out.println(path);//print the path
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(MethodDeclaration n, Object arg) {
                        super.visit(n, arg);
                        YamlPrinter printer = new YamlPrinter(true);
                        JsonPrinter jprinter=new  JsonPrinter(true);

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
        String dir="D:\\Thesis\\Mined\\Output_from_28_5_2019\\28_5_2019_";///change with respect to data folder
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

    public static void main(String[] args) throws IOException {
   

       File projectDir = new File("D:\\Thesis\\Mined\\output from  28-5-2019");
       listMethodDeclaration(projectDir);
       Parse();
        listString(projectDir);
       String dir="D:\\Thesis\\Mined\\Output_from_28_5_2019\\28_5_2019_";
       for(int i=1;i<=7412;i++){// depends on number of file
            listString(new File(dir+Integer.toString(i)),dir+Integer.toString(i));
        }

    }
}
