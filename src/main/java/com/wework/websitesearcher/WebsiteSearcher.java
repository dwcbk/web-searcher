package com.wework.websitesearcher;

import com.wework.websitesearcher.io.UrlReader;
import com.wework.websitesearcher.services.ConcurrentUrlReaderService;
import com.wework.websitesearcher.util.Stopwatch;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main class that is run when executing this JAR.
 */
public class WebsiteSearcher {
    private static final Logger LOG = LoggerFactory.getLogger(WebsiteSearcher.class);

    private static final int DEFAULT_MAX_THREADS = 20;
    private static final int DEFAULT_MAX_URLS = 9999;
    private static final String DEFAULT_SEARCH_TERM = "(?s).*(facebook|twitter).*";
    private static final String DEFAULT_URLS_LOCATION = "https://s3.amazonaws.com/fieldlens-public/urls.txt";

    private final String urlsLocation;
    private final int maxThreads;
    private final int maxUrls;
    private final String searchTerm;
    private final Stopwatch stopwatch;

    public static void main(String[] args) {
        System.out.println("Welcome to Website Searcher. Args: " + Arrays.asList(args));


        // get max number of threads to use or use the default value if user didn't specify
        int maxThreads = args.length >= 1 ? getIntArg(args[0], DEFAULT_MAX_THREADS) : DEFAULT_MAX_THREADS;
        // make sure user doesn't run with too many threads
        if (maxThreads > DEFAULT_MAX_THREADS) {
            maxThreads = DEFAULT_MAX_THREADS;
        }
        // get max number of URLs to search or use the default value if user didn't specify
        int maxUrls = args.length >= 2 ? getIntArg(args[1], DEFAULT_MAX_URLS) : DEFAULT_MAX_URLS;
        // get search term or use the default value if user didn't specify
        String searchTerm = args.length == 3 ? args[2] : DEFAULT_SEARCH_TERM;
        System.out.println("Running with settings: max threads: " + maxThreads +
                ", max URLs to search: " + maxUrls +
                ", search term: " + searchTerm);
        LOG.info("Running with settings: max threads: {}, max URLs to search: {}, urls location: {}" +
                        ", search term: {}",
                maxThreads, maxUrls, DEFAULT_URLS_LOCATION, searchTerm);

        // initialize and run the tool
        WebsiteSearcher websiteSearcher = new WebsiteSearcher(DEFAULT_URLS_LOCATION, maxThreads, maxUrls, searchTerm);
        websiteSearcher.run();
    }

    /**
     * Convert String to Integer or return the defaultValue if the value can't be converted to an Integer
     *
     * @param val
     * @param defaultVal
     * @return
     */
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
        // get the list of URLs from the CSV file located at 'urlsLocation'
        List<String> urls = UrlReader.getUrlsFromCsvUrl(urlsLocation);
        if (urls == null || urls.isEmpty()) {
            // if there aren't any URLs, then exit the program right away.
            System.out.println("Couldn't load the list of URLs or the list was empty.");
            LOG.warn("Couldn't load the list of URLs or the list was empty.");
            System.exit(0);
        }
        else if (urls.size() > maxUrls) {
            // truncate the list of URLs if the MAX urls to search is less than the total # of urls in the list
            urls = urls.subList(0, maxUrls);
        }
        // set cookie handler global property so that more URLs will return 200 responses.
        UrlReader.enableCookies();

        LOG.info("Loaded list of URLs to search ({} urls)", urls.size());
        List<String> results = null;
        // run the search of all the URLs in single or multi threaded mode
        if (maxThreads == 1) {
            results = runSingleThreaded(urls);
        }
        else {
            results = runMultiThreaded(urls);
        }

        // now that we have the results, write hte output to file "results.txt"
        String outputContents = "URLs containing the search term '" + searchTerm + "'\n" + StringUtils.join(results, "\n");
        File outputFilename = new File("results.txt");
        try {
            FileUtils.writeStringToFile(outputFilename, outputContents, "UTF-8");
        } catch (IOException e) {
            LOG.error("Error writing output to file " + outputFilename);
            throw new RuntimeException("Error writing output to file " + outputFilename, e);
        }

        stopwatch.stop();
        long elapsed = stopwatch.getElapsedTime();
        LOG.info("Website Search is complete. Found {} results from {} urls in {}", results.size(), urls.size(), Stopwatch.toHuman(elapsed));
        System.out.println(String.format("Website Search is complete. Found %s results from %s urls time %s", results.size(), urls.size(), Stopwatch.toHuman(elapsed)));
    }

    private List<String> runMultiThreaded(List<String> urls) {
        ConcurrentUrlReaderService urlReaderService = new ConcurrentUrlReaderService(DEFAULT_MAX_THREADS);
        return urlReaderService.getUrlContent(urls, searchTerm);
    }

    private List<String> runSingleThreaded(List<String> urls) {
        List<String> results = new ArrayList<>();
        for (String url : urls) {
            String urlContents = UrlReader.getUrlContents(url);
            LOG.trace("read url: {} with content size: {}", url, StringUtils.length(urlContents));
        }
        return results;
    }


}
