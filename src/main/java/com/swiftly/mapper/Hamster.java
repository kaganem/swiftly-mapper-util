package com.swiftly.mapper;

import java.util.concurrent.ScheduledFuture;

import javax.ws.rs.QueryParam;

import org.jboss.resteasy.client.exception.ResteasyWebApplicationException;

import com.swiftly.mapper.SwiftlySessionService.LoginResult;
import com.swiftly.mapper.SwiftlyUserService.Profile;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;

public class Hamster implements Runnable {

    private final SwiftlyUserService userService;
    private final SwiftlySessionService sessionService;
    private final Timer getSelfTimer;
    private final Counter failedGetSelf;
    private final Counter successfulGetSelf;
    private String session;
    private ScheduledFuture<?> hamsterSchedule;

    public static Hamster buildHamster(
            SwiftlyUserService userService,
            SwiftlySessionService sessionService,
            Timer getSelfTimer,
            Counter failedGetSelf, Counter successfulGetSelf,
            Counter failedLogin, Counter successfulLogin) {

        final Hamster hamster = new Hamster(userService, sessionService,
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

    public Hamster(SwiftlyUserService userService, SwiftlySessionService sessionService,
            Timer getSelfTimer,
            Counter failedGetSelf, Counter successfulGetSelf) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.getSelfTimer = getSelfTimer;
        this.failedGetSelf = failedGetSelf;
        this.successfulGetSelf = successfulGetSelf;
    }

    private LoginResult login() {
        try {
            return sessionService.login("ekagan@gehtsoft.com",
                    "LpZ3IjJIf7OgWNWPLDEAI+B+QBfJTVbMX65LVLRGBfQqdbCx81iZH4xsvptotk5bKgnQrSP8rAfumpGYp0Xh1Q==");
        } catch (ResteasyWebApplicationException exception) {
            if (exception.getResponse().getStatus() == 429) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
            return null;
        }
    }

    private boolean whoAmI(String session) {
        try {
            return this.getSelfTimer.record(() -> {
                userService.getSelf(session);
                return true;
            });
        } catch (Exception e) {
            return false;
        }
    }

    public void setSchedule(ScheduledFuture<?> hamsterSchedule2) {
        this.hamsterSchedule = hamsterSchedule2;
    }

    public void die() {
        if (this.hamsterSchedule != null) {
            this.hamsterSchedule.cancel(false);
        }
    }

    @Override
    public void run() {
        if (this.whoAmI(this.session)) {
            this.successfulGetSelf.increment();
        } else {
            this.failedGetSelf.increment();
        }
    }

}
