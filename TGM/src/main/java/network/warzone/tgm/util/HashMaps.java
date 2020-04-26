package network.warzone.tgm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HashMaps {

    public static <T, R> List<T> reverseGet(R key, HashMap<T, R> hashMap) {
        List<T> results = new ArrayList<>();
        for (HashMap.Entry<T, R> entry : hashMap.entrySet()) {
            if (entry.getValue().equals(key)) {
                results.add(entry.getKey());
            }
        }
        return results;
    }

    public static <T, R> T reverseGetFirst(R key, HashMap<T, R> hashMap) {
        for (HashMap.Entry<T, R> entry : hashMap.entrySet()) {
            if (entry.getValue().equals(key)) {
                return entry.getKey();
            }
        }
        return null;
    }

}
