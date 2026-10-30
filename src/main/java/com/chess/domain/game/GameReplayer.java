package com.chess.domain.game;

import java.util.List;
import java.util.Objects;

import com.chess.domain.port.IGameFactory;
import com.chess.domain.port.IGameReplayer;
import com.chess.domain.port.IGameService;

/**
 * Reproduce códigos de jugada en una partida creada por la fábrica indicada, para reconstruir una partida guardada
 * con las mismas reglas que una nueva.
 */
public final class GameReplayer implements IGameReplayer {

    private final IGameFactory gameFactory;

    public GameReplayer(IGameFactory gameFactory) {
        this.gameFactory = Objects.requireNonNull(gameFactory, "gameFactory must not be null");
    }

    @Override
    public IGameService replay(List<String> moveCodes) {
        Objects.requireNonNull(moveCodes, "moveCodes must not be null");
        IGameService game = gameFactory.create();
        for (int index = 0; index < moveCodes.size(); index++) {
            replayMove(game, moveCodes.get(index), index + 1);
        }
        return game;
    }

    private void replayMove(IGameService game, String moveCode, int moveNumber) {
        try {
            game.playMoveCode(moveCode);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Move " + moveNumber + " cannot be replayed: '" + moveCode + "'", exception);
        }
    }
}
