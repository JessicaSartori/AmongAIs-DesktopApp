package it.unipi.cs.smartapp.statemanager;

import javafx.scene.input.KeyCode;


public class PlayerSettings {
    static private PlayerSettings instance = null;

    // Moving Key Bindings
    private KeyCode moveUp = null;
    private KeyCode moveDown = null;
    private KeyCode moveLeft = null;
    private KeyCode moveRight = null;

    // Shooting Key Bindings
    private KeyCode shootUp = null;
    private KeyCode shootDown = null;
    private KeyCode shootLeft = null;
    private KeyCode shootRight = null;

    // Map & Status Frequencies
    private Integer mapFreq = null;
    private Integer statusFreq = null;

    static public PlayerSettings getInstance() {
        if(instance == null) instance = new PlayerSettings();
        return instance;
    }

    // Default Player Settings
    private PlayerSettings() {
        moveUp = KeyCode.W; moveDown = KeyCode.S;
        moveLeft = KeyCode.A; moveRight = KeyCode.D;

        shootUp = KeyCode.I; shootDown = KeyCode.K;
        shootLeft = KeyCode.J; shootRight = KeyCode.L;

        mapFreq = 200; statusFreq = 200;
    }

    public boolean isAlreadySet(KeyCode key) {
        return key == moveUp || key == moveDown || key == moveLeft || key == moveRight ||
                key == shootUp || key == shootDown || key == shootLeft || key == shootRight;
    }

    /*
     * Setters
     */
    public void setMoveUp(KeyCode up) { moveUp = up; }
    public void setMoveDown(KeyCode down) { moveDown = down; }
    public void setMoveLeft(KeyCode left) { moveLeft = left; }
    public void setMoveRight(KeyCode right) { moveRight = right; }
    public void setShootUp(KeyCode up) { shootUp = up; }
    public void setShootDown(KeyCode down) { shootDown = down; }
    public void setShootLeft(KeyCode left) { shootLeft = left; }
    public void setShootRight(KeyCode right) { shootRight = right; }
    public void setMapFreq(Integer freq) { mapFreq = freq; }
    public void setStatusFreq(Integer freq) { statusFreq = freq; }

    /*
     * Getters
     */
    public KeyCode getMoveUp() { return moveUp; }
    public KeyCode getMoveDown() { return moveDown; }
    public KeyCode getMoveLeft() { return moveLeft; }
    public KeyCode getMoveRight() { return moveRight; }
    public KeyCode getShootUp() { return shootUp; }
    public KeyCode getShootDown() { return shootDown; }
    public KeyCode getShootLeft() { return shootLeft; }
    public KeyCode getShootRight() { return shootRight; }
    public Integer getMapFreq() { return mapFreq; }
    public Integer getStatusFreq() { return statusFreq; }
}
