package view;

import dao.UserDAO;
import model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import model.User;
import java.util.List;
import java.util.ArrayList;

public class LoginView extends JFrame {

    private JTextField txtUser;
    private JPasswordField txtPass;

    public LoginView() {
        setTitle("VocaLearn - Login");
        setSize(850, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Cấu trúc bo góc cho JFrame
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 850, 500, 30, 30));

        showLoginUI();
    }

    public void showLoginUI() {
        JPanel loginMainPanel = new JPanel(null);
        loginMainPanel.setBackground(Color.WHITE);
        loginMainPanel.setBounds(0, 0, 850, 500);

        // --- PHẦN BÊN TRÁI: HÌNH ẢNH ---
        JPanel leftPanel = new JPanel(null);
        leftPanel.setBounds(0, 0, 400, 500);
        JLabel backgroundLabel = new JLabel();
        backgroundLabel.setBounds(0, 0, 400, 500);

        // Load ảnh nền (Hoa nhớ kiểm tra đúng đường dẫn src/img/anh1.jpg nhé)
        try {
            String imagePath = "src/img/anh1.jpg";
            ImageIcon logo = new ImageIcon(imagePath);
            Image img = logo.getImage().getScaledInstance(400, 500, Image.SCALE_SMOOTH);
            backgroundLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            backgroundLabel.setBackground(new Color(108, 92, 231));
            backgroundLabel.setOpaque(true);
        }

        JLabel lblSlogan = new JLabel("<html><center>Smart Vocabulary<br>Learner<br><small style='font-weight:normal; font-size:12px;'>Memories of yesterday for tomorrow</small></center></html>");
        lblSlogan.setFont(new Font("Serif", Font.BOLD, 28));
        lblSlogan.setForeground(Color.WHITE);
        lblSlogan.setBounds(0, 150, 400, 200);
        lblSlogan.setHorizontalAlignment(JLabel.CENTER);

        leftPanel.add(lblSlogan);
        leftPanel.add(backgroundLabel);

        // --- PHẦN BÊN PHẢI: FORM ĐĂNG NHẬP ---
        JPanel rightPanel = new JPanel(null);
        rightPanel.setBounds(400, 0, 450, 500);
        rightPanel.setBackground(Color.WHITE);

        // Nút X để thoát ứng dụng nhanh
        JButton btnClose = new JButton("✕");
        btnClose.setBounds(410, 10, 30, 30);
        btnClose.setFont(new Font("Arial", Font.BOLD, 16));
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> System.exit(0));
        rightPanel.add(btnClose);

        JLabel lblTitle = new JLabel("Welcome Back!");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 26));
        lblTitle.setBounds(50, 60, 300, 40);
        rightPanel.add(lblTitle);

        // Ô nhập Username
        JLabel lblUser = new JLabel("Username");
        lblUser.setBounds(50, 120, 100, 20);
        lblUser.setForeground(new Color(41, 128, 185));
        rightPanel.add(lblUser);

        txtUser = new JTextField();
        txtUser.setBounds(50, 145, 350, 35);
        txtUser.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        txtUser.setFont(new Font("SansSerif", Font.PLAIN, 14));
        rightPanel.add(txtUser);

        // Ô nhập Password
        JLabel lblPass = new JLabel("Password");
        lblPass.setBounds(50, 200, 100, 20);
        lblPass.setForeground(new Color(41, 128, 185));
        rightPanel.add(lblPass);

        txtPass = new JPasswordField();
        txtPass.setBounds(50, 225, 350, 35);
        txtPass.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        rightPanel.add(txtPass);

        // Nút Sign In
        // --- Nút Sign In (Đã sửa lỗi màu) ---
        JButton btnSignIn = new JButton("Sign In");
        btnSignIn.setBounds(50, 310, 350, 45);
        btnSignIn.setBackground(new Color(52, 152, 219)); // Màu xanh đậm chuẩn
        btnSignIn.setForeground(Color.WHITE);
        btnSignIn.setFont(new Font("SansSerif", Font.BOLD, 16));

        // Hai dòng cực kỳ quan trọng để hiện đúng màu trên Windows/MacOS
        btnSignIn.setContentAreaFilled(true);
        btnSignIn.setOpaque(true);

        btnSignIn.setBorderPainted(false); // Xóa viền mặc định
        btnSignIn.setFocusPainted(false);  // Xóa viền xanh khi nhấn vào
        btnSignIn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hiệu ứng đổi màu nhẹ khi di chuột vào (Hover)
        btnSignIn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnSignIn.setBackground(new Color(41, 128, 185)); // Xanh đậm hơn chút
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnSignIn.setBackground(new Color(52, 152, 219)); // Quay lại màu cũ
            }
        });

        btnSignIn.addActionListener(e -> performLogin());
        rightPanel.add(btnSignIn);

        // Bắt sự kiện phím Enter cho cả 2 ô nhập liệu
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) performLogin();
            }
        };
        txtUser.addKeyListener(enterKey);
        txtPass.addKeyListener(enterKey);

        // Nút chuyển sang Đăng ký
        JLabel lblFooter = new JLabel("<html>If you are not a member, please <span style='color:#3498db; font-weight:bold;'>Sign up </span></html>");
        lblFooter.setBounds(110, 410, 300, 20);
        lblFooter.setFont(new Font("Arial", Font.PLAIN, 12));
        lblFooter.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblFooter.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                switchPanel(new RegisterPanel(LoginView.this));
            }
        });
        rightPanel.add(lblFooter);

        loginMainPanel.add(leftPanel);
        loginMainPanel.add(rightPanel);

        switchPanel(loginMainPanel);
    }

    // Hàm xử lý logic Đăng nhập kết nối với DAO
    // Hàm xử lý logic Đăng nhập kết nối với DAO
    private void performLogin() {
        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
            return;
        }

        UserDAO userDAO = new UserDAO();
        User loggedUser = userDAO.login(username, password);

        if (loggedUser != null) {
            // 2. Cập nhật ngày đăng nhập mới nhất (Dùng loggedUser chứ không phải user)
            userDAO.updateLastLogin(loggedUser.getUserID());

            // --- HOÀN TẤT ĐĂNG NHẬP ---
            JOptionPane.showMessageDialog(this, "Đăng nhập thành công! Chào mừng " + loggedUser.getUsername());

            // Mở Dashboard và truyền dữ liệu người dùng sang
            MainDashboard dashboard = new MainDashboard(loggedUser);
            dashboard.setVisible(true);

            this.dispose(); // Đóng màn hình đăng nhập
        } else {
            JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void switchPanel(JPanel newPanel) {
        this.getContentPane().removeAll();
        this.getContentPane().add(newPanel);
        this.revalidate();
        this.repaint();
    }



    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}

        // Tạo luồng riêng để quét và gửi mail ngay khi bật app (không làm chậm giao diện)
        new Thread(() -> {
            System.out.println("Hệ thống đang quét danh sách người dùng vắng mặt quá 3 ngày...");
            UserDAO dao = new UserDAO();
            List<User> lườiHọc = dao.getListUserToRemind();

            if (!lườiHọc.isEmpty()) {
                // 1. Thực hiện gửi mail hàng loạt qua tiện ích dịch vụ
                utils.EmailService.sendBulkReminder(lườiHọc);
                System.out.println("Đã tiến hành gửi mail nhắc nhở.");

                // 2. DUYỆT QUA DANH SÁCH VÀ ĐÓNG DẤU ĐÃ GỬI LÊN DATABASE NGAY
                // Điều này giúp lần khởi động tiếp theo trong ngày không bị quét lại nữa
                for (User u : lườiHọc) {
                    // Cập nhật ngày gửi email nhắc nhở là ngày hôm nay (Dùng hàm trong UserDAO)
                    dao.updateLastEmailSent(u.getUserID());
                }
                System.out.println("Đã cập nhật trạng thái last_email_sent để chống trùng lặp.");
            } else {
                System.out.println("Không có ai nghỉ học quá 3 ngày hoặc đợt nhắc nhở hôm nay đã được gửi rồi.");
            }
        }).start();

        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}