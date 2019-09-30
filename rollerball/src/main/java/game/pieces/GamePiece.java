package game.pieces;

import game.Board;
import game.Game;

public abstract class GamePiece{
    static GamePiece create(int x, int y, Board board){
        char type = board.getBoard()[y][x];
        switch(type){
            case '_':
                return null;
            case 'K': case 'k':
                return new King(x,y,Character.isUpperCase(type));
            case 'R': case 'r':
                return new Rook(x,y,Character.isUpperCase(type));
            default:
                throw new UnsupportedOperationException(String.format("Can't create a game piece at point (%d, %d)", x,y ));
        }
    }

    protected int x, y;
    public Game.Direction getDirection(){
        if((x == 1 && y >=1 && y <=6) ||
                (x==2 && y>=2 && y<=5)){
            return Game.Direction.UP;
        }
        else if((x == 6 && y>=3 && y<=6) ||
                (x == 7 && y >= 2 && y <= 7)){
            return Game.Direction.DOWN;
        }
        else if ((y == 1 && x>=2 && x<=7) ||
                (y == 2 && x >=3 && x<=6 ) ){
            return Game.Direction.LEFT;
        }
        else {
            return Game.Direction.RIGHT;
        }
    }
    public GamePiece(int x, int y, boolean team){
        this.x = x;
        this.y = y;
        this.team = team;
    }

    protected boolean team;

    protected boolean locationIsOnBoard(int toX, int toY){
        return (!(toX <1 || toX > 7 || toY <1 || toY > 7)) &&
                (!(toX > 2 && toX < 6 && toY > 2 && toY < 6));
    }

    public abstract boolean moveIsLegal(int toX, int toY, Board board);

}