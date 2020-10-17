import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;

class MinesweeperUI extends Application {

    // This constant will be used to create the dimensions of the game board
    private final int SQUARE_SIZE = 20;

    // The Minesweeper object contains all information about the state of the game
    private Minesweeper board;

    // These Nodes are all used in scene1 to select difficulty of play
    private Text greeting = new Text("Select mode of play:");
    private Button beginner = new Button("Beginner");
    private Button intermediate = new Button("Intermediate");
    private Button advanced = new Button("Advanced");
    private Button custom = new Button("Custom");
    private VBox buttons = new VBox(beginner, intermediate, advanced, custom);
    private VBox pane1 = new VBox(greeting, buttons);
    private Scene scene1 = new Scene(pane1, 500, 400);

    // If choosing custom parameters, pane1 is modified to use these Nodes
    private ComboBox<Integer> heightBox = new ComboBox<>(FXCollections.observableArrayList(generateArray(8, 25)));
    private ComboBox<Integer> widthBox = new ComboBox<>(FXCollections.observableArrayList(generateArray(8, 50)));
    private ComboBox<Integer> numMinesBox = new ComboBox<>(FXCollections.observableArrayList(generateArray(1, 32)));
    private Text heightLabel = new Text("Height:");
    private Text widthLabel = new Text("Width:");
    private Text numMinesLabel = new Text("Mines:");
    private Button startButton = new Button("Start Game");

    // These Nodes are used to display the game board once parameters have been set
    private Counter mineCounter = new Counter();
    private Button resetButton = new Button("Reset");
    private Counter timeCounter = new Counter();
    private BorderPane header = new BorderPane(resetButton, null, timeCounter, null, mineCounter);
    private VBox gameGrid = new VBox();
    private VBox pane2 = new VBox(header, gameGrid);
    private Scene scene2 = new Scene(pane2);

    // The Timer object gameTimer will be used to update timeCounter
    private Timer gameTimer = new Timer();

    @Override
    public void start(Stage primaryStage) {

        // Sets font, dimensions, and alignment for Nodes in scene1
        greeting.setTextAlignment(TextAlignment.CENTER);
        greeting.setFont(new Font(30));
        beginner.setFont(new Font(20));
        beginner.setPrefWidth(200);
        intermediate.setFont(new Font(20));
        intermediate.setPrefWidth(200);
        advanced.setFont(new Font(20));
        advanced.setPrefWidth(200);
        custom.setFont(new Font(20));
        custom.setPrefWidth(200);
        buttons.setAlignment(Pos.CENTER);
        pane1.setAlignment(Pos.CENTER);
        pane1.setSpacing(40);

        // Each difficulty level calls buildGameGrid with preset values and sets scene to scene2
        beginner.setOnAction(e -> {
            buildGameGrid(8, 8, 10);
            primaryStage.setScene(scene2);
        });
        intermediate.setOnAction(e -> {
            buildGameGrid(16, 16, 40);
            primaryStage.setScene(scene2);
        });
        advanced.setOnAction(e -> {
            buildGameGrid(16, 31, 99);
            primaryStage.setScene(scene2);
        });

        // If custom is selected, pane1 is reorganized to include drop down menus for each parameter
        custom.setOnAction(e -> {

            // Sets default starting values for the ComboBoxes
            heightBox.setValue(8);
            widthBox.setValue(8);
            numMinesBox.setValue(10);

            // Sets font size for Labels and startButton
            heightLabel.setFont(new Font(15));
            widthLabel.setFont(new Font(15));
            numMinesLabel.setFont(new Font(15));
            startButton.setFont(new Font(20));

            // Reorganizes pane1 to show Labels, ComboBoxes, and startButton
            pane1.getChildren().remove(buttons);
            FlowPane choices = new FlowPane(heightLabel, heightBox, widthLabel, widthBox, numMinesLabel, numMinesBox);
            choices.setAlignment(Pos.CENTER);
            choices.setHgap(10);
            pane1.getChildren().add(choices);
            pane1.getChildren().add(startButton);

            // If height or width are adjusted, changes range and selected value of numMinesBox accordingly
            // Max value in numMinesBox should not exceed height * width / 2
            // Selected value in numMinesBox should not exceed the max value
            heightBox.setOnAction(f -> {
                int currentMines = numMinesBox.getValue();
                int maxMines = heightBox.getValue() * widthBox.getValue() / 2;
                numMinesBox.setItems(FXCollections.observableArrayList(generateArray(1, maxMines)));
                numMinesBox.setValue(Math.min(currentMines, maxMines));
            });
            widthBox.setOnAction(f -> {
                int currentMines = numMinesBox.getValue();
                int maxMines = heightBox.getValue() * widthBox.getValue() / 2;
                numMinesBox.setItems(FXCollections.observableArrayList(generateArray(1, maxMines)));
                numMinesBox.setValue(Math.min(currentMines, maxMines));
            });

            // When pressed, startButton passes buildGameGrid the values of the ComboBoxes and sets the scene
            startButton.setOnAction(f -> {
                buildGameGrid(heightBox.getValue(), widthBox.getValue(), numMinesBox.getValue());
                primaryStage.setScene(scene2);
            });

        });

        // Sets title and initial scene for primaryStage, makes window size constant, and launches window
        primaryStage.setTitle("Minesweeper");
        primaryStage.setScene(scene1);
        primaryStage.setResizable(false);
        primaryStage.show();

        // Closes out gameTimer when window is closed
        primaryStage.setOnCloseRequest(e -> {
            gameTimer.cancel();
            gameTimer.purge();
        });
    }

