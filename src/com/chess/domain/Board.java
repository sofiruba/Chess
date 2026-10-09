package com.chess.domain;

import java.util.Objects;

public final class Board {

    public static final int SIZE = 8;
    private final Piece[][] squares;

    public Board() {
        this.squares = new Piece[SIZE][SIZE];
    }

    public Piece pieceAt(Position position) {
        Objects.requireNonNull(position, "position must not be null");
        return squares[position.x()][position.y()];
    }

    public void placePiece(Piece piece, Position position) {
        Objects.requireNonNull(piece, "piece must not be null");
        Objects.requireNonNull(position, "position must not be null");
        if (pieceAt(position) != null) {
            throw new IllegalStateException("Position is occupied: " + position);
        }
        squares[position.x()][position.y()] = piece;
    }

    public Piece removePiece(Position position) {
        Objects.requireNonNull(position, "position must not be null");
        Piece removed = pieceAt(position);
        squares[position.x()][position.y()] = null;
        return removed;
    }

    public Piece movePiece(Position from, Position to) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        if (from.equals(to)) {
            throw new IllegalArgumentException("A piece cannot move to the same position");
        }
        Piece movingPiece = pieceAt(from);
        if (movingPiece == null) {
            throw new IllegalArgumentException("There is no piece at " + from);
        }
        Piece capturedPiece = pieceAt(to);
        squares[from.x()][from.y()] = null;
        squares[to.x()][to.y()] = movingPiece;
        return capturedPiece;
    }
}
