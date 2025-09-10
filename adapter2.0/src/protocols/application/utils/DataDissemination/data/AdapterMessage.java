package protocols.application.utils.DataDissemination.data;






//adapter message data model
import com.google.gson.annotations.SerializedName;


public class AdapterMessage {

    private String msgSrcAdr;

    public String getMsgSrcAdr() {
        return msgSrcAdr;
    }

    public void setMsgSrcAdr(String msgSrcAdr) {
        this.msgSrcAdr = msgSrcAdr;
    }

    @SerializedName("dst")
    private int dst;

    @SerializedName("src")
    private int src;

    @SerializedName("payload")
    private Object payload;

    public int getDst() {
        return this.dst;
    }

    public Object getPayload() {
        return this.payload;
    }

    public int getSrc() {
        return this.src;
    }
}

