package com.chess.domain.state;

import com.chess.domain.model.*;

import com.chess.domain.draw.*;


import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface IGameEndEvaluator {

    /**
     * Decides whether the game is over for the player about to move.
     *
     * @param counters the 50-move clock and the positions seen, read by the draw rules
     * @param legalMoves legal destinations for a given origin square
     * @return the final state, or empty if the game goes on
     */
    Optional<IGameState> evaluate(
            Board board,
            Color player,
            DrawCounters counters,
            Function<Position, List<Position>> legalMoves);
}
