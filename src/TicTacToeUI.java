import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

class TicTacToeUI extends Application {

    // These constants will be used to create the dimensions of the game board
    private final double SQUARE_SIZE = 200;
    private final double BORDER = 25;

    // The TicTacToe object contains all information about the state of the game
    private TicTacToe board;

    // These nodes are all used in scene1 to select how the game will be played
    private Text greeting = new Text("Select mode of play:");
    private Button pvp = new Button("Player vs. Player");
    private Button pvc = new Button("Player vs. Computer");
    private Button playerFirst = new Button("Player goes first");
    private Button computerFirst = new Button("Computer goes first");
    private FlowPane buttons = new FlowPane(pvp, pvc);
    private VBox pane1 = new VBox(greeting, buttons);
    private Scene scene1 = new Scene(pane1, 500, 400);

    // These nodes are all used in scene2 to construct the game board
    private Line vLine1 = new Line(SQUARE_SIZE + BORDER, BORDER,
            SQUARE_SIZE + BORDER, 3 * SQUARE_SIZE + BORDER);
    private Line vLine2 = new Line(2 * SQUARE_SIZE + BORDER, BORDER,
            2 * SQUARE_SIZE + BORDER, 3 * SQUARE_SIZE + BORDER);
    private Line hLine1 = new Line(BORDER, SQUARE_SIZE + BORDER,
            3 * SQUARE_SIZE + BORDER, SQUARE_SIZE + BORDER);
    private Line hLine2 = new Line(BORDER, 2 * SQUARE_SIZE + BORDER,
            3 * SQUARE_SIZE + BORDER, 2 * SQUARE_SIZE + BORDER);
    private Pane grid = new Pane(vLine1, vLine2, hLine1, hLine2);
    private Pane letters = new Pane();
    private StackPane pane2 = new StackPane(grid, letters);
    private Scene scene2 = new Scene(pane2, SQUARE_SIZE * 3 + BORDER * 2, SQUARE_SIZE * 3 + BORDER * 2);

    // These nodes appear only once the game is over
    private Text gameOverMessage = new Text();
    private Button playAgainButton = new Button("Play Again");
    private VBox gameOverPane = new VBox(gameOverMessage, playAgainButton);

