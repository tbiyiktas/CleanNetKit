package lib.net;


import java.lang.reflect.Type;

import lib.net.parser.IResponseParser;

public class ResponseHandler {

    private final IResponseParser parser;

    public ResponseHandler(IResponseParser parser) {
        this.parser = parser;
    }

    @SuppressWarnings("unchecked")
    public <T> NetResult<T> handle(NetResult<String> rawResult, Type responseType) {
        if (rawResult instanceof NetResult.Success) {
            String jsonContent = ((NetResult.Success<String>) rawResult).Data();
            try {
                if (responseType == String.class) {
                    return (NetResult<T>) rawResult; // Passthrough
                }
                if (jsonContent == null || jsonContent.isEmpty()) {
                    return new NetResult.Success<>(null);   // 204 vb.
                }
                T parsed = parser.parse(jsonContent, responseType);
                return new NetResult.Success<>(parsed);
            } catch (Exception e) { // <-- JsonSyntaxException dışındaki hataları da kapsa
                return new NetResult.Error<>(e, -1, "Parse error: " + e.getMessage());
            }
        } else if (rawResult instanceof NetResult.Error) {
            return (NetResult<T>) rawResult;
        }
        return new NetResult.Error<>(new Exception("Unknown raw result type"), -1, "Unknown error");
    }
}
