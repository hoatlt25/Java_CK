package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Role;
import utils.DatabaseConnection;

public class RoleDAO {

    // 1. Lấy tên quyền dựa vào RoleID (Dùng để hiển thị lên giao diện)
    public Role getById(int roleID) {
        String sql = "SELECT * FROM Role WHERE RoleID = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, roleID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Role(
                        rs.getInt("RoleID"),
                        rs.getString("RoleName")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 2. Lấy danh sách tất cả các quyền (Dùng cho ComboBox khi quản lý User)
    public List<Role> getAll() {
        List<Role> list = new ArrayList<>();
        String sql = "SELECT * FROM Role";
        try (Connection con = DatabaseConnection.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Role(
                        rs.getInt("RoleID"),
                        rs.getString("RoleName")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}