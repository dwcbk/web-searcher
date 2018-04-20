package com.wework.websitesearcher;

import com.wework.websitesearcher.io.UrlReader;
import org.junit.Test;

import java.io.IOException;

public class UrlReaderTest {

//    @Test
    public void testUrl() {
        UrlReader.enableCookies();
        runUrlTest("https://flickr.com");
        runUrlTest("https://qq.com");
        runUrlTest("https://blog.com");
    }

    private void runUrlTest(String url) {
        try {
            String urlContents = UrlReader.getUrlContentsWithTimeout(url, 10);
            if (urlContents != null) {
                System.out.println("content length for url: " + url + " is " + urlContents.length());
            }
            else {
                System.out.println("URL: " + url + " timed out or returned no content.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading url: " + url, e);
        }
    }

}
