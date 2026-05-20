package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Vocabulary;
import dao.VocabularyDAO;

public class GameMatchPanel extends JPanel {
    private JPanel gridPanel;
    private List<Vocabulary> fullGameData;
    private List<String> cardValues;
    private JButton firstCard, secondCard;
    private String firstVal, secondVal;

    // Các thành phần điều hướng lật trang Dashboard
    private JPanel mainContent;
    private CardLayout cardLayout;

    // Vận hành thuật toán tăng từ theo màn
    private int currentLimit = 2;
    private int matchedPairs = 0;

    // Bộ màu đồng bộ hệ thống VocaLearn
    private final Color purpleMain = new Color(108, 92, 231);
    private final Color purpleLight = new Color(241, 240, 255);
    private final Color bgMain = new Color(248, 249, 253);
    private final Color greenSuccess = new Color(46, 204, 113);

    private JLabel lblLevel;

    // ĐÃ SỬA: Hàm dựng nhận thêm bộ điều hướng mainContent và cardLayout từ Dashboard
    public GameMatchPanel(int topicId, JPanel mainContent, CardLayout cardLayout) {
        this.mainContent = mainContent;
        this.cardLayout = cardLayout;

        setLayout(new BorderLayout(0, 20));
        setBackground(bgMain);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- CỤM THANH TIÊU ĐỀ TRÊN ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel headerLeft = new JPanel(new GridLayout(2, 1, 5, 5));
        headerLeft.setOpaque(false);

        JLabel lblTitle = new JLabel("TRÒ CHƠI GHÉP ĐÔI TỪ VỰNG", JLabel.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(purpleMain);

        lblLevel = new JLabel("Màn chơi: Đang chuẩn bị dữ liệu...", JLabel.LEFT);
        lblLevel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblLevel.setForeground(Color.GRAY);

        headerLeft.add(lblTitle);
        headerLeft.add(lblLevel);
        topPanel.add(headerLeft, BorderLayout.WEST);

        // --- ĐOẠN VIẾT THÊM: NÚT THOÁT NHANH KHI ĐANG CHƠI ---
        JButton btnExit = new JButton("Thoát trò chơi ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 10);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btnExit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnExit.setBackground(new Color(231, 76, 60)); // Màu đỏ nhạt cảnh báo
        btnExit.setForeground(Color.WHITE);
        btnExit.setContentAreaFilled(false);
        btnExit.setFocusPainted(false);
        btnExit.setBorderPainted(false);
        btnExit.setPreferredSize(new Dimension(130 ,10));
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnExit.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn dừng chơi và quay lại màn hình chọn bộ thẻ?", "Xác nhận thoát", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                // Lật giao diện quay về màn hình chọn bộ từ Ban đầu
                cardLayout.show(mainContent, "GAME_SELECT_TOPIC");
            }
        });
        topPanel.add(btnExit, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- KẾT NỐI DATABASE THỰC TẾ ---
        VocabularyDAO dao = new VocabularyDAO();
        fullGameData = dao.getByTopicId(topicId);

        if (fullGameData != null && !fullGameData.isEmpty()) {
            Collections.shuffle(fullGameData);
        } else {
            fullGameData = new ArrayList<>();
        }

        gridPanel = new JPanel();
        add(gridPanel, BorderLayout.CENTER);

        if (!fullGameData.isEmpty()) {
            startNewLevel();
        } else {
            lblLevel.setText("Bộ thẻ này hiện tại chưa có từ vựng nào để tạo trò chơi!");
            btnExit.setText("Quay lại");
        }
    }

    private void startNewLevel() {
        gridPanel.removeAll();
        matchedPairs = 0;
        firstCard = null;
        secondCard = null;

        int actualLimit = Math.min(fullGameData.size(), currentLimit);
        lblLevel.setText("Màn chơi: Đang ôn tập " + actualLimit + " từ vựng (Tổng: " + actualLimit * 2 + " thẻ)");

        cardValues = new ArrayList<>();
        for (int i = 0; i < actualLimit; i++) {
            Vocabulary v = fullGameData.get(i);
            cardValues.add(v.getWord());
            cardValues.add(v.getMeaning());
        }
        Collections.shuffle(cardValues);

        int totalCards = cardValues.size();
        int cols = (totalCards <= 4) ? 2 : 4;
        int rows = (int) Math.ceil((double) totalCards / cols);

        gridPanel.setLayout(new GridLayout(rows, cols, 15, 15));
        gridPanel.setOpaque(false);

        for (String val : cardValues) {
            JButton btnCard = new JButton("?") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    if (getText().equals("?")) {
                        g2.setColor(new Color(220, 223, 230));
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                    }
                    super.paintComponent(g);
                    g2.dispose();
                }
            };

