package com.minehut.tgm.modules.filter.evaluate;

import com.minehut.tgm.modules.filter.FilterResult;

public interface FilterEvaluator {
    FilterResult evaluate(Object... objects);
}
