// lib/net/ABaseApi.java
package lib.net;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import lib.net.command.ACommand;
import lib.net.command.DeleteCommand;
import lib.net.command.GetCommand;
import lib.net.command.MultipartCommand;
import lib.net.command.PatchCommand;
import lib.net.command.PostCommand;
import lib.net.command.PutCommand;
import lib.net.strategy.RequestConfigurator;

public abstract class ABaseApi {
    protected final String baseUrl;
    protected final NetworkManager networkManager;
    private final RequestConfigurator defaultRc; // opsiyonel

    public ABaseApi(String baseUrl, NetworkManager nm) {
        this(baseUrl, nm, null);
    }

    public ABaseApi(String baseUrl, NetworkManager nm, RequestConfigurator rc) {
        this.baseUrl = baseUrl;
        this.networkManager = nm;
        this.defaultRc = rc;
    }

    /**
     * Future-only gönderim (T döner)
     */
    protected <T> CancellableFuture<T> send(ACommand cmd, Type t) {
        if (defaultRc != null) cmd.withRequestConfigurator(defaultRc);
        return networkManager.enqueueFuture(baseUrl, cmd, t);
    }

    /**
     * Future-only gönderim (NetResult<T> döner)
     */
    protected <T> CancellableFuture<NetResult<T>> sendResult(ACommand cmd, Type t) {
        if (defaultRc != null) cmd.withRequestConfigurator(defaultRc);
        return networkManager.enqueueFutureResult(baseUrl, cmd, t);
    }

    /* ------------------- Future kısayolları ------------------- */

    protected <T> CancellableFuture<T> getF(String rel, Map<String, String> q, Map<String, String> h, Type t) {
        return send(new GetCommand(rel, toHash(q), toHash(h)), t);
    }

    protected <T> CancellableFuture<T> getF(String rel, Type t) {
        return getF(rel, null, null, t);
    }

    protected <T> CancellableFuture<T> deleteF(String rel, Map<String, String> q, Map<String, String> h, Type t) {
        return send(new DeleteCommand(rel, toHash(q), toHash(h)), t);
    }

    protected <T> CancellableFuture<T> postF(String rel, String body, Map<String, String> h, Type t) {
        return send(new PostCommand(rel, body, toHash(h)), t);
    }

    protected <T> CancellableFuture<T> putF(String rel, String body, Map<String, String> h, Type t) {
        return send(new PutCommand(rel, body, toHash(h)), t);
    }

    protected <T> CancellableFuture<T> patchF(String rel, String body, Map<String, String> h, Type t) {
        return send(new PatchCommand(rel, body, toHash(h)), t);
    }

    protected <T> CancellableFuture<T> uploadF(String rel,
                                               Map<String, String> fields,
                                               Map<String, File> files,
                                               Map<String, String> h,
                                               Type t) {
        return send(new MultipartCommand(rel, toHash(h), toHash(fields), toHash(files)), t);
    }

    /* ------------------- yardımcı ------------------- */
    private static <K, V> HashMap<K, V> toHash(Map<K, V> m) {
        return (m == null || m instanceof HashMap) ? (HashMap<K, V>) m : new HashMap<>(m);
    }
}
