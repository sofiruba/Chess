package com.chess.domain.rule;



/**
 * Convierte un código de jugada persistido ("e2e4", "e7e8q") nuevamente en una solicitud de jugada para la partida.
 */
public interface IMoveCodeParser {

    /**
     * @throws IllegalArgumentException si el texto no es un código de jugada bien formado
     */
    MoveRequest parse(String moveCode);
}
