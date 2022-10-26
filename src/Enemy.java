import processing.core.PImage;

import java.util.ArrayList;
import java.util.Random;

public class Enemy extends Position{

    public ArrayList<Bomb> bombs = new ArrayList<>();
    public PImage[] images;

    public PlayerType type = PlayerType.PLAYER_FRONT_IDLE;
    public PlayerDirection direction = PlayerDirection.IDLE;

    private Random rand;

    public Enemy(int i, int j){
        setPosition(i, j);
        rand = new Random();
    }

    public void show(App app){
        app.image(images[type.ordinal()], getX(), getY(), App.TILE_WIDTH, App.TILE_HEIGHT);
    }

    public void setType(PlayerType type) {
        this.type = type;
    }

    public PlayerType getType() {
        return type;
    }

    public void randomWalk(char[] moves){
        int index = rand.nextInt(moves.length);
        char nextMove = moves[index];

        if (nextMove == 'w') {
            direction = PlayerDirection.UP;

        } else if (nextMove == 'a') {
            direction = PlayerDirection.LEFT;

        } else if (nextMove == 'd') {
            direction = PlayerDirection.RIGHT;
        } else if (nextMove == 's') {
            direction = PlayerDirection.DOWN;
        } else {
            direction = PlayerDirection.IDLE;
        }
    }
}
