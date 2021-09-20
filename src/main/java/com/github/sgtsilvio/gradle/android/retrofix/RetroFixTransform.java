package com.github.sgtsilvio.gradle.android.retrofix;

import com.android.build.api.transform.*;
import com.android.build.gradle.BaseExtension;
import com.github.sgtsilvio.gradle.android.retrofix.backport.Backport;
import com.github.sgtsilvio.gradle.android.retrofix.backport.FutureBackport;
import com.github.sgtsilvio.gradle.android.retrofix.backport.StreamsBackport;
import com.github.sgtsilvio.gradle.android.retrofix.backport.TimeBackport;
import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap;
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap;
import com.github.sgtsilvio.gradle.android.retrofix.util.Lambdas;
import com.google.common.collect.ImmutableSet;
import javassist.*;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Silvio Giebl
 */
class RetroFixTransform extends Transform {

    private static final @NotNull Logger logger = LoggerFactory.getLogger(RetroFixTransform.class);

    private final @NotNull BaseExtension android;

    RetroFixTransform(final @NotNull BaseExtension android) {
        this.android = android;
    }

    @Override
    public @NotNull String getName() {
        return "androidRetroFix";
    }

    @Override
    public @NotNull Set<QualifiedContent.ContentType> getInputTypes() {
        return ImmutableSet.of(QualifiedContent.DefaultContentType.CLASSES);
    }

