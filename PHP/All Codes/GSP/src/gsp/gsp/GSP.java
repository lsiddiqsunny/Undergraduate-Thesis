package gsp;

import gudusoft.gsqlparser.*;
import org.json.JSONObject;
import org.json.XML;

import com.google.gson.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GSP{

    public static String toPrettyFormat(String jsonString)
    {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

    static String parseQuery(String directory, String fileName, int n) throws IOException {
        File file=new File(directory+fileName);
        if (!file.exists()){
            System.out.println("File not exists:"+directory+fileName);
            return "";
        }
        Path path = Paths.get(directory+fileName);
        Charset charset = StandardCharsets.UTF_8;

        String content = new String(Files.readAllBytes(path), charset);
//        content = content.replace("[\'", "[\\\'");
//        content = content.replace("\']", "\\\']");
        content = content.replace("="," = ");
        content = content.replace("`","");
        //content = content.replace("\'","");
        content = content.replace("->",".");
        content = content.replace("[\'","[\\\'");
        content = content.replace("\']","\\\']");
        System.out.println(content);
        Files.write(path, content.getBytes(charset));

        EDbVendor dbVendor = EDbVendor.dbvmysql;

        System.out.println("Selected SQL dialect: "+dbVendor.toString());

        TGSqlParser sqlParser = new TGSqlParser(dbVendor);

        sqlParser.sqlfilename  = directory+fileName;
        String xmlFile = directory+"beforeXML"+ n + ".xml";
        String jsonFile = directory+"beforeParsed"+n+".json";
        int ret = sqlParser.parse();
        if (ret == 0){
            String xsdFile = "file:/C:/prg/gsp_java_maven/doc/xml/sqlschema.xsd";
            xmlVisitor xv2 = new xmlVisitor( xsdFile );
            xv2.run( sqlParser );
            xv2.validXml();
            xv2.writeToFile( xmlFile );
            System.out.println( xmlFile + " was generated!" );

            BufferedReader br = new BufferedReader(new FileReader(xmlFile));
            String str="";
            String line="";
            while ((line = br.readLine()) != null)
            {
                str+=line;
            }
            JSONObject xmlJSONObj = XML.toJSONObject(str);

            FileWriter fw=new FileWriter(jsonFile);
            fw.write(toPrettyFormat(xmlJSONObj.toString()));
            fw.close();
            System.out.println( jsonFile + " was generated!" );

        }else{
            System.out.println(sqlParser.getErrormessage());
            return "";
        }
        return "";
    }
    public static void main(String[] args) throws IOException {
        //String parsed = parseQuery("test.sql");
        //System.out.println(parsed);

        String directory = "D:\\Thesis\\Undergraduate-Thesis\\PHP\\All Codes\\Dataset\\Temp2\\";
        File directoryPath = new File(directory);
        String[] contents = directoryPath.list();
        System.out.println("List of files and directories in the specified directory:");
        for(int i=0; i<contents.length; i++) {
            File file = new File(directory+contents[i]);
            if(file.isDirectory()){
                String[] fileContents = file.list();
                int j=0;
                System.out.println(file.getName());
                for(String fileName:fileContents){
                    if(fileName.contains("beforeQuery")){
                        j++;
                        String parsed = parseQuery(directory+contents[i]+"//",fileName, j);
                        //System.out.println(parsed);
                        //FileWriter myWriter = new FileWriter(directory+contents[i]+"//"+"Parsed"+j+".json");
                        //myWriter.write(parsed);
                        //myWriter.close();
                    }
                }

            }
        }
    }

}
