package it.unipi.cs.smartapp.drivers;

import java.util.HashMap;
import java.util.Map;

public class GameServerResponse {
    public final ResponseCode code;
    private final Map<String, Object> map;

    public GameServerResponse(ResponseCode c) {
        code = c;
        map = new HashMap<>();
    }

    public void put(String key, Object data) {
        map.put(key, data);
    }

    public Object get(String key) {
        return map.get(key);
    }
}
