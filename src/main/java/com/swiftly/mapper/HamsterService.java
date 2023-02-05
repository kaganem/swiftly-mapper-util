package com.swiftly.mapper;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@ApplicationScoped
public class HamsterService {
    final List<Hamster> hamsters = new LinkedList<Hamster>();

    @Inject
    @RestClient
    SwiftlySessionService sessionService;

    @Inject
    @RestClient
    SwiftlyUserService userService;

    private final Timer getSelfTimer;
    private final Counter failedGetSelf;
    private final Counter successfulGetSelf;
    private final Counter failedLogin;
    private final Counter successfulLogin;
    private final ScheduledExecutorService hamsterExecutor;

    HamsterService(MeterRegistry registry) {
        this.failedGetSelf = registry.counter("get-self.failed");
        this.successfulGetSelf = registry.counter("get-self.successful");
        this.failedLogin = registry.counter("login.failed");
        this.successfulLogin = registry.counter("login.successful");
        Gauge.builder("hamsters.alive", this.hamsters, List::size).register(registry);
        this.getSelfTimer = Timer.builder("swiftly.get.self.timer")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                .publishPercentileHistogram(true).register(registry);
        this.hamsterExecutor = Executors.newScheduledThreadPool(32);
    }

    public void spawnHamsters(int count, int interval) {
        for (int i = 0; i < count; i++) {
            final Hamster hamster = Hamster.buildHamster(userService, sessionService,
                    getSelfTimer, failedGetSelf, successfulGetSelf,
                    failedLogin, successfulLogin);

            final ScheduledFuture<?> hamsterSchedule = this.hamsterExecutor
                    .scheduleAtFixedRate(hamster, 0, interval, TimeUnit.MILLISECONDS);

            hamster.setSchedule(hamsterSchedule);
            hamsters.add(hamster);
        }
    }

    public void killHamsters(int count) {
        int killedHamsters = 0;
        while (this.hamsters.size() > 0 && killedHamsters < count) {
            final Hamster hamster = this.hamsters.get(0);
            hamster.die();
            this.hamsters.remove(hamster);
            killedHamsters++;
        }
    }
}
