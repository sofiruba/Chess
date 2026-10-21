package com.chess.domain;

import java.util.ArrayList;
import java.util.List;

public final class PawnMoveStrategy implements IMoveStrategy {

    @Override
    public List<Position> getValidMoves(Board board, Position currentPosition) {
        Piece pawn = requirePawn(board, currentPosition);
        int direction = forward(pawn);
        List<Position> moves = new ArrayList<>(forwardMoves(board, currentPosition, pawn, direction));
        for (int deltaX : new int[]{-1, 1}) {
            addCapture(board, pawn, currentPosition, deltaX, direction, moves);
        }
        return List.copyOf(moves);
    }

    @Override
    public List<Position> getAttackedMoves(Board board, Position currentPosition) {
        Piece pawn = requirePawn(board, currentPosition);
        int direction = forward(pawn);
        List<Position> attacks = new ArrayList<>();
        for (int deltaX : new int[]{-1, 1}) {
            int x = currentPosition.x() + deltaX;
            int y = currentPosition.y() + direction;
            if (board.contains(x, y)) {
                attacks.add(new Position(x, y));
            }
        }
        return List.copyOf(attacks);
    }

    private Piece requirePawn(Board board, Position position) {
        Piece pawn = board.pieceAt(position);
        if (pawn == null) {
            throw new IllegalArgumentException("There is no piece at " + position);
        }
        return pawn;
    }

    private int forward(Piece pawn) {
        return pawn.color() == Color.WHITE ? 1 : -1;
    }

    private int initialRank(Board board, Piece pawn) {
        return pawn.color() == Color.WHITE ? 1 : board.size() - 2;
    }

    private List<Position> forwardMoves(Board board, Position from, Piece pawn, int direction) {
        List<Position> moves = new ArrayList<>();
        int oneStepY = from.y() + direction;
        if (!board.contains(from.x(), oneStepY) || board.pieceAt(new Position(from.x(), oneStepY)) != null) {
            return moves;
        }
        moves.add(new Position(from.x(), oneStepY));
        int twoStepY = from.y() + 2 * direction;
        if (from.y() == initialRank(board, pawn) && isFree(board, from.x(), twoStepY)) {
            moves.add(new Position(from.x(), twoStepY));
        }
        return moves;
    }

    private boolean isFree(Board board, int x, int y) {
        return board.contains(x, y) && board.pieceAt(new Position(x, y)) == null;
    }

    private void addCapture(Board board, Piece pawn, Position from, int deltaX, int direction, List<Position> moves) {
        int x = from.x() + deltaX;
        int y = from.y() + direction;
        if (!board.contains(x, y)) {
            return;
        }
        Position target = new Position(x, y);
        Piece occupant = board.pieceAt(target);
        if (occupant != null && occupant.color() != pawn.color()) {
            moves.add(target);
        }
    }
}
