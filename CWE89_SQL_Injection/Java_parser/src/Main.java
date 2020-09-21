import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class Main {


public static void Parser(File file,String basePath){
    //JavaParser.getStaticConfiguration().setAttributeComments(false);
        System.out.println("Method declaration:");
    try {
        FileWriter fr = new FileWriter(new File(basePath+"\\target.txt"), true);
        BufferedWriter br = new BufferedWriter(fr);

        new VoidVisitorAdapter<Object>() {
            @Override

            public void visit(MethodDeclaration n, Object arg) {
                super.visit(n, arg);
               // System.out.println(n.toString());
                String[] lines = n.toString().split("\n");
                String targetlines = "";
                int ok=0;
                for(String line:lines){
                    if(line.contains("/*") || line.contains("*/")) {
                        continue;
                    }

                    if(line.contains("Statement sqlStatement") && !line.contains("PreparedStatement sqlStatement")){
                        targetlines+=line.trim();
                        targetlines+="#";
                        ok++;
                    }
                    if(line.contains("dbConnection.createStatement()")){
                        targetlines+=line.trim();
                        targetlines+="#";
                        ok++;
                    }
                    if(line.contains("execute") && line.contains("\"")){
                        targetlines+=line.trim();
                        targetlines+="#";
                        ok++;
                    }
                }

                if(targetlines.contains("Batch") || (targetlines.contains("insert") && targetlines.contains("where"))){
                    return;
                }
                if(!targetlines.contains("dbConnection.createStatement()")){
                    return;
                }
                if(ok<3){
                    return;
                }

                try {
                    System.out.println(targetlines);
                    br.write(targetlines+"\n##########\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }.visit(JavaParser.parse(file),null);
        // System.out.println(); // empty line
        br.close();
        fr.close();
    } catch (RuntimeException e) {
        new RuntimeException(e);
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }

}
    public  static void findLines(String path) throws IOException {
        System.out.println(path);
        for(int i=1;i<=976;i++) {
            String[] pathnames;

            File f = new File(path+"\\"+i);
            pathnames = f.list();
            if(pathnames==null){
                continue;
            }
            for(String pathname:pathnames){

                if(pathname.contains(".java")) {
                    System.out.println(pathname);
                    Set<Integer> lines = new HashSet<>();
                    InputStream is = new FileInputStream(path + i + "\\" + pathname);

                    BufferedReader buf = new BufferedReader(new InputStreamReader(is));

                    String line = buf.readLine();
                    int lineNo=1;
                    while(line != null){
                        if(line.contains("/*") || line.contains("*/")){
                            line = buf.readLine();
                            lineNo++;
                            continue;
                        }
                        if(line.contains("Statement sqlStatement")){
                            lines.add(lineNo);
                        }
                        if(line.contains("dbConnection.createStatement()")){
                            lines.add(lineNo);
                        }
                        if(line.contains("execute") && !line.contains("Batch")){
                            lines.add(lineNo);
                        }
                       /* if(line.toLowerCase().contains(new String("Select ").toLowerCase())
                                && line.toLowerCase().contains(new String("from ").toLowerCase())
                                && line.toLowerCase().contains(new String("Where ").toLowerCase())){
                            lines.add(lineNo);
                        }
                        if(line.toLowerCase().contains(new String("Insert ").toLowerCase())
                                && line.toLowerCase().contains(new String("into ").toLowerCase())){
                            lines.add(lineNo);
                        }
                        if(line.toLowerCase().contains(new String("Update ").toLowerCase())
                                && line.toLowerCase().contains(new String("set ").toLowerCase())){
                            lines.add(lineNo);
                        }
                        if(line.toLowerCase().contains(new String("Delete ").toLowerCase())
                                && line.toLowerCase().contains(new String("from ").toLowerCase())
                                && line.toLowerCase().contains(new String("Where ").toLowerCase())){
                            lines.add(lineNo);
                        }*/
                        line = buf.readLine();
                        lineNo++;
                    }
                    ArrayList<Integer> list = new ArrayList<>();
                    for(int x: lines){
                        list.add(x);
                    }
                    Collections.sort(list);
                    FileWriter fr = new FileWriter(new File(path + i + "\\" + pathname+".txt"));
                    //System.out.println(function);

                    for(int x:list){
                        System.out.println(x);
                        fr.write(x+"\n");
                    }

                    fr.close();

                }
            }
        }
}

public  static void modifyFiles(String path) throws IOException {
    for(int i=1;i<=976;i++){
        System.out.println(i);
        try {
            InputStream is = new FileInputStream(path + i + "\\" + "functions.txt");

        BufferedReader buf = new BufferedReader(new InputStreamReader(is));

        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();

        while(line != null){
            sb.append(line).append("\n");
            line = buf.readLine();
        }

        String fileAsString = sb.toString();
        String[] functions = fileAsString.split("##########");
        int j=1;
        for(String function: functions){

                if(function.contains("Statement") && !function.contains("PreparedStatement")){
                    FileWriter fr = new FileWriter(new File(path+i+"\\"+j+".java"));
                    //System.out.println(function);
                    fr.write(function);
                    fr.close();
                    j++;
                }

        }
        }catch (Exception e){
            continue;
        }
    }
}

    public static void main(String[] args)  {

for(int i=1;i<=4;i++){
        String[] pathnames;
        String basePath = "D:\\Thesis\\CWE89_SQL_Injection\\CWE89_SQL_Injection\\s0"+i+"\\";
        File f = new File(basePath);
        pathnames = f.list();

        for (String pathname : pathnames) {

            if(pathname.endsWith(".java") && !pathname.contains("Main")) {
                File directory = new File("D:\\Thesis\\CWE89_SQL_Injection\\Target\\s0"+i+"\\"+ pathname);

                if (! directory.exists()){
                    directory.mkdir();
                }
                Parser(new File(basePath+pathname),"D:\\Thesis\\CWE89_SQL_Injection\\Target\\s0"+i+"\\"+ pathname);

                //break;
            }
        }

//        try {
//            modifyFiles("D:\\Thesis\\CWE89_SQL_Injection\\Functions\\s0"+i+"\\");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    try {
//        findLines("D:\\Thesis\\CWE89_SQL_Injection\\Functions\\s0"+i+"\\");
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
}
}
}
