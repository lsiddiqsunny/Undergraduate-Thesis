import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ReplaceSuggestion {

    public JsonElement after;
    public JsonElement before;

    public ReplaceSuggestion(JsonElement a, JsonElement b){
        before=b;
        after=a;
    }

}
