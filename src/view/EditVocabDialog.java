package view;

import dao.VocabularyDAO;
import model.Vocabulary;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EditVocabDialog extends JDialog {
    private JTextField txtWord, txtPronunciation, txtMeaning, txtType;
    private JTextArea txtExample;
    private JButton btnSave;
    private boolean isSuccess = false;
    private int wordId;
    private VocabularyDAO dao = new VocabularyDAO();

    public EditVocabDialog(Frame parent, int wordId) {
        super(parent, "Chỉnh sửa từ vựng", true);
        this.wordId = wordId;
        initializeUI();
        loadOldData(); // Load dữ liệu cũ lên form
    }

    private void initializeUI() {
        setSize(450, 550);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(5, 1, 10, 10));
        form.setBorder(new EmptyBorder(20, 30, 20, 30));

        txtWord = new JTextField();
        txtWord.setBorder(BorderFactory.createTitledBorder("Từ vựng"));
        txtPronunciation = new JTextField();
        txtPronunciation.setBorder(BorderFactory.createTitledBorder("Phiên âm"));
        txtMeaning = new JTextField();
        txtMeaning.setBorder(BorderFactory.createTitledBorder("Nghĩa"));
        txtType = new JTextField();
        txtType.setBorder(BorderFactory.createTitledBorder("Loại từ"));

        txtExample = new JTextArea(4, 20);
        txtExample.setBorder(BorderFactory.createTitledBorder("Ví dụ"));

        form.add(txtWord);
        form.add(txtPronunciation);
        form.add(txtMeaning);
        form.add(txtType);

        btnSave = new JButton("Cập nhật thay đổi");
        btnSave.setBackground(new Color(9, 132, 227));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> handleUpdate());

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(txtExample), BorderLayout.CENTER);
        add(btnSave, BorderLayout.SOUTH);
    }

    private void loadOldData() {
        Vocabulary v = dao.getById(wordId); // Dùng hàm getById có sẵn trong DAO của Hoa
        if (v != null) {
            txtWord.setText(v.getWord());
            txtPronunciation.setText(v.getPronunciation());
            txtMeaning.setText(v.getMeaning());
            txtType.setText(v.getType());
            txtExample.setText(v.getExample());
        }
    }

    private void handleUpdate() {
        Vocabulary v = dao.getById(wordId);
        v.setWord(txtWord.getText().trim());
        v.setMeaning(txtMeaning.getText().trim());
        v.setPronunciation(txtPronunciation.getText().trim());
        v.setExample(txtExample.getText().trim());
        v.setType(txtType.getText().trim());

        if (dao.update(v)) { // Gọi hàm update trong DAO
            isSuccess = true;
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            dispose();
        }
    }

    public boolean isSuccess() { return isSuccess; }
}