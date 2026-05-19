package view;

import dao.LearningProgressDAO;
import model.User;
import utils.RoundButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AccountPanel extends JPanel {
    private Color purpleMain = new Color(108, 92, 231);
    private Color bgMain = new Color(248, 249, 253);
    private Color redLogout = new Color(225, 112, 85);

    private statisticPanel statisticSection;
    // 1. Khai báo bảng (nhớ trỏ đúng vào cái JTable Hoa đã tạo ở giao diện)
    private JTable tableLichSu;

    // 2. Khai báo DAO để lấy dữ liệu
    private LearningProgressDAO lpDAO = new LearningProgressDAO();

    // 3. Đảm bảo có userId (lấy từ User login)
    private int userId ;

    public AccountPanel(User user) {
        this.userId = user.getUserID();

        setLayout(new BorderLayout(0, 20));
        setBackground(bgMain);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- PHẦN 1: HEADER THÔNG TIN TÀI KHOẢN ---
        add(createProfileHeader(user), BorderLayout.NORTH);

        // --- PHẦN 2: VÙNG THỐNG KÊ CHI TIẾT ---
        statisticSection = new statisticPanel();
        statisticSection.refreshData(user.getUserID());
        add(statisticSection, BorderLayout.CENTER);

        // --- PHẦN 3: NÚT ĐĂNG XUẤT (GÓC DƯỚI PHẢI) ---
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);

        JButton btnLogout = new RoundButton("Đăng xuất");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(redLogout);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setPreferredSize(new Dimension(120, 35));

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // 1. Tìm và đóng Frame hiện tại (MainDashboard)
                Window ancestor = SwingUtilities.getWindowAncestor(this);
                if (ancestor != null) {
                    ancestor.dispose();
                }

                // 2. Mở lại màn hình Login (Hoa kiểm tra tên class Login của mình nhé)
                // Giả sử class login của bạn là LoginView
                new LoginView().setVisible(true);
            }
        });

        footer.add(btnLogout);
        return footer;
    }

    private JPanel createProfileHeader(User user) {
        JPanel header = new JPanel(new BorderLayout(25, 0));
        header.setOpaque(false);

        // Avatar hình tròn
        String initial = String.valueOf(user.getUsername().charAt(0)).toUpperCase();
        JLabel lblAvatar = new JLabel(initial, JLabel.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(purpleMain);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblAvatar.setPreferredSize(new Dimension(100, 100));
        lblAvatar.setForeground(Color.WHITE);
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 40));

        // Thông tin chữ
        JPanel infoText = new JPanel(new GridLayout(3, 1, 0, 5));
        infoText.setOpaque(false);

        JLabel lblName = new JLabel(user.getUsername());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JLabel lblEmail = new JLabel((user.getEmail() != null ? user.getEmail() : "Chưa cập nhật email"));
        lblEmail.setForeground(Color.GRAY);
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        String roleText = (user.getRoleID() == 1) ? "Quản trị viên" : "Thành viên học tập";
        JLabel lblRole = new JLabel(roleText);
        lblRole.setForeground(purpleMain);
        lblRole.setFont(new Font("Segoe UI", Font.ITALIC, 14));

        infoText.add(lblName);
        infoText.add(lblEmail);
        infoText.add(lblRole);

        header.add(lblAvatar, BorderLayout.WEST);
        header.add(infoText, BorderLayout.CENTER);

        return header;
    }

    public void loadHistoryTable(int userId) {
        // Thay vì tự làm, AccountPanel sẽ bảo statisticSection cập nhật dữ liệu
        if (statisticSection != null) {
            statisticSection.refreshData(userId);
            System.out.println("Đã yêu cầu statisticSection cập nhật lịch sử cho User: " + userId);
        }
    }
}