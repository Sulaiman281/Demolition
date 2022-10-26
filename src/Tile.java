public class Tile extends Position {

    private TileType type;

    public Tile(int i, int j){
        setPosition(i, j);
    }

    public void setType(char ch){
        if(ch == 'W') type = TileType.SOLD;
        else if(ch == 'B') type = TileType.BREAKABLE;
        else if(ch == 'G') type = TileType.GOAL;
        else type = TileType.EMPTY;
    }

    public void setType(TileType type) {
        this.type = type;
    }

    public TileType getType() {
        return type;
    }
}
