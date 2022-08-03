package com.mrgrd56.illusioncardsdownloader.service;

import com.mrgrd56.illusioncardsdownloader.model.IllusionCardsSearchPage;
import com.mrgrd56.illusioncardsdownloader.model.NamedFile;
import com.mrgrd56.illusioncardsdownloader.util.HttpEntityBuilder;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

@Service
public class CardsDownloadingService {
    private static final Map<String, String> HTTP_HEADERS = Map.ofEntries(
            Map.entry("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36")
    );

    private static final HttpEntity<Void> HTTP_ENTITY = new HttpEntityBuilder()
            .headers(HTTP_HEADERS)
            .build();

    private static final int ITEMS_PER_PAGE = 20;

    private final ArchivationService archivationService;

    public CardsDownloadingService(ArchivationService archivationService) {
        this.archivationService = archivationService;
    }

    public byte[] downloadIllusionCards(String search, Integer pageLimit) {
        var files = downloadIllusionCardsFiles(search, pageLimit);
        try {
            return archivationService.zipFiles(files);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<NamedFile> downloadIllusionCardsFiles(String search, Integer pageLimit) {
        final var FIRST_PAGE_ID = 0;

        var actualPageLimit = pageLimit == null ? null : Math.max(pageLimit, 0);
        var firstPage = getSearchPage(search, FIRST_PAGE_ID);
        var pagesCount = firstPage.getPagesCount();
        var actualPagesCount = actualPageLimit == null ? pagesCount : Math.min(pagesCount, actualPageLimit);

        var result = Collections.synchronizedList(new ArrayList<NamedFile>());

        var threadPool = Executors.newFixedThreadPool(7);
        var callables = new ArrayList<Callable<Void>>();

        for (var page = 0; page < actualPagesCount; page++) {
            var searchPage = page == FIRST_PAGE_ID
                    ? firstPage
                    : getSearchPage(search, page);

            var pageCallables = searchPage.getResults().stream()
                    .map(url -> {
                        return (Callable<Void>) (() -> {
                            var file = downloadIllusionCardByHtmlUrl(url);
                            result.add(file);

                            return null;
                        });
                    })
                    .toList();

            callables.addAll(pageCallables);
        }

        try {
            threadPool.invokeAll(callables).forEach(future -> {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Deprecated
    private String getSearchPageHtml(String search, int page) {
        return new RestTemplate().exchange(
                prepareSearchUrl(search, page),
                HttpMethod.GET,
//                RequestEntity.EMPTY,
                HTTP_ENTITY,
                String.class
        ).getBody();
    }

    private IllusionCardsSearchPage getSearchPage(String search, int page) {
        Document document;
        try {
            document = Jsoup.connect(prepareSearchUrl(search, page))
                    .headers(HTTP_HEADERS)
                    .get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var lastPageLink = document.selectFirst("#paginator > [alt='last page']");
        Integer pagesCount = 1;
        if (lastPageLink != null) {
            var lastPageUrl = lastPageLink.attr("href");
            var pidMatcher = Pattern.compile("pid=(\\d+)").matcher(lastPageUrl);
            pidMatcher.find();
            var lastPageId = Integer.parseInt(pidMatcher.group(1));
            pagesCount = (lastPageId / ITEMS_PER_PAGE) + 1;
        }

        var cardLinkElements = document.select(".content .thumb > a");
        var cardLinks = cardLinkElements.stream()
                .map(element -> {
                    return element.absUrl("href");
                })
                .toList();

        return new IllusionCardsSearchPage(cardLinks, pagesCount);
    }

    private NamedFile downloadIllusionCardByHtmlUrl(String htmlUrl) {
        Document document;
        try {
            document = Jsoup.connect(htmlUrl)
                    .headers(HTTP_HEADERS)
                    .get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var cardUrl = document.getElementById("image").absUrl("src");
        var filename = FilenameUtils.getName(cardUrl);

        var content = new RestTemplate().exchange(
                cardUrl, HttpMethod.GET, HTTP_ENTITY, byte[].class
        ).getBody();

        return new NamedFile(filename, content);
    }

    private String prepareSearchUrl(String search, int page) {
        var pid = page * ITEMS_PER_PAGE;

        return String.format(
                "https://illusioncards.booru.org/index.php?page=post&s=list&tags=%s&pid=%d",
                search, pid);
    }
}
