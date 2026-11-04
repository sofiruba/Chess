package com.chess.domain.power;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.chess.domain.model.Board;
import com.chess.domain.model.Piece;
import com.chess.domain.model.PieceRoles;
import com.chess.domain.model.Position;

/**
 * Base de los comandos de poder (Template Method): controla que no se ejecuten ni deshagan dos veces y define lo que
 * todos comparten. Cada poder completa {@link #apply()}, {@link #revert()} y su notación.
 *
 * <p>Por defecto la pieza que usa el poder no se mueve, por lo que no pierde derechos de enroque, y un poder nunca
 * habilita una captura al paso. El código de jugada es {@code @} seguido de la casilla de la pieza y la del objetivo
 * ("@e4e7"), lo que alcanza para reproducir el uso.
 */
abstract class PowerCommand implements IPowerCommand {

    static final String CODE_PREFIX = "@";

    protected final Board board;
    private final Position from;
    private final Position to;
    private final Piece holder;
    private boolean executed;

    PowerCommand(Board board, Position from, Position to) {
        this.board = Objects.requireNonNull(board, "board must not be null");
        this.from = Objects.requireNonNull(from, "from must not be null");
        this.to = Objects.requireNonNull(to, "to must not be null");
        this.holder = Objects.requireNonNull(board.pieceAt(from), "holder must not be null");
    }

    protected abstract void apply();

    protected abstract void revert();

    @Override
    public final void execute() {
        if (executed) {
            throw new IllegalStateException("Command has already been executed");
        }
        apply();
        executed = true;
    }

    @Override
    public final void undo() {
        if (!executed) {
            throw new IllegalStateException("Command has not been executed");
        }
        revert();
        executed = false;
    }

    @Override
    public Piece holder() {
        return holder;
    }

    @Override
    public Position from() {
        return from;
    }

    @Override
    public Position to() {
        return to;
    }

    @Override
    public Piece movingPiece() {
        return holder;
    }

    @Override
    public Piece capturedPiece() {
        List<CapturedPiece> captured = capturedPieces();
        return captured.isEmpty() ? null : captured.get(0).piece();
    }

    @Override
    public List<CapturedPiece> capturedPieces() {
        return List.of();
    }

    @Override
    public List<MovedPiece> movedPieces() {
        return List.of();
    }

    @Override
    public Optional<Position> enPassantSquare(PieceRoles roles) {
        return Optional.empty();
    }

    @Override
    public String moveCode() {
        return CODE_PREFIX + Squares.name(from) + Squares.name(to);
    }
}
