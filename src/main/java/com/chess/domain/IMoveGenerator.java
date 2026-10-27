package com.chess.domain;

import java.util.List;

/**
 * Knows which moves are legal in a position and how to build the command for a move.
 */
public interface IMoveGenerator {

    List<Position> legalMoves(MoveRuleContext context, Color player, Position from);

    ICommand createCommand(MoveRuleContext context, MoveRequest request);

    boolean requiresPromotion(MoveRuleContext context, Position from, Position to);
}
