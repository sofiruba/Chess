package com.chess.ui;

import java.util.List;

import com.chess.domain.model.Color;
import com.chess.domain.model.PieceType;

/**
 * Asks the player which piece a pawn becomes when it reaches the last rank. It exists so that ChessApplication
 * does not depend on the dialog that shows the question.
 */
public interface IPromotionChooser {

    /**
     * @param color the color of the promoting pawn, so the options can be drawn in that color
     * @param options the pieces the game allows, in the order they should be shown; never empty
     * @return one of the options
     */
    PieceType choosePiece(Color color, List<PieceType> options);
}