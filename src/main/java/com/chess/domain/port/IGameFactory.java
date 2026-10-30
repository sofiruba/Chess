package com.chess.domain.port;

import java.util.List;
import java.util.Objects;

public interface IGameFactory {

    IGameService create();

    default IGameService create(List<String> moveCodes) {
        Objects.requireNonNull(moveCodes, "moveCodes must not be null");
        IGameService game = create();
        for (int index = 0; index < moveCodes.size(); index++) {
            replayMove(game, moveCodes.get(index), index + 1);
        }
        return game;
    }

    private static void replayMove(IGameService game, String moveCode, int moveNumber) {
        try {
            game.playMoveCode(moveCode);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Move " + moveNumber + " cannot be replayed: '" + moveCode + "'",
                    exception);
        }
    }
}