    // This method prepares all necessary conditions for the game board to run
    private void buildGameGrid(int height, int width, int numMines) {

        // Initializes board by creating a Minesweeper object using the given parameters
        board = new Minesweeper(height, width, numMines);

        // Sets the mineCounter to reflect the number of mines on the board
        mineCounter.setValue(numMines);

        // Creates a border of width SQUARE_SIZE around the board and header
        gameGrid.setPadding(new Insets(0, SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE));
        header.setPadding(new Insets(SQUARE_SIZE));

        // Sets preferred size of resetButton
        resetButton.setPrefSize(50, 25);

        // When resetButton is pressed, reset board, clear and rebuild the grid, reset timeCounter and gameTimer
        resetButton.setOnAction(e -> {
            board.reset();
            gameGrid.getChildren().clear();
            buildGameGrid(board.getHeight(), board.getWidth(), board.getNumFlagsRemaining());
            timeCounter.setValue(0);
            gameTimer.cancel();
            gameTimer = new Timer();
        });

        // Constructs the grid of buttons that make up the game board
        for (int i = 0 ; i < board.getHeight() ; i++) {

            // Each row in the grid is an HBox
            HBox row = new HBox();
            for (int j = 0 ; j < board.getWidth() ; j++) {

                // Necessary to use the values i and j in upcoming lambda statements
                int y = i;
                int x = j;

                // Creates a button of fixed size for each square on the board
                Button square = new Button();
                square.setMaxSize(SQUARE_SIZE, SQUARE_SIZE);
                square.setMinSize(SQUARE_SIZE, SQUARE_SIZE);

                // Necessary to get emojis to fit on the button
                square.setPadding(Insets.EMPTY);

                // Actions to be completed if button is pressed or right-clicked
                square.setOnMouseClicked(e -> {

                    // Only performs an action if game is not yet over
                    if (!board.isComplete() && !board.isDead()) {

                        // If this is the first move, set gameTimer to start incrementing timeCounter every second
                        if (!board.isInitialized()) {
                            gameTimer.scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    timeCounter.setValue(timeCounter.getValue() + 1);
                                }
                            }, 1000, 1000);
                        }

                        // Calls board.flag() on chosen square if a right click or board.uncover() if a left click
                        if (e.getButton() == MouseButton.SECONDARY) {
                            board.flag(y, x);
                        } else {
                            board.uncover(y, x);
                        }

                        // Updates gameGrid and mineCounter to reflect any changes
                        updateGameGrid();
                        mineCounter.setValue(board.getNumFlagsRemaining());
                    }
                });

                // Adds each square to the current row
                row.getChildren().add(square);
            }

            // Adds each row to gameGrid
            gameGrid.getChildren().add(row);
        }
    }

    // This method is called so that any changes in board are reflected in gameGrid
    private void updateGameGrid() {

        // If the game is over, reset gameTimer so it stops incrementing timeCounter
        if (board.isDead() || board.isComplete()) {
            gameTimer.cancel();
            gameTimer.purge();
        }

        // Iterates over every square in gameGrid
        for (int i = 0 ; i < board.getHeight() ; i++) {
            HBox row = (HBox)gameGrid.getChildren().get(i);
            for (int j = 0 ; j < board.getWidth() ; j++) {

                // Accesses the state of the corresponding tile in board
                MinesweeperTile tile = board.getTile(i, j);

                // If a covered square is flagged or has a question mark, a corresponding emoji is added to the button
                // If not, the button is updated to display nothing
                if (tile == MinesweeperTile.FLAG) {
                    Button button = ((Button)row.getChildren().get(j));
                    button.setText("\uD83D\uDEA9");
                    if (board.isComplete()) {
                        button.setBackground(new Background(new BackgroundFill(Color.GREEN, null ,null)));
                    }
                }
                else if (tile == MinesweeperTile.MAYBE) {
                    ((Button)row.getChildren().get(j)).setText("❓");
                }
                else if (tile == MinesweeperTile.COVERED) {
                    ((Button)row.getChildren().get(j)).setText("");
                }

                // If the square has been uncovered, display it instead as a grey square
                else {
                    StackPane space = new StackPane();
                    space.setMaxSize(SQUARE_SIZE, SQUARE_SIZE);
                    space.setMinSize(SQUARE_SIZE, SQUARE_SIZE);
                    Rectangle square = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
                    square.setFill(Color.LIGHTGREY);
                    square.setStroke(Color.DARKGREY);
                    square.setStrokeWidth(1);
                    space.getChildren().add(square);

                    // Creates a label for non-empty squares
                    if (tile != MinesweeperTile.EMPTY) {
                        Text symbol = new Text();

                        // If the user clicked on a mine, turn the square red and display a skull and crossbones emoji
                        if (tile == MinesweeperTile.DEATH) {
                            symbol.setText("☠");
                            square.setFill(Color.RED);
                        }

                        // If the game is lost, display all other squares with mines as bomb emojis
                        else if (tile == MinesweeperTile.MINE) {
                            symbol.setText("\uD83D\uDCA3");
                        }

                        // Otherwise display the number of adjacent bombs
                        else {
                            MinesweeperTile[] tiles = {MinesweeperTile.B1, MinesweeperTile.B2,
                                    MinesweeperTile.B3, MinesweeperTile.B4, MinesweeperTile.B5,
                                    MinesweeperTile.B6, MinesweeperTile.B7, MinesweeperTile.B8};
                            for (int t = 0 ; t < tiles.length ; t++) {
                                if (tiles[t] == tile) {
                                    symbol.setText("" + (t + 1));
                                }
                            }
                        }

                        // Adds the chosen number or emoji to the square
                        space.getChildren().add(symbol);
                    }

                    // Saves i and j to local variables for use in the following lambda expression
                    int y = i;
                    int x = j;

                    // If an uncovered space is double-clicked, call board.doubleClick() on it and update gameGrid
                    space.setOnMouseClicked(e -> {
                        if (e.getClickCount() == 2) {
                            board.doubleClick(y, x);
                            updateGameGrid();
                        }
                    });

                    // Sets the value of the uncovered square in gameGrid to the newly created StackPane object space
                    row.getChildren().set(j, space);
                }
            }
        }
    }

    // This method is used to create an array of Integers of increasing value to be used for the above ComboBoxes
    private Integer[] generateArray(int min, int max) {
        int value = min;
        Integer[] array = new Integer[max - min + 1];
        for (int i = 0 ; i < array.length ; i++, value++) {
            array[i] = value;
        }
        return array;
    }

    public static void main(String[] args) {
        launch(args);
    }

}

// The Counter object is a StackPane containing three characters of text to represent an integer
class Counter extends StackPane {

    private Text text;
    private int value;

    // The only public constructor creates a Counter with a value of zero
    Counter() {
        this(0);
    }

    // This constructor initializes text, calls setValue, sets a monospaced font, and adds text to the StackPane
    private Counter(int value) {
        super();
        text = new Text();
        setValue(value);
        text.setFont(new Font("Consolas", 25));
        super.getChildren().addAll(text);
    }

    // SetValue updates this.value and sets each character to reflect a digit of value or a minus sign
    void setValue(int value) {
        this.value = value;
        char digit3;
        char digit2;
        char digit1;
        if (value < 0) {
            digit1 = '-';
            digit2 = (char)('0' - value % 100 / 10);
            digit3 = (char)('0' - value % 10);
        }
        else {
            digit1 = (char)('0' + value % 1000 / 100);
            digit2 = (char)('0' + value % 100 / 10);
            digit3 = (char)('0' + value % 10);
        }
        text.setText("" + digit1 + digit2 + digit3);
    }

    // Returns the current value stored by the counter
    int getValue() {
        return value;
    }

}