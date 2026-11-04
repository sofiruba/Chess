package com.chess.domain.power;

import java.util.List;

import com.chess.domain.model.Color;
import com.chess.domain.model.Position;

/**
 * Sabe qué usos de poderes son legales en una posición y cómo construir el comando de cada uno. Es al uso de poderes
 * lo que {@link com.chess.domain.rule.IMoveGenerator} es a las jugadas.
 */
public interface IPowerRules {

    /** Ningún poder registrado: el ajedrez clásico. */
    IPowerRules NONE = new IPowerRules() {
        @Override
        public List<Position> targets(PowerContext context, Color player, Position holder) {
            return List.of();
        }

        @Override
        public IPowerCommand createCommand(PowerContext context, Position holder, Position target) {
            throw new IllegalArgumentException("There are no powers in this game");
        }
    };

    /**
     * Objetivos legales para el poder de la pieza en {@code holder}. Vacío si la casilla no tiene una pieza de
     * {@code player} que lleve un poder y pueda actuar, o si ningún uso deja al propio rey a salvo.
     */
    List<Position> targets(PowerContext context, Color player, Position holder);

    IPowerCommand createCommand(PowerContext context, Position holder, Position target);
}
