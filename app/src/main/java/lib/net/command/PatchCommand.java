package lib.net.command;

import java.util.HashMap;

import lib.net.strategy.MethodStrategy;
import lib.net.strategy.impl.JsonBodyWriter;
import lib.net.strategy.impl.PatchMethodStrategy;

public class PatchCommand extends ACommand {
    private final String jsonContent;

    public PatchCommand(String relativeUrl, String jsonContent) {
        super(relativeUrl);
        this.jsonContent = jsonContent;
        withBodyWriter(new JsonBodyWriter(() -> this.jsonContent));
        withMethodStrategy(new PatchMethodStrategy());
    }

    public PatchCommand(String relativeUrl, String jsonContent, HashMap<String, String> headers) {
        super(relativeUrl, null, headers);
        this.jsonContent = jsonContent;
        withBodyWriter(new JsonBodyWriter(() -> this.jsonContent));
        withMethodStrategy(new PatchMethodStrategy());
    }

    @Override
    public String getMethodName() {
        return "PATCH";
    }

    /**
     * İstersen post override ile kullan:
     */
    public PatchCommand withPostOverride(MethodStrategy patchMethodStrategy) {
        return (PatchCommand) withMethodStrategy(patchMethodStrategy);
    }
}
