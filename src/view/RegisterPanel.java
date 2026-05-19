package view;

import javax.swing.*; // Nhớ import thư viện này
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// SỬA: Đổi từ Panel thành JPanel
public class RegisterPanel extends JPanel {
    private JTextField txtUser, txtEmail;
    private JPasswordField txtPass;
    private JButton btnSignUp;
    private LoginView parentFrame; // Đổi kiểu dữ liệu từ JFrame thành LoginView cụ thể

    public RegisterPanel(LoginView parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setBackground(Color.WHITE);
        // Kích thước này phải khớp với JFrame chính
        setBounds(0, 0, 850, 500);

        // --- PHẦN BÊN TRÁI: HÌNH ẢNH (Giữ nguyên code của bạn) ---
        JPanel leftPanel = new JPanel(null);
        leftPanel.setBounds(0, 0, 400, 500);
        // ... (Code hiển thị ảnh anh1.jpg) ...
        JLabel backgroundLabel = new JLabel();
        backgroundLabel.setBounds(0, 0, 400, 500);
        backgroundLabel.setLayout(new GridBagLayout());
        ImageIcon icon = new ImageIcon("src/img/anh1.jpg");
        Image img = icon.getImage().getScaledInstance(400, 500, Image.SCALE_SMOOTH);
        backgroundLabel.setIcon(new ImageIcon(img));

        JLabel lblSlogan = new JLabel("<html><center>Join Us Today<br><small>Start your journey</small></center></html>");
        lblSlogan.setFont(new Font("Serif", Font.BOLD, 25));
        lblSlogan.setForeground(Color.WHITE);
        backgroundLabel.add(lblSlogan);
        leftPanel.add(backgroundLabel);

        // --- PHẦN BÊN PHẢI: FORM ---
        JPanel rightPanel = new JPanel(null);
        rightPanel.setBounds(400, 0, 450, 500);
        rightPanel.setBackground(Color.WHITE);
        // ... (Code thêm các ô input) ...
        addInputFields(rightPanel);

        btnSignUp = new JButton("Sign Up");
        btnSignUp.setBounds(50, 350, 350, 45);
        btnSignUp.setBackground(new Color(52, 152, 219));
        btnSignUp.setForeground(Color.WHITE);
        btnSignUp.setBorderPainted(false);
        rightPanel.add(btnSignUp);

        // Thêm vào trong Constructor của RegisterPanel
        btnSignUp.addActionListener(e -> {
            String user = txtUser.getText().trim();
            String email = txtEmail.getText().trim();
            String pass = new String(txtPass.getPassword());

            // 1. Kiểm tra trống
            if (user.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            // 2. Gọi DAO để lưu
            dao.UserDAO userDAO = new dao.UserDAO();
            if (userDAO.registerUser(user, email, pass)) {
                JOptionPane.showMessageDialog(this, "Đăng ký thành công!");
                // Đăng ký xong thì quay lại màn hình Login
                parentFrame.showLoginUI();
            } else {
                JOptionPane.showMessageDialog(this, "Đăng ký thất bại! Tên đăng nhập có thể đã tồn tại.");
            }
        });

        // LIÊN KẾT QUAY LẠI LOGIN
        JLabel lblBack = new JLabel("<html>Already have an account? <span style='color:#3498db; font-weight:bold;'>Login</span></html>");
        lblBack.setBounds(130, 430, 250, 20);
        lblBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // SỬA: Gọi lại hàm hiển thị giao diện Login trong JFrame chính
                parentFrame.showLoginUI();
            }
        });
        rightPanel.add(lblBack);

        add(leftPanel);
        add(rightPanel);
    }

    // (Giữ nguyên hàm addInputFields bên dưới của bạn)
    private void addInputFields(JPanel panel) {
        JLabel lblUser = new JLabel("Username");
        lblUser.setBounds(50, 100, 100, 20);
        panel.add(lblUser);
        txtUser = new JTextField();
        txtUser.setBounds(50, 125, 350, 30);
        txtUser.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        panel.add(txtUser);

        JLabel lblEmail = new JLabel("Email");
        lblEmail.setBounds(50, 175, 100, 20);
        panel.add(lblEmail);
        txtEmail = new JTextField();
        txtEmail.setBounds(50, 200, 350, 30);
        txtEmail.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        panel.add(txtEmail);

        JLabel lblPass = new JLabel("Password");
        lblPass.setBounds(50, 250, 100, 20);
        panel.add(lblPass);
        txtPass = new JPasswordField();
        txtPass.setBounds(50, 275, 350, 30);
        txtPass.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        panel.add(txtPass);
    }


}