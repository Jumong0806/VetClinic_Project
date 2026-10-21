package vetclinic.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import vetclinic.Main;
import vetclinic.exception.StorageException;
import vetclinic.model.Staff;
import vetclinic.service.AuthService;
import vetclinic.service.ClinicManager;

/**
 * MainFrame - the main window: a sidebar (Dashboard, Patients, Staff, Logout), a header that shows
 * who is logged in, and the pages in the middle.
 * Owner: Ben
 *
 * Swing concepts: BorderLayout (frame), BoxLayout (sidebar), FlowLayout (header), CardLayout (pages).
 * OOP concepts: INHERITANCE (extends JFrame), ENCAPSULATION (private components).
 */
public class MainFrame extends JFrame {

    private ClinicManager manager;
    private AuthService authService;

    // UI Containers
    private JPanel pnlSidebar;
    private JPanel rightWrapper;        // Holds the new unified header and the content
    private JPanel pnlContent;          // CardLayout: one "card" (page) per sidebar button

    // Unified Header Elements
    private JLabel lblPageTitle;        // Dynamic title that changes based on the sidebar click
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

    public MainFrame(ClinicManager manager, AuthService authService) {
        super("Veterinary Clinic Patient Record System");
        this.manager = manager;
        this.authService = authService;
        initComponents();
    }

    private void initComponents() {
        setSize(1200, 760);
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

        // 2. Build the Sidebar (Locks to the WEST, full height)
        buildSidebar();

        // 3. Right-Side Wrapper (Holds Header + Content)
        rightWrapper = new JPanel(new BorderLayout());
        rightWrapper.setBackground(new Color(0xF4F7FC)); // Match your overall background

        // 4. Build the Unified Header (North of rightWrapper)
        buildHeader();

        // 5. Build Content Area (Center of rightWrapper)
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

    // The blue-white bar on the left with logo, navigation buttons, and logout.
    private void buildSidebar() {
        pnlSidebar = new JPanel();
        pnlSidebar.setLayout(new BoxLayout(pnlSidebar, BoxLayout.Y_AXIS));
        pnlSidebar.setBackground(new Color(0xEAF1FD));
        pnlSidebar.setPreferredSize(new Dimension(210, 0));
        pnlSidebar.setBorder(BorderFactory.createEmptyBorder(30, 15, 30, 15));

        // --- Logo and Title Block (Centered) ---
        JPanel pnlLogoContainer = new JPanel();
        pnlLogoContainer.setLayout(new BoxLayout(pnlLogoContainer, BoxLayout.Y_AXIS));
        pnlLogoContainer.setOpaque(false);
        pnlLogoContainer.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblLogoIcon = new JLabel("🐾", SwingConstants.CENTER); // Placeholder for paw icon image
        lblLogoIcon.setFont(new Font("SansSerif", Font.PLAIN, 32));
        lblLogoIcon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblLogoText = new JLabel("Vet Kita", SwingConstants.CENTER);
        lblLogoText.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblLogoText.setForeground(new Color(0x1F2A44));
        lblLogoText.setAlignmentX(CENTER_ALIGNMENT);

        pnlLogoContainer.add(lblLogoIcon);
        pnlLogoContainer.add(Box.createVerticalStrut(5));
        pnlLogoContainer.add(lblLogoText);

        pnlSidebar.add(pnlLogoContainer);
        pnlSidebar.add(Box.createVerticalStrut(40)); // Spacing between logo and menu items

        // --- Navigation Buttons ---
        btnDashboard = new JButton("Dashboard");
        btnPatients = new JButton("Patients");
        btnStaff = new JButton("Staff");
        btnTreatments = new JButton("Treatments");
        btnLogout = new JButton("Logout");

        styleSidebarButton(btnDashboard);
        styleSidebarButton(btnPatients);
        styleSidebarButton(btnStaff);
        styleSidebarButton(btnTreatments);
        styleSidebarButton(btnLogout);

        pnlSidebar.add(btnDashboard);
        pnlSidebar.add(Box.createVerticalStrut(8));
        pnlSidebar.add(btnPatients);
        pnlSidebar.add(Box.createVerticalStrut(8));
        pnlSidebar.add(btnStaff);
        pnlSidebar.add(Box.createVerticalStrut(8));
        pnlSidebar.add(btnTreatments);
        pnlSidebar.add(Box.createVerticalGlue());   // Pushes Logout to the bottom
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
        topHeaderPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        topHeaderPanel.setBackground(new Color(0xF4F7FC));

        // --- Left: Title and Subtitle Stack ---
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        lblPageTitle = new JLabel("Dashboard");
        lblPageTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblPageTitle.setForeground(new Color(0x1F2A44));

        lblPageSubtitle = new JLabel("A quick look at the clinic");
        lblPageSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblPageSubtitle.setForeground(new Color(0x6B7A99));

        titlePanel.add(lblPageTitle);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(lblPageSubtitle);
        topHeaderPanel.add(titlePanel, BorderLayout.WEST);

        // --- Right: Notifications & Profile ---
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        profilePanel.setOpaque(false);

        btnNotifications = new JButton("Notifications");
        btnNotifications.setFocusPainted(false);
        btnNotifications.addActionListener(e -> handleNotifications());

        JPanel userTextPanel = new JPanel(new GridLayout(2, 1));
        userTextPanel.setOpaque(false);
        lblUserName = new JLabel("Front Desk Admin");
        lblUserName.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblUserRole = new JLabel("System Administrator");
        lblUserRole.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblUserRole.setForeground(new Color(0x6B7A99));

        userTextPanel.add(lblUserName);
        userTextPanel.add(lblUserRole);

        profilePanel.add(btnNotifications);
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

        btnDashboard.setBackground(pageName.equals("Dashboard") ? new Color(0xD6E4FF) : new Color(0xEAF1FD));
        btnPatients.setBackground(pageName.equals("Patients") ? new Color(0xD6E4FF) : new Color(0xEAF1FD));
        btnStaff.setBackground(pageName.equals("Staff") ? new Color(0xD6E4FF) : new Color(0xEAF1FD));
        btnTreatments.setBackground(pageName.equals("Treatments") ? new Color(0xD6E4FF) : new Color(0xEAF1FD));
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
            // Close the current MainFrame window
            dispose();

            // Re-open the Login window with both manager and authService
            new vetclinic.ui.LoginFrame(manager, authService).setVisible(true);
        }
    }

    private void handleSaveAll() {
        try {
            manager.saveAll();
            JOptionPane.showMessageDialog(this, "All records were saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (StorageException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
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
        new NotificationDialog(this, manager).setVisible(true);
    }

    private void styleSidebarButton(JButton button) {
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setBackground(new Color(0xEAF1FD));
        button.setForeground(new Color(0x1F2A44));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 15));

        // 10px top, 25px left (leaves space for future icon), 10px bottom, 10px right
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 10));
    }
}