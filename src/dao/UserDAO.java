package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.User;
import utils.DatabaseConnection;

public class UserDAO {

    // 1. Kiểm tra đăng nhập (Dùng cho chức năng quản lý tài khoản)
    public User login(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getInt("roleID"),
                        rs.getInt("id_user"),
                        rs.getString("email")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 2. Thêm người dùng mới (Đăng ký)
    public boolean insert(User user) {
        String sql = "INSERT INTO Users (id_user, username, password, roleID) VALUES (?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, user.getUserID());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setInt(4, user.getRoleID());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 3. Cập nhật thông tin (Đổi mật khẩu)
    public boolean updatePassword(String userID, String newPassword) {
        String sql = "UPDATE Users SET password = ? WHERE id_user = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, newPassword);
            pstmt.setString(2, userID);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getEmailByUserId(String userID) {
        String email = "";
        String sql = "SELECT email FROM Users WHERE id_user = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, userID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                email = rs.getString("email");
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi lấy email: " + e.getMessage());
            e.printStackTrace();
        }
        return email;
    }


    public List<Object[]> getAllUsersForAdmin() {
        List<Object[]> list = new ArrayList<>();
        // Lấy thông tin người dùng và vai trò (Role)
        String sql = "SELECT id_user, username, email, roleID FROM Users";
        try (Connection con = utils.DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("id_user"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getInt("roleID") == 1 ? "Admin" : "Người dùng"
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM Users WHERE id_user = ?";
        try (Connection con = utils.DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean registerUser(String username, String email, String password) {
        // Câu lệnh SQL: id_user thường tự tăng nên không cần chèn, roleID mặc định là 2 (User)
        String sql = "INSERT INTO Users (username, email, password, RoleID) VALUES (?, ?, ?, 2)";

        try (Connection con = utils.DatabaseConnection.getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, password);

            int result = pstmt.executeUpdate();
            return result > 0; // Trả về true nếu chèn thành công
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Trong file UserDAO.java

    // 1. Hàm cập nhật thời gian đăng nhập
    public void updateLastLogin(int userId) {
        String sql = "UPDATE Users SET last_login = GETDATE() WHERE id_user = ?";
        try (Connection con = utils.DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 2. Hàm kiểm tra xem có nên gửi mail nhắc nhở không
    public boolean shouldSendReminder(int userId) {
        // DATEDIFF tính số ngày chênh lệch giữa lần cuối login và hôm nay
        String sql = "SELECT DATEDIFF(day, last_login, GETDATE()) as SoNgayVang FROM Users WHERE id_user = ?";
        try (Connection con = utils.DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int days = rs.getInt("SoNgayVang");
                return days >= 3; // Nếu nghỉ quá 3 ngày thì trả về true
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public List<User> getListUserToRemind() {
        List<User> list = new ArrayList<>();
        // Lấy tất cả user có last_login cách đây >= 3 ngày
        String sql = "SELECT * FROM [Users] \n" +
                "WHERE DATEDIFF(day, last_login, GETDATE()) >= 3 \n" +
                "  AND (last_email_sent IS NULL OR DATEDIFF(day, last_email_sent, GETDATE()) > 0);";

        try (Connection con = utils.DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setUserID(rs.getInt("id_user"));;
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                list.add(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateLastEmailSent(int userId) {
        String sql = "UPDATE [Users] SET last_email_sent = GETDATE() WHERE id_user = ?";
        try (Connection conn = utils.DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserByAdmin(int id, String username, String password, String email, int roleId) {
        String sql;
        boolean hasNewPass = !password.isEmpty();

        if (hasNewPass) {
            sql = "UPDATE [User] SET username = ?, password = ?, email = ?, roleID = ? WHERE userID = ?";
        } else {
            sql = "UPDATE [User] SET username = ?, email = ?, roleID = ? WHERE userID = ?";
        }

        try (Connection conn = utils.DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (hasNewPass) {
                ps.setString(1, username);
                ps.setString(2, password); // Hoa có thể dùng hàm băm MD5/SHA-256 ở đây nếu hệ thống có dùng
                ps.setString(3, email);
                ps.setInt(4, roleId);
                ps.setInt(5, id);
            } else {
                ps.setString(1, username);
                ps.setString(2, email);
                ps.setInt(3, roleId);
                ps.setInt(4, id);
            }
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}