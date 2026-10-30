package com.chess.domain.port;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

/**
 * The ways to change a game.
 */
public interface IGameActions {
    void playMove(Position from, Position to);
    void playMove(Position from, Position to, PieceType promotionType);
    void playMoveCode(String moveCode);
    void undoLastMove();
    void redoLastMove();
}