package com.github.sgtsilvio.gradle.android.retrofix;

import com.android.build.api.transform.*;
import com.android.build.gradle.BaseExtension;
import com.android.utils.FileUtils;
import com.github.sgtsilvio.gradle.android.retrofix.backport.Backport;
import com.github.sgtsilvio.gradle.android.retrofix.backport.FutureBackport;
import com.github.sgtsilvio.gradle.android.retrofix.backport.StreamsBackport;
import com.github.sgtsilvio.gradle.android.retrofix.backport.TimeBackport;
import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap;
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap;
import com.github.sgtsilvio.gradle.android.retrofix.util.Lambdas;
import com.google.common.collect.ImmutableSet;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Silvio Giebl
 */
class RetroFixTransform extends Transform {

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
    public @NotNull Set<? super QualifiedContent.Scope> getReferencedScopes() {
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
        final Stream<File> depsStream = Stream.concat(
                transformInvocation.getReferencedInputs().stream().map(TransformInput::getJarInputs),
                transformInvocation.getReferencedInputs().stream().map(TransformInput::getDirectoryInputs))
                .flatMap(Collection::stream)
                .map(QualifiedContent::getFile);

        final List<String> deps = Stream.concat(android.getBootClasspath().stream(), depsStream)
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());

        final List<Backport> backports = new LinkedList<>();
        if (hasBackport("net.sourceforge.streamsupport:android-retrostreams", transformInvocation)) {
            backports.add(new StreamsBackport());
        }
        if (hasBackport("net.sourceforge.streamsupport:android-retrofuture", transformInvocation)) {
            backports.add(new FutureBackport());
        }
        if (hasBackport("org.threeten:threetenbp", transformInvocation)) {
            backports.add(new TimeBackport());
        }

        final TypeMap typeMap = new TypeMap();
        final MethodMap methodMap = new MethodMap();
        backports.forEach(backport -> backport.map(typeMap, methodMap));

        if (!transformInvocation.isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll();
        }

        transformInvocation.getInputs().forEach(transformInput -> {
            transformInput.getDirectoryInputs().forEach(Lambdas.consumer(directoryInput -> {
                final File outputDir = transformInvocation.getOutputProvider().getContentLocation(
                        directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);

                FileUtils.deleteRecursivelyIfExists(outputDir);
                if (!outputDir.mkdirs()) {
                    throw new RuntimeException("Could not create output directory");
                }

                final ClassPool classPool = new ClassPool();
                classPool.appendSystemPath();
                classPool.insertClassPath(directoryInput.getFile().getAbsolutePath());
                for (final String dep : deps) {
                    classPool.insertClassPath(dep);
                }

                final Path inputPath = Paths.get(directoryInput.getFile().getAbsolutePath());
                Files.walk(inputPath)
                        .filter(Files::isRegularFile)
                        .filter(Lambdas.predicate(path -> {
                            if (path.getFileName().toFile().getName().endsWith(".class")) {
                                return true;
                            }
                            final File file = new File(outputDir, inputPath.relativize(path).toString());
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
                if (jarInput.getName().startsWith("net.sourceforge.streamsupport")) {
                    FileUtils.copyFile(jarInput.getFile(), outputJar);
                    return;
                }
                FileUtils.deleteRecursivelyIfExists(outputDir);
                FileUtils.deleteIfExists(outputJar);
                if (jarInput.getStatus() == Status.REMOVED) {
                    return;
                }
                if (!outputDir.mkdirs()) {
                    throw new RuntimeException("Could not create output directory");
                }

                final ClassPool classPool = new ClassPool();
                classPool.appendSystemPath();
                classPool.insertClassPath(jarInput.getFile().getAbsolutePath());
                for (final String dep : deps) {
                    classPool.insertClassPath(dep);
                }

                final ZipFile zipFile = new ZipFile(jarInput.getFile());
                zipFile.stream()
                        .filter(entry -> !entry.isDirectory())
                        .filter(Lambdas.predicate(entry -> {
                            if (entry.getName().endsWith(".class") && !entry.getName().startsWith("META-INF/")
                                    && !entry.getName().startsWith("META-INF\\")) {
                                return true;
                            }
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
                FileUtils.deleteRecursivelyIfExists(outputDir);
            }));
        });
    }

    private static boolean hasBackport(
            final @NotNull String name, final @NotNull TransformInvocation transformInvocation) {

        return transformInvocation.getInputs().stream()
                .flatMap(transformInput -> transformInput.getJarInputs().stream())
                .anyMatch(jarInput -> jarInput.getName().startsWith(name));
    }

    private static void transformClass(
            final @NotNull ClassPool classPool, final @NotNull String className, final @NotNull TypeMap typeMap,
            final @NotNull MethodMap methodMap, final @NotNull File outputDir) throws Exception {

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
