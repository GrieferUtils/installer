package de.byteandbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.byteandbit.api.TranslationApi;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static de.byteandbit.Constants.UI_ARTIFICIAL_DELAY_MS;

/**
 * Utility class for file downloads and OS detection.
 */
public class Util {

    /**
     * Downloads a zip file from the given URL and extracts the first file in it to a temporary file.
     */
    public static File downloadUnzippedTempFile(String url) throws IOException {
        File zipFile = downloadTempFile(url);

        File tempDir = Files.createTempDirectory("unzipped_").toFile();
        tempDir.deleteOnExit();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry = zis.getNextEntry();
            if (entry == null) {
                throw new IOException("Zip file is empty");
            }

            File extractedFile = new File(tempDir, entry.getName());
            extractedFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(extractedFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = zis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            zis.closeEntry();
            return extractedFile;
        }
    }

    /**
     * Extracts a resource file from the JAR to a temporary file.
     *
     * @param resourcePath URL to the resource (e.g., from getClassLoader().getResource())
     * @return Temporary file containing the extracted resource
     * @throws IOException if reading or writing fails
     */
    public static File extractResourceToTempFile(URL resourcePath) throws IOException {
        if (resourcePath == null) {
            throw new IllegalArgumentException("Resource path cannot be null");
        }

        // Extract file extension from the resource path
        String extension = "";
        String path = resourcePath.getPath();
        int lastDot = path.lastIndexOf('.');
        int lastSlash = path.lastIndexOf('/');
        if (lastDot > lastSlash && lastDot != -1) {
            extension = path.substring(lastDot);
        }

        File tempFile = Files.createTempFile("extracted_", extension).toFile();

        try (InputStream in = resourcePath.openStream();
             FileOutputStream out = new FileOutputStream(tempFile)) {
            tempFile.deleteOnExit();

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            return tempFile;
        } catch (IOException e) {
            tempFile.delete(); // Clean up on failure
            throw e;
        }
    }

    /**
     * Downloads a file from the given URL to a temporary file.
     */
    public static File downloadTempFile(String url) throws IOException {
        String extension = "";
        int lastDot = url.lastIndexOf('.');
        int lastSlash = url.lastIndexOf('/');
        if (lastDot > lastSlash && lastDot != -1) {
            extension = url.substring(lastDot);
        }

        File tempFile = Files.createTempFile("download_", extension).toFile();
        tempFile.deleteOnExit();
        downloadFile(url, tempFile);
        return tempFile;
    }

    /**
     * Downloads a file from the given URL to the given file.
     */
    public static void downloadFile(String url, File file) throws IOException {
        URL downloadUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        try (InputStream in = connection.getInputStream(); FileOutputStream out = new FileOutputStream(file)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        connection.disconnect();
    }

    /**
     * Downloads a file from the given URL to the specified folder.
     * The filename is derived from the URL.
     */
    public static File downloadFileToFolder(String url, File folder) throws IOException {
        if (!folder.exists() || !folder.isDirectory()) {
            throw new IOException("Invalid folder: " + folder.getAbsolutePath());
        }

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty("User-Agent", "BAB-Installer");

        int status = conn.getResponseCode();
        if (status >= 400) {
            throw new IOException("HTTP error " + status);
        }

        // --- Determine filename ---
        String filename = null;
        String disposition = conn.getHeaderField("Content-Disposition");

        if (disposition != null) {
            Matcher m = Pattern.compile(
                    "filename\\*=UTF-8''([^;]+)|filename=\"?([^\";]+)\"?",
                    Pattern.CASE_INSENSITIVE
            ).matcher(disposition);

            if (m.find()) {
                filename = m.group(1) != null
                        ? URLDecoder.decode(m.group(1), String.valueOf(StandardCharsets.UTF_8))
                        : m.group(2);
            }
        }

        // Fallback: URL path
        if (filename == null || filename.isEmpty()) {
            String path = conn.getURL().getPath();
            filename = Paths.get(path).getFileName().toString();
        }

        if (filename == null || filename.isEmpty()) {
            throw new IOException("Could not determine filename");
        }

        File destination = new File(folder, filename);

        // --- Download ---
        try (InputStream in = conn.getInputStream();
             OutputStream out = new BufferedOutputStream(Files.newOutputStream(destination.toPath()))) {

            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }

        return destination;
    }

    public static OS getOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return OS.WINDOWS;
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            return OS.MACOS;
        } else if (osName.contains("nux") || osName.contains("nix") || osName.contains("aix")) {
            return OS.LINUX;
        } else {
            return OS.OTHER;
        }
    }

    public static void ui_wait() {
        try {
            Thread.sleep(UI_ARTIFICIAL_DELAY_MS);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Fetches JSON from a URL and deserializes it into the specified class.
     */
    public static <T> T getJsonResponse(String url, Class<T> clazz, ObjectMapper mapper) throws IOException {
        URL downloadUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        try (InputStream in = connection.getInputStream()) {
            return mapper.readValue(in, clazz);
        } finally {
            connection.disconnect();
        }
    }

    public static String uiText(String key) {
        return TranslationApi.getInstance().get(key);
    }

    public enum OS {
        WINDOWS, LINUX, MACOS, OTHER
    }

    public static byte[] readAllBytes(InputStream is) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
}
