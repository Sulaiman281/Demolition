import processing.core.PApplet;
import processing.core.PImage;

import java.net.URISyntaxException;
import java.util.*;

public class App extends PApplet {

    public static final int WIDTH = 480;
    public static final int HEIGHT = 480;

    public static int COL = 15, ROW = 15;

    public static int TILE_WIDTH = WIDTH/COL;
    public static int TILE_HEIGHT = HEIGHT/ROW-2; // game view row start from 2 row first 2 rows are for menu.

    public static final int FPS = 60;

    public Queue<Level> levels = new ArrayDeque<>();
    public Level currentLevel;

    public PImage[] wall_image = new PImage[4];
    public PImage[] player_images = new PImage[20];
    public PImage[] red_images = new PImage[20];
    public PImage[] yellow_images = new PImage[20];
    public PImage[] bomb_images = new PImage[9];
    public PImage[] explosion_images = new PImage[7];
    public PImage liveImage;
    public PImage timerImage;

    public Player player = new Player(-1, -1);

    public ArrayList<Explosion> explosions = new ArrayList<>();

    float moveSpeed = 3.5f;

    float nextLevelDelay = -1f;

    private String gameOverMsg;
    boolean isGameOver = false;

    @Override
    public void setup() {
        frameRate(FPS);
        DataManager manager = new DataManager();
        loadImages();
        manager.readFile(levels, player);
        if(!levels.isEmpty()) {
            currentLevel = levels.poll();
            player.images = player_images;
            player.setPosition(currentLevel.playerSX, currentLevel.playerSY);
            if(currentLevel.getRed() != null)
                currentLevel.getRed().images = red_images;
            if(currentLevel.getYellow() != null)
                currentLevel.getYellow().images = yellow_images;
        }else{
            isGameOver = true;
            gameOverMsg = "No Levels";
        }
    }

    @Override
    public void draw() {
        update();

        if(currentLevel.getTimer() <= 0 && !isGameOver || player.getLives().equals("0")){
            // game over.
            isGameOver = true;
            gameOverMsg = "Game Over";
            gameOverMenu();
        }else if(frameCount % 60 == 0) {
            if(player.reSpawnTime == 0) player.spawn(currentLevel.playerSX, currentLevel.playerSY);
            player.reSpawnTime--;
            currentLevel.updateTimer();
            nextLevelDelay--;

            if(nextLevelDelay == 0){
                isGameOver = false;
                currentLevel = levels.poll();
                player.setPosition(currentLevel.playerSX, currentLevel.playerSY);
                if(currentLevel.getRed() != null)
                    currentLevel.getRed().images = red_images;
                if(currentLevel.getYellow() != null)
                    currentLevel.getYellow().images = yellow_images;
            }
        }

        if(frameCount % 4 == 0) {
            player_update();
            if(currentLevel.getYellow() != null)
                enemy_update(currentLevel.getYellow());
            if(currentLevel.getRed() != null)
                enemy_update(currentLevel.getRed());
        }
        if(frameCount % 6 == 0) {
            update_animations();
            if(currentLevel.getYellow() != null)
                enemy_animation_update(currentLevel.getYellow());
            if(currentLevel.getRed() != null)
                enemy_animation_update(currentLevel.getRed());
        }

        if(frameCount % 3 == 0)
            explosions.forEach(explosion -> explosion.show(this, new Random().nextInt(5)));
        else {
            explosions.forEach(explosion -> {
                explosion.show(this, 0);
                if (explosion.caughtInExplosion(player)) {
                    player.reSpawnTime = 3f;
                    player.die(currentLevel.playerSX, currentLevel.playerSY);
                }
                if (currentLevel.getRed() != null && explosion.caughtInExplosion(currentLevel.getRed())) {
                    currentLevel.setRed(null);
                }
                if (currentLevel.getYellow() != null && explosion.caughtInExplosion(currentLevel.getYellow())) {
                    currentLevel.setYellow(null);
                }
            });
        }
        if(frameCount % 40 == 0){
            for(int i = 0; i< player.bombs.size(); i++){
                player.bombs.get(i).countDown();
                if(player.bombs.get(i).getTimer() <= 0){
                    //explosions
                    explosions.add(new Explosion(player.bombs.get(i), this, ExplosionType.EXPLOSION_CENTER));
                    player.bombs.remove(i--);
                }
            }
            if(currentLevel.getRed() != null) {
                for (int i = 0; i < currentLevel.getRed().bombs.size(); i++) {
                    currentLevel.getRed().bombs.get(i).countDown();
                    if (currentLevel.getRed().bombs.get(i).getTimer() <= 0) {
                        //explosions
                        explosions.add(new Explosion(currentLevel.getRed().bombs.get(i), this, ExplosionType.EXPLOSION_CENTER));
                        currentLevel.getRed().bombs.remove(i--);
                    }
                }
            }
            if(currentLevel.getYellow() != null) {
                for (int i = 0; i < currentLevel.getYellow().bombs.size(); i++) {
                    currentLevel.getYellow().bombs.get(i).countDown();
                    if (currentLevel.getYellow().bombs.get(i).getTimer() <= 0) {
                        //explosions
                        explosions.add(new Explosion(currentLevel.getYellow().bombs.get(i), this, ExplosionType.EXPLOSION_CENTER));
                        currentLevel.getYellow().bombs.remove(i--);
                    }
                }
            }
            for(int i = 0; i< explosions.size(); i++){
                if(explosions.get(i).updateTime() <= 0) explosions.remove(i--);
            }
        }
    }

