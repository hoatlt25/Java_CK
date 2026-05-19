package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Language;
import utils.DatabaseConnection;

public class LanguageDAO {

    // 1. Lấy danh sách tất cả ngôn ngữ (Dùng để hiển thị trong ComboBox khi thêm từ vựng)
    public List<Language> getAll() {
        List<Language> list = new ArrayList<>();
        String sql = "SELECT * FROM Language";
        try (Connection con = DatabaseConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Language(
                        rs.getString("LanguageID"),
                        rs.getString("LanguageName")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 2. Thêm một ngôn ngữ mới
    public boolean insert(Language lang) {
        String sql = "INSERT INTO Language (LanguageID, LanguageName) VALUES (?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, lang.getLanguageID());
            pstmt.setString(2, lang.getLanguageName());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 3. Tìm kiếm ngôn ngữ theo ID
    public Language getById(String languageID) {
        String sql = "SELECT * FROM Language WHERE LanguageID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, languageID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Language(
                        rs.getString("LanguageID"),
                        rs.getString("LanguageName")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}