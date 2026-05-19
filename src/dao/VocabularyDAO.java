package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Vocabulary;
import utils.DatabaseConnection;

public class VocabularyDAO {

    public List<Vocabulary> getByTopicId(int topicId) {
        List<Vocabulary> list = new ArrayList<>();
        // Tên cột: word_id, word, meaning, topic_id...
        String sql = "SELECT * FROM Vocabulary WHERE topic_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, topicId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToVocabulary(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean insert(Vocabulary v) {
        String sql = "INSERT INTO Vocabulary (word, meaning, pronunciation, example, language_id, topic_id,type) VALUES (?, ?,  ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getWord());
            ps.setString(2, v.getMeaning());
            ps.setString(3, v.getPronunciation());
            ps.setString(4, v.getExample());

            ps.setInt(6, v.getLanguageID()); // Khóa ngoại language_id
            ps.setInt(7, v.getTopicID());    // Khóa ngoại topic_id
            ps.setString(8, v.getType());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Vocabulary v) {
        String sql = "UPDATE Vocabulary SET word=?, meaning=?, pronunciation=?, example=?,  type=? WHERE word_id=?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getWord());
            ps.setString(2, v.getMeaning());
            ps.setString(3, v.getPronunciation());
            ps.setString(4, v.getExample());

            ps.setInt(5, v.getWordID());
            ps.setString(6, v.getType());;
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int wordId) {
        String sql = "DELETE FROM Vocabulary WHERE word_id = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, wordId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private Vocabulary mapResultSetToVocabulary(ResultSet rs) throws SQLException {
        return new Vocabulary(
                rs.getInt("word_id"),
                rs.getString("word"),
                rs.getString("meaning"),
                rs.getString("pronunciation"),
                rs.getString("example"),

                rs.getInt("language_id"),
                rs.getInt("topic_id"),
                rs.getString("type"),
                rs.getString("status")
        );
    }

    public Vocabulary getById(int wordId) {
        // Sử dụng tên cột word_id theo đúng sơ đồ ERD trong image_f277bc.png
        String sql = "SELECT * FROM Vocabulary WHERE word_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, wordId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Khởi tạo đối tượng Vocabulary từ dữ liệu ResultSet
                    return new Vocabulary(
                            rs.getInt("word_id"),
                            rs.getString("word"),
                            rs.getString("meaning"),
                            rs.getString("pronunciation"),
                            rs.getString("example"),

                            rs.getInt("language_id"),
                            rs.getInt("topic_id"),
                            rs.getString("type"),
                            rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy từ vựng theo ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Trả về null nếu không tìm thấy từ vựng với ID tương ứng
    }
    public boolean importFromExcel(List<Vocabulary> list) {
        String sql = "INSERT INTO Vocabulary (word,type, meaning, pronunciation, example, topic_id, language_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false); // Bật giao dịch để đảm bảo dữ liệu toàn vẹn
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Vocabulary v : list) {
                    ps.setString(1, v.getWord());          // Khớp với 'word'
                    ps.setString(2, v.getType());          // Khớp với 'type'
                    ps.setString(3, v.getMeaning());       // Khớp với 'meaning'
                    ps.setString(4, v.getPronunciation()); // Khớp với 'pronunciation'
                    ps.setString(5, v.getExample());       // Khớp với 'example'
                    ps.setInt(6, v.getTopicID());          // Khớp với 'topic_id'
                    ps.setInt(7, 1); // Mặc định ID ngôn ngữ là 1
                    ps.addBatch();
                }
                ps.executeBatch();
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Vocabulary> getAll() {
        List<Vocabulary> list = new ArrayList<>();
        // Câu lệnh SQL lấy tất cả các cột từ bảng Vocabulary
        String sql = "SELECT * FROM Vocabulary";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Vocabulary v = new Vocabulary();
                // Đổ dữ liệu từ ResultSet vào đối tượng Model
                v.setWordID(rs.getInt("word_id"));
                v.setWord(rs.getString("word"));
                v.setMeaning(rs.getString("meaning"));
                v.setPronunciation(rs.getString("pronunciation"));
                v.setExample(rs.getString("example"));

                v.setLanguageID(rs.getInt("language_id"));
                v.setTopicID(rs.getInt("topic_id"));
                v.setType(rs.getString("type"));
                v.setStatus(rs.getString("status"));

                list.add(v);
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi lấy toàn bộ từ vựng: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public List<Object[]> getAllVocabForAdmin() {
        List<Object[]> list = new ArrayList<>();
        // Lệnh SQL JOIN với bảng Topics để lấy tên chủ đề thay vì chỉ lấy ID
        String sql = "SELECT v.word_id, v.word, v.pronunciation, v.meaning, t.name_topic " +
                "FROM Vocabulary v LEFT JOIN Topic t ON v.topic_id = t.id_topic";

        try (Connection con = utils.DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("word_id"),
                        rs.getString("word"),
                        rs.getString("pronunciation"),
                        rs.getString("meaning"),
                        rs.getString("name_topic"),
                        "Sửa | Xóa" // Cột giả để làm nút thao tác
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Hàm xóa từ vựng theo ID
    public boolean deleteVocab(String id) {
        String sql = "DELETE FROM Vocabulary WHERE word_id = ?";
        try (Connection con = utils.DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public int getNewVocabToday() {
        // Truy vấn đếm số từ có ngày tạo là hôm nay
        String sql = "SELECT COUNT(*) FROM Vocabulary WHERE CAST(created_at AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection con = utils.DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public List<Object[]> searchVocab(String keyword) {
        List<Object[]> list = new ArrayList<>();
        // Tìm kiếm theo từ tiếng Anh hoặc nghĩa tiếng Việt
        String sql = "SELECT v.word_id, v.word, v.pronunciation, v.meaning, t.name_topic " +
                "FROM Vocabulary v LEFT JOIN Topic t ON v.topic_id = t.id_topic " +
                "WHERE v.word LIKE ? OR v.meaning LIKE ?";

        try (Connection con = utils.DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("word_id"),
                        rs.getString("word"),
                        rs.getString("pronunciation"),
                        rs.getString("meaning"),
                        rs.getString("name_topic")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    // Đếm tổng số từ vựng của một người dùng cụ thể
    public int countTotalByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM Vocabulary v " +
                "JOIN Topic t ON v.topic_id = t.id_topic " +
                "WHERE t.id_user = ?";
        try (Connection con = utils.DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // Đếm số từ đã thuộc (Ví dụ: những từ có status = 'Mastered')
    public int countByStatus(int userId, String status) {
        String sql = "SELECT COUNT(*) FROM Vocabulary v " +
                "JOIN Topic t ON v.topic_id = t.id_topic " +
                "WHERE t.id_user = ? AND v.status = ?";
        try (Connection con = utils.DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public boolean insertWithTopicName(Vocabulary v, String topicName) {
        // 1. Câu lệnh tìm ID từ tên Topic
        String sqlFindTopic = "SELECT id_topic FROM Topic WHERE name_topic = ?";
        String sqlInsert = "INSERT INTO Vocabulary (word, type, meaning, pronunciation, example, topic_id, language_id) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = utils.DatabaseConnection.getConnection()) {
            // Tìm ID của Topic
            int topicId = -1;
            try (PreparedStatement psFind = con.prepareStatement(sqlFindTopic)) {
                psFind.setString(1, topicName);
                ResultSet rs = psFind.executeQuery();
                if (rs.next()) {
                    topicId = rs.getInt("id_topic");
                }
            }

            if (topicId == -1) return false; // Không tìm thấy Topic này

            // Lưu từ vựng với ID vừa tìm được
            try (PreparedStatement ps = con.prepareStatement(sqlInsert)) {
                ps.setString(1, v.getWord());
                ps.setString(2, v.getType());
                ps.setString(3, v.getMeaning());
                ps.setString(4, v.getPronunciation());
                ps.setString(5, v.getExample());
                ps.setInt(6, topicId);
                ps.setInt(7, v.getLanguageID());

                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatus(int wordId, String status) {
        String sql = "UPDATE Vocabulary SET status = ? WHERE word_id = ?";
        try (Connection con = utils.DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, wordId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}