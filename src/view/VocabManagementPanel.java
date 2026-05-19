package view;

import dao.VocabularyDAO;
import utils.RoundButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class VocabManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private VocabularyDAO vocabDAO = new VocabularyDAO();

    private JLabel lblTotalVocab, lblNewToday;
    private Color purpleMain = new Color(108, 92, 231);
    private Color bgMain = new Color(248, 249, 253);

    public VocabManagementPanel() {
        setLayout(new BorderLayout(0, 25));
        setBackground(bgMain);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- 1. PHẦN THỐNG KÊ (CARDS) ---
        add(createStatCards(), BorderLayout.NORTH);

        // --- 2. PHẦN BẢNG VÀ CÔNG CỤ ---
        add(createMainContent(), BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createStatCards() {
        JPanel container = new JPanel(new GridLayout(1, 3, 20, 0));
        container.setOpaque(false);

        // Card 1: Tổng số từ
        lblTotalVocab = new JLabel("0");
        container.add(createCard("Tổng số từ vựng", lblTotalVocab, new Color(9, 132, 227)));

        // Card 2: Từ mới hôm nay
        lblNewToday = new JLabel("0");
        container.add(createCard("Từ mới hôm nay", lblNewToday, new Color(0, 184, 148)));

        // Card 3: Chủ đề đa dạng nhất
        container.add(createCard("Hệ thống", new JLabel("Ổn định"), new Color(108, 92, 231)));

        return container;
    }

    private JPanel createCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(Color.GRAY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setOpaque(false);

        // --- THANH CÔNG CỤ (SEARCH + BUTTONS) ---
        JPanel toolBar = new JPanel(new BorderLayout());
        toolBar.setOpaque(false);

        // Bên trái: Ô tìm kiếm
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchBox.setOpaque(false);

        JTextField txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(300, 35));
        // Gợi ý nhỏ: Hoa có thể dùng FlatLaf để có placeholder "Tìm kiếm từ vựng..."
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Sự kiện tìm kiếm thời gian thực
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String query = txtSearch.getText().trim();
                updateTableBySearch(query);
            }
        });

        searchBox.add(new JLabel("🔍  "));
        searchBox.add(txtSearch);

        // Bên phải: Các nút thao tác
        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonGroup.setOpaque(false);

        JButton btnAdd = new RoundButton("+ Thêm mới");
        JButton btnEdit = new RoundButton("Sửa");
        JButton btnDelete = new RoundButton("Xóa");

        styleButton(btnAdd, new Color(0, 184, 148));
        styleButton(btnEdit, new Color(9, 132, 227));
        styleButton(btnDelete, new Color(225, 112, 85));

        btnAdd.addActionListener(e -> openAddDialog());
        btnEdit.addActionListener(e -> openEditDialog());
        btnDelete.addActionListener(e -> deleteAction());

        buttonGroup.add(btnAdd);
        buttonGroup.add(btnEdit);
        buttonGroup.add(btnDelete);

        toolBar.add(searchBox, BorderLayout.WEST);
        toolBar.add(buttonGroup, BorderLayout.EAST);

        // --- BẢNG DỮ LIỆU ---
        String[] columns = {"ID", "Từ vựng", "Phiên âm", "Nghĩa", "Chủ đề"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(40);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        content.add(toolBar, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);

        return content;
    }

    // Hàm phụ để cập nhật bảng khi tìm kiếm
    private void updateTableBySearch(String keyword) {
        tableModel.setRowCount(0);
        List<Object[]> data = vocabDAO.searchVocab(keyword);
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
    }

    public void refreshData() {
        // 1. Cập nhật tổng số từ
        int total = vocabDAO.getAll().size();
        lblTotalVocab.setText(String.valueOf(total));

        // 2. Cập nhật số từ mới hôm nay (SỬA CHỖ NÀY)
        int countToday = vocabDAO.getNewVocabToday(); // Gọi hàm vừa tạo ở Bước 1
        lblNewToday.setText(String.valueOf(countToday));

        // 3. Cập nhật lại bảng dữ liệu
        tableModel.setRowCount(0);
        List<Object[]> data = vocabDAO.getAllVocabForAdmin();
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
    }

    private void deleteAction() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Chọn một từ để xóa!");
            return;
        }
        String id = tableModel.getValueAt(row, 0).toString();
        if (vocabDAO.deleteVocab(id)) {
            JOptionPane.showMessageDialog(this, "Đã xóa!");
            refreshData();
        }
    }

    private void openAddDialog() {
        // 1. Lấy Frame cha từ Panel hiện tại
        Window parentWindow = SwingUtilities.getWindowAncestor(this);

        // 2. Gọi Dialog từ file bên ngoài
        AddVocabDialog dialog = new AddVocabDialog((Frame) parentWindow);
        dialog.setVisible(true);

        // 3. Nếu người dùng lưu thành công, tự động làm mới bảng và thống kê
        if (dialog.isSuccess()) {
            refreshData();
        }
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(100, 32));
    }

    // Hàm hỗ trợ tạo TextField có Title border cho đẹp
    private JTextField createStyledField(String title) {
        JTextField field = new JTextField();
        field.setBorder(BorderFactory.createTitledBorder(title));
        return field;
    }
    private void openEditDialog() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một từ vựng để sửa!");
            return;
        }

        // Lấy ID từ dòng đang chọn (Cột 0)
        int wordId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());

        // Mở Dialog Sửa (Sử dụng chung hoặc riêng file với AddVocabDialog)
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        // Ở đây mình giả định Hoa tạo một class EditVocabDialog
        EditVocabDialog dialog = new EditVocabDialog((Frame) parentWindow, wordId);
        dialog.setVisible(true);

        if (dialog.isSuccess()) {
            refreshData(); // Lưu xong thì nạp lại bảng
        }
    }

}