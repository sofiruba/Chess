package com.chess.domain.power;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.chess.domain.command.IMoveInfo;
import com.chess.domain.model.Board;
import com.chess.domain.model.Color;
import com.chess.domain.model.Piece;
import com.chess.domain.model.Position;
import com.chess.domain.model.PowerType;
import com.chess.domain.rule.IPieceStatus;

/**
 * Todo lo que la mecánica de las cajas misteriosas agrega a una posición: las cajas que quedan en el tablero, qué
 * poder lleva cada pieza, qué piezas están congeladas y cuáles son inmunes. Es inmutable, como
 * {@link com.chess.domain.rule.CastlingRights}: después de cada jugada se crea uno nuevo y el anterior queda en el
 * historial, por lo que deshacer y rehacer no necesitan lógica propia.
 *
 * <p>Reglas de duración:
 * <ul>
 *   <li>Una pieza congelada no puede moverse ni usar su poder durante el siguiente turno de su dueño, y se
 *   descongela cuando ese turno termina.</li>
 *   <li>Una pieza con estrella es inmune durante el siguiente turno del rival, y deja de serlo cuando ese turno
 *   termina.</li>
 *   <li>Cada pieza lleva como máximo un poder. Al terminar cada jugada, toda pieza sin poder que esté sobre una caja
 *   la abre y se queda con su poder; una pieza que ya tiene uno deja la caja donde está.</li>
 *   <li>Si una pieza sale del tablero (capturada, eliminada o promocionada), su poder se pierde.</li>
 *   <li>Al terminar cada turno, el {@link IBoxSpawner} puede agregar cajas nuevas en casillas libres.</li>
 * </ul>
 * Las piezas se identifican por identidad, igual que en el resto del dominio.
 */
