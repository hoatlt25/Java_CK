package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=DACS1;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASS = "123456789";

    public static Connection getConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=DACS1;encrypt=true;trustServerCertificate=true", "sa", "123456789");
        } catch (Exception e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Đã đóng kết nối.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
    public static void main(String[] args) {
        System.out.println("Đang kết nối đến SQL sever....");
        Connection conn = getConnection();
        if(conn!=null) {
            System.out.println("Kết nối database thành công");
            System.out.println("Database: DACS 1");
            closeConnection(conn);
        }else {
            System.out.println("Kết nối thất bại!");
        }

    }

}
