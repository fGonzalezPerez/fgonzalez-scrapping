package es.fgonzalez.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsoupManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsoupManager.class);

	private static final String USER_AGENT = "Mozilla/5.0";
	private static final int TIMEOUT = 10000;
	private static final String TRUE = "true";
	private static final String TEXT = "text";
	private static final String SRC = "src";

	public List<String> getScrapping(String url, String cssQuery, String attr, String htmlCheck) {

		List<String> response = new ArrayList<String>();

		if (getUrlStatus(url) == 200) {
			Document document = getDocument(url);
			Elements elements = getElements(document, cssQuery);

			if (TRUE.equalsIgnoreCase(htmlCheck)) {
				response.add(getHtml(elements));
			} else if (TEXT.equalsIgnoreCase(attr)) {
				for (Element e : elements) {
					response.add(e.text());
				}
			} else if (SRC.equalsIgnoreCase(attr)) {
				for (Element e : elements) {
					response.add(e.absUrl(attr));
				}
			} else {
				for (Element e : elements) {
					response.add(e.attr(attr));
				}
			}
		}
		return response;
	}

	private Document getDocument(String url) {
		Document document = null;
		try {
			document = Jsoup.connect(url).userAgent(USER_AGENT).timeout(TIMEOUT).get();
		} catch (IOException e) {
			LOGGER.warn("Exception getting HTML:" + e.getMessage());
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("StackTrace:", e);
			}
		}
		return document;
	}

	private Elements getElements(Document document, String cssQuery) {
		return document.select(cssQuery);
	}

	private String getHtml(Elements elements) {
		String html = elements.html();
		LOGGER.debug("HTML:" + html);
		return html;
	}

	private int getUrlStatus(String url) {
		Response response = null;
		int code = 0;
		try {
			response = Jsoup.connect(url).userAgent(USER_AGENT).timeout(TIMEOUT).ignoreHttpErrors(true).execute();
		} catch (IOException e) {
			LOGGER.warn("Exception getting Status:" + e.getMessage());
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("StackTrace:", e);
			}
		}
		code = response.statusCode();
		LOGGER.debug("Status code:" + code);
		return code;

	}

}
