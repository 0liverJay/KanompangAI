package com.noey;

import org.junit.Test;
import java.nio.file.*;
import java.util.zip.*;
import static org.junit.Assert.*;

/** The independent oracle is the supplied archive, not a copy of app logic. */
public class ArchiveFidelityTest {
    @Test public void productionSourcesAndBuildFilesMatchArchive() throws Exception {
        Path root = Paths.get("..").toAbsolutePath().normalize();
        if (!Files.exists(root.resolve("test.zip"))) root = Paths.get("").toAbsolutePath();
        int count = 0;
        try (ZipFile zip = new ZipFile(root.resolve("test.zip").toFile())) {
            java.util.Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory() || !(name.startsWith("app/src/main/") ||
                        name.startsWith("gradle/") || name.equals("app/build.gradle.kts") ||
                        name.equals("build.gradle.kts") || name.equals("settings.gradle") ||
                        name.equals("gradle.properties") || name.equals("gradlew") ||
                        name.equals("gradlew.bat"))) continue;
                try (java.io.InputStream input = zip.getInputStream(entry)) {
                    java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
                    byte[] buffer = new byte[8192]; int n;
                    while ((n = input.read(buffer)) != -1) output.write(buffer, 0, n);
                    assertArrayEquals(name, output.toByteArray(), Files.readAllBytes(root.resolve(name)));
                }
                count++;
            }
        }
        assertTrue("Must verify real production files", count >= 35);
    }
}
