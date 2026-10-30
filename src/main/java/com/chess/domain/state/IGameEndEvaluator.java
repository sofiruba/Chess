package com.chess.domain.state;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.chess.domain.draw.DrawCounters;
import com.chess.domain.model.Board;
import com.chess.domain.model.Color;
import com.chess.domain.model.Position;

public interface IGameEndEvaluator {

    /**
     * Decide si la partida terminó para el jugador que está por mover.
     *
     * @param counters el contador de 50 jugadas y las posiciones vistas, leídos por las reglas de tablas
     * @param legalMoves destinos legales para una casilla de origen dada
     * @return el estado final, o vacío si la partida continúa
     */
    Optional<IGameState> evaluate(
            Board board,
            Color player,
            DrawCounters counters,
            Function<Position, List<Position>> legalMoves);
}
