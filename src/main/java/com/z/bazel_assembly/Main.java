package com.z.bazel_assembly;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.devtools.build.lib.view.proto.Deps.Dependencies;
import com.google.devtools.build.lib.view.proto.Deps.Dependency;

import org.apache.commons.cli.*;

/**
 * A java util for creating a singlejar in bazel.
 * The tools will include deps only if it's a non transitive or it's used by the input jar.
 *
 * @author zacharya19@gmail.com
 */
public class Main {
    private static final String JDEPS_SUFFIX = ".jdeps";
    private static final String JAR_SUFFIX = ".jar";
    private static final String[] REMOVE_FROM_NAME = { "-ijar", "-hjar" };

    private static String normalizeName(String name) {
        for (String remove : REMOVE_FROM_NAME) {
            name = name.replace(remove, "");
        }
        return name;
    }

    private static List<String> extractDeps(String jdepsFile, String[] includeList, Map<String, String> depJars) {
        List<String> dependencies = new ArrayList<>();
        try (FileInputStream ins = new FileInputStream(jdepsFile)) {
            Dependencies jdeps = Dependencies.parseFrom(ins);
            for (Dependency dep : jdeps.getDependencyList()) {
                File f = new File(dep.getPath());
                String pathToJar = depJars.get(normalizeName(f.getName()));
                if (pathToJar == null) {
                    continue; // Will happen in case of neverlink deps
                }

                dependencies.add(pathToJar);
            }

            for (String dep : includeList) {
                dependencies.add(dep);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Jdeps file " + jdepsFile + " Doesn't exists!");
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse jdeps proto");
        }

        return dependencies;
    }

    private static CommandLine cmdParse(String[] args) {
        Options options = new Options();

        Option input = new Option("j", "jar", true, "jar target path");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output path");
        output.setRequired(true);
        options.addOption(output);

        Option deps = new Option("d", "deps", true, "deps jar path");
        deps.setRequired(true);
        options.addOption(deps);

        Option include = new Option("i", "include", true, "must include jars");
        options.addOption(include);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Bazel Assembly", options);

            System.exit(1);
        }

        return null;
    }

    public static void main(String args[]) throws FileNotFoundException, IOException {
        CommandLine cmd = cmdParse(args);

        String inputFilePath = cmd.getOptionValue("jar");
        String jdepsFile = inputFilePath.replace(JAR_SUFFIX, JDEPS_SUFFIX);
        String outputFilePath = cmd.getOptionValue("output");
        String includeList[] = cmd.getOptionValues("include");

        String depList[] = cmd.getOptionValues("deps");
        Map<String, String> depJars = new HashMap<>();
        for (String dep : depList) {
            File f = new File(dep);
            depJars.put(f.getName(), dep);
        }

        List<String> dependencies = extractDeps(jdepsFile, includeList, depJars);
        dependencies.add(inputFilePath);

        try (JarCreator creator = new JarCreator(outputFilePath)) {
            for (String dep : dependencies) {
                creator.mergeJarFile(dep);
            }
        }
    }
}
