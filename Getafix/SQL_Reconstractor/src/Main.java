import com.google.gson.*;

import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {

        JsonParser parser = new JsonParser();
        try
        {
            File file=new File("test.json");    //creates a new file instance
            FileReader fr=new FileReader(file);   //reads the file
            BufferedReader br=new BufferedReader(fr);  //creates a buffering character input stream
            StringBuffer sb=new StringBuffer();    //constructs a string buffer with no characters
            String line;
            while((line=br.readLine())!=null)
            {
                sb.append(line);      //appends line to string buffer
                sb.append("\n");     //line feed
            }
            fr.close();    //closes the stream and release the resources
            JsonElement jsonTree = parser.parse(sb.toString());
            //System.out.println(jsonTree);
            if(jsonTree.isJsonObject()){
                JsonObject jsonObject = jsonTree.getAsJsonObject();

                JsonElement type = jsonObject.get("type");
                Builder builder = null;
                //System.out.printf(type.toString());
                if(type.toString().equals("\"sstinsert\"")){
                    builder = new insertBuilder();
                    builder.build(jsonObject);
                }
                if(type.toString().equals("\"sstupdate\"")){
                    builder = new updateBuilder();
                    builder.build(jsonObject);
                }


            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }


}
