package org.gp.spyder.crawl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.gp.spyder.DBContext;

public class Parser extends Thread {

	private static final String HTML_HREF = "href";
	private static final String HTML_HTTP = "http://";
	private static final String HTML_IMG = "<img";
	private static final String HTML_IMG_SRC = "src";

	private final static Logger LOGGER = Logger.getLogger(Parser.class
			.getName());

	private DBContext dbContext;
	private String baseUrl;

	private BlockingQueue<String> urlQueue;
	private BlockingQueue<String> dataQueue;
	private BlockingQueue<String> imageQueue;

	public Parser(DBContext dbContext, String baseUrl,
			BlockingQueue<String> urlQueue, BlockingQueue<String> dataQueue,
			BlockingQueue<String> imageQueue) {
		this.dbContext = dbContext;
		this.baseUrl = baseUrl;
		this.urlQueue = urlQueue;
		this.dataQueue = dataQueue;
		this.imageQueue = imageQueue;
	}

	public static boolean isUrlValid(String url) {

		String pattern = "(@)?(href=')?(HREF=')?(HREF=\")?(href=\")?(http[s]?://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?";

		Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

		Matcher matcher = regex.matcher(url);

		return matcher.matches();
	}

	public List<String> extractUrls(String text) {

		List<String> urls = new ArrayList<String>();

		int beginning;
		int index;
		int openingTagIndex;
		int closingTagIndex;
		char openingTag;
		String url;

		beginning = 0;
		index = text.indexOf(HTML_HREF, beginning);

		while (index != -1) {

			openingTagIndex = index + 5;
			openingTag = text.charAt(openingTagIndex);
			closingTagIndex = text.indexOf(openingTag, openingTagIndex + 1);

			url = text.substring(openingTagIndex + 1, closingTagIndex);

			if (url.indexOf(HTML_HTTP) == -1) {
				url = this.baseUrl + url;
			}

			if (isUrlValid(url)) {
				if (url.contains("mangareader") && url.contains("naruto")) {
					urls.add(url);
				}

			}

			beginning = closingTagIndex + 1;
			index = text.indexOf(HTML_HREF, beginning);
		}

		return urls;
	}

	public List<String> extractImageUrls(String text) {

		List<String> urls = new ArrayList<String>();

		int beginning;
		int index;
		int openingTagIndex;
		int closingTagIndex;
		int sourceIndex;
		char openingTag;
		String url;

		beginning = 0;
		index = text.indexOf(HTML_IMG, beginning);

		while (index != -1) {
			sourceIndex = text.indexOf(HTML_IMG_SRC, index);

			openingTagIndex = sourceIndex + 4;
			openingTag = text.charAt(openingTagIndex);
			closingTagIndex = text.indexOf(openingTag, openingTagIndex + 1);

			url = text.substring(openingTagIndex + 1, closingTagIndex);

			if (isUrlValid(url)) {
				// TODO: keywords should be provided separately to the
				// constructor
				if (url.contains("naruto")) {
					urls.add(url);
				}
			}

			beginning = closingTagIndex + 1;
			index = text.indexOf(HTML_IMG, beginning);
		}

		return urls;
	}

	@Override
	public void run() {
		try {
			String rawText;
			List<String> urls;
			List<String> imgUrls;
			
			while (true) {
				rawText = dataQueue.take();
				urls = extractUrls(rawText);
				imgUrls = extractImageUrls(rawText);

				for (String url : urls) {
					if (dbContext.getWebPageRepository().findByUrl(url) == null) {
						LOGGER.info("New URL: [" + url + "]");
						urlQueue.put(url);
					}
				}

				for (String imgUrl : imgUrls) {
					if (dbContext.getImageRepository().findByUrl(imgUrl) == null) {
						LOGGER.info("New Image: [" + imgUrl + "]");
						imageQueue.put(imgUrl);
					}
				}

				Thread.sleep(1000);
			}

		} catch (InterruptedException e) {
			LOGGER.error(e);
		}
	}
}
