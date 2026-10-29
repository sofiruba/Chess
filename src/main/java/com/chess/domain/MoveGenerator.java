package com.chess.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MoveGenerator implements IMoveGenerator {

    private final IMoveCommandFactory commandFactory;
    private final ICheckDetector checkDetector;
    private final List<IMoveRule> moveRules;

    public MoveGenerator(
            IMoveCommandFactory commandFactory,
            ICheckDetector checkDetector,
            List<IMoveRule> moveRules) {
        this.commandFactory = Objects.requireNonNull(commandFactory, "commandFactory must not be null");
        this.checkDetector = Objects.requireNonNull(checkDetector, "checkDetector must not be null");
        this.moveRules = List.copyOf(Objects.requireNonNull(moveRules, "moveRules must not be null"));
    }

    @Override
    public List<Position> legalMoves(MoveRuleContext context, Color player, Position from) {
        Piece piece = context.board().pieceAt(from);
        if (piece == null || piece.color() != player) {
            return List.of();
        }
        return candidateMoves(context, piece, from).stream()
                .filter(to -> keepsKingSafe(context, new MoveRequest(from, to, null), player))
                .toList();
    }

    @Override
    public ICommand createCommand(MoveRuleContext context, MoveRequest request) {
        return moveRules.stream()
                .filter(rule -> rule.appliesTo(context, request))
                .findFirst()
                .map(rule -> rule.createCommand(context, request))
                .orElseGet(() -> commandFactory.create(context.board(), request.from(), request.to()));
    }

    @Override
    public boolean requiresPromotion(MoveRuleContext context, Position from, Position to) {
        return moveRules.stream().anyMatch(rule -> rule.requiresPromotion(context, from, to));
    }

    @Override
    public List<PieceType> promotionOptions() {
        return moveRules.stream()
                .flatMap(rule -> rule.promotionOptions().stream())
                .distinct()
                .toList();
    }

    private List<Position> candidateMoves(MoveRuleContext context, Piece piece, Position from) {
        List<Position> candidates = new ArrayList<>(piece.getValidMoves(context.board(), from));
        for (IMoveRule rule : moveRules) {
            candidates.addAll(rule.extraMoves(context, from));
        }
        return candidates.stream().distinct().toList();
    }

    private boolean keepsKingSafe(MoveRuleContext context, MoveRequest request, Color color) {
        ICommand command = createCommand(context, request);
        command.execute();
        try {
            return !checkDetector.isInCheck(context.board(), color);
        } finally {
            command.undo();
        }
    }
}