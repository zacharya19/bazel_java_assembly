package com.z.bazel_assembly;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipException;

enum Action {
    MERGE, COPY
}

class JarCreator implements AutoCloseable {
    private FileOutputStream outputFile;
    private JarOutputStream outputJar;
    private static final byte[] BUFFER = new byte[4096 * 1024];

    public JarCreator(String outputPath) throws FileNotFoundException, IOException {
        outputFile = new FileOutputStream(outputPath);
        outputJar = new JarOutputStream(outputFile);
    }

    public void mergeJarFile(String path) throws IOException {
        mergeJarFile("", path);
    }

    public void addFile(String basepath, String path, Action action) throws IOException {
        if (action == Action.MERGE)
            mergeJarFile(basepath, path);
        else {
            File f = new File(path);
            Path entryPath = Paths.get(basepath, f.getName());
            copyFile(entryPath.toString(), new FileInputStream(path));
        }
    }

    private void mergeJarFile(String basepath, String path) throws IOException {
        JarFile jar = new JarFile(new File(path));
        Enumeration<JarEntry> enumOfJar = jar.entries();

        while (enumOfJar.hasMoreElements()) {
            JarEntry entry = enumOfJar.nextElement();
            Path entryPath = Paths.get(basepath, entry.getName());
            JarEntry finalEntry = new JarEntry(entryPath.toString());

            try {
                outputJar.putNextEntry(finalEntry);
            } catch (ZipException e) {
                continue;
            }

            if (!entry.isDirectory()) {
                copy(jar.getInputStream(entry), outputJar);
            }
            outputJar.closeEntry();
        }
    }

    private void copyFile(String path, InputStream is) throws IOException {
        JarEntry entry = new JarEntry(path);

        try {
            outputJar.putNextEntry(entry);
        } catch (ZipException e) {
            return;
        }

        copy(is, outputJar);
    }

    public void addManifest(String mainClass) throws IOException {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();

        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(new Attributes.Name("Created-By"), "blaze_java_assembly");

        if (mainClass != null)
            attributes.put(Attributes.Name.MAIN_CLASS, mainClass);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            manifest.write(out);

            InputStream in = new ByteArrayInputStream(out.toByteArray());
            copyFile(JarFile.MANIFEST_NAME, in);
            in.close();
        }
    }

    public void close() throws IOException {
        outputJar.close();
        outputFile.close();
    }

    public static void copy(InputStream input, OutputStream output) throws IOException {
        int bytesRead;
        while ((bytesRead = input.read(BUFFER)) != -1) {
            output.write(BUFFER, 0, bytesRead);
        }
    }
}
