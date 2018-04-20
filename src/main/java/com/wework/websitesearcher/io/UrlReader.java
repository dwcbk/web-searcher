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

public class UrlReader {
    private static final Logger LOG = LoggerFactory.getLogger(UrlReader.class);

    public static List<String> getUrlsFromCsvUrl(String urlVal) {
        if (StringUtils.isEmpty(urlVal)) {
            throw new IllegalArgumentException("URL string can not be null.");
        }

        String urlContents = getUrlContents(urlVal);
        CsvParser csvParser = new CsvParser();
        return csvParser.parseAndReturnUrls(urlContents);
    }

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

    public static String getUrlContentsWithTimeout(String url, int timeoutSecs) throws IOException {
        LOG.debug("Reading url: " + url);

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
        String result = IOUtils.toString(huc.getInputStream(), "UTF-8");
//        if (result.length() < 1000) {
//            System.out.println( "response for " + url + ":\n\t\t" + result);
//        }
        return result;
    }

    private static URL toUrl(String urlVal) {
        try {
            return new URL( urlVal );
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error parsing URL string: " + urlVal, e);
        }
    }

    public static void enableCookies() {
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    }
}
