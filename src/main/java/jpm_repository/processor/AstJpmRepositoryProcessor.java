package jpm_repository.processor;

import annotation.JpmRepository;
import annotation.MqInject;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import config.AppConfig;

import exception.ErrorCollector;
import exception.cache.domain.vo.FieldSourceLocation;
import exception.cache.domain.SourceLocationCache;
import exception.config.JpmChainExtractorScanner;
import exception.config.JpmToolbox;
import jpm_repository.parse.domain.policy.sql_mapper_binder.SqlMapperBinder;
import jpm_repository.parse.domain.vo.EntityMeta;
import jpm_repository.parse.domain.vo.MethodMeta;
import jpm_repository.parse.domain.vo.RepoMeta;
import jpm_repository.generator.infra.mapper.MybatisXmlGenerator;
import jpm_repository.parse.domain.vo.ResultMapMeta;
import jpm_repository.parse.infra.ast.jpm_repo_parser.AstRepoParserHandlerV3Impl;
import jpm_repository.parse.domain.cache.RepoMetaRegistry;
import utils.LogPrinter;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;


public class AstJpmRepositoryProcessor {

    private final RoundEnvironment roundEnv;
    private final ProcessingEnvironment processingEnv;
    private static final RepoMetaRegistry REPO_META_REGISTRY = AppConfig.getEntityMetaRegistry();
    private static final SqlMapperBinder binder = AppConfig.getSqlMapperBinder();
    private static final SourceLocationCache sourceLocationCache = AppConfig.getSourceLocationCache();

    private final JpmChainExtractorScanner jpmChainExtractorScanner;
    private final JpmToolbox jpmToolbox;
    public AstJpmRepositoryProcessor(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv) {
        this.roundEnv = roundEnv;
        this.processingEnv = processingEnv;
        this.jpmToolbox = new JpmToolbox(this.processingEnv);
        this.jpmChainExtractorScanner = new JpmChainExtractorScanner(this.jpmToolbox);

    }




    public void generate() {


        for (Element element : roundEnv.getElementsAnnotatedWith(MqInject.class)) {
            if (element.getKind() != ElementKind.FIELD) continue;

            VariableElement field = (VariableElement) element;
            TypeMirror fieldType = field.asType();
            Element typeElement = processingEnv.getTypeUtils().asElement(fieldType);

            if (!(typeElement instanceof TypeElement)) continue;

            String segmentFqcn = ((TypeElement) typeElement).getQualifiedName().toString();
            String repoName    = field.getEnclosingElement().getSimpleName().toString();
            String varName     = field.getSimpleName().toString(); // "orderSegment"

            REPO_META_REGISTRY.registerSegmentPath(repoName, varName, segmentFqcn);

            sourceLocationCache.registerFieldLocation(repoName, varName,
                    FieldSourceLocation.builder()
                            .className(repoName)
                            .fieldName(varName)
                            .element(field)
                            .build()
            );
        }


        for (Element element : this.roundEnv.getElementsAnnotatedWith(JpmRepository.class)) {




            TypeElement repoElement = (TypeElement) element;
            Trees tree = Trees.instance(this.processingEnv);

            TreePath path = tree.getPath(repoElement);
            jpmToolbox.setCurrentCut(path.getCompilationUnit());
            jpmChainExtractorScanner.setClassName(element.getSimpleName().toString());
            ErrorCollector.setClassName(repoElement.getSimpleName().toString());
            ErrorCollector.setTrees(tree);
            jpmChainExtractorScanner.init(element);
            jpmChainExtractorScanner.scan(path, null);


            RepoMeta repo =  new AstRepoParserHandlerV3Impl(REPO_META_REGISTRY)
                    .parseRepo(repoElement, this.processingEnv, tree);


            List<MybatisXmlGenerator.MethodData> methodDataList = new ArrayList<>();



            try {
                for (MethodMeta method : repo.getMethods()) {
                    ErrorCollector.setMethodName(method.getMethodName());

                    EntityMeta entityMeta = REPO_META_REGISTRY.getEntityMeta(method.getTargetType());

                    LogPrinter.info("generateSql: " + method.getTargetType());

                    String finalSql = String.valueOf(binder.generateSql(method, entityMeta));

                    ResultMapMeta mappingMeta = ResultMapMeta.from(method);

                    // 2. XML 생성을 위해 리스트에 데이터 적재 (아직 XML 생성 안 함)
                    methodDataList.add(new MybatisXmlGenerator.MethodData(method, mappingMeta, finalSql));

                }
            }
            catch (Exception e)
            {
                LogPrinter.error(e.getMessage());
            }

            String errorMessage = ErrorCollector.reportAll();
            if(errorMessage != null) {throw  new IllegalArgumentException(errorMessage);}

            if (!methodDataList.isEmpty()) {
                MybatisXmlGenerator mybatisXmlGenerator = new MybatisXmlGenerator();

                String resultXml = mybatisXmlGenerator.generateXml(repo.getNamespace(), methodDataList);

                LogPrinter.info("\n[완성된 MyBatis XML]");
                LogPrinter.info(resultXml);
            }



            // registry 저장 등 처리
        }
    }


}
