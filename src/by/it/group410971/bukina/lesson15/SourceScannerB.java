package by.it.group410971.bukina.lesson15;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnmappableCharacterException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SourceScannerB {

    static class FileData implements Comparable<FileData> {
        String path;
        long size;

        FileData(String path, long size) {
            this.path = path;
            this.size = size;
        }

        @Override
        public int compareTo(FileData other) {
            if (this.size != other.size) {
                return Long.compare(this.size, other.size);
            }
            return this.path.compareTo(other.path);
        }
    }

    public static void main(String[] args) {
        String src = System.getProperty("user.dir") + File.separator + "src" + File.separator;
        List<FileData> result = new ArrayList<>();

        try {
            Files.walkFileTree(Paths.get(src), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().endsWith(".java")) {
                        processFile(file, result, src);
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

        // Сортируем результаты
        Collections.sort(result);

        // Выводим результаты
        for (FileData data : result) {
            System.out.println(data.size + " " + data.path);
        }
    }

    private static void processFile(Path file, List<FileData> result, String src) {
        try {
            // Читаем файл с обработкой ошибок кодировки
            String content = readFileWithFallback(file);

            // Пропускаем тестовые файлы
            if (content.contains("@Test") || content.contains("org.junit.Test")) {
                return;
            }

            // Обрабатываем содержимое
            String processedContent = processContent(content);

            // Получаем относительный путь с правильным разделителем
            String relativePath = getRelativePath(file.toString(), src);

            // Вычисляем размер в байтах (в кодировке UTF-8)
            long size = processedContent.getBytes(StandardCharsets.UTF_8).length;

            result.add(new FileData(relativePath, size));

        } catch (IOException e) {
            // Игнорируем файлы с ошибками чтения
        }
    }

    private static String readFileWithFallback(Path file) throws IOException {
        // Пробуем разные кодировки для чтения файла
        Charset[] charsets = {StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1,
                Charset.forName("windows-1251"), StandardCharsets.US_ASCII};

        for (Charset charset : charsets) {
            try {
                return Files.readString(file, charset);
            } catch (MalformedInputException | UnmappableCharacterException e) {
                // Пробуем следующую кодировку
            }
        }

        // Если все кодировки не подошли, читаем как бинарный файл и конвертируем с заменой невалидных символов
        byte[] bytes = Files.readAllBytes(file);
        return new String(bytes, StandardCharsets.UTF_8).replaceAll("[^\\x00-\\x7F]", "?");
    }

    private static String processContent(String content) {
        StringBuilder result = new StringBuilder();
        boolean inBlockComment = false;
        boolean inString = false;
        boolean inChar = false;
        char prevChar = 0;

        // Обработка за O(n) - один проход по тексту
        for (int i = 0; i < content.length(); i++) {
            char currentChar = content.charAt(i);

            if (inBlockComment) {
                // Внутри блочного комментария - ищем конец
                if (prevChar == '*' && currentChar == '/') {
                    inBlockComment = false;
                    prevChar = 0;
                } else {
                    prevChar = currentChar;
                }
                continue;
            }

            if (inString) {
                // Внутри строкового литерала
                result.append(currentChar);
                if (currentChar == '"' && prevChar != '\\') {
                    inString = false;
                }
                prevChar = currentChar;
                continue;
            }

            if (inChar) {
                // Внутри символьного литерала
                result.append(currentChar);
                if (currentChar == '\'' && prevChar != '\\') {
                    inChar = false;
                }
                prevChar = currentChar;
                continue;
            }

            // Проверка на начало комментариев и литералов
            if (prevChar == '/' && currentChar == '*') {
                // Начало блочного комментария
                inBlockComment = true;
                result.deleteCharAt(result.length() - 1); // Удаляем предыдущий '/'
                prevChar = 0;
                continue;
            }

            if (prevChar == '/' && currentChar == '/') {
                // Начало строчного комментария - пропускаем до конца строки
                result.deleteCharAt(result.length() - 1); // Удаляем предыдущий '/'
                while (i < content.length() && content.charAt(i) != '\n') {
                    i++;
                }
                if (i < content.length()) {
                    result.append('\n');
                }
                prevChar = 0;
                continue;
            }

            if (currentChar == '"') {
                inString = true;
            } else if (currentChar == '\'') {
                inChar = true;
            }

            result.append(currentChar);
            prevChar = currentChar;
        }

        // Удаляем package, imports и пустые строки
        String processed = removePackageAndImports(result.toString());
        processed = removeEmptyLines(processed);

        // Удаляем символы с кодом <33 в начале и конце
        return trimLowChars(processed);
    }

    private static String removePackageAndImports(String content) {
        StringBuilder result = new StringBuilder();
        String[] lines = content.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();
            // Пропускаем package и import строки
            if (!trimmed.startsWith("package ") && !trimmed.startsWith("import ")) {
                result.append(line).append("\n");
            }
        }

        return result.toString();
    }

    private static String removeEmptyLines(String content) {
        StringBuilder result = new StringBuilder();
        String[] lines = content.split("\n");

        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                result.append(line).append("\n");
            }
        }

        return result.toString();
    }

    private static String trimLowChars(String text) {
        int start = 0;
        int end = text.length();

        while (start < end && text.charAt(start) < 33) {
            start++;
        }
        while (end > start && text.charAt(end - 1) < 33) {
            end--;
        }

        return text.substring(start, end);
    }

    private static String getRelativePath(String fullPath, String src) {
        // Используем Path API для получения правильного относительного пути
        Path full = Paths.get(fullPath);
        Path base = Paths.get(src);

        try {
            Path relative = base.relativize(full);
            // Приводим к строке с системным разделителем
            return relative.toString().replace("/", File.separator).replace("\\", File.separator);
        } catch (IllegalArgumentException e) {
            // Fallback: просто обрезаем начало пути
            String relative = fullPath.substring(src.length());
            return relative.replace("/", File.separator).replace("\\", File.separator);
        }
    }
}