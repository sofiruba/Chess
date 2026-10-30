package com.chess.domain.movement;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

import java.util.ArrayList;
import java.util.List;

public final class OneStepMoveStrategy implements IMoveStrategy {

    @Override
    public List<Position> getValidMoves(Board board, Position currentPosition) {
        Piece originPiece = board.pieceAt(currentPosition);
        if (originPiece == null) {
            throw new IllegalArgumentException("There is no piece at " + currentPosition);
        }
        List<Position> moves = new ArrayList<>();
        for (int deltaX = -1; deltaX <= 1; deltaX++) {
            for (int deltaY = -1; deltaY <= 1; deltaY++) {
                addIfReachable(board, originPiece, currentPosition, deltaX, deltaY, moves);
            }
        }
        return List.copyOf(moves);
    }

    private void addIfReachable(Board board, Piece originPiece, Position origin, int deltaX, int deltaY,
            List<Position> moves) {
        int x = origin.x() + deltaX;
        int y = origin.y() + deltaY;
        if ((deltaX == 0 && deltaY == 0) || !board.contains(x, y)) {
            return;
        }
        Position candidate = new Position(x, y);
        Piece occupant = board.pieceAt(candidate);
        if (occupant == null || occupant.color() != originPiece.color()) {
            moves.add(candidate);
        }
    }
}
