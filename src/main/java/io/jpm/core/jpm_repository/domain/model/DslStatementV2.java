package io.jpm.core.jpm_repository.domain.model;


import groovy.transform.EqualsAndHashCode;

import java.util.List;
import java.util.Objects;


@EqualsAndHashCode
public class DslStatementV2 {
    private final String command;       // 예: "where", "select", "whereExistsGroup"
    private final List<Object> args;    // 예: ["target.id", "=", "10"]
    private final List<DslStatementV2> subStatements; // 그룹/서브쿼리용




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



        sb.append('}');
        return sb.toString();
    }





}
