package com.vkr.validatorgen.infrastructure;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.vkr.validatorgen.domain.DtoParser;
import com.vkr.validatorgen.domain.DtoSpec;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class PsiDtoParser implements DtoParser {

    private final Project project;
    private final FileType javaFileType;

    public PsiDtoParser(Project project) {
        this.project = project;
        this.javaFileType = FileTypeManager.getInstance().getFileTypeByExtension("java");
    }

    @Override
    public DtoSpec parse(String javaText) {
        if (javaText == null) return null;

        return ReadAction.compute(() -> {
            PsiFile psiFile = PsiFileFactory.getInstance(project)
                    .createFileFromText("Dto.java", javaFileType, javaText);

            PsiJavaFile javaFile = (psiFile instanceof PsiJavaFile) ? (PsiJavaFile) psiFile : null;
            if (javaFile == null) return null;

            String pkg = javaFile.getPackageName();

            PsiClass psiClass = PsiTreeUtil.findChildOfType(javaFile, PsiClass.class);
            if (psiClass == null || psiClass.getName() == null) return null;

            String className = psiClass.getName();

            Set<String> getterNames = Arrays.stream(psiClass.getMethods())
                    .map(PsiMethod::getName)
                    .collect(Collectors.toSet());

            List<String> intFields = Arrays.stream(psiClass.getFields())
                    .filter(f -> PsiType.INT.equals(f.getType()))
                    .map(PsiField::getName)
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());

            return new DtoSpec(pkg, className, getterNames, intFields);
        });
    }
}
