package com.chess.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Board {

    private final int size;
    private final Piece[][] squares;
    private final List<Position> positions;

    public Board(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Board size must be positive: " + size);
        }
        this.size = size;
        this.squares = new Piece[size][size];
        this.positions = createPositions(size);
    }

    public int size() {
        return size;
    }

    public boolean contains(int x, int y) {
        return x >= 0 && x < size && y >= 0 && y < size;
    }

    public boolean contains(Position position) {
        Objects.requireNonNull(position, "position must not be null");
        return contains(position.x(), position.y());
    }

    /** Every square of the board, column by column. */
    public List<Position> positions() {
        return positions;
    }

    public Piece pieceAt(Position position) {
        Objects.requireNonNull(position, "position must not be null");
        requireInside(position);
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

    public Position findPiece(Color color, PieceType type) {
        for (Position position : positions) {
            Piece piece = squares[position.x()][position.y()];
            if (piece != null && piece.color() == color && piece.type() == type) {
                return position;
            }
        }
        return null;
    }

    private void requireInside(Position position) {
        if (!contains(position)) {
            throw new IllegalArgumentException("Position is outside the " + size + "x" + size + " board: " + position);
        }
    }

    private static List<Position> createPositions(int size) {
        List<Position> all = new ArrayList<>();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                all.add(new Position(x, y));
            }
        }
        return List.copyOf(all);
    }
}
