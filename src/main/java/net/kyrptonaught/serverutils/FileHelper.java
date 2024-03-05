package net.kyrptonaught.serverutils;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileHelper {

    public static boolean deleteDir(Path directory) {
        try {
            FileUtils.deleteDirectory(directory.toFile());
            return true;
        } catch (IOException exception) {
            System.out.println("Failed to delete directory: " + directory);
            exception.printStackTrace();
        }
        return false;
    }

    public static boolean createDir(Path directory) {
        try {
            Files.createDirectories(directory);
            return true;
        } catch (IOException exception) {
            System.out.println("Failed to create directory: " + directory);
            exception.printStackTrace();
        }
        return false;
    }

    public static boolean copyDirectory(Path source, Path target) {
        try {
            Files.walkFileTree(source, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                    Files.createDirectories(target.resolve(source.relativize(dir)));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        } catch (IOException exception) {
            System.out.println("Failed to copy directory: " + source + " to: " + target);
            exception.printStackTrace();
        }
        return false;
    }

    public static String readFileFromZip(Path zipFile, String fileName) {
        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            ZipEntry entry = zip.getEntry(fileName);

            return new String(zip.getInputStream(entry).readAllBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String cleanFileName(String badFileName) {
        StringBuilder cleanName = new StringBuilder();
        int len = badFileName.codePointCount(0, badFileName.length());
        for (int i = 0; i < len; i++) {
            if (isCharValid(badFileName.charAt(i))) {
                cleanName.append(badFileName.charAt(i));
            }
        }
        return cleanName.toString();
    }

    public static boolean isCharValid(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' || c == ':' || c == '/' || c == '.' || c == '-';
    }
}
