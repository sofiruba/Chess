package com.chess;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chess.configuration.DefaultMoveCommandFactory;
import com.chess.configuration.GameSetup;
import com.chess.configuration.StandardGameFactory;
import com.chess.domain.model.Board;
import com.chess.domain.model.Color;
import com.chess.domain.model.IPieceFactory;
import com.chess.domain.model.Piece;
import com.chess.domain.model.PieceFactory;
import com.chess.domain.model.PieceType;
import com.chess.domain.model.Position;
import com.chess.domain.port.IGameService;
import com.chess.domain.rule.IMoveRule;

/**
 * Utilidades de prueba para crear posiciones desde diagramas de texto y jugar movimientos escritos como "e2e4" o
 * "e7e8q". Las filas del diagrama van desde la fila superior (primera) hasta la fila 1 (última); las mayúsculas son
 * blancas, las minúsculas negras y '.' está vacío. El diagrama debe ser cuadrado y su tamaño es el del tablero.
 */
public final class TestGames {

    private static final Pattern MOVE_PATTERN = Pattern.compile("([a-z]\\d+)([a-z]\\d+)([a-z])?");
    private static final IPieceFactory PIECES = new PieceFactory(GameSetup.standardPieceDefinitions());

    private TestGames() {
    }

    public static IGameService standard() {
        return new StandardGameFactory(new DefaultMoveCommandFactory()).create();
    }

    public static IGameService fromDiagram(Color turn, String... rows) {
        return fromDiagram(List.of(), turn, rows);
    }

    public static IGameService fromDiagram(List<IMoveRule> extraRules, Color turn, String... rows) {
        GameSetup setup = GameSetup.standard().withAdditionalMoveRules(extraRules);
        StandardGameFactory factory = new StandardGameFactory(new DefaultMoveCommandFactory(), setup);
        return factory.create(board(rows), turn);
    }

    public static Board board(String... rows) {
        Board board = new Board(rows.length);
        for (int row = 0; row < rows.length; row++) {
            placeRow(board, rows[row], rows.length - 1 - row);
        }
        return board;
    }

    public static Position at(String square) {
        int x = square.charAt(0) - 'a';
        int y = Integer.parseInt(square.substring(1)) - 1;
        return new Position(x, y);
    }

    /** The given squares as a set, to compare destinations without depending on the order of a list. */
    public static Set<Position> squares(String... squares) {
        List<Position> positions = new ArrayList<>();
        for (String square : squares) {
            positions.add(at(square));
        }
        return Set.copyOf(positions);
    }

    public static void play(IGameService game, String... moves) {
        for (String move : moves) {
            playOne(game, move);
        }
    }

    private static void playOne(IGameService game, String move) {
        Matcher matcher = MOVE_PATTERN.matcher(move);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid move text: " + move);
        }
        Position from = at(matcher.group(1));
        Position to = at(matcher.group(2));
        if (matcher.group(3) != null) {
            game.playMove(from, to, promotionType(matcher.group(3).charAt(0)));
        } else {
            game.playMove(from, to);
        }
    }

    private static void placeRow(Board board, String row, int y) {
        if (row.length() != board.size()) {
            throw new IllegalArgumentException(
                    "Diagram row must have " + board.size() + " squares like rows in the diagram: " + row);
        }
        for (int x = 0; x < row.length(); x++) {
            char symbol = row.charAt(x);
            if (symbol != '.') {
                board.placePiece(pieceFor(symbol), new Position(x, y));
            }
        }
    }

    private static Piece pieceFor(char symbol) {
        Color color = Character.isUpperCase(symbol) ? Color.WHITE : Color.BLACK;
        return PIECES.create(color, pieceType(Character.toLowerCase(symbol)));
    }

    private static PieceType pieceType(char letter) {
        return switch (letter) {
            case 'k' -> PieceType.KING;
            case 'q' -> PieceType.QUEEN;
            case 'r' -> PieceType.ROOK;
            case 'b' -> PieceType.BISHOP;
            case 'n' -> PieceType.KNIGHT;
            case 'p' -> PieceType.PAWN;
            default -> throw new IllegalArgumentException("Unknown piece letter: " + letter);
        };
    }

    private static PieceType promotionType(char letter) {
        return pieceType(Character.toLowerCase(letter));
    }
}
