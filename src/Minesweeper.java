class Minesweeper {

    // This array will be used later to more easily convert integers to Tile states
    private static MinesweeperTile[] TOKENS = {MinesweeperTile.EMPTY,
            MinesweeperTile.B1, MinesweeperTile.B2, MinesweeperTile.B3, MinesweeperTile.B4,
            MinesweeperTile.B5, MinesweeperTile.B6, MinesweeperTile.B7, MinesweeperTile.B8};

    // Indicates whether a starting square has been selected yet
    private boolean initialized;

    // Indicate whether the game has been won or lost
    private boolean dead;
    private boolean complete;

    // These are the necessary starting values required to construct the board
    private int height;
    private int width;
    private int numMines;

    // These will be updated to reflect the state of the board
    private int numFlags;
    private int numUncovered;

    // This array contains the absolute state of each square (-1 for a mine, 0-8 for number of adjacent mines)
    private int[][] board;

    // This array contains the state of each square as displayed for the user
    private MinesweeperTile[][] display;

    // The constructor sets height, width, and numMines and then calls the reset() method
    Minesweeper(int height, int width, int numMines) {
        this.height = height;
        this.width = width;
        this.numMines = numMines;
        reset();
    }

    // This method creates a new, uninitialized board and display, and sets all values to start conditions
    void reset() {
        initialized = false;
        dead = false;
        complete = false;
        numFlags = 0;
        numUncovered = 0;
        board = new int[height][width];
        display = new MinesweeperTile[height][width];
        for (MinesweeperTile[] row : display) {
            java.util.Arrays.fill(row, MinesweeperTile.COVERED);
        }
    }

    // Returns the state of a given square on the display
    MinesweeperTile getTile(int i, int j) {
        return display[i][j];
    }

    // Returns number of mines left to flag
    int getNumFlagsRemaining() {
        return numMines - numFlags;
    }

    // Returns height of the board
    int getHeight() {
        return height;
    }

    // Returns width of the board
    int getWidth() {
        return width;
    }

    // Returns value of boolean initialized
    boolean isInitialized() {
        return initialized;
    }

    // Returns value of boolean dead
    boolean isDead() {
        return dead;
    }

    // Returns value of boolean complete
    boolean isComplete() {
        return complete;
    }

    // Called in the event of a double-click on a square by a user
    // If the number of adjacent flags matches the number of adjacent mines,
    // this method will uncover all unflagged adjacent squares
    void doubleClick(int y, int x) {
        if (board[y][x] > 0) {

            // Iterates over all adjacent squares to add up the number of flags
            int localFlags = 0;
            for (int i = y - 1 ; i <= y + 1 ; i++) {
                for (int j = x - 1 ; j <= x + 1 ; j++) {
                    if(i >= 0 && i < height && j >= 0 && j < width) {
                        if (display[i][j] == MinesweeperTile.FLAG) {
                            localFlags++;
                        }
                    }
                }
            }

            // If the proper number of adjacent squares are flagged, uncover is called on all adjacent squares
            if (localFlags == board[y][x]) {
                for (int i = y - 1; i <= y + 1; i++) {
                    for (int j = x - 1; j <= x + 1; j++) {
                        if (i != y || j != x) {
                            uncover(i, j);
                        }
                    }
                }
            }
        }
    }

    // This method is called any time the user attempts to uncover a square
    void uncover(int y, int x) {

        // If the board has not yet been initialized, populateBoard is called on the chosen square
        if (!initialized) {
            populateBoard(y, x);
            initialized = true;
        }

        // Checks that both coordinates are in range and the square is question is not flagged or already uncovered
        if (y < height && y >= 0 && x < width && x >= 0 &&
                (display[y][x] == MinesweeperTile.COVERED || display[y][x] == MinesweeperTile.MAYBE)) {

            // If a mine is chosen, display the death condition on chosen square, show all mines, and set dead to true
            if (board[y][x] == -1) {
                display[y][x] = MinesweeperTile.DEATH;
                for (int i = 0 ; i < height ; i++) {
                    for (int j = 0 ; j < width ; j++) {
                        if (board[i][j] == -1 && !(i == y && j == x) && display[i][j] != MinesweeperTile.FLAG) {
                            display[i][j] = MinesweeperTile.MINE;
                        }
                    }
                }
                dead = true;
            }

            // If the square does not contain a mine, show the number of adjacent mines
            else {
                display[y][x] = TOKENS[board[y][x]];

                // If the square has no adjacent mines, uncover all adjacent squares as well
                if (board[y][x] == 0) {
                    for (int i = y - 1 ; i <= y + 1 ; i++) {
                        for (int j = x - 1 ; j <= x + 1 ; j++) {
                            if (i != y || j != x) {
                                uncover(i, j);
                            }
                        }
                    }
                }

                // Update the number of uncovered squares
                numUncovered++;

                // If the only remaining covered squares are mines, display flags on all mines and set complete to true
                if (numUncovered == height * width - numMines) {
                    for (int i = 0 ; i < height ; i++) {
                        for (int j = 0 ; j < width ; j++) {
                            if (board[i][j] == -1 && display[i][j] != MinesweeperTile.FLAG) {
                                display[i][j] = MinesweeperTile.FLAG;
                            }
                        }
                    }
                    complete = true;
                }
            }
        }
    }

    // This method is used to flag/unflag the selected square
    void flag(int y, int x) {

        // If the square is blank, flag it and update numFlags
        if (display[y][x] == MinesweeperTile.COVERED) {
            display[y][x] = MinesweeperTile.FLAG;
            numFlags++;
        }

        // If the the square is flagged, change it to maybe and update numFlags
        else if (display[y][x] == MinesweeperTile.FLAG) {
            display[y][x] = MinesweeperTile.MAYBE;
            numFlags--;
        }

        // If the square is a maybe, turn it blank again
        else if (display[y][x] == MinesweeperTile.MAYBE) {
            display[y][x] = MinesweeperTile.COVERED;
        }
    }

    // This method is used to generate the locations of mines along with counts of adjacent mines for empty squares
    private void populateBoard(int startY, int startX) {

        // Randomly places all mines onto empty squares which are not adjacent to the chosen starting square
        int minesLeft = numMines;
        while (minesLeft > 0) {
            int randY = (int) (Math.random() * height);
            int randX = (int) (Math.random() * width);
            boolean badRow = startY == randY || startY == randY - 1 || startY == randY + 1;
            boolean badColumn = startX == randX || startX == randX - 1 || startX == randX + 1;
            if (board[randY][randX] == 0 && !(badRow && badColumn)) {
                board[randY][randX] = -1;
                minesLeft--;
            }
        }

        // For each mine on the board, increment the stored number for each adjacent empty square by 1
        // Once this has been done, the value of each empty square will reflect the number of adjacent mines
        for (int i = 0 ; i < height ; i++) {
            for (int j = 0 ; j < width ; j++) {
                if (board[i][j] == -1) {
                    for (int y = i - 1 ; y <= i + 1 ; y++) {
                        for (int x = j - 1 ; x <= j + 1 ; x++) {
                            if (y < height && y >= 0 && x < width && x >= 0 && board[y][x] != -1) {
                                board[y][x]++;
                            }
                        }
                    }
                }
            }
        }
    }

}

// This enumerated type represents all possible states for a square on the display
enum MinesweeperTile {
    COVERED,EMPTY,FLAG,MAYBE,MINE,DEATH,B1,B2,B3,B4,B5,B6,B7,B8
}