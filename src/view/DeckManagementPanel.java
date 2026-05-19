package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DeckManagementPanel extends JPanel {

    private Color bgDark = new Color(30, 30, 30); // Màu nền tối
    private Color cardDark = new Color(45, 45, 45); // Màu ô chứa danh sách
    private Color purpleSub = new Color(108, 92, 231);

    public DeckManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(bgDark);
        setBorder(new EmptyBorder(20, 50, 20, 50));

        // --- 1. THANH MENU TRÊN CÙNG ---
        add(createTopMenu(), BorderLayout.NORTH);

        // --- 2. VÙNG HIỂN THỊ DANH SÁCH BỘ THẺ (CENTER) ---
        add(createCenterContent(), BorderLayout.CENTER);

        // --- 3. THANH HÀNH ĐỘNG DƯỚI CÙNG ---
        add(createBottomActions(), BorderLayout.SOUTH);
    }

    private JPanel createTopMenu() {
        JPanel menu = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        menu.setOpaque(false);

        String[] navItems = {"Bộ thẻ", "Thêm", "Sửa", "Xóa", "Nhập tập tin"};
        for (String item : navItems) {
            JLabel lbl = new JLabel(item);
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 15));
            lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
            menu.add(lbl);
        }
        return menu;
    }

    private JPanel createCenterContent() {
        JPanel container = new JPanel(new BorderLayout(0, 20));
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(30, 0, 30, 0));

        // Bảng hiển thị danh sách bộ thẻ
        String[] columns = {"Bộ thẻ", "Mới", "Đang học", "Đến hạn"};
        Object[][] data = {
                {"+ 4000 Essential English Words - Book 1", "20", "0", "2"},
                {"Chinese", "20", "5", "7"},
                {"English", "0", "0", "0"}
        };

        DefaultTableModel model = new DefaultTableModel(data, columns);
        JTable table = new JTable(model);

        // Custom giao diện Table giống hình mẫu
        table.setBackground(cardDark);
        table.setForeground(Color.WHITE);
        table.setRowHeight(40);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setGridColor(new Color(60, 60, 60));
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setBorder(null);

        // Chỉnh Header Table
        table.getTableHeader().setBackground(cardDark);
        table.getTableHeader().setForeground(Color.GRAY);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBorder(null);

        // Căn giữa nội dung các cột số
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(cardDark);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1, true));
        scrollPane.getViewport().setBackground(cardDark);

        container.add(scrollPane, BorderLayout.CENTER);

        // Dòng trạng thái bên dưới bảng
        JLabel lblStatus = new JLabel("Đã học 0 thẻ trong 0 giây hôm nay (0 giây/thẻ)", JLabel.CENTER);
        lblStatus.setForeground(Color.LIGHT_GRAY);
        lblStatus.setFont(new Font("SansSerif", Font.ITALIC, 13));
        container.add(lblStatus, BorderLayout.SOUTH);

        return container;
    }

    private JPanel createBottomActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        p.setOpaque(false);

        p.add(createStyledButton("Lấy bộ thẻ chia sẻ"));
        p.add(createStyledButton("Tạo bộ thẻ"));
        p.add(createStyledButton("Nhập tập tin"));

        return p;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(70, 70, 70));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hiệu ứng hover
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(90, 90, 90)); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(new Color(70, 70, 70)); }
        });

        return btn;
    }
}