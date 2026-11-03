package com.chess.domain.port;

import java.util.List;

import com.chess.domain.model.Color;
import com.chess.domain.model.GameResult;
import com.chess.domain.model.Piece;
import com.chess.domain.model.PieceType;
import com.chess.domain.model.Position;
import com.chess.domain.model.PowerType;

/**
 * Vista de solo lectura de una partida. Quien la recibe no puede modificarla.
 */
public interface IGameQueries {
    int boardSize();
    Piece pieceAt(Position position);
    Color turn();

    /** Es verdadero cuando hay una pieza en la casilla y es del jugador que debe mover. */
    boolean belongsToActivePlayer(Position position);

    boolean isInCheck(Color color);
    Position kingInCheckPosition();
    List<Position> legalMoves(Position from);
    boolean requiresPromotion(Position from, Position to);

    /** Las piezas en las que puede convertirse un peón, en el orden en que se deben ofrecer. */
    List<PieceType> promotionOptions();
    boolean isFinished();
    GameResult result();
    List<Piece> capturedPieces();
    List<String> moveNotation();
    List<String> moveCodes();
    List<MoveHistoryEntry> moveHistory();

    /** Las casillas con cajas misteriosas sin abrir. Su contenido no se revela hasta que una pieza las abre. */
    List<Position> mysteryBoxes();

    /** El poder que lleva la pieza de esa casilla, o null si está vacía o la pieza no lleva ninguno. */
    PowerType powerAt(Position position);

    /** Los poderes que tienen guardados las piezas de un color. Ambos jugadores pueden ver los del otro. */
    List<HeldPower> heldPowers(Color color);

    boolean isFrozen(Position position);

    boolean isImmune(Position position);

    /** Objetivos legales del poder de la pieza en {@code holder}; vacío si no puede usar ninguno ahora. */
    List<Position> powerTargets(Position holder);

    /** El contenido inicial de las cajas, necesario para reproducir la partida (vacío si no hay cajas). */
    String setupCode();
}