package com.wework.websitesearcher.io;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * This class reads a URL and returns the body contents as a String.
 */
public class UrlReader {
    private static final Logger LOG = LoggerFactory.getLogger(UrlReader.class);

    private UrlReader() {}

    /**
     * Loads the contents of a CSV file from the specified URL and returns the list of URLs found in the URL column.
     * See {@link CsvParser} for more details.
     *
     * @param urlVal fully-qualified URL containing the CSV file
     * @param max max number of URLs to return
     * @return the list of URLs or an empty list (never null)
     * @see CsvParser
     * @throws IllegalArgumentException if urlVal is null or empty
     */
    public static List<String> getUrlsFromCsvUrl(String urlVal, int max) {
        if (StringUtils.isEmpty(urlVal)) {
            throw new IllegalArgumentException("URL string can not be null.");
        }

        String urlContents = getUrlContents(urlVal);
        CsvParser csvParser = new CsvParser();
        List<String> results = csvParser.parseAndReturnUrls(urlContents);
        if (results == null || results.isEmpty()) {
            // if there aren't any URLs, then exit the program right away.
            System.out.println("Couldn't load the list of URLs or the list was empty.");
            LOG.warn("Couldn't load the list of URLs or the list was empty.");
            System.exit(0);
        }
        else if (results.size() > max) {
            // truncate the list of URLs if the MAX urls to search is less than the total # of urls in the list
            results = results.subList(0, max);
        }
        return results;
    }

    /**
     * Return the body/contents of a URL. If the URL could not be loaded (due to timeout or a 400/500 error), then null
     * is returned. To specify a specific timeout, use {@link #getUrlContentsWithTimeout(String, int)}.
     *
     * @param url
     * @return
     */
    public static String getUrlContents(String url) {
        InputStream inputStream = null;
        try {
            inputStream = toUrl(url).openStream();
        } catch (IOException e) {
            throw new RuntimeException("Error opening stream for URL: " + url, e);
        }

        try {
            return IOUtils.toString( inputStream, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException("Error reading URL: " + url, e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Return the body/contents of a URL. If the URL could not be loaded (due to timeout or a 400/500 error), then null
     * is returned.
     *
     * @param url
     * @param timeoutSecs
     * @return
     * @throws IOException
     */
    public static String getUrlContentsWithTimeout(String url, int timeoutSecs) throws IOException {
        LOG.debug("Reading url: " + url);

        // Create the request object
        HttpURLConnection huc = (HttpURLConnection) toUrl(url).openConnection();
        HttpURLConnection.setFollowRedirects(true);
        huc.setConnectTimeout(timeoutSecs * 1000);
        huc.setReadTimeout(timeoutSecs * 1000);
        huc.setRequestMethod("GET");
        huc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.1 Safari/605.1.15");
        try {
            huc.connect();
        } catch (SocketTimeoutException e) {
            LOG.trace("URL: " + url + " TIMEOUT");
            return null;
        } catch (UnknownHostException e) {
            LOG.warn("Unknown host: " + url);
            return null;
        } catch (SSLHandshakeException e) {
            LOG.warn("SSLHandshakeException for url: " + url);
            return null;
        }
        // get the HTTP response code. If the response is a redirect (HTTP status 300), then recursively call
        // this method to follow the redirect
        int responseCode = huc.getResponseCode();
        if (responseCode != 200) {
            LOG.debug("URL: " + url + " returned status code " + responseCode);
            boolean redirect = false;

            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                    || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                    || responseCode == HttpURLConnection.HTTP_SEE_OTHER) {
                redirect = true;
            }
            if (redirect) {

                String location = huc.getHeaderField("Location");
                LOG.debug("Redirecting from " + url + " to " + location);
                return getUrlContentsWithTimeout(location, timeoutSecs);
            }
        }
        return IOUtils.toString(huc.getInputStream(), "UTF-8");
    }

    /**
     * Returns a URL string as a {@link URL} object
     *
     * @param urlVal
     * @return
     */
    private static URL toUrl(String urlVal) {
        try {
            return new URL( urlVal );
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error parsing URL string: " + urlVal, e);
        }
    }

    /**
     * Helper method for globally setting the cookie policy (so more URLs will return 200 responses vs not
     * accepting cookies)
     */
    public static void enableCookies() {
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    }
}
