package com.textqr.converter.ui;

import com.textqr.converter.core.QRGenerator;
import com.textqr.converter.core.TextInputHandler;
import com.textqr.converter.model.QRChunk;
import com.textqr.converter.model.QRSession;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MainWindow {
    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);
    
    private final QRGenerator qrGenerator;
    private final TextInputHandler textInputHandler;
    
    private TextArea inputTextArea;
    private Label statusLabel;
    private VBox qrDisplayArea;
    private CheckBox compressionCheckBox;
    private Button generateButton;
    private ProgressBar progressBar;
    
    private QRSession currentSession;
    private List<BufferedImage> currentQRImages;
    
    public MainWindow(QRGenerator qrGenerator, TextInputHandler textInputHandler) {
        this.qrGenerator = qrGenerator;
        this.textInputHandler = textInputHandler;
        this.currentQRImages = new ArrayList<>();
    }
    
    public void show(Stage primaryStage) {
        primaryStage.setTitle("Text to QR Code Converter");
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // Create menu bar
        MenuBar menuBar = createMenuBar(primaryStage);
        root.setTop(menuBar);
        
        // Create main content
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.4);
        
        // Left side - Input area
        VBox inputPane = createInputPane(primaryStage);
        
        // Right side - QR display area
        ScrollPane scrollPane = new ScrollPane();
        qrDisplayArea = new VBox(10);
        qrDisplayArea.setAlignment(Pos.TOP_CENTER);
        qrDisplayArea.setPadding(new Insets(10));
        scrollPane.setContent(qrDisplayArea);
        
        splitPane.getItems().addAll(inputPane, scrollPane);
        root.setCenter(splitPane);
        
        // Bottom - Status bar
        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);
        
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private MenuBar createMenuBar(Stage stage) {
        MenuBar menuBar = new MenuBar();
        
        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open File...");
        MenuItem saveItem = new MenuItem("Save QR Codes...");
        MenuItem exitItem = new MenuItem("Exit");
        
        openItem.setOnAction(e -> openFile(stage));
        saveItem.setOnAction(e -> saveQRCodes(stage));
        exitItem.setOnAction(e -> Platform.exit());
        
        fileMenu.getItems().addAll(openItem, saveItem, new SeparatorMenuItem(), exitItem);
        
        // Edit menu
        Menu editMenu = new Menu("Edit");
        MenuItem copyItem = new MenuItem("Copy from Clipboard");
        MenuItem pasteItem = new MenuItem("Paste to Clipboard");
        MenuItem clearItem = new MenuItem("Clear");
        
        copyItem.setOnAction(e -> copyFromClipboard());
        pasteItem.setOnAction(e -> pasteToClipboard());
        clearItem.setOnAction(e -> clearInput());
        
        editMenu.getItems().addAll(copyItem, pasteItem, new SeparatorMenuItem(), clearItem);
        
        // Help menu
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAboutDialog());
        helpMenu.getItems().add(aboutItem);
        
        menuBar.getMenus().addAll(fileMenu, editMenu, helpMenu);
        return menuBar;
    }
    
    private VBox createInputPane(Stage stage) {
        VBox inputPane = new VBox(10);
        inputPane.setPadding(new Insets(10));
        
        Label inputLabel = new Label("Input Text:");
        inputLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        inputTextArea = new TextArea();
        inputTextArea.setPromptText("Enter or paste your text here...");
        inputTextArea.setWrapText(true);
        inputTextArea.setPrefRowCount(20);
        
        // Input options
        HBox optionsBox = new HBox(10);
        optionsBox.setAlignment(Pos.CENTER_LEFT);
        
        compressionCheckBox = new CheckBox("Enable Compression");
        compressionCheckBox.setSelected(true);
        
        Label statsLabel = new Label("0 characters");
        inputTextArea.textProperty().addListener((obs, old, text) -> {
            int chars = text.length();
            int lines = text.split("\n").length;
            statsLabel.setText(String.format("%d characters, %d lines", chars, lines));
        });
        
        optionsBox.getChildren().addAll(compressionCheckBox, new Region(), statsLabel);
        HBox.setHgrow(optionsBox.getChildren().get(1), Priority.ALWAYS);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        generateButton = new Button("Generate QR Codes");
        generateButton.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        generateButton.setOnAction(e -> generateQRCodes());
        
        Button loadFileButton = new Button("Load from File");
        loadFileButton.setOnAction(e -> openFile(stage));
        
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> clearInput());
        
        buttonBox.getChildren().addAll(generateButton, loadFileButton, clearButton);
        
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);
        
        inputPane.getChildren().addAll(
            inputLabel, 
            inputTextArea, 
            optionsBox, 
            buttonBox,
            progressBar
        );
        
        VBox.setVgrow(inputTextArea, Priority.ALWAYS);
        
        return inputPane;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");
        
        statusLabel = new Label("Ready");
        statusBar.getChildren().add(statusLabel);
        
        return statusBar;
    }
    
    private void generateQRCodes() {
        String text = inputTextArea.getText().trim();
        if (text.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Input", "Please enter some text to convert to QR codes.");
            return;
        }
        
        generateButton.setDisable(true);
        progressBar.setVisible(true);
        progressBar.setProgress(-1);
        statusLabel.setText("Generating QR codes...");
        
        final String finalText = text;
        final boolean useCompression = compressionCheckBox.isSelected();
        
        CompletableFuture.runAsync(() -> {
            try {
                // Validate and normalize text
                String normalizedText = textInputHandler.validateAndNormalizeText(finalText);
                
                // Create QR session
                currentSession = qrGenerator.createSession(normalizedText, useCompression);
                
                // Generate QR images
                currentQRImages.clear();
                List<QRChunk> chunks = currentSession.getChunks();
                
                for (int i = 0; i < chunks.size(); i++) {
                    QRChunk chunk = chunks.get(i);
                    BufferedImage qrImage = qrGenerator.generateQRCode(chunk);
                    currentQRImages.add(qrImage);
                    
                    final int progress = i + 1;
                    Platform.runLater(() -> {
                        progressBar.setProgress((double) progress / chunks.size());
                        statusLabel.setText(String.format("Generated %d of %d QR codes", progress, chunks.size()));
                    });
                }
                
                Platform.runLater(() -> {
                    displayQRCodes();
                    progressBar.setVisible(false);
                    generateButton.setDisable(false);
                    statusLabel.setText(String.format("Generated %d QR code(s) successfully", currentQRImages.size()));
                });
                
            } catch (Exception e) {
                logger.error("Error generating QR codes", e);
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Generation Error", "Failed to generate QR codes: " + e.getMessage());
                    progressBar.setVisible(false);
                    generateButton.setDisable(false);
                    statusLabel.setText("Error: " + e.getMessage());
                });
            }
        });
    }
    
    private void displayQRCodes() {
        qrDisplayArea.getChildren().clear();
        
        if (currentQRImages.isEmpty()) {
            return;
        }
        
        // Display session info
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(10));
        infoBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1;");
        
        Label sessionLabel = new Label("Session ID: " + currentSession.getSessionId().substring(0, 8));
        Label chunksLabel = new Label("Total QR Codes: " + currentSession.getChunkCount());
        Label sizeLabel = new Label("Original Size: " + currentSession.getTotalSize() + " bytes");
        
        infoBox.getChildren().addAll(sessionLabel, chunksLabel, sizeLabel);
        qrDisplayArea.getChildren().add(infoBox);
        
        // Display QR codes
        for (int i = 0; i < currentQRImages.size(); i++) {
            BufferedImage qrImage = currentQRImages.get(i);
            
            VBox qrBox = new VBox(5);
            qrBox.setAlignment(Pos.CENTER);
            qrBox.setPadding(new Insets(10));
            qrBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");
            
            ImageView imageView = new ImageView();
            Image fxImage = SwingFXUtils.toFXImage(qrImage, null);
            imageView.setImage(fxImage);
            imageView.setFitWidth(300);
            imageView.setFitHeight(300);
            imageView.setPreserveRatio(true);
            
            Label label = new Label(String.format("QR Code %d of %d", i + 1, currentQRImages.size()));
            label.setStyle("-fx-font-weight: bold;");
            
            qrBox.getChildren().addAll(label, imageView);
            qrDisplayArea.getChildren().add(qrBox);
        }
    }
    
    private void openFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Text File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.log", "*.json", "*.xml"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                String text = textInputHandler.readFromFile(file);
                inputTextArea.setText(text);
                statusLabel.setText("Loaded file: " + file.getName());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "File Error", "Failed to read file: " + e.getMessage());
            }
        }
    }
    
    private void saveQRCodes(Stage stage) {
        if (currentQRImages.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No QR Codes", "Please generate QR codes first.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save QR Codes");
        fileChooser.setInitialFileName("qr_codes_" + currentSession.getSessionId().substring(0, 8));
        
        File directory = fileChooser.showSaveDialog(stage);
        if (directory != null) {
            try {
                File outputDir = new File(directory.getParent(), directory.getName());
                outputDir.mkdirs();
                
                // Save individual QR codes
                for (int i = 0; i < currentQRImages.size(); i++) {
                    String filename = String.format("qr_%03d.png", i + 1);
                    File outputFile = new File(outputDir, filename);
                    qrGenerator.saveQRCode(currentQRImages.get(i), outputFile);
                }
                
                // Save composite if multiple codes
                if (currentQRImages.size() > 1) {
                    BufferedImage composite = qrGenerator.createCompositeImage(currentQRImages, 4);
                    File compositeFile = new File(outputDir, "qr_composite.png");
                    qrGenerator.saveQRCode(composite, compositeFile);
                }
                
                statusLabel.setText("QR codes saved to: " + outputDir.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Success", "QR codes saved successfully!");
                
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save QR codes: " + e.getMessage());
            }
        }
    }
    
    private void copyFromClipboard() {
        try {
            String text = textInputHandler.readFromClipboard();
            inputTextArea.setText(text);
            statusLabel.setText("Text loaded from clipboard");
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Clipboard Error", "Failed to read from clipboard: " + e.getMessage());
        }
    }
    
    private void pasteToClipboard() {
        String text = inputTextArea.getText();
        if (!text.isEmpty()) {
            textInputHandler.copyToClipboard(text);
            statusLabel.setText("Text copied to clipboard");
        }
    }
    
    private void clearInput() {
        inputTextArea.clear();
        qrDisplayArea.getChildren().clear();
        currentQRImages.clear();
        currentSession = null;
        statusLabel.setText("Ready");
    }
    
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Text to QR Code Converter");
        alert.setContentText(
            "Version 1.0.0\n\n" +
            "High-precision text to QR code converter for air-gapped network data transfer.\n\n" +
            "Built with Java and ZXing library."
        );
        alert.showAndWait();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}