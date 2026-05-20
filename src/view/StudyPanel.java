package view;

import dao.VocabularyDAO;
import dao.TopicDAO;
import model.Vocabulary;
import utils.RoundButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StudyPanel extends JPanel {

    private boolean isFlipped = false;
    private JLabel lblWord, lblPhonetic, lblMean, lblExample;
    private JPanel flashcard;
    private Color purpleSub = new Color(108, 92, 231);

    // ================= Logic Data =================
    private VocabularyDAO vocabDAO = new VocabularyDAO();
    private TopicDAO topicDAO = new TopicDAO();
    private LinkedList<Vocabulary> studyQueue = new LinkedList<>();
    private int totalInSession = 0;
    private int learnedCount = 0;

    private JComboBox<String> cbTopics;
    private Map<Integer, String> topicMap;
    private JProgressBar pb;
    private JLabel lblProg, lblStatLearned, lblStatRemaining, lblStatTotal;
    private StatBarChart statBarChart;
    private int userId;

    public StudyPanel(int id) {
        this.userId = id;
        initTTS();

        setLayout(new BorderLayout(20, 0));
        setBackground(new Color(248, 249, 253));
        setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- BÊN TRÁI: KHU VỰC HỌC ---
        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Học thẻ");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 26));

        // --- PANEL CHỨA COMBOBOX VÀ NÚT RELOAD ---
        JPanel comboControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        comboControlPanel.setOpaque(false);

        cbTopics = new JComboBox<>();
        cbTopics.setPreferredSize(new Dimension(200, 30));
        loadTopicsToCombo();
        cbTopics.addActionListener(e -> loadVocabBySelectedTopic());

        // Tạo nút Reload
        JButton btnReload = new JButton("🔄");
        btnReload.setPreferredSize(new Dimension(40, 30));
        btnReload.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnReload.setToolTipText("Làm mới danh sách bộ thẻ");
        btnReload.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReload.setFocusPainted(false);

        btnReload.addActionListener(e -> {
            loadTopicsToCombo();
            JOptionPane.showMessageDialog(this, "Đã cập nhật danh sách bộ thẻ mới nhất!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        });

        comboControlPanel.add(cbTopics);
        comboControlPanel.add(btnReload);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(comboControlPanel, BorderLayout.EAST);
        leftPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);

        lblProg = new JLabel("0 / 0", JLabel.CENTER);
        lblProg.setAlignmentX(Component.CENTER_ALIGNMENT);
        pb = new JProgressBar(0, 100);
        pb.setForeground(purpleSub);
        pb.setMaximumSize(new Dimension(500, 6));
        pb.setAlignmentX(Component.CENTER_ALIGNMENT);

        flashcard = createFlashcard();

        centerContent.add(lblProg);
        centerContent.add(Box.createVerticalStrut(10));
        centerContent.add(pb);
        centerContent.add(Box.createVerticalStrut(20));
        centerContent.add(flashcard);
        leftPanel.add(centerContent, BorderLayout.CENTER);

        leftPanel.add(createActionButtons(), BorderLayout.SOUTH);
        add(leftPanel, BorderLayout.CENTER);

        // --- BÊN PHẢI: SIDEBAR THỐNG KÊ ---
        add(createRightStatsSidebar(), BorderLayout.EAST);

        loadVocabBySelectedTopic();
    }

    private void initTTS() {
        System.out.println("Audio Service (Microsoft Edge/Google TTS) đã sẵn sàng.");
    }

    private void speak(String text) {
        if (text == null || text.isEmpty()) return;

        // Strip HTML tags if any to speak cleanly
        String cleanText = text.replaceAll("<[^>]*>", "").trim();
        if (cleanText.matches(".*[\\u4e00-\\u9fa5].*")) {
            utils.AudioService.playChinese(cleanText);
        } else {
            utils.AudioService.playEnglish(cleanText);
        }
    }

    private void loadTopicsToCombo() {
        topicMap = topicDAO.getTopicsByUserId(this.userId);
        cbTopics.removeAllItems();
        cbTopics.addItem("-- Chọn bộ thẻ để học --");
        if (topicMap != null) {
            for (String name : topicMap.values()) {
                cbTopics.addItem(name);
            }
        }
    }

    // --- CẬP NHẬT TRẠNG THÁI BAN ĐẦU (MẶT TRƯỚC CHỨA ẢNH CACHE CỤC BỘ) ---
    private void updateUIWithCard() {
        if (studyQueue.isEmpty()) {
            showFinishedState();
            return;
        }

        Vocabulary v = studyQueue.getFirst();
        isFlipped = false;

        // 🌟 TÍNH TOÁN ĐƯỜNG DẪN FILE ẢNH TRONG THƯ MỤC CACHE CỦA MÁY
        String cachePath = "img/cache/img_" + v.getWordID() + ".png";
        java.io.File file = new java.io.File(cachePath);

        StringBuilder frontHtml = new StringBuilder("<html><center>");

        // Nếu file ảnh đã được luồng ngầm tải về máy thành công, ta chèn thẻ <img> đọc file cục bộ
        if (file.exists()) {
            frontHtml.append("<img src='file:").append(cachePath)
                    .append("' width='140' height='100'><br><br>");
        }

        frontHtml.append(v.getWord()).append("</center></html>");
        lblWord.setText(frontHtml.toString());

        // --- KIỂM TRA ĐỔI FONT CHỮ CHỐNG LỖI Ô VUÔNG ---
        if (v.getWord() != null && v.getWord().matches(".*[\\u4e00-\\u9fa5].*")) {
            lblWord.setFont(new Font("Microsoft YaHei", Font.BOLD, 45));
        } else {
            lblWord.setFont(new Font("Segoe UI", Font.BOLD, 45));
        }
        lblWord.setForeground(purpleSub);

        speak(v.getWord());

        String phonetic = v.getPronunciation();
        if (phonetic != null && !phonetic.trim().isEmpty()) {
            lblPhonetic.setText(phonetic + " 🔊");
        } else {
            lblPhonetic.setText(" ");
        }
        lblPhonetic.setVisible(true);

        lblMean.setVisible(false);
        lblExample.setVisible(false);

        lblProg.setText(learnedCount + " / " + totalInSession);
        pb.setMaximum(totalInSession);
        pb.setValue(learnedCount);

        updateStats();
        flashcard.setBackground(Color.WHITE);

        // Ép làm tươi đồ họa đồ họa để hiển thị ảnh ngay lập tức
        flashcard.revalidate();
        flashcard.repaint();
    }

    // --- LOGIC LẬT THẺ (FLIP CARD) ---
    private void flipCard() {
        if (studyQueue.isEmpty()) return;
        Vocabulary v = studyQueue.getFirst();
        isFlipped = !isFlipped;

        if (isFlipped) {
            // --- CHUYỂN SANG MẶT SAU (Ẩn ảnh, hiện Nghĩa + Ví dụ) ---
            flashcard.setBackground(new Color(250, 250, 255));

            lblWord.setText(v.getWord());

            if (v.getWord() != null && v.getWord().matches(".*[\\u4e00-\\u9fa5].*")) {
                lblWord.setFont(new Font("Microsoft YaHei", Font.BOLD, 28));
            } else {
                lblWord.setFont(new Font("Segoe UI", Font.BOLD, 28));
            }

            lblPhonetic.setVisible(false);

            String wordType = (v.getType() != null && !v.getType().trim().isEmpty()) ? v.getType() : "chưa rõ";
            String wordMeaning = (v.getMeaning() != null && !v.getMeaning().trim().isEmpty()) ? v.getMeaning() : "Chưa có nghĩa";
            lblMean.setText("<html><center><b style='color:#6c5ce7'>(" + wordType + ")</b><br><span style='font-size:15px; color:#2d3436'>" + wordMeaning + "</span></center></html>");
            lblMean.setVisible(true);

            String wordExample = (v.getExample() != null && !v.getExample().trim().isEmpty()) ? v.getExample() : "Chưa có ví dụ minh họa.";
            lblExample.setText("<html><center><i style='font-size:14px; color:#636e72'>Ví dụ: " + wordExample + "</i></center></html>");
            lblExample.setVisible(true);
        } else {
            // --- QUAY TRỞ LẠI MẶT TRƯỚC (Đọc lại ảnh từ ổ cứng) ---
            flashcard.setBackground(Color.WHITE);

            String cachePath = "img/cache/img_" + v.getWordID() + ".png";
            java.io.File file = new java.io.File(cachePath);

            StringBuilder frontHtml = new StringBuilder("<html><center>");
            if (file.exists()) {
                frontHtml.append("<img src='file:").append(cachePath)
                        .append("' width='140' height='100'><br><br>");
            }
            frontHtml.append(v.getWord()).append("</center></html>");
            lblWord.setText(frontHtml.toString());

            if (v.getWord() != null && v.getWord().matches(".*[\\u4e00-\\u9fa5].*")) {
                lblWord.setFont(new Font("Microsoft YaHei", Font.BOLD, 45));
            } else {
                lblWord.setFont(new Font("Segoe UI", Font.BOLD, 45));
            }

            String phonetic = v.getPronunciation();
            if (phonetic != null && !phonetic.trim().isEmpty()) {
                lblPhonetic.setText(phonetic + " 🔊");
            } else {
                lblPhonetic.setText(" ");
            }
            lblPhonetic.setVisible(true);

            lblMean.setVisible(false);
            lblExample.setVisible(false);

            speak(v.getWord());
        }

        // Đảm bảo giao diện mượt mà khi lật qua lật lại
        flashcard.revalidate();
        flashcard.repaint();
    }

    private void nextCard(boolean isMastered) {
        if (studyQueue.isEmpty()) return;
        Vocabulary current = studyQueue.removeFirst();

        if (isMastered) {
            vocabDAO.updateStatus(current.getWordID(), "Mastered");
            learnedCount++;
        } else {
            vocabDAO.updateStatus(current.getWordID(), "Learning");
            studyQueue.addLast(current);
        }
        updateUIWithCard();
    }

    private JPanel createFlashcard() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(new Color(230, 230, 230));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 30, 30);
                g2.dispose();
            }
        };
        card.setPreferredSize(new Dimension(550, 350));
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        lblWord = new JLabel("", JLabel.CENTER);
        lblWord.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblPhonetic = new JLabel("", JLabel.CENTER);
        lblPhonetic.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPhonetic.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblPhonetic.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!lblPhonetic.getText().trim().isEmpty()) {
                    speak(lblWord.getText());
                }
            }
        });

        lblMean = new JLabel("", JLabel.CENTER);
        lblMean.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblExample = new JLabel("", JLabel.CENTER);
        lblExample.setAlignmentX(Component.CENTER_ALIGNMENT);

        // BỐ CỤC CHUẨN: Đẩy chữ lên, HTML sẽ tự động xử lý kẹp ảnh nằm trên đầu chữ
        info.add(lblWord);
        info.add(Box.createVerticalStrut(15));
        info.add(lblPhonetic);
        info.add(lblMean);
        info.add(Box.createVerticalStrut(20));
        info.add(lblExample);
        card.add(info);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { flipCard(); }
        });
        return card;
    }

    private JPanel createActionButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        p.setOpaque(false);
        JButton btnHard = createRatingBtn("Học lại", "Cần ôn tập", new Color(255, 235, 235), Color.RED);
        JButton btnEasy = createRatingBtn("Đã thuộc", "Lưu tiến độ", new Color(235, 255, 240), new Color(46, 204, 113));
        btnHard.addActionListener(e -> nextCard(false));
        btnEasy.addActionListener(e -> nextCard(true));
        p.add(btnHard); p.add(btnEasy);
        return p;
    }

    private void showFinishedState() {
        lblWord.setText("Xong!");
        lblWord.setFont(new Font("Segoe UI", Font.BOLD, 45));
        lblPhonetic.setText(" ");
        lblMean.setText("Bạn đã thuộc tất cả từ!");
        lblMean.setVisible(true);
        lblExample.setText("");
        lblProg.setText(totalInSession + " / " + totalInSession);
        pb.setValue(totalInSession);
        updateStats();
        flashcard.setBackground(new Color(240, 255, 240));
    }

    private void updateStats() {
        int remaining = studyQueue.size();
        lblStatTotal.setText(String.valueOf(totalInSession));
        lblStatLearned.setText(String.valueOf(learnedCount));
        lblStatRemaining.setText(String.valueOf(remaining));
        statBarChart.setValues(learnedCount, remaining);
    }

    private JPanel createRightStatsSidebar() {
        JPanel right = new JPanel();
        right.setPreferredSize(new Dimension(280, 0));
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(new EmptyBorder(0, 10, 0, 0));

        JPanel p2 = createWhiteWidget("Trạng thái học tập");
        statBarChart = new StatBarChart();
        p2.add(statBarChart);
        p2.add(Box.createVerticalStrut(20));

        lblStatLearned = new JLabel("0");
        lblStatRemaining = new JLabel("0");
        lblStatTotal = new JLabel("0");

        p2.add(createStatRow("🟢 Đã thuộc:", lblStatLearned));
        p2.add(Box.createVerticalStrut(10));
        p2.add(createStatRow("🔴 Cần học lại:", lblStatRemaining));
        p2.add(Box.createVerticalStrut(10));
        p2.add(createStatRow("⚪ Tổng số từ:", lblStatTotal));

        p2.add(Box.createVerticalStrut(25));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        JButton btnReset = new JButton("Học lại");
        btnReset.setPreferredSize(new Dimension(90, 35));
        btnReset.setBackground(new Color(240, 240, 240));
        btnReset.setFocusPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> {
            int res = JOptionPane.showConfirmDialog(this, "Học lại bộ thẻ này từ đầu?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if(res == JOptionPane.YES_OPTION) loadVocabBySelectedTopic();
        });

        JButton btnExit = new JButton("Thoát");
        btnExit.setPreferredSize(new Dimension(90, 35));
        btnExit.setBackground(new Color(255, 235, 235));
        btnExit.setForeground(Color.RED);
        btnExit.setFocusPainted(false);
        btnExit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExit.addActionListener(e -> setVisible(false));

        buttonPanel.add(btnReset); buttonPanel.add(btnExit);
        p2.add(buttonPanel);
        right.add(p2);
        return right;
    }

    class StatBarChart extends JComponent {
        private int learned, remaining;
        public StatBarChart() { setPreferredSize(new Dimension(200, 40)); }
        public void setValues(int l, int r) { this.learned = l; this.remaining = r; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            int total = learned + remaining;
            if (total == 0) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int learnedW = (learned * w) / total;
            g2.setColor(new Color(46, 204, 113));
            g2.fillRoundRect(0, 10, learnedW, 20, 10, 10);
            g2.setColor(new Color(255, 121, 121));
            g2.fillRoundRect(learnedW + 2, 10, Math.max(0, w - learnedW - 2), 20, 10, 10);
        }
    }

    private JPanel createStatRow(String title, JLabel valLbl) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(new JLabel(title), BorderLayout.WEST);
        valLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        row.add(valLbl, BorderLayout.EAST);
        return row;
    }

    private JPanel createWhiteWidget(String title) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(15, 15, 15, 15));
        p.setOpaque(false);
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lbl); p.add(Box.createVerticalStrut(15));
        return p;
    }

    private JButton createRatingBtn(String t, String s, Color bg, Color tc) {
        JButton b = new RoundButton("<html><center><b>"+t+"</b><br><small>"+s+"</small></center></html>");
        b.setPreferredSize(new Dimension(150, 65));
        b.setBackground(bg); b.setForeground(tc);
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        return b;
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
    }

    // =========================================================================
    // 🌟 KHU VỰC CÁC HÀM XỬ LÝ ẢNH CACHE VÀ ĐA LUỒNG NGẦM (THÂN THIỆN HỆ THỐNG) 🌟
    // =========================================================================

    // Hàm tải ảnh từ link mạng lưu thẳng thành file vật lý trong ổ cứng máy tính
    private void downloadImageLocal(String urlString, String fileName) {
        try {
            // Tự động tạo thư mục src/img/cache nếu trên máy chưa có sẵn
            java.io.File dir = new java.io.File("img/cache/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Kiểm tra xem ảnh này trước đó đã được tải về máy chưa, nếu có rồi thì bỏ qua không tải lại
            java.io.File destinationFile = new java.io.File(dir, fileName);
            if (destinationFile.exists()) return;

            java.net.URL url = new java.net.URL(urlString);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();

            // Bộ Header vượt tường lửa chuẩn cấu hình Chrome browser chống lỗi chặn HTTP 403
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            conn.setRequestProperty("Accept", "image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");

            conn.connect();

            if (conn.getResponseCode() == java.net.HttpURLConnection.HTTP_OK) {
                try (java.io.InputStream in = conn.getInputStream();
                     java.io.FileOutputStream out = new java.io.FileOutputStream(destinationFile)) {

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

                // Sau khi tải xong 1 file ảnh vật lý, lập tức báo cho luồng UI render giao diện thời gian thực
                SwingUtilities.invokeLater(() -> {
                    flashcard.revalidate();
                    flashcard.repaint();
                });
            }
        } catch (Exception e) {
            System.out.println("Lỗi tải file ảnh ngầm: " + e.getMessage());
        }
    }

    // Hàm bốc dữ liệu và quản lý kích hoạt đa luồng tải ảnh bất đồng bộ độc quyền
    private void loadVocabBySelectedTopic() {
        String selectedName = (String) cbTopics.getSelectedItem();
        int topicId = -1;
        if (topicMap != null) {
            for (Map.Entry<Integer, String> entry : topicMap.entrySet()) {
                if (entry.getValue().equals(selectedName)) {
                    topicId = entry.getKey();
                    break;
                }
            }
        }
        if (topicId != -1) {
            List<Vocabulary> list = vocabDAO.getByTopicId(topicId);
            studyQueue = new LinkedList<>(list);
            totalInSession = list.size();
            learnedCount = 0;

            // 🌟 KHỞI CHẠY LUỒNG NGẦM TẢI TRƯỚC TOÀN BỘ ẢNH KHI CHỌN TOPIC (ĐÃ THÊM DELAY AN TOÀN)
            new Thread(() -> {
                for (Vocabulary v : list) {
                    if (v.getImagePath() != null && v.getImagePath().startsWith("http")) {
                        // Tải ảnh về đặt tên là: img_MãTừVựng.png
                        downloadImageLocal(v.getImagePath(), "img_" + v.getWordID() + ".png");

                        // 🌟 MẸO UX: Nghỉ 300 mili-giây trước khi tải từ tiếp theo để tránh bị Server chặn ngầm
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                // Sau khi quét và tải xong xuôi, ép giao diện hiển thị tươi mới lại lần nữa
                SwingUtilities.invokeLater(() -> updateUIWithCard());
            }).start();

            updateUIWithCard();
        }
    }
}