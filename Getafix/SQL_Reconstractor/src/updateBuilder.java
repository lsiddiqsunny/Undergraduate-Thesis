import com.google.gson.JsonObject;

public class updateBuilder implements Builder{
    public  void build(JsonObject jsonObject) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE ");
        JsonObject insert_statement = jsonObject.get("update_statement").getAsJsonObject();
        String tableName = getTableName(insert_statement).replaceAll("\"","");
        stringBuilder.append(tableName);

        JsonObject setValue = insert_statement.getAsJsonObject("set_clause");
        String set  = getSet(setValue);
        stringBuilder.append(set);

        JsonObject whereValue = insert_statement.getAsJsonObject("where_clause");
        if(whereValue==null){
            System.out.println(stringBuilder);
            //System.out.println("here");
            return;
        }
        String condition  = getConditions(whereValue);
        stringBuilder.append(condition);
        System.out.println(stringBuilder);

    }

    private String getConditions(JsonObject whereValue) {
        String expressionType = whereValue.getAsJsonObject("condition").get("expr_type").toString();
        if(expressionType.equals("\"simple_comparison_t\"")){
            JsonObject expression = whereValue.getAsJsonObject("condition").getAsJsonObject("comparison_expr");
            JsonObject leftSide = expression.getAsJsonObject("first_expr");
            String leftValue="";
            if(leftSide.get("expr_type").toString().equals("\"simple_object_name_t\"")){
                leftValue = leftSide.getAsJsonObject("column_referenced_expr").getAsJsonObject("objectName").getAsJsonPrimitive("full_name").getAsString();
                //System.out.println(leftValue);
            }

            JsonObject rightSide = expression.getAsJsonObject("second_expr");
            String rightValue="";
            if(rightSide.get("expr_type").toString().equals("\"simple_constant_t\"")){
                rightValue = rightSide.getAsJsonObject("constant_expr").getAsJsonObject("literal").getAsJsonPrimitive("value").getAsString();
                //System.out.println(rightValue);
            }
            return " WHERE "+leftValue+" "+expression.get("type").toString().replaceAll("\"","")+" "+rightValue;
        }

        return "";
    }

    private String getSet(JsonObject setValue) {
        String setValueType = setValue.get("expr_type").toString();
        if(setValueType.equals("\"assignment_t\"")){

            JsonObject leftSide = setValue.getAsJsonObject("assignment_expr").getAsJsonObject("first_expr");
            String leftValue="";
            if(leftSide.get("expr_type").toString().equals("\"simple_object_name_t\"")){
                leftValue = leftSide.getAsJsonObject("column_referenced_expr").getAsJsonObject("objectName").getAsJsonPrimitive("full_name").getAsString();
                //System.out.println(leftValue);
            }

            JsonObject rightSide = setValue.getAsJsonObject("assignment_expr").getAsJsonObject("second_expr");
            String rightValue="";
            if(rightSide.get("expr_type").toString().equals("\"simple_constant_t\"")){
                rightValue = rightSide.getAsJsonObject("constant_expr").getAsJsonObject("literal").getAsJsonPrimitive("value").getAsString();
                //System.out.println(rightValue);
            }
            return " SET "+leftValue+" = "+ rightValue;
        }
        return "";
    }

}