    void update(){
        background(255, 171, 82);
        fill(255, 171, 82);
        if(!isGameOver) {
            drawGrid();
            drawIcons();
            drawBombs();
            if(player.reSpawnTime < 0) {
                drawPlayer();
            }
            if(currentLevel.getRed() != null) {
                currentLevel.getRed().show(this);
            }
            if(currentLevel.getYellow() != null) {
                currentLevel.getYellow().show(this);
            }
            bombInput();
        }else{
            gameOverMenu();
        }
    }

    void drawPlayer(){
        image(player_images[player.getType().ordinal()], player.getX(), player.getY(), TILE_WIDTH, TILE_HEIGHT);
    }

    void gameOverMenu(){
        fill(0);
        textSize(50);
        textAlign(CENTER);
        text(gameOverMsg, WIDTH/2f , HEIGHT/2f);
    }

    void drawIcons(){
        fill(0);
        textSize(TILE_WIDTH/1.3f);
        textAlign(CENTER,CENTER);
        text(player.getLives(), COL/3.2f*TILE_WIDTH, .85f * TILE_HEIGHT);
        image(liveImage,COL/4.5f*TILE_WIDTH,.5f * TILE_HEIGHT);
        text(String.valueOf(currentLevel.getTimer()), COL/1.25f*TILE_WIDTH, .87f * TILE_HEIGHT);
        image(timerImage,COL/1.5f*TILE_WIDTH,.5f * TILE_HEIGHT);
    }

