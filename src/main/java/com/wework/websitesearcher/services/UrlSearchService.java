package com.wework.websitesearcher.services;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Service for loading the content from a list of URLs and searching that content for the given search term regex.
 */
public interface UrlSearchService {

    /**
     * Run this service for the given list of URLs and return the URLs whose content mathces the regex search term.
     *
     * @param urls list of URLs whose content you wish to search
     * @param searchTerm regex to search for in the contents of each URL.
     * @return the list of URLs whose contents matches the given regex, or an empty list of none found
     */
    List<String> searchUrlsForTerm(List<String> urls, String searchTerm);

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
    static boolean contentsMatchRegex(String content, String regex) {
        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(regex)) {
            return false;
        }
        return content.matches(regex);
    }

}
