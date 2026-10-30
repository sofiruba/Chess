package com.chess.ui;

import java.util.List;

import com.chess.domain.model.Color;
import com.chess.domain.model.PieceType;

/**
 * Pregunta al jugador en qué pieza se convierte un peón cuando llega a la última fila. Existe para que
 * ChessApplication no dependa del diálogo que muestra la pregunta.
 */
public interface IPromotionChooser {

    /**
     * @param color el color del peón que promociona, para dibujar las opciones con ese color
     * @param options las piezas permitidas por la partida, en el orden en que deben mostrarse; nunca está vacío
     * @return una de las opciones
     */
    PieceType choosePiece(Color color, List<PieceType> options);
}
