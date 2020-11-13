package it.unipi.cs.smartapp.statemanager;

public class MapStatus {

    private Character[][] gameMap = null;
    private Integer mapSize = null;

    // Constant - Size of canvas in gameScene
    private static final int CANVAS_SIZE = 416;

    public MapStatus(){}

    /*
     * Setters
     */
    public void setGameMap(Character[][] m) { gameMap = m; }
    public void setMapSize(Integer s) { mapSize = s; }

    /*
     * Getters
     */
    public Character[][] getGameMap(){ return gameMap; }
    public Integer getMapSize(){ return mapSize; }
    public Integer getCellSize(){ return Math.round(CANVAS_SIZE/mapSize); }
}
