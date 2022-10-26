public class Level {

    public int playerSX, playerSY;

    private Tile[][] grid = new Tile[App.COL][App.ROW-2];
    private int timer;
    private Enemy red;
    private Enemy yellow;

    public Level(){

    }

    public Level(Enemy r, Enemy y, Tile[][] grid, int timer) {
        this.grid = grid;
        this.timer = timer;
        red = r;
        yellow = y;
    }

    public void setRed(Enemy red) {
        this.red = red;
    }

    public void setYellow(Enemy yellow) {
        this.yellow = yellow;
    }

    public Enemy getRed() {
        return red;
    }

    public Enemy getYellow() {
        return yellow;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public int getTimer() {
        return timer;
    }

    public int updateTimer(){
        return timer--;
    }

    public void setGrid(Tile[][] grid) {
        this.grid = grid;
    }

    public Tile[][] getGrid() {
        return grid;
    }
}
