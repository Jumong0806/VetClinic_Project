package vetclinic.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import vetclinic.exception.StorageException;
import vetclinic.service.AuthService;
import vetclinic.service.ClinicManager;
import vetclinic.util.ImageUtil;

/**
 * MainFrame - the main window: sidebar (Dashboard, Patients, Staff, Treatments, Logout),
 * header showing who is logged in, and pages in the middle.
 * Owner: Ben
 * Swing concepts: BorderLayout (frame), BoxLayout (sidebar), FlowLayout (header), CardLayout (pages).
 * OOP concepts: INHERITANCE (extends JFrame), ENCAPSULATION (private components).
 */
public class MainFrame extends JFrame {

    private ClinicManager manager;
    private AuthService authService;

    // UI Containers
    private JPanel pnlSidebar;
    private JPanel rightWrapper;
    private JPanel pnlContent;

    // Unified Header Elements
    private JLabel lblPageTitle;
    private JButton btnNotifications;
    private JLabel lblUserName;
    private JLabel lblUserRole;
    private JLabel lblPageSubtitle;

    // Sidebar Buttons
    private JButton btnDashboard;
    private JButton btnPatients;
    private JButton btnStaff;
    private JButton btnTreatments;
    private JButton btnLogout;

    // Pages
    private DashboardPanel dashboardPanel;
    private AnimalPanel animalPanel;
    private StaffPanel staffPanel;
    private TreatmentPanel treatmentPanel;

    // Theme Colors
    private final Color SIDEBAR_BG = new Color(0xF0F4F9);
    private final Color ACTIVE_BG = new Color(0xDBEAFE);
    private final Color ACTIVE_TEXT = new Color(0x1D4ED8);
    private final Color INACTIVE_TEXT = new Color(0x1E293B);
    private final Color TOP_BG = Color.WHITE;

    public MainFrame(ClinicManager manager, AuthService authService) {
        super("Veterinary Clinic Patient Record System");
        URL iconURL = getClass().getResource("/images/app-logo.png");

        if (iconURL != null) {
            setIconImage(new ImageIcon(iconURL).getImage());
        }
        this.manager = manager;
        this.authService = authService;
        initComponents();
        setAppIcon();
    }

    private void setAppIcon() {
        java.net.URL iconURL = getClass().getResource("/images/logo-final.png");
        if (iconURL != null) {
            javax.swing.ImageIcon icon = new javax.swing.ImageIcon(iconURL);
            setIconImage(icon.getImage());
        }
    }

