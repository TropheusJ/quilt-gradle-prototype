package org.quiltmc.quilt_gradle.remapping.task;

import net.neoforged.art.api.Renamer;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.TaskAction;
import org.quiltmc.quilt_gradle.util.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class RemapClasspath extends DefaultTask {
    @InputFiles
    public abstract Property<FileCollection> getClasspath();
    @InputFile
    public abstract Property<File> getMappingFile();

    @OutputFiles
    public abstract ListProperty<File> getRemapped();

    @TaskAction
    public void remap() {
        FileCollection files = this.getClasspath().get();
        if (files.isEmpty())
            return;
        Renamer.Builder builder = Renamer.builder()
                .map(this.getMappingFile().get())
                .withJvmClasspath();
        files.forEach(builder::lib);

        try (Renamer renamer = builder.build()) {
            List<File> remappedFiles = new ArrayList<>();
            for (File file : files) {
                String name = file.getName();
                String remappedName = FileUtils.suffixed(name, "-remapped");
                File target = file.toPath().resolveSibling(remappedName).toFile();
                renamer.run(file, target);
                remappedFiles.add(target);
            }
            this.getRemapped().set(remappedFiles);
        } catch (IOException e) {
            throw new RuntimeException("Error remapping mods", e);
        }
    }


}
