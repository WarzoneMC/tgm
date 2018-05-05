package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Jorge on 2/23/2018.
 */
@AllArgsConstructor
public class RankEditRequest {

    private String name;

    private Object value;

    public static enum EditableField {
        PREFIX(){
            @Override
            public String parseValue(String value) {
                return (String) value;
            }
        },
        PRIORITY() {
            @Override
            public Integer parseValue(String value) {
                return Integer.parseInt((String) value);
            }
        },
        PERMISSIONS(){
            @Override
            public List<String> parseValue(String value) {
                List<String> list = new ArrayList<>();
                if (value instanceof String) {
                    String[] args = ((String) value).split(" ");
                    list = Arrays.asList(args);
                }
                return list;
            }
        },
        STAFF(){
            @Override
            public Boolean parseValue(String value) {
                return Boolean.valueOf((String) value);
            }
        };

        public abstract Object parseValue(String value);
    }

}
