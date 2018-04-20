package com.wework.websitesearcher;

import com.wework.websitesearcher.services.ContentSearchService;
import org.junit.Test;

public class ContentSearchTest {

    @Test
    public void testRegexSearch() {
//        String content = "Hello World, foo Bar.";
        String content = "Hello World Foo\nFoo Bar.";
        test(content, "foo");
        test(content, ".*Foo.*");
        test(content, "(?s).*Foo.*");
        test(content, "(?s).*Bar.*");
        test(content, "(?s).*(facebook|twitter).*");
        test(content, ".*(facebook|twitter).*");
    }

    private void test(String content, String regex) {
        System.out.println("Content contains regex '" + regex + "': " + ContentSearchService.contentsMatchRegex(content, regex));
    }
}
