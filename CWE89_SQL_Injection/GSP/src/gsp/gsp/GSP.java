package gsp;

import gudusoft.gsqlparser.*;
import org.json.JSONObject;
import org.json.XML;

import com.google.gson.*;

import java.io.*;

public class GSP{

    public static String toPrettyFormat(String jsonString)
    {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

    static String parseQuery(String directory, String fileName) throws IOException {
        File file=new File(directory+fileName);
        if (!file.exists()){
            System.out.println("File not exists:"+directory+fileName);
            return "";
        }

        EDbVendor dbVendor = EDbVendor.dbvoracle;

        System.out.println("Selected SQL dialect: "+dbVendor.toString());

        TGSqlParser sqlParser = new TGSqlParser(dbVendor);

        sqlParser.sqlfilename  = directory+fileName;
        String xmlFile = directory+fileName+".xml";
        String jsonFile = directory+fileName+".json";
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
        for(int l=1;l<=4;l++){
        String directory = "D:\\Thesis\\CWE89_SQL_Injection\\Target\\s0"+l+"\\";
        File directoryPath = new File(directory);
        String[] contents = directoryPath.list();
        System.out.println("List of files and directories in the specified directory:");
        for(int i=0; i<contents.length; i++) {
            File file = new File(directory+contents[i]);
            if(file.isDirectory()){
                String[] fileContents = file.list();
                int j=0;
                //System.out.println(file.getName());

                for(String fileName:fileContents){
                    if(fileName.endsWith(".sql")){
                        j++;
                        //System.out.println(fileName);
                        String parsed = parseQuery(directory+contents[i]+"//",fileName);
                        //System.out.println(parsed);
                        //FileWriter myWriter = new FileWriter(directory+contents[i]+"//"+"Parsed"+j+".json");
                        //myWriter.write(parsed);
                        //myWriter.close();
                    }
                }

            }
        }}
    }

}
