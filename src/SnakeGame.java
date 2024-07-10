import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private class Tile {
        int x;
        int y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }  

    int boardWidth;
    int boardHeight;
    int tileSize = 25;
    
    //snake
    Tile snakeHead;
    ArrayList<Tile> snakeBody;
    ArrayList<Tile> walls;

    //food
    Tile food;
    Tile bonusFood;
    boolean bonusFoodVisible = false;
    boolean bonusFoodEaten = false;
    int foodEatenCount = 0;
    Timer awesomeMessageTimer;
    Random random;

    //game logic
    int velocityX;
    int velocityY;
    Timer gameLoop;

    boolean gameOver = false;

    SnakeGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);

        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<Tile>();
        walls = new ArrayList<Tile>();
        addWalls();

        food = new Tile(10, 10);
        bonusFood = new Tile(-1, -1); // Initially place bonus food outside the board
        random = new Random();
        placeFood();

        velocityX = 1;
        velocityY = 0;
        
        //game timer
        gameLoop = new Timer(100, this); //how long it takes to start timer, milliseconds gone between frames 
        gameLoop.start();
    }	
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        //Grid Lines
        for(int i = 0; i < boardWidth/tileSize; i++) {
            //(x1, y1, x2, y2)
            g.drawLine(i*tileSize, 0, i*tileSize, boardHeight);
            g.drawLine(0, i*tileSize, boardWidth, i*tileSize); 
        }

        //Food
        g.setColor(Color.red);
        g.fill3DRect(food.x*tileSize, food.y*tileSize, tileSize, tileSize, true);

        //Bonus Food
        if (bonusFoodVisible) {
            g.setColor(Color.blue);
            g.fill3DRect(bonusFood.x*tileSize, bonusFood.y*tileSize, tileSize, tileSize, true);
        }

        //Snake Head
        g.setColor(Color.green);
        g.fill3DRect(snakeHead.x*tileSize, snakeHead.y*tileSize, tileSize, tileSize, true);
        
        //Snake Body
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile snakePart = snakeBody.get(i);
            g.fill3DRect(snakePart.x*tileSize, snakePart.y*tileSize, tileSize, tileSize, true);
        }

        //Walls
        g.setColor(Color.gray);
        for (Tile wall : walls) {
            g.fill3DRect(wall.x*tileSize, wall.y*tileSize, tileSize, tileSize, true);
        }

        //Score
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        if (gameOver) {
            g.setColor(Color.red);
            g.drawString("Game Over: " + String.valueOf(snakeBody.size()), tileSize - 16, tileSize);
        } else {
            g.setColor(Color.white);
            g.drawString("Score: " + String.valueOf(snakeBody.size()), tileSize - 16, tileSize);
        }

        // Display "Awesome" if bonus food was eaten
        if (bonusFoodEaten) {
            g.setColor(Color.yellow);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Awesome!", boardWidth / 2 - 75, boardHeight / 2);
        }
    }

    public void placeFood(){
        food.x = random.nextInt(boardWidth/tileSize);
        food.y = random.nextInt(boardHeight/tileSize);
    }

    public void placeBonusFood(){
        bonusFood.x = random.nextInt(boardWidth/tileSize);
        bonusFood.y = random.nextInt(boardHeight/tileSize);
        bonusFoodVisible = true;
    }

    private void addWalls() {
        // Add some internal walls
        walls.add(new Tile(10, 10));
        walls.add(new Tile(10, 11));
        walls.add(new Tile(10, 12));
        walls.add(new Tile(10, 13));
        walls.add(new Tile(15, 5));
        walls.add(new Tile(15, 6));
        walls.add(new Tile(15, 7));
        walls.add(new Tile(15, 8));
        walls.add(new Tile(20, 15));
        walls.add(new Tile(21, 15));
        walls.add(new Tile(22, 15));
        walls.add(new Tile(23, 15));
    }

    public void move() {
        //eat food
        if (collision(snakeHead, food)) {
            snakeBody.add(new Tile(food.x, food.y));
            placeFood();
            foodEatenCount++;

            if (foodEatenCount % 5 == 0) {
                placeBonusFood();
            }
        }

        //eat bonus food
        if (bonusFoodVisible && collision(snakeHead, bonusFood)) {
            bonusFoodVisible = false;
            bonusFoodEaten = true;
            bonusFood.x = -1;
            bonusFood.y = -1;
            
            awesomeMessageTimer = new Timer(2000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    bonusFoodEaten = false;
                    awesomeMessageTimer.stop();
                }
            });
            awesomeMessageTimer.start();
        }

        //move snake body
        for (int i = snakeBody.size()-1; i >= 0; i--) {
            Tile snakePart = snakeBody.get(i);
            if (i == 0) { //right before the head
                snakePart.x = snakeHead.x;
                snakePart.y = snakeHead.y;
            } else {
                Tile prevSnakePart = snakeBody.get(i-1);
                snakePart.x = prevSnakePart.x;
                snakePart.y = prevSnakePart.y;
            }
        }

        //move snake head
        snakeHead.x += velocityX;
        snakeHead.y += velocityY;

        //game over conditions
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile snakePart = snakeBody.get(i);

            //collide with snake head
            if (collision(snakeHead, snakePart)) {
                gameOver = true;
            }
        }

        //collide with walls
        for (Tile wall : walls) {
            if (collision(snakeHead, wall)) {
                gameOver = true;
            }
        }

        //passed borders
        if (snakeHead.x < 0 || snakeHead.x >= boardWidth/tileSize ||
            snakeHead.y < 0 || snakeHead.y >= boardHeight/tileSize) {
            gameOver = true;
        }
    }

    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) { //called every x milliseconds by gameLoop timer
        if (!gameOver) {
            move();
            repaint();
        } else {
            gameLoop.stop();
        }
    }  

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP && velocityY != 1) {
            velocityX = 0;
            velocityY = -1;
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN && velocityY != -1) {
            velocityX = 0;
            velocityY = 1;
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT && velocityX != 1) {
            velocityX = -1;
            velocityY = 0;
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT && velocityX != -1) {
            velocityX = 1;
            velocityY = 0;
        }
    }

    //not needed
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
