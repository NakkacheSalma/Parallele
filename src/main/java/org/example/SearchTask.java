package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SearchTask extends RecursiveTask<List<String>> {
    private final String content;
    private final String pattern;
    private final int start, end, startingLine;
    private static final int THRESHOLD = 10_000; // Réglé pour un meilleur équilibre

    public SearchTask(String content, String pattern, int start, int end, int startingLine) {
        this.content = content;
        this.pattern = pattern;
        this.start = start;
        this.end = end;
        this.startingLine = startingLine;
    }

    @Override
    protected List<String> compute() {
        if (end - start <= THRESHOLD) {
            return search(content, start, end, pattern, startingLine);
        } else {
            int mid = (start + end) / 2;
            int midLine = startingLine + countLines(content.substring(0, mid));

            SearchTask leftTask = new SearchTask(content, pattern, start, mid, startingLine);
            SearchTask rightTask = new SearchTask(content, pattern, mid, end, midLine);

            invokeAll(leftTask, rightTask);

            List<String> results = new ArrayList<>(leftTask.join());
            results.addAll(rightTask.join());
            return results;
        }
    }

    private List<String> search(String content, int start, int end, String pattern, int startingLine) {
        List<String> results = new ArrayList<>();
        String[] lines = content.substring(start, end).split("\n");
        Pattern regex = Pattern.compile(pattern);

        for (int i = 0; i < lines.length; i++) {
            Matcher matcher = regex.matcher(lines[i]);
            if (matcher.find()) {
                results.add("Ligne " + (startingLine + i) + ": " + lines[i]);
            }
        }

        return results;
    }

    private int countLines(String text) {
        return (int) text.chars().filter(c -> c == '\n').count();
    }

    public static List<String> searchInBlocksWithLines(List<Block> blocks, String pattern) {
        ForkJoinPool pool = new ForkJoinPool();
        List<String> totalResults = new ArrayList<>();

        for (Block block : blocks) {
            SearchTask task = new SearchTask(block.content, pattern, 0, block.content.length(), block.startingLine);
            totalResults.addAll(pool.invoke(task));
        }

        return totalResults;
    }
}
