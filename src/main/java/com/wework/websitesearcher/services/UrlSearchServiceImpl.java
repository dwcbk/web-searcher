package com.wework.websitesearcher.services;

import com.wework.websitesearcher.io.UrlReader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Single-threaded implementation of {@link UrlSearchService}
 */
public class UrlSearchServiceImpl implements UrlSearchService {
    private static final Logger LOG = LoggerFactory.getLogger(UrlSearchServiceImpl.class);

    @Override
    public List<String> searchUrlsForTerm(List<String> urls, String searchTerm) {
        List<String> results = new ArrayList<>();
        for (String url : urls) {
            String urlContents = UrlReader.getUrlContents(url);
            LOG.trace("read url: {} with content size: {}", url, StringUtils.length(urlContents));
        }
        return results;
    }
}
