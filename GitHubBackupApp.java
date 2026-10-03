import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;
import java.util.Date;

public class GitHubBackupApp extends JFrame {
    private JTextField orgTextField;
    private JTextField gitNameTextField;
    private JTextField gitEmailTextField;
    private JTextField workspaceTextField;
    private JTextArea logTextArea;
    private JButton runButton;
    private final File configFile = new File("config.properties");

    public GitHubBackupApp() {
        setTitle("GitHub Backup Manager");
        setSize(850, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        loadConfig();
    }

    private static void aplicarTemaOscuroTotal() {
        try {
            // Forzar colores oscuros a nivel global del sistema de ventanas (Metal/CrossPlatform mejorado)
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            
            Color fondoOscuro = new Color(43, 45, 48);
            Color panelOscuro = new Color(49, 51, 53);
            Color textoClaro = new Color(220, 220, 220);
            Color inputOscuro = new Color(60, 63, 65);
            Color bordeGris = new Color(80, 80, 80);

            UIManager.put("Panel.background", fondoOscuro);
            UIManager.put("OptionPane.background", fondoOscuro);
            UIManager.put("Panel.foreground", textoClaro);
            UIManager.put("Label.foreground", textoClaro);
            UIManager.put("TextField.background", inputOscuro);
            UIManager.put("TextField.foreground", Color.WHITE);
            UIManager.put("TextField.caretForeground", Color.WHITE);
            UIManager.put("TextField.border", BorderFactory.createLineBorder(bordeGris));
            UIManager.put("TextArea.background", new Color(30, 31, 34));
            UIManager.put("TextArea.foreground", new Color(49, 231, 110));
            UIManager.put("Button.background", panelOscuro);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.border", BorderFactory.createLineBorder(bordeGris));
            UIManager.put("TitledBorder.titleColor", Color.WHITE);
            UIManager.put("ScrollPane.background", fondoOscuro);
            UIManager.put("ScrollBar.background", fondoOscuro);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        // Fondo general de la ventana principal oscuro
        getContentPane().setBackground(new Color(43, 45, 48));
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel Superior: Configuración
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBackground(new Color(43, 45, 48));
        configPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(90, 93, 95)), 
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

        // Fila para la Carpeta de Proyectos con botón Examinar
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblWorkspace = new JLabel("Carpeta de Proyectos:");
        lblWorkspace.setForeground(Color.WHITE);
        configPanel.add(lblWorkspace, gbc);

        JPanel workspacePanel = new JPanel(new BorderLayout(5, 0));
        workspacePanel.setBackground(new Color(43, 45, 48));
        workspaceTextField.setEditable(false);
        workspacePanel.add(workspaceTextField, BorderLayout.CENTER);

        JButton browseButton = new JButton("Examinar...");
        browseButton.setBackground(new Color(60, 63, 65));
        browseButton.setForeground(Color.WHITE);
        browseButton.addActionListener(e -> seleccionarCarpetaWorkspace());
        workspacePanel.add(browseButton, BorderLayout.EAST);

        gbc.gridx = 1; 
        configPanel.add(workspacePanel, gbc);

        // Botón Guardar Configuración
        JButton saveButton = new JButton("Guardar Configuración");
        saveButton.setBackground(new Color(60, 63, 65));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> saveConfig());
        gbc.gridx = 1; gbc.gridy = 4; gbc.anchor = GridBagConstraints.CENTER;
        configPanel.add(saveButton, gbc);

        add(configPanel, BorderLayout.NORTH);

        // Panel Central: Consola de Registro
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        scrollPane.setBackground(new Color(43, 45, 48));
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(90, 93, 95)), 
            " Registro de Actividad ", 
            0, 0, new Font("SansSerif", Font.BOLD, 12), Color.WHITE
        ));
        add(scrollPane, BorderLayout.CENTER);

        // Panel Inferior: Botón de Ejecución
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(new Color(43, 45, 48));
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
        String org = orgTextField.getText().trim();
        String name = gitNameTextField.getText().trim();
        String email = gitEmailTextField.getText().trim();
        String workspacePath = workspaceTextField.getText().trim();

        if (org.isEmpty() || name.isEmpty() || email.isEmpty() || workspacePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor completa todos los campos y selecciona la carpeta de proyectos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File workspaceDir = new File(workspacePath);
        if (!workspaceDir.exists() || !workspaceDir.isDirectory()) {
            JOptionPane.showMessageDialog(this, "La carpeta de proyectos seleccionada no es válida.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        runButton.setEnabled(false);
        logTextArea.setText("");

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                publish("[INFO] Verificando configuración de Git...");
                verificarYConfigurarGit(name, email);

                File[] subCarpetas = workspaceDir.listFiles(File::isDirectory);
                if (subCarpetas == null || subCarpetas.length == 0) {
                    publish("[INFO] No se encontraron carpetas en el directorio de trabajo.");
                    return null;
                }

                publish("[INFO] Directorio seleccionado: " + workspacePath);
                publish("[INFO] Organización de destino: " + org);
                publish("--------------------------------------------------\n");

                for (File carpetaProyecto : subCarpetas) {
                    String nombreRepo = carpetaProyecto.getName();
                    if (nombreRepo.startsWith(".")) continue;

                    publish("Procesando carpeta: " + nombreRepo);

                    File gitDir = new File(carpetaProyecto, ".git");
                    if (!gitDir.exists()) {
                        publish(" > Carpeta sin Git. Inicializando...");
                        ejecutarComandoConSalida("git init", carpetaProyecto);
                        ejecutarComandoConSalida("git branch -M main", carpetaProyecto);
                    } else {
                        publish(" > Repositorio Git existente encontrado.");
                    }

                    String repoFullName = org + "/" + nombreRepo;
                    publish(" > Verificando repositorio en GitHub (" + repoFullName + ")...");
                    
                    int checkRepo = ejecutarCodigoSalida("gh repo view " + repoFullName, carpetaProyecto);
                    if (checkRepo != 0) {
                        publish(" > Creando repositorio privado en GitHub: " + repoFullName + "...");
                        ejecutarComandoConSalida("gh repo create " + repoFullName + " --private --source=. --remote=origin", carpetaProyecto);
                    } else {
                        ejecutarComandoConSalida("git remote remove origin", carpetaProyecto);
                        ejecutarComandoConSalida("git remote add origin https://github.com/" + repoFullName + ".git", carpetaProyecto);
                    }

                    publish(" > Guardando cambios locales...");
                    ejecutarComandoConSalida("git add .", carpetaProyecto);
                    
                    String status = ejecutarCapturaSalida("git status --porcelain", carpetaProyecto);
                    if (!status.isEmpty()) {
                        ejecutarComandoConSalida("git commit -m \"Respaldo automático: " + new Date() + "\"", carpetaProyecto);
                        publish(" > Subiendo cambios a GitHub...");
                        ejecutarComandoConSalida("git push -u origin main", carpetaProyecto);
                        publish(" > ¡Respaldo completado con éxito!\n");
                    } else {
                        publish(" > No hay cambios nuevos para respaldar.\n");
                    }
                    publish("--------------------------------------------------");
                }

                publish("[INFO] ¡Proceso de respaldo de todas las carpetas finalizado!");
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String mensaje : chunks) {
                    logTextArea.append(mensaje + "\n");
                    logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
                }
            }

            @Override
            protected void done() {
                runButton.setEnabled(true);
            }
        };

        worker.execute();
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

    private void ejecutarComandoConSalida(String comando, File directorio) {
        try {
            ProcessBuilder builder;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                builder = new ProcessBuilder("cmd.exe", "/c", comando);
            } else {
                builder = new ProcessBuilder("bash", "-c", comando);
            }
            builder.directory(directorio);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int ejecutarCodigoSalida(String comando, File directorio) {
        try {
            ProcessBuilder builder;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                builder = new ProcessBuilder("cmd.exe", "/c", comando);
            } else {
                builder = new ProcessBuilder("bash", "-c", comando);
            }
            builder.directory(directorio);
            Process process = builder.start();
            return process.waitFor();
        } catch (Exception e) {
            return -1;
        }
    }

    private String ejecutarCapturaSalida(String comando, File directorio) {
        try {
            ProcessBuilder builder;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                builder = new ProcessBuilder("cmd.exe", "/c", comando);
            } else {
                builder = new ProcessBuilder("bash", "-c", comando);
            }
            builder.directory(directorio);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            process.waitFor();
            return sb.toString().trim();
        } catch (Exception e) {
            return "";
        }
    }

    public static void main(String[] args) {
        // Aplicar el tema oscuro antes de inicializar la interfaz gráfica
        aplicarTemaOscuroTotal();
        SwingUtilities.invokeLater(() -> new GitHubBackupApp().setVisible(true));
    }
}
