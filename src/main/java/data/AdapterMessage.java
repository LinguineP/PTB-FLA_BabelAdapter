package data;

import com.google.gson.JsonObject;


//adapter message data model
public class AdapterMessage {
    private String dst;
    private String src;
    private JsonObject payload;

    public String getDst() {
        return this.dst;
    }

    public JsonObject getPayload() {
        return this.payload;
    }

    public String getSrc() {
        return this.src;
    }


}
