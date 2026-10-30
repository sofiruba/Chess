package com.chess.domain.state;

import java.util.Objects;

import com.chess.domain.model.Color;
import com.chess.domain.model.GameResult;
import com.chess.domain.rule.IMoveGenerator;

public final class TurnStateFactory implements ITurnStateFactory {

    private final IMoveGenerator moveGenerator;

    public TurnStateFactory(IMoveGenerator moveGenerator) {
        this.moveGenerator = Objects.requireNonNull(moveGenerator, "moveGenerator must not be null");
    }

    @Override
    public IGameState forColor(Color color) {
        return new TurnState(color, this, moveGenerator);
    }

    @Override
    public IGameState checkmate(Color checkedPlayer) {
        return new FinishedState(checkedPlayer, victoryOf(checkedPlayer.opposite()), "checkmate");
    }

    @Override
    public IGameState stalemate(Color playerWithoutMoves) {
        return new FinishedState(playerWithoutMoves, GameResult.DRAW, "stalemate");
    }

    @Override
    public IGameState draw(Color playerToMove) {
        return new FinishedState(playerToMove, GameResult.DRAW, "draw");
    }

    private static GameResult victoryOf(Color winner) {
        return winner == Color.WHITE ? GameResult.WHITE_WINS : GameResult.BLACK_WINS;
    }
}
