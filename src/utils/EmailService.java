package utils;


import model.User;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.util.List;
import java.util.Properties;

public class EmailService {

    // Thông tin cấu hình mail server (Dùng Gmail)
    private static final String FROM_EMAIL = "hoat8914@gmail.com"; // Email của bạn
    private static final String APP_PASSWORD = "bqbyuuqfasyujmph";   // Mật khẩu ứng dụng 16 số

    /**
     * Hàm gửi email nhắc nhở học tập
     * @param toEmail: Địa chỉ người nhận
     * @param username: Tên người dùng để cá nhân hóa nội dung
     */
    public static void sendReminderEmail(String toEmail, String username) {

        // 1. Cấu hình các thông số kết nối SMTP của Gmail
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // SMTP Host
        props.put("mail.smtp.port", "587");           // TLS Port
        props.put("mail.smtp.auth", "true");          // Bật xác thực
        props.put("mail.smtp.starttls.enable", "true"); // Bật kết nối an toàn TLS

        // 2. Tạo phiên làm việc (Session) và xác thực tài khoản
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            // 3. Tạo đối tượng tin nhắn (MimeMessage)
            Message message = new MimeMessage(session);

            // Người gửi
            message.setFrom(new InternetAddress(FROM_EMAIL));

            // Người nhận
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));

            // Tiêu đề thư (Dùng UTF-8 để không lỗi tiếng Việt)
            message.setSubject(MimeUtility.encodeText("💡 Đã lâu bạn chưa ghé thăm VocaLearn!", "UTF-8", "B"));

            // Nội dung thư
            String content = "Chào " + username + ",\n\n"
                    + "VocaLearn nhận thấy đã hơn 3 ngày bạn chưa ôn tập từ vựng. "
                    + "Đừng để kiến thức bị rơi rụng nhé!\n\n"
                    + "Hãy dành ra 5 phút mỗi ngày để đạt được mục tiêu của mình.\n"

                    + "Thân mến,\nĐội ngũ VocaLearn.";

            message.setText(content);

            // 4. Gửi mail
            Transport.send(message);

            System.out.println("Email nhắc nhở đã được gửi tới: " + toEmail);

        } catch (Exception e) {
            System.err.println("Lỗi khi gửi Email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void sendBulkReminder(List<User> users) {
        for (User u : users) {
            if (u.getEmail() != null && !u.getEmail().isEmpty()) {
                sendReminderEmail(u.getEmail(), u.getUsername());
                // In ra console để theo dõi
                System.out.println("Đã gửi mail nhắc nhở cho: " + u.getUsername());
            }
        }
    }
}