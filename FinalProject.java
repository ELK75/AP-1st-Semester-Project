import java.awt.*;
import javax.swing.*;
import java.util.*;

public class FinalProject extends JComponentWithEvents {
  // the baord is actually 6x7 but I made
  // it 8x9 in order to give it padding
  private int rows = 8;
  private int cols = 9;
  // the board of ints which correspond to colors
  // in the paintCell method
  private int[][] board = new int[rows][cols];
  // 1 = human player
  // -1 = AI / opponent player
  private int currentPiece = 1;
  // defines which column the piece
  // should be falling. When
  // columnClicked is 0 it means
  // no column has been selected
  private int columnClicked = -1;
  // defines what row the piece
  // is when falling
  private int drop = 1;
  private boolean gameOver = false;
  // isBoardChecked makes the program more efficent
  // since tiemrFired only checks if there is a winning
  // move once after a piece is placed
  private boolean isBoardChecked = true;
  private boolean isTie = false;
  // defines direction for the gameover animation
  // 1 is down and -1 is up
  private int direction = 1;
  // defines how the colored piece at the top moves
  private int shift = 0;
  // defines whether the game is a 2 player game
  // or against a computer
  private boolean playerGame = false;
  private boolean computerGame = false;
  private boolean instructions = false;
  // 0: user is playing against another player
  // 1: user chose easy difficulty
  // 2: user chose hard difficulty
  private int computerDifficulty = 0;
  // changes color of the connect 4 in th main
  // menu
  private Color introColor = Color.red;
  // used to avoid a row in which the computer
  // drops a piece which the user than stacks
  // upon to win
  // -1 means there is no row to avoid
  private int rowToAvoid = -1;
  
  // ----------------------------------
  // methods that define player actions
  // ----------------------------------
  
  public void playerChoseComputer(int x, int y) {
    int height = getHeight();
    int width = getWidth();
    if (y <= width / 2) computerDifficulty = 1;
    else computerDifficulty = 2;
    setTimerDelay(100);
  }
  
  public void playerHasNotChoseGameMode(int x, int y) {
    int height = getHeight();
    int width = getWidth();
    if (y <= height/2 && y >= height/4) 
      playerGame = true;
    else if (y <= (int) (height * (0.75)) && y >= height/2)
      computerGame = true;
    else if (y >= (int) (height * 0.75)) instructions = true;
      setTimerDelay(100);
  }
  
  private boolean isComputersTurn() {
    if (computerDifficulty > 0 && currentPiece == -1)
      return true;
    return false;
  }
  
  public void mousePressed(int x, int y) {
    if (instructions) {
      instructions = false;
    }
    else if (computerGame) {
      playerChoseComputer(x, y);
    }
    else if (!playerGame && !computerGame) {
      playerHasNotChoseGameMode(x, y);
    } 
    // needed so player cannot click
    // twice before the piece falls
    if (drop == 1 && (playerGame || computerDifficulty > 0)
       && !isComputersTurn()) {
      int height = getHeight();
      int width = getWidth();
      // number of areas user can clicked
      int numberOfClickableAreas = cols-2;
      // The bottomOfPressArea is the bottom of the
      // gray areas.
      int bottomOfPressArea = height / rows;
      // defines where the right most border
      // of each light gray area lies.
      int[] rightSideOfPressArea = new int[numberOfClickableAreas];
      for (int col = 0; col < rightSideOfPressArea.length; col++) {
        // col + 2 is used instead of col + 1 to compensate for the
        // padding around the baord
        rightSideOfPressArea[col] = (col + 2) * width / cols;
      }
      int leftBorder = width / rows;
      int rightBorder = rightSideOfPressArea[rightSideOfPressArea.length-1];
      
      if (x >= leftBorder && x <= rightBorder && 
          y <= bottomOfPressArea) {
        for (int col = 0; col < numberOfClickableAreas; col++) {
          // the isBoardChecked is in order to prevent two pieces
          // being placed on the same turn
          if (rightSideOfPressArea[col] > x && isBoardChecked) {
            // columnClicked = col+1 in order to compensate for 
            // the padding
            if (!isColumnFull(col+1)) { 
              columnClicked = col +1;
              // places piece at top to where the user clicked
              shift = columnClicked -4;
              drawBorder();
            }
            else columnClicked = 0;
            break;
          }
        }
      } 
    }
  }
  
