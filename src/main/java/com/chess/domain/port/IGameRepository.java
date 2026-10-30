package com.chess.domain.port;


/**
 * Puerto de salida para el historial de partidas. Decisión: solo se almacena el historial de partidas; no hay
 * usuarios.
 */
public interface IGameRepository {
    /**
     * @param record un registro con id {@link GameRecord#UNSAVED_ID}
     * @return el registro almacenado, con el id asignado por el repositorio
     */
    GameRecord save(GameRecord record);

    /** Partidas legibles, primero las más recientes, y cantidad de filas almacenadas que no se pudieron leer. */
    SavedGames findAll();
}