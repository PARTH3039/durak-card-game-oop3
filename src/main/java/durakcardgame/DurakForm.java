package durakcardgame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class DurakForm extends JFrame {
    private final DurakGame game = new DurakGame();
    private final GameHistoryDatabase historyDatabase = new GameHistoryDatabase();
    private DurakAI ai;

    private final JPanel aiHandPanel;
    private final JPanel tableCardsPanel;
    private final JPanel playerHandPanel;
    private final JPanel deckPanel;

    private final JButton playerPickButton;
    private final JButton playerPassButton;
    private final JButton historyButton;
    private final JButton playButton;

    private final JLabel statusLabel;
    private final JLabel deckCountLabel;
    private final JLabel aiStatusLabel;
    private final JLabel playerStatusLabel;

    private final JTextField playerNameTextField;
    private final JComboBox<String> difficultyComboBox;

    private int selectedCardIndex = -1;
    private boolean gameSaved = false;

    public DurakForm() {
        super("Durak Card Game");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 820));
        setResizable(false);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.setBackground(new Color(21, 109, 65));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 12));
        topBar.setBackground(new Color(18, 92, 58));
        topBar.setBorder(new EmptyBorder(4, 8, 4, 8));

        playButton = new JButton("Start Game");
        styleButton(playButton);
        playButton.addActionListener(e -> {
            reset();
            playButton.setEnabled(false);
        });
        topBar.add(playButton);

        topBar.add(createTopLabel("Player:"));

        String playerName = "";
        try {
            playerName = Files.readString(Path.of("player.txt"));
        } catch (IOException ignored) {
        }

        playerNameTextField = new JTextField(playerName, 12);
        playerNameTextField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        topBar.add(playerNameTextField);

        topBar.add(createTopLabel("AI Level:"));

        difficultyComboBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        difficultyComboBox.setFont(new Font("SansSerif", Font.BOLD, 14));
        topBar.add(difficultyComboBox);

        historyButton = new JButton("History");
        styleButton(historyButton);
        historyButton.addActionListener(e -> showHistory());
        topBar.add(historyButton);

        contentPane.add(topBar, BorderLayout.NORTH);

        JPanel tableRoot = new JPanel(new BorderLayout());
        tableRoot.setBackground(new Color(21, 109, 65));
        tableRoot.setBorder(new EmptyBorder(16, 20, 12, 20));
        contentPane.add(tableRoot, BorderLayout.CENTER);

        JPanel topArea = new JPanel(new BorderLayout());
        topArea.setOpaque(false);

        aiStatusLabel = createInfoLabel("AI");
        aiStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        aiStatusLabel.setBorder(new EmptyBorder(0, 0, 6, 0));
        topArea.add(aiStatusLabel, BorderLayout.NORTH);

        aiHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, -38, 8));
        aiHandPanel.setOpaque(false);
        topArea.add(aiHandPanel, BorderLayout.CENTER);

        tableRoot.add(topArea, BorderLayout.NORTH);

        JPanel middleArea = new JPanel(new BorderLayout(20, 0));
        middleArea.setOpaque(false);
        middleArea.setBorder(new EmptyBorder(12, 0, 12, 0));

        deckPanel = new JPanel();
        deckPanel.setOpaque(false);
        deckPanel.setLayout(new BoxLayout(deckPanel, BoxLayout.Y_AXIS));
        middleArea.add(deckPanel, BorderLayout.WEST);

        JPanel centerBattleArea = new JPanel();
        centerBattleArea.setOpaque(false);
        centerBattleArea.setLayout(new BoxLayout(centerBattleArea, BoxLayout.Y_AXIS));

        statusLabel = createInfoLabel("Press Start Game to begin.");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        tableCardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 12));
        tableCardsPanel.setOpaque(false);

        centerBattleArea.add(Box.createVerticalGlue());
        centerBattleArea.add(statusLabel);
        centerBattleArea.add(Box.createVerticalStrut(18));
        centerBattleArea.add(tableCardsPanel);
        centerBattleArea.add(Box.createVerticalGlue());

        middleArea.add(centerBattleArea, BorderLayout.CENTER);

        JPanel rightInfo = new JPanel();
        rightInfo.setOpaque(false);
        rightInfo.setLayout(new BoxLayout(rightInfo, BoxLayout.Y_AXIS));

        deckCountLabel = createInfoLabel("Cards remaining: 0");
        deckCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightInfo.add(deckCountLabel);
        rightInfo.add(Box.createVerticalStrut(10));

        playerStatusLabel = createInfoLabel("Your turn");
        playerStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightInfo.add(playerStatusLabel);

        middleArea.add(rightInfo, BorderLayout.EAST);

        tableRoot.add(middleArea, BorderLayout.CENTER);

        JPanel bottomArea = new JPanel(new BorderLayout());
        bottomArea.setOpaque(false);

        JLabel handTitle = createInfoLabel("Your Hand");
        handTitle.setHorizontalAlignment(SwingConstants.CENTER);
        bottomArea.add(handTitle, BorderLayout.NORTH);

        playerHandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, -28, 10));
        playerHandPanel.setOpaque(false);
        bottomArea.add(playerHandPanel, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 6));
        actionPanel.setOpaque(false);

        playerPickButton = new JButton("Play Selected Card");
        styleButton(playerPickButton);
        playerPickButton.setEnabled(false);
        playerPickButton.addActionListener(e -> {
            if (selectedCardIndex == -1) {
                return;
            }
            game.step(selectedCardIndex);
            runAITurns();
            updateGameUI();
        });

        playerPassButton = new JButton("Pass / Take");
        styleButton(playerPassButton);
        playerPassButton.setEnabled(false);
        playerPassButton.addActionListener(e -> {
            game.step(-1);
            runAITurns();
            updateGameUI();
        });

        actionPanel.add(playerPickButton);
        actionPanel.add(playerPassButton);
        bottomArea.add(actionPanel, BorderLayout.SOUTH);

        tableRoot.add(bottomArea, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        renderStaticState();
    }

    @Override
    public void dispose() {
        try {
            Files.writeString(Path.of("player.txt"), playerNameTextField.getText());
        } catch (IOException ignored) {
        }
        super.dispose();
    }

    private void reset() {
        game.reset();
        ai = new DurakAI(((String) difficultyComboBox.getSelectedItem()).toLowerCase());
        selectedCardIndex = -1;
        gameSaved = false;

        while (game.getWinner() == -1 && game.getActivePlayer() != 0) {
            game.step(ai.chooseAction(game, game.getActivePlayerValidCardIndices()));
        }

        updateGameUI();
    }

    private void runAITurns() {
        while (game.getWinner() == -1 && game.getActivePlayer() != 0) {
            game.step(ai.chooseAction(game, game.getActivePlayerValidCardIndices()));
        }
    }

    private void updateGameUI() {
        selectedCardIndex = -1;

        rebuildAIHand();
        rebuildDeckPanel();
        rebuildTableCards();
        rebuildPlayerHand();

        deckCountLabel.setText("Cards remaining: " + game.getDeckSize());

        int winner = game.getWinner();
        if (winner != -1) {
            statusLabel.setText(winner == 0 ? "You win!" : "AI wins!");
            aiStatusLabel.setText("Game Over");
            playerStatusLabel.setText("Game finished");
            playerPickButton.setEnabled(false);
            playerPassButton.setEnabled(false);
            playButton.setEnabled(true);

            if (!gameSaved) {
                historyDatabase.saveGame(
                        playerNameTextField.getText(),
                        (String) difficultyComboBox.getSelectedItem(),
                        winner
                );
                gameSaved = true;
            }

            JOptionPane.showMessageDialog(
                    this,
                    winner == 0 ? "You win!" : "AI wins!",
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE
            );
            revalidate();
            repaint();
            return;
        }

        if (game.getActivePlayer() == 0) {
            playerStatusLabel.setText("Your turn: " + game.getState());
            aiStatusLabel.setText("AI is waiting");
        } else {
            playerStatusLabel.setText("Please wait");
            aiStatusLabel.setText("AI turn: " + game.getState());
        }

        statusLabel.setText(buildStatusText());

        boolean canPass = game.getActivePlayer() == 0 &&
                Arrays.stream(game.getActivePlayerValidCardIndices()).anyMatch(i -> i == -1);
        playerPassButton.setEnabled(canPass);

        revalidate();
        repaint();
    }

    private String buildStatusText() {
        return switch (game.getState()) {
            case ATTACK -> game.getActivePlayer() == 0 ? "Attack with a valid card." : "AI is attacking.";
            case DEFEND -> game.getActivePlayer() == 0 ? "Defend or take the cards." : "AI is defending.";
            case DISCARD -> game.getActivePlayer() == 0 ? "Add more cards or pass." : "AI is adding cards.";
        };
    }

    private void renderStaticState() {
        aiHandPanel.removeAll();
        tableCardsPanel.removeAll();
        playerHandPanel.removeAll();
        deckPanel.removeAll();

        aiStatusLabel.setText("AI");
        playerStatusLabel.setText("Your turn");
        deckCountLabel.setText("Cards remaining: 0");
        statusLabel.setText("Press Start Game to begin.");

        revalidate();
        repaint();
    }

    private void rebuildAIHand() {
        aiHandPanel.removeAll();

        int aiCards = game.getPlayerHand(1).size();
        ImageIcon back = getBackImage();

        for (int i = 0; i < aiCards; i++) {
            JLabel label = new JLabel();
            label.setPreferredSize(new Dimension(90, 124));

            if (back != null) {
                label.setIcon(back);
            } else {
                label.setOpaque(true);
                label.setBackground(Color.WHITE);
                label.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            }

            aiHandPanel.add(label);
        }
    }

    private void rebuildDeckPanel() {
        deckPanel.removeAll();

        JLabel title = createInfoLabel("Deck");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        deckPanel.add(title);
        deckPanel.add(Box.createVerticalStrut(8));

        JLayeredPane layeredDeck = new JLayeredPane();
        layeredDeck.setPreferredSize(new Dimension(150, 190));
        layeredDeck.setMaximumSize(new Dimension(150, 190));
        layeredDeck.setMinimumSize(new Dimension(150, 190));

        ImageIcon back = getBackImage();
        int visibleDeckCards = Math.min(4, Math.max(1, game.getDeckSize() / 6 + 1));

        for (int i = 0; i < visibleDeckCards; i++) {
            JLabel deckCard = new JLabel();
            deckCard.setBounds(18 + i * 3, 28 + i * 3, 90, 124);

            if (back != null) {
                deckCard.setIcon(back);
            } else {
                deckCard.setOpaque(true);
                deckCard.setBackground(Color.WHITE);
                deckCard.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            }

            layeredDeck.add(deckCard, Integer.valueOf(i));
        }

        Card trump = game.getTrumpCard();
        if (trump != null) {
            JLabel trumpCard = new JLabel();
            trumpCard.setBounds(50, 10, 90, 124);

            ImageIcon trumpIcon = getCardImage(trump);
            if (trumpIcon != null) {
                trumpCard.setIcon(trumpIcon);
            } else {
                trumpCard.setText(trump.toString());
            }

            layeredDeck.add(trumpCard, Integer.valueOf(10));
        }

        deckPanel.add(layeredDeck);
        deckPanel.add(Box.createVerticalStrut(8));

        JLabel trumpText = createSmallInfoLabel("Trump: " + game.getTrumpCard());
        trumpText.setAlignmentX(Component.CENTER_ALIGNMENT);
        deckPanel.add(trumpText);
    }

    private void rebuildTableCards() {
        tableCardsPanel.removeAll();

        List<Card> pile = game.getPile();
        for (int i = 0; i < pile.size(); i += 2) {
            JPanel pairPanel = new JPanel(null);
            pairPanel.setOpaque(false);
            pairPanel.setPreferredSize(new Dimension(130, 145));

            Card attack = pile.get(i);
            JLabel attackLabel = createCardLabel(attack);
            attackLabel.setBounds(0, 18, 90, 124);
            pairPanel.add(attackLabel);

            if (i + 1 < pile.size()) {
                Card defend = pile.get(i + 1);
                JLabel defendLabel = createCardLabel(defend);
                defendLabel.setBounds(38, 0, 90, 124);
                pairPanel.add(defendLabel);
            }

            tableCardsPanel.add(pairPanel);
        }
    }

    private void rebuildPlayerHand() {
        playerHandPanel.removeAll();
        playerPickButton.setEnabled(false);

        List<Card> hand = game.getPlayerHand(0);
        int[] valid = game.getActivePlayer() == 0 ? game.getActivePlayerValidCardIndices() : new int[0];

        for (int i = 0; i < hand.size(); i++) {
            JButton btn = createCardButton(hand.get(i));
            final int index = i;
            boolean isValid = isValidIndex(valid, index);

            if (isValid) {
                btn.setBorder(BorderFactory.createLineBorder(new Color(245, 206, 66), 3));
            } else {
                btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            }

            btn.addActionListener(e -> {
                if (game.getActivePlayer() != 0) {
                    return;
                }

                selectedCardIndex = index;
                highlightSelectedCard(index, valid);
                playerPickButton.setEnabled(isValid);
            });

            playerHandPanel.add(btn);
        }
    }

    private void highlightSelectedCard(int selectedIndex, int[] valid) {
        Component[] components = playerHandPanel.getComponents();

        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof JButton button) {
                boolean isValid = isValidIndex(valid, i);

                if (i == selectedIndex) {
                    button.setBorder(BorderFactory.createLineBorder(Color.RED, 4));
                } else if (isValid) {
                    button.setBorder(BorderFactory.createLineBorder(new Color(245, 206, 66), 3));
                } else {
                    button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
                }
            }
        }
    }

    private boolean isValidIndex(int[] valid, int index) {
        for (int value : valid) {
            if (value == index) {
                return true;
            }
        }
        return false;
    }

    private JLabel createCardLabel(Card card) {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(90, 124));

        ImageIcon icon = getCardImage(card);
        if (icon != null) {
            label.setIcon(icon);
        } else {
            label.setText(card.toString());
            label.setOpaque(true);
            label.setBackground(Color.WHITE);
            label.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        }

        return label;
    }

    private JButton createCardButton(Card card) {
        ImageIcon icon = getCardImage(card);
        JButton button = icon != null ? new JButton(icon) : new JButton(card.toString());

        button.setPreferredSize(new Dimension(90, 124));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }

    private ImageIcon getCardImage(Card card) {
        String suit = switch (card.suite()) {
            case 0 -> "H";
            case 1 -> "D";
            case 2 -> "S";
            case 3 -> "C";
            default -> "";
        };

        String rank = switch (card.rank()) {
            case 11 -> "J";
            case 12 -> "Q";
            case 13 -> "K";
            case 14 -> "A";
            default -> String.valueOf(card.rank());
        };

        String path = "/cards/" + rank + suit + ".png";
        URL url = getClass().getResource(path);

        if (url == null) {
            System.out.println("Missing image: " + path);
            return null;
        }

        ImageIcon icon = new ImageIcon(url);
        Image scaled = icon.getImage().getScaledInstance(90, 124, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private ImageIcon getBackImage() {
        URL url = getClass().getResource("/cards/back.png");

        if (url == null) {
            System.out.println("Missing image: /cards/back.png");
            return null;
        }

        ImageIcon icon = new ImageIcon(url);
        Image scaled = icon.getImage().getScaledInstance(90, 124, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private JLabel createTopLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 15));
        return label;
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        return label;
    }

    private JLabel createSmallInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return label;
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(new Color(239, 196, 87));
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
    }

    private void showHistory() {
        List<String> history = historyDatabase.getRecentGames(10);

        JOptionPane.showMessageDialog(
                this,
                history.isEmpty() ? "No history yet." : String.join("\n", history),
                "Recent Games",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new DurakForm().setVisible(true));
    }
}