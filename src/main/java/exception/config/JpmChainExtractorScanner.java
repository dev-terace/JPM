package exception.config;

import com.sun.source.tree.*;

import com.sun.source.util.TreePathScanner;
import config.AppConfig;
import exception.cache.domain.vo.ChainSourceLocation;
import exception.cache.domain.SourceLocationCache;
import utils.LogPrinter;

import javax.lang.model.element.Element;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class JpmChainExtractorScanner extends TreePathScanner<Void, Void> {



    private static final Set<String> SUPPORTED_CHAIN_METHODS;

    static {
        // Java 8 방식의 불변 Set 초기화
        Set<String> tempSet = new HashSet<>(Arrays.asList(
                "select", "from", "where", "and", "or", "andGroup", "orGroup", "endGroup",
                "innerJoin", "leftJoin", "hashJoin", "mergeJoin", "loopJoin",
                "insertInto", "update", "deleteFrom", "value", "set", "setRaw",
                "orderBy", "groupBy", "limit", "offset", "sql", "selectRaw", "orderByRaw", "groupByRaw",
                "whereInGroup", "group", "fromGroup", "selectCase",
                "mapTarget", "mapId", "mapResult", "mapJoin", "innerJoinGroup", "leftJoinGroup",
                "whereExistsGroup", "whereNotExistsGroup"
        ));
        SUPPORTED_CHAIN_METHODS = Collections.unmodifiableSet(tempSet);
    }

    private final SourceLocationCache cache = AppConfig.getSourceLocationCache();
    private final JpmToolbox toolbox; // 앞서 만든 LineNumber, Element 추출 로직 포함 객체
    private String currentClassName;
    private String currentMethodName;
    private int chainIndexCounter = 0;
    private Element currentElement;


    public void init(Element currentElement) {
        this.currentElement = currentElement;
    }


    public JpmChainExtractorScanner(JpmToolbox toolbox) {

        this.toolbox = toolbox;
    }



    @Override
    public Void visitMethod(MethodTree node, Void p) {
        // 1. 새로운 메서드 진입 시 이름 저장 및 체인 인덱스 초기화
        try {
            this.currentMethodName = node.getName().toString();
            this.chainIndexCounter = 0;

            return super.visitMethod(node, p);
        }catch (Exception e) {
            LogPrinter.exceptionInfo(e);
            throw new RuntimeException("");
        }
    }




    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        // 먼저 내부(재귀)를 방문하여 안쪽 체인부터 처리하거나, 
        // 혹은 현재 노드부터 처리할 수 있습니다. (여기서는 현재 노드부터 처리)

        // 2. 메서드 이름 추출 (ex: select, from, where)
        String methodName = toolbox.getMethodName(node);

        if (isChainMethod(methodName)) {
            // 4. 위치 정보 및 Element 계산
            ExpressionTree methodSelect = node.getMethodSelect();
            long line = toolbox.getLineNumber(methodSelect);

            // 3. 로그 찍어보기
            LogPrinter.info("메서드: " + methodName + " | 확정된 Line: " + line);


            // 5. 빌더를 사용하여 ChainSourceLocation 생성


            String codeSnippest = node.toString();


            ChainSourceLocation loc = ChainSourceLocation.builder()
                    .className(this.currentClassName)
                    .methodName(this.currentMethodName)
                    .chainMethodName(methodName)
                    .chainIndex(chainIndexCounter++) // 순서 기록
                    .lineNumber(line)
                    .element(currentElement)
                    .expression(codeSnippest)
                    .build();

            // 6. 캐시에 저장!
            cache.registerChainLocation(this.currentClassName, this.currentMethodName, loc);

            LogPrinter.info("캐시 등록: " + currentMethodName + " -> " + methodName + " (Line: " + line + ")");
        }

        return super.visitMethodInvocation(node, p);
    }

    private boolean isChainMethod(String name) {
        // Set의 contains는 대량의 데이터 비교 시 matches(정규식)보다 훨씬 빠릅니다.
        return SUPPORTED_CHAIN_METHODS.contains(name);
    }

    public void setClassName(String className) {
        this.currentClassName = className;
    }
}