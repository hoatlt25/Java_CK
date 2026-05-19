package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import utils.DatabaseConnection;

public class LearningProgressDAO {

    // 1. Hàm lưu tiến độ (Hàm này giúp dữ liệu bay vào Database)
    public void updateProgressAfterReview(int userId, int wordId, boolean isCorrect) {
        String sql = "IF EXISTS (SELECT 1 FROM LearningProgress WHERE id_user = ? AND word_id = ?) " +
                "BEGIN " +
                "  UPDATE LearningProgress " +
                "  SET status = ?, review_count = review_count + 1, last_review_date = GETDATE() " +
                "  WHERE id_user = ? AND word_id = ? " +
                "END " +
                "ELSE " +
                "BEGIN " +
                "  INSERT INTO LearningProgress (id_user, word_id, status, review_count, last_review_date) " +
                "  VALUES (?, ?, ?, 1, GETDATE()) " +
                "END";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            String status = isCorrect ? "Mastered" : "Learning";
            pstmt.setInt(1, userId);
            pstmt.setInt(2, wordId);
            pstmt.setString(3, status);
            pstmt.setInt(4, userId);
            pstmt.setInt(5, wordId);
            pstmt.setInt(6, userId);
            pstmt.setInt(7, wordId);
            pstmt.setString(8, status);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<Object[]> getRecentHistory(int userId) {
        List<Object[]> list = new ArrayList<>();

        // SQL: Lấy thông tin luyện tập gom nhóm theo từng Bộ thẻ (Topic)
        String sql = "SELECT " +
                "    t.name_topic, " + // Lấy tên bộ thẻ
                "    CAST(MAX(lp.last_review_date) AS DATE) as NgayHoc, " +
                "    COUNT(lp.word_id) as SoTuDaHoc, " +
                "    SUM(CASE WHEN lp.status = 'Mastered' THEN 1 ELSE 0 END) * 100 / COUNT(lp.word_id) as PhanTram " +
                "FROM LearningProgress lp " +
                "JOIN Vocabulary v ON lp.word_id = v.word_id " +
                "JOIN Topic t ON v.topic_id = t.id_topic " + // Hoa kiểm tra id_topic hay TopicID nhé
                "WHERE lp.id_user = ? " +
                "GROUP BY t.name_topic " +
                "ORDER BY NgayHoc DESC";

        try (Connection con = utils.DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int phanTram = rs.getInt("PhanTram");
                String danhGia = (phanTram >= 80) ? "Xuất sắc" : (phanTram >= 50 ? "Khá" : "Cần cố gắng");

                // Trả về đúng các cột cho bảng: Bộ thẻ | Ngày | Số từ | Điểm (%) | Đánh giá
                list.add(new Object[] {
                        rs.getString("name_topic"),
                        rs.getDate("NgayHoc"),
                        rs.getInt("SoTuDaHoc"),
                        phanTram,
                        danhGia
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}