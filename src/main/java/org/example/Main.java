package org.example;

import javafx.application.Application;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.*;
import java.util.List;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        Label fileLabel = new Label("Fichier : Aucun fichier sélectionné");
        Button selectFileButton = new Button("Choisir un fichier");
        TextField keywordField = new TextField();
        keywordField.setPromptText("Entrez un mot-clé ou une expression régulière");
        Button searchButton = new Button("Rechercher");
        TextFlow resultsFlow = new TextFlow(); // Pour afficher les résultats formatés
        ScrollPane scrollPane = new ScrollPane(resultsFlow);
        scrollPane.setFitToWidth(true);

        final String[] selectedFilePath = {null};

        selectFileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                selectedFilePath[0] = selectedFile.getAbsolutePath();
                fileLabel.setText("Fichier : " + selectedFile.getAbsolutePath());
            }
        });

        searchButton.setOnAction(event -> {
            if (selectedFilePath[0] == null) {
                resultsFlow.getChildren().clear();
                resultsFlow.getChildren().add(new Text("Veuillez sélectionner un fichier.\n"));
                return;
            }

            try {
                List<Block> blocks = FileHandler.readFileInBlocksWithLines(selectedFilePath[0]);
                String keyword = keywordField.getText();
                List<String> results = SearchTask.searchInBlocksWithLines(blocks, keyword);

                resultsFlow.getChildren().clear();

                if (results.isEmpty()) {
                    resultsFlow.getChildren().add(new Text("Aucune occurrence trouvée.\n"));
                } else {
                    // Afficher le total des occurrences
                    Text totalOccurrences = new Text("Total des occurrences trouvées : " + results.size() + "\n");
                    totalOccurrences.setStyle("-fx-font-weight: bold;");
                    resultsFlow.getChildren().add(totalOccurrences);

                    // Afficher chaque ligne avec le mot en rouge
                    for (String result : results) {
                        String[] parts = result.split(keyword, -1);
                        Text linePrefix = new Text(parts[0]);
                        Text highlightedWord = new Text(keyword);
                        highlightedWord.setStyle("-fx-fill: red; -fx-font-weight: bold;");
                        resultsFlow.getChildren().add(linePrefix);
                        resultsFlow.getChildren().add(highlightedWord);

                        if (parts.length > 1) {
                            Text lineSuffix = new Text(parts[1] + "\n");
                            resultsFlow.getChildren().add(lineSuffix);
                        }
                    }
                }
            } catch (Exception e) {
                resultsFlow.getChildren().clear();
                resultsFlow.getChildren().add(new Text("Erreur : " + e.getMessage() + "\n"));
            }
        });

        VBox layout = new VBox(10, fileLabel, selectFileButton, keywordField, searchButton, scrollPane);
        Scene scene = new Scene(layout, 600, 400);

        primaryStage.setTitle("Moteur de Recherche Textuelle");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
