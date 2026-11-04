package com.chess.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

/**
 * Dibuja lo que la mecánica de las cajas agrega al tablero: el bloque "?" de las cajas sin abrir, el hielo de una
 * pieza congelada y el arcoíris de una pieza con estrella. Separado de {@link BoardView} para que el tablero solo
 * decida qué mostrar en cada casilla y esta clase, cómo se ve.
 *
 * <p>El "?" se dibuja como figura en pixel art y no como texto, porque los signos de las tipografías no están
 * centrados en su caja: una figura queda centrada exactamente a cualquier tamaño.
 */
final class PowerVisuals {

    /** El "?" del bloque de Mario en una grilla de 6 x 8 píxeles; X es un píxel pintado. */
    private static final String[] QUESTION_MARK = {
            ".XXXX.",
            "XX..XX",
            "....XX",
            "...XX.",
            "..XX..",
            "..XX..",
            "......",
            "..XX.."};

    private static final Color BLOCK_TEXT = Color.web("#fff6d6");
    private static final Color BLOCK_SHADOW = Color.web("#8a4c26");
    private static final Color ICE_GLOW = Color.web("#7fd8ff");
    private static final Color STAR_GLOW = Color.web("#ffd84a");
    private static final Duration RAINBOW_CYCLE = Duration.millis(1100);

    private final List<Animation> animations = new ArrayList<>();

    /** Detiene las animaciones de las casillas anteriores; se llama antes de volver a dibujar el tablero. */
    void stopAnimations() {
        animations.forEach(Animation::stop);
        animations.clear();
    }

    /** El bloque "?" de una caja sin abrir, con el signo centrado y los cuatro remaches de las esquinas. */
    StackPane mysteryBox(double side) {
        StackPane block = new StackPane();
        block.getStyleClass().add("mystery-box");
        block.setMinSize(side, side);
        block.setPrefSize(side, side);
        block.setMaxSize(side, side);

        SVGPath mark = new SVGPath();
        mark.setContent(pixelPath(QUESTION_MARK, side * 0.075));
        mark.setFill(BLOCK_TEXT);
        mark.setEffect(new DropShadow(BlurType.ONE_PASS_BOX, BLOCK_SHADOW, 0, 0, side * 0.04, side * 0.04));
        block.getChildren().add(mark);

        double rivet = Math.max(1.5, side * 0.045);
        double inset = side * 0.13;
        for (Pos corner : List.of(Pos.TOP_LEFT, Pos.TOP_RIGHT, Pos.BOTTOM_LEFT, Pos.BOTTOM_RIGHT)) {
            Circle dot = new Circle(rivet, BLOCK_SHADOW);
            StackPane.setAlignment(dot, corner);
            StackPane.setMargin(dot, new javafx.geometry.Insets(inset));
            block.getChildren().add(dot);
        }
        return block;
    }

    /** Tiñe la pieza de celeste y le da un aura fría. */
    void freeze(Node piece) {
        ColorAdjust tint = new ColorAdjust(-0.06, -0.3, 0.04, 0);
        DropShadow aura = new DropShadow(BlurType.GAUSSIAN, ICE_GLOW, 14, 0.55, 0, 0);
        aura.setInput(tint);
        piece.setEffect(aura);
    }

    /** El bloque de hielo translúcido que encierra a la pieza congelada, con un copo de nieve en la esquina. */
    StackPane iceBlock(double squareSize) {
        double side = squareSize - 8;
        Region ice = new Region();
        ice.getStyleClass().add("ice-block");
        ice.setMinSize(side, side);
        ice.setPrefSize(side, side);
        ice.setMaxSize(side, side);

        Region shine = new Region();
        shine.getStyleClass().add("ice-shine");
        shine.setMaxSize(side * 0.12, side * 0.5);
        shine.setRotate(28);
        StackPane.setAlignment(shine, Pos.TOP_LEFT);
        StackPane.setMargin(shine, new javafx.geometry.Insets(side * 0.08, 0, 0, side * 0.2));

        SVGPath flake = snowflake(Math.max(6, squareSize * 0.13));
        StackPane.setAlignment(flake, Pos.BOTTOM_LEFT);
        StackPane.setMargin(flake, new javafx.geometry.Insets(0, 0, squareSize * 0.08, squareSize * 0.08));

        StackPane block = new StackPane(ice, shine, flake);
        block.setMouseTransparent(true);
        return block;
    }

    /**
     * La pieza cambia de color en un ciclo de arcoíris, como Mario con la estrella, con un aura dorada. El arcoíris
     * se anima girando el tono de la imagen.
     */
    void starPower(Node piece) {
        ColorAdjust rainbow = new ColorAdjust(0, 0.35, 0.08, 0.1);
        DropShadow aura = new DropShadow(BlurType.GAUSSIAN, STAR_GLOW, 16, 0.6, 0, 0);
        aura.setInput(rainbow);
        piece.setEffect(aura);
        Timeline cycle = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(rainbow.hueProperty(), -1, Interpolator.LINEAR)),
                new KeyFrame(RAINBOW_CYCLE, new KeyValue(rainbow.hueProperty(), 1, Interpolator.LINEAR)));
        cycle.setCycleCount(Animation.INDEFINITE);
        cycle.play();
        animations.add(cycle);
    }

    /** Un copo de seis brazos, dibujado con trazos para no depender de que la fuente tenga el símbolo. */
    private static SVGPath snowflake(double size) {
        double r = size / 2;
        StringBuilder path = new StringBuilder();
        for (int arm = 0; arm < 3; arm++) {
            double angle = Math.toRadians(90 + arm * 60);
            double dx = r * Math.cos(angle);
            double dy = r * Math.sin(angle);
            path.append(String.format(java.util.Locale.ROOT, "M %.2f %.2f L %.2f %.2f ", -dx, -dy, dx, dy));
        }
        SVGPath flake = new SVGPath();
        flake.setContent(path.toString());
        flake.setStroke(Color.WHITE);
        flake.setStrokeWidth(Math.max(1.2, size * 0.16));
        flake.setStrokeLineCap(StrokeLineCap.ROUND);
        flake.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.web("#2f86c6"), 3, 0.4, 0, 0));
        return flake;
    }

    /** Une los píxeles pintados de la grilla en una sola figura, cada uno de {@code pixel} de lado. */
    private static String pixelPath(String[] rows, double pixel) {
        StringBuilder path = new StringBuilder();
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                if (rows[y].charAt(x) == 'X') {
                    path.append(String.format(java.util.Locale.ROOT, "M %.2f %.2f h %.2f v %.2f h %.2f Z ",
                            x * pixel, y * pixel, pixel, pixel, -pixel));
                }
            }
        }
        return path.toString();
    }
}
