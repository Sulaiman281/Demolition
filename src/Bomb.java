public class Bomb extends Position{

    private int timer = 8;
    private int power = 3;
    private BombType type;
    public Bomb(int i, int j){
        setPosition(i, j);
        type = BombType.BOMB;
    }

    public Bomb(Position pos){
        setPosition(pos);
        type = BombType.BOMB;
    }

    public int getPower() {
        return power;
    }

    public void countDown(){
        timer--;
        switch (timer){
            case 8: type = BombType.BOMB; break;
            case 7: type = BombType.BOMB_1; break;
            case 6: type = BombType.BOMB_2; break;
            case 5: type = BombType.BOMB_3; break;
            case 4: type = BombType.BOMB_4; break;
            case 3: type = BombType.BOMB_5; break;
            case 2: type = BombType.BOMB_6; break;
            case 1: type = BombType.BOMB_7; break;
            case 0: type = BombType.BOMB_8; break;
        }
    }

    public void dropTimer(){
        timer = 1;
    }

    public int getTimer() {
        return timer;
    }

    public BombType getType() {
        return type;
    }
}
