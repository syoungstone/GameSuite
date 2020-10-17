import javafx.animation.KeyFrame;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class GameSuite extends Application {

    // Creates the text, buttons, panes, and overall scene for the GameSuite window
    private Text greeting = new Text("Welcome to the Game Suite!\nPick a game to play:");
    private Button ticTacToe = new Button("Tic Tac Toe");
    private Button minesweeper = new Button("Minesweeper");
    private Button pool = new Button("8-Ball Pool (unfinished)");
    private VBox buttons = new VBox(ticTacToe, minesweeper, pool);
    private VBox pane = new VBox(greeting, buttons);
    private Scene scene = new Scene(pane, 500, 400);

    @Override
    public void start(Stage primaryStage) {

        // Set alignment and font for the greeting text
        greeting.setTextAlignment(TextAlignment.CENTER);
        greeting.setFont(new Font(30));

        // Set alignment, font, and spacing for the buttons
        ticTacToe.setFont(new Font(20));
        ticTacToe.setPrefWidth(300 );
        minesweeper.setFont(new Font(20));
        minesweeper.setPrefWidth(300);
        pool.setFont(new Font(20));
        pool.setPrefWidth(300);
        buttons.setAlignment(Pos.CENTER);

        // Set alignment and spacing for the pane that groups them together
        pane.setAlignment(Pos.CENTER);
        pane.setSpacing(40);

        // When pressed, ticTacToe button closes window and launches TicTacToeUI
        ticTacToe.setOnAction(e -> {
            primaryStage.close();
            TicTacToeUI ticTacToeUI = new TicTacToeUI();
            ticTacToeUI.start(primaryStage);
        });

        // When pressed, minesweeper button closes window and launches MinesweeperUI
        minesweeper.setOnAction(e -> {
            primaryStage.close();
            MinesweeperUI minesweeperUI = new MinesweeperUI();
            minesweeperUI.start(primaryStage);
        });

        // When pressed, minesweeper button closes window and launches MinesweeperUI
        pool.setOnAction(e -> {
            primaryStage.close();
            PoolUI poolUI = new PoolUI();
            poolUI.start(primaryStage);
        });

        // Sets title and scene for primaryStage, makes window size constant, and launches window
        primaryStage.setTitle("Game Suite");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
