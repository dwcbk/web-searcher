package com.wework.websitesearcher.services;

import com.wework.websitesearcher.io.UrlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Queue;

public class UrlReaderWorker extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(UrlReaderWorker.class);

    private final Queue<String> urlQueue;
    private final List<String> results;
    private final String searchTerm;

    public UrlReaderWorker(Queue<String> urlQueue, String name, List<String> results, String searchTerm) {
        super(name);
        this.urlQueue = urlQueue;
        this.results = results;
        this.searchTerm = searchTerm;
    }

    @Override
    public void run() {
        LOG.debug("BEGIN Thread: {}", getName());
        while(!urlQueue.isEmpty()) {
            String url = urlQueue.poll();
            if (url == null) {
                return;
            }
            String urlContents = getUrlContents(url);
            if (urlContents != null) {
                LOG.debug("content length for url: {} is {}", url, urlContents.length());
                if (ContentSearchService.stringContainsTerm(urlContents, searchTerm)) {
                    LOG.debug("URL {} DID contain search term {}", url, searchTerm);
                    results.add(url);
                }
                else {
                    LOG.debug("URL {} did NOT contain search term {}", url, searchTerm);
                }
            }
            else {
                LOG.debug("url: {} timed out or contained no content", url);
            }
        }
        LOG.debug("Closing");
    }

    private String getUrlContents(String url) {
        try {
            return UrlReader.getUrlContentsWithTimeout(url, 10);
        } catch (SocketTimeoutException e) {
            return null;
        } catch (IOException e) {
            LOG.error("Error reading url: {}, message: {}", url, e);
            return null;
        }
    }
}
