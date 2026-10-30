package com.chess.domain.rule;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

import java.util.List;

/**
 * Knows which moves are legal in a position and how to build the command for a move.
 */
public interface IMoveGenerator {

    List<Position> legalMoves(MoveRuleContext context, Color player, Position from);

    ICommand createCommand(MoveRuleContext context, MoveRequest request);

    boolean requiresPromotion(MoveRuleContext context, Position from, Position to);

    /** The pieces a pawn can be promoted to, as declared by the registered rules. */
    List<PieceType> promotionOptions();
}