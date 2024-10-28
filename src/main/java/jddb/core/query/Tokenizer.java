package jddb.core.query;



import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    private List<String> tokens = new ArrayList<>();

    public Tokenizer(String nativeQuery){
        boolean isQuoted = false;
        StringBuilder buffer = new StringBuilder();

        for (char chr: nativeQuery.toCharArray()){
            if (chr == '"'){
                isQuoted = !isQuoted;
            }

            if (!isQuoted){
                if (chr == ';'){
                    break;
                }

                if (chr == ' ' || chr=='='){
                    if (!buffer.isEmpty()){
                        tokens.add(buffer.toString());
                    }

                    buffer.setLength(0);
                    continue;
                }


            }

            buffer.append(chr);

        }

        tokens.add(buffer.toString());
    }

    public List<String> getTokens(){
        return tokens;
    }
}
