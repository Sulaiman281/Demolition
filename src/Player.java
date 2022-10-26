import processing.core.PImage;

import java.util.ArrayList;

public class Player extends Position{

    private PlayerType type;
    private PlayerDirection dir;
    private int lives;
    public float reSpawnTime = -1f;

    public PImage[] images;
    public ArrayList<Bomb> bombs = new ArrayList<>();


    public Player(int i, int j){
        setPosition(i, j);
        type = PlayerType.PLAYER_FRONT_IDLE;
        dir = PlayerDirection.IDLE;
    }

    public void setType(PlayerType type) {
        this.type = type;
    }

    public PlayerType getType() {
        return type;
    }

    public PlayerDirection direction(){
        return dir;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public String getLives(){
        return String.valueOf(lives);
    }

    public void die(int sX, int sY){
        if(lives == 0){
            // game over
            return;
        }
        reSpawnTime = 3f;
        setPosition(-sX*App.TILE_WIDTH, -sY* App.TILE_HEIGHT);
        type = PlayerType.PLAYER_FRONT_IDLE;
        dir = PlayerDirection.IDLE;
        lives--;
    }
    public void setDir(PlayerDirection dir) {
        this.dir = dir;
    }

    public void spawn(int playerSX, int playerSY){
        setPosition(playerSX, playerSY);
    }
}
