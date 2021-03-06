package game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class ChessBoardTest {
    private ChessBoard board;
    @BeforeEach
    public void createBoard(){
        board = new ChessBoard();
    }

    @Test
    void initialize() throws IllegalPositionException {
        board.initialize();

        for(int row=0;row<=6;row++){
            for (int col=0;col<=6;col++){
                ChessPiece piece = board.getPiece((char)('a'+col)+""+(char)('1'+row));
                if(row>=2 && row <=4 ){
                    assertEquals(null, piece, "There should be no pieces outside of starting rows");
                }
                else{
                    if(piece == null){
                        assertTrue(col<2 || col > 4, "no piece only in empty columns");
                        continue;
                    }
                    assertFalse((row < 2 && piece.getColor() != ChessPiece.Color.WHITE) || (row > 5 && piece.getColor() != ChessPiece.Color.BLACK)
                    , "Color should match row");
                    switch (row <2? col: 6-col){
                        case 3: assertTrue((piece instanceof Bishop && (row ==0 || row ==6)) ||
                                (piece instanceof King && (row == 1 || row == 5)), "Piece should be bishop/king"); break;
                        case 2: assertTrue(piece instanceof Pawn, "Piece should be pawn"); break;
                        case 4: assertTrue(piece instanceof Rook, "Piece should be rook"); break;
                    }
                }
            }
        }
    }

    @Test
    void getPiece() throws IllegalPositionException {
        assertThrows(IllegalPositionException.class, ()->board.getPiece("a9"));
        assertThrows(IllegalPositionException.class, ()->board.getPiece("a1."));
        assertThrows(IllegalPositionException.class, ()->board.getPiece(null));
        assertThrows(IllegalPositionException.class, ()->board.getPiece(""));
        assertThrows(IllegalPositionException.class, ()->board.getPiece("c"));
        assertThrows(IllegalPositionException.class, ()->board.getPiece(".c8"));
        assertEquals(board.getPiece("b3"), null);
        Pawn pawn=new Pawn(board, ChessPiece.Color.BLACK);
        board.placePiece(pawn, "b7");
        assertEquals(board.getPiece("b7"), pawn);
    }

    @Test
    void placePiece() throws IllegalPositionException{
        Pawn piece = new Pawn(board, ChessPiece.Color.BLACK);
        assertFalse(board.placePiece(piece, "a9"));
        assertFalse(board.placePiece(piece, "a1."));
        assertFalse(board.placePiece(piece, null));
        assertFalse(board.placePiece(piece, ""));
        assertFalse(board.placePiece(piece, "c"));
        assertFalse(board.placePiece(piece, ".c8"));
        assertTrue(board.placePiece(piece, "a2"));
        assertEquals(piece.getPosition(), "a2", "Piece position should change after successful placement.");
        ChessPiece sameSidePiece = new Pawn(board, ChessPiece.Color.BLACK);
        assertFalse(board.placePiece(sameSidePiece, "a2"), "Shouldn't be able to capture same side pieces");
        assertNotEquals(sameSidePiece, board.getPiece("a2"), "Piece should not have replaced existing one");
        Pawn opposingPiece = new Pawn(board, ChessPiece.Color.WHITE);
        assertTrue(board.placePiece(opposingPiece, "a2"), "Should be able to capture opposing pieces");
        assertEquals(opposingPiece, board.getPiece("a2"), "Opposing piece should replace current");
    }

    @Test
    void placeNullPiece(){
        assertFalse(board.placePiece(null, "b4"), "Shouldn't be able to place null piece");
    }

    @Test
    void move() throws IllegalPositionException {
        ChessPiece piece =  new Pawn(board, ChessPiece.Color.WHITE);
        assertTrue(board.placePiece(piece, "g2"));
        assertThrows(IllegalMoveException.class, ()->board.move("g2", "h1"));
        assertThrows(IllegalMoveException.class, ()->board.move("g2", "i3"));
        assertThrows(IllegalMoveException.class, ()->board.move("g2", null));
        assertThrows(IllegalMoveException.class, ()->board.move(null, "g1"));
        assertThrows(IllegalMoveException.class, ()->board.move(null, null));
        assertThrows(IllegalMoveException.class, ()->board.move("f2", "g1"));
        assertDoesNotThrow(()-> board.move("g2", "g1"));
        assertEquals(piece.getPosition(), "g1");
        assertEquals(piece, board.getPiece("g1"));

    }

    @Test
    void byteSerializationSimple() throws IllegalPositionException {
        board.initialize();
        byte[] serialized = board.serializeToBytes();
        ChessBoard deserialized = new ChessBoard(serialized);
        compareBoards(board, deserialized);
    }

    @Test
    void byteSerializationComplex() throws IllegalPositionException {
        assertTrue(board.placePiece(new King(board, ChessPiece.Color.BLACK), "g7"));
        assertTrue(board.placePiece(new King(board, ChessPiece.Color.WHITE), "a1"));
        assertTrue(board.placePiece(new Rook(board, ChessPiece.Color.WHITE), "b4"));
        assertTrue(board.placePiece(new Rook(board, ChessPiece.Color.WHITE), "a7"));
        assertTrue(board.placePiece(new Rook(board, ChessPiece.Color.WHITE), "c2"));
        assertTrue(board.placePiece(new Bishop(board, ChessPiece.Color.WHITE), "f5"));
        assertTrue(board.placePiece(new Bishop(board, ChessPiece.Color.WHITE), "g6"));
        byte[] serialized = board.serializeToBytes();
        ChessBoard deserialized = new ChessBoard(serialized);
        compareBoards(board, deserialized);
    }

    public void compareBoards(ChessBoard board, ChessBoard deserialized) throws IllegalPositionException {
        for(char row = '1'; row <= '7'; row += 1){
            for(char col = 'a'; col <= 'g'; col+=1){
                String position = col+""+row;
                ChessPiece originalPiece = board.getPiece(position);
                ChessPiece deserializedPiece = deserialized.getPiece(position);
                if(originalPiece == null)
                    assertNull(deserializedPiece, "Null/Not null mismatch at "+position);
                else{
                    assertEquals(originalPiece.toString(), deserializedPiece.toString(), "Piece mismatch at "+position);
                }
            }
        }
    }


  @Test
  void moveCheck() throws IllegalMoveException {

	board.placePiece(new King(board, ChessPiece.Color.WHITE), "b4");
	board.placePiece(new Bishop(board, ChessPiece.Color.WHITE), "f3");
	board.placePiece(new King(board, ChessPiece.Color.BLACK), "b6");
	board.placePiece(new Rook(board, ChessPiece.Color.BLACK), "b2");

	assertThrows(IllegalMoveException.class, ()->board.move("f3", "d1"));
	assertDoesNotThrow(()-> board.move("b4", "a3"));
    }

    @Test
    void moveCheckPin() throws IllegalMoveException {
	board.placePiece(new King(board, ChessPiece.Color.WHITE), "a1");
	board.placePiece(new Pawn(board, ChessPiece.Color.WHITE), "b1");
	board.placePiece(new King(board, ChessPiece.Color.BLACK), "a5");
	board.placePiece(new Rook(board, ChessPiece.Color.BLACK), "f4");
	
	assertDoesNotThrow(()-> board.move("f4", "f1")); //move should allow this, as black is not in check
	assertThrows(IllegalMoveException.class, ()->board.move("b1", "a2")); //should not allow this, as it would put white in check
    }

}
