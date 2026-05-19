package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import utils.RoundButton;

public class EditUserDialog extends JDialog {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtEmail;
    private JComboBox<String> cbRole;
    private JButton btnSave, btnCancel;
    private boolean success = false;

    // Các biến chứa dữ liệu ban đầu
    private int userId;

    public EditUserDialog(Frame parent, int userId, String username, String email, String role) {
        super(parent, "Sửa thông tin tài khoản ✨", true);
        this.userId = userId;

        setSize(400, 360);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);

        // --- 1. TÊN ĐĂNG NHẬP ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(labelFont);
        mainPanel.add(lblUser, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtUsername = new JTextField(username);
        txtUsername.setFont(inputFont);
        styleTextField(txtUsername);
        mainPanel.add(txtUsername, gbc);

        // --- 2. MẬT KHẨU MỚI ---
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblPass = new JLabel("Mật khẩu mới:");
        lblPass.setFont(labelFont);
        mainPanel.add(lblPass, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtPassword = new JPasswordField();
        txtPassword.setFont(inputFont);
        txtPassword.setToolTipText("Để trống nếu không muốn đổi mật khẩu");
        styleTextField(txtPassword);
        mainPanel.add(txtPassword, gbc);

        // --- 3. EMAIL ---
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(labelFont);
        mainPanel.add(lblEmail, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        txtEmail = new JTextField(email);
        txtEmail.setFont(inputFont);
        styleTextField(txtEmail);
        mainPanel.add(txtEmail, gbc);

        // --- 4. VAI TRÒ (ROLE) ---
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel lblRole = new JLabel("Vai trò:");
        lblRole.setFont(labelFont);
        mainPanel.add(lblRole, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        cbRole = new JComboBox<>(new String[]{"Admin", "User"});
        cbRole.setFont(inputFont);
        cbRole.setSelectedItem(role);
        mainPanel.add(cbRole, gbc);

        // --- 5. NHÓM NÚT BẤM XỬ LÝ ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnPanel.setOpaque(false);

        btnCancel = new RoundButton("Hủy");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.addActionListener(e -> dispose());

        btnSave = new RoundButton("Lưu cấu hình");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setBackground(new Color(108, 92, 231));
        btnSave.setForeground(Color.WHITE);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        add(mainPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        // Đóng gói sự kiện nút lưu
        btnSave.addActionListener(e -> handleSaveData());
    }

    private void handleSaveData() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String email = txtEmail.getText().trim();
        String roleName = cbRole.getSelectedItem().toString();

        if (username.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không được để trống Tên đăng nhập và Email!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ánh xạ chuỗi hiển thị thành mã Quyền tương ứng trong CSDL của bạn
        int roleId = roleName.equals("Admin") ? 1 : 2;

        // Gọi UserDAO để thực thi cập nhật dữ liệu xuống SQL
        dao.UserDAO dao = new dao.UserDAO();

        // Thao tác logic sửa đổi: Bạn cần chuẩn bị hàm này trong UserDAO nhé
        boolean isUpdated = dao.updateUserByAdmin(userId, username, password, email, roleId);

        if (isUpdated) {
            success = true;
            JOptionPane.showMessageDialog(this, "Cập nhật tài khoản thành công!");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi: Không thể cập nhật thông tin dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSuccess() {
        return success;
    }

    private void styleTextField(JTextField tf) {
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 223, 230), 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }
}