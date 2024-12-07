package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SequentialSearch {
    public static List<String> searchInFile(String content, String pattern, int startingLine) {
        List<String> results = new ArrayList<>();
        String[] lines = content.split("\n");
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
