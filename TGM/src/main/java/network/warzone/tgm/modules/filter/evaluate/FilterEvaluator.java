package network.warzone.tgm.modules.filter.evaluate;

import network.warzone.tgm.modules.filter.FilterResult;

public interface FilterEvaluator {
    FilterResult evaluate(Object... objects);
}
