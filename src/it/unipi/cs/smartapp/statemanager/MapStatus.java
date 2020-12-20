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

    private final Character[][] gameMap;
    private final Integer mapHeight, mapWidth;
    private final Character ratio;

    private Double cellSize;
    private Boolean loaded;
    private char hidden_tile = '.';

    private final HashMap<Character, Image> sprites;

    // Constructor
    public MapStatus(Integer size, Character r) {
        mapHeight = size;
        ratio = r;
        mapWidth = (ratio == 'Q') ? mapHeight : 2* mapHeight;
        gameMap = new Character[mapHeight][mapWidth];

        sprites = new HashMap<>();
        loaded = false;
    }

    // Update the game map
    public void setGameMap(String[] rows) {
        for (int r = 0; r < mapHeight; r++)
            for (int c = 0; c < mapWidth; c++)
                gameMap[r][c] = rows[r].charAt(c);
    }

    /*
     * Methods to draw the map on canvas
     */
    public void drawMap(Canvas mapCanvas, ObservableList<Player> players, String currentUser) {
        // Adjust canvas size and load sprites every time scene is loaded
        if(!loaded){
            double canvasHeight = mapCanvas.getHeight();

            cellSize = canvasHeight / (mapHeight + 2);

            // Enlarge canvas accordingly
            if(ratio == 'W') mapCanvas.setWidth(canvasHeight + cellSize* mapHeight);
            else mapCanvas.setWidth(canvasHeight);

            loadSprites();
            loaded = true;
        }

        GraphicsContext canvasContext = mapCanvas.getGraphicsContext2D();

        // Clear canvas
        canvasContext.clearRect(0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());

        // Increase font size and make it bold
        canvasContext.setFont(Font.font(canvasContext.getFont().getFamily(), FontWeight.BOLD, 15));
        canvasContext.setLineWidth(2);

        // Draw map
        Double xCanvas = cellSize, yCanvas = cellSize;
        for(int r = 0; r < mapHeight; r++) {
            for (int c = 0; c < mapWidth; c++) {
                Image sprite = setSprite(gameMap[r][c]);
                canvasContext.drawImage(sprite, xCanvas, yCanvas, cellSize, cellSize);

                if(Character.isUpperCase(gameMap[r][c])) { canvasContext.setFill(Color.web("#B30000")); canvasContext.setStroke(Color.web("#B30000"));}
                else { canvasContext.setFill(Color.BLUE); canvasContext.setStroke(Color.BLUE); }

                // Write names on players
                String username = findName(players, gameMap[r][c]);
                if(username != null) {
                    canvasContext.fillText(username, xCanvas - (username.length() * 2), yCanvas - 5);

                    if(currentUser != null && currentUser.equals(username)) canvasContext.strokeRect(xCanvas, yCanvas, cellSize, cellSize);
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
        double xCanvas = x*cellSize, yCanvas = y*cellSize;

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

    public void drawShot(Canvas mapCanvas, Integer[] playerPos, Integer team, Character shotDirection, Character landed, Integer prevEnergy) {
        GraphicsContext canvasContext = mapCanvas.getGraphicsContext2D();

        Integer c = playerPos[0], r = playerPos[1];
        char playerKey = ' ', explosionKey;

        if(Character.isUpperCase(gameMap[r][c])) canvasContext.setStroke(Color.web("#B30000"));
        else canvasContext.setStroke(Color.BLUE);

        switch (shotDirection) {
            case 'N':
                if(landed == '?') r = -1;
                else if(landed == '.' || landed == '~' || landed == '@') r -= prevEnergy;
                else while(r > 0 && gameMap[r][c] != landed) r--;
                playerKey = (team == 0) ? '8' : '7';
                break;
            case 'S':
                if(landed == '?') r = mapHeight;
                else if(landed == '.' || landed == '~' || landed == '@') r += prevEnergy;
                else while(r < mapHeight && gameMap[r][c] != landed) r++;
                playerKey = (team == 0) ? '2' : '1';
                break;
            case 'W':
                if(landed == '?') c = -1;
                else if(landed == '.' || landed == '~' || landed == '@') c -= prevEnergy;
                else while(c > 0 && gameMap[r][c] != landed) c--;
                playerKey = (team == 0) ? '4' : '3';
                break;
            case 'E':
                if(landed == '?') c = mapWidth;
                else if(landed == '.' || landed == '~' || landed == '@') c += prevEnergy;
                else while(c < mapWidth && gameMap[r][c] != landed) c++;
                playerKey = (team == 0) ? '6' : '5';
                break;
        }

        // Turn the player correctly
        Image player = sprites.get(playerKey);
        drawCell(canvasContext, playerPos[0] + 1, playerPos[1] + 1, player);
        // Draw explosion according to terrain
        switch (landed) {
            case '~': explosionKey = '-'; break;
            case '@': explosionKey = '+'; break;
            case '?': explosionKey = '/'; break;
            default: explosionKey = '*'; break;
        }
        Image explosion = sprites.get(explosionKey);
        drawCell(canvasContext, c + 1, r + 1, explosion);

        // Redraw square around current player
        double xCanvas = (playerPos[0] + 1)*cellSize, yCanvas = (playerPos[1] + 1)*cellSize;
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

        gameMap[y][x] = hidden_tile;

        int new_x = x, new_y = y;
        switch (direction) {
            case 'N': new_y = y - 1; break;
            case 'S': new_y = y + 1; break;
            case 'W': new_x = x - 1; break;
            case 'E': new_x = x + 1; break;
        }

        if(gameMap[new_y][new_x] == '~') hidden_tile = '~';
        else {
            hidden_tile = '.';
            if(gameMap[new_y][new_x] == '&') {
                switch (direction) {
                    case 'N': gameMap[new_y - 1][x] = '&'; break;
                    case 'S': gameMap[new_y + 1][x] = '&'; break;
                    case 'W': gameMap[y][new_x - 1] = '&'; break;
                    case 'E': gameMap[y][new_x + 1] = '&'; break;
                }
            }
        }
        gameMap[new_y][new_x] = old;
    }
}