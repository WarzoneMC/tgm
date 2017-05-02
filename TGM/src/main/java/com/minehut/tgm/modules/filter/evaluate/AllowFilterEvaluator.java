package com.minehut.tgm.modules.filter.evaluate;

import com.minehut.tgm.modules.filter.FilterResult;

public class AllowFilterEvaluator implements FilterEvaluator {

    @Override
    public FilterResult evaluate(Object... objects) {
        return FilterResult.ALLOW;
    }
}
