package io.jpm.core.m_entity.domain;

import io.jpm.core.m_entity.utils.GeneratedPathResolver;

import javax.annotation.processing.Filer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MFieldJavaTypeMeta {

    private Path generatePath;

    private final List<info> typeInfos = new ArrayList<>();

    private final Set<String> importPackages = new HashSet<>();


    private final Filer filer;

    public MFieldJavaTypeMeta(Filer filer) {
        this.filer = filer;
    }

    public void addTypeInfo(String fieldName, String mFieldType)
    {
        String packageName = MFieldJavaType.getMFieldTypeMap(mFieldType);



            String simpleName = packageName.substring(packageName.lastIndexOf('.') + 1);
            typeInfos.add(new info(fieldName, simpleName));
            importPackages.add(packageName);



    }

    public void setGeneratePath(String fqcn)
    {
        generatePath = GeneratedPathResolver.resolve(fqcn, filer);
    }

    public String getJavaTypeSimpleName() {
        String fileName = generatePath.getFileName().toString();
        return fileName.replace(".java", "");
    }

    public Path getGeneratePath() {
        return generatePath;
    }

    public String getJavaTypeFqcn() {
        List<Path> parts = new ArrayList<>();
        generatePath.iterator().forEachRemaining(parts::add);

        // "main" 인덱스 이후부터 패키지 경로
        int mainIdx = -1;
        for (int i = 0; i < parts.size(); i++) {
            if (parts.get(i).toString().equals("main")) {
                mainIdx = i;
                break;
            }
        }

        // main/entity/ROrderItemEntity.java → entity.ROrderItemEntity
        return IntStream.range(mainIdx + 1, parts.size())
                .mapToObj(i -> parts.get(i).toString())
                .collect(Collectors.joining("."))
                .replace(".java", "");
    }

    public String getPackageName() {
        String fqcn = getJavaTypeFqcn();
        int last = fqcn.lastIndexOf('.');
        return last < 0 ? "" : fqcn.substring(0, last);
    }

    public List<info> getTypeInfos() {
        return typeInfos;
    }

    public Set<String> getImportPackages() {
        return importPackages;
    }

    public static class info{
        private final String fieldName;
        private final String mFieldType;


        public info(String fieldName, String mFieldType) {
            this.fieldName = fieldName;
            this.mFieldType = mFieldType;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getMFieldType() {
            return mFieldType;
        }


    }

}
