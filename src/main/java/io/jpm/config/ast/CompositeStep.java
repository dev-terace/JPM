package io.jpm.config.ast;

import java.util.List;

public interface CompositeStep<C extends Context>  {

    List<Step<C>> getSteps();

    default void execute(C context) throws Exception {
        for (Step<C> step : getSteps()) {
            step.execute(context);
        }
    }

}
