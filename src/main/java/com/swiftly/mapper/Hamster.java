package com.swiftly.mapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftly.mapper.SwiftlySessionService.LoginResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Hamster implements Runnable {

    private final Timer getSelfTimer;
    private final Counter failedGetSelf;
    private final Counter successfulGetSelf;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ObjectMapper mapper;
    private final HttpClient httpClient;
    private String session;

    static SSLContext insecureContext() {
        TrustManager[] noopTrustManager = new TrustManager[] {
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] xcs, String string) {
                    }

                    public void checkServerTrusted(X509Certificate[] xcs, String string) {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };
        try {
            SSLContext sc = SSLContext.getInstance("ssl");
            sc.init(null, noopTrustManager, null);
            return sc;
        } catch (KeyManagementException | NoSuchAlgorithmException ex) {
        }
        return null;
    }

    public static Hamster buildHamster(
            ExecutorService executorService,
            ObjectMapper mapper,
            Timer getSelfTimer,
            Counter failedGetSelf, Counter successfulGetSelf,
            Counter failedLogin, Counter successfulLogin) {

        final Hamster hamster = new Hamster(executorService, mapper,
                getSelfTimer,
                failedGetSelf, successfulGetSelf);

        LoginResult loginResult = null;

        while (loginResult == null) {
            loginResult = hamster.login();
            if (loginResult != null && loginResult.successful) {
                hamster.session = loginResult.session;
                successfulLogin.increment();
            } else {
                loginResult = null;
                failedLogin.increment();
            }
        }

        return hamster;

    }

    public Hamster(ExecutorService executorService,
            ObjectMapper mapper,
            Timer getSelfTimer,
            Counter failedGetSelf, Counter successfulGetSelf) {

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .sslContext(insecureContext())
                .build();
        this.mapper = mapper;
        this.getSelfTimer = getSelfTimer;
        this.failedGetSelf = failedGetSelf;
        this.successfulGetSelf = successfulGetSelf;
    }

    private LoginResult login() {
        try {
            final String URL = "https://dev-api.swiftly.bz/api/session/login?user=ekagan%40gehtsoft.com&hash=LpZ3IjJIf7OgWNWPLDEAI%2BB%2BQBfJTVbMX65LVLRGBfQqdbCx81iZH4xsvptotk5bKgnQrSP8rAfumpGYp0Xh1Q%3D%3D";
            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder().GET()
                            .uri(new URI(URL))
                            .build(),
                    BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return this.mapper.readValue(response.body(), LoginResult.class);
            } else {
                return null;
            }
        } catch (URISyntaxException e) {
            System.err.println(e.toString());
        } catch (IOException e) {
            System.err.println(e.toString());
        } catch (InterruptedException e) {
            System.err.println(e.toString());
        }
        return null;
    }

    private boolean whoAmI(String session) {
        try {
            return this.getSelfTimer.record(() -> {
                try {
                    final String URL = "https://dev-api.swiftly.bz/api/user/get-self";
                    HttpResponse<String> response = httpClient.send(
                            HttpRequest.newBuilder().GET()
                                    .uri(new URI(URL))
                                    .header("x-swiftly-session-id", this.session)
                                    .build(),
                            BodyHandlers.ofString());
                    return response.statusCode() == 200;
                } catch (URISyntaxException e) {
                    System.err.println(e.toString());
                } catch (IOException e) {
                    System.err.println(e.toString());
                } catch (InterruptedException e) {
                    System.err.println(e.toString());
                }
                return false;
            });
        } catch (Exception e) {
            return false;
        }
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }

    @Override
    public void run() {
        long cycleStart = 0;
        while (this.running.get()) {
            cycleStart = System.currentTimeMillis();
            if (this.whoAmI(this.session)) {
                this.successfulGetSelf.increment();
            } else {
                this.failedGetSelf.increment();
            }
            long took = System.currentTimeMillis() - cycleStart;
            if (took < 1000) {
                try {
                    Thread.sleep(1000 - took);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
