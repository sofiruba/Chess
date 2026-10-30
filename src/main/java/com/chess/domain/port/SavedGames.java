package com.chess.domain.port;



import java.util.List;
import java.util.Objects;

/**
 * El resultado de leer el historial de partidas: las partidas que se pudieron leer, primero las más recientes, y
 * cuántas filas almacenadas se omitieron porque estaban dañadas.
 */
public record SavedGames(List<GameRecord> records, int unreadableRows) {
    public SavedGames {
        Objects.requireNonNull(records, "records must not be null");
        if (unreadableRows < 0) {
            throw new IllegalArgumentException("unreadableRows must not be negative: " + unreadableRows);
        }
        records = List.copyOf(records);
    }
}
