package io.jpm.core.jpm_repository.domain.model;

import java.util.List;
import java.util.Objects;


public class DslStatementV2 {
    private final String command;       // 예: "where", "select", "whereExistsGroup"
    private final List<Object> args;    // 예: ["target.id", "=", "10"]
    private final List<DslStatementV2> subStatements; // 그룹/서브쿼리용



    // 추가: segment 인라인 시 원본 출처
    private String sourceClassName;   // "DefaultQuerySegment"
    private String sourceMethodName;  // "defaultSelectAndOneWhere"


    public DslStatementV2 withSource(String className, String methodName) {
        this.sourceClassName  = className;
        this.sourceMethodName = methodName;
        return this;
    }


    public String getSourceClassName()  { return sourceClassName; }
    public String getSourceMethodName() { return sourceMethodName; }


    public DslStatementV2(String command, List<Object> args) {
        this(command, args, null);
    }

    public DslStatementV2(String command, List<Object> args, List<DslStatementV2> subStatements) {
        this.command = command;
        this.args = args;
        this.subStatements = subStatements;
    }

    public String getCommand() { return command; }
    public List<Object> getArgs() { return args; }
    public List<DslStatementV2> getSubStatements() { return subStatements; }

    // 편의 메서드
    public Object getArg(int index) {
        if (args == null || index >= args.size()) return null;
        return args.get(index);
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DslStatement{");
        sb.append("command='").append(command).append('\'');

        if (args != null && !args.isEmpty()) {
            sb.append(", args=").append(args);
        }

        if (subStatements != null && !subStatements.isEmpty()) {
            sb.append(", subStatements=").append(subStatements);
        }

        if (sourceClassName != null) {
            sb.append(", source=").append(sourceClassName).append('#').append(sourceMethodName);
        }

        sb.append('}');
        return sb.toString();
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DslStatementV2)) return false;
        DslStatementV2 that = (DslStatementV2) o;
        return Objects.equals(command, that.command)
                && Objects.equals(args, that.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, args);
    }

}