  public void restart() {
    clearBoard();
    currentPiece = 1;
    drop = 1;
    setTimerDelay(100);
    isTie = false;
    isBoardChecked = true;
    shift = 0;
  }
  
  public void mainMenu() {
    playerGame = false;
    computerGame = false;
    computerDifficulty = 0;
  }
  
  public void keyPressed(char key) {
    if (instructions) instructions = false;
    if (key == 'r') { 
      restart();
    }
    // goes back to main menu
    if (key == 'm') {
      restart();
      mainMenu();
    }
    if (gameOver) {
      restart();
    } else if (playerGame || computerDifficulty > 0) {
      if (drop == 1 && isBoardChecked && !isComputersTurn()) {
        if (currentPiece == 1) {
          if (key == 'd' && shift < 3) shift++;
          if (key == 'a' && shift > -3) shift--;
          if (key == 's' || key == SPACE)
            if (!isColumnFull(4 + shift)) columnClicked = 4 + shift;
        } else if (currentPiece == -1) {
          if (key == RIGHT && shift < 3) shift++;
          if (key == LEFT && shift > -3) shift--;
          if (key == DOWN || key == SPACE)
            if (!isColumnFull(4 + shift)) columnClicked = 4 + shift;
        }
      }
    }
    gameOver = false;
    drawBorder();
  }
  
  // -------------------------------
  // methods that test winning moves
  // modified from word search
  // -------------------------------
  
  // method to define peice we branch out of 
  // to check if there are any winnners
  public boolean isWinningMove(int pieceToLookFor) {
    for (int row = 1; row < rows -1; row++) {
      for (int col = 1; col < cols -1; col++) {
        if (board[row][col] == pieceToLookFor) {
          if (isWinningMove(row, col, pieceToLookFor)) return true;
        }
      }
    }
    return false;
  }
  
  // defines the direction to break out
  public boolean isWinningMove(int startRow, int startCol,
                               int pieceToLookFor) {
    for (int dRow = -1; dRow <= 1; dRow++) {
      for (int dCol = -1; dCol <= 1; dCol++) {
        if (!(dRow == 0 && dCol == 0) &&
            (isWinningMove(startRow, startCol, dRow, dCol, 
                           pieceToLookFor)))
          return true;
      }
    }
    return false;
  }
  
  // checks to see if there are 4 in a row
  public boolean isWinningMove(int startRow, int startCol,
                               int dRow, int dCol, int pieceToLookFor) {
    int row = startRow;
    int col = startCol;
    for (int i = 0; i < 4; i++) {
      if ((row < 1) || (row >= rows -1) || (col < 1) || (col >= cols -1))
        // we're off the board, so we did not match
        return false;
      if (board[row][col] != pieceToLookFor)
        // we're on the board, but we don't match
        return false;
      row += dRow;
      col += dCol;
    }
    return true;
  }
  
  // ---------------------
  // methods for AI pieces
  // ---------------------
  
  Random random = new Random();
  
  public void dropEasyPiece() {
    columnClicked = random.nextInt(7) + 1;
    while (true) {
      if (isColumnFull(columnClicked))
        columnClicked = random.nextInt(7) + 1;
      else
        break;
    }
    shift = columnClicked -4;
    drawBorder();
  }
  
  // returns the lowest row not occupied by
  // a piece
  public int nextFreeRowGivenCol(int col) {
    for (int row = 1; row < rows -1; row++) {
      if (board[row][col] != 0)  {
        return row-1;
      }
    }
    // returns bottom row
    return rows-2;
  }
  
  public void triesToEitherWinOrBlock(boolean isTryingToWin) {
    int pieceToDrop;
    if (isTryingToWin) pieceToDrop = -1;
    else pieceToDrop = 1;
    
    for (int col = 1; col < cols -1; col++) {
      int lowestUnoccupiedRow = nextFreeRowGivenCol(col);
      board[lowestUnoccupiedRow][col] = pieceToDrop;
      if (isWinningMove(pieceToDrop)) {
        board[lowestUnoccupiedRow][col] = 0;
        columnClicked = col;
        break;
      }
      board[lowestUnoccupiedRow][col] = 0;
    }
  }
  
  public int numberOfPieces(int col) {
    int numberOfPieces = 0;
    for (int row = 1; row < rows-1; row++) {
      if (board[row][col] != 0) numberOfPieces++;
    }
    return numberOfPieces;
  }
  
