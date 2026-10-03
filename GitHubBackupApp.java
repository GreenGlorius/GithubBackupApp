import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class GitHubBackupApp extends JFrame {
    private JTextField orgTextField;
    private JTextField gitNameTextField;
    private JTextField gitEmailTextField;
    private JTextArea logTextArea;
    private JButton runButton;
    private final File configFile = new File("config.properties");

    public GitHubBackupApp() {
        setTitle("Auto Backup a GitHub");
        setSize(750, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        loadConfig();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JPanel prereqPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        prereqPanel.setBorder(BorderFactory.createTitledBorder("1. ¿Te falta algún programa? (Instálalos primero)"));
        
        JButton btnGit = new JButton("Instalar Git");
        btnGit.setFocusPainted(false);
        btnGit.addActionListener(e -> openWebPage("https://git-scm.com/downloads"));
        
        JButton btnGh = new JButton("Instalar GitHub CLI");
        btnGh.setFocusPainted(false);
        btnGh.addActionListener(e -> openWebPage("https://cli.github.com/"));

        prereqPanel.add(btnGit);
        prereqPanel.add(btnGh);
        topContainer.add(prereqPanel);

        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder("2. Configuración de Usuario y Organización"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        configPanel.add(new JLabel("Organización GitHub:"), gbc);
        gbc.gridx = 1; 
        orgTextField = new JTextField(22);
        configPanel.add(orgTextField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        configPanel.add(new JLabel("Nombre (Git User):"), gbc);
        gbc.gridx = 1; 
        gitNameTextField = new JTextField(22);
        configPanel.add(gitNameTextField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        configPanel.add(new JLabel("Correo (Git Email):"), gbc);
        gbc.gridx = 1; 
        gitEmailTextField = new JTextField(22);
        configPanel.add(gitEmailTextField, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        JButton saveButton = new JButton("Guardar Configuración");
        saveButton.setBackground(new Color(70, 130, 180));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> saveConfigAndApplyGit());
        configPanel.add(saveButton, gbc);

        topContainer.add(configPanel);
        add(topContainer, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        centerPanel.add(new JLabel("Registro de Actividad:"), BorderLayout.NORTH);
        
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setBackground(new Color(30, 30, 30));
        logTextArea.setForeground(new Color(0, 255, 0));
        logTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        runButton = new JButton("Iniciar Respaldo de Carpetas");
        runButton.setFont(new Font("Arial", Font.BOLD, 12));
        runButton.setBackground(new Color(76, 175, 80));
        runButton.setForeground(Color.WHITE);
        runButton.setFocusPainted(false);
        runButton.addActionListener(e -> startBackupThread());
        
        bottomPanel.add(runButton);
        
        JLabel infoLabel = new JLabel("Recuerda ejecutar 'gh auth login' en tu terminal la primera vez.");
        infoLabel.setForeground(Color.GRAY);
        bottomPanel.add(infoLabel);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void openWebPage(String urlString) {
        try {
            Desktop.getDesktop().browse(new URI(urlString));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir el navegador: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadConfig() {
        if (configFile.exists()) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                orgTextField.setText(props.getProperty("orgName", ""));
                gitNameTextField.setText(props.getProperty("gitName", ""));
                gitEmailTextField.setText(props.getProperty("gitEmail", ""));
            } catch (IOException e) {
                log("[ERROR] No se pudo leer el archivo de configuración.");
            }
        }
    }

    private void saveConfigAndApplyGit() {
        String orgName = orgTextField.getText().trim();
        String gitName = gitNameTextField.getText().trim();
        String gitEmail = gitEmailTextField.getText().trim();

        if (orgName.isEmpty() || gitName.isEmpty() || gitEmail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor completa todos los campos de configuración.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Properties props = new Properties();
        props.setProperty("orgName", orgName);
        props.setProperty("gitName", gitName);
        props.setProperty("gitEmail", gitEmail);
        
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            props.store(fos, "Configuracion de GitHub Backup");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar el archivo config.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            ProcessBuilder pbName = new ProcessBuilder("git", "config", "--global", "user.name", gitName);
            ProcessBuilder pbEmail = new ProcessBuilder("git", "config", "--global", "user.email", gitEmail);
            
            Process pName = pbName.start();
            int exitCodeName = pName.waitFor();

            Process pEmail = pbEmail.start();
            int exitCodeEmail = pEmail.waitFor();

            if (exitCodeName == 0 && exitCodeEmail == 0) {
                JOptionPane.showMessageDialog(this, "¡Configuración guardada y Git configurado con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Se guardaron los datos, pero hubo un problema al configurar Git globalmente. ¿Tienes Git instalado?", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al intentar ejecutar comandos de Git: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logTextArea.append(message + "\n");
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
        });
    }

    private void startBackupThread() {
        String orgName = orgTextField.getText().trim();
        if (orgName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingresa y guarda el nombre de tu organización primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        runButton.setEnabled(false);
        runButton.setBackground(Color.GRAY);
        logTextArea.setText("");

        new Thread(() -> runBackupProcess(orgName)).start();
    }

    private void runBackupProcess(String orgName) {
        File rootPath = new File(System.getProperty("user.dir"));
        log("Directorio de trabajo: " + rootPath.getAbsolutePath() + "\n");

        try {
            File[] folders = rootPath.listFiles(File::isDirectory);
            if (folders == null || folders.length == 0) {
                log("[AVISO] No se encontraron carpetas secundarias.");
                return;
            }

            for (File folder : folders) {
                if (folder.getName().startsWith(".")) continue;

                log("\n------------------------------------------");
                log("Procesando carpeta: " + folder.getName());

                File gitPath = new File(folder, ".git");

                if (!gitPath.exists()) {
                    log("-> Carpeta sin Git. Inicializando...");
                    executeCommand(folder, "git", "init", "-b", "main");

                    File[] contents = folder.listFiles(f -> !f.getName().equals(".git"));
                    if (contents == null || contents.length == 0) {
                        File readme = new File(folder, "README.md");
                        try (FileWriter fw = new FileWriter(readme)) {
                            fw.write("Respaldo automático inicial\n");
                        }
                    }

                    executeCommand(folder, "git", "add", ".");
                    executeCommand(folder, "git", "commit", "-m", "Primer commit automatico (creacion inicial)");

                    log("-> Creando repositorio privado en GitHub: " + orgName + "/" + folder.getName() + "...");
                    
                    ProcessBuilder pb = new ProcessBuilder("gh", "repo", orgName + "/" + folder.getName(), "--private", "--source=.", "--remote=origin", "--push");
                    pb.directory(folder);
                    Process process = pb.start();

                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    StringBuilder errorMsg = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorMsg.append(errorLine).append("\n");
                    }

                    int exitCode = process.waitFor();

                    if (exitCode == 0) {
                        log("-> ¡Éxito! Repositorio creado y vinculado.");
                    } else {
                        log("-> [ERROR] No se pudo crear el repositorio en GitHub.");
                        log("-> Motivo: " + errorMsg.toString().trim());
                        log("-> Consejo: Verifica que la organización exista, esté bien escrita y tengas permisos.");
                    }
                } else {
                    log("-> Repositorio existente. Verificando cambios...");
                    
                    String status = executeCommandOutput(folder, "git", "status", "--porcelain");
                    if (status != null && !status.trim().isEmpty()) {
                        log("-> Cambios detectados. Guardando respaldo...");
                        executeCommand(folder, "git", "add", ".");
                        
                        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
                        executeCommand(folder, "git", "commit", "-m", "Respaldo automatico: " + fecha);
                        
                        log("-> Subiendo cambios a GitHub...");
                        
                        ProcessBuilder pbPush = new ProcessBuilder("git", "push");
                        pbPush.directory(folder);
                        Process processPush = pbPush.start();

                        BufferedReader errorReaderPush = new BufferedReader(new InputStreamReader(processPush.getErrorStream()));
                        StringBuilder errorMsgPush = new StringBuilder();
                        String errorLinePush;
                        while ((errorLinePush = errorReaderPush.readLine()) != null) {
                            errorMsgPush.append(errorLinePush).append("\n");
                        }

                        int exitCodePush = processPush.waitFor();

                        if (exitCodePush == 0) {
                            log("-> ¡Respaldo completado con éxito!");
                        } else {
                            log("-> [ERROR] Hubo un problema al hacer push.");
                            log("-> Motivo: " + errorMsgPush.toString().trim());
                        }
                    } else {
                        log("-> Sin cambios nuevos. Todo al día.");
                    }
                }
            }

            log("\n==========================================");
            log("¡Proceso de respaldo finalizado con éxito!");
            log("==========================================");

        } catch (Exception e) {
            log("[EXCEPCIÓN CRÍTICA] " + e.getMessage());
        } finally {
            SwingUtilities.invokeLater(() -> {
                runButton.setEnabled(true);
                runButton.setBackground(new Color(76, 175, 80));
            });
        }
    }

    private int executeCommand(File workingDir, String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDir);
        Process process = pb.start();
        return process.waitFor();
    }

    private String executeCommandOutput(File workingDir, String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workingDir);
        Process process = pb.start();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            process.waitFor();
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new GitHubBackupApp().setVisible(true);
        });
    }
}