            btnCard.setBackground(purpleLight);
            btnCard.setForeground(purpleMain);
            btnCard.setContentAreaFilled(false);
            btnCard.setFocusPainted(false);
            btnCard.setBorderPainted(false);
            btnCard.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (val != null && val.matches(".*[\\u4e00-\\u9fa5].*")) {
                btnCard.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
            } else {
                btnCard.setFont(new Font("Segoe UI", Font.BOLD, 16));
            }

            btnCard.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { if(btnCard.getText().equals("?")) btnCard.setBackground(new Color(225, 222, 255)); }
                @Override public void mouseExited(java.awt.event.MouseEvent e) { if(btnCard.getText().equals("?")) btnCard.setBackground(purpleLight); }
            });

            btnCard.addActionListener(e -> {
                if (btnCard == firstCard || btnCard.getText().equals("") || !btnCard.isEnabled()) return;

                if (firstCard == null) {
                    firstCard = btnCard;
                    firstVal = val;
                    btnCard.setText(val);
                    btnCard.setBackground(purpleMain);
                    btnCard.setForeground(Color.WHITE);
                } else if (secondCard == null) {
                    secondCard = btnCard;
                    secondVal = val;
                    btnCard.setText(val);
                    btnCard.setBackground(purpleMain);
                    btnCard.setForeground(Color.WHITE);

                    checkMatch();
                }
            });
            gridPanel.add(btnCard);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void checkMatch() {
        boolean isMatch = false;
        for (Vocabulary v : fullGameData) {
            if ((v.getWord().equals(firstVal) && v.getMeaning().equals(secondVal)) ||
                    (v.getWord().equals(secondVal) && v.getMeaning().equals(firstVal))) {
                isMatch = true;
                break;
            }
        }

        if (isMatch) {
            Timer t = new Timer(400, e -> {
                firstCard.setBackground(greenSuccess);
                firstCard.setForeground(Color.WHITE);
                firstCard.setText("perfect");
                firstCard.setEnabled(false);

                secondCard.setBackground(greenSuccess);
                secondCard.setForeground(Color.WHITE);
                secondCard.setText("perfect");
                secondCard.setEnabled(false);

                matchedPairs++;

                int maxWordsInThisRound = Math.min(fullGameData.size(), currentLimit);
                if (matchedPairs == maxWordsInThisRound) {
                    handleLevelUp();
                } else {
                    resetSelection();
                }
            });
            t.setRepeats(false);
            t.start();
        } else {
            firstCard.setBackground(new Color(231, 76, 60));
            secondCard.setBackground(new Color(231, 76, 60));

            Timer t = new Timer(800, e -> {
                firstCard.setText("?");
                firstCard.setBackground(purpleLight);
                firstCard.setForeground(purpleMain);
                secondCard.setText("?");
                secondCard.setBackground(purpleLight);
                secondCard.setForeground(purpleMain);
                resetSelection();
            });
            t.setRepeats(false);
            t.start();
        }
    }

    private void handleLevelUp() {
        if (currentLimit >= fullGameData.size()) {
            // ĐÃ SỬA: Chơi xong hết từ vựng thì thông báo và tự lật màn hình quay về menu chọn bộ thẻ ban đầu
            JOptionPane.showMessageDialog(this, "Xuất sắc! Bạn đã vượt qua toàn bộ " + fullGameData.size() + " từ vựng của bộ thẻ này!");
            cardLayout.show(mainContent, "GAME_SELECT_TOPIC");
        } else {
            currentLimit += 2;
            JOptionPane.showMessageDialog(this, " Màn chơi hoàn thành! Độ khó tăng lên: " + currentLimit + " từ vựng.");
            startNewLevel();
        }
    }

    private void resetSelection() {
        firstCard = null; secondCard = null;
        firstVal = null; secondVal = null;
    }
}