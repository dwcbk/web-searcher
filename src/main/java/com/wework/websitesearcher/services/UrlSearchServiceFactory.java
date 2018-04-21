package com.wework.websitesearcher.services;

public class UrlSearchServiceFactory {

    public static UrlSearchService getInstance(int maxThreads) {
        if (maxThreads <= 0) {
            throw new IllegalArgumentException("Cannot have negative number of threads!");
        }

        if (maxThreads == 1) {
            return new UrlSearchServiceImpl();
        }
        else {
            return new ConcurrentUrlSearchService(maxThreads);
        }
    }
}
