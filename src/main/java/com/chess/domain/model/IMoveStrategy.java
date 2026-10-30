package com.chess.domain.model;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

import java.util.List;

public interface IMoveStrategy {
    List<Position> getValidMoves(Board board, Position currentPosition);
    default List<Position> getAttackedMoves(Board board, Position currentPosition) {
        return getValidMoves(board, currentPosition);
    }
}
