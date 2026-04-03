package io.jpm.core.m_entity.steps.MEntityJavaTypeResolverHandler.main;

import io.jpm.common.utils.LogPrinter;
import io.jpm.config.ast.Step;
import io.jpm.core.m_entity.domain.MFieldJavaTypeMeta;
import io.jpm.core.m_entity.steps.MEntityJavaTypeResolverHandler.context.GeneratedJavaTypeContext;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;

import java.util.Set;

public class GeneratedJavaTypeMainStep implements Step<GeneratedJavaTypeContext> {


    @Override
    public void execute(GeneratedJavaTypeContext ctx) throws Exception {
        Set<Path> createdDirs = new HashSet<>();



        for (MFieldJavaTypeMeta meta : ctx.getMetas()) {



                Path filePath = meta.getGeneratePath();

                // 디렉토리 생성 중복 제거
                Path parentDir = filePath.getParent();
                if (createdDirs.add(parentDir)) {
                    Files.createDirectories(parentDir);
                }

                // StringBuilder로 전체 조립 후 단 1회 write
                String content = buildContent(meta);


                LogPrinter.info("GeneratedJavaTypeMainStep content: " + content +", filePath : "+filePath);
                Files.write(filePath,
                        content.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);


        }
    }


    private String buildContent(MFieldJavaTypeMeta meta) {

        StringBuilder sb = new StringBuilder(512);

        sb.append("package ").append(meta.getPackageName()).append(";\n");
        for (String importPackage : meta.getImportPackages()) {
            sb.append("import ").append(importPackage).append(";\n");
        }

        sb.append("\n");

        sb.append("public class ").append(meta.getJavaTypeSimpleName()).append(" {\n\n");

        for (MFieldJavaTypeMeta.info info : meta.getTypeInfos()) {
            sb.append("    private ")
                    .append(info.getMFieldType())
                    .append(" ")
                    .append(info.getFieldName())
                    .append(";\n");
        }

        sb.append("}");

        return sb.toString();
    }




}
