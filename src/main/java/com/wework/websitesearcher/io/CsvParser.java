package com.wework.websitesearcher.io;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Parses the contents of a CSV file and returns the list of URLs in the 2nd column. Assumes the file is formatted like:
 * <pre>
 * "Rank","URL","Linking Root Domains","External Links","mozRank","mozTrust"
 * 1,"facebook.com/",9616487,1688316928,9.54,9.34
 * </pre>
 *
 * If the URLs don't start with "http://" then that prefix is added to each URL.
 */
class CsvParser {
    private static final Logger LOG = LoggerFactory.getLogger(CsvParser.class);

    List<String> parseAndReturnUrls(String csvContents) {
        if (StringUtils.isEmpty(csvContents)) {
            LOG.warn("CSV Contents are empty.");
            return Collections.emptyList();
        }

        List<CsvRow> allRows = Arrays.stream(csvContents.split("\\n"))
                .skip(1)
                .map(line -> line.split(","))
                .map(cols -> new CsvRow(Integer.parseInt(cols[0]),
                                        cols[1],
                                        Integer.parseInt(cols[2]),
                                        Integer.parseInt(cols[3]),
                                        Double.parseDouble(cols[4]),
                                        Double.parseDouble(cols[5])))
                .collect(Collectors.toList());

        return allRows.stream()
                .map(CsvRow::getUrl)
                .filter(StringUtils::isNotEmpty)
                .map(url -> url.replace("\"",""))
                .map(url -> !url.startsWith("http") ? "http://" + url : url)
                .collect(Collectors.toList());
    }

    /**
     * Represents the contents of one CSV row. All we're interested in is the 'url' value.
     */
    private class CsvRow {
        private final Integer rank;
        private final String url;
        private final Integer linkingRootDomains;
        private final Integer externalLinks;
        private final Double mozRank;
        private final Double mozTrust;

        public CsvRow(Integer rank, String url, Integer linkingRootDomains, Integer externalLinks, Double mozRank, Double mozTrust) {
            this.rank = rank;
            this.url = url;
            this.linkingRootDomains = linkingRootDomains;
            this.externalLinks = externalLinks;
            this.mozRank = mozRank;
            this.mozTrust = mozTrust;
        }

        public Integer getRank() {
            return rank;
        }

        public String getUrl() {
            return url;
        }

        public Integer getLinkingRootDomains() {
            return linkingRootDomains;
        }

        public Integer getExternalLinks() {
            return externalLinks;
        }

        public Double getMozRank() {
            return mozRank;
        }

        public Double getMozTrust() {
            return mozTrust;
        }

        @Override
        public String toString() {
            return "CsvRow{" +
                    "rank=" + rank +
                    ", url='" + url + '\'' +
                    ", linkingRootDomains=" + linkingRootDomains +
                    ", externalLinks=" + externalLinks +
                    ", mozRank=" + mozRank +
                    ", mozTrust=" + mozTrust +
                    '}';
        }
    }
}
