import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class insertBuilder implements Builder{

    public void build(JsonObject jsonObject) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO ");
        JsonObject insert_statement = jsonObject.get("insert_statement").getAsJsonObject();

        String tableName = getTableName(insert_statement).replaceAll("\"","");
        stringBuilder.append(tableName);

        JsonObject column_list = insert_statement.getAsJsonObject("column_list");
        String columns  = getColumns(column_list);
        stringBuilder.append(columns);

        stringBuilder.append("VALUES");
        JsonObject value_list = insert_statement.getAsJsonObject("insert_values").getAsJsonObject("row_values");
        String values = getValues(value_list);
        stringBuilder.append(values);

        stringBuilder.append(";");
        System.out.println(stringBuilder);
    }
    private String getValues(JsonObject value_list){
        JsonArray values = value_list .getAsJsonArray("expression");
        String valueString = " (";
        for(int i=0;i<values.size();i++){
            //System.out.println(values.get(i));
            if(i!=values.size()-1){
                String value = values.get(i).getAsJsonObject().getAsJsonObject("constant_expr").getAsJsonObject("literal").get("value").toString().replaceAll("\"","");
                valueString+=value+",";
            }
            else{
                String value = values.get(i).getAsJsonObject().getAsJsonObject("constant_expr").getAsJsonObject("literal").get("value").toString().replaceAll("\"","");
                valueString+=value;
            }

        }
        valueString+=") ";
        return valueString;
    }


    private String getColumns(JsonObject column_list){
        JsonArray columns = column_list.getAsJsonArray("objectName");
        String columnString = " (";
        for(int i=0;i<columns.size();i++){

            if(i!=columns.size()-1){
                columnString+=columns.get(i).getAsJsonObject().get("full_name").toString().replaceAll("\"","")+",";
            }
            else{
                columnString+=columns.get(i).getAsJsonObject().get("full_name").toString().replaceAll("\"","");
            }

        }
        columnString+=") ";
        return columnString;
    }



}
