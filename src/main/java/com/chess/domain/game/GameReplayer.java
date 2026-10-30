package com.chess.domain.game;

import java.util.List;
import java.util.Objects;

import com.chess.domain.port.IGameFactory;
import com.chess.domain.port.IGameReplayer;
import com.chess.domain.port.IGameService;

/**
 * Replays move codes on a game created by the supplied factory.
 */
public final class GameReplayer implements IGameReplayer {

    private final IGameFactory gameFactory;

    public GameReplayer(IGameFactory gameFactory) {
        this.gameFactory = Objects.requireNonNull(gameFactory, "gameFactory must not be null");
    }

    @Override
    public IGameService replay(List<String> moveCodes) {
        return gameFactory.create(moveCodes);
    }
}