  public int highestCol() {
    int biggest = 0;
    for (int col = 1; col < cols-1; col++) {
      if (numberOfPieces(col) > biggest) {
        biggest = numberOfPieces(col);
      }
    }
    return biggest;
  }
  
  // the purpose of this function is to aim for the
  // center of the board except where it would make a 
  // tower higher than the board already has
  public void aimLower() {
    for (int colShift = 0; colShift <= 2; colShift++) {
      if (numberOfPieces(4-colShift) +1 <= highestCol() 
          && !isColumnFull(4-colShift) && !colToAvoid(4-colShift)) {
        columnClicked = 4-colShift;
        break;
      }
      else if (numberOfPieces(4+colShift) +1 <= highestCol() 
          && !isColumnFull(4+colShift) && !colToAvoid(4+colShift)) {
        columnClicked = 4+colShift;
        break;
      }
    }
  }
  
  // in the beginning it chooses to always choose
  // the center squares at the bottom if they are available
  public void aimForTheCenter() {
    for (int colShift = 0; colShift <= 2; colShift++) {
      if (board[6][4] == 1) {
        if (board[6][5] == 0 && !colToAvoid(5)) {
          columnClicked = 5;
          break;
        }
      }
      if (board[6][4-colShift] == 0 && !colToAvoid(4-colShift)) {
        columnClicked = 4-colShift;
        break;
      }
      else if (board[6][4+colShift] == 0 && !colToAvoid(4+colShift)) {
        columnClicked = 4+colShift;
        break;
      }
    }
  }
  
  public void chooseRandomCol() {
    if (columnClicked == 0) {
      columnClicked = random.nextInt(7) + 1;
      while (true && !colToAvoid(columnClicked)) {
        if (isColumnFull(columnClicked))
          columnClicked = random.nextInt(7) + 1;
        else
          break;
      }
    }
  }
  
  public boolean colToAvoid(int col) {
    int lowestUnoccupiedRow = nextFreeRowGivenCol(col);
    if (lowestUnoccupiedRow >= 2) {
      // in order to see if diagonal attacks can happen above
      lowestUnoccupiedRow--;
      board[lowestUnoccupiedRow][col] = 1;
      if (isWinningMove(1)) {
        board[lowestUnoccupiedRow][col] = 0;
        rowToAvoid = col;
        return true;
      }
      board[lowestUnoccupiedRow][col] = 0;
      return false;
    } else return false;
  }
  
  public void dropHardPiece() {
    if (!gameOver) {
      // first tries to win
      triesToEitherWinOrBlock(true);
      // then tries to block a winning move
      if (columnClicked == 0) {
        triesToEitherWinOrBlock(false);
      } if (columnClicked == 0) {
        aimForTheCenter();
      } if (columnClicked == 0) {
        aimLower();
      } if (columnClicked == 0) {
        chooseRandomCol();
      }
      shift = columnClicked -4;
      drawBorder();
    }
  }
  
  // -------------------------
  // methods for falling piece
  // -------------------------
  
  public boolean isColumnFull(int col) {
    return (board[1][col] != 0);
  }
  
  public boolean isTie() {
    for (int col = 1; col < cols -1; col++) {
      if (board[1][col] == 0) return false;
    }
    return true;
  }
  
  public boolean isNextMoveLegal() {
    if (drop >= rows-2 || 
        board[drop+1][columnClicked] != 0) return false;
    else return true;
  }
  
  public void eraseTrail() {
    board[drop-1][columnClicked] = 0;
  }
  
  public void dropPiece() {
    board[drop][columnClicked] = currentPiece;
    if (drop >= 2) eraseTrail();
    if (isNextMoveLegal()) drop++;
    else {
      drop = 1;
      columnClicked = 0;
      isBoardChecked = false;
    }
  }
  
  public void checkBoardAndChangePiece() {
    // places colored block at the center
    if (!isBoardChecked) {
      if (isTie()) {
        isTie = true;
        gameOver = true;
      }
      else if (isWinningMove(currentPiece)) gameOver = true;
      else { 
        currentPiece *= -1;
        //
        //shift = 0;
        drawBorder();
      }
    }
    isBoardChecked = true;
    if (gameOver) { 
      drop = 0;
      direction = 1;
    }
  }
  
  public void gameOverAnimation() {
    setTimerDelay(800);
    drawBorder();
    drop += direction;
    if (drop == rows -2 || drop == 0) direction *= -1;
  }
  