    public void drawGrid(){
        for(int i = 0; i < COL; i++){
            for(int j = 0; j< ROW; j++){
                if(j < 2){
                    stroke(255, 171, 82);
                    rect(i*TILE_WIDTH, j* TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
                }else{
                    Tile tile = currentLevel.getGrid()[i][j-2];
                    image(wall_image[tile.getType().ordinal()], tile.getWordX(), tile.getWordY(), TILE_WIDTH, TILE_HEIGHT);
                }
            }
        }
    }

    void drawBombs(){
        for (Bomb bomb : player.bombs) {
            image(bomb_images[bomb.getType().ordinal()], bomb.getWordX(), bomb.getWordY(), TILE_WIDTH, TILE_HEIGHT);
        }
        if(currentLevel.getYellow() != null){
            for (Bomb bomb : currentLevel.getYellow().bombs) {
                image(bomb_images[bomb.getType().ordinal()], bomb.getWordX(), bomb.getWordY(), TILE_WIDTH, TILE_HEIGHT);
            }
        }
        if(currentLevel.getRed() != null){
            for (Bomb bomb : currentLevel.getRed().bombs) {
                image(bomb_images[bomb.getType().ordinal()], bomb.getWordX(), bomb.getWordY(), TILE_WIDTH, TILE_HEIGHT);
            }
        }
    }

    public void settings() {
        size(WIDTH, HEIGHT);
    }

    void player_update(){
        switch(player.direction()){
            case IDLE:
                break;
            case UP:
                if(!collisionEnter(player, 0, -moveSpeed, true))
                    player.move(0, -moveSpeed);
                break;
            case LEFT:
                if(!collisionEnter(player, -moveSpeed, 0, true))
                    player.move(-moveSpeed, 0);
                break;
            case RIGHT:
                if(!collisionEnter(player, moveSpeed, 0, true))
                    player.move(moveSpeed, 0);
                break;
            case DOWN:
                if(!collisionEnter(player, 0, moveSpeed, true))
                    player.move(0, moveSpeed);
                break;
        }
    }
    void enemy_update(Enemy enemy){

        switch(enemy.direction){
            case IDLE:
                enemy.randomWalk(new char[]{ 'a', 'w', 's', 'd'});
                break;
            case UP:
                if(!collisionEnter(enemy, 0, -moveSpeed, false)) {
                    enemy.move(0, -moveSpeed);
                    enemy_bombPlace(enemy, 0, -moveSpeed);
                    enemy.randomWalk(new char[]{ 'w'});
                }
                else{
                    enemy.randomWalk(new char[]{ 'a', 's', 'd'});
                }
                break;
            case LEFT:
                if(!collisionEnter(enemy, -moveSpeed, 0, false)) {
                    enemy.move(-moveSpeed, 0);
                    enemy_bombPlace(enemy, -moveSpeed, 0);
                    enemy.randomWalk(new char[]{ 'a'});
                } else{
                    enemy.randomWalk(new char[]{ 's', 'w', 'd'});
                }
                break;
            case RIGHT:
                if(!collisionEnter(enemy, moveSpeed, 0, false)) {
                    enemy.move(moveSpeed, 0);
                    enemy_bombPlace(enemy, moveSpeed, 0);
                    enemy.randomWalk(new char[]{ 'd'});
                } else{
                    enemy.randomWalk(new char[]{ 'a', 'w', 's'});
                }
                break;
            case DOWN:
                if(!collisionEnter(enemy, 0, moveSpeed, false)) {
                    enemy.move(0, moveSpeed);
                    enemy_bombPlace(enemy, 0, moveSpeed);
                    enemy.randomWalk(new char[]{ 's'});
                } else{
                    enemy.randomWalk(new char[]{ 'a', 'w', 'd'});
                }
                break;
        }
    }
    
    public void enemy_bombPlace(Enemy e, float i , float j){
        int x = Math.round((e.getX()+i)/TILE_WIDTH);
        int y = Math.round((e.getY()+j)/TILE_HEIGHT);

        Tile tile = currentLevel.getGrid()[x][y-2];
        if(tile != null && tile.getType() == TileType.BREAKABLE){
            e.bombs.add(new Bomb(x, y));
        }
    }
    
    void update_animations(){
        switch(player.direction()){
            case IDLE:
                break;
            case UP:
                player.setType(player.getType() == PlayerType.PLAYER_BACK_MOVING ? PlayerType.PLAYER_BACK_MOVING_LF :
                        player.getType() == PlayerType.PLAYER_BACK_MOVING_LF ? PlayerType.PLAYER_BACK_MOVING_RF : PlayerType.PLAYER_BACK_MOVING);
                break;
            case LEFT:
                player.setType(player.getType() == PlayerType.PLAYER_LEFT_MOVING ? PlayerType.PLAYER_LEFT_MOVING_LF :
                        player.getType() == PlayerType.PLAYER_LEFT_MOVING_LF ? PlayerType.PLAYER_LEFT_MOVING_RF : PlayerType.PLAYER_LEFT_MOVING);
                break;
            case RIGHT:
                player.setType(player.getType() == PlayerType.PLAYER_RIGHT_MOVING ? PlayerType.PLAYER_RIGHT_MOVING_LF :
                        player.getType() == PlayerType.PLAYER_RIGHT_MOVING_LF ? PlayerType.PLAYER_RIGHT_MOVING_RF : PlayerType.PLAYER_RIGHT_MOVING);
                break;
            case DOWN:
                player.setType(player.getType() == PlayerType.PLAYER_FRONT_MOVING ? PlayerType.PLAYER_FRONT_MOVING_LF :
                        player.getType() == PlayerType.PLAYER_FRONT_MOVING_LF ? PlayerType.PLAYER_FRONT_MOVING_RF : PlayerType.PLAYER_FRONT_MOVING);
                break;
        }
    }
    void enemy_animation_update(Enemy enemy){
        switch(enemy.direction){
            case IDLE:
                break;
            case UP:
                enemy.setType(enemy.getType() == PlayerType.PLAYER_BACK_MOVING ? PlayerType.PLAYER_BACK_MOVING_LF :
                        enemy.getType() == PlayerType.PLAYER_BACK_MOVING_LF ? PlayerType.PLAYER_BACK_MOVING_RF : PlayerType.PLAYER_BACK_MOVING);
                break;
            case LEFT:
                enemy.setType(enemy.getType() == PlayerType.PLAYER_LEFT_MOVING ? PlayerType.PLAYER_LEFT_MOVING_LF :
                        enemy.getType() == PlayerType.PLAYER_LEFT_MOVING_LF ? PlayerType.PLAYER_LEFT_MOVING_RF : PlayerType.PLAYER_LEFT_MOVING);
                break;
            case RIGHT:
                enemy.setType(enemy.getType() == PlayerType.PLAYER_RIGHT_MOVING ? PlayerType.PLAYER_RIGHT_MOVING_LF :
                        enemy.getType() == PlayerType.PLAYER_RIGHT_MOVING_LF ? PlayerType.PLAYER_RIGHT_MOVING_RF : PlayerType.PLAYER_RIGHT_MOVING);
                break;
            case DOWN:
                enemy.setType(enemy.getType() == PlayerType.PLAYER_FRONT_MOVING ? PlayerType.PLAYER_FRONT_MOVING_LF :
                        enemy.getType() == PlayerType.PLAYER_FRONT_MOVING_LF ? PlayerType.PLAYER_FRONT_MOVING_RF : PlayerType.PLAYER_FRONT_MOVING);
                break;
        }
    }

    @Override
    public void keyPressed() {
        char input = key;
        if(player.reSpawnTime < 0) {
            if (input == 'w') {
                player.setDir(PlayerDirection.UP);

            } else if (input == 'a') {
                player.setDir(PlayerDirection.LEFT);

            } else if (input == 'd') {
                player.setDir(PlayerDirection.RIGHT);
            } else if (input == 's') {
                player.setDir(PlayerDirection.DOWN);
            } else {
                player.setDir(PlayerDirection.IDLE);
            }
        }

    }

    public void bombInput(){
        if(key == ' '){

            int x = Math.round((player.getX())/TILE_WIDTH);
            int y = Math.round((player.getY())/TILE_HEIGHT);

            // if the bomb is on same location.
            boolean cond = false;
            for (Bomb bomb : player.bombs) {
                if(bomb.isEqual(x, y)) cond = true;
            }
            if(!cond)
                player.bombs.add(new Bomb(x, y));
        }
    }

    boolean collisionEnter(Position pos, float i, float j, boolean isPlayer){

        int x = Math.round((pos.getX()+i)/(TILE_WIDTH));
        int y = Math.round((pos.getY()+j)/TILE_HEIGHT);

        boolean cond = false;
        if(x >= 0 && x < COL){
            if(y >= 2 && y < ROW){
                Tile tile = currentLevel.getGrid()[x][y-2];
                if(tile.isEqual(new Player(x, y)) && tile.getType() == TileType.GOAL && isPlayer) {
                    // won the player
                    if (levels.isEmpty()) {
                        isGameOver = true;
                        gameOverMsg = "You Win!";
                    } else {
                        nextLevelDelay = 4f;
                        gameOverMsg = "Loading Next Level";
                        isGameOver = true;
                    }
                    gameOverMenu();
                    return false;
                }
                cond = tile.isEqual(new Player(x, y)) && tile.getType() != TileType.EMPTY;
            }
        }
        return cond;
    }

    @Override
    public void keyReleased() {
        switch (key){
            case UP:{
                player.setType(PlayerType.PLAYER_BACK_IDLE);
                break;
            }
            case LEFT:
            {
                player.setType(PlayerType.PLAYER_LEFT_IDLE);
                break;
            }
            case RIGHT:{
                player.setType(PlayerType.PLAYER_RIGHT_IDLE);
                break;
            }
            case DOWN:{
                player.setType(PlayerType.PLAYER_FRONT_IDLE);
                break;
            }
        }
        player.setDir(PlayerDirection.IDLE);
    }

    public static Tile getTile(int i, int j, char type){
        Tile tile = new Tile(i, j);
        tile.setType(type);
        return tile;
    }

    public void loadImages(){
        try {
            // loading walls
            wall_image[0] = this.loadImage(getClass().getResource("/empty/empty.png").toURI().getPath());
            wall_image[1] = this.loadImage(getClass().getResource("/wall/solid.png").toURI().getPath());
            wall_image[2] = this.loadImage(getClass().getResource("/broken/broken.png").toURI().getPath());
            wall_image[3] = this.loadImage(getClass().getResource("/goal/goal.png").toURI().getPath());

            // load player pictures
            player_images[0] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player1.png").toURI().getPath());
            player_images[1] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player.gif").toURI().getPath());
            player_images[2] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player2.png").toURI().getPath());
            player_images[3] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player3.png").toURI().getPath());
            player_images[4] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player4.png").toURI().getPath());
            player_images[5] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player_left1.png").toURI().getPath());
            player_images[6] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player_left.gif").toURI().getPath());
            player_images[7] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player_left2.png").toURI().getPath());
            player_images[8] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player_left3.png").toURI().getPath());
            player_images[9] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player_left4.png").toURI().getPath());
            player_images[10] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player_right1.png").toURI().getPath());
            player_images[11] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player_right.gif").toURI().getPath());
            player_images[12] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player_right2.png").toURI().getPath());
            player_images[13] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player_right3.png").toURI().getPath());
            player_images[14] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player_right4.png").toURI().getPath());
            player_images[15] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player_up1.png").toURI().getPath());
            player_images[16] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player_up.gif").toURI().getPath());
            player_images[17] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player_up2.png").toURI().getPath());
            player_images[18] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player_up3.png").toURI().getPath());
            player_images[19] = this.loadImage(getClass().getResource("/currentLevel.getWhite()/player_up4.png").toURI().getPath());

            // load red player pictures
            red_images[0] = this.loadImage(getClass().getResource("/red_enemy/red_down1.png").toURI().getPath());
            red_images[1] = this.loadImage(getClass().getResource("/red_enemy/red_down.gif").toURI().getPath());
            red_images[2] = this.loadImage(getClass().getResource("/red_enemy/red_down2.png").toURI().getPath());
            red_images[3] = this.loadImage(getClass().getResource("/red_enemy/red_down3.png").toURI().getPath());
            red_images[4] = this.loadImage(getClass().getResource("/red_enemy/red_down4.png").toURI().getPath());
            red_images[5] = this.loadImage(getClass().getResource("/red_enemy/red_left1.png").toURI().getPath());
            red_images[6] = this.loadImage(getClass().getResource("/red_enemy/red_left.gif").toURI().getPath());
            red_images[7] = this.loadImage(getClass().getResource("/red_enemy/red_left2.png").toURI().getPath());
            red_images[8] = this.loadImage(getClass().getResource("/red_enemy/red_left3.png").toURI().getPath());
            red_images[9] = this.loadImage(getClass().getResource("/red_enemy/red_left4.png").toURI().getPath());
            red_images[10] = this.loadImage(getClass().getResource("/red_enemy/red_right1.png").toURI().getPath());
            red_images[11] = this.loadImage(getClass().getResource("/red_enemy/red_right.gif").toURI().getPath());
            red_images[12] = this.loadImage(getClass().getResource("/red_enemy/red_right2.png").toURI().getPath());
            red_images[13] = this.loadImage(getClass().getResource("/red_enemy/red_right3.png").toURI().getPath());
            red_images[14] = this.loadImage(getClass().getResource("/red_enemy/red_right4.png").toURI().getPath());
            red_images[15] = this.loadImage(getClass().getResource("/red_enemy/red_up1.png").toURI().getPath());
            red_images[16] = this.loadImage(getClass().getResource("/red_enemy/red_up.gif").toURI().getPath());
            red_images[17] = this.loadImage(getClass().getResource("/red_enemy/red_up2.png").toURI().getPath());
            red_images[18] = this.loadImage(getClass().getResource("/red_enemy/red_up3.png").toURI().getPath());
            red_images[19] = this.loadImage(getClass().getResource("/red_enemy/red_up4.png").toURI().getPath());

            // load yellow player pictures
            yellow_images[0] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_down1.png").toURI().getPath());
            yellow_images[1] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_down3.png").toURI().getPath());
            yellow_images[2] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_down2.png").toURI().getPath());
            yellow_images[3] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_down3.png").toURI().getPath());
            yellow_images[4] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_down4.png").toURI().getPath());
            yellow_images[5] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_left1.png").toURI().getPath());
            yellow_images[6] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_left3.png").toURI().getPath());
            yellow_images[7] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_left2.png").toURI().getPath());
            yellow_images[8] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_left3.png").toURI().getPath());
            yellow_images[9] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_left4.png").toURI().getPath());
            yellow_images[10] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_right1.png").toURI().getPath());
            yellow_images[11] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_right3.png").toURI().getPath());
            yellow_images[12] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_right2.png").toURI().getPath());
            yellow_images[13] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_right3.png").toURI().getPath());
            yellow_images[14] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_right4.png").toURI().getPath());
            yellow_images[15] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_up1.png").toURI().getPath());
            yellow_images[16] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_up3.png").toURI().getPath());
            yellow_images[17] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_up2.png").toURI().getPath());
            yellow_images[18] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_up3.png").toURI().getPath());
            yellow_images[19] = this.loadImage(getClass().getResource("/yellow_enemy/yellow_up4.png").toURI().getPath());

            //load bomb images.
            bomb_images[0] = this.loadImage(getClass().getResource("/bomb/bomb.png").toURI().getPath());
            bomb_images[1] = this.loadImage(getClass().getResource("/bomb/bomb1.png").toURI().getPath());
            bomb_images[2] = this.loadImage(getClass().getResource("/bomb/bomb2.png").toURI().getPath());
            bomb_images[3] = this.loadImage(getClass().getResource("/bomb/bomb3.png").toURI().getPath());
            bomb_images[4] = this.loadImage(getClass().getResource("/bomb/bomb4.png").toURI().getPath());
            bomb_images[5] = this.loadImage(getClass().getResource("/bomb/bomb5.png").toURI().getPath());
            bomb_images[6] = this.loadImage(getClass().getResource("/bomb/bomb6.png").toURI().getPath());
            bomb_images[7] = this.loadImage(getClass().getResource("/bomb/bomb7.png").toURI().getPath());
            bomb_images[8] = this.loadImage(getClass().getResource("/bomb/bomb8.png").toURI().getPath());

            //load explosions
            explosion_images[0] = this.loadImage(getClass().getResource("/explosion/centre.png").toURI().getPath());
            explosion_images[1] = this.loadImage(getClass().getResource("/explosion/end_bottom.png").toURI().getPath());
            explosion_images[2] = this.loadImage(getClass().getResource("/explosion/end_left.png").toURI().getPath());
            explosion_images[3] = this.loadImage(getClass().getResource("/explosion/end_right.png").toURI().getPath());
            explosion_images[4] = this.loadImage(getClass().getResource("/explosion/end_top.png").toURI().getPath());
            explosion_images[5] = this.loadImage(getClass().getResource("/explosion/vertical.png").toURI().getPath());
            explosion_images[6] = this.loadImage(getClass().getResource("/explosion/horizontal.png").toURI().getPath());

            // load other images
            liveImage = this.loadImage(getClass().getResource("/icons/player.png").toURI().getPath());
            timerImage = this.loadImage(getClass().getResource("/icons/clock.png").toURI().getPath());

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
