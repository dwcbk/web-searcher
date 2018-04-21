package com.wework.websitesearcher.services;

import com.wework.websitesearcher.io.UrlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Queue;

/**
 * Package-private thread that loads the content from a list of URLs and searches their content for the given search
 * term regex.
 */
class UrlSearchServiceWorker extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(UrlSearchServiceWorker.class);

    private final Queue<String> urlQueue;
    private final List<String> results;
    private final String searchTerm;

    /**
     * Constructor.
     *
     * @param urlQueue Queue containing of URLs to search (assumes the Queue object is thread-safe)
     * @param name Name of the thread (for debugging purposes mostly)
     * @param results Results list. If a URL's contents match the regex, they will be added to this list. (assumes the
     *                List object is thread-safe)
     * @param searchTerm regex search
     */
    UrlSearchServiceWorker(Queue<String> urlQueue, String name, List<String> results, String searchTerm) {
        super(name);
        this.urlQueue = urlQueue;
        this.results = results;
        this.searchTerm = searchTerm;
    }

    /**
     * Override the {@link Thread#run()} method so we can process the URLs queue. For each URL, load its contents and
     * search in the contents for a search term (regex).
     */
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
                if (UrlSearchService.contentsMatchRegex(urlContents, searchTerm)) {
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

    /**
     * Get the contents of a URL.
     *
     * @param url
     * @return the contents of the URL as a String or null if there was an IOException.
     */
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