    @Override
    public void start(Stage primaryStage) {

        // Sets font, spacing, and alignment for Nodes in scene1
        greeting.setTextAlignment(TextAlignment.CENTER);
        greeting.setFont(new Font(30));
        gameOverMessage.setTextAlignment(TextAlignment.CENTER);
        gameOverMessage.setFont(new Font(60));
        pvp.setFont(new Font(20));
        pvc.setFont(new Font(20));
        playAgainButton.setFont(new Font(20));
        playerFirst.setFont(new Font(20));
        computerFirst.setFont(new Font(20));
        buttons.setAlignment(Pos.CENTER);
        buttons.setHgap(10);
        pane1.setAlignment(Pos.CENTER);
        pane1.setSpacing(40);

        // Sets alignment and spacing for gameOverPane
        gameOverPane.setAlignment(Pos.CENTER);
        gameOverPane.setSpacing(40);

        // Sets stroke width for the lines in grid
        vLine1.setStrokeWidth(5);
        vLine2.setStrokeWidth(5);
        hLine1.setStrokeWidth(5);
        hLine2.setStrokeWidth(5);

        // If Player vs. Computer is selected, updates the buttons pane to include options for who goes first
        pvc.setOnAction(e -> {
            buttons.getChildren().clear();
            buttons.getChildren().addAll(playerFirst, computerFirst);
        });

        // Initializes board with selected boolean and changes the scene to scene2 (the game board)
        pvp.setOnAction(e -> {
            board = new TicTacToe(true, false);
            primaryStage.setScene(scene2);
        });
        playerFirst.setOnAction(e -> {
            board = new TicTacToe(false, false);
            primaryStage.setScene(scene2);
        });
        computerFirst.setOnAction(e -> {
            board = new TicTacToe(false, true);
            primaryStage.setScene(scene2);

            // The call to drawBoard() is added if the computer goes first so that the computer's move gets displayed
            drawBoard();
        });

        // This action listener is used to detect player moves on the board
        pane2.setOnMouseClicked(e -> {

            // Only activates if the game is not yet over
            if (board.getGameStatus() == WinCondition.NONE) {

                // Detects the coordinates of the click
                double x = e.getX();
                double y = e.getY();

                // Iterates through each space on the grid to see if the click occurred within one of them
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {

                        // Click must not have occurred too close to a grid line or edge for it to register
                        double xMin = j * SQUARE_SIZE + 2 * BORDER;
                        double xMax = (j + 1) * SQUARE_SIZE;
                        double yMin = i * SQUARE_SIZE + 2 * BORDER;
                        double yMax = (i + 1) * SQUARE_SIZE;
                        if (x > xMin && x < xMax && y > yMin && y < yMax) {

                            // Attempts a move at the selected square and redraws the board
                            board.makeMove(i, j);
                            drawBoard();

                            // If the game is now over, grey out the board and display gameOverPane
                            if (board.getGameStatus() != WinCondition.NONE) {
                                letters.setOpacity(0.3);
                                grid.setOpacity(0.3);
                                pane2.getChildren().add(gameOverPane);

                                // Sets text on gameOverPane to reflect the win condition and the mode of play
                                if (board.getGameStatus() == WinCondition.DRAW) {
                                    gameOverMessage.setText("DRAW");
                                }
                                else if (board.isTwoPlayerMode()) {
                                    if (board.getGameStatus() == WinCondition.X) {
                                        gameOverMessage.setText("X wins!");
                                    }
                                    else {
                                        gameOverMessage.setText("O wins!");
                                    }
                                }
                                else {
                                    if (board.getGameStatus() == WinCondition.X) {
                                        gameOverMessage.setText("You win!\nI am humbled by\nyour intelligence!");
                                    }
                                    else {
                                        gameOverMessage.setText("You lose!\nAnother demonstration\nof AI superiority!");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        // If playAgainButton is pressed, reset the board and return to selecting mode of play
        playAgainButton.setOnAction(e -> {
            letters.setOpacity(1);
            grid.setOpacity(1);
            pane2.getChildren().remove(gameOverPane);
            letters.getChildren().clear();
            buttons.getChildren().clear();
            buttons.getChildren().addAll(pvp, pvc);
            primaryStage.setScene(scene1);
        });

        // Sets title and initial scene for primaryStage, makes window size constant, and launches window
        primaryStage.setTitle("Tic Tac Toe");
        primaryStage.setScene(scene1);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    // This method clears the letters pane and redraws it in accordance with the state of each square within board
    private void drawBoard() {
        letters.getChildren().clear();
        for (int i = 0 ; i < 3 ; i++) {
            for (int j = 0 ; j < 3 ; j++) {
                if (board.getTileValue(i, j) == TileValue.X) {
                    drawX(i, j);
                }
                else if (board.getTileValue(i, j) == TileValue.O) {
                    drawO(i, j);
                }
            }
        }
    }

    // This method draws a red X at the given coordinates within the game board
    private void drawX(int i, int j) {
        double leftX = j * SQUARE_SIZE + 2 * BORDER;
        double rightX = (j + 1) * SQUARE_SIZE;
        double topY = i * SQUARE_SIZE + 2 * BORDER;
        double bottomY = (i + 1) * SQUARE_SIZE;
        Line Line1 = new Line(leftX, bottomY, rightX, topY);
        Line Line2 = new Line(leftX, topY, rightX, bottomY);
        Line1.setStrokeWidth(10);
        Line2.setStrokeWidth(10);
        Line1.setStroke(Color.RED);
        Line2.setStroke(Color.RED);
        letters.getChildren().addAll(Line1, Line2);
    }

    // This method draws a blue O at the given coordinates within the game board
    private void drawO(int i, int j) {
        double x = (j + 0.5) * SQUARE_SIZE + BORDER;
        double y = (i + 0.5) * SQUARE_SIZE + BORDER;
        double radius = SQUARE_SIZE * 0.5 - BORDER;
        Circle o = new Circle(x, y, radius);
        o.setFill(Color.WHITE);
        o.setStroke(Color.BLUE);
        o.setStrokeWidth(10);
        letters.getChildren().add(o);
    }

    public static void main(String[] args) {
        launch(args);
    }

}