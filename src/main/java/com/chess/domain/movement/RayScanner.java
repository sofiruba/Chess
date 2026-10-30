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

final class RayScanner {

    private RayScanner() {
    }

    static List<Position> collect(Board board, Position origin, int[][] directions) {
        Piece originPiece = board.pieceAt(origin);
        if (originPiece == null) {
            throw new IllegalArgumentException("There is no piece at " + origin);
        }
        List<Position> moves = new ArrayList<>();
        for (int[] direction : directions) {
            collectRay(board, origin, direction, originPiece, moves);
        }
        return List.copyOf(moves);
    }

    private static void collectRay(Board board, Position origin, int[] direction, Piece originPiece,
            List<Position> moves) {
        int x = origin.x() + direction[0];
        int y = origin.y() + direction[1];
        while (board.contains(x, y)) {
            Position candidate = new Position(x, y);
            Piece occupant = board.pieceAt(candidate);
            if (occupant == null) {
                moves.add(candidate);
            } else {
                if (occupant.color() != originPiece.color()) {
                    moves.add(candidate);
                }
                return;
            }
            x += direction[0];
            y += direction[1];
        }
    }
}