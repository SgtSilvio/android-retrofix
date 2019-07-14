package com.github.sgtsilvio.gradle.android.retrofix;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.AppPlugin;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class RetroFixPlugin implements Plugin<Project> {

    @Override
    public void apply(final @NotNull Project project) {
        final boolean isAndroid = project.getPlugins().hasPlugin(AppPlugin.class);
        if (!isAndroid) {
            throw new GradleException("'com>.android.application' or 'com.android.library' plugin required.");
        }

        final AppExtension android = (AppExtension) project.getExtensions().findByName("android");
        if (android == null) {
            throw new GradleException("'com.android.application' or 'com.android.library' plugin required.");
        }
        android.registerTransform(new RetroFixTransform(android));

        project.afterEvaluate(project1 -> {
            if (android.getDefaultConfig().getMinSdkVersion().getApiLevel() >= 24) {
                throw new GradleException("the RetroFix plugin should not be used when the minSdkVersion >= 24");
            }
        });
    }
}
