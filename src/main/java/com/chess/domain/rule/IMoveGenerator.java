package com.chess.domain.rule;

import com.chess.domain.model.*;

import com.chess.domain.command.*;

import java.util.List;

/**
 * Sabe qué jugadas son legales en una posición y cómo construir el comando para una jugada.
 */
public interface IMoveGenerator {

    List<Position> legalMoves(MoveRuleContext context, Color player, Position from);

    ICommand createCommand(MoveRuleContext context, MoveRequest request);

    boolean requiresPromotion(MoveRuleContext context, Position from, Position to);

    /** Las piezas a las que puede promocionar un peón, según las reglas registradas. */
    List<PieceType> promotionOptions();
}
