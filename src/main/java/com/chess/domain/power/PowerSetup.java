package com.chess.domain.power;

import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;

import com.chess.domain.model.PowerType;

/**
 * Lo que hace falta guardar para reproducir los poderes de una partida: el contenido de las cajas iniciales y, si
 * durante la partida aparecen cajas nuevas, la semilla de ese sorteo. Se guarda como texto:
 * {@code "a5=i,c5=s,f4=f,h4=t|s=8125"}. Sin la parte {@code |s=} no hay semilla; una partida que la necesita usa 0.
 */
public record PowerSetup(BoxLayout boxes, OptionalLong seed) {

    private static final String SEED_SEPARATOR = "|s=";

    public PowerSetup {
        Objects.requireNonNull(boxes, "boxes must not be null");
        Objects.requireNonNull(seed, "seed must not be null, use OptionalLong.empty()");
    }

    /**
     * @throws IllegalArgumentException si el texto está mal formado
     */
    public static PowerSetup parse(String code, List<PowerType> powers) {
        Objects.requireNonNull(code, "code must not be null");
        int separator = code.indexOf(SEED_SEPARATOR);
        if (separator < 0) {
            return new PowerSetup(BoxLayout.parse(code, powers), OptionalLong.empty());
        }
        String seedText = code.substring(separator + SEED_SEPARATOR.length()).trim();
        try {
            return new PowerSetup(
                    BoxLayout.parse(code.substring(0, separator), powers),
                    OptionalLong.of(Long.parseLong(seedText)));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid mystery box seed: '" + seedText + "'", exception);
        }
    }

    public String code() {
        return boxes.code() + (seed.isPresent() ? SEED_SEPARATOR + seed.getAsLong() : "");
    }
}
