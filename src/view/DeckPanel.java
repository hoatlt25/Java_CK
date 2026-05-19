package view;

import dao.VocabularyDAO;
import dao.TopicDAO;
import model.User;
import model.Vocabulary;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utils.RoundButton;

public class DeckPanel extends JPanel {
    private CardLayout cardLayout = new CardLayout();
    private JPanel contentPanel = new JPanel(cardLayout);
    private VocabularyDAO vocabDAO = new VocabularyDAO();
    private TopicDAO topicDAO = new TopicDAO();

    private JTable wordTable;
    private DefaultTableModel tableModel;
    private int currentTopicId = -1;
    private String currentTopicName = "";
    private JPanel topicGrid;

    // Biến hỗ trợ chọn bộ thẻ để sửa/xóa
    private int selectedTopicIdForAction = -1;
    private JPanel lastSelectedCard = null;
    private User currentUser;

    private final Color PURPLE_PRIMARY = new Color(108, 92, 231);
    private final Color WHITE = Color.WHITE;
    private final Color BG_LIGHT = new Color(245, 246, 250);
    private final Color TEXT_COLOR = new Color(45, 52, 54);

    public DeckPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(BG_LIGHT);

        contentPanel.setOpaque(false);
        contentPanel.add(createTopicSelectionView(), "TOPIC_LIST");
        contentPanel.add(createWordManagementView(), "WORD_MGMT");

