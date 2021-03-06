package com.poprosturonin.sites.jbzd;

import com.poprosturonin.data.Meme;
import com.poprosturonin.data.Page;
import com.poprosturonin.data.Tag;
import com.poprosturonin.data.contents.Content;
import com.poprosturonin.data.contents.GIFContent;
import com.poprosturonin.data.contents.ImageContent;
import com.poprosturonin.data.contents.VideoContent;
import com.poprosturonin.exceptions.PageIsEmptyException;
import com.poprosturonin.sites.PageScrapper;
import com.poprosturonin.utils.ParsingUtils;
import com.poprosturonin.utils.URLUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JbzdPageScrapper implements PageScrapper {
    private final static String SEQUENCE_404 = "Error 404";

    private boolean is404(String title) {
        return title.contains(SEQUENCE_404);
    }

    @Override
    public Page parsePage(Document document) {
        Page page = new Page();

        String title = document.title();
        if (is404(title))
            throw new PageIsEmptyException();
        page.setTitle(title);

        //Get next link page
        Elements nextPageElement = document.getElementsByClass("btn-next-page");
        if (nextPageElement.size() > 0)
            page.setNextPage("/jbzd/page" + URLUtils.cutToSecondSlash(URLUtils.cutOffParameters(nextPageElement.get(0).attr("href"))).get());

        //Get content
        Elements listElements = document.select("section[role=listing] > article");
        List<Meme> memes = listElements.stream()
                .map(this::parseListElement)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(meme -> URLUtils.getPathId(meme.getUrl()).ifPresent(s -> meme.setViewUrl(String.format("/jbzd/%s", s))))
                .collect(Collectors.toList());

        page.getMemes().addAll(memes);

        if (page.isEmpty())
            throw new PageIsEmptyException();

        return page;
    }

    private Content getContent(Element mediaElement) {
        Element image = mediaElement.select("img").first();
        if (image != null) {
            if (image.attr("src").endsWith(".gif"))
                return new GIFContent(image.attr("src"));
            else
                return new ImageContent(image.attr("src"));
        }

        Elements videos = mediaElement.select("video > source");
        if (!videos.isEmpty())
            return new VideoContent(videos.attr("src"));

        return null;
    }

    private Optional<Meme> parseListElement(Element element) {
        String title, url;
        int comments, votes = 0;

        // Get header
        Element titleElement = element.select("div.title > a").first();
        if (titleElement != null) {
            title = titleElement.text();
            url = titleElement.attr("href");
        } else
            return Optional.empty();

        // Get content
        Content content = getContent(element.select("div.media").first());
        if (content == null)
            return Optional.empty();

        // Get votes
        Element plusOneElement = element.select("a.btn-plus").first();
        if (plusOneElement != null) {
            votes = ParsingUtils.parseIntOrGetZero(plusOneElement.select("span").text());
        }

        // Get comments
        comments = ParsingUtils.parseIntOrGetZero(element.select("span.comments").first().ownText());

        // Get tags
        List<Tag> tags = null;
        Element tagListElement = element.select("div.tags").first();
        if (tagListElement != null) {
            Elements tagsElements = tagListElement.select("a.tag");
            tags = tagsElements.stream()
                    .map((Element e) -> new Tag(
                            e.text().replaceFirst("#", ""),
                            e.attr("href"),
                            URLUtils.cutToSecondSlash(e.attr("href")).orElse(" ").substring(1)))
                    .collect(Collectors.toList());
        }

        Meme meme = new Meme(title, content, url, comments, votes);
        meme.setTags(tags);
        return Optional.of(meme);
    }
}
