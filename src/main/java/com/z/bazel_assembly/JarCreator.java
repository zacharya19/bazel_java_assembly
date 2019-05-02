package com.z.bazel_assembly;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipException;

class JarCreator implements AutoCloseable {
    private FileOutputStream outputFile;
    private JarOutputStream outputJar;
    private static final byte[] BUFFER = new byte[4096 * 1024];

    public JarCreator(String outputPath) throws FileNotFoundException, IOException {
        outputFile = new FileOutputStream(outputPath);
        outputJar = new JarOutputStream(outputFile);
    }

    public void mergeJarFile(String path) throws IOException {
        JarFile jar = new JarFile(new File(path));
        Enumeration<JarEntry> enumOfJar = jar.entries();

        while (enumOfJar.hasMoreElements()) {
            JarEntry entry = enumOfJar.nextElement();
            try {
                outputJar.putNextEntry(entry);
            } catch (ZipException e) {
                continue;
            }
            
            if (!entry.isDirectory()) {
                copy(jar.getInputStream(entry), outputJar);
            }
            outputJar.closeEntry();
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
