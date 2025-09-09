package com.example.cleannetkit;

import android.app.Application;

import java.util.Arrays;

import lib.net.NetworkManager;
import lib.net.connection.OkHttpConnectionFactory;
import lib.net.parser.GsonResponseParser;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

public class MyApplication extends Application {

    private static MyApplication instance = null;
    private NetworkManager networkManager = null;

    @Override
    public synchronized void onCreate() {
        super.onCreate();

        OkHttpClient ok =
                new OkHttpClient.Builder()
                        .connectTimeout(java.time.Duration.ofSeconds(10))
                        .readTimeout(java.time.Duration.ofSeconds(20))
                        .callTimeout(java.time.Duration.ofSeconds(30))
                        .retryOnConnectionFailure(true)
                        .followRedirects(true)
                        .connectionSpecs(Arrays.asList(
                                new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                                        .tlsVersions(TlsVersion.TLS_1_3, TlsVersion.TLS_1_2)
                                        //.allEnabledCipherSuites() // kurum gereksinimine göre daraltılabilir
                                        .build(),
                                ConnectionSpec.CLEARTEXT // gerekliyse
                        ))
                        .build();

        networkManager = new NetworkManager.Builder()
                .factory(new OkHttpConnectionFactory(ok))
                .parser(new GsonResponseParser())
                .addInterceptor(new lib.net.interceptor.LoggingInterceptor())
                .threadPoolSize(4)
                .queueCapacity(8)
                .build();

        instance = this;
    }

    public synchronized static NetworkManager getNetworkManager() {
        if (instance == null) {
            throw new IllegalStateException("Uygulama henüz başlatılmadı.");
        }
        return instance.networkManager;
    }
}