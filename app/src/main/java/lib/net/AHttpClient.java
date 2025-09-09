package lib.net;

import java.io.IOException;

import lib.net.connection.HttpUrlConnectionFactory;
import lib.net.connection.IHttpConnection;
import lib.net.connection.IHttpConnectionFactory;

public abstract class AHttpClient {

    private final IHttpConnectionFactory connectionFactory;
    private final String basePath;

    protected AHttpClient(String basePath, IHttpConnectionFactory connectionFactory) {
        this.basePath = basePath;
        this.connectionFactory = connectionFactory;
    }

    protected AHttpClient(String basePath) {
        this.basePath = basePath;
        this.connectionFactory = new HttpUrlConnectionFactory();
    }

    public IHttpConnection createConnection(String fullUrl) throws IOException {
        IHttpConnection connection = connectionFactory.createConnection(fullUrl);

        connection.setInstanceFollowRedirects(true);
        connection.setUseCaches(false);
        connection.setAllowUserInteraction(false);

        return connection;
    }
//    public String buildUrlString(ACommand command) {
//        return UrlBuilder.build(this.basePath, command);
//    }
}