package com.swiftly.mapper;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.enterprise.context.ApplicationScoped;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@ApplicationScoped
public class HamsterService {
    final List<Hamster> hamsters = new LinkedList<Hamster>();

    private final Timer getSelfTimer;
    private final Counter failedGetSelf;
    private final Counter successfulGetSelf;
    private final Counter failedLogin;
    private final Counter successfulLogin;
    private final ExecutorService executorService;
    private final ObjectMapper mapper;

    HamsterService(MeterRegistry registry, ObjectMapper mapper) {
        this.mapper = mapper;
        this.failedGetSelf = registry.counter("get-self.failed");
        this.successfulGetSelf = registry.counter("get-self.successful");
        this.failedLogin = registry.counter("login.failed");
        this.successfulLogin = registry.counter("login.successful");
        Gauge.builder("hamsters.alive", this.hamsters, List::size).register(registry);
        this.getSelfTimer = Timer.builder("swiftly.get.self.timer")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                .publishPercentileHistogram(true).register(registry);
        this.executorService = Executors.newCachedThreadPool();
    }

    public void spawnHamsters(int count) {
        for (int i = 0; i < count; i++) {
            final Hamster hamster = Hamster.buildHamster(executorService, mapper,
                    getSelfTimer, failedGetSelf, successfulGetSelf,
                    failedLogin, successfulLogin);
            final Thread t = new Thread(hamster);
            hamster.setRunning(true);
            t.start();
            hamsters.add(hamster);
        }
    }

    public void killHamsters(int count) {
        int killedHamsters = 0;
        while (this.hamsters.size() > 0 && killedHamsters < count) {
            final Hamster hamster = this.hamsters.get(0);
            hamster.setRunning(false);
            this.hamsters.remove(hamster);
            killedHamsters++;
        }
    }
}
