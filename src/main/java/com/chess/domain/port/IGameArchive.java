package com.chess.domain.port;

/**
 * Lo que la interfaz necesita del historial de partidas: guardar la partida actual y listar las guardadas.
 * Existe para que la interfaz no arme registros ni conozca el reloj, que son decisiones de la aplicación.
 */
public interface IGameArchive {

    /**
     * @return el registro almacenado con la partida tal como está ahora, con el id asignado por el repositorio
     */
    GameRecord save(IGameQueries game);

    /** Partidas legibles, primero las más recientes, y cantidad de filas almacenadas que no se pudieron leer. */
    SavedGames findAll();
}