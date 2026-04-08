package durakcardgame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class DurakForm extends JFrame {
    private final DurakGame game = new DurakGame();
    private DurakAI ai;

    private final JList<Card> playerHandList;
    private final DefaultListModel<Card> playerHandModel;
    private final JButton playerPickButton;
    private final JButton playerPassButton;
    private final JLabel statusLabel;
    private final JLabel deckLabel;
    private final JLabel trumpLabel;
    private final JLabel pileLabel;
    private final JButton playButton;
    private final JTextField playerNameTextField;

    public DurakForm() {
        super("Durak Card Game");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setMinimumSize(new Dimension(640, 480));

        JPanel topPanel = new JPanel();
        Container contentPane = getContentPane();
        contentPane.add(topPanel, BorderLayout.NORTH);

        playButton = new JButton("Play!");
        playButton.addActionListener((event) -> {
            reset();
            playButton.setEnabled(false);
        });
        topPanel.add(playButton);

        String playerName = "";
        try {
            playerName = Files.readString(Path.of("player.txt"));
        } catch (IOException ignored) {
        }

        playerNameTextField = new JTextField(playerName);
        playerNameTextField.setToolTipText("Player name");
        playerNameTextField.setColumns(10);
        topPanel.add(playerNameTextField);

        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new BorderLayout());
        contentPane.add(gamePanel, BorderLayout.CENTER);

        JPanel playerHandPanel = new JPanel(new BorderLayout());
        gamePanel.add(playerHandPanel, BorderLayout.WEST);
        playerHandModel = new DefaultListModel<>();

        playerHandList = new JList<>(playerHandModel);
        playerHandList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        playerHandPanel.add(playerHandList, BorderLayout.CENTER);

        playerPickButton = new JButton("Pick card");
        playerPickButton.setEnabled(false);
        playerPickButton.addActionListener(event -> {
            game.step(playerHandList.getSelectedIndex());
            while (game.getActivePlayer() != 0) {
                game.step(ai.sampleAction(game.getActivePlayerValidCardIndices()));
            }
            updateGameUI();
        });
        playerPassButton = new JButton("Pass turn");
        playerPassButton.setEnabled(false);
        playerPassButton.addActionListener(event -> {
            game.step(-1);
            while (game.getActivePlayer() != 0) {
                game.step(ai.sampleAction(game.getActivePlayerValidCardIndices()));
            }
            updateGameUI();
        });
        JPanel playerActionPanel = new JPanel();
        playerHandPanel.add(playerActionPanel, BorderLayout.SOUTH);
        playerActionPanel.add(playerPickButton);
        playerActionPanel.add(playerPassButton);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        gamePanel.add(infoPanel, BorderLayout.CENTER);

        deckLabel = new JLabel();
        deckLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoPanel.add(deckLabel);
        trumpLabel = new JLabel();
        trumpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoPanel.add(trumpLabel);
        pileLabel = new JLabel();
        pileLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoPanel.add(pileLabel);

        JPanel bottomPanel = new JPanel();
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        statusLabel = new JLabel("Nothing yet.");
        bottomPanel.add(statusLabel);

        playerHandList.addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) {
                return;
            }
            playerPickButton.setEnabled(false);
            if (game.getActivePlayer() != 0) {
                return;
            }
            int index = playerHandList.getSelectedIndex();
            int[] validCardIndices = game.getActivePlayerValidCardIndices();
            if (Arrays.stream(validCardIndices).noneMatch(i -> i == index) || index == -1) {
                return;
            }
            playerPickButton.setEnabled(true);
        });

        pack();
        setLocationRelativeTo(null);
    }

    @Override
    public void dispose() {
        try {
            Files.writeString(Path.of("player.txt"), playerNameTextField.getText());
        } catch (IOException ignored) {
        }
        super.dispose();
    }

    private void updateGameUI() {
        playerHandList.setSelectedIndex(-1);

        List<Card> hand = game.getPlayerHand(0);
        playerHandModel.clear();
        playerHandModel.addAll(hand);

        List<Card> pile = game.getPile();

        deckLabel.setText("Cards remaining in deck: " + game.getDeckSize());
        trumpLabel.setText("Trump card: " + game.getTrumpCard());
        pileLabel.setText("Pile: " + pile);

        int winner = game.getWinner();
        if (winner != -1) {
            statusLabel.setText("Player " + (winner + 1) + " wins!");
            playButton.setEnabled(true);
            return;
        }

        int player = game.getActivePlayer();
        statusLabel.setText("It's player " + (player + 1) + "'s turn to " + game.getState().toString().toLowerCase() + ".");
        if (player == 0) {
            playerPassButton.setEnabled(Arrays.stream(game.getActivePlayerValidCardIndices()).anyMatch(i -> i == -1));
        } else {
            playerPassButton.setEnabled(false);
        }
    }

    private void reset() {
        game.reset();
        ai = new DurakAI();
        while (game.getActivePlayer() != 0) {
            game.step(ai.sampleAction(game.getActivePlayerValidCardIndices()));
        }
        updateGameUI();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            new DurakForm().setVisible(true);
        });
    }
}
