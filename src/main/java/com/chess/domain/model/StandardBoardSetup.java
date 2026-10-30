package com.chess.domain.model;


import java.util.List;
import java.util.Objects;

/**
 * Construye la posición inicial. El tamaño del tablero es la longitud de la fila trasera, por lo que ambos coinciden.
 */
public final class StandardBoardSetup {

    private static final int MIN_SIZE = 4;

    private final IPieceFactory pieceFactory;
    private final List<PieceType> backRank;

    public StandardBoardSetup(IPieceFactory pieceFactory, List<PieceType> backRank) {
        this.pieceFactory = Objects.requireNonNull(pieceFactory, "pieceFactory must not be null");
        Objects.requireNonNull(backRank, "backRank must not be null");
        this.backRank = List.copyOf(backRank);
        validateLayout();
    }

    public Board create() {
        int size = backRank.size();
        Board board = new Board(size);
        for (int x = 0; x < size; x++) {
            placeColumn(board, x, Color.WHITE, 0, 1);
            placeColumn(board, x, Color.BLACK, size - 1, size - 2);
        }
        return board;
    }

    private void placeColumn(Board board, int x, Color color, int backRankY, int pawnRankY) {
        board.placePiece(pieceFactory.create(color, backRank.get(x)), new Position(x, backRankY));
        board.placePiece(pieceFactory.create(color, PieceType.PAWN), new Position(x, pawnRankY));
    }

    private void validateLayout() {
        int size = backRank.size();
        if (size < MIN_SIZE) {
            throw new IllegalArgumentException("The back rank needs at least " + MIN_SIZE + " pieces but has " + size);
        }
        int kingColumn = backRank.indexOf(PieceType.KING);
        if (kingColumn < 0 || kingColumn != backRank.lastIndexOf(PieceType.KING)) {
            throw new IllegalArgumentException("The back rank must contain exactly one king");
        }
        if (kingColumn != size / 2) {
            throw new IllegalArgumentException("A board of size " + size + " needs the king on column " + size / 2
                    + " (castling expects it there) but it is on column " + kingColumn);
        }
    }
}
