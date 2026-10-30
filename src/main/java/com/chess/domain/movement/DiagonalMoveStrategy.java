package com.chess.domain.movement;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

import java.util.List;

public final class DiagonalMoveStrategy implements IMoveStrategy {
    @Override
    public List<Position> getValidMoves(Board board, Position currentPosition) {
        return RayScanner.collect(board, currentPosition,
                new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}});
    }
}