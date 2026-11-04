package com.chess.domain.power;

import java.util.Map;

import com.chess.domain.model.Board;
import com.chess.domain.model.Position;
import com.chess.domain.model.PowerType;

/**
 * Decide si aparecen cajas misteriosas nuevas después de un turno, dónde y con qué contenido (Strategy). Tiene que
 * ser determinista: con la misma posición y el mismo número de turno devuelve siempre las mismas cajas, porque
 * rehacer una jugada o cargar una partida guardada vuelven a calcularlas.
 */
public interface IBoxSpawner {

    /** Nunca aparecen cajas nuevas. */
    IBoxSpawner NONE = new IBoxSpawner() {
        @Override
        public Map<Position, PowerType> spawn(PowerState powers, Board board, int turnsPlayed) {
            return Map.of();
        }

        @Override
        public int turnsUntilNext(int turnsPlayed) {
            return -1;
        }

        @Override
        public int maxOnBoard() {
            return 0;
        }
    };

    /**
     * @param powers el estado después del turno, con las cajas que ya hay en el tablero
     * @param board el tablero después del turno
     * @param turnsPlayed cuántos turnos se jugaron en total, contando el que acaba de terminar
     * @return las cajas nuevas; nunca en una casilla con pieza ni con otra caja
     */
    Map<Position, PowerType> spawn(PowerState powers, Board board, int turnsPlayed);

    /** Cuántos turnos faltan para la próxima aparición, o -1 si nunca aparecen cajas. */
    int turnsUntilNext(int turnsPlayed);

    /** Cuántas cajas puede haber como máximo al mismo tiempo para que aparezcan nuevas. */
    int maxOnBoard();
}
