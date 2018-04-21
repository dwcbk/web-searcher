package com.wework.websitesearcher.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Single-threaded implementation of {@link UrlSearchService}
 *
 *  Maximum threads allowed is 50.
 */
class ConcurrentUrlSearchService implements UrlSearchService {
    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentUrlSearchService.class);

    private int maxThreads;

    /**
     * Initialize the service with the number of threads. Threads are not created until you actually call
     * {@link #searchUrlsForTerm(List, String)}. Maximum number of threads is 50.
     *
     * @param maxThreads number of threads to use. Must be (1 <= maxThreads <= 50)
     * @throws IllegalArgumentException if (maxThreads < 1) || (maxThreads > 50)
     */
    public ConcurrentUrlSearchService(int maxThreads) {
        if (maxThreads < 1 || maxThreads > 50) {
            throw new IllegalArgumentException("Max threads should be between 1-50 (inclusive). Max threads arg: " + maxThreads);
        }
        this.maxThreads = maxThreads;
    }

    @Override
    public List<String> searchUrlsForTerm(List<String> urls, String searchTerm) {
        if (urls == null || urls.isEmpty()) {
            throw new IllegalArgumentException("URLs can not be null or empty.");
        }

        // Initialize the thread-safe queue and thread-safe results list
        Queue<String> queue = getQueue();
        queue.addAll(urls);
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        // set cookie handler to accept all cookies.
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

        LOG.debug("STARTING [" + maxThreads + "] workers...");
        List<UrlSearchServiceWorker> workers = new ArrayList<>();
        // startup all the threads and run the search!
        for (int i=0; i < maxThreads; i++) {
            UrlSearchServiceWorker worker = new UrlSearchServiceWorker(queue, "worker_" + (i+1), results, searchTerm);
            workers.add(worker);
            worker.start();
        }

        LOG.debug("Done initializing threads.");

        // wait for all the threads to finish before returning anything to the caller
        for (UrlSearchServiceWorker worker : workers) {
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

    /**
     * Returns an empty concurrent Queue
     *
     * @return
     */
    private Queue<String> getQueue() {
        return new ConcurrentLinkedQueue<>();
    }


}
