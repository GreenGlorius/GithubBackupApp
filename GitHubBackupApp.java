import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class GitHubBackupApp extends JFrame {
    private JTextField orgTextField;
    private JTextField gitNameTextField;
    private JTextField gitEmailTextField;
    private JTextField workspaceTextField;
    private JTextArea logTextArea;
    private JButton runButton;
    private final File configFile = new File("config.properties");

    public GitHubBackupApp() {
        setDarkTheme();

        setTitle("GitHub Backup Manager");
        setSize(850, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        loadConfig();
    }

    private void setDarkTheme() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            
            Color darkBackground = new Color(43, 45, 48);
            Color darkPanel = new Color(60, 63, 65);
            Color textForeground = new Color(187, 187, 187);
            Color inputBackground = new Color(69, 73, 74);

            UIManager.put("Panel.background", darkBackground);
            UIManager.put("OptionPane.background", darkBackground);
            UIManager.put("Panel.foreground", textForeground);
            UIManager.put("Label.foreground", textForeground);
            UIManager.put("TextField.background", inputBackground);
            UIManager.put("TextField.foreground", Color.WHITE);
            UIManager.put("TextField.caretForeground", Color.WHITE);
            UIManager.put("TextArea.background", new Color(30, 31, 34));
            UIManager.put("TextArea.foreground", new Color(49, 231, 110));
            UIManager.put("Button.background", darkPanel);
            UIManager.put("Button.foreground", Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel Superior: Configuración
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100)), 
            " Configuración de Usuario, Organización y Directorio ", 
            0, 0, new Font("SansSerif", Font.BOLD, 12), Color.WHITE
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        // Campos
        orgTextField = new JTextField(25);
        gitNameTextField = new JTextField(25);
        gitEmailTextField = new JTextField(25);
        workspaceTextField = new JTextField(25);

        addFormField(configPanel, gbc, 0, "Organización GitHub:", orgTextField, "e.g., my-org-name");
        addFormField(configPanel, gbc, 1, "Nombre (Git User):", gitNameTextField, "e.g., Dad User");
        addFormField(configPanel, gbc, 2, "Correo (Git Email):", gitEmailTextField, "e.g., dad@example.com");

        // Fila especial para la Carpeta de Proyectos con botón Examinar
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblWorkspace = new JLabel("Carpeta de Proyectos:");
        lblWorkspace.setForeground(Color.WHITE);
        configPanel.add(lblWorkspace, gbc);

        JPanel workspacePanel = new JPanel(new BorderLayout(5, 0));
        workspacePanel.setBackground(new Color(43, 45, 48));
        workspaceTextField.setEditable(false); // Para forzar el uso del selector
        workspacePanel.add(workspaceTextField, BorderLayout.CENTER);

        JButton browseButton = new JButton("Examinar...");
        browseButton.addActionListener(e -> seleccionarCarpetaWorkspace());
        workspacePanel.add(browseButton, BorderLayout.EAST);

        gbc.gridx = 1; 
        configPanel.add(workspacePanel, gbc);

        // Botón Guardar Configuración
        JButton saveButton = new JButton("Guardar Configuración");
        saveButton.addActionListener(e -> saveConfig());
        gbc.gridx = 1; gbc.gridy = 4; gbc.anchor = GridBagConstraints.CENTER;
        configPanel.add(saveButton, gbc);

        add(configPanel, BorderLayout.NORTH);

        // Panel Central: Consola de Registro
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100)), 
            " Registro de Actividad ", 
            0, 0, new Font("SansSerif", Font.BOLD, 12), Color.WHITE
        ));
        add(scrollPane, BorderLayout.CENTER);

        // Panel Inferior: Botón de Ejecución
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        runButton = new JButton("Iniciar Respaldo de Carpetas");
        runButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        runButton.setBackground(new Color(40, 120, 60));
        runButton.setForeground(Color.WHITE);
        runButton.setPreferredSize(new Dimension(300, 40));
        runButton.addActionListener(e -> ejecutarRespaldoConValidacion());
        bottomPanel.add(runButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JTextField textField, String tooltip) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel label = new JLabel(labelText);
        label.setForeground(Color.WHITE);
        panel.add(label, gbc);

        gbc.gridx = 1;
        textField.setToolTipText(tooltip);
        panel.add(textField, gbc);
    }

    private void seleccionarCarpetaWorkspace() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            workspaceTextField.setText(selectedDir.getAbsolutePath());
        }
    }

    private void saveConfig() {
        try (OutputStream output = new FileOutputStream(configFile)) {
            Properties prop = new Properties();
            prop.setProperty("github.org", orgTextField.getText().trim());
            prop.setProperty("git.name", gitNameTextField.getText().trim());
            prop.setProperty("git.email", gitEmailTextField.getText().trim());
            prop.setProperty("workspace.dir", workspaceTextField.getText().trim());
            prop.store(output, null);
            logTextArea.append("[INFO] Configuración guardada correctamente.\n");
        } catch (IOException io) {
            logTextArea.append("[ERROR] No se pudo guardar la configuración.\n");
        }
    }

    private void loadConfig() {
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                Properties prop = new Properties();
                prop.load(input);
                orgTextField.setText(prop.getProperty("github.org", ""));
                gitNameTextField.setText(prop.getProperty("git.name", ""));
                gitEmailTextField.setText(prop.getProperty("git.email", ""));
                workspaceTextField.setText(prop.getProperty("workspace.dir", ""));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void ejecutarRespaldoConValidacion() {
        String name = gitNameTextField.getText().trim();
        String email = gitEmailTextField.getText().trim();
        String workspacePath = workspaceTextField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || workspacePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor completa todos los campos y selecciona la carpeta de proyectos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File workspaceDir = new File(workspacePath);
        if (!workspaceDir.exists() || !workspaceDir.isDirectory()) {
            JOptionPane.showMessageDialog(this, "La carpeta de proyectos seleccionada no es válida.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validar y configurar Git localmente
        verificarYConfigurarGit(name, email);

        logTextArea.append("[INFO] Directorio de trabajo seleccionado: " + workspacePath + "\n");
        logTextArea.append("[INFO] Iniciando proceso de respaldo de repositorios...\n");
        
        // Aquí puedes usar 'workspaceDir' para recorrer las subcarpetas de los proyectos de tu papá
    }

    private void verificarYConfigurarGit(String expectedName, String expectedEmail) {
        try {
            String currentName = ejecutarComandoGit("git config --global user.name");
            String currentEmail = ejecutarComandoGit("git config --global user.email");

            if (currentName.isEmpty() || !currentName.equals(expectedName)) {
                ejecutarComandoGit("git config --global user.name \"" + expectedName + "\"");
                logTextArea.append("[GIT] Usuario configurado automáticamente: " + expectedName + "\n");
            }

            if (currentEmail.isEmpty() || !currentEmail.equals(expectedEmail)) {
                ejecutarComandoGit("git config --global user.email \"" + expectedEmail + "\"");
                logTextArea.append("[GIT] Correo configurado automáticamente: " + expectedEmail + "\n");
            }
        } catch (Exception e) {
            logTextArea.append("[ERROR] No se pudo verificar o configurar Git: " + e.getMessage() + "\n");
        }
    }

    private String ejecutarComandoGit(String comando) {
        try {
            Process process = Runtime.getRuntime().exec(comando);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line.trim());
            }
            process.waitFor();
            return output.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GitHubBackupApp().setVisible(true));
    }
}