        add(contentPanel, BorderLayout.CENTER);
    }

    // =========================================================================
    // VIEW 1: QUẢN LÝ BỘ THẺ (4 THẺ / HÀNG)
    // =========================================================================
    private JPanel createTopicSelectionView() {
        JPanel mainP = new JPanel(new BorderLayout());
        mainP.setBackground(BG_LIGHT);

        JPanel topicToolBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));
        topicToolBar.setOpaque(false);

        JButton btnAddT = createStyledButton("Tạo bộ thẻ", PURPLE_PRIMARY, true);
        JButton btnEditT = createStyledButton("Sửa tên bộ", new Color(9, 132, 227), true);
        JButton btnDelT = createStyledButton("Xóa bộ thẻ", new Color(214, 48, 49), true);

        btnAddT.addActionListener(e -> handleAddTopic());
        btnEditT.addActionListener(e -> handleEditTopic());
        btnDelT.addActionListener(e -> handleDeleteTopic());

        topicToolBar.add(btnAddT); topicToolBar.add(btnEditT); topicToolBar.add(btnDelT);
        mainP.add(topicToolBar, BorderLayout.NORTH);

        // Sử dụng GridLayout với 4 cột cố định
        topicGrid = new JPanel(new GridLayout(0, 4, 25, 25));
        topicGrid.setOpaque(false);
        topicGrid.setBorder(new EmptyBorder(20, 50, 40, 50));
        refreshTopicUI();

        // Wrapper để các thẻ không bị kéo giãn theo chiều dọc
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(topicGrid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        mainP.add(scroll, BorderLayout.CENTER);

        return mainP;
    }

    private void refreshTopicUI() {
        topicGrid.removeAll();
        // Truyền id của user hiện tại vào hàm lấy dữ liệu
        Map<Integer, String> topics = topicDAO.getTopicsByUserId(currentUser.getUserID());
        for (var entry : topics.entrySet()) {
            topicGrid.add(createTopicCard(entry.getKey(), entry.getValue()));
        }
        topicGrid.revalidate();
        topicGrid.repaint();
    }

    private JPanel createTopicCard(int id, String name) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Vẽ nền bo tròn
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBorder() != null ? new Color(230, 230, 230) : new Color(230, 230, 230));

                if (getBorder() instanceof javax.swing.border.LineBorder) {
                    g2d.setStroke(new BasicStroke(1));
                    g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                }
            }
        };

        card.setPreferredSize(new Dimension(200, 120));
        card.setBackground(WHITE);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true));
        card.setOpaque(false); // Quan trọng: để transparent để vẽ được hình tròn

        JLabel lbl = new JLabel(name);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(TEXT_COLOR);
        card.add(lbl);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    currentTopicId = id;
                    currentTopicName = name;
                    refreshWordTable(id);
                    cardLayout.show(contentPanel, "WORD_MGMT");
                } else {
                    if (lastSelectedCard != null) {
                        lastSelectedCard.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true));
                    }
                    selectedTopicIdForAction = id;
                    currentTopicName = name;
                    lastSelectedCard = card;
                    card.setBorder(BorderFactory.createLineBorder(PURPLE_PRIMARY, 2, true));
                }
            }
            public void mouseEntered(MouseEvent e) {
                if (selectedTopicIdForAction != id) card.setBorder(BorderFactory.createLineBorder(PURPLE_PRIMARY, 1, true));
            }
            public void mouseExited(MouseEvent e) {
                if (selectedTopicIdForAction != id) card.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true));
            }
        });
        return card;
    }

    // =========================================================================
    // VIEW 2: QUẢN LÝ TỪ VỰNG
    // =========================================================================
    private JPanel createWordManagementView() {
        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(30, 50, 30, 50));

        JPanel toolBar = new JPanel(new BorderLayout());
        toolBar.setOpaque(false);

        JButton btnBack = createStyledButton("← Quay lại", new Color(189, 195, 199), false);
        btnBack.addActionListener(e -> cardLayout.show(contentPanel, "TOPIC_LIST"));

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnGroup.setOpaque(false);

        JButton btnAddW = createStyledButton("Thêm từ", PURPLE_PRIMARY, true);
        JButton btnEditW = createStyledButton("Sửa từ", new Color(9, 132, 227), true);
        JButton btnDelW = createStyledButton("Xóa từ", new Color(214, 48, 49), true);
        JButton btnImport = createStyledButton("Nhập Excel", new Color(46, 204, 113), true);

        // --- Nút Thêm từ ---
        btnAddW.addActionListener(e -> {
            // Lấy Frame cha để làm chủ cho Dialog
            Window parent = SwingUtilities.getWindowAncestor(this);
            AddVocabDialog dialog = new AddVocabDialog((Frame) parent);
            dialog.setVisible(true);

            // Nếu lưu thành công thì nạp lại bảng từ vựng
            if (dialog.isSuccess()) {
                refreshWordTable(currentTopicId);
            }
        });

        // --- Nút Sửa từ ---
        btnEditW.addActionListener(e -> {
            int selectedRow = wordTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một hàng để sửa!");
                return;
            }

            // Lấy ID ẩn (wordID) từ tableModel
            int wordId = (int) tableModel.getValueAt(selectedRow, 0);

            // Mở Dialog Sửa
            Window parent = SwingUtilities.getWindowAncestor(this);
            EditVocabDialog dialog = new EditVocabDialog((Frame) parent, wordId);
            dialog.setVisible(true);

            if (dialog.isSuccess()) {
                refreshWordTable(currentTopicId);
            }
        });
        btnDelW.addActionListener(e -> handleDeleteWord());
        btnImport.addActionListener(e -> handleImportExcel());

        btnGroup.add(btnAddW); btnGroup.add(btnEditW); btnGroup.add(btnDelW); btnGroup.add(btnImport);
        toolBar.add(btnBack, BorderLayout.WEST);
        toolBar.add(btnGroup, BorderLayout.EAST);

        String[] columns = {"ID", "Từ vựng", "Từ loại", "Phiên âm" ,"Nghĩa", "Ví dụ"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        wordTable = new JTable(tableModel);
        wordTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        wordTable.removeColumn(wordTable.getColumnModel().getColumn(0)); // Ẩn ID
        wordTable.setRowHeight(45);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i<wordTable.getColumnCount(); i++) wordTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        p.add(toolBar, BorderLayout.NORTH);
        p.add(new JScrollPane(wordTable), BorderLayout.CENTER);
        return p;
    }

    // =========================================================================
    // LOGIC XỬ LÝ TỪ VỰNG
    // =========================================================================


    private void handleImportExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files", "xlsx", "xls"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            List<Vocabulary> list = new ArrayList<>();

            try (FileInputStream fis = new FileInputStream(file); Workbook wb = WorkbookFactory.create(fis)) {
                Sheet sheet = wb.getSheetAt(0);
                DataFormatter fmt = new DataFormatter();

                // Đọc dòng tiêu đề (thường là dòng 0) để định vị cột ngẫu nhiên
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    JOptionPane.showMessageDialog(this, "File Excel trống hoặc không có dòng tiêu đề!");
                    return;
                }

                // Các biến lưu vị trí index của cột (-1 nghĩa là chưa tìm thấy)
                int colWord = -1, colType = -1, colPron = -1, colMean = -1, colExample = -1;

                for (int cellIdx = 0; cellIdx < headerRow.getLastCellNum(); cellIdx++) {
                    String headerVal = fmt.formatCellValue(headerRow.getCell(cellIdx)).trim().toLowerCase();

                    // Hỗ trợ nhận diện thông minh cả tiếng Việt, tiếng Anh và tiếng Trung lộn xộn
                    if (headerVal.contains("từ vựng") || headerVal.contains("word") || headerVal.contains("单词") || headerVal.contains("词")) {
                        colWord = cellIdx;
                    } else if (headerVal.contains("loại từ") || headerVal.contains("từ loại") || headerVal.contains("type") || headerVal.contains("词性")) {
                        colType = cellIdx;
                    } else if (headerVal.contains("phát âm") || headerVal.contains("phiên âm") || headerVal.contains("pronun") || headerVal.contains("拼音")) {
                        colPron = cellIdx;
                    } else if (headerVal.contains("nghĩa") || headerVal.contains("meaning") || headerVal.contains("意思") || headerVal.contains("翻译")) {
                        colMean = cellIdx;
                    } else if (headerVal.contains("ví dụ") || headerVal.contains("example") || headerVal.contains("例句")) {
                        colExample = cellIdx;
                    }
                }

                // Kiểm tra xem đã tìm thấy các cột bắt buộc chưa (Ít nhất phải có Từ vựng và Nghĩa)
                if (colWord == -1 || colMean == -1) {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy cột 'Từ vựng' hoặc 'Nghĩa' trong file Excel!");
                    return;
                }

                // Vòng lặp đọc dữ liệu bắt đầu từ dòng thứ 2 (index 1)
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    // Kiểm tra nếu ô từ vựng bị trống hoàn toàn thì bỏ qua dòng đó
                    String wordStr = (colWord != -1) ? fmt.formatCellValue(row.getCell(colWord)).trim() : "";
                    if (wordStr.isEmpty()) continue;

                    Vocabulary v = new Vocabulary();
                    v.setWord(wordStr);
                    v.setType((colType != -1) ? fmt.formatCellValue(row.getCell(colType)).trim() : "");
                    v.setPronunciation((colPron != -1) ? fmt.formatCellValue(row.getCell(colPron)).trim() : "");
                    v.setMeaning((colMean != -1) ? fmt.formatCellValue(row.getCell(colMean)).trim() : "");
                    v.setExample((colExample != -1) ? fmt.formatCellValue(row.getCell(colExample)).trim() : "");
                    v.setTopicID(currentTopicId);

                    // --- TỰ ĐỘNG NHẬN DIỆN NGÔN NGỮ ĐỂ GÁN ID ---
                    // Nếu từ vựng có chứa ký tự chữ Hán (Regex Unicode), gán languageID = 2 (China)
                    // Ngược lại mặc định gán languageID = 1 (English)
                    if (wordStr.matches(".*[\\u4e00-\\u9fa5].*")) {
                        v.setLanguageID(2);
                    } else {
                        v.setLanguageID(1);
                    }

                    list.add(v);
                }

                if (!list.isEmpty()) {
                    if (vocabDAO.importFromExcel(list)) {
                        refreshWordTable(currentTopicId);
                        JOptionPane.showMessageDialog(this, "Nhập dữ liệu thành công " + list.size() + " từ!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Lỗi: Không thể lưu từ vựng vào Cơ sở dữ liệu.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu hợp lệ để import.");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi đọc file: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    public void refreshWordTable(int id) {
        tableModel.setRowCount(0);
        List<Vocabulary> list = vocabDAO.getByTopicId(id);
        for (Vocabulary v : list) {
            tableModel.addRow(new Object[]{v.getWordID(), v.getWord(), v.getType(),  v.getPronunciation(),v.getMeaning(), v.getExample()});
        }
    }

    // =========================================================================
    // LOGIC BỘ THẺ
    // =========================================================================
    private void handleAddTopic() {
        String n = JOptionPane.showInputDialog(this, "Tên bộ thẻ mới:");

        if (n != null && !n.trim().isEmpty()) {
            // PHẢI truyền thêm currentUser.getId_user() vào đây
            if (topicDAO.insertTopic(n.trim(), currentUser.getUserID())) {
                refreshTopicUI(); // Cập nhật lại giao diện các thẻ
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi: Không thể tạo bộ thẻ vào Database!");
            }
        }
    }

    private void handleEditTopic() {
        if (selectedTopicIdForAction == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 thẻ!"); return;
        }
        String n = JOptionPane.showInputDialog(this, "Tên mới:", currentTopicName);
        if (n != null && !n.trim().isEmpty()) if (topicDAO.updateTopic(selectedTopicIdForAction, n.trim())) refreshTopicUI();
    }

    private void handleDeleteTopic() {
        if (selectedTopicIdForAction == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 thẻ để xóa!"); return;
        }
        if (JOptionPane.showConfirmDialog(this, "Xác nhận xóa bộ thẻ: " + currentTopicName + "?") == 0) {
            if (topicDAO.deleteTopic(selectedTopicIdForAction)) refreshTopicUI();
        }
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================
    private void handleEditWord() {
        int r = wordTable.getSelectedRow();
        if (r != -1) {
            // Lấy ID từ cột ẩn (cột 0)
            int wordId = (int) tableModel.getValueAt(r, 0);

            // Mở file EditVocabDialog đã tách riêng
            Window parent = SwingUtilities.getWindowAncestor(this);
            EditVocabDialog dialog = new EditVocabDialog((Frame) parent, wordId);
            dialog.setVisible(true);

            // Nếu sửa thành công thì load lại bảng
            if (dialog.isSuccess()) {
                refreshWordTable(currentTopicId);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hàng cần sửa!");
        }
    }

    private void handleDeleteWord() {
        // Lấy tất cả các chỉ số dòng đang được chọn/bôi đen trên bảng
        int[] selectedRows = wordTable.getSelectedRows();

        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một hàng để xóa!");
            return;
        }

        // Tạo câu thông báo động tùy thuộc vào số lượng từ được chọn
        String confirmMsg = (selectedRows.length == 1)
                ? "Bạn có chắc chắn muốn xóa từ vựng này không?"
                : "Bạn có chắc chắn muốn xóa tất cả " + selectedRows.length + " từ vựng đã chọn không?";

        int confirm = JOptionPane.showConfirmDialog(this, confirmMsg, "Xác nhận xóa hàng loạt", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            List<Integer> idsToDelete = new ArrayList<>();

            // Duyệt qua danh sách dòng được chọn để bóc ID ẩn (nằm ở cột index 0 của tableModel)
            for (int r : selectedRows) {
                int modelRow = wordTable.convertRowIndexToModel(r); // Đảm bảo đúng index kể cả khi bảng có sắp xếp
                int wordId = (int) tableModel.getValueAt(modelRow, 0);
                idsToDelete.add(wordId);
            }

            // Tiến hành gọi database để xóa từng ID trong danh sách ngầm
            boolean allSuccess = true;
            for (int id : idsToDelete) {
                if (!vocabDAO.delete(id)) {
                    allSuccess = false; // Nếu có 1 từ lỗi, đánh dấu lại
                }
            }

            // Làm mới lại giao diện bảng dữ liệu
            refreshWordTable(currentTopicId);

            if (allSuccess) {
                JOptionPane.showMessageDialog(this, "Đã xóa thành công " + idsToDelete.size() + " từ vựng!");
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra trong quá trình xóa một số từ vựng!", "Thông báo lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addFormField(JDialog d, String lbl, JComponent field, GridBagConstraints g, int row) {
        g.gridwidth = 1; g.gridy = row; g.gridx = 0; d.add(new JLabel(lbl), g);
        g.gridx = 1; d.add(field, g);
    }

    private JButton createStyledButton(String text, Color bg, boolean primary) {
        JButton b = new RoundButton(text, 15);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(125, 38));
        if (primary) { b.setBackground(bg); b.setForeground(WHITE); b.setBorder(null); }
        else { b.setBackground(WHITE); b.setForeground(bg); b.setBorder(BorderFactory.createLineBorder(bg)); }
        return b;
    }
}