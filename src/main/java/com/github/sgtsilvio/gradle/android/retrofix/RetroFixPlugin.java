package com.github.sgtsilvio.gradle.android.retrofix;

import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.BasePlugin;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class RetroFixPlugin implements Plugin<Project> {

    private @Nullable BaseExtension androidExtension;

    @Override
    public void apply(final @NotNull Project project) {
        project.getPlugins().withType(BasePlugin.class, plugin -> {
            androidExtension = (BaseExtension) project.getExtensions().getByName("android");
            androidExtension.registerTransform(new RetroFixTransform(androidExtension));
        });

        project.afterEvaluate(project1 -> {
            if (androidExtension == null) {
                throw new GradleException("The RetroFix plugin requires the 'com.android.application' plugin.");
            }
            if (androidExtension.getDefaultConfig().getMinSdkVersion().getApiLevel() >= 24) {
                throw new GradleException("The RetroFix plugin should not be used when the minSdkVersion >= 24.");
            }
        });
    }
}