    @Override
    public @NotNull Set<? super QualifiedContent.Scope> getScopes() {
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
    public void transform(final @NotNull TransformInvocation transformInvocation) throws IOException {
        final List<Backport> backports = new LinkedList<>();
        backports.add(new StreamsBackport());
        backports.add(new FutureBackport());
        backports.add(new TimeBackport());
        final List<Backport> presentBackports = new LinkedList<>();
        final List<JarInput> backportJars = new LinkedList<>();

        final ClassPool classPool = new ClassPool();
        classPool.appendSystemPath();
        try {
            for (final File file : android.getBootClasspath()) {
                classPool.insertClassPath(file.getAbsolutePath());
            }
            for (final TransformInput input : transformInvocation.getInputs()) {
                for (final JarInput jarInput : input.getJarInputs()) {
                    classPool.insertClassPath(jarInput.getFile().getAbsolutePath());
                    backports.removeIf(backport -> {
                        if (backport.isPresent(classPool)) {
                            presentBackports.add(backport);
                            backportJars.add(jarInput);
                            return true;
                        }
                        return false;
                    });
                }
                for (final DirectoryInput directoryInput : input.getDirectoryInputs()) {
                    classPool.insertClassPath(directoryInput.getFile().getAbsolutePath());
                }
            }
        } catch (final NotFoundException e) {
            throw new RuntimeException(e);
        }

        final TypeMap typeMap = new TypeMap();
        final MethodMap methodMap = new MethodMap();
        presentBackports.forEach(backport -> backport.apply(typeMap, methodMap));

        if (!transformInvocation.isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll();
        }

        transformInvocation.getInputs().forEach(transformInput -> {
            transformInput.getDirectoryInputs().forEach(Lambdas.consumer(directoryInput -> {
                final File outputDir = transformInvocation.getOutputProvider().getContentLocation(
                        directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);

                if (outputDir.exists()) {
                    FileUtils.deleteDirectory(outputDir);
                }
                if (!outputDir.mkdirs()) {
                    throw new RuntimeException("Could not create output directory");
                }
                logger.info("Transforming directory {}", directoryInput.getName());

                final Path inputPath = Paths.get(directoryInput.getFile().getAbsolutePath());
                Files.walk(inputPath)
                        .filter(Files::isRegularFile)
                        .filter(Lambdas.predicate(path -> {
                            if (path.getFileName().toFile().getName().endsWith(".class")) {
                                return true;
                            }
                            final String filePath = inputPath.relativize(path).toString();
                            logger.info("Copying file {}", filePath);
                            final File file = new File(outputDir, filePath);
                            //noinspection ResultOfMethodCallIgnored
                            file.getParentFile().mkdirs();
                            Files.copy(path, file.toPath());
                            return false;
                        }))
                        .map(inputPath::relativize)
                        .map(Path::toString)
                        .map(s -> s.replaceAll("/", ".").replaceAll("\\\\", "."))
                        .map(s -> s.substring(0, s.length() - ".class".length()))
                        .forEach(Lambdas.consumer(s -> transformClass(classPool, s, typeMap, methodMap, outputDir)));
            }));
            transformInput.getJarInputs().forEach(Lambdas.consumer(jarInput -> {
                final File outputDir = transformInvocation.getOutputProvider().getContentLocation(
                        jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(), Format.DIRECTORY);
                final File outputJar = transformInvocation.getOutputProvider().getContentLocation(
                        jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);

                if ((jarInput.getStatus() == Status.NOTCHANGED) && outputJar.exists()) {
                    return;
                }
                if (backportJars.contains(jarInput)) {
                    FileUtils.copyFile(jarInput.getFile(), outputJar);
                    return;
                }
                if (outputDir.exists()) {
                    FileUtils.deleteDirectory(outputDir);
                }
                if (outputJar.exists()) {
                    FileUtils.delete(outputJar);
                }
                if (jarInput.getStatus() == Status.REMOVED) {
                    return;
                }
                if (!outputDir.mkdirs()) {
                    throw new RuntimeException("Could not create output directory");
                }
                logger.info("Transforming jar {}", jarInput.getName());

                final ZipFile zipFile = new ZipFile(jarInput.getFile());
                zipFile.stream()
                        .filter(entry -> !entry.isDirectory())
                        .filter(Lambdas.predicate(entry -> {
                            if (entry.getName().endsWith(".class") && !entry.getName().startsWith("META-INF/")
                                    && !entry.getName().startsWith("META-INF\\")) {
                                return true;
                            }
                            logger.info("Copying file {}", entry.getName());
                            final File file = new File(outputDir, entry.getName());
                            //noinspection ResultOfMethodCallIgnored
                            file.getParentFile().mkdirs();
                            Files.copy(zipFile.getInputStream(entry), file.toPath());
                            return false;
                        }))
                        .map(ZipEntry::getName)
                        .map(s -> s.replaceAll("/", ".").replaceAll("\\\\", "."))
                        .map(s -> s.substring(0, s.length() - ".class".length()))
                        .forEach(Lambdas.consumer(s -> transformClass(classPool, s, typeMap, methodMap, outputDir)));

                zip(outputDir, outputJar);
                FileUtils.deleteDirectory(outputDir);
            }));
        });
    }

    private static void transformClass(
            final @NotNull ClassPool classPool, final @NotNull String className, final @NotNull TypeMap typeMap,
            final @NotNull MethodMap methodMap, final @NotNull File outputDir) throws Exception {

        logger.info("Transforming class {}", className);
        final CtClass ctClass = classPool.get(className);

        final HashMap<Integer, String> replaceMap = new HashMap<>();

        ctClass.instrument(Lambdas.methodEditor((m, c) -> {
            final String key = m.getMethodName() + " " + m.getSignature();
            MethodMap.Entry methodMapEntry = methodMap.get(key);
            while (methodMapEntry != null) {
                final CtMethod method = m.getMethod();
                final CtClass declaringClass = method.getDeclaringClass();
                final boolean matches;
                if (Modifier.isStatic(method.getModifiers())) {
                    matches = declaringClass.getName().equals(methodMapEntry.type);
                } else {
                    matches = declaringClass.subtypeOf(declaringClass.getClassPool().get(methodMapEntry.type));
                }
                if (matches) {
                    replaceMap.put(c, methodMapEntry.replacement);
                    break;
                }
                methodMapEntry = methodMapEntry.next;
            }
        }));

        ctClass.getClassFile().renameClass(typeMap);

        ctClass.instrument(Lambdas.methodEditor((m, c) -> {
            final String replacement = replaceMap.get(c);
            if (replacement != null) {
                m.replace(replacement);
            }
        }));

        ctClass.getClassFile().compact();

        ctClass.writeFile(outputDir.getAbsolutePath());
    }

    private static void zip(final @NotNull File inputDir, final @NotNull File outputFile) {
        final Project p = new Project();
        p.init();
        final Zip zip = new Zip();
        zip.setProject(p);
        zip.setDestFile(outputFile);
        zip.setBasedir(inputDir);
        zip.perform();
    }
}
