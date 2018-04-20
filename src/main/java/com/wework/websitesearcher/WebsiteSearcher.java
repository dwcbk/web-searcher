package com.wework.websitesearcher;

import com.wework.websitesearcher.io.UrlReader;
import com.wework.websitesearcher.services.ConcurrentUrlReaderService;
import com.wework.websitesearcher.util.Stopwatch;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class WebsiteSearcher {
    private static final Logger LOG = LoggerFactory.getLogger(WebsiteSearcher.class);
    private static final int DEFAULT_MAX_THREADS = 20;
    private static final int DEFAULT_MAX_URLS = 9999;
    private static final String DEFAULT_SEARCH_TERM = "facebook";

    private final String urlsLocation;
    private final int maxThreads;
    private final int maxUrls;
    private final String searchTerm;
    private final Stopwatch stopwatch;

    public static void main(String[] args) {
        System.out.println("Welcome to Website Searcher. Args: " + Arrays.asList(args));
        String urlsLocation = "https://s3.amazonaws.com/fieldlens-public/urls.txt";

        // get max number of threads to use or use the default value if user didn't specify
        int maxThreads = args.length >= 1 ? getIntArg(args[0], DEFAULT_MAX_THREADS) : DEFAULT_MAX_THREADS;
        // make sure user doesn't run with too many threads
        if (maxThreads > DEFAULT_MAX_THREADS) {
            maxThreads = DEFAULT_MAX_THREADS;
        }
        // get max number of URLs to search or use the default value if user didn't specify
        int maxUrls = args.length == 2 ? getIntArg(args[1], DEFAULT_MAX_URLS) : DEFAULT_MAX_URLS;
        // get search term or use the default value if user didn't specify
        String searchTerm = args.length == 3 ? args[2] : DEFAULT_SEARCH_TERM;
        System.out.println("Running with settings: max threads: " + maxThreads +
                ", max URLs to search: " + maxUrls +
                ", search term: " + searchTerm);
        LOG.info("Running with settings: max threads: {}, max URLs to search: {}, urls location: {}" +
                        ", search term: {}",
                maxThreads, maxUrls, urlsLocation, searchTerm);

        // initialize and run the tool
        WebsiteSearcher websiteSearcher = new WebsiteSearcher(urlsLocation, maxThreads, maxUrls, searchTerm);
        websiteSearcher.run();
    }

    private static int getIntArg(String val, int defaultVal) {
        if (val == null || val.isEmpty()) {
            return defaultVal;
        }
        else {
            try {
                return Integer.valueOf(val);
            } catch (NumberFormatException e) {
                LOG.warn("Error parsing arg '" + val + "' as a number. Using default value (" + defaultVal + ").");
                return defaultVal;
            }
        }
    }

    public WebsiteSearcher(String urlsLocation, int maxThreads, int maxUrls, String searchTerm) {
        this.urlsLocation = urlsLocation;
        this.maxThreads = maxThreads;
        this.maxUrls = maxUrls;
        this.searchTerm = searchTerm;
        this.stopwatch = new Stopwatch();
    }

    private void run() {
        stopwatch.start();
        List<String> urls = UrlReader.getUrlsFromCsvUrl(urlsLocation);
        if (urls == null || urls.isEmpty()) {
            System.out.println("Couldn't load the list of URLs or the list was empty.");
            LOG.warn("Couldn't load the list of URLs or the list was empty.");
            System.exit(0);
        }
        else if (urls.size() > maxUrls) {
            urls = urls.subList(0, maxUrls);
        }
        UrlReader.enableCookies();

        LOG.info("Loaded list of URLs to search ({} urls)", urls.size());
        if (maxThreads == 1) {
            runSingleThreaded(urls);
        }
        else {
            runMultiThreaded(urls);
        }
        stopwatch.stop();
        long elapsed = stopwatch.getElapsedTime();
        long avg = elapsed / urls.size();
        LOG.info("Elapsed time ({} urls): {}", urls.size(), Stopwatch.toHuman(elapsed));
        LOG.info("Average time per URL ({} urls): {}", urls.size(), Stopwatch.toHuman(avg));
    }

    private void runMultiThreaded(List<String> urls) {
        ConcurrentUrlReaderService urlReaderService = new ConcurrentUrlReaderService(DEFAULT_MAX_THREADS);
        urlReaderService.getUrlContent(urls);
    }

    private void runSingleThreaded(List<String> urls) {
        for (String url : urls) {
            String urlContents = UrlReader.getUrlContents(url);
            LOG.trace("read url: {} with content size: {}", url, StringUtils.length(urlContents));
        }
    }


}
