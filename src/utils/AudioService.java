package utils;

import javazoom.jl.player.Player;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AudioService {

    public static void playEnglish(String text) {
        new Thread(() -> {
            try {
                // Sửa lỗi gạch đỏ ở đây bằng StandardCharsets.UTF_8
                String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);

                // Link giọng đọc Google chuẩn
                String urlString = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=en&q=" + encodedText;

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                InputStream inputStream = conn.getInputStream();

                // Dùng thư viện JLayer (jl1.0.1.jar) để phát
                Player player = new Player(inputStream);
                player.play();

            } catch (Exception e) {
                System.err.println("Lỗi phát âm thanh: " + e.getMessage());
            }
        }).start();
    }

    public static void playChinese(String text) {
        try {
            // Đổi tham số tl=en thành tl=zh để gọi giọng Trung Quốc
            String url = "https://translate.google.com/translate_tts?ie=UTF-8&tl=zh&client=tw-ob&q="
                    + java.net.URLEncoder.encode(text, "UTF-8");

            // Đoạn code xử lý play audio giống hệt playEnglish của bạn ở dưới...
            // (Ví dụ: sử dụng JLayer hoặc InputStream để chạy file âm thanh)
            // Thay thế cho dòng playFromUrl(url);
            try {
                java.net.URL urlConnection = new java.net.URL(url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) urlConnection.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                java.io.InputStream is = conn.getInputStream();
                javazoom.jl.player.Player player = new javazoom.jl.player.Player(is);

                // Chạy audio trên một Thread độc lập để tránh bị đơ giao diện UI
                new Thread(() -> {
                    try {
                        player.play();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}