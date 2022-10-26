public abstract class Position {
    private float x;
    private float y;

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getWordY() {
        return (int) (y * App.TILE_HEIGHT);
    }

    public int getWordX() {
        return (int) (x * App.TILE_WIDTH);
    }

    public Position setPosition(int i , int j){
        x = i;
        y = j;
        return this;
    }

    public void move(float i, float j) {
        x += i;
        y += j;
    }

    public Position setPosition(Position pos){
        this.x = pos.x;
        this.y = pos.y;
        return this;
    }

    public boolean isEqual(Position pos){
        return pos.x == this.x && pos.y == this.y;
    }
    public boolean isEqual(int i, int j){
        return i == this.x && j == this.y;
    }

    public Position getPosition(){
        return this;
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
