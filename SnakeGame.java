import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Simple Snake game demonstrating:
 * - Variables & Expressions
 * - Conditionals
 * - Loops
 * - Functions (methods)
 * - Classes
 * - Java Collections Framework (ArrayList)
 * - Additional requirement: File I/O (reads/writes a high score file)
 */
public class SnakeGame extends JPanel implements ActionListener, KeyListener {

    // ----- VARIABLES -----
    private static final int TILE_SIZE = 20;
    private static final int GRID_WIDTH = 25;
    private static final int GRID_HEIGHT = 25;
    private static final int WINDOW_WIDTH = TILE_SIZE * GRID_WIDTH;
    private static final int WINDOW_HEIGHT = TILE_SIZE * GRID_HEIGHT;
    private static final String HIGH_SCORE_FILE = "highscore.txt";

    private ArrayList<Point> snake;   // COLLECTIONS FRAMEWORK: ArrayList holds the snake's body segments
    private Point food;
    private char direction;           // 'U', 'D', 'L', 'R'
    private boolean running;
    private int score;
    private int highScore;
    private Timer timer;
    private Random random;

    public SnakeGame() {
        random = new Random();
        snake = new ArrayList<>();
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        highScore = readHighScore();  // FILE I/O: read saved high score on startup
        startGame();
    }

    // ----- FUNCTIONS (methods) -----

    private void startGame() {
        snake.clear();
        snake.add(new Point(GRID_WIDTH / 2, GRID_HEIGHT / 2));
        direction = 'R';
        score = 0;
        running = true;
        spawnFood();

        if (timer != null) timer.stop();
        timer = new Timer(120, this);
        timer.start();
    }

    private void spawnFood() {
        int x, y;
        boolean onSnake;
        do {
            x = random.nextInt(GRID_WIDTH);
            y = random.nextInt(GRID_HEIGHT);
            onSnake = false;
            for (Point p : snake) {               // LOOP: for-each loop
                if (p.x == x && p.y == y) {        // CONDITIONAL
                    onSnake = true;
                    break;
                }
            }
        } while (onSnake);                         // LOOP: do-while loop
        food = new Point(x, y);
    }

    private void move() {
        Point head = snake.get(0);
        int newX = head.x;
        int newY = head.y;

        // CONDITIONALS: pick new head position based on current direction
        if (direction == 'U') newY--;
        else if (direction == 'D') newY++;
        else if (direction == 'L') newX--;
        else if (direction == 'R') newX++;

        // EXPRESSIONS: modulo arithmetic wraps the snake around the edges
        newX = (newX + GRID_WIDTH) % GRID_WIDTH;
        newY = (newY + GRID_HEIGHT) % GRID_HEIGHT;

        Point newHead = new Point(newX, newY);
        snake.add(0, newHead);

        if (newHead.x == food.x && newHead.y == food.y) {
            score += 10;
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    private boolean checkCollision() {
        Point head = snake.get(0);
        for (int i = 1; i < snake.size(); i++) {   // LOOP: standard for loop
            if (head.equals(snake.get(i))) {       // CONDITIONAL
                return true;
            }
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.RED);
        g.fillRect(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        for (int i = 0; i < snake.size(); i++) {   // LOOP: draw every body segment
            Point p = snake.get(i);
            g.setColor(i == 0 ? Color.GREEN : Color.GREEN.darker());
            g.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        g.setColor(Color.WHITE);
        g.drawString("Score: " + score + "   High Score: " + highScore, 10, 15);

        if (!running) {
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("GAME OVER - Press R to Restart", 40, WINDOW_HEIGHT / 2);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            if (checkCollision()) {
                running = false;
                timer.stop();
                if (score > highScore) {          // CONDITIONAL
                    highScore = score;
                    writeHighScore(highScore);    // FILE I/O: save new high score
                }
            }
        }
        repaint();
    }

    // ----- FILE I/O (Additional Requirement) -----

    private int readHighScore() {
        File file = new File(HIGH_SCORE_FILE);
        if (!file.exists()) return 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null) return Integer.parseInt(line.trim());
        } catch (IOException | NumberFormatException e) {
            System.out.println("Could not read high score file: " + e.getMessage());
        }
        return 0;
    }

    private void writeHighScore(int newHighScore) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            writer.write(String.valueOf(newHighScore));
        } catch (IOException e) {
            System.out.println("Could not write high score file: " + e.getMessage());
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        // CONDITIONALS: prevent the snake from reversing directly into itself
        if (key == KeyEvent.VK_UP && direction != 'D') direction = 'U';
        else if (key == KeyEvent.VK_DOWN && direction != 'U') direction = 'D';
        else if (key == KeyEvent.VK_LEFT && direction != 'R') direction = 'L';
        else if (key == KeyEvent.VK_RIGHT && direction != 'L') direction = 'R';
        else if (key == KeyEvent.VK_R && !running) startGame();
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}

    // ----- CLASS: simple data holder for a grid coordinate -----
    private static class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Point)) return false;
            Point p = (Point) obj;
            return this.x == p.x && this.y == p.y;
        }
    }

    // ----- FUNCTION: program entry point -----
    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        SnakeGame game = new SnakeGame();
        frame.add(game);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}