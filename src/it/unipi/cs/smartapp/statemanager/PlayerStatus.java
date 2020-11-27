package it.unipi.cs.smartapp.statemanager;

public class PlayerStatus {
    public String username = null;
    public Character symbol = null;
    public Integer[] position = {-1 , -1};

    public Integer team = null;
    public Integer loyalty = null;
    public Integer energy = null;
    public Integer score = null;
    public String state = null;


    public PlayerStatus() {}

    public PlayerStatus(String username) {
        this.username = username;
    }

    public void updateWith(String info) {
        String[] tokens = info.split("[ =]");

        for(int i=0; i < tokens.length; i += 2) {
            String keyword = tokens[i], value = tokens[i+1];
            switch (keyword) {
                case "symbol" -> symbol = value.toCharArray()[0];
                case "name" -> username = value;
                case "team" -> team = Integer.parseInt(value);
                case "loyalty" -> loyalty = Integer.parseInt(value);
                case "energy" -> energy = Integer.parseInt(value);
                case "score" -> score = Integer.parseInt(value);
                case "x" -> position[0] = Integer.parseInt(value);
                case "y" -> position[1] = Integer.parseInt(value);
                case "state" -> state = value.toLowerCase();
            }
        }
    }
}
