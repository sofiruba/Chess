package com.chess;

import java.time.Clock;

import com.chess.configuration.DefaultMoveCommandFactory;
import com.chess.configuration.StandardGameFactory;
import com.chess.domain.game.GameArchive;
import com.chess.persistence.SqliteGameRepository;
import com.chess.ui.ChessApplication;
import com.chess.ui.PieceSymbols;
import com.chess.ui.PromotionDialog;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Main extends Application {

    @Override
    public void start(Stage stage) {
        SqliteGameRepository repository = new SqliteGameRepository("chess.db");
        StandardGameFactory gameFactory = new StandardGameFactory(new DefaultMoveCommandFactory());
        GameArchive archive = new GameArchive(repository, Clock.systemDefaultZone());
        PieceSymbols symbols = PieceSymbols.standard();
        ChessApplication application = new ChessApplication(
                archive,
                gameFactory,
                symbols,
                new PromotionDialog(symbols));
        stage.setTitle("Ajedrez");
        stage.setScene(new Scene(application.createView()));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}