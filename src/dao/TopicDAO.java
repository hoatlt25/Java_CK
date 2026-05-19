package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Topic;
import utils.DatabaseConnection;

public class TopicDAO {

    // 1. Lấy Map các bộ thẻ THEO USER (Dùng cho ComboBox)
    public Map<Integer, String> getTopicsByUserId(int userId) {
        Map<Integer, String> map = new LinkedHashMap<>();
        // Hoa kiểm tra trong DB là 'name_topic' hay 'topic_name' nhé.
        // Ở đây mình để 'name_topic' theo hàm insert của bạn.
        String sql = "SELECT id_topic, name_topic FROM Topic WHERE id_user = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getInt("id_topic"), rs.getString("name_topic"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    // 2. Thêm một chủ đề mới có gắn ID người tạo
    public boolean insertTopic(String name, int userId) {
        String sql = "INSERT INTO Topic (name_topic, id_user) VALUES (?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. Sửa tên bộ thẻ
    public boolean updateTopic(int id, String name) {
        String sql = "UPDATE Topic SET name_topic = ? WHERE id_topic = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. Xóa bộ thẻ
    public boolean deleteTopic(int id) {
        String sql = "DELETE FROM Topic WHERE id_topic = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}