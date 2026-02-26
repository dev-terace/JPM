package exception.config;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

public class JpmToolbox {

    private final Trees trees;
    private final SourcePositions positions;
    private CompilationUnitTree currentCut;

    public JpmToolbox(ProcessingEnvironment processingEnv) {
        this.trees = Trees.instance(processingEnv);
        this.positions = trees.getSourcePositions();
    }

    /**
     * 현재 분석 중인 파일(CompilationUnit)을 업데이트합니다.
     * 스캐너의 visitCompilationUnit에서 호출해야 합니다.
     */
    public void setCurrentCut(CompilationUnitTree cut) {
        this.currentCut = cut;
    }

    /**
     * MethodInvocationTree에서 메서드 이름만 추출합니다. (ex: select, where)
     */
    public String getMethodName(MethodInvocationTree call) {

        if (call == null) {
            return "not_an_invocation";
        }

        ExpressionTree methodSelect = call.getMethodSelect();

        if (methodSelect instanceof MemberSelectTree) {
            // query.select() 형태
            return ((MemberSelectTree) methodSelect).getIdentifier().toString();
        } else if (methodSelect instanceof IdentifierTree) {
            // select() 형태
            return ((IdentifierTree) methodSelect).getName().toString();
        }

        return methodSelect.toString();
    }

    /**
     * 특정 노드의 줄 번호(Line Number)를 가져옵니다.
     */
    public long getLineNumber(Tree tree) {
        if (currentCut == null || tree == null) return -1;

        long startPos = positions.getStartPosition(currentCut, tree);
        if (startPos == -1) return -1;

        return currentCut.getLineMap().getLineNumber(startPos);
    }

    /**
     * 현재 TreePath에 해당하는 Element(의미 정보)를 가져옵니다.
     */
    public VariableTree getVariableTree(Element element)
    {
        return (VariableTree) trees.getTree(element);
    }
    public Element getElement(TreePath path) {
        if (path == null) return null;

        Element element = trees.getElement(path);
        if (element != null) return element;

        // fallback: 타입 기준으로 시도
        TypeMirror type = trees.getTypeMirror(path);
        if (type != null) {
            return trees.getElement(path);
        }

        return null;

    }

    /**
     * (선택) 에러 발생 시 파일명을 가져오기 위한 유틸리티
     */
    public String getFileName() {
        if (currentCut == null) return "Unknown";
        return currentCut.getSourceFile().getName();
    }
}
