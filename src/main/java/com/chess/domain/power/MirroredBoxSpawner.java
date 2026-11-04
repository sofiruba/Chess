package com.chess.domain.power;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SplittableRandom;

import com.chess.domain.model.Board;
import com.chess.domain.model.Position;
import com.chess.domain.model.PowerType;

/**
 * Cada {@code everyTurns} turnos (contando la jugada de cada jugador) hace aparecer {@code boxesPerSide} cajas en cada
 * mitad del tablero. Las casillas se eligen al azar en la mitad de las blancas y cada una tiene su espejo en la mitad
 * de las negras, con la misma simetría de las cajas iniciales (una rotación de 180 grados: a5 y h4). El espejo es
 * solo de posición: el contenido de cada caja se sortea por separado.
 *
 * <p>Nunca hay más de {@code maxOnBoard} cajas al mismo tiempo, contando las que todavía no se abrieron. Para no
 * romper la simetría, las cajas aparecen siempre de a pares espejo: si solo queda lugar para una, no aparece
 * ninguna. Solo se usan pares de casillas en los que ambas están libres (sin pieza y sin caja); si quedan menos
 * pares libres que los que entran, aparecen los que haya.
 *
 * <p>El azar sale de una semilla fija combinada con el número de turno, así que el resultado depende solo de la
 * posición y del turno: rehacer o reproducir una partida da las mismas cajas.
 */
public final class MirroredBoxSpawner implements IBoxSpawner {

    private final int everyTurns;
    private final int boxesPerSide;
    private final int maxOnBoard;
    private final List<PowerType> powers;
    private final long seed;

    /** Sin límite de cajas simultáneas. */
    public MirroredBoxSpawner(int everyTurns, int boxesPerSide, List<PowerType> powers, long seed) {
        this(everyTurns, boxesPerSide, Integer.MAX_VALUE, powers, seed);
    }

    public MirroredBoxSpawner(int everyTurns, int boxesPerSide, int maxOnBoard, List<PowerType> powers, long seed) {
        if (everyTurns <= 0) {
            throw new IllegalArgumentException("everyTurns must be positive");
        }
        if (boxesPerSide <= 0) {
            throw new IllegalArgumentException("boxesPerSide must be positive");
        }
        if (maxOnBoard < 2) {
            throw new IllegalArgumentException("maxOnBoard must leave room for at least one mirrored pair");
        }
        this.powers = List.copyOf(Objects.requireNonNull(powers, "powers must not be null"));
        if (this.powers.isEmpty()) {
            throw new IllegalArgumentException("Mystery boxes need at least one registered power");
        }
        this.everyTurns = everyTurns;
        this.boxesPerSide = boxesPerSide;
        this.maxOnBoard = maxOnBoard;
        this.seed = seed;
    }

    @Override
    public Map<Position, PowerType> spawn(PowerState state, Board board, int turnsPlayed) {
        Objects.requireNonNull(state, "state must not be null");
        Objects.requireNonNull(board, "board must not be null");
        int pairs = pairsThatFit(state);
        if (turnsPlayed <= 0 || turnsPlayed % everyTurns != 0 || pairs == 0) {
            return Map.of();
        }
        SplittableRandom random = new SplittableRandom(seed ^ (turnsPlayed * 0x9E3779B97F4A7C15L));
        List<Position> candidates = freeSquaresWithFreeMirror(state, board);
        Map<Position, PowerType> spawned = new HashMap<>();
        for (int placed = 0; placed < pairs && !candidates.isEmpty(); placed++) {
            Position square = candidates.remove(random.nextInt(candidates.size()));
            spawned.put(square, randomPower(random));
            spawned.put(mirror(square, board), randomPower(random));
        }
        return spawned;
    }

    @Override
    public int maxOnBoard() {
        return maxOnBoard;
    }

    /** Cuántos pares espejo entran sin pasar el máximo, hasta {@code boxesPerSide}. */
    private int pairsThatFit(PowerState state) {
        int room = Math.max(0, maxOnBoard - state.boxes().size());
        return Math.min(boxesPerSide, room / 2);
    }

    @Override
    public int turnsUntilNext(int turnsPlayed) {
        return everyTurns - Math.floorMod(turnsPlayed, everyTurns);
    }

    /** Casillas de la mitad de las blancas que están libres y cuyo espejo también lo está, en un orden fijo. */
    private List<Position> freeSquaresWithFreeMirror(PowerState state, Board board) {
        List<Position> candidates = new ArrayList<>();
        for (Position square : board.positions()) {
            if (square.y() < board.size() / 2
                    && isFree(square, state, board)
                    && isFree(mirror(square, board), state, board)) {
                candidates.add(square);
            }
        }
        return candidates;
    }

    private static boolean isFree(Position square, PowerState state, Board board) {
        return board.pieceAt(square) == null && !state.boxes().containsKey(square);
    }

    static Position mirror(Position square, Board board) {
        return new Position(board.size() - 1 - square.x(), board.size() - 1 - square.y());
    }

    private PowerType randomPower(SplittableRandom random) {
        return powers.get(random.nextInt(powers.size()));
    }
}
