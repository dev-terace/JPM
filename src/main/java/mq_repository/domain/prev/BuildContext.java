package mq_repository.domain.prev;

import mq_repository.domain.SqlDialectType;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class BuildContext {

    private final List<Object> bindValues = new ArrayList<>();
    private final SqlDialectType dialect;

    public BuildContext(SqlDialectType dialect) {
        this.dialect = dialect;
    }

    public void addBind(Object value) {
        bindValues.add(value);
    }

    public List<Object> getBindValues() {
        return bindValues;
    }

    public SqlDialectType getDialect() {
        return dialect;
    }
}

