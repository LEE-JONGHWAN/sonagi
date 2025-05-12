import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SonagiGame extends JFrame {
    private JTextField inputField;
    private JLabel scoreLabel;
    private JButton pauseButton;
    private GamePanel gamePanel;
    private Vector<String> wordList = new Vector<>();

    public SonagiGame() {
        setTitle("소나기 게임 - 떨어지는 단어 맞추기");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 900);
        setLocationRelativeTo(null);

        loadWords();

        inputField = new JTextField(15);
        inputField.addActionListener(new InputHandler());

        scoreLabel = new JLabel("점수: 0");

        pauseButton = new JButton("일시정지");
        pauseButton.addActionListener(new PauseHandler());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(scoreLabel);
        bottomPanel.add(inputField);
        bottomPanel.add(pauseButton);

        gamePanel = new GamePanel();

        add(gamePanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
        gamePanel.startGame();
    }

    private void loadWords() {
        String filePath = "C:/LJH/miniworkspaces/mini1/words.txt";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    wordList.add(line.trim());
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "words.txt 파일을 찾을 수 없습니다.", "에러", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private class GamePanel extends JPanel implements Runnable {
        private JLabel fallingLabel;
        private int yPosition = 0;
        private String currentWord = "";
        private int score = 0;
        private Thread thread;
        private volatile boolean isPaused = false;

        public GamePanel() {
            setLayout(null);
            fallingLabel = new JLabel("", SwingConstants.CENTER);
            fallingLabel.setFont(new Font("Serif", Font.BOLD, 28));
            FontMetrics fm = fallingLabel.getFontMetrics(fallingLabel.getFont());
            int textWidth = fm.stringWidth(currentWord);
            fallingLabel.setSize(textWidth + 20, 30); // 여유를 20px 줌
            fallingLabel.setForeground(Color.MAGENTA);
            add(fallingLabel);
        }

        public void startGame() {
            setRandomWord();
            thread = new Thread(this);
            thread.start();
        }

        private void setRandomWord() {
            if (wordList.isEmpty())
                return;
            int idx = (int) (Math.random() * wordList.size());
            currentWord = wordList.get(idx);
            fallingLabel.setText(currentWord);

            // 텍스트 너비 계산 후 라벨 크기 재조정
            FontMetrics fm = fallingLabel.getFontMetrics(fallingLabel.getFont());
            int textWidth = fm.stringWidth(currentWord);
            fallingLabel.setSize(textWidth + 20, 30); // 여유 공간 포함

            yPosition = 0;
            fallingLabel.setLocation((int) (Math.random() * (getWidth() - fallingLabel.getWidth())), yPosition);
        }

        public String getCurrentWord() {
            return currentWord;
        }

        public void nextWord() {
            setRandomWord();
        }

        public void togglePause() {
            isPaused = !isPaused;
        }

        public boolean isPaused() {
            return isPaused;
        }

        public void increaseScore() {
            score++;
            scoreLabel.setText("점수: " + score);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(300);

                    if (isPaused)
                        continue;

                    yPosition += 10;
                    fallingLabel.setLocation(fallingLabel.getX(), yPosition);

                    if (yPosition > getHeight() - 70) {
                        JOptionPane.showMessageDialog(SonagiGame.this, "시간초과! 게임 종료", "Game Over",
                                JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    private class InputHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (gamePanel.isPaused())
                return;

            String input = inputField.getText().trim();

            if (input.equalsIgnoreCase("그만")) {
                System.exit(0);
            }

            if (input.equals(gamePanel.getCurrentWord())) {
                gamePanel.increaseScore();
                gamePanel.nextWord();
            }

            inputField.setText("");
        }
    }

    private class PauseHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            gamePanel.togglePause();
            pauseButton.setText(gamePanel.isPaused() ? "계속하기" : "일시정지");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SonagiGame());
    }
}
