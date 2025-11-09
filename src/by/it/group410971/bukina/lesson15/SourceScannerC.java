package by.it.group410971.bukina.lesson15;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class SourceScannerC {

    static class FileData {
        String path;
        String content;

        FileData(String path, String content) {
            this.path = path;
            this.content = content;
        }
    }

    // Оптимизированное расстояние Левенштейна
    public static int levenshteinDistance(String s1, String s2, int threshold) {
        if (s1 == null || s2 == null) return Integer.MAX_VALUE;

        int n = s1.length();
        int m = s2.length();

        if (Math.abs(n - m) > threshold) return Integer.MAX_VALUE;

        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];

        for (int j = 0; j <= m; j++) {
            prev[j] = j;
        }

        for (int i = 1; i <= n; i++) {
            curr[0] = i;
            int minInRow = i;

            for (int j = 1; j <= m; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                curr[j] = Math.min(
                        Math.min(curr[j - 1] + 1, prev[j] + 1),
                        prev[j - 1] + cost
                );
                minInRow = Math.min(minInRow, curr[j]);
            }

            if (minInRow > threshold) {
                return Integer.MAX_VALUE;
            }

            int[] temp = prev;
            prev = curr;
            curr = temp;
        }

        return prev[m] <= threshold ? prev[m] : Integer.MAX_VALUE;
    }

    public static void main(String[] args) {
        String src = System.getProperty("user.dir") + File.separator + "src" + File.separator;
        List<FileData> files = new ArrayList<>();

        try {
            Files.walkFileTree(Paths.get(src), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().endsWith(".java")) {
                        processFile(file, files, src);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Находим копии
        Map<String, List<String>> copies = findCopies(files);

        // Сортируем и выводим результаты
        List<String> sortedPaths = new ArrayList<>(copies.keySet());
        Collections.sort(sortedPaths);

        for (String path : sortedPaths) {
            System.out.println(path);
            List<String> copyPaths = copies.get(path);
            Collections.sort(copyPaths);
            for (String copyPath : copyPaths) {
                System.out.println(copyPath);
            }
        }
    }

    private static Map<String, List<String>> findCopies(List<FileData> files) {
        Map<String, List<String>> copies = new HashMap<>();
        int threshold = 10;

        // Сначала находим точные копии по хешу
        Map<String, List<FileData>> exactCopies = new HashMap<>();
        for (FileData file : files) {
            exactCopies.computeIfAbsent(file.content, k -> new ArrayList<>()).add(file);
        }

        // Добавляем точные копии
        for (List<FileData> fileGroup : exactCopies.values()) {
            if (fileGroup.size() > 1) {
                Collections.sort(fileGroup, (a, b) -> a.path.compareTo(b.path));
                for (int i = 0; i < fileGroup.size(); i++) {
                    for (int j = i + 1; j < fileGroup.size(); j++) {
                        copies.computeIfAbsent(fileGroup.get(i).path, k -> new ArrayList<>())
                                .add(fileGroup.get(j).path);
                    }
                }
            }
        }

        // Затем ищем похожие файлы по Левенштейну
        for (int i = 0; i < files.size(); i++) {
            FileData file1 = files.get(i);

            for (int j = i + 1; j < files.size(); j++) {
                FileData file2 = files.get(j);

                // Пропускаем если уже нашли как точные копии
                if (copies.containsKey(file1.path) && copies.get(file1.path).contains(file2.path)) {
                    continue;
                }

                // Быстрая проверка по длине
                if (Math.abs(file1.content.length() - file2.content.length()) > threshold) {
                    continue;
                }

                // Проверка расстояния Левенштейна
                int distance = levenshteinDistance(file1.content, file2.content, threshold);
                if (distance <= threshold) {
                    copies.computeIfAbsent(file1.path, k -> new ArrayList<>()).add(file2.path);
                }
            }
        }

        return copies;
    }

    private static void processFile(Path file, List<FileData> files, String src) {
        try {
            String content = readFileWithFallback(file);

            // Пропускаем тестовые файлы
            if (content.contains("@Test") || content.contains("org.junit.Test")) {
                return;
            }

            // Обрабатываем содержимое
            String processedContent = processContent(content);

            // Получаем относительный путь с правильным разделителем
            String relativePath = getRelativePath(file.toString(), src);

            files.add(new FileData(relativePath, processedContent));

        } catch (IOException e) {
            // Игнорируем файлы с ошибками чтения
        }
    }

    private static String readFileWithFallback(Path file) throws IOException {
        Charset[] charsets = {StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1,
                Charset.forName("windows-1251"), StandardCharsets.US_ASCII};

        for (Charset charset : charsets) {
            try {
                return Files.readString(file, charset);
            } catch (MalformedInputException | UnmappableCharacterException e) {
                // Пробуем следующую кодировку
            }
        }

        byte[] bytes = Files.readAllBytes(file);
        return new String(bytes, StandardCharsets.UTF_8).replaceAll("[^\\x00-\\x7F]", "?");
    }

    private static String processContent(String content) {
        StringBuilder result = new StringBuilder();
        boolean inBlockComment = false;
        boolean inString = false;
        boolean inChar = false;
        char prevChar = 0;
        boolean lastWasSpace = false;

        for (int i = 0; i < content.length(); i++) {
            char currentChar = content.charAt(i);

            if (inBlockComment) {
                if (prevChar == '*' && currentChar == '/') {
                    inBlockComment = false;
                    prevChar = 0;
                } else {
                    prevChar = currentChar;
                }
                continue;
            }

            if (inString) {
                result.append(currentChar);
                if (currentChar == '"' && prevChar != '\\') {
                    inString = false;
                }
                prevChar = currentChar;
                continue;
            }

            if (inChar) {
                result.append(currentChar);
                if (currentChar == '\'' && prevChar != '\\') {
                    inChar = false;
                }
                prevChar = currentChar;
                continue;
            }

            if (prevChar == '/' && currentChar == '*') {
                inBlockComment = true;
                result.deleteCharAt(result.length() - 1);
                prevChar = 0;
                continue;
            }

            if (prevChar == '/' && currentChar == '/') {
                result.deleteCharAt(result.length() - 1);
                while (i < content.length() && content.charAt(i) != '\n') {
                    i++;
                }
                if (i < content.length()) {
                    if (!lastWasSpace) {
                        result.append(' ');
                        lastWasSpace = true;
                    }
                }
                prevChar = 0;
                continue;
            }

            if (currentChar == '"') {
                inString = true;
            } else if (currentChar == '\'') {
                inChar = true;
            }

            if (currentChar < 33) {
                if (!lastWasSpace) {
                    result.append(' ');
                    lastWasSpace = true;
                }
            } else {
                result.append(currentChar);
                lastWasSpace = false;
            }

            prevChar = currentChar;
        }

        String processed = removePackageAndImports(result.toString());
        return processed.trim();
    }

    private static String removePackageAndImports(String content) {
        StringBuilder result = new StringBuilder();
        String[] lines = content.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("package") && !trimmed.startsWith("import")) {
                result.append(line).append("\n");
            }
        }

        return result.toString();
    }

    private static String getRelativePath(String fullPath, String src) {
        // Используем Path API для правильного относительного пути
        Path full = Paths.get(fullPath);
        Path base = Paths.get(src);

        try {
            Path relative = base.relativize(full);
            return relative.toString().replace("/", File.separator).replace("\\", File.separator);
        } catch (IllegalArgumentException e) {
            String relative = fullPath.substring(src.length());
            return relative.replace("/", File.separator).replace("\\", File.separator);
        }
    }
}