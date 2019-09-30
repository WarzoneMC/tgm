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

    public enum EditableField {
        PREFIX(){
            @Override
            public String parseValue(String value) {
                return value;
            }
        },
        PRIORITY() {
            @Override
            public Integer parseValue(String value) {
                return Integer.parseInt(value);
            }
        },
        PERMISSIONS(){
            @Override
            public List<String> parseValue(String value) {
                List<String> list = new ArrayList<>();
                if (value != null) {
                    String[] args = value.split(" ");
                    list = Arrays.asList(args);
                }
                return list;
            }
        },
        STAFF(){
            @Override
            public Boolean parseValue(String value) {
                return Boolean.valueOf(value);
            }
        },
        DEFAULT(){
            @Override
            public Boolean parseValue(String value) {
                return Boolean.valueOf(value);
            }
        };

        public abstract Object parseValue(String value);
    }

}
