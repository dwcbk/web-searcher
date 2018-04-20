package com.wework.websitesearcher.services;

import org.apache.commons.lang3.StringUtils;

public class ContentSearchService {

    private ContentSearchService() {}

    public static boolean stringContainsTerm(String content, String searchTerm) {
        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(searchTerm)) {
            return false;
        }
        return content.matches(searchTerm);
    }
}
