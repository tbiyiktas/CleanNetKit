package lib.net.command;

import java.util.HashMap;

import lib.net.strategy.impl.JsonBodyWriter;

public class PutCommand extends ACommand {
    private final String jsonContent;

    public PutCommand(String relativeUrl, String jsonContent) {
        super(relativeUrl);
        this.jsonContent = jsonContent;
        withBodyWriter(new JsonBodyWriter(() -> this.jsonContent));
    }

    public PutCommand(String relativeUrl, String jsonContent, HashMap<String, String> headers) {
        super(relativeUrl, null, headers);
        this.jsonContent = jsonContent;
        withBodyWriter(new JsonBodyWriter(() -> this.jsonContent));
    }

    @Override
    public String getMethodName() {
        return "PUT";
    }
}
