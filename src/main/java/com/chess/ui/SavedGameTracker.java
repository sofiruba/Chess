package com.chess.ui;

import java.util.List;
import java.util.Objects;

/**
 * Recuerda los códigos de jugada de la partida que ya está en la base de datos, porque se acaba de guardar o porque
 * se cargó desde allí. Es solo estado de pantalla: aquí no se almacena nada. No usa JavaFX, por lo que la regla
 * "¿se puede guardar esta partida?" se puede probar de manera aislada.
 */
public final class SavedGameTracker {

    /** Null hasta que se guarda o carga una partida. */
    private List<String> savedMoveCodes;

    /** Una partida se puede guardar cuando tiene jugadas y estas no son las que ya están en la base de datos. */
    public boolean canSave(List<String> moveCodes) {
        Objects.requireNonNull(moveCodes, "moveCodes must not be null");
        return !moveCodes.isEmpty() && !isAlreadySaved(moveCodes);
    }

    public void markSaved(List<String> moveCodes) {
        Objects.requireNonNull(moveCodes, "moveCodes must not be null");
        savedMoveCodes = List.copyOf(moveCodes);
    }

    private boolean isAlreadySaved(List<String> moveCodes) {
        return savedMoveCodes != null && savedMoveCodes.equals(moveCodes);
    }
}
