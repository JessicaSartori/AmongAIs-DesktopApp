package it.unipi.cs.smartapp.statemanager;

import javafx.scene.input.KeyCode;


public class PlayerSettings {

    static private PlayerSettings instance = null;

    static public PlayerSettings getInstance() {
        if(instance == null) instance = new PlayerSettings();
        return instance;
    }


    // Moving/Shooting Key Bindings
    private KeyCode moveUp, moveDown, moveLeft, moveRight;
    private KeyCode shootUp, shootDown, shootLeft, shootRight;

    // Side panels Key Bindings
    private KeyCode flipLeft, flipRight;

    // Map & Status Frequencies
    private Integer mapFreq, statusFreq;


    // Default Player Settings
    private PlayerSettings() {
        moveUp = KeyCode.W; moveDown = KeyCode.S;
        moveLeft = KeyCode.A; moveRight = KeyCode.D;

        shootUp = KeyCode.I; shootDown = KeyCode.K;
        shootLeft = KeyCode.J; shootRight = KeyCode.L;

        flipLeft = KeyCode.P; flipRight = KeyCode.ENTER;

        mapFreq = 1000; statusFreq = 1000;
    }

    public boolean isAlreadySet(KeyCode key) {
        return key == moveUp || key == moveDown || key == moveLeft || key == moveRight ||
                key == shootUp || key == shootDown || key == shootLeft || key == shootRight ||
                key == flipLeft || key == flipRight;
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
    public void setFlipLeft(KeyCode left) { flipLeft = left; }
    public void setFlipRight(KeyCode right) { flipRight = right; }
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
    public KeyCode getFlipLeft() { return flipLeft; }
    public KeyCode getFlipRight() { return flipRight; }
    public Integer getMapFreq() { return mapFreq; }
    public Integer getStatusFreq() { return statusFreq; }
}
