package com.chess.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.chess.domain.model.PowerType;

/**
 * Cómo se presenta cada poder en la interfaz: su nombre, la letra de la insignia que aparece sobre la pieza y la
 * instrucción que ve el jugador al usarlo. Igual que {@link PieceSymbols}, pertenece a la interfaz y no al dominio. Un
 * poder sin textos registrados se muestra con su nombre interno, por lo que un poder nuevo siempre es visible.
 */
public final class PowerNames {

    private final Map<PowerType, Entry> entries;

    private record Entry(String name, String badge, String styleClass, String instruction) {
    }

    private PowerNames(Map<PowerType, Entry> entries) {
        this.entries = Map.copyOf(entries);
    }

    public static PowerNames standard() {
        Map<PowerType, Entry> entries = new HashMap<>();
        entries.put(PowerType.ICE_FLOWER, new Entry("Flor de hielo", "❄", "power-ice",
                "Elegí la pieza enemiga a congelar."));
        entries.put(PowerType.FEATHER, new Entry("Pluma", "P", "power-feather",
                "Elegí la casilla vacía de destino."));
        entries.put(PowerType.STAR, new Entry("Estrella", "★", "power-star",
                "Tocá la pieza para activar la estrella."));
        entries.put(PowerType.SHELL, new Entry("Caparazón", "C", "power-shell",
                "Elegí qué pieza enemiga golpear."));
        return new PowerNames(entries);
    }

    public String name(PowerType power) {
        Entry entry = entries.get(Objects.requireNonNull(power, "power must not be null"));
        return entry == null ? power.name() : entry.name();
    }

    public String badge(PowerType power) {
        Entry entry = entries.get(Objects.requireNonNull(power, "power must not be null"));
        return entry == null ? power.code().toUpperCase() : entry.badge();
    }

    /** Clase CSS que da color a la insignia del poder. */
    public String styleClass(PowerType power) {
        Entry entry = entries.get(Objects.requireNonNull(power, "power must not be null"));
        return entry == null ? "power-generic" : entry.styleClass();
    }

    public String instruction(PowerType power) {
        Entry entry = entries.get(Objects.requireNonNull(power, "power must not be null"));
        return entry == null ? "Elegí el objetivo del poder." : entry.instruction();
    }
}
