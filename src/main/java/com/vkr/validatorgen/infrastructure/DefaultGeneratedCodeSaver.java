package com.vkr.validatorgen.infrastructure;

import com.intellij.openapi.project.Project;
import com.vkr.validatorgen.domain.DtoSpec;
import com.vkr.validatorgen.domain.GeneratedCodeSaver;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DefaultGeneratedCodeSaver implements GeneratedCodeSaver {

    private final Project project;

    public DefaultGeneratedCodeSaver(Project project) {
        this.project = project;
    }

    @Override
    public Path save(DtoSpec dto, String code) {
        String baseDir = project.getBasePath();
        if (baseDir == null || baseDir.isBlank()) {
            throw new IllegalStateException("Project basePath is null. Cannot save.");
        }
        if (code == null) code = "";

        String outPackage = dto.getPackageName().isBlank()
                ? "generated"
                : dto.getPackageName() + ".generated";

        String className = dto.getClassName() + "GeneratedValidator";

        String packagePath = outPackage.replace('.', '/');
        Path outDir = Path.of(baseDir, "generated-sources", "validator", packagePath);

        try {
            Files.createDirectories(outDir);
            Path outFile = outDir.resolve(className + ".java");
            Files.writeString(outFile, code, StandardCharsets.UTF_8);
            return outFile;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save generated code: " + e.getMessage(), e);
        }
    }
}
