package com.chess.domain.state;

import com.chess.domain.model.*;

import com.chess.domain.command.*;

import com.chess.domain.rule.*;


import java.util.List;
import java.util.Objects;

/**
 * La partida está en curso y es el turno de {@code player}.
 */
public final class TurnState implements IGameState {

    private final Color player;
    private final ITurnStateFactory stateFactory;
    private final IMoveGenerator moveGenerator;

    public TurnState(Color player, ITurnStateFactory stateFactory, IMoveGenerator moveGenerator) {
        this.player = Objects.requireNonNull(player, "player must not be null");
        this.stateFactory = Objects.requireNonNull(stateFactory, "stateFactory must not be null");
        this.moveGenerator = Objects.requireNonNull(moveGenerator, "moveGenerator must not be null");
    }

    @Override
    public Color activePlayerColor() {
        return player;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public GameResult result() {
        return GameResult.IN_PROGRESS;
    }

    @Override
    public IGameState handleMove(ICommand move) {
        Objects.requireNonNull(move, "move must not be null");
        if (move.movingColor() != player) {
            throw new IllegalArgumentException("It is " + player.name().toLowerCase() + "'s turn");
        }
        move.execute();
        return stateFactory.forColor(player.opposite());
    }

    @Override
    public List<Position> legalMoves(MoveRuleContext context, Position from) {
        return moveGenerator.legalMoves(context, player, from);
    }
}
