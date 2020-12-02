package it.unipi.cs.smartapp.statemanager;

import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;


import java.util.HashMap;

public class MapStatus {

    // Constant - Size of canvas in gameScene
    private static final int CANVAS_SIZE = 650;

    private Character[][] gameMap = null;
    private Integer mapSize = null;
    private final HashMap<Character, Image> sprites;

    public MapStatus(){
        sprites = new HashMap<>();
        loadSprites();
    }

    /*
     * Setters
     */
    public void setGameMap(String[] rows) { gameMap = stringToCharMap(rows); }
    public void setMapSize(Integer s) { mapSize = s; }

    /*
     * Getters
     */
    public Character[][] getGameMap(){ return gameMap; }
    public Integer getMapSize(){ return mapSize; }
    public Integer getCellSize(){ return CANVAS_SIZE / (mapSize + 2); }

    /*
     * Method to parse map
     */
    public Character[][] stringToCharMap(String[] rows) {
        Character[][] parsedMap = new Character[mapSize][mapSize];

        for(int r = 0; r < mapSize; r++)
            for(int c = 0; c < mapSize; c++) {
                parsedMap[r][c] = rows[r].charAt(c);
            }

        return parsedMap;
    }

    /*
     * Methods to draw the map on canvas
     */
    public void drawMap(GraphicsContext canvasContext, Canvas mapCanvas, ObservableList<Player> players, String currentUser) {
        Integer cellSize = getCellSize();

        // Clear canvas
        canvasContext.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

        // Draw map
        int xCanvas = cellSize, yCanvas = cellSize;
        for(int r = 0; r < mapSize; r++) {
            for (int c = 0; c < mapSize; c++) {
                Image sprite = setSprite(gameMap[r][c]);
                canvasContext.drawImage(sprite, xCanvas, yCanvas, cellSize, cellSize);

                // Write names on players
                String username = findName(players, gameMap[r][c]);
                if(username != null) {
                    if(Character.isUpperCase(gameMap[r][c])) { canvasContext.setFill(Color.RED); canvasContext.setStroke(Color.RED);}
                    else { canvasContext.setFill(Color.BLUE); canvasContext.setStroke(Color.BLUE); }

                    canvasContext.fillText(username, xCanvas - username.length()/2, yCanvas - username.length()/2);
                }
                if(currentUser != null && currentUser.equals(username))
                    canvasContext.strokeRect(xCanvas, yCanvas, cellSize, cellSize);

                xCanvas += cellSize;
            }
            yCanvas += cellSize;
            xCanvas = cellSize;
        }
    }

    public String findName(ObservableList<Player> players, Character symbol){
        for(Player p: players)
            if(p.getSymbol() == symbol)
                return p.getUsername();
        return null;
    }

    public void drawCell(GraphicsContext canvasContext, Integer x, Integer y, Image image) {
        Integer cellSize = getCellSize();
        int xCanvas = x*cellSize, yCanvas = y*cellSize;

        canvasContext.drawImage(image, xCanvas, yCanvas, cellSize, cellSize);
    }

    public Image setSprite(Character value) {
        if(Character.isUpperCase(value) && value != 'X') return sprites.get('8');
        if(Character.isLowerCase(value) && value != 'x') return sprites.get('7');
        return sprites.get(value);
    }

    public void drawShot(GraphicsContext canvasContext, Integer[] playerPos, Integer team, Character shotDirection, Character landed, Integer prevEnergy) {
        Integer c = playerPos[0], r = playerPos[1];
        char playerKey = ' ';

        switch (shotDirection) {
            case 'N' -> {
                if(landed == '?') r = -1;
                else if(landed == '.') r -= prevEnergy;
                else while(gameMap[r][c] != landed) r--;
                playerKey = (team == 0) ? '8' : '7';
            }
            case 'S' -> {
                if(landed == '?') r = mapSize;
                else if(landed == '.') r += prevEnergy;
                else while(gameMap[r][c] != landed) r++;
                playerKey = (team == 0) ? '2' : '1';
            }
            case 'W' -> {
                if(landed == '?') c = -1;
                else if(landed == '.') c -= prevEnergy;
                else while(gameMap[r][c] != landed) c--;
                playerKey = (team == 0) ? '4' : '3';
            }
            case 'E' -> {
                if(landed == '?') c = mapSize;
                else if(landed == '.') c += prevEnergy;
                else while(gameMap[r][c] != landed) c++;
                playerKey = (team == 0) ? '6' : '5';
            }
        }

        Image player = sprites.get(playerKey);
        drawCell(canvasContext, playerPos[0] + 1, playerPos[1] + 1, player);
        Image explosion = sprites.get('*');
        drawCell(canvasContext, c + 1, r + 1, explosion);
    }

    public void loadSprites(){
        Image icon = new Image("it/unipi/cs/smartapp/sprites/transparent.png");
        sprites.put(' ', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/grass.png"); // Grass
        sprites.put('.', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/wall.png"); // Wall
        sprites.put('#', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/river.png"); // River
        sprites.put('~', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/ocean.png"); // Ocean
        sprites.put('@', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/trap.png"); // Trap
        sprites.put('!', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/energy.png"); // Energy recharge
        sprites.put('$', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/barrier.png"); // Barrier
        sprites.put('&', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/flagRed.png"); // Flag team 0
        sprites.put('X', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/flagBlue.png"); // Flag team 1
        sprites.put('x', icon);

        icon = new Image("it/unipi/cs/smartapp/sprites/explosion.png");
        sprites.put('*', icon);

        icon = new Image("it/unipi/cs/smartapp/sprites/playerDownBlue.png");
        sprites.put('1', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerDownRed.png");
        sprites.put('2', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerLeftBlue.png");
        sprites.put('3', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerLeftRed.png");
        sprites.put('4', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerRightBlue.png");
        sprites.put('5', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerRightRed.png");
        sprites.put('6', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerTopBlue.png");
        sprites.put('7', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerTopRed.png");
        sprites.put('8', icon);
    }

    public void updatePosition(Integer x, Integer y, Character direction) {
        char old = gameMap[y][x];
        gameMap[y][x] = '.';
        switch (direction) {
            case 'N' -> gameMap[y - 1][x] = old;
            case 'S' -> gameMap[y + 1][x] = old;
            case 'W' -> gameMap[y][x - 1] = old;
            case 'E' -> gameMap[y][x + 1] = old;
        }
    }
}