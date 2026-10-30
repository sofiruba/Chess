package com.chess;

import java.util.List;

import com.chess.domain.model.Color;
import com.chess.domain.command.ICommand;
import com.chess.domain.rule.IMoveGenerator;
import com.chess.domain.rule.MoveRequest;
import com.chess.domain.rule.MoveRuleContext;
import com.chess.domain.model.PieceType;
import com.chess.domain.model.Position;

/**
 * Generador para pruebas de estado: devuelve una lista fija de destinos y recuerda para quién se solicitó.
 */
final class StubMoveGenerator implements IMoveGenerator {

    private final List<Position> destinations;
    private Color askedPlayer;
    private Position askedFrom;

    StubMoveGenerator(List<Position> destinations) {
        this.destinations = List.copyOf(destinations);
    }

    Color askedPlayer() {
        return askedPlayer;
    }

    Position askedFrom() {
        return askedFrom;
    }

    @Override
    public List<Position> legalMoves(MoveRuleContext context, Color player, Position from) {
        askedPlayer = player;
        askedFrom = from;
        return destinations;
    }

    @Override
    public ICommand createCommand(MoveRuleContext context, MoveRequest request) {
        throw new UnsupportedOperationException("Not needed by the state tests");
    }

    @Override
    public boolean requiresPromotion(MoveRuleContext context, Position from, Position to) {
        return false;
    }

    @Override
    public List<PieceType> promotionOptions() {
        return List.of();
    }
}
