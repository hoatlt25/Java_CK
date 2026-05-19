package view;

import dao.VocabularyDAO;
import model.Vocabulary;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddVocabDialog extends JDialog {
    private JTextField txtWord, txtPronunciation, txtMeaning, txtType, txtNameTopic;
    private JTextArea txtExample;
    private JButton btnSave, btnCancel;
    private boolean isSuccess = false;

    // Màu sắc đồng bộ với hệ thống của Hoa
    private Color greenAdd = new Color(0, 184, 148);
    private Color purpleMain = new Color(108, 92, 231);

    public AddVocabDialog(Frame parent) {
        super(parent, "Thêm từ vựng mới", true);
        initializeUI();
    }

    private void initializeUI() {
        setSize(450, 600);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- Header ---
        JPanel header = new JPanel();
        header.setBackground(purpleMain);
        JLabel lblHeader = new JLabel("NHẬP THÔNG TIN TỪ MỚI");
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(lblHeader);
        add(header, BorderLayout.NORTH);

        // --- Form Nhập liệu ---
        JPanel form = new JPanel(new GridLayout(6, 1, 10, 10));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(20, 30, 10, 30));

        txtWord = createStyledField("Từ vựng (Word)");
        txtPronunciation = createStyledField("Phiên âm (Pronunciation)");
        txtMeaning = createStyledField("Nghĩa tiếng Việt (Meaning)");
        txtType = createStyledField("Loại từ (n, v, adj...)");
        txtNameTopic = createStyledField("Tên chủ đề");

        form.add(txtWord);
        form.add(txtPronunciation);
        form.add(txtMeaning);
        form.add(txtType);
        form.add(txtNameTopic);

        txtExample = new JTextArea(4, 20);
        txtExample.setLineWrap(true);
        txtExample.setWrapStyleWord(true);
        txtExample.setBorder(BorderFactory.createTitledBorder("Ví dụ minh họa"));
        JScrollPane scrollExample = new JScrollPane(txtExample);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(form, BorderLayout.NORTH);
        centerPanel.add(scrollExample, BorderLayout.CENTER);
        centerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(centerPanel, BorderLayout.CENTER);

        // --- Nút bấm ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        footer.setOpaque(false);

        btnCancel = new JButton("Hủy bỏ");
        btnSave = new JButton("Lưu từ vựng");

        styleButton(btnCancel, new Color(150, 150, 150));
        styleButton(btnSave, greenAdd);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> handleSave());

        footer.add(btnCancel);
        footer.add(btnSave);
        add(footer, BorderLayout.SOUTH);
    }

    private JTextField createStyledField(String title) {
        JTextField field = new JTextField();
        field.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)), title));
        return field;
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 38));
    }

    private void handleSave() {
        try {
            // 1. Kiểm tra dữ liệu đầu vào
            String word = txtWord.getText().trim();
            String meaning = txtMeaning.getText().trim();
            String topicName = txtNameTopic.getText().trim();

            if (word.isEmpty() || meaning.isEmpty() || topicName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ: Từ vựng, Nghĩa và Tên chủ đề!");
                return;
            }

            // 2. Đóng gói dữ liệu vào Model
            Vocabulary v = new Vocabulary();
            v.setWord(word);
            v.setPronunciation(txtPronunciation.getText().trim());
            v.setMeaning(meaning);
            v.setType(txtType.getText().trim());
            v.setExample(txtExample.getText().trim());
            v.setLanguageID(1);

            // 3. Gọi DAO xử lý (Truyền thêm tên Topic thay vì ID)
            VocabularyDAO dao = new VocabularyDAO();

            // Hoa nên viết một hàm insert mới nhận vào tên Topic
            if (dao.insertWithTopicName(v, topicName)) {
                isSuccess = true;
                JOptionPane.showMessageDialog(this, "Đã thêm thành công vào chủ đề: " + topicName);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi: Chủ đề không tồn tại hoặc lỗi kết nối!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi hệ thống!");
            e.printStackTrace();
        }
    }

    public boolean isSuccess() { return isSuccess; }
}