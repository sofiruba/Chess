package com.chess.domain.game;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

import java.util.List;
import java.util.Objects;

/**
 * Replays move codes on a game made by the given factory, so a saved game is rebuilt with the same rules as a new one.
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
        for (String moveCode : moveCodes) {
            game.playMoveCode(moveCode);
        }
        return game;
    }
}
