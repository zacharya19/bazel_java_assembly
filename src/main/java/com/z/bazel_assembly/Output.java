package com.z.bazel_assembly;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class Output {
    final String LIB_PATH = "";
    final Action LIB_ACTION = Action.MERGE;
    final String CLASSES_PATH = "";

    public void build(String inputFile, String outPath, String mainClass, List<String> dependencies)
            throws IOException, FileNotFoundException {
        try (JarCreator creator = new JarCreator(outPath)) {
            creator.addManifest(mainClass);

            for (String dep : dependencies) {
                creator.addFile(LIB_PATH, dep, LIB_ACTION);
            }

            creator.addFile(CLASSES_PATH, inputFile, Action.MERGE);
        }
    }
}
