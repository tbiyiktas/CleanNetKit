// lib/net/interceptor/LoggingInterceptor.java
package lib.net.interceptor;

import android.util.Log;

import lib.net.command.ACommand;
import lib.net.connection.IHttpConnection;

public class LoggingInterceptor implements Interceptor {
    @Override
    public void onRequest(IHttpConnection c, ACommand cmd) {
        Log.d("NET", "→ " + cmd.getMethodName() + " " + cmd.getRelativeUrl());
    }

    @Override
    public void onResponseHeaders(IHttpConnection c, ACommand cmd, int s) {
        Log.d("NET", "← status " + s);
    }
}
