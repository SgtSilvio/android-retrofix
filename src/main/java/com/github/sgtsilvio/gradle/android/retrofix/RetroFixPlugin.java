package com.github.sgtsilvio.gradle.android.retrofix;

import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryPlugin;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author Silvio Giebl
 */
public class RetroFixPlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {
        final boolean isAndroid = project.getPlugins().hasPlugin(AppPlugin.class) ||
                project.getPlugins().hasPlugin(LibraryPlugin.class);
        if (!isAndroid) {
            throw new GradleException("'com.android.application' or 'com.android.library' plugin required.");
        }

        final BaseExtension android = (BaseExtension) project.getExtensions().findByName("android");
        if (android == null) {
            throw new GradleException("'com.android.application' or 'com.android.library' plugin required.");
        }
        android.registerTransform(new RetroFixTransform(android));
    }
}
