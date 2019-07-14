package com.github.sgtsilvio.gradle.android.retrofix;

import com.android.build.gradle.AppPlugin;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Silvio Giebl
 */
public class RetroFixPlugin implements Plugin<Project> {

    private static final @NotNull Logger logger = Logging.getLogger(RetroFixPlugin.class);

    private @Nullable AppPlugin appPlugin;

    @Override
    public void apply(final @NotNull Project project) {
        if (project.getPlugins().hasPlugin(AppPlugin.class)) {
            init(project.getPlugins().getPlugin(AppPlugin.class));
        } else {
            project.getPlugins().whenPluginAdded(plugin -> {
                if (plugin instanceof AppPlugin) {
                    logger.warn("The 'com.android.application' plugin should be applied before the RetroFix plugin.");
                    init((AppPlugin) plugin);
                }
            });
        }

        project.afterEvaluate(project1 -> {
            if (appPlugin == null) {
                throw new GradleException("The RetroFix plugin requires the 'com.android.application' plugin.");
            }
            if (appPlugin.getExtension().getDefaultConfig().getMinSdkVersion().getApiLevel() >= 24) {
                throw new GradleException("The RetroFix plugin should not be used when the minSdkVersion >= 24.");
            }
        });
    }

    private void init(final @NotNull AppPlugin appPlugin) {
        this.appPlugin = appPlugin;
        appPlugin.getExtension().registerTransform(new RetroFixTransform(appPlugin.getExtension()));
    }
}
