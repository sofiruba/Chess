package com.chess.domain.power;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.chess.domain.model.Color;
import com.chess.domain.model.Piece;
import com.chess.domain.model.Position;
import com.chess.domain.model.PowerType;

/**
 * Busca el {@link IPower} que corresponde al poder de cada pieza y filtra sus objetivos: un poder solo se puede usar
 * si después el propio rey no queda en jaque, igual que una jugada normal.
 */
public final class PowerRules implements IPowerRules {

    private final Map<PowerType, IPower> powersByType;
    private final List<PowerType> types;

    public PowerRules(List<IPower> powers) {
        Objects.requireNonNull(powers, "powers must not be null");
        Map<PowerType, IPower> index = new HashMap<>();
        for (IPower power : powers) {
            if (index.put(power.type(), power) != null) {
                throw new IllegalArgumentException("Two powers share the type " + power.type());
            }
        }
        this.powersByType = Map.copyOf(index);
        this.types = powers.stream().map(IPower::type).toList();
    }

    /** Los tipos registrados, en el orden en que se registraron: los únicos que pueden salir de una caja. */
    public List<PowerType> types() {
        return types;
    }

    @Override
    public List<Position> targets(PowerContext context, Color player, Position holder) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(holder, "holder must not be null");
        Piece piece = context.board().pieceAt(holder);
        if (piece == null || piece.color() != player || !context.powers().canAct(piece)) {
            return List.of();
        }
        IPower power = powerFor(context, piece);
        if (power == null) {
            return List.of();
        }
        return power.targets(context, holder).stream()
                .filter(target -> keepsKingSafe(context, holder, target, player))
                .toList();
    }

    @Override
    public IPowerCommand createCommand(PowerContext context, Position holder, Position target) {
        Piece piece = context.board().pieceAt(holder);
        if (piece == null) {
            throw new IllegalArgumentException("There is no piece at " + holder);
        }
        IPower power = powerFor(context, piece);
        if (power == null) {
            throw new IllegalArgumentException("The piece at " + holder + " has no power");
        }
        return power.createCommand(context, holder, target);
    }

    /** La estrategia del poder que lleva la pieza, o null si no lleva ninguno registrado. */
    private IPower powerFor(PowerContext context, Piece piece) {
        PowerType type = context.powers().powerOf(piece);
        return type == null ? null : powersByType.get(type);
    }

    private boolean keepsKingSafe(PowerContext context, Position holder, Position target, Color player) {
        IPowerCommand command = createCommand(context, holder, target);
        command.execute();
        try {
            return !context.rules().checkDetector().isInCheck(context.board(), player);
        } finally {
            command.undo();
        }
    }
}
