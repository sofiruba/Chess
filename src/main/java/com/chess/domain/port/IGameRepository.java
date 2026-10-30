package com.chess.domain.port;


/**
 * Output port for the game history. Decision: only the history of games is stored, there are no users.
 */
public interface IGameRepository {
    /**
     * @param record a record with id {@link GameRecord#UNSAVED_ID}
     * @return the stored record, with the id the repository assigned
     */
    GameRecord save(GameRecord record);

    /** Readable games, newest first, and the number of stored rows that could not be read. */
    SavedGames findAll();
}