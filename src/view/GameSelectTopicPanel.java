package view;

import model.User;
import dao.TopicDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class GameSelectTopicPanel extends JPanel {
    private JComboBox<String> cbTopics;
    private Map<Integer, String> topicMap; // Lưu cặp (topicID, topicName) bốc từ DB
    private JPanel mainContent;
    private CardLayout cardLayout;

    public GameSelectTopicPanel(User currentUser, JPanel mainContent, CardLayout cardLayout) {
        this.mainContent = mainContent;
        this.cardLayout = cardLayout;

        setLayout(new GridBagLayout());
        setBackground(new Color(248, 249, 253));
        setBorder(new EmptyBorder(40, 40, 40, 40));

        // Khung Panel bo tròn trung tâm
        JPanel centerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(new EmptyBorder(40, 50, 40, 50));
        centerPanel.setOpaque(false);
        centerPanel.setPreferredSize(new Dimension(500, 300));

        // 1. Tiêu đề
        JLabel lblTitle = new JLabel("THỬ THÁCH TỪ VỰNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(108, 92, 231));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblTitle);
        centerPanel.add(Box.createVerticalStrut(10));

        JLabel lblSub = new JLabel("Vui lòng lựa chọn bộ thẻ bạn muốn ôn tập", JLabel.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblSub.setForeground(Color.GRAY);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(lblSub);
        centerPanel.add(Box.createVerticalStrut(30));

        // 2. ComboBox đổ dữ liệu động từ ID người dùng
        cbTopics = new JComboBox<>();
        cbTopics.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cbTopics.setMaximumSize(new Dimension(400, 40));
        cbTopics.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Gọi DAO lấy danh sách bộ thẻ của User đăng nhập
        try {
            TopicDAO topicDAO = new TopicDAO();
            topicMap = topicDAO.getTopicsByUserId(currentUser.getUserID());

            if (topicMap != null && !topicMap.isEmpty()) {
                for (String topicName : topicMap.values()) {
                    cbTopics.addItem(topicName);
                }
            } else {
                cbTopics.addItem("-- Bạn chưa có bộ thẻ nào --");
            }
        } catch (Exception e) {
            cbTopics.addItem("-- Lỗi kết nối dữ liệu --");
        }
        centerPanel.add(cbTopics);
        centerPanel.add(Box.createVerticalStrut(35));

        // 3. Nút bấm Bắt đầu chơi
        JButton btnStart = new JButton("Bắt đầu chơi ngay") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnStart.setBackground(new Color(108, 92, 231));
        btnStart.setForeground(Color.WHITE);
        btnStart.setContentAreaFilled(false);
        btnStart.setFocusPainted(false);
        btnStart.setBorderPainted(false);
        btnStart.setMaximumSize(new Dimension(400, 45));
        btnStart.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnStart.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hiệu ứng hover cho nút
        btnStart.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btnStart.setBackground(new Color(90, 74, 219)); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { btnStart.setBackground(new Color(108, 92, 231)); }
        });

        // Xử lý sự kiện click để vào game
        btnStart.addActionListener(e -> {
            String selectedName = (String) cbTopics.getSelectedItem();
            if (selectedName == null || selectedName.startsWith("--")) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một bộ thẻ hợp lệ để chơi!");
                return;
            }

            // Tìm ngược lại topicID dựa vào tên bộ thẻ được chọn
            int selectedTopicId = -1;
            for (Map.Entry<Integer, String> entry : topicMap.entrySet()) {
                if (entry.getValue().equals(selectedName)) {
                    selectedTopicId = entry.getKey();
                    break;
                }
            }

            if (selectedTopicId != -1) {
                // =========================================================================
                // 🛠️ ĐOẠN ĐÃ SỬA: Truyền thêm mainContent và cardLayout vào hàm dựng mới
                // =========================================================================
                GameMatchPanel gameMatchPanel = new GameMatchPanel(selectedTopicId, mainContent, cardLayout);

                // Nạp đè vào mainContent của Dashboard và lật màn hình sang chơi game
                mainContent.add(gameMatchPanel, "GAME_PLAY_SCREEN");
                cardLayout.show(mainContent, "GAME_PLAY_SCREEN");
            }
        });

        centerPanel.add(btnStart);
        add(centerPanel); // Đẩy vào tâm màn hình GridBagLayout
    }
}