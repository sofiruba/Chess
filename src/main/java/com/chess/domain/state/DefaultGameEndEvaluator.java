package com.chess.domain.state;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.chess.domain.draw.DrawCounters;
import com.chess.domain.draw.IDrawRule;
import com.chess.domain.model.Board;
import com.chess.domain.model.Color;
import com.chess.domain.model.Position;
import com.chess.domain.rule.ICheckDetector;

/**
 * El jaque mate y las tablas por ahogado se evalúan primero (no hay jugadas legales); de lo contrario, cualquier
 * {@link IDrawRule} registrada puede terminar la partida.
 */
public final class DefaultGameEndEvaluator implements IGameEndEvaluator {

    private final ICheckDetector checkDetector;
    private final ITurnStateFactory stateFactory;
    private final List<IDrawRule> drawRules;

    public DefaultGameEndEvaluator(
            ICheckDetector checkDetector,
            ITurnStateFactory stateFactory,
            List<IDrawRule> drawRules) {
        this.checkDetector = Objects.requireNonNull(checkDetector, "checkDetector must not be null");
        this.stateFactory = Objects.requireNonNull(stateFactory, "stateFactory must not be null");
        this.drawRules = List.copyOf(Objects.requireNonNull(drawRules, "drawRules must not be null"));
    }

    @Override
    public Optional<IGameState> evaluate(
            Board board,
            Color player,
            DrawCounters counters,
            Function<Position, List<Position>> legalMoves) {
        if (!hasLegalMove(board, legalMoves)) {
            return Optional.of(stateWithoutMoves(board, player));
        }
        if (isDrawByRule(board, counters)) {
            return Optional.of(stateFactory.draw(player));
        }
        return Optional.empty();
    }

    private IGameState stateWithoutMoves(Board board, Color player) {
        if (checkDetector.isInCheck(board, player)) {
            return stateFactory.checkmate(player);
        }
        return stateFactory.stalemate(player);
    }

    private boolean isDrawByRule(Board board, DrawCounters counters) {
        return drawRules.stream().anyMatch(rule -> rule.isDraw(board, counters));
    }

    private boolean hasLegalMove(Board board, Function<Position, List<Position>> legalMoves) {
        for (Position position : board.positions()) {
            if (!legalMoves.apply(position).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}