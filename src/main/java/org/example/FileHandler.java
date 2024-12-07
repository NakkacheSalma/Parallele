package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

class FileHandler {
    private static final int BLOCK_SIZE = 512 * 1024; // Réduction à 512 Ko pour un meilleur parallélisme
// Taille de bloc : 1 Mo

    public static List<Block> readFileInBlocksWithLines(String filePath) throws IOException {
        List<Block> blocks = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder blockContent = new StringBuilder();
            int lineNumber = 0;
            int currentBlockLineStart = 1;
            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                blockContent.append(line).append("\n");

                if (blockContent.length() >= BLOCK_SIZE) {
                    blocks.add(new Block(blockContent.toString(), currentBlockLineStart));
                    blockContent.setLength(0);
                    currentBlockLineStart = lineNumber + 1;
                }
            }

            if (blockContent.length() > 0) {
                blocks.add(new Block(blockContent.toString(), currentBlockLineStart));
            }
        }

        return blocks;
    }
}

class Block {
    String content;
    int startingLine;

    public Block(String content, int startingLine) {
        this.content = content;
        this.startingLine = startingLine;
    }
}
