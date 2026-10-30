package com.chess.domain.port;

/**
 * Permite suscribirse a los eventos de una partida. Está separada de {@link IGameActions} porque suscribirse no
 * modifica la partida.
 */
public interface IGameObservable {

    /** Los oyentes se notifican en el orden en que se agregaron. Agregar dos veces el mismo no tiene efecto. */
    void addListener(IGameListener listener);

    void removeListener(IGameListener listener);
}