    // Initializes the window properties and constructs the main UI layout
    // (Sidebar, Header, and CardLayout content pages).
    private void initComponents() {
        setSize(1300, 800);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });

        // 1. Main Layout
        setLayout(new BorderLayout());

        // 2. Build the Sidebar
        buildSidebar();

        // 3. Right-Side Wrapper (Holds Header + Content)
        rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setBackground(new Color(0xF8FAFC));

        // 4. Build the Unified Header
        buildHeader();

        // 5. Build Content Area
        pnlContent = new JPanel(new CardLayout());

        dashboardPanel = new DashboardPanel(manager);
        animalPanel = new AnimalPanel(manager, authService);
        staffPanel = new StaffPanel(manager, authService);
        treatmentPanel = new TreatmentPanel(manager, authService);

        pnlContent.add(dashboardPanel, "Dashboard");
        pnlContent.add(animalPanel, "Patients");
        pnlContent.add(staffPanel, "Staff");
        pnlContent.add(treatmentPanel, "Treatments");

        rightWrapper.add(pnlContent, BorderLayout.CENTER);

        // 6. Add rightWrapper to the Main Frame
        add(rightWrapper, BorderLayout.CENTER);

        showPage("Dashboard");
    }

    // Sidebar strictly matched to reference spacing and layout
    private void buildSidebar() {
        pnlSidebar = new JPanel();
        pnlSidebar.setLayout(new BoxLayout(pnlSidebar, BoxLayout.Y_AXIS));
        pnlSidebar.setBackground(SIDEBAR_BG);
        pnlSidebar.setPreferredSize(new Dimension(260, 0));
        // Generous vertical breathing room with zero side padding so selection highlights span full width
        pnlSidebar.setBorder(new EmptyBorder(36, 0, 36, 0));

        // --- Logo and Title Block (Centered) ---
        JPanel pnlLogoContainer = new JPanel();
        pnlLogoContainer.setLayout(new BoxLayout(pnlLogoContainer, BoxLayout.Y_AXIS));
        pnlLogoContainer.setOpaque(false);
        pnlLogoContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblLogoIcon = new JLabel(ImageUtil.load("/images/logo-final.png", 64, 64));
        lblLogoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblLogoText = new JLabel("Vet Kita");
        lblLogoText.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblLogoText.setForeground(new Color(0x1E3A8A));
        lblLogoText.setAlignmentX(Component.CENTER_ALIGNMENT);

        pnlLogoContainer.add(lblLogoIcon);
        pnlLogoContainer.add(Box.createVerticalStrut(12));
        pnlLogoContainer.add(lblLogoText);

        pnlSidebar.add(pnlLogoContainer);
        pnlSidebar.add(Box.createVerticalStrut(45));

        // --- Navigation Buttons (Full-Width Span with Icon Spacing) ---
        btnDashboard = createNavButton("Dashboard", "/images/dashboard.png");
        btnPatients = createNavButton("Patients", "/images/patients.png");
        btnStaff = createNavButton("Staff", "/images/staff.png");
        btnTreatments = createNavButton("Treatments", "/images/treatments.png");
        btnLogout = createNavButton("Logout", "/images/logout.png");

        pnlSidebar.add(btnDashboard);
        pnlSidebar.add(Box.createVerticalStrut(4));
        pnlSidebar.add(btnPatients);
        pnlSidebar.add(Box.createVerticalStrut(4));
        pnlSidebar.add(btnStaff);
        pnlSidebar.add(Box.createVerticalStrut(4));
        pnlSidebar.add(btnTreatments);

        pnlSidebar.add(Box.createVerticalGlue());
        pnlSidebar.add(btnLogout);

        // Action Listeners
        btnDashboard.addActionListener(e -> showPage("Dashboard"));
        btnPatients.addActionListener(e -> showPage("Patients"));
        btnStaff.addActionListener(e -> showPage("Staff"));
        btnTreatments.addActionListener(e -> showPage("Treatments"));
        btnLogout.addActionListener(e -> handleLogout());

        add(pnlSidebar, BorderLayout.WEST);
    }

    private void buildHeader() {
        JPanel topHeaderPanel = new JPanel(new BorderLayout());
        topHeaderPanel.setBackground(TOP_BG);
        topHeaderPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE2E8F0)),
                new EmptyBorder(20, 35, 20, 35) // Slightly wider side padding
        ));

        // --- Left: Title and Subtitle Stack ---
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        lblPageTitle = new JLabel("Dashboard");
        lblPageTitle.setFont(new Font("SansSerif", Font.BOLD, 28)); // Larger, bolder title
        lblPageTitle.setForeground(new Color(0x111827)); // Darker navy/black

        lblPageSubtitle = new JLabel("A quick look at the clinic");
        lblPageSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblPageSubtitle.setForeground(new Color(0x6B7280)); // Softer gray

        titlePanel.add(lblPageTitle);
        titlePanel.add(Box.createVerticalStrut(2)); // Very tight vertical gap
        titlePanel.add(lblPageSubtitle);
        topHeaderPanel.add(titlePanel, BorderLayout.WEST);

        // --- Right: Notifications & Profile ---
        // Used BoxLayout.X_AXIS for PERFECT vertical centering
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.X_AXIS));
        profilePanel.setOpaque(false);

        btnNotifications = new JButton(ImageUtil.load("/images/bell-alert-try.png", 24, 24));
        btnNotifications.setFocusPainted(false);
        btnNotifications.setBorderPainted(false);
        btnNotifications.setContentAreaFilled(false);
        btnNotifications.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Force the bell to perfectly center vertically with the avatar
        btnNotifications.setAlignmentY(Component.CENTER_ALIGNMENT);
        btnNotifications.addActionListener(e -> handleNotifications());

        JLabel lblAvatar = new JLabel(ImageUtil.load("/images/avatar-final.png", 42, 42)); // Sized up slightly
        // Force the avatar to perfectly center vertically
        lblAvatar.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Use BoxLayout for the text stack so it behaves predictably
        JPanel userTextPanel = new JPanel();
        userTextPanel.setLayout(new BoxLayout(userTextPanel, BoxLayout.Y_AXIS));
        userTextPanel.setOpaque(false);
        // Force the entire text block to center vertically next to the avatar
        userTextPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        lblUserName = new JLabel("Front Desk Admin");
        lblUserName.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblUserName.setForeground(new Color(0x111827));
        lblUserName.setAlignmentX(Component.LEFT_ALIGNMENT); // Left align text inside its box

        lblUserRole = new JLabel("System Administrator");
        lblUserRole.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblUserRole.setForeground(new Color(0x6B7280));
        lblUserRole.setAlignmentX(Component.LEFT_ALIGNMENT); // Left align text inside its box

        userTextPanel.add(lblUserName);
        userTextPanel.add(Box.createVerticalStrut(2)); // Tight gap between name and role
        userTextPanel.add(lblUserRole);

        // ADD COMPONENTS IN THE EXACT PROTOTYPE ORDER WITH PRECISE SPACING
        profilePanel.add(btnNotifications);
        profilePanel.add(Box.createHorizontalStrut(-1)); // Gap between bell and avatar
        profilePanel.add(lblAvatar);
        profilePanel.add(Box.createHorizontalStrut(12)); // Tighter gap between avatar and text
        profilePanel.add(userTextPanel);

        topHeaderPanel.add(profilePanel, BorderLayout.EAST);
        rightWrapper.add(topHeaderPanel, BorderLayout.NORTH);
    }

    private void showPage(String pageName) {
        ((CardLayout) pnlContent.getLayout()).show(pnlContent, pageName);
        lblPageTitle.setText(pageName);

        if (pageName.equals("Dashboard")) {
            lblPageSubtitle.setText("A quick look at the clinic");
            dashboardPanel.refresh();
        } else if (pageName.equals("Patients")) {
            lblPageSubtitle.setText("View and manage all patient records");
            animalPanel.refreshTable(manager.getAllAnimals());
        } else if (pageName.equals("Staff")) {
            lblPageSubtitle.setText("Manage your veterinary team and staff members");
            staffPanel.refreshTable(manager.getAllStaff());
        } else if (pageName.equals("Treatments")) {
            lblPageSubtitle.setText("View and manage all clinic treatment records");
            treatmentPanel.refreshTable(manager.getAllTreatments());
        }

        updateButtonActiveState(btnDashboard, pageName.equals("Dashboard"));
        updateButtonActiveState(btnPatients, pageName.equals("Patients"));
        updateButtonActiveState(btnStaff, pageName.equals("Staff"));
        updateButtonActiveState(btnTreatments, pageName.equals("Treatments"));
    }

    private void updateButtonActiveState(JButton button, boolean isActive) {
        if (isActive) {
            button.setBackground(ACTIVE_BG);
            button.setForeground(ACTIVE_TEXT);
        } else {
            button.setBackground(SIDEBAR_BG);
            button.setForeground(INACTIVE_TEXT);
        }
    }

    private JButton createNavButton(String text, String iconPath) {
        JButton button = new JButton(text);
        try {
            button.setIcon(ImageUtil.load(iconPath, 22, 22));
        } catch (Exception e) {
            // Graceful fallback if icon asset is missing yet
        }

        button.setHorizontalAlignment(SwingConstants.LEFT);
        // Generous gap to separate icon from text cleanly matching your prototype layout
        button.setIconTextGap(18);

        // Forces button to span full width of the sidebar while maintaining height
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        button.setPreferredSize(new Dimension(260, 52));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("SansSerif", Font.BOLD, 16));

        // Exact left padding to align icons and text precisely with reference image
        button.setBorder(BorderFactory.createEmptyBorder(10, 28, 10, 15));

        // Default inactive colors
        button.setBackground(SIDEBAR_BG);
        button.setForeground(INACTIVE_TEXT);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!isCurrentActiveButton(button)) {
                    button.setBackground(new Color(0xE2E8F0));
                    button.setForeground(new Color(0x0F172A));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!isCurrentActiveButton(button)) {
                    button.setBackground(SIDEBAR_BG);
                    button.setForeground(INACTIVE_TEXT);
                }
            }
        });

        return button;
    }

    private boolean isCurrentActiveButton(JButton button) {
        String title = lblPageTitle.getText();
        if (button == btnDashboard && title.equals("Dashboard")) return true;
        if (button == btnPatients && title.equals("Patients")) return true;
        if (button == btnStaff && title.equals("Staff")) return true;
        if (button == btnTreatments && title.equals("Treatments")) return true;
        return false;
    }

    private void handleLogout() {
        int answer = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (answer == JOptionPane.YES_OPTION) {
            dispose();
            new vetclinic.ui.LoginFrame(manager, authService).setVisible(true);
        }
    }

    private void handleExit() {
        int answer = JOptionPane.showConfirmDialog(this,
                "Exit the program? Your records are saved automatically.", "Exit",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            manager.saveAll();
        } catch (StorageException e) {
            int again = JOptionPane.showConfirmDialog(this,
                    "Could not save:\n" + e.getMessage() + "\n\nExit anyway and lose changes?",
                    "Save error", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (again != JOptionPane.YES_OPTION) {
                return;
            }
        }
        dispose();
        System.exit(0);
    }

    private void handleNotifications() {
        new NotificationDialog(this, manager).setVisible(true);   // modal: blocks until closed
        dashboardPanel.refresh();                                  // update the Due for Checkup count
    }
}