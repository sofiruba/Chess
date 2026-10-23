package com.chess.ui;

import com.chess.domain.PieceType;

/**
 * Asks the player which piece a pawn becomes when it reaches the last rank. It exists so that ChessApplication
 * does not depend on the dialog that shows the question.
 */
public interface IPromotionChooser {

    PieceType choosePiece();
}