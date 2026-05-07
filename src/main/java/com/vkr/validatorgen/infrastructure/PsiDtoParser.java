package com.vkr.validatorgen.infrastructure;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.vkr.validatorgen.domain.DtoParser;
import com.vkr.validatorgen.domain.DtoSpec;
import com.vkr.validatorgen.domain.FieldMeta;
import com.vkr.validatorgen.domain.TypeRef;

import java.util.*;
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

            List<FieldMeta> fields = Arrays.stream(psiClass.getFields())
                    .map(f -> toFieldMeta(f, getterNames))
                    .sorted(Comparator.comparing(FieldMeta::name))
                    .toList();

            return new DtoSpec(pkg, className, getterNames, fields);
        });
    }

    private FieldMeta toFieldMeta(PsiField field, Set<String> getterNames) {
        PsiType psiType = field.getType();
        String canonical = psiType.getCanonicalText();
        String presentable = psiType.getPresentableText();
        TypeRef typeRef = TypeRef.of(canonical, presentable);

        boolean isPrimitive = psiType instanceof PsiPrimitiveType;
        String normalized = canonical == null || canonical.isBlank() ? presentable : canonical;

        boolean isStringLike = "java.lang.String".equals(normalized) || "String".equals(presentable)
                || "java.lang.CharSequence".equals(normalized) || "CharSequence".equals(presentable);
        boolean isBooleanLike = "boolean".equals(normalized) || "java.lang.Boolean".equals(normalized);
        boolean isNumericLike = isNumericType(normalized);
        boolean isEnumLike = isEnumType(field);
        boolean isReferenceLike = !isPrimitive && !isStringLike && !isBooleanLike && !isNumericLike && !isEnumLike;

        String getter = getterName(field.getName(), isBooleanLike);
        String accessor = getterNames.contains(getter) ? getter : "";

        return new FieldMeta(field.getName(), typeRef, isPrimitive, isStringLike, isBooleanLike, isNumericLike, isEnumLike, isReferenceLike, accessor);
    }

    private boolean isEnumType(PsiField field) {
        PsiType type = field.getType();
        if (!(type instanceof PsiClassType classType)) return false;
        PsiClass resolved = classType.resolve();
        return resolved != null && resolved.isEnum();
    }

    private boolean isNumericType(String normalizedType) {
        return Set.of(
                "byte", "short", "int", "long", "float", "double",
                "java.lang.Byte", "java.lang.Short", "java.lang.Integer", "java.lang.Long", "java.lang.Float", "java.lang.Double",
                "java.math.BigDecimal", "java.math.BigInteger"
        ).contains(normalizedType);
    }

    private String getterName(String fieldName, boolean booleanLike) {
        if (fieldName == null || fieldName.isBlank()) return "";
        String suffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        return booleanLike ? "is" + suffix : "get" + suffix;
    }
}

