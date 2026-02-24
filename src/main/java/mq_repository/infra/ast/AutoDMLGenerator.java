package mq_repository.infra.ast;

import annotation.JpmRepository;
import annotation.MqInject;
import com.sun.source.util.Trees;
import config.AppConfig;

import mq_mapper.domain.policy.SqlMapperBinder;
import mq_mapper.domain.vo.EntityMeta;
import mq_mapper.domain.vo.MethodMeta;
import mq_mapper.domain.vo.RepoMeta;
import mq_mapper.infra.MybatisXmlGenerator;
import mq_mapper.infra.ResultMapMeta;
import mq_mapper.infra.ast.MqRepoParserV3;
import mq_mapper.infra.repo.EntityMetaRegistry;
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

public class AutoDMLGenerator {

    private final RoundEnvironment roundEnv;
    private final ProcessingEnvironment processingEnv;
    private static final EntityMetaRegistry entityMetaRegistry = AppConfig.getEntityMetaRegistry();
    private static final SqlMapperBinder binder = AppConfig.getSqlMapperBinder();

    public AutoDMLGenerator(RoundEnvironment roundEnv, ProcessingEnvironment processingEnv) {
        this.roundEnv = roundEnv;
        this.processingEnv = processingEnv;

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

            entityMetaRegistry.registerSegmentPath(repoName, varName, segmentFqcn);

            LogPrinter.info("registered segment: " + repoName + "." + varName + " → " + segmentFqcn);
        }


        for (Element element : this.roundEnv.getElementsAnnotatedWith(JpmRepository.class)) {

            TypeElement repoElement = (TypeElement) element;


            Trees tree = Trees.instance(this.processingEnv);
            RepoMeta repo =  new MqRepoParserV3(entityMetaRegistry)
                    .parseRepo(repoElement, this.processingEnv, tree);
            /*MqRepoParserV2.parseRepo(repoElement, this.processingEnv, tree);*/

            List<MybatisXmlGenerator.MethodData> methodDataList = new ArrayList<>();


            try {
                for (MethodMeta method : repo.getMethods()) {


                    EntityMeta entityMeta = entityMetaRegistry.getEntityMeta(method.getTargetType());

                    LogPrinter.info("generateSql: " + method.getTargetType());
                    String finalSql = String.valueOf(binder.generateSql(method, entityMeta));




                    ResultMapMeta mappingMeta = ResultMapMeta.from(method);

                    // 2. XML 생성을 위해 리스트에 데이터 적재 (아직 XML 생성 안 함)
                    methodDataList.add(new MybatisXmlGenerator.MethodData(method, mappingMeta, finalSql));


                }
            }catch (Exception e)
            {
                LogPrinter.exceptionInfo(e);
            }


            LogPrinter.info("[4]" + repo.getNamespace() + " ");
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
