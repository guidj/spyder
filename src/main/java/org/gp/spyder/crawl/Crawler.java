package org.gp.spyder.crawl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.gp.spyder.DBContext;
import org.gp.spyder.domain.WebPage;

public class Crawler extends Thread {

  private final static Logger LOGGER = Logger.getLogger(Crawler.class
      .getName());

  private BlockingQueue<String> urlQueue;
  private BlockingQueue<String> dataQueue;

  DBContext dbContext;

  public Crawler(DBContext dbContext, BlockingQueue<String> urlQueue,
      BlockingQueue<String> dataQueue) {
    this.dbContext = dbContext;
    this.urlQueue = urlQueue;
    this.dataQueue = dataQueue;
  }

  public String crawl(String url) {

    InputStream inputStream = null;
    BufferedReader bufferedReader;
    String line;
    StringBuilder stringBuilder = new StringBuilder();

    try {

      URL webUrl = new URL(url);
      inputStream = webUrl.openStream();
      bufferedReader = new BufferedReader(new InputStreamReader(
          inputStream));

      while ((line = bufferedReader.readLine()) != null) {
        stringBuilder.append(line);
      }

    } catch (MalformedURLException mue) {

      LOGGER.error(mue.getStackTrace().toString());

    } catch (IOException ioe) {
      LOGGER.error(ioe.getStackTrace().toString());
    } finally {

      try {
        if (inputStream != null) {
          inputStream.close();
        }
      } catch (IOException ioe) {

      }
    }

    if (stringBuilder.length() > 0) {
      return stringBuilder.toString();
    } else {
      return null;
    }
  }

  @Override
  public void run() {
    try {

      WebPage page;
      String url;
      String rawText;

      while (true) {

        url = urlQueue.take();

        page = dbContext.getWebPageRepository().findByUrl(url);

        if (page == null) {
          page = new WebPage(url, false, 0);
        }

        if (page.isCrawled() == false) {

          rawText = crawl(url);

          // TODO: Too naive. check what the issue was (400, 500, 404, etc)
          if (rawText != null) {
            dataQueue.put(rawText);

            page.setCrawled(true);
            page.setStatus(1);

            LOGGER.info("Data queue size: " + dataQueue.size());
          } else {
            page.setStatus(-1);
          }
        }

        dbContext.getWebPageRepository().save(page);

        Thread.sleep(1000);

      }
    } catch (InterruptedException e) {
      LOGGER.error(e);
    }
  }
}