  public void drawMainMenuAnimation() {
    setTimerDelay(800);
    introColor = (introColor == Color.yellow) ? Color.red :
      Color.yellow;
  }
  
  public void timerFired() {
    if (!gameOver) {
      // when a column is chosen
      if (columnClicked > 0) {
        dropPiece();
      }
      // after piece has fallen
      else {
        checkBoardAndChangePiece();
        if (currentPiece == -1 && isComputersTurn()) {
          if (computerDifficulty == 1) {
            dropEasyPiece();
          } else if (computerDifficulty == 2) {
            dropHardPiece();
          }
        }
      }
    // draws falling animation for when the game is over
    // might cause bug.. if it does says else if (gameOver)
    } else {
      gameOverAnimation();
    }
    
    // draws flashing in the main menu
    if (!playerGame && !computerGame) {
      drawMainMenuAnimation();
    }
  }
  
  // ----------------------------
  // methods for making the board
  // and erasing the board
  // ----------------------------
  
  public void clearBoard() {
    for (int row = 1; row < rows -1; row++) {
      for (int col = 1; col < cols -1; col++) {
        board[row][col] = 0;
      }
    }
  }
  
  public void drawBorder() {
    // fills left and right border
    for (int row = 0; row < rows; row++) {
      board[row][0] = 4;
      board[row][cols-1] = 4;
    }
    for (int col = 0; col < cols; col++) {
      board[rows-1][col] = 2;
    }
    // fills the light gray at the top as well as the bottom
    // colors to indicate whose turn it is
    for (int grayCount = 1; grayCount < cols -1; grayCount++) {
      board[0][grayCount] = 3;
      if (shift <= 3 && shift >= -3) 
        board[0][4 + shift] = 5;
    }
    // draws side animation
    if (gameOver) {
      if (isTie) currentPiece *= -1;
      
      board[drop][0] = 5;
      board[drop][cols-1] = 5;
    }
  }
  
  public void paintCell(Graphics2D page, int row, int col) {
    int height = getHeight();
    int width = getWidth();
    int left = col * width / cols;
    int right = (col + 1) * width / cols; 
    int top  = row * height / rows;
    int bottom = (row + 1) * height / rows;
    
    // draws board and animations after game is over    
    Color currentColor = Color.white;
    if (board[row][col] == 2) currentColor = Color.black;
    else if (board[row][col] == 3) currentColor = Color.lightGray;
    // 4 draws sides of the board to indicate turn
    else if (board[row][col] == 4) {
      if (!gameOver) {
        if (currentPiece == 1) currentColor = Color.red;
        else currentColor = Color.yellow;
      } 
      else currentColor = Color.blue;
    }
    // draws the falling animation
    else if (board[row][col] == 5) {
      if (currentPiece == 1) currentColor = Color.red;
      else if (currentPiece == -1) currentColor = Color.yellow;
    }
    page.setColor(currentColor);
    page.fillRect(left, top, right-left, bottom-top);
    page.setColor(Color.darkGray);
    page.drawRect(left, top, right-left, bottom-top);
    
    // draws pieces
    if (board[row][col] == 1) currentColor = Color.red;
    else if (board[row][col] == -1) currentColor = Color.yellow;
    page.setColor(currentColor);
    page.fillOval(left, top, right-left, bottom-top);
    
    // draws text after the game is over
    if (gameOver) {  
      page.setColor(Color.blue);
      page.setFont(new Font("Arial", Font.BOLD, 25));
      page.drawString("GAME OVER", width/2 - 85, height/2);
      
      if (isTie) page.drawString("Tie Game", width/2 -65, height/2 +50);
      else if (currentPiece == 1) 
        page.drawString("Red Wins", width/2 -65, height/2 +50);
      else
        page.drawString("Yellow Wins", width/2 -78, height/2 +50);
      
      page.setFont(new Font("Arial", Font.BOLD, 20));
      page.drawString("Press Any Key to Continue", width/2 -150,
                      height/2 +100);
    }
  }
  
