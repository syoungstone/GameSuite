import java.util.ArrayList;

class TicTacToe {

    // This constant is used throughout, and may be changed to have O go first
    private TileValue PLAYER_1_TOKEN = TileValue.X;

    // This boolean determines whether play is between two players or one player and the computer
    private boolean twoPlayerMode;

    // This variable determines who has won the game
    private WinCondition gameStatus;

    // This variable determines which token will be used for the next move
    private TileValue nextMove;

    // This array stores the value of each of the nine spaces on the board
    private TicTacToeTile[][] board;

    // This array references spaces within board for the purpose of analyzing the state of the game
    private TicTacToeTile[][] paths;

    // Constructs an empty board with associated paths array, takes value of twoPlayerMode & computerFirst as arguments
    TicTacToe(boolean twoPlayerMode, boolean computerFirst) {
        this.twoPlayerMode = twoPlayerMode;
        gameStatus = WinCondition.NONE;
        board = new TicTacToeTile[][] {
                {new TicTacToeTile(), new TicTacToeTile(), new TicTacToeTile()},
                {new TicTacToeTile(), new TicTacToeTile(), new TicTacToeTile()},
                {new TicTacToeTile(), new TicTacToeTile(), new TicTacToeTile()}
        };

        // Each row in paths represents a grouping of squares within board
        paths = new TicTacToeTile[][] {
                // Top row
                {board[0][0], board[0][1], board[0][2]},
                // Middle row
                {board[1][0], board[1][1], board[1][2]},
                // Bottom row
                {board[2][0], board[2][1], board[2][2]},
                // Left column
                {board[0][0], board[1][0], board[2][0]},
                // Center column
                {board[0][1], board[1][1], board[2][1]},
                // Right column
                {board[0][2], board[1][2], board[2][2]},
                // Diagonal from top left to bottom right
                {board[0][0], board[1][1], board[2][2]},
                // Diagonal from bottom left to top right
                {board[2][0], board[1][1], board[0][2]},
                // Corner squares, listed clockwise from top left
                {board[0][0], board[0][2], board[2][2], board[2][0]},
                // Middle side squares, listed clockwise from top center
                {board[0][1], board[1][2], board[2][1], board[1][0]}
        };

        // Set nextMove based on who goes first, calls setTile() on computerMove() move if computer goes first
        if (computerFirst) {
            nextMove = (PLAYER_1_TOKEN == TileValue.X ? TileValue.O : TileValue.X);
            setTile(computerMove());
        }
        else {
            nextMove = PLAYER_1_TOKEN;
        }
    }

    // Calls setTile on the selected tile
    void makeMove(int i, int j) {
        setTile(board[i][j]);
    }

    // Places an X or O on the board if and only if the chosen space is empty,
    // then updates the values of nextMove and gameStatus accordingly
    private void setTile(TicTacToeTile tile) {
        if (tile.getValue() == TileValue.EMPTY) {

            // Sets the value of the chosen tile to the token stored in nextMove
            tile.setValue(nextMove);

            // Sets nextMove to O if current X, and X if currently O
            nextMove = (nextMove == TileValue.X ? TileValue.O : TileValue.X);

            // Updates game status
            gameStatus = newGameStatus();

            // If the computer has a turn next, calls setTile() on computerMove()
            if (!twoPlayerMode && nextMove != PLAYER_1_TOKEN && gameStatus == WinCondition.NONE) {
                setTile(computerMove());
            }
        }
    }

    // Returns the value of a particular Tile on the board
    TileValue getTileValue(int i, int j) {
        return board[i][j].getValue();
    }

    // Returns the value of gameStatus
    WinCondition getGameStatus() {
        return gameStatus;
    }

    // Returns the value of twoPlayerMode
    boolean isTwoPlayerMode() {
        return twoPlayerMode;
    }

    // Returns ideal tile for computer to makes its move in
    private TicTacToeTile computerMove() {

        // Creates reference arrays by traversing all rows in paths
        // Indices 0-7 represent rows, columns, and diagonals
        // Index 8 represents corners, index 9 middle side squares

        // Represents number of existing computer moves within a given path
        int[] numComputerMoves = new int[paths.length];

        // Represents number of existing player moves within a given path
        int[] numPlayer1Moves = new int[paths.length];

        // Stores all open tiles within each given path
        ArrayList<ArrayList<TicTacToeTile>> openTilesList = new ArrayList<>();

        // Traverses paths and stores current values in numComputerMoves, numPlayer1Moves, and openTilesList
        for (int i = 0 ; i < paths.length ; i++) {
            ArrayList<TicTacToeTile> openTiles = new ArrayList<>();
            for (int j = 0 ; j < paths[i].length ; j++) {

                // Increments numPlayer1Moves if the square contains a move from Player 1
                if (paths[i][j].getValue() == PLAYER_1_TOKEN) {
                    numPlayer1Moves[i]++;
                }

                // Adds the current square to the current ArrayList in openTilesList if the square is empty
                else if (paths[i][j].getValue() == TileValue.EMPTY) {
                    openTiles.add(paths[i][j]);
                }

                // Increments numComputerMoves if the square contains a move from the computer
                else {
                    numComputerMoves[i]++;
                }
            }
            openTilesList.add(openTiles);
        }

        // The following code shows desired moves for the computer in descending priority

        // 1) If computer can win, make a winning move
        for (int i = 0 ; i < paths.length - 2 ; i++) {
            if (numComputerMoves[i] == 2 && numPlayer1Moves[i] == 0) {
                return openTilesList.get(i).get(0);
            }
        }

        // 2) If player has opportunity to win, block it
        for (int i = 0 ; i < paths.length - 2 ; i++) {
            if (numComputerMoves[i] == 0 && numPlayer1Moves[i] == 2) {
                return openTilesList.get(i).get(0);
            }
        }

        // 3) If possible, create a forking move (two possibilities to win on next move)
        for (int i = 0 ; i < paths.length - 2 ; i++) {
            if (numComputerMoves[i] == 1 && numPlayer1Moves[i] == 0) {
                for (int j = 0 ; j < paths.length - 2 ; j++) {
                    if (j != i && numComputerMoves[j] == 1 && numPlayer1Moves[j] == 0) {
                        for (TicTacToeTile t1 : openTilesList.get(i)) {
                            for (TicTacToeTile t2 : openTilesList.get(j)) {
                                if (t1 == t2) {
                                    return t1;
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4) If player has an opportunity to fork, block it
        for (int i = 0 ; i < paths.length - 2 ; i++) {
            if (numComputerMoves[i] == 0 && numPlayer1Moves[i] == 1) {
                for (int j = 0 ; j < paths.length - 2 ; j++) {
                    if (j != i && numComputerMoves[j] == 0 && numPlayer1Moves[j] == 1) {
                        for (TicTacToeTile t1 : openTilesList.get(i)) {
                            for (TicTacToeTile t2 : openTilesList.get(j)) {
                                if (t1 == t2) {
                                    return t1;
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5) If center square is available, choose it
        if (board[1][1].getValue() == TileValue.EMPTY) {
            return board[1][1];
        }

        // 6) If player is in a corner and opposite corner is available, choose it
        if (numPlayer1Moves[8] > 0 && numComputerMoves[8] + numPlayer1Moves[8] < 4) {
            if (board[0][0].getValue() == PLAYER_1_TOKEN && board[2][2].getValue() == TileValue.EMPTY) {
                return board[2][2];
            }
            if (board[0][2].getValue() == PLAYER_1_TOKEN && board[2][0].getValue() == TileValue.EMPTY) {
                return board[2][0];
            }
            if (board[2][2].getValue() == PLAYER_1_TOKEN && board[0][0].getValue() == TileValue.EMPTY) {
                return board[0][0];
            }
            if (board[2][0].getValue() == PLAYER_1_TOKEN && board[0][2].getValue() == TileValue.EMPTY) {
                return board[0][2];
            }
        }

        // 7) If an empty corner square is available, choose it
        if (numComputerMoves[8] + numPlayer1Moves[8] < 4) {
            return openTilesList.get(8).get(0);
        }

        // 8) If nothing else is available, play in an empty middle square on one of the sides
        else {
            return openTilesList.get(9).get(0);
        }

    }

    // This method calculates whether the game is over or not
    private WinCondition newGameStatus() {

        // Iterates over paths to check each row, column, and diagonal
        int blockedPaths = 0;
        for (int i = 0 ; i < paths.length - 2 ; i++) {

            // Count the number of moves from X and O along each path
            int numX = 0;
            int numO = 0;
            for (TicTacToeTile tile : paths[i]) {
                if (tile.getValue() == TileValue.X) {
                    numX++;
                }
                else if (tile.getValue() == TileValue.O) {
                    numO++;
                }
            }

            // If 3 Xs in a row are detected, X wins
            if (numX == 3) {
                return WinCondition.X;
            }

            // If 3 Os in a row are detected, O wins
            else if (numO == 3) {
                return WinCondition.O;
            }

            // If at least one move from each player is detected, increment the number of blocked paths
            else if (numX > 0 && numO > 0) {
                blockedPaths++;
            }
        }

        // If all paths are blocked, the game is a draw
        if (blockedPaths == 8) {
            return WinCondition.DRAW;
        }

        // If none of the above conditions are fulfilled, the game is not yet over
        else {
            return WinCondition.NONE;
        }
    }

}

// This enumerated type represents all possible win conditions for the game
enum WinCondition {
    NONE,X,O,DRAW
}

// This enumerated type represents all possible states for a square on the board
enum TileValue {
    EMPTY,X,O
}

// This class is essentially a wrapper class for TileValue. It is used so that any updates to a Tile object
// in the board array will be reflected immediately in the paths array.
class TicTacToeTile {
    private TileValue value;

    TicTacToeTile() {
        value = TileValue.EMPTY;
    }

    void setValue(TileValue value) {
        this.value = value;
    }

    TileValue getValue() {
        return value;
    }
}