package rest.resources;

import java.util.ArrayList;
import java.util.List;

import javassist.bytecode.ByteArray;

public class SateliteData {

    private String nodeId;

    private List<Float> coeficientsList;

    private ByteArray serializedData;

    

    public SateliteData(String nodeId, List<Float> coeficientsList,ByteArray byteData) {
        this.nodeId = nodeId;
        this.coeficientsList = coeficientsList;
        this.serializedData=byteData;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public List<Float> getCoeficientsList() {
        return coeficientsList;
    }

    public void setCoeficientsList(List<Float> coeficientsList) {
        this.coeficientsList = coeficientsList;
    }


    public ByteArray getSerializedData() {
        return serializedData;
    }

    public void setSerializedData(ByteArray serializedData) {
        this.serializedData = serializedData;
    }

}
