package com.chess.domain.port;


import java.util.List;

/**
 * Reconstruye una partida a partir de los códigos de jugada almacenados. Existe para que la interfaz pueda cargar una
 * partida guardada sin conocer cómo se crea una partida ni cómo se interpretan los códigos de jugada.
 */
public interface IGameReplayer {

    /**
     * @param moveCodes los códigos de jugada en el orden en que se jugaron; una lista vacía crea una partida nueva
     * @return una partida en la que se jugaron todas las jugadas y que se puede deshacer y rehacer
     * @throws IllegalArgumentException si un código de jugada tiene un formato incorrecto o no es legal en su turno
     */
    IGameService replay(List<String> moveCodes);
}
