package protocols.application.utils.DataDissemination.data;

import java.util.List;

import com.google.gson.JsonObject;

public class PTBFLAmessage {
    private Object msgData; 
    public Object getMsgData() {
        return msgData;
    }
    public void setMsgData(Object msgData) {
        this.msgData = msgData;
    }
    private List<Object> msgSrcAdr;  
    public List<Object> getMsgSrcAdr() {
        return msgSrcAdr;
    }
    public void setMsgSrcAdr(List<Object> msgSrcAdr) {
        this.msgSrcAdr = msgSrcAdr;
    }
    private int remote;
    public int getRemote() {
        return remote;
    }
    public void setRemote(int remote) {
        this.remote = remote;
    }

}
