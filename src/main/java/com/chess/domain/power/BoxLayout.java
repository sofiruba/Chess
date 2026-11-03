package com.chess.domain.power;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.random.RandomGenerator;

import com.chess.domain.model.Position;
import com.chess.domain.model.PowerType;

/**
 * Qué poder hay dentro de cada caja misteriosa al empezar la partida. El contenido se sortea, pero se guarda como
 * texto ("a5=i,c5=s,f4=f,h4=t") junto con las jugadas, para que una partida cargada vuelva a tener las mismas
 * cajas y sus jugadas se puedan reproducir.
 */
public record BoxLayout(Map<Position, PowerType> contents) {

    private static final Comparator<Position> SQUARE_ORDER =
            Comparator.comparingInt(Position::x).thenComparingInt(Position::y);

    public BoxLayout {
        contents = Map.copyOf(Objects.requireNonNull(contents, "contents must not be null"));
    }

    public static BoxLayout empty() {
        return new BoxLayout(Map.of());
    }

    /** Sortea el poder de cada caja de forma independiente entre los poderes disponibles. */
    public static BoxLayout random(List<Position> boxes, List<PowerType> powers, RandomGenerator random) {
        Objects.requireNonNull(boxes, "boxes must not be null");
        Objects.requireNonNull(powers, "powers must not be null");
        Objects.requireNonNull(random, "random must not be null");
        if (boxes.isEmpty()) {
            return empty();
        }
        if (powers.isEmpty()) {
            throw new IllegalArgumentException("Mystery boxes need at least one registered power");
        }
        Map<Position, PowerType> contents = new HashMap<>();
        for (Position box : boxes) {
            if (contents.put(box, powers.get(random.nextInt(powers.size()))) != null) {
                throw new IllegalArgumentException("Two mystery boxes share the square " + box);
            }
        }
        return new BoxLayout(contents);
    }

    /**
     * @param code texto creado por {@link #code()}; vacío significa que no hay cajas
     * @param powers los poderes que se pueden reconocer por su código
     * @throws IllegalArgumentException si el texto está mal formado o nombra un poder desconocido
     */
    public static BoxLayout parse(String code, List<PowerType> powers) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(powers, "powers must not be null");
        if (code.isBlank()) {
            return empty();
        }
        Map<Position, PowerType> contents = new HashMap<>();
        for (String entry : code.split(",")) {
            String[] parts = entry.trim().split("=");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid mystery box entry: '" + entry + "'");
            }
            if (contents.put(square(parts[0]), power(parts[1], powers)) != null) {
                throw new IllegalArgumentException("Two mystery boxes share the square " + parts[0]);
            }
        }
        return new BoxLayout(contents);
    }

    public String code() {
        List<String> entries = new ArrayList<>();
        contents.keySet().stream().sorted(SQUARE_ORDER)
                .forEach(box -> entries.add(Squares.name(box) + "=" + contents.get(box).code()));
        return String.join(",", entries);
    }

    private static Position square(String text) {
        if (!text.matches("[a-z][1-9][0-9]?")) {
            throw new IllegalArgumentException("Invalid mystery box square: '" + text + "'");
        }
        return new Position(text.charAt(0) - 'a', Integer.parseInt(text.substring(1)) - 1);
    }

    private static PowerType power(String code, List<PowerType> powers) {
        return powers.stream()
                .filter(power -> power.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown power code: '" + code + "'"));
    }
}