public record PowerState(
        Map<Position, PowerType> boxes,
        Map<Piece, PowerType> held,
        Set<Piece> frozen,
        Set<Piece> immune,
        int turnsPlayed,
        IBoxSpawner spawner) implements IPieceStatus {

    private static final Comparator<Position> SQUARE_ORDER =
            Comparator.comparingInt(Position::x).thenComparingInt(Position::y);

    public PowerState {
        boxes = Map.copyOf(Objects.requireNonNull(boxes, "boxes must not be null"));
        held = Map.copyOf(Objects.requireNonNull(held, "held must not be null"));
        frozen = Set.copyOf(Objects.requireNonNull(frozen, "frozen must not be null"));
        immune = Set.copyOf(Objects.requireNonNull(immune, "immune must not be null"));
        Objects.requireNonNull(spawner, "spawner must not be null");
        if (turnsPlayed < 0) {
            throw new IllegalArgumentException("turnsPlayed must not be negative");
        }
    }

    /** Sin cajas ni poderes: el ajedrez clásico. */
    public static PowerState none() {
        return new PowerState(Map.of(), Map.of(), Set.of(), Set.of(), 0, IBoxSpawner.NONE);
    }

    /** Las cajas al empezar, sin cajas nuevas durante la partida. */
    public static PowerState initial(Map<Position, PowerType> boxes, Board board) {
        return initial(boxes, board, IBoxSpawner.NONE);
    }

    /** Las cajas al empezar. Si alguna pieza ya está sobre una caja, la abre en ese momento. */
    public static PowerState initial(Map<Position, PowerType> boxes, Board board, IBoxSpawner spawner) {
        Objects.requireNonNull(board, "board must not be null");
        return new PowerState(boxes, Map.of(), Set.of(), Set.of(), 0, spawner).withPickups(board);
    }

    /**
     * @param move la jugada (o el uso de un poder) ya ejecutada en el tablero
     * @param board el tablero después de esa jugada
     */
    public PowerState after(IMoveInfo move, Board board) {
        Objects.requireNonNull(move, "move must not be null");
        Objects.requireNonNull(board, "board must not be null");
        PowerState next = expireEffects(move.movingColor());
        if (move instanceof IPowerCommand powerUse) {
            next = powerUse.applyEffect(next.withoutPowerOf(powerUse.holder()));
        }
        return next.withoutPiecesOffBoard(board).withPickups(board).afterTurn(board);
    }

    /** Cuántos turnos faltan para que aparezcan cajas nuevas, o -1 si en esta partida no aparecen. */
    public int turnsUntilNewBoxes() {
        return spawner.turnsUntilNext(turnsPlayed);
    }

    /** El máximo de cajas simultáneas para que aparezcan nuevas (0 si en esta partida no aparecen). */
    public int maxBoxesOnBoard() {
        return spawner.maxOnBoard();
    }

    @Override
    public boolean canAct(Piece piece) {
        return !frozen.contains(piece);
    }

    @Override
    public boolean canBeCaptured(Piece piece) {
        return !immune.contains(piece);
    }

    /** El poder que lleva la pieza, o null si no lleva ninguno. */
    public PowerType powerOf(Piece piece) {
        return held.get(piece);
    }

    public boolean isFrozen(Piece piece) {
        return frozen.contains(piece);
    }

    public boolean isImmune(Piece piece) {
        return immune.contains(piece);
    }

    /** Las casillas con cajas sin abrir, columna por columna. */
    public List<Position> boxPositions() {
        return boxes.keySet().stream().sorted(SQUARE_ORDER).toList();
    }

    public PowerState withFrozen(Piece piece) {
        Set<Piece> updated = new HashSet<>(frozen);
        updated.add(Objects.requireNonNull(piece, "piece must not be null"));
        return new PowerState(boxes, held, updated, immune, turnsPlayed, spawner);
    }

    public PowerState withImmune(Piece piece) {
        Set<Piece> updated = new HashSet<>(immune);
        updated.add(Objects.requireNonNull(piece, "piece must not be null"));
        return new PowerState(boxes, held, frozen, updated, turnsPlayed, spawner);
    }

    /**
     * Texto que distingue dos estados de poderes iguales sobre el mismo tablero, para la regla de repetición. Vacío
     * cuando no hay cajas ni efectos, por lo que el ajedrez clásico no cambia.
     */
    public String signature(Board board) {
        StringBuilder text = new StringBuilder();
        for (Position box : boxPositions()) {
            text.append('?').append(box.x()).append('.').append(box.y()).append(boxes.get(box).code()).append(';');
        }
        for (Position position : board.positions()) {
            Piece piece = board.pieceAt(position);
            if (piece != null && hasAnyMark(piece)) {
                text.append(position.x()).append('.').append(position.y()).append(marksOf(piece)).append(';');
            }
        }
        return text.toString();
    }

    private boolean hasAnyMark(Piece piece) {
        return held.containsKey(piece) || frozen.contains(piece) || immune.contains(piece);
    }

    private String marksOf(Piece piece) {
        PowerType power = held.get(piece);
        return (power == null ? "" : power.code())
                + (frozen.contains(piece) ? "*" : "")
                + (immune.contains(piece) ? "!" : "");
    }

    /**
     * Quien acaba de jugar cumplió su turno: sus piezas congeladas se descongelan y las piezas inmunes del rival,
     * que estaban protegidas durante ese turno, dejan de estarlo.
     */
    private PowerState expireEffects(Color mover) {
        Set<Piece> stillFrozen = new HashSet<>(frozen);
        stillFrozen.removeIf(piece -> piece.color() == mover);
        Set<Piece> stillImmune = new HashSet<>(immune);
        stillImmune.removeIf(piece -> piece.color() != mover);
        return new PowerState(boxes, held, stillFrozen, stillImmune, turnsPlayed, spawner);
    }

    private PowerState withoutPowerOf(Piece holder) {
        Map<Piece, PowerType> updated = new HashMap<>(held);
        updated.remove(holder);
        return new PowerState(boxes, updated, frozen, immune, turnsPlayed, spawner);
    }

    private PowerState withoutPiecesOffBoard(Board board) {
        Set<Piece> onBoard = new HashSet<>();
        for (Position position : board.positions()) {
            Piece piece = board.pieceAt(position);
            if (piece != null) {
                onBoard.add(piece);
            }
        }
        Map<Piece, PowerType> keptPowers = new HashMap<>(held);
        keptPowers.keySet().retainAll(onBoard);
        Set<Piece> keptFrozen = new HashSet<>(frozen);
        keptFrozen.retainAll(onBoard);
        Set<Piece> keptImmune = new HashSet<>(immune);
        keptImmune.retainAll(onBoard);
        return new PowerState(boxes, keptPowers, keptFrozen, keptImmune, turnsPlayed, spawner);
    }

    /** Cuenta el turno que terminó y agrega las cajas nuevas que correspondan. */
    private PowerState afterTurn(Board board) {
        PowerState counted = new PowerState(boxes, held, frozen, immune, turnsPlayed + 1, spawner);
        Map<Position, PowerType> spawned = spawner.spawn(counted, board, counted.turnsPlayed());
        if (spawned.isEmpty()) {
            return counted;
        }
        Map<Position, PowerType> allBoxes = new HashMap<>(boxes);
        for (Map.Entry<Position, PowerType> box : spawned.entrySet()) {
            if (board.pieceAt(box.getKey()) != null || allBoxes.put(box.getKey(), box.getValue()) != null) {
                throw new IllegalStateException("A new mystery box must appear on a free square: " + box.getKey());
            }
        }
        return new PowerState(allBoxes, held, frozen, immune, counted.turnsPlayed(), spawner);
    }

    private PowerState withPickups(Board board) {
        Map<Position, PowerType> remainingBoxes = new HashMap<>(boxes);
        Map<Piece, PowerType> updatedPowers = new HashMap<>(held);
        for (Position box : boxPositions()) {
            Piece piece = board.pieceAt(box);
            if (piece != null && !updatedPowers.containsKey(piece)) {
                updatedPowers.put(piece, remainingBoxes.remove(box));
            }
        }
        return new PowerState(remainingBoxes, updatedPowers, frozen, immune, turnsPlayed, spawner);
    }
}
