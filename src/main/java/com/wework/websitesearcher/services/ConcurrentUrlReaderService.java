package com.wework.websitesearcher.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConcurrentUrlReaderService {
    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentUrlReaderService.class);

    private int maxThreads;

    public ConcurrentUrlReaderService(int maxThreads) {
        if (maxThreads < 1 || maxThreads > 50) {
            throw new IllegalArgumentException("Max threads should be between 1-50 (inclusive). Max threads arg: " + maxThreads);
        }
        this.maxThreads = maxThreads;
    }

    public List<String> getUrlContent(List<String> urls, String searchTerm) {
        if (urls == null || urls.isEmpty()) {
            throw new IllegalArgumentException("URLs can not be null or empty.");
        }

        Queue<String> queue = getQueue();
        queue.addAll(urls);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        // set cookie handler to accept all cookies.
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

        LOG.debug("STARTING [" + maxThreads + "] workers...");
        List<UrlReaderWorker> workers = new ArrayList<>();
        for (int i=0; i < maxThreads; i++) {
            UrlReaderWorker worker = new UrlReaderWorker(queue, "worker_" + (i+1), results, searchTerm);
            workers.add(worker);
            worker.start();
        }

        LOG.debug("Done initializing threads.");

        for (UrlReaderWorker worker : workers) {
            LOG.debug("Waiting for [" + worker.getName() + "] to complete");
            if (worker.isAlive()) {
                try {
                    worker.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Interrupted waiting for thread [" + worker.getName() + "] to complete");
                }
            }
        }

        return results;
    }

    private Queue<String> getQueue() {
        return new ConcurrentLinkedQueue<>();
    }


}
