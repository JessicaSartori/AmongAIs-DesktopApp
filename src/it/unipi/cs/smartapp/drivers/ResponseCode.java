package it.unipi.cs.smartapp.drivers;

public enum ResponseCode {
    OK,     // Request succeeded ("OK" from server)
    ERROR,  // Request failed ("ERROR" from server)
    FAIL;   // General error during the communication

    static public ResponseCode fromString(String s) {
        switch (s) {
            case "OK":
                return OK;
            case "ERROR":
                return ERROR;
            default:
                return FAIL;
        }
    }
}
