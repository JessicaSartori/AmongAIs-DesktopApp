package it.unipi.cs.smartapp.statemanager;

import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;


public class MapStatus {

    // Sizes of canvas in gameScene
    private Double canvasHeight = null, canvasWidth = null;

    private Character[][] gameMap = null;
    private Integer mapSize = null, mapWidth = null;
    private Double cellSize = null;
    private Character ratio = null;
    private Boolean loaded;

    private final HashMap<Character, Image> sprites;

    /*
     * Constructor
     */
    public MapStatus(){
        sprites = new HashMap<>();
        loaded = false;
    }

    /*
     * Setters
     */
    public void setGameMap(String[] rows) { gameMap = stringToCharMap(rows); }
    public void setMapSize(Integer s) { mapSize = s; }
    public void setMapRatio(Character r) { ratio = r; mapWidth = (ratio == 'Q') ? mapSize : mapSize*2; }
    public void setCanvasHeight(Double h) { canvasHeight = h; }
    public void setCanvasWidth(Double w) { canvasWidth = w; }
    public void setLoaded(Boolean l) { loaded = l; }

    /*
     * Getters
     */
    public Character[][] getGameMap(){ return gameMap; }
    public Integer getMapSize(){ return mapSize; }
    public Double getCellSize(){ return cellSize; }

    /*
     * Method to parse map
     */
    public Character[][] stringToCharMap(String[] rows) {
        Character[][] parsedMap = new Character[mapSize][mapWidth];

        for(int r = 0; r < mapSize; r++)
            for(int c = 0; c < mapWidth; c++) {
                parsedMap[r][c] = rows[r].charAt(c);
            }

        return parsedMap;
    }

    /*
     * Methods to draw the map on canvas
     */
    public void drawMap(GraphicsContext canvasContext, Canvas mapCanvas, ObservableList<Player> players, String currentUser) {
        // First time, adjust canvas and load sprites
        if(!loaded){
            canvasHeight = mapCanvas.getHeight();
            cellSize = canvasHeight / (mapSize + 2);

            if(ratio == 'W') {
                // Enlarge canvas accordingly
                mapCanvas.setWidth(mapCanvas.getWidth() + cellSize*mapSize);
                // Recenter canvas width-wise
                mapCanvas.setLayoutX(mapCanvas.getLayoutX() - (cellSize*mapSize) / 2);
            }
            else
                mapCanvas.setLayoutX(318.0);

            canvasWidth = mapCanvas.getWidth();

            loadSprites();
            loaded = true;
        }

        // Clear canvas
        canvasContext.clearRect(0, 0, canvasWidth, canvasHeight);

        // Increase font size and make it bold
        canvasContext.setFont(Font.font(canvasContext.getFont().getFamily(), FontWeight.BOLD, 15));

        // Draw map
        Double xCanvas = cellSize, yCanvas = cellSize;
        for(int r = 0; r < mapSize; r++) {
            for (int c = 0; c < mapWidth; c++) {
                Image sprite = setSprite(gameMap[r][c]);
                canvasContext.drawImage(sprite, xCanvas, yCanvas, cellSize, cellSize);

                if(Character.isUpperCase(gameMap[r][c])) { canvasContext.setFill(Color.web("#B30000")); canvasContext.setStroke(Color.web("#B30000"));}
                else { canvasContext.setFill(Color.BLUE); canvasContext.setStroke(Color.BLUE); }
                canvasContext.setLineWidth(2);

                // Write names on players
                String username = findName(players, gameMap[r][c]);
                if(username != null) {
                    canvasContext.fillText(username, xCanvas - (username.length() * 2), yCanvas - 5);

                    if(currentUser.equals(username)) canvasContext.strokeRect(xCanvas, yCanvas, cellSize, cellSize);
                } else if(gameMap[r][c] == 'X' || gameMap[r][c] == 'x') {
                    canvasContext.fillText("Flag", xCanvas - 5, yCanvas - 5);
                    canvasContext.strokeRect(xCanvas, yCanvas, cellSize, cellSize);
                }

                xCanvas += cellSize;
            }
            yCanvas += cellSize;
            xCanvas = cellSize;
        }
    }

    public void drawCell(GraphicsContext canvasContext, Integer x, Integer y, Image image) {
        Double xCanvas = x*cellSize, yCanvas = y*cellSize;

        canvasContext.drawImage(image, xCanvas, yCanvas, cellSize, cellSize);
    }

    public String findName(ObservableList<Player> players, Character symbol){
        for(Player p: players)
            if(p.getSymbol() == symbol)
                return p.getUsername();
        return null;
    }

    public Image setSprite(Character value) {
        if(Character.isUpperCase(value) && value != 'X') return sprites.get('8');
        if(Character.isLowerCase(value) && value != 'x') return sprites.get('7');
        return sprites.get(value);
    }

