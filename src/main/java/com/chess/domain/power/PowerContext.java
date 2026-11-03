package com.chess.domain.power;

import java.util.Objects;

import com.chess.domain.model.Board;
import com.chess.domain.rule.MoveRuleContext;

/**
 * Lo que un poder necesita conocer de la posición actual: el contexto de las reglas de movimiento (tablero,
 * detector de jaque, última jugada) y el estado de los poderes.
 */
public record PowerContext(MoveRuleContext rules, PowerState powers) {

    public PowerContext {
        Objects.requireNonNull(rules, "rules must not be null");
        Objects.requireNonNull(powers, "powers must not be null");
    }

    public Board board() {
        return rules.board();
    }
}
