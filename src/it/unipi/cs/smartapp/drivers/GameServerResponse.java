package it.unipi.cs.smartapp.drivers;


public class GameServerResponse {

    public final ResponseCode code;
    public final Object data;
    public final String freeText;

    public GameServerResponse(ResponseCode code, Object data, String text) {
        this.code = code;
        this.data = data;
        this.freeText = text;
    }
}
