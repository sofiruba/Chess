package com.chess.domain.power;

import java.util.List;

import com.chess.domain.model.Position;
import com.chess.domain.model.PowerType;

/**
 * Un poder de las cajas misteriosas (Strategy). Cada poder es una clase propia que se registra en la configuración,
 * por lo que agregar uno nuevo no modifica {@link PowerRules} ni la partida.
 *
 * <p>Todos los poderes se usan igual: se elige la pieza que lo lleva y una casilla objetivo. Para los poderes que no
 * necesitan objetivo (la estrella), el objetivo es la propia casilla de la pieza.
 */
public interface IPower {

    PowerType type();

    /**
     * Objetivos posibles para la pieza en {@code holder}, antes de comprobar que el propio rey quede a salvo. Quien
     * llama ya verificó que la pieza lleva este poder y puede actuar.
     */
    List<Position> targets(PowerContext context, Position holder);

    IPowerCommand createCommand(PowerContext context, Position holder, Position target);
}
