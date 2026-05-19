package view;

import dao.VocabularyDAO;
import dao.TopicDAO;
import dao.LearningProgressDAO;
import model.Vocabulary;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class ReviewPanel extends JPanel {

    private CardLayout cardLayout;
    private JPanel container;
    private Color purpleSub = new Color(108, 92, 231);
    private Color bgMain = new Color(248, 249, 253);

    // DAOs
    private VocabularyDAO vocabDAO = new VocabularyDAO();
    private TopicDAO topicDAO = new TopicDAO();
    private LearningProgressDAO lpDAO = new LearningProgressDAO();

    private List<Vocabulary> listReview = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int correctCount = 0;
    private int userId ;
    private String currentMode = "";

    private JComboBox<String> cbTopicSelector;
    private Map<Integer, String> topicMap;
    private boolean isProcessing = false;
    private Timer activeTimer;

    public ReviewPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());
        setBackground(bgMain);

        cardLayout = new CardLayout();
        container = new JPanel(cardLayout);
        container.setOpaque(false);

        container.add(createSelectionMenu(), "MENU");

        add(container, BorderLayout.CENTER);
        cardLayout.show(container, "MENU");
    }

    private void loadTopics() {
        topicMap = topicDAO.getTopicsByUserId(userId);
        cbTopicSelector.removeAllItems();
        cbTopicSelector.addItem("-- Tất cả các bộ --");
        if (topicMap != null) {
            for (String name : topicMap.values()) cbTopicSelector.addItem(name);
        }
    }

    private JPanel createSelectionMenu() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(50, 50, 50, 50));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setOpaque(false);
        topBar.add(new JLabel("Chọn bộ thẻ: "));
        cbTopicSelector = new JComboBox<>();
        loadTopics();
        topBar.add(cbTopicSelector);
        p.add(topBar, BorderLayout.NORTH);

        JPanel centerP = new JPanel(new GridBagLayout());
        centerP.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel title = new JLabel("Hệ thống Ôn tập");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 50, 0);
        centerP.add(title, gbc);

        JPanel grid = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        grid.setOpaque(false);
        grid.add(createModeCard("Trắc nghiệm", "Chọn đáp án đúng", "CHOICE"));
        grid.add(createModeCard("Điền từ", "Gõ từ chính xác", "FILL"));
        grid.add(createModeCard("Ví dụ", "Hoàn thành câu", "SENTENCE"));

        gbc.gridy = 1;
        centerP.add(grid, gbc);
        p.add(centerP, BorderLayout.CENTER);
        return p;
    }

    private void startReview(String mode) {
        if (activeTimer != null) activeTimer.stop();

        String selectedTopic = (String) cbTopicSelector.getSelectedItem();
        int topicId = -1;
        if (topicMap != null && !selectedTopic.equals("-- Tất cả các bộ --")) {
            for (var entry : topicMap.entrySet()) {
                if (entry.getValue().equals(selectedTopic)) { topicId = entry.getKey(); break; }
            }
        }

        listReview = (topicId == -1) ? vocabDAO.getByTopicId(1) : vocabDAO.getByTopicId(topicId);

        if (listReview == null || listReview.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bộ thẻ này chưa có từ vựng để ôn tập!");
            return;
        }

        currentMode = mode;
        score = 0;
        correctCount = 0;
        currentQuestionIndex = 0;
        isProcessing = false;
        Collections.shuffle(listReview);

        showQuestion();
    }

    private void showQuestion() {
        container.removeAll();
        JPanel questionView;
        switch (currentMode) {
            case "CHOICE": questionView = createMultipleChoiceView(); break;
            case "FILL": questionView = createFillInBlankView(); break;
            case "SENTENCE": questionView = createSentenceCompletionView(); break;
            default: questionView = new JPanel();
        }
        container.add(questionView, "CURRENT_VIEW");
        cardLayout.show(container, "CURRENT_VIEW");
        container.revalidate();
        container.repaint();
    }

    private void processAnswer(boolean isCorrect, int points) {
        Vocabulary v = listReview.get(currentQuestionIndex);
        lpDAO.updateProgressAfterReview(userId, v.getWordID(), isCorrect);

        if (isCorrect) {
            score += points;
            correctCount++;
        }

        activeTimer = new Timer(1000, ev -> nextQuestion());
        activeTimer.setRepeats(false);
        activeTimer.start();
    }

    private void nextQuestion() {
        if (activeTimer != null) activeTimer.stop();
        if (currentQuestionIndex < listReview.size() - 1) {
            currentQuestionIndex++;
            isProcessing = false;
            showQuestion();
        } else {
            showResultScreen();
        }
    }

    // --- VIEW CHẾ ĐỘ 1: TRẮC NGHIỆM ---
    private JPanel createMultipleChoiceView() {
        Vocabulary currentV = listReview.get(currentQuestionIndex);
        JPanel p = createBaseView("Câu hỏi " + (currentQuestionIndex + 1) + "/" + listReview.size());
        JPanel centerP = new JPanel();
        centerP.setLayout(new BoxLayout(centerP, BoxLayout.Y_AXIS));
        centerP.setOpaque(false);

        // NGHĨA luôn là tiếng Việt nên giữ font chuẩn
        JPanel qCard = createQuestionCard("Chọn từ đúng cho nghĩa:<br><b>\"" + currentV.getMeaning() + "\"</b>");

        JPanel optionsP = new JPanel(new GridLayout(2, 2, 20, 20));
        optionsP.setOpaque(false);
        optionsP.setMaximumSize(new Dimension(600, 180));

        List<String> options = new ArrayList<>();
        options.add(currentV.getWord());
        for (Vocabulary v : listReview) {
            if (options.size() < 4 && !v.getWord().equals(currentV.getWord())) options.add(v.getWord());
        }
        Collections.shuffle(options);

        for (String s : options) {
            JButton btn = createStyledButton(s);

            // --- ĐOẠN ĐÃ FIX: Đổi font nút bấm nếu là chữ tiếng Trung ---
            if (s != null && s.matches(".*[\\u4e00-\\u9fa5].*")) {
                btn.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));
            } else {
                btn.setFont(new Font("Segoe UI", Font.BOLD, 17));
            }

            btn.addActionListener(e -> {
                if (isProcessing) return;
                isProcessing = true;
                boolean isCorrect = s.equals(currentV.getWord());

                for (Component c : optionsP.getComponents()) {
                    c.setEnabled(false);
                    JButton b = (JButton) c;
                    if (b.getText().equals(currentV.getWord())) b.setBackground(new Color(46, 204, 113));
                }
                if (!isCorrect) btn.setBackground(new Color(231, 76, 60));

                processAnswer(isCorrect, 10);
            });
            optionsP.add(btn);
        }

        centerP.add(Box.createVerticalGlue());
        centerP.add(qCard); centerP.add(Box.createVerticalStrut(30));
        centerP.add(optionsP); centerP.add(Box.createVerticalGlue());
        p.add(centerP, BorderLayout.CENTER);
        return p;
    }

    // --- VIEW CHẾ ĐỘ 2: ĐIỀN TỪ ---
    private JPanel createFillInBlankView() {
        Vocabulary currentV = listReview.get(currentQuestionIndex);
        JPanel p = createBaseView("Luyện viết từ vựng");
        JPanel centerP = new JPanel(new GridBagLayout());
        centerP.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        // Đọc ngôn ngữ để hiển thị câu hỏi gợi ý phù hợp
        boolean isChinese = currentV.getWord() != null && currentV.getWord().matches(".*[\\u4e00-\\u9fa5].*");
        String hintText = isChinese ? "Dịch sang tiếng Trung:<br><b>" : "Dịch sang tiếng Anh:<br><b>";

        JLabel lblHint = new JLabel("<html><center>" + hintText + currentV.getMeaning() + "</b></center></html>");
        lblHint.setFont(new Font("SansSerif", Font.PLAIN, 22));
        gbc.gridy = 0; centerP.add(lblHint, gbc);

        JTextField txtInput = new JTextField(15);
        txtInput.setHorizontalAlignment(JTextField.CENTER);
        txtInput.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, purpleSub));
        txtInput.setOpaque(false);

        // --- ĐOẠN ĐÃ FIX: Tăng font và đổi sang YaHei nếu người dùng cần gõ chữ Hán ---
        if (isChinese) {
            txtInput.setFont(new Font("Microsoft YaHei", Font.BOLD, 30));
        } else {
            txtInput.setFont(new Font("SansSerif", Font.BOLD, 30));
        }

        txtInput.addActionListener(e -> {
            if (isProcessing) return;
            String text = txtInput.getText().trim();
            if (text.isEmpty()) return;
            isProcessing = true;
            txtInput.setEditable(false);

            boolean isCorrect = text.equalsIgnoreCase(currentV.getWord());
            if (isCorrect) {
                txtInput.setForeground(new Color(46, 204, 113));
            } else {
                txtInput.setForeground(Color.RED);

                // --- ĐOẠN ĐÃ FIX: Cài đặt font hiển thị đáp án đúng nếu là tiếng Trung nhằm tránh ô vuông ---
                String fontStyle = isChinese ? "font-family:'Microsoft YaHei'; font-size:24px;" : "font-size:22px;";
                lblHint.setText("<html><center>Sai rồi! Đáp án là: <b style='" + fontStyle + " color:green'>" + currentV.getWord() + "</b></center></html>");
            }
            processAnswer(isCorrect, 15);
        });

        gbc.gridy = 1; gbc.insets = new Insets(50, 0, 0, 0);
        centerP.add(txtInput, gbc);
        p.add(centerP, BorderLayout.CENTER);
        SwingUtilities.invokeLater(txtInput::requestFocusInWindow);
        return p;
    }

    // --- VIEW CHẾ ĐỘ 3: VÍ DỤ ---
    private JPanel createSentenceCompletionView() {
        Vocabulary currentV = listReview.get(currentQuestionIndex);
        JPanel p = createBaseView("Học từ qua ví dụ");
        JPanel centerP = new JPanel();
        centerP.setLayout(new BoxLayout(centerP, BoxLayout.Y_AXIS));
        centerP.setOpaque(false);

        String sentence = currentV.getExample();
        String hidden = sentence.replaceAll("(?i)" + currentV.getWord(), "_______");

        // --- ĐOẠN ĐÃ FIX: Nếu câu ví dụ chứa chữ Hán, cấu hình ép font hiển thị của Card câu hỏi ---
        JPanel qCard = createQuestionCard("Điền vào chỗ trống:<br><i style='font-size:18px'>\"" + hidden + "\"</i>");
        if (sentence != null && sentence.matches(".*[\\u4e00-\\u9fa5].*")) {
            for (Component c : qCard.getComponents()) {
                if (c instanceof JLabel) {
                    c.setFont(new Font("Microsoft YaHei", Font.PLAIN, 20));
                }
            }
        }

        JPanel optionsP = new JPanel(new GridLayout(2, 2, 20, 20));
        optionsP.setOpaque(false);
        optionsP.setMaximumSize(new Dimension(600, 180));

        List<String> options = new ArrayList<>();
        options.add(currentV.getWord());
        for (Vocabulary v : listReview) {
            if (options.size() < 4 && !v.getWord().equals(currentV.getWord())) options.add(v.getWord());
        }
        Collections.shuffle(options);

        for (String s : options) {
            JButton btn = createStyledButton(s);

            // --- ĐOẠN ĐÃ FIX: Thiết lập font cho các đáp án lựa chọn của chế độ Ví dụ ---
            if (s != null && s.matches(".*[\\u4e00-\\u9fa5].*")) {
                btn.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));
            } else {
                btn.setFont(new Font("Segoe UI", Font.BOLD, 17));
            }

            btn.addActionListener(e -> {
                if (isProcessing) return;
                isProcessing = true;
                boolean isCorrect = s.equals(currentV.getWord());

                for (Component c : optionsP.getComponents()) {
                    c.setEnabled(false);
                    JButton b = (JButton) c;
                    if (b.getText().equals(currentV.getWord())) b.setBackground(new Color(46, 204, 113));
                }
                if (!isCorrect) btn.setBackground(new Color(231, 76, 60));

                processAnswer(isCorrect, 10);
            });
            optionsP.add(btn);
        }

        centerP.add(Box.createVerticalGlue());
        centerP.add(qCard); centerP.add(Box.createVerticalStrut(30));
        centerP.add(optionsP); centerP.add(Box.createVerticalGlue());
        p.add(centerP, BorderLayout.CENTER);
        return p;
    }

    private void showResultScreen() {
        container.removeAll();
        JPanel resP = new JPanel(new GridBagLayout());
        resP.setBackground(bgMain);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 0, 10, 0);

        JLabel lblTitle = new JLabel("BẢNG ĐIỂM ÔN TẬP");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTitle.setForeground(purpleSub);
        g.gridy = 0; resP.add(lblTitle, g);

        JLabel lblCorrect = new JLabel("Câu đúng: " + correctCount + " / " + listReview.size());
        lblCorrect.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        g.gridy = 1; resP.add(lblCorrect, g);

        JLabel lblScore = new JLabel("Tổng điểm: " + score);
        lblScore.setFont(new Font("Segoe UI", Font.BOLD, 28));
        g.gridy = 2; resP.add(lblScore, g);

        double rate = (double) correctCount / listReview.size();
        String rank = (rate == 1.0) ? "XUẤT SẮC!" : (rate >= 0.7) ? "RẤT TỐT!" : "CỐ GẮNG LÊN!";
        JLabel lblRank = new JLabel(rank);
        lblRank.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblRank.setForeground(new Color(230, 126, 34));
        g.gridy = 3; resP.add(lblRank, g);

        JButton btnBack = createStyledButton("Quay về Menu");
        btnBack.setPreferredSize(new Dimension(200, 50));
        btnBack.addActionListener(e -> exitReview());
        g.gridy = 4; g.insets = new Insets(30, 0, 0, 0);
        resP.add(btnBack, g);

        container.add(resP, "RESULT");
        cardLayout.show(container, "RESULT");
        container.revalidate(); container.repaint();
    }

    private void exitReview() {
        if (activeTimer != null) activeTimer.stop();
        isProcessing = false;
        container.removeAll();
        container.add(createSelectionMenu(), "MENU");
        cardLayout.show(container, "MENU");
        container.revalidate(); container.repaint();
    }

    private JPanel createBaseView(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(bgMain);
        p.setBorder(new EmptyBorder(25, 25, 25, 25));
        JLabel lbl = new JLabel(title); lbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        JButton btn = new JButton("Thoát");
        btn.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(this, "Thoát và không lưu kết quả?", "Xác nhận", 0) == 0) exitReview();
        });
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false); top.add(lbl, BorderLayout.WEST); top.add(btn, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);
        return p;
    }

    private JPanel createQuestionCard(String text) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.dispose();
            }
        };
        card.setPreferredSize(new Dimension(650, 180)); card.setOpaque(false);
        JLabel lbl = new JLabel("<html><center>" + text + "</center></html>");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 22)); card.add(lbl);
        return card;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                super.paintComponent(g); g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 17)); btn.setBackground(Color.WHITE);
        btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(225, 225, 225)));
        return btn;
    }

    private JPanel createModeCard(String title, String desc, String mode) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);
                g2.dispose();
            }
        };
        card.setPreferredSize(new Dimension(220, 160)); card.setOpaque(false); card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        JLabel lblT = new JLabel(title); lblT.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel lblD = new JLabel(desc); lblD.setForeground(Color.GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; card.add(lblT, gbc); gbc.gridy = 1; gbc.insets = new Insets(10, 0, 0, 0); card.add(lblD, gbc);
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { startReview(mode); }
            @Override public void mouseEntered(MouseEvent e) { lblT.setForeground(purpleSub); }
            @Override public void mouseExited(MouseEvent e) { lblT.setForeground(Color.BLACK); }
        });
        return card;
    }
}