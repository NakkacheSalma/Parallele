package org.example;

import javafx.application.Application;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.*;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class MainApp2 extends Application {
    @Override
    public void start(Stage primaryStage) {
        Label fileLabel = new Label("Fichier : Aucun fichier sélectionné");
        Button selectFileButton = new Button("Choisir un fichier");
        TextField keywordField = new TextField();
        keywordField.setPromptText("Entrez un mot-clé ou une expression régulière");
        Button searchButton = new Button("Comparer Performances");
        TextArea resultsArea = new TextArea();

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
                resultsArea.setText("Veuillez sélectionner un fichier.\n");
                return;
            }

            try {
                // Lire le fichier et le découper en blocs
                List<Block> blocks = FileHandler.readFileInBlocksWithLines(selectedFilePath[0]);
                String keyword = keywordField.getText();

                // Recherche séquentielle
                long seqStartTime = System.currentTimeMillis();
                List<String> sequentialResults = SequentialSearch.searchInFile(String.join("\n", blocks.stream().map(b -> b.content).toArray(String[]::new)), keyword, 1);
                long seqEndTime = System.currentTimeMillis();
                long seqDuration = seqEndTime - seqStartTime;

                // Recherche multi-niveaux
                long parallelStartTime = System.currentTimeMillis();
                MultiLevelSearchTask parallelTask = new MultiLevelSearchTask(blocks, keyword);
                ForkJoinPool pool = new ForkJoinPool();
                List<String> parallelResults = pool.invoke(parallelTask);
                long parallelEndTime = System.currentTimeMillis();
                long parallelDuration = parallelEndTime - parallelStartTime;

                // Afficher les résultats
                resultsArea.setText("### Comparaison des Performances ###\n");
                resultsArea.appendText("Recherche Séquentielle : " + seqDuration + " ms\n");
                resultsArea.appendText("Occurrences trouvées : " + sequentialResults.size() + "\n\n");

                resultsArea.appendText("Recherche Parallèle Multi-Niveaux : " + parallelDuration + " ms\n");
                resultsArea.appendText("Occurrences trouvées : " + parallelResults.size() + "\n\n");

            } catch (Exception e) {
                resultsArea.setText("Erreur : " + e.getMessage() + "\n");
            }
        });

        VBox layout = new VBox(10, fileLabel, selectFileButton, keywordField, searchButton, resultsArea);
        Scene scene = new Scene(layout, 600, 400);

        primaryStage.setTitle("Comparaison Performances Multi-Niveaux");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
