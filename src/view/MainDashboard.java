package view;

import model.User;
import org.imgscalr.Scalr;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;

public class MainDashboard extends JFrame {

    private JPanel mainContent;
    private CardLayout cardLayout;
    private User currentUser;

    public MainDashboard(User user) {
        this.currentUser = user;
        AccountPanel accountPanel = new AccountPanel(currentUser);

        setTitle("VocaLearn - Dashboard");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. SIDEBAR cố định bên trái
        add(createSidebar(), BorderLayout.WEST);

        // 2. VÙNG NỘI DUNG dùng CardLayout
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setOpaque(false);

        // --- Nạp các trang dành cho TẤT CẢ người dùng ---
        mainContent.add(new DeckPanel(user), "TOPIC_VIEW");
        mainContent.add(new StudyPanel(user.getUserID()), "STUDY_VIEW");
        mainContent.add(new ReviewPanel(user.getUserID()), "REVIEW_VIEW");
       // mainContent.add(new statisticPanel(), "STATISTIC_VIEW");
        mainContent.add(new VocabManagementPanel(), "VOCAB_MANAGEMENT");
        mainContent.add(new UserManagementPanel(), "ADMIN_USER");

        // --- PHÂN QUYỀN: Chỉ nạp trang quản trị nếu là ADMIN (Role 1) ---
        if (currentUser.getRoleID() == 1) {
            // mainContent.add(new AdminVocabPanel(), "ADMIN_VOCAB");
            // mainContent.add(new AdminUserPanel(), "ADMIN_USER");
        }

        // Màn hình chào mừng
        JPanel welcome = new JPanel(new GridBagLayout());
        welcome.setBackground(new Color(248, 249, 253));
        JLabel lblWelcome = new JLabel("Chào mừng " + currentUser.getUsername() + " quay trở lại!");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcome.add(lblWelcome);
        mainContent.add(welcome, "EMPTY_VIEW");

        add(mainContent, BorderLayout.CENTER);
        cardLayout.show(mainContent, "EMPTY_VIEW");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(240, 800));
        sidebar.setBackground(Color.WHITE);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(235, 235, 235)));

        // --- Logo ---
        try {
            ImageIcon icon = new ImageIcon("src/img/book (2).png");
            Image scaled = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            JLabel logo = new JLabel("VocaLearn", new ImageIcon(scaled), JLabel.LEFT);
            logo.setIconTextGap(12);
            logo.setFont(new Font("SansSerif", Font.BOLD, 22));
            logo.setBorder(new EmptyBorder(30, 25, 40, 25));
            logo.setAlignmentX(Component.LEFT_ALIGNMENT);
            sidebar.add(logo);
        } catch (Exception e) {
            JLabel logo = new JLabel("VocaLearn");
            logo.setFont(new Font("SansSerif", Font.BOLD, 22));
            logo.setBorder(new EmptyBorder(30, 25, 40, 25));
            sidebar.add(logo);
        }

        // --- MENU CƠ BẢN (Cho mọi User) ---
        sidebar.add(createMenuButton("Bộ thẻ của tôi", "src/img/clipboard-regular.png", "TOPIC_VIEW"));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(createMenuButton("Học thẻ", "src/img/clone-solid.png", "STUDY_VIEW"));
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(createMenuButton("Ôn tập", "src/img/clock-rotate-left-solid.png", "REVIEW_VIEW"));
        sidebar.add(Box.createVerticalStrut(5));
      //  sidebar.add(createMenuButton("Thống kê", "src/img/chart-line-solid.png", "STATISTIC_VIEW"));

        // --- PHÂN QUYỀN TRÊN SIDEBAR (Chỉ hiện nếu là ADMIN) ---
        if (currentUser.getRoleID() == 1) {
           // sidebar.add(Box.createVerticalStrut(5)); // Khoảng cách

//            JLabel lblAdmin = new JLabel("");
//            lblAdmin.setFont(new Font("SansSerif", Font.BOLD, 11));
//            lblAdmin.setForeground(new Color(150, 150, 150));
//            lblAdmin.setBorder(new EmptyBorder(10, 25, 10, 10));
//            sidebar.add(lblAdmin);

            sidebar.add(createMenuButton("Quản lý từ vựng", "src/img/clipboard-adminList-solid.png", "VOCAB_MANAGEMENT"));
            sidebar.add(Box.createVerticalStrut(5));
            sidebar.add(createMenuButton("Quản lý người dùng", "src/img/clipboard-user-solid.png", "ADMIN_USER"));
        }

        sidebar.add(Box.createVerticalGlue());

        // --- Profile Section ---
        String iconPath = "file:src/img/chevron-down-solid.png";
        String profileText = "<html>" + currentUser.getUsername() + " <img src='" + iconPath + "' width='10' height='10'></html>";
        JLabel profile = new JLabel(profileText);
        profile.setFont(new Font("Segoe UI", Font.BOLD, 15));
        profile.setBorder(new EmptyBorder(20, 25, 30, 25));
        profile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profile.setAlignmentX(Component.LEFT_ALIGNMENT);

        profile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                AccountPanel accountPanel = new AccountPanel(currentUser);
                mainContent.add(accountPanel, "ACCOUNT_VIEW");
                cardLayout.show(mainContent, "ACCOUNT_VIEW");
            }
            @Override
            public void mouseEntered(MouseEvent e) { profile.setForeground(new Color(108, 92, 231)); }
            @Override
            public void mouseExited(MouseEvent e) { profile.setForeground(Color.BLACK); }
        });

        sidebar.add(profile);
        return sidebar;
    }

    private JButton createMenuButton(String text, String iconPath, String pageName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setForeground(new Color(100, 100, 100));
        btn.setIconTextGap(15);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setMaximumSize(new Dimension(240, 50));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        try {
            BufferedImage img = ImageIO.read(new File(iconPath));
            btn.setIcon(new ImageIcon(Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, 22)));
        } catch (Exception e) {}

        btn.addActionListener(e -> cardLayout.show(mainContent, pageName));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(240, 235, 255));
                btn.setForeground(new Color(108, 92, 231));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
                btn.setForeground(new Color(100, 100, 100));
            }
        });

        return btn;
    }

    public static void main(String[] args) {

    }
}