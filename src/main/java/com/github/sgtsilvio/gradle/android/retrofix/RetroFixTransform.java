package com.github.sgtsilvio.gradle.android.retrofix;

import com.android.build.api.transform.*;
import com.android.build.gradle.BaseExtension;
import com.android.utils.FileUtils;
import com.google.common.collect.ImmutableSet;
import javassist.ClassMap;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Silvio Giebl
 */
class RetroFixTransform extends Transform {

    private final BaseExtension android;

    RetroFixTransform(final BaseExtension android) {
        this.android = android;
    }

    @Override
    public String getName() {
        return "android-retrofix";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return ImmutableSet.of(QualifiedContent.DefaultContentType.CLASSES);
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return ImmutableSet.of(
                QualifiedContent.Scope.PROJECT,
                QualifiedContent.Scope.SUB_PROJECTS,
                QualifiedContent.Scope.EXTERNAL_LIBRARIES);
    }

    @Override
    public Set<? super QualifiedContent.Scope> getReferencedScopes() {
        return ImmutableSet.of(
                QualifiedContent.Scope.PROJECT,
                QualifiedContent.Scope.SUB_PROJECTS,
                QualifiedContent.Scope.EXTERNAL_LIBRARIES);
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    @Override
    public void transform(final TransformInvocation transformInvocation) {
        final Stream<File> depsStream = Stream.concat(
                transformInvocation.getReferencedInputs().stream().map(TransformInput::getJarInputs),
                transformInvocation.getReferencedInputs().stream().map(TransformInput::getDirectoryInputs))
                .flatMap(Collection::stream)
                .map(QualifiedContent::getFile);

        final List<String> deps = Stream.concat(android.getBootClasspath().stream(), depsStream)
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());

        final ClassMap classMap = new ClassMap() {
            @Override
            public String get(final Object jvmClassName) {
                final String s = (String) jvmClassName;
                if (s.startsWith("java/util/Optional")) {
                    return "java9/util/Optional" + s.substring("java/util/Optional".length());
                }
                if (s.startsWith("java/util/PrimitiveIterator")) {
                    return "java9/util/PrimitiveIterator" + s.substring("java/util/PrimitiveIterator".length());
                }
                if (s.startsWith("java/util/Spliterator")) {
                    return "java9/util/Spliterator" + s.substring("java/util/Spliterator".length());
                }
                if (s.startsWith("java/util/function")) {
                    return "java9/util/function" + s.substring("java/util/function".length());
                }
                if (s.startsWith("java/util/stream")) {
                    return "java9/util/stream" + s.substring("java/util/stream".length());
                }
                return super.get(jvmClassName);
            }
        };
        classMap.put("java/lang/FunctionalInterface", "java9/lang/FunctionalInterface");

        classMap.put("java/util/DoubleSummaryStatistics", "java9/util/DoubleSummaryStatistics");
        classMap.put("java/util/IntSummaryStatistics", "java9/util/IntSummaryStatistics");
        classMap.put("java/util/LongSummaryStatistics", "java9/util/LongSummaryStatistics");
        classMap.put("java/util/SplittableRandom", "java9/util/SplittableRandom");
        classMap.put("java/util/StringJoiner", "java9/util/StringJoiner");

        classMap.put("java/util/concurrent/CompletableFuture", "java9/util/concurrent/CompletableFuture");
        classMap.put("java/util/concurrent/CompletionException", "java9/util/concurrent/CompletionException");
        classMap.put("java/util/concurrent/CompletionStage", "java9/util/concurrent/CompletionStage");
        classMap.put("java/util/concurrent/CountedCompleter", "java9/util/concurrent/CountedCompleter");
        classMap.put("java/util/concurrent/ForkJoinPool", "java9/util/concurrent/ForkJoinPool");
        classMap.put("java/util/concurrent/ForkJoinTask", "java9/util/concurrent/ForkJoinTask");
        classMap.put("java/util/concurrent/ForkJoinWorkerTask", "java9/util/concurrent/ForkJoinWorkerTask");
        classMap.put("java/util/concurrent/RecursiveAction", "java9/util/concurrent/RecursiveAction");
        classMap.put("java/util/concurrent/RecursiveTask", "java9/util/concurrent/RecursiveTask");
        classMap.put("java/util/concurrent/ThreadLocalRandom", "java9/util/concurrent/ThreadLocalRandom");

        transformInvocation.getInputs().forEach(transformInput -> {
            transformInput.getDirectoryInputs().forEach(directoryInput -> {
                try {
                    final File outputDir = transformInvocation.getOutputProvider().getContentLocation(
                            directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);

                    FileUtils.deleteRecursivelyIfExists(outputDir);

                    final ClassPool classPool = new ClassPool();
                    classPool.appendSystemPath();
                    classPool.insertClassPath(directoryInput.getFile().getAbsolutePath());
                    for (final String dep : deps) {
                        classPool.insertClassPath(dep);
                    }

                    final Path inputPath = Paths.get(directoryInput.getFile().getAbsolutePath());
                    Files.walk(inputPath)
                            .filter(Files::isRegularFile)
                            .map(inputPath::relativize)
                            .filter(path -> {
                                if (path.getFileName().toFile().getName().endsWith(".class")) {
                                    return true;
                                }
                                try {
                                    final File file = new File(outputDir, path.toString());
                                    //noinspection ResultOfMethodCallIgnored
                                    file.getParentFile().mkdirs();
                                    Files.copy(path, file.toPath());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return false;
                            })
                            .map(Path::toString)
                            .map(s -> s.replaceAll("/", ".").replaceAll("\\\\", "."))
                            .map(s -> s.substring(0, s.length() - ".class".length()))
                            .forEach(s -> transformClass(classPool, classMap, s, outputDir));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            transformInput.getJarInputs().forEach(jarInput -> {
                try {
                    final File outputDir = transformInvocation.getOutputProvider().getContentLocation(
                            jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(), Format.DIRECTORY);
                    final File outputJar = transformInvocation.getOutputProvider().getContentLocation(
                            jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);

                    if ((jarInput.getStatus() == Status.NOTCHANGED) && outputJar.exists()) {
                        return;
                    }
                    FileUtils.deleteRecursivelyIfExists(outputDir);
                    FileUtils.deleteIfExists(outputJar);

                    final ClassPool classPool = new ClassPool();
                    classPool.appendSystemPath();
                    classPool.insertClassPath(jarInput.getFile().getAbsolutePath());
                    for (final String dep : deps) {
                        classPool.insertClassPath(dep);
                    }

                    final ZipFile zipFile = new ZipFile(jarInput.getFile());
                    zipFile.stream()
                            .filter(entry -> !entry.isDirectory())
                            .filter(entry -> {
                                if (entry.getName().endsWith(".class")) {
                                    return true;
                                }
                                try {
                                    final File file = new File(outputDir, entry.getName());
                                    //noinspection ResultOfMethodCallIgnored
                                    file.getParentFile().mkdirs();
                                    Files.copy(zipFile.getInputStream(entry), file.toPath());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return false;
                            })
                            .map(ZipEntry::getName)
                            .map(s -> s.replaceAll("/", ".").replaceAll("\\\\", "."))
                            .map(s -> s.substring(0, s.length() - ".class".length()))
                            .forEach(s -> transformClass(classPool, classMap, s, outputDir));

                    zip(outputDir, outputJar);
                    FileUtils.deleteRecursivelyIfExists(outputDir);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private static void transformClass(
            final ClassPool classPool, final ClassMap classMap, final String className, final File outputDir) {

        try {
            final CtClass ctClass = classPool.get(className);

            ctClass.getClassFile().renameClass(classMap);
            ctClass.rebuildClassFile();

            ctClass.writeFile(outputDir.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void zip(final File inputDir, final File outputFile) {
        final Project p = new Project();
        p.init();
        final Zip zip = new Zip();
        zip.setProject(p);
        zip.setDestFile(outputFile);
        zip.setBasedir(inputDir);
        zip.perform();
    }
}

