package io.jpm.core.jpm_repository.domain.model;

import java.util.List;


public class DslStatement {
    private final String command;       // 예: "where", "select", "whereExistsGroup"
    private final List<String> args;    // 예: ["target.id", "=", "10"]
    private final List<DslStatement> subStatements; // 그룹/서브쿼리용



    // 추가: segment 인라인 시 원본 출처
    private String sourceClassName;   // "DefaultQuerySegment"
    private String sourceMethodName;  // "defaultSelectAndOneWhere"


    public DslStatement withSource(String className, String methodName) {
        this.sourceClassName  = className;
        this.sourceMethodName = methodName;
        return this;
    }


    public String getSourceClassName()  { return sourceClassName; }
    public String getSourceMethodName() { return sourceMethodName; }


    public DslStatement(String command, List<String> args) {
        this(command, args, null);
    }

    public DslStatement(String command, List<String> args, List<DslStatement> subStatements) {
        this.command = command;
        this.args = args;
        this.subStatements = subStatements;
    }

    public String getCommand() { return command; }
    public List<String> getArgs() { return args; }
    public List<DslStatement> getSubStatements() { return subStatements; }

    // 편의 메서드
    public String getArg(int index) {
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
}
