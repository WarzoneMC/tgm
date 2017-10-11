package network.warzone.tgm.modules.filter.evaluate;

import network.warzone.tgm.modules.filter.FilterResult;

public class DenyFilterEvaluator implements FilterEvaluator {

    @Override
    public FilterResult evaluate(Object... objects) {
        return FilterResult.DENY;
    }
}
