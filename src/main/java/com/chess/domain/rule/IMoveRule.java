package com.chess.domain.rule;

import com.chess.domain.model.*;

import com.chess.domain.command.*;

import java.util.List;

/**
 * Una jugada especial (enroque, captura al paso, promoción, ...). Cada regla posee su propio comando, por lo que
 * agregar una regla implica añadir una clase y registrarla en la configuración, sin modificar Game u otras reglas.
 */
public interface IMoveRule {

    /** Destinos que esta regla pone a disposición de la pieza en {@code from}, antes de filtrar por seguridad del rey. */
    List<Position> extraMoves(MoveRuleContext context, Position from);

    /** Es verdadero cuando la jugada solicitada debe ejecutarse con el comando de esta regla y no con uno simple. */
    boolean appliesTo(MoveRuleContext context, MoveRequest request);

    ICommand createCommand(MoveRuleContext context, MoveRequest request);

    default boolean requiresPromotion(MoveRuleContext context, Position from, Position to) {
        return false;
    }

    /** Piezas en las que puede convertirse un peón según esta regla. Solo la regla de promoción ofrece alguna. */
    default List<PieceType> promotionOptions() {
        return List.of();
    }
}
