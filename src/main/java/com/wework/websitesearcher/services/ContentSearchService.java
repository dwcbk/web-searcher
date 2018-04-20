package com.wework.websitesearcher.services;

import org.apache.commons.lang3.StringUtils;

/**
 * Service with a helper method for matching a regex against a String.
 */
public class ContentSearchService {

    private ContentSearchService() {}

    /**
     * Returns true if the content matches the regex. Returns false if the regex doesn't match or the content or regex
     * arguments are null or empty. If the content is multi-line, your regex must be set to handle that (e.g. add '(?s)'
     * to the beginning of the regex. For example the regex <code>(?s).*(facebook|twitter).*</code> returns true if the
     * multi-line content contains the words 'facebook' or 'twitter' anywhere.
     *
     * @param content
     * @param regex
     * @return
     */
    public static boolean contentsMatchRegex(String content, String regex) {
        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(regex)) {
            return false;
        }
        return content.matches(regex);
    }
}
