package it.unipi.cs.smartapp.drivers;

public enum ResponseCode {
    OK,     // Request succeeded ("OK" from server)
    ERROR,  // Request failed ("ERROR" from server)
    FAIL;   // General error during the communication

    static public ResponseCode fromString(String s) {
        return switch (s) {
            case "OK" -> OK;
            case "ERROR" -> ERROR;
            default -> FAIL;
        };
    }
}
