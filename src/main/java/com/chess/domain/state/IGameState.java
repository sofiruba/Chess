package com.chess.domain.state;

import com.chess.domain.model.*;

import com.chess.domain.command.*;

import com.chess.domain.rule.*;


import java.util.List;

/**
 * Estado de una partida: el turno de un jugador o una partida finalizada. Cada implementación cumple este contrato,
 * por lo que la partida puede usar cualquiera sin preguntar cuál tiene.
 */
public interface IGameState {

    /** El jugador que debe mover, o quien habría movido al terminar la partida. Nunca null. */
    Color activePlayerColor();

    /** Es verdadero exactamente cuando {@link #result()} no es {@link GameResult#IN_PROGRESS}. */
    boolean isFinished();

    GameResult result();

    /**
     * Ejecuta la jugada y devuelve el estado que la sigue.
     * <ul>
     *   <li>Un estado de turno lanza {@link NullPointerException} para una jugada null y
     *   {@link IllegalArgumentException} cuando la jugada pertenece al otro jugador. En ambos casos no se ejecuta.</li>
     *   <li>Un estado finalizado lanza {@link IllegalStateException} para cualquier jugada y nunca la ejecuta.</li>
     * </ul>
     */
    IGameState handleMove(ICommand move);

    /**
     * Destinos legales de la pieza en {@code from}. Nunca null; vacío cuando la partida terminó.
     */
    List<Position> legalMoves(MoveRuleContext context, Position from);
}
