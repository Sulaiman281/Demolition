import java.util.ArrayList;

public class Explosion extends Position{

    private int time;
    private ExplosionType type;

    private ArrayList<Explosion> children = new ArrayList<>();

    public Explosion(Bomb bomb, App app, ExplosionType type){
        time = 2;
        this.type = type;
        setPosition(bomb);
        int x = (int) bomb.getX();
        int y = (int) bomb.getY();
        for (Bomb b : app.player.bombs) {
            if(b.isEqual(this)){
                b.dropTimer();
            }
        }
        Tile tile = null;
        try{
            tile = app.currentLevel.getGrid()[x][y-2];
        } catch (Exception ignored) {

        }

        if(tile != null) {
            if(tile.getType() == TileType.BREAKABLE) tile.setType(TileType.EMPTY);
        }

        if(type == ExplosionType.EXPLOSION_CENTER) {
            for (int i = 1; i < bomb.getPower(); i++) {
                int local_x = x - i;
                if(local_x < 0) break;

                tile = app.currentLevel.getGrid()[local_x][y-2];
                if(tile != null && tile.getType() != TileType.EMPTY) {
                    i = bomb.getPower();
                }

                children.add(new Explosion((Bomb) bomb.setPosition(local_x, y), app, i == bomb.getPower() - 1
                        ? ExplosionType.EXPLOSION_END_LEFT : ExplosionType.EXPLOSION_HORIZONTAL));
            }
            for (int i = 1; i < bomb.getPower(); i++) {
                int local_x = x + i;
                if(local_x > App.COL) break;
                tile = app.currentLevel.getGrid()[local_x][y-2];
                if(tile != null && tile.getType() != TileType.EMPTY) {
                    i = bomb.getPower();
                }
                children.add(new Explosion((Bomb) bomb.setPosition(local_x, y), app, i == bomb.getPower() - 1
                        ? ExplosionType.EXPLOSION_END_RIGHT : ExplosionType.EXPLOSION_HORIZONTAL));
            }
            for (int i = 1; i < bomb.getPower(); i++) {
                int local_y = y - i;
                if(local_y < 0) break;
                tile = app.currentLevel.getGrid()[x][local_y-2];

                if(tile != null && tile.getType() != TileType.EMPTY) {
                    i = bomb.getPower();
                }

                children.add(new Explosion((Bomb) bomb.setPosition(x, local_y), app, i == bomb.getPower() - 1
                        ? ExplosionType.EXPLOSION_END_TOP : ExplosionType.EXPLOSION_VERTICAL));
            }
            for (int i = 1; i < bomb.getPower(); i++) {
                int local_y = y + i;
                if(local_y > App.ROW-2) break;
                tile = app.currentLevel.getGrid()[x][local_y-2];
                if(tile != null && tile.getType() != TileType.EMPTY) {
                    i = bomb.getPower();
                }

                children.add(new Explosion((Bomb) bomb.setPosition(x, local_y), app, i == bomb.getPower() - 1
                        ? ExplosionType.EXPLOSION_END_BOTTOM : ExplosionType.EXPLOSION_VERTICAL));

            }
        }
    }

    public void show(App app, int size){
        app.image(app.explosion_images[type.ordinal()], getWordX(), getWordY(), App.TILE_WIDTH-size,App.TILE_HEIGHT-size);
        for (Explosion child : children) {
            app.image(app.explosion_images[child.type.ordinal()], child.getWordX(), child.getWordY(), App.TILE_WIDTH-size, App.TILE_HEIGHT-size);
        }
    }

    public boolean caughtInExplosion(Position pos){
        int x = Math.round((pos.getX())/App.TILE_WIDTH);
        int y = Math.round((pos.getY())/App.TILE_HEIGHT);

        if(pos.isEqual(x, y)) return true;
        for (Explosion child : children) {
            if(child.isEqual(x, y)) return true;
        }
        return false;
    }

    public int updateTime(){
        return --time;
    }
}
