package vetclinic.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
    private JPanel pnlSidebar;
    private JPanel pnlContent;          // CardLayout: one "card" (page) per sidebar button
    private JButton btnDashboard;
    private JButton btnPatients;
    private JButton btnStaff;
    private JButton btnTreatments;
    private JButton btnLogout;
    private JLabel lblUserName;         // logged-in staff name
    private JLabel lblUserRole;         // logged-in staff role
    private DashboardPanel dashboardPanel;
    private AnimalPanel animalPanel;
    private StaffPanel staffPanel;
    private TreatmentPanel treatmentPanel;
    private JButton btnNotifications;   // bell icon

    public MainFrame(ClinicManager manager, AuthService authService) {
        super("Veterinary Clinic Patient Record System");
        this.manager = manager;
        this.authService = authService;
        initComponents();
    }

    // Builds the window: the pages first, then the sidebar and the header.
    private void initComponents() {
        setSize(1200, 760);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);

        // We do not close immediately: handleExit() first asks for confirmation and saves.
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });

        setLayout(new BorderLayout());
        dashboardPanel = new DashboardPanel(manager);
        animalPanel = new AnimalPanel(manager, authService);
        staffPanel = new StaffPanel(manager, authService);

        pnlContent = new JPanel(new CardLayout());
        pnlContent.add(dashboardPanel, "Dashboard");
        pnlContent.add(animalPanel, "Patients");
        pnlContent.add(staffPanel, "Staff");
        treatmentPanel = new TreatmentPanel(manager, authService);
        pnlContent.add(treatmentPanel, "Treatments");
        add(pnlContent, BorderLayout.CENTER);

        buildSidebar();
        buildHeader();

        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("File");
        JMenuItem itemSave = new JMenuItem("Save All");
        itemSave.addActionListener(e -> handleSaveAll());
        menuFile.add(itemSave);
        menuBar.add(menuFile);
        setJMenuBar(menuBar);

        showPage("Dashboard");
    }

    // The blue-white bar on the left with the four buttons.
    private void buildSidebar() {
        pnlSidebar = new JPanel();
        pnlSidebar.setLayout(new BoxLayout(pnlSidebar, BoxLayout.Y_AXIS));   // stacks the buttons top to bottom
        pnlSidebar.setBackground(new Color(0xEAF1FD));
        pnlSidebar.setPreferredSize(new Dimension(210, 0));
        pnlSidebar.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        JLabel lblLogo = new JLabel("Vet Kita", SwingConstants.CENTER);
        lblLogo.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblLogo.setForeground(new Color(0x1F2A44));
        lblLogo.setAlignmentX(CENTER_ALIGNMENT);
        pnlSidebar.add(lblLogo);
        pnlSidebar.add(Box.createVerticalStrut(30));

        btnDashboard = new JButton("Dashboard");
        btnPatients = new JButton("Patients");
        btnStaff = new JButton("Staff");
        btnTreatments = new JButton("Treatments");
        styleSidebarButton(btnTreatments);
        btnLogout = new JButton("Logout");
        styleSidebarButton(btnDashboard);
        styleSidebarButton(btnPatients);
        styleSidebarButton(btnStaff);
        styleSidebarButton(btnLogout);

        pnlSidebar.add(btnDashboard);
        pnlSidebar.add(Box.createVerticalStrut(6));
        pnlSidebar.add(btnPatients);
        pnlSidebar.add(Box.createVerticalStrut(6));
        pnlSidebar.add(btnStaff);
        pnlSidebar.add(Box.createVerticalStrut(6));
        pnlSidebar.add(btnTreatments);
        pnlSidebar.add(Box.createVerticalGlue());   // pushes Logout to the bottom
        pnlSidebar.add(btnLogout);

        btnDashboard.addActionListener(e -> showPage("Dashboard"));
        btnPatients.addActionListener(e -> showPage("Patients"));
        btnStaff.addActionListener(e -> showPage("Staff"));
        btnTreatments.addActionListener(e -> showPage("Treatments"));
        btnLogout.addActionListener(e -> handleLogout());

        add(pnlSidebar, BorderLayout.WEST);
    }

    // The thin bar at the top: bell icon + the logged-in user's name and role.
    private void buildHeader() {
        JPanel pnlHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        pnlHeader.setBackground(new Color(0xF4F7FC));

        btnNotifications = new JButton("Notifications");
        btnNotifications.setFocusPainted(false);
        btnNotifications.setToolTipText("Checkup notifications");
        btnNotifications.addActionListener(e -> handleNotifications());

        // Displays the standard kiosk profile in the header
        lblUserName = new JLabel("Front Desk Admin");
        lblUserName.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblUserRole = new JLabel("System Administrator");
        lblUserRole.setForeground(new Color(0x6B7A99));

        JPanel pnlUser = new JPanel();
        pnlUser.setOpaque(false);
        pnlUser.setLayout(new BoxLayout(pnlUser, BoxLayout.Y_AXIS));
        pnlUser.add(lblUserName);
        pnlUser.add(lblUserRole);

        pnlHeader.add(btnNotifications);
        pnlHeader.add(pnlUser);
        add(pnlHeader, BorderLayout.NORTH);
    }

    // Shows one page and refreshes it so it always displays the newest data.
    private void showPage(String pageName) {
        ((CardLayout) pnlContent.getLayout()).show(pnlContent, pageName);

        if (pageName.equals("Dashboard")) {
            dashboardPanel.refresh();
        } else if (pageName.equals("Patients")) {
            animalPanel.refreshTable(manager.getAllAnimals());
        } else if (pageName.equals("Staff")) {
            staffPanel.refreshTable(manager.getAllStaff());
        } else if (pageName.equals("Treatments")) {
            treatmentPanel.refreshTable(manager.getAllTreatments());
        }

        // Highlight the button of the page we are on.
        btnDashboard.setBackground(pageName.equals("Dashboard") ? new Color(0xD6E4FF) : new Color(0xEAF1FD));
        btnPatients.setBackground(pageName.equals("Patients") ? new Color(0xD6E4FF) : new Color(0xEAF1FD));
        btnStaff.setBackground(pageName.equals("Staff") ? new Color(0xD6E4FF) : new Color(0xEAF1FD));
        btnTreatments.setBackground(pageName.equals("Treatments") ? new Color(0xD6E4FF) : new Color(0xEAF1FD));
    }

    // Logout: ask, save, forget the user, go back to the login window.
    private void handleLogout() {
        int answer = JOptionPane.showConfirmDialog(this, "Do you want to log out?", "Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            manager.saveAll();
        } catch (StorageException e) {
            JOptionPane.showMessageDialog(this, "Could not save, so you stay logged in:\n" + e.getMessage(),
                    "Save error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        authService.logout();
        dispose();
        new LoginFrame(manager, authService).setVisible(true);
    }

    // Manual save from the File menu.
    private void handleSaveAll() {
        try {
            manager.saveAll();
            JOptionPane.showMessageDialog(this, "All records were saved.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (StorageException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Save error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Exit: ask, save automatically, close the program.
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
                    "Could not save:\n" + e.getMessage() + "\n\nExit anyway and lose the unsaved changes?",
                    "Save error", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (again != JOptionPane.YES_OPTION) {
                return;
            }
        }
        dispose();
        System.exit(0);
    }

    // Opens the list of checkups that are due or coming up.
    private void handleNotifications() {
        new NotificationDialog(this, manager).setVisible(true);
    }

    // Same look for all sidebar buttons (extra helper, see DEVIATIONS).
    private void styleSidebarButton(JButton button) {
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setBackground(new Color(0xEAF1FD));
        button.setForeground(new Color(0x1F2A44));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
    }
}
