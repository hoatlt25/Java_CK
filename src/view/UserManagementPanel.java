package view;

import dao.UserDAO;
import utils.RoundButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private UserDAO userDAO = new UserDAO();

    private JLabel lblTotalUsers;
    private Color purpleMain = new Color(108, 92, 231);
    private Color bgMain = new Color(248, 249, 253);

    public UserManagementPanel() {
        setLayout(new BorderLayout(0, 25));
        setBackground(bgMain);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- 1. THÔNG TIN TỔNG QUÁT ---
        add(createSummaryHeader(), BorderLayout.NORTH);

        // --- 2. BẢNG QUẢN LÝ ---
        add(createMainContent(), BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createSummaryHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Quản lý tài khoản");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));

        lblTotalUsers = new JLabel("Tổng cộng: 0 người dùng");
        lblTotalUsers.setForeground(Color.GRAY);
        lblTotalUsers.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JPanel leftGroup = new JPanel(new GridLayout(2, 1));
        leftGroup.setOpaque(false);
        leftGroup.add(lblTitle);
        leftGroup.add(lblTotalUsers);

        header.add(leftGroup, BorderLayout.WEST);
        return header;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setOpaque(false);

        // Thanh công cụ
        JPanel toolBar = new JPanel(new BorderLayout());
        toolBar.setOpaque(false);

        // Tìm kiếm
        JTextField txtSearch = new JTextField(" Tìm kiếm người dùng...");
        txtSearch.setPreferredSize(new Dimension(300, 35));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                // Tích hợp hàm search ở đây
            }
        });

        // Nhóm nút
        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnGroup.setOpaque(false);

        JButton btnResetPass = new RoundButton("Sửa tài khoản");
        JButton btnDelete = new RoundButton("Khóa tài khoản");
        styleButton(btnResetPass, new Color(9, 132, 227));
        styleButton(btnDelete, new Color(214, 48, 49));

        btnDelete.addActionListener(e -> handleDeleteUser());
        btnResetPass.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng trên bảng để sửa!");
                return;
            }

            // Bóc tách các dữ liệu thô từ hàng đang chọn trên JTable công khai
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String username = tableModel.getValueAt(selectedRow, 1).toString();
            String email = tableModel.getValueAt(selectedRow, 2).toString();
            String role = tableModel.getValueAt(selectedRow, 3).toString();

            // Tìm Frame cha gốc để gán Anchor tọa độ cho Dialog phụ
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

            // Khởi tạo cửa sổ sửa đổi thông tin
            EditUserDialog dialog = new EditUserDialog(topFrame, id, username, email, role);
            dialog.setVisible(true);

            // Nếu lưu dữ liệu thành công dưới CSDL, làm mới lại danh sách bảng
            if (dialog.isSuccess()) {
                refreshData();
            }
        });

        btnGroup.add(btnResetPass);
        btnGroup.add(btnDelete);

        toolBar.add(txtSearch, BorderLayout.WEST);
        toolBar.add(btnGroup, BorderLayout.EAST);

        // Bảng dữ liệu
        String[] columns = {"ID", "Tên người dùng", "Email", "Vai trò"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Căn giữa ID
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        content.add(toolBar, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);

        return content;
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<Object[]> users = userDAO.getAllUsersForAdmin();
        for (Object[] row : users) {
            tableModel.addRow(row);
        }
        lblTotalUsers.setText("Tổng cộng: " + users.size() + " người dùng");
    }

    private void handleDeleteUser() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Chọn người dùng cần xóa!");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String name = tableModel.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận xóa tài khoản: " + name + "?", "Cảnh báo", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (userDAO.deleteUser(id)) {
                JOptionPane.showMessageDialog(this, "Đã xóa!");
                refreshData();
            }
        }
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(130, 35));
    }
}