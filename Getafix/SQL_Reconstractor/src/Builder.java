import com.google.gson.JsonObject;

public interface Builder {
    public default String getTableName(JsonObject statement) {
        return statement.getAsJsonObject("target_table").getAsJsonObject("named_table_reference").getAsJsonObject("table_name").get("full_name").toString();
    }
    void build(JsonObject jsonObject);
}