  public void drawIntroScreen(Graphics2D page) {
    int height = getHeight();
    int width = getWidth();
    
    page.setColor(Color.black);
    page.drawRect(-1, 0, width +1, height/4);
    page.setFont(new Font("Arial", Font.BOLD, 40));
    page.setColor(introColor);
    page.drawString("CONNECT 4", width/2 - 145,
                    height/4 - height/8);
    
    page.setColor(Color.black);
    page.drawRect(-1, height/4, width +1, height/2 - height/4);
    page.setFont(new Font("Arial", Font.BOLD, 20));
    page.drawString("PRESS HERE TO PLAY AGAINST A PLAYER",
                    width/2 - 240, height/4 + height/8);
    
    page.drawRect(-1, height/2, width +1, 
                  (int) (height * (0.75))- height/2);
    page.setColor(Color.lightGray);
    page.setColor(Color.black);
    page.setFont(new Font("Arial", Font.BOLD, 20));
    page.drawString("PRESS HERE TO PLAY AGAINST A COMPUTER", 
                    width/2 - 250, height/2 + height/8);
    
    page.drawRect(-1, (int) (height * (0.75)), width +1, 
                  height);
    page.setColor(Color.lightGray);
    page.setColor(Color.black);
    page.setFont(new Font("Arial", Font.BOLD, 20));
    page.drawString("INSTRUCTIONS", width/2 - 90, 
                    (int) (height *(0.75) + height/8));
    
  }
  
  public void drawComputerOptions(Graphics2D page) {
    int height = getHeight();
    int width = getWidth();
    
    page.setColor(Color.black);
    page.setFont(new Font("Arial", Font.BOLD, 40));
    page.drawString("EASY", width/2 -70, 
                    height/2 - height/4);
    
    page.setFont(new Font("Arial", Font.BOLD, 15));
    page.drawString("Chooses Randomly", width/2 -85,
                    height/2 - height/4 +50);
    
    // draws line in center... -1 and width +1 are used
    // in order to hide the two lines on the side of the
    // triangle
    page.drawRect(-1, height/2 +2, width +1, height/2 -2);
    page.setFont(new Font("Arial", Font.BOLD, 40));
    page.drawString("HARD", 
                    width/2 - 70, height/2 + height/4);
    
    page.setFont(new Font("Arial", Font.BOLD, 15));
    page.drawString("Places Winning Moves", width/2 -95,
                    height/2 + height/4 +50);
    
    page.drawString("Able to Block Winning Moves", width/2 -120,
                    height/2 + height/4 +75);
  }
  
  public void drawInstructions(Graphics2D page) {
    int height = getHeight();
    int width = getWidth();
    
    page.setColor(Color.black);
    page.setFont(new Font("Arial", Font.BOLD, 30));
    page.drawString("INSTRUCTIONS:", width/2 -130, 
                    40);
    
    page.setColor(Color.red);
    page.fillOval(5, height/8 + 30, 50, 50);
    page.setColor(Color.yellow);
    page.fillOval(60, height/8 + 30, 50, 50);
    
    page.setColor(Color.black);
    
    page.setFont(new Font("Arial", Font.BOLD, 15));
    page.drawString("Try to Get Four in a Row to Win the Game", 5,
                    height/8 + 110);
    
    page.drawString("Player One Uses A and D to Move and S or SPACE to Drop", 5,
                    height/8 + 140); 
    
    page.drawString("The Options Above are not Available When Playing a Computer", 5,
                    height/8 + 170); 
    
    page.drawString("Player Two Uses LEFT and RIGHT to Move With DOWN or SPACE to Drop.", 5,
                    height/8 + 200);
    
    page.drawString("Pressing R Restarts the Game while M Goes Back to the Menu",
                    5, height/8 + 230);
    
    page.setColor(Color.lightGray);
    page.fillRect(5, height/8 + 260, 80, 80);
    
    page.setColor(Color.black);
    page.drawString("Clicking on the Gray Squares at the Top Will Also Drop a Piece",
                    5, height/8 + 370);
    
    
    page.drawString("Press Any Key to go Back to the Menu and Have Fun!",
                    5, height/8 + 400);
  }
  
  // got method from from snake
  public void paint(Graphics2D page) {
    if (!playerGame && !computerGame && !instructions)
      drawIntroScreen(page);
    // means user clicked to play against a computer but
    // has yet to choose difficulty
    else if (computerGame && computerDifficulty == 0)
      drawComputerOptions(page);
    else if (instructions) 
      drawInstructions(page);
    else {
      for (int row = 0; row < rows; row++) {
        for (int col = 0; col < cols; col++) {
          paintCell(page, row, col);
        }
      }
    }
  }
  
  public void start() {
    drawBorder();
    setTimerDelay(100);
  }
  
  public static void main(String[] args)  { launch(700, 600); }
  
}
