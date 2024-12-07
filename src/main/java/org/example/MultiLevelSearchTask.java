package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MultiLevelSearchTask extends RecursiveTask<List<String>> {
    private final List<Block> blocks;
    private final String pattern;

    public MultiLevelSearchTask(List<Block> blocks, String pattern) {
        this.blocks = blocks;
        this.pattern = pattern;
    }

    @Override
    protected List<String> compute() {
        if (blocks.size() <= 2) {
            // Si le nombre de blocs est très faible, traiter sans diviser
            List<String> results = new ArrayList<>();
            for (Block block : blocks) {
                BlockSearchTask blockTask = new BlockSearchTask(block.content, pattern, 0, block.content.length(), block.startingLine);
                results.addAll(blockTask.compute());
            }
            return results;
        } else {
            // Diviser les blocs pour un traitement parallèle plus efficace
            int mid = blocks.size() / 2;
            MultiLevelSearchTask leftTask = new MultiLevelSearchTask(blocks.subList(0, mid), pattern);
            MultiLevelSearchTask rightTask = new MultiLevelSearchTask(blocks.subList(mid, blocks.size()), pattern);

            invokeAll(leftTask, rightTask);

            List<String> results = new ArrayList<>(leftTask.join());
            results.addAll(rightTask.join());
            return results;
        }
    }
}

class BlockSearchTask extends RecursiveTask<List<String>> {
    private final String content;
    private final String pattern;
    private final int start, end, startingLine;
    private static final int LINE_THRESHOLD = 200; // Optimisation du seuil

    public BlockSearchTask(String content, String pattern, int start, int end, int startingLine) {
        this.content = content;
        this.pattern = pattern;
        this.start = start;
        this.end = end;
        this.startingLine = startingLine;
    }

    @Override
    protected List<String> compute() {
        String[] lines = content.split("\n");

        if (lines.length <= LINE_THRESHOLD) {
            // Si le nombre de lignes dans le bloc est inférieur au seuil, effectuer une recherche séquentielle
            return searchSequentially(lines, pattern, startingLine);
        } else {
            // Diviser les lignes du bloc en sous-blocs pour une recherche parallèle
            int mid = lines.length / 2;
            int midLineNumber = startingLine + mid;

            BlockSearchTask leftTask = new BlockSearchTask(String.join("\n", Arrays.copyOfRange(lines, 0, mid)), pattern, start, mid, startingLine);
            BlockSearchTask rightTask = new BlockSearchTask(String.join("\n", Arrays.copyOfRange(lines, mid, lines.length)), pattern, mid, end, midLineNumber);

            invokeAll(leftTask, rightTask);

            List<String> results = new ArrayList<>(leftTask.join());
            results.addAll(rightTask.join());
            return results;
        }
    }

    private List<String> searchSequentially(String[] lines, String pattern, int startingLine) {
        List<String> results = new ArrayList<>();
        Pattern regex = Pattern.compile(pattern);

        for (int i = 0; i < lines.length; i++) {
            Matcher matcher = regex.matcher(lines[i]);
            if (matcher.find()) {
                results.add("Ligne " + (startingLine + i) + ": " + lines[i]);
            }
        }

        return results;
    }
}
