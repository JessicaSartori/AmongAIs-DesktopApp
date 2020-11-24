package it.unipi.cs.smartapp.statemanager;

import com.sun.glass.ui.EventLoop;

public class PlayerSettings {
    static private PlayerSettings instance = null;

    // Moving Key Bindings
    private Character moveUp = null;
    private Character moveDown = null;
    private Character moveLeft = null;
    private Character moveRight = null;

    // Shooting Key Bindings
    private Character shootUp = null;
    private Character shootDown = null;
    private Character shootLeft = null;
    private Character shootRight = null;

    // Map & Status Frequencies
    private Integer mapFreq = null;
    private Integer statusFreq = null;

    static public PlayerSettings getInstance() {
        if(instance == null) instance = new PlayerSettings();
        return instance;
    }

    // Default Player Settings
    private PlayerSettings() {
        moveUp = 'W'; moveDown = 'S';
        moveLeft = 'A'; moveRight = 'D';

        shootUp = 'I'; shootDown = 'K';
        shootLeft = 'J'; shootRight = 'L';

        mapFreq = 200; statusFreq = 200;
    }

    public boolean isAlreadySet(Character key) {
        if (key.equals(moveUp) || key.equals(moveDown) || key.equals(moveLeft) || key.equals(moveRight) ||
            key.equals(shootUp) || key.equals(moveDown) || key.equals(moveLeft) || key.equals(moveRight))
            return true;
        return false;
    }

    /*
     * Setters
     */
    public void setMoveUp(Character up) { moveUp = up; }
    public void setMoveDown(Character down) { moveDown = down; }
    public void setMoveLeft(Character left) { moveLeft = left; }
    public void setMoveRigth(Character right) { moveRight = right; }
    public void setShootUp(Character up) { shootUp = up; }
    public void setShootDown(Character down) { shootDown = down; }
    public void setShootLeft(Character left) { shootLeft = left; }
    public void setShootRight(Character right) { shootRight = right; }
    public void setMapFreq(Integer freq) { mapFreq = freq; }
    public void setStatusFreq(Integer freq) { statusFreq = freq; }

    /*
     * Getters
     */
    public Character getMoveUp() { return moveUp; }
    public Character getMoveDown() { return moveDown; }
    public Character getMoveLeft() { return moveLeft; }
    public Character getMoveRight() { return moveRight; }
    public Character getShootUp() { return shootUp; }
    public Character getShootDown() { return shootDown; }
    public Character getShootLeft() { return shootLeft; }
    public Character getShootRight() { return shootRight; }
    public Integer getMapFreq() { return mapFreq; }
    public Integer getStatusFreq() { return statusFreq; }
}
