// lib/net/command/DeleteCommand.java
package lib.net.command;

import java.util.HashMap;

public class DeleteCommand extends ACommand {

    public DeleteCommand(String relativeUrl) {
        super(relativeUrl);
    }

    public DeleteCommand(String relativeUrl, HashMap<String, String> parameters) {
        super(relativeUrl, parameters);
    }

    public DeleteCommand(String relativeUrl,
                         HashMap<String, String> parameters,
                         HashMap<String, String> headers) {
        super(relativeUrl, parameters, headers);
    }

    @Override
    public String getMethodName() {
        return "DELETE";
    }
}
