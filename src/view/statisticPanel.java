package view;

import dao.VocabularyDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class statisticPanel extends JPanel {

    private Color purpleMain = new Color(108, 92, 231);
    private Color bgMain = new Color(248, 249, 253);

    private VocabularyDAO vocabDAO = new VocabularyDAO();


    private StatCard cardTotal, cardLearned, cardMastered, cardReview;
    private DefaultTableModel tableModel;
    // Thêm dòng này vào phần khai báo biến
    private dao.LearningProgressDAO lpDAO = new dao.LearningProgressDAO();

    public statisticPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(bgMain);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- 1. TIÊU ĐỀ ---
        JLabel lblTitle = new JLabel("Lộ trình & Thống kê học tập");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        add(lblTitle, BorderLayout.NORTH);

        // --- 2. VÙNG TRUNG TÂM ---
        JPanel centerPanel = new JPanel(new BorderLayout(0, 30));
        centerPanel.setOpaque(false);

        // A. Các ô Card thông số
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));;
        cardsPanel.setOpaque(false);

        cardTotal = new StatCard("Tổng từ vựng", "0", purpleMain);
       // cardLearned = new StatCard("Đang học", "0", new Color(46, 204, 113));
        cardMastered = new StatCard("Đã thuộc", "0", new Color(241, 194, 50));
        cardReview = new StatCard("Cần ôn tập", "0", Color.RED);

        cardsPanel.add(cardTotal);
      //  cardsPanel.add(cardLearned);
        cardsPanel.add(cardMastered);
        cardsPanel.add(cardReview);
        centerPanel.add(cardsPanel, BorderLayout.NORTH);

        // B. Bảng lịch sử Quiz
        centerPanel.add(createScoreHistoryPanel(), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createScoreHistoryPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 15));
        p.setOpaque(false);

        JLabel lblTableTitle = new JLabel("Lịch sử luyện tập gần đây");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        p.add(lblTableTitle, BorderLayout.NORTH);

        String[] columns = {"STT","Topic", "Ngày thực hiện", "Số câu hỏi", "Điểm số", "Đánh giá"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(40);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(245, 242, 255));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Object.class, centerRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        scroll.getViewport().setBackground(Color.WHITE);

        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    /**
     * Nạp dữ liệu từ CSDL dựa trên ID người dùng đang đăng nhập
     */
    public void refreshData(int userId) {
        try {
            // 1. Cập nhật các ô Card thông số
            int total = vocabDAO.countTotalByUserId(userId);
            cardTotal.updateValue(String.valueOf(total));

            int mastered = vocabDAO.countByStatus(userId, "Mastered");
            cardMastered.updateValue(String.valueOf(mastered));

            int review = total - mastered;
            cardReview.updateValue(String.valueOf(Math.max(0, review)));
        } catch (Exception e) {
            System.err.println("Lỗi load thống kê Card: " + e.getMessage());
        }

        // 2. Nạp dữ liệu vào bảng Lịch sử
        tableModel.setRowCount(0);
        List<Object[]> history = lpDAO.getRecentHistory(userId);

        if (history != null) {
            int stt = 1;
            for (Object[] data : history) {
                tableModel.addRow(new Object[]{
                        stt++,      // 0: STT
                        data[0],    // 1: Bộ thẻ (Lấy từ TopicName)
                        data[1],    // 2: Ngày thực hiện
                        data[2],    // 3: Số từ đã học
                        data[3],    // 4: Điểm số
                        data[4]     // 5: Đánh giá
                });
            }
        }
    }

    class StatCard extends JPanel {
        private JLabel lblValue;
        private Color themeColor;

        public StatCard(String title, String value, Color color) {
            this.themeColor = color;
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(15, 20, 15, 20));
            setOpaque(false);

            lblValue = new JLabel(value);
            lblValue.setFont(new Font("Segoe UI", Font.BOLD, 30));
            lblValue.setForeground(color);

            JLabel lblTit = new JLabel(title);
            lblTit.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblTit.setForeground(new Color(120, 120, 120));

            add(lblValue, BorderLayout.CENTER);
            add(lblTit, BorderLayout.SOUTH);
        }

        public void updateValue(String newValue) {
            lblValue.setText(newValue);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.setColor(new Color(230, 230, 230));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.setColor(themeColor);
            g2.fillRoundRect(0, 20, 5, getHeight() - 40, 5, 5);
            g2.dispose();
        }
    }
}