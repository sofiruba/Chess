package com.chess.domain.port;

import java.util.List;
import java.util.Objects;

public interface IGameFactory {

    /** Una partida nueva. Si la variante tiene cajas misteriosas, su contenido se sortea. */
    IGameService create();

    /**
     * Una partida nueva con el contenido de cajas indicado, como lo devuelve {@link IGameQueries#setupCode()}. Un
     * código vacío equivale a {@link #create()}.
     *
     * @throws IllegalArgumentException si el código está mal formado
     */
    IGameService createWithSetup(String setupCode);

    default IGameService create(List<String> moveCodes) {
        Objects.requireNonNull(moveCodes, "moveCodes must not be null");
        return replay(create(), moveCodes);
    }

    /** Reconstruye una partida guardada: mismas cajas y mismas jugadas. */
    default IGameService createWithSetup(String setupCode, List<String> moveCodes) {
        Objects.requireNonNull(setupCode, "setupCode must not be null");
        Objects.requireNonNull(moveCodes, "moveCodes must not be null");
        return replay(createWithSetup(setupCode), moveCodes);
    }

    private static IGameService replay(IGameService game, List<String> moveCodes) {
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
