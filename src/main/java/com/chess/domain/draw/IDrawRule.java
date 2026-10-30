package com.chess.domain.draw;

import com.chess.domain.model.*;

/**
 * Una condición que termina la partida en tablas. Cada regla es una clase propia y se registra en la configuración,
 * por lo que agregar una nunca modifica el evaluador.
 */
public interface IDrawRule {

    /**
     * @param board la posición justo después de la última jugada
     * @param counters el contador de 50 jugadas y las posiciones vistas hasta ahora, ya actualizados para esa jugada
     */
    boolean isDraw(Board board, DrawCounters counters);
}