    public void drawShot(GraphicsContext canvasContext, Integer[] playerPos, Integer team, Character shotDirection, Character landed, Integer prevEnergy) {
        Integer c = playerPos[0], r = playerPos[1];
        Character playerKey = ' ', explosionKey = ' ';

        if(Character.isUpperCase(gameMap[r][c])) canvasContext.setStroke(Color.web("#B30000"));
        else canvasContext.setStroke(Color.BLUE);

        switch (shotDirection) {
            case 'N' -> {
                if(landed == '?') r = -1;
                else if(landed == '.' || landed == '~' || landed == '@') r -= prevEnergy;
                else while(gameMap[r][c] != landed && r >= 0) r--;
                playerKey = (team == 0) ? '8' : '7';
            }
            case 'S' -> {
                if(landed == '?') r = mapSize;
                else if(landed == '.' || landed == '~' || landed == '@') r += prevEnergy;
                else while(gameMap[r][c] != landed && r <= mapSize) r++;
                playerKey = (team == 0) ? '2' : '1';
            }
            case 'W' -> {
                if(landed == '?') c = -1;
                else if(landed == '.' || landed == '~' || landed == '@') c -= prevEnergy;
                else while(gameMap[r][c] != landed && c >= 0) c--;
                playerKey = (team == 0) ? '4' : '3';
            }
            case 'E' -> {
                if(landed == '?') c = mapWidth;
                else if(landed == '.' || landed == '~' || landed == '@') c += prevEnergy;
                else while(gameMap[r][c] != landed && c <= mapWidth) c++;
                playerKey = (team == 0) ? '6' : '5';
            }
        }

        // Turn the player correctly
        Image player = sprites.get(playerKey);
        drawCell(canvasContext, playerPos[0] + 1, playerPos[1] + 1, player);
        // Draw explosion according to terrain
        switch (landed) {
            case '~' -> explosionKey = '-';
            case '@' -> explosionKey = '+';
            case '?' -> explosionKey = '/';
            default -> explosionKey = '*';
        }
        Image explosion = sprites.get(explosionKey);
        drawCell(canvasContext, c + 1, r + 1, explosion);

        // Redraw square around current player
        Double xCanvas = (playerPos[0] + 1)*cellSize, yCanvas = (playerPos[1] + 1)*cellSize;
        canvasContext.strokeRect(xCanvas, yCanvas, cellSize, cellSize);
    }

    public void loadSprites(){
        Image icon = new Image("it/unipi/cs/smartapp/sprites/transparent.png", cellSize, cellSize, true, true);
        sprites.put(' ', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/grass.png", cellSize, cellSize, true, true); // Grass
        sprites.put('.', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/wall.png", cellSize, cellSize, true, true); // Wall
        sprites.put('#', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/river.png", cellSize, cellSize, true, true); // River
        sprites.put('~', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/ocean.png", cellSize, cellSize, true, true); // Ocean
        sprites.put('@', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/trap.png", cellSize, cellSize, true, true); // Trap
        sprites.put('!', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/energy.png", cellSize, cellSize, true, true); // Energy recharge
        sprites.put('$', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/barrier.png", cellSize, cellSize, true, true); // Barrier
        sprites.put('&', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/flagRed.png", cellSize, cellSize, true, true); // Flag team 0
        sprites.put('X', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/flagBlue.png", cellSize, cellSize, true, true); // Flag team 1
        sprites.put('x', icon);

        // Explosions
        icon = new Image("it/unipi/cs/smartapp/sprites/explosion.png", cellSize, cellSize, true, true);
        sprites.put('*', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/explosionOcean.png", cellSize, cellSize, true, true);
        sprites.put('+', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/explosionRiver.png", cellSize, cellSize, true, true);
        sprites.put('-', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/explosionTransparent.png", cellSize, cellSize, true, true);
        sprites.put('/', icon);

        // Players
        icon = new Image("it/unipi/cs/smartapp/sprites/playerDownBlue.png", cellSize, cellSize, true, true);
        sprites.put('1', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerDownRed.png", cellSize, cellSize, true, true);
        sprites.put('2', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerLeftBlue.png", cellSize, cellSize, true, true);
        sprites.put('3', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerLeftRed.png", cellSize, cellSize, true, true);
        sprites.put('4', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerRightBlue.png", cellSize, cellSize, true, true);
        sprites.put('5', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerRightRed.png", cellSize, cellSize, true, true);
        sprites.put('6', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerTopBlue.png", cellSize, cellSize, true, true);
        sprites.put('7', icon);
        icon = new Image("it/unipi/cs/smartapp/sprites/playerTopRed.png", cellSize, cellSize, true, true);
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