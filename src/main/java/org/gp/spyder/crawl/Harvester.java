package org.gp.spyder.crawl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.gp.spyder.DBContext;
import org.gp.spyder.domain.Image;
import org.gp.spyder.domain.WebPage;

public class Harvester extends Thread {

	private static final String HTML_HTTP = "http";
	private final static Logger LOGGER = Logger.getLogger(Harvester.class
			.getName());

	private DBContext dbContext;
	private BlockingQueue<String> imageQueue;

	String downloadDir;

	public Harvester(DBContext dbContext, String downloadDir,
			BlockingQueue<String> imageQueue) {
		this.dbContext = dbContext;
		this.downloadDir = downloadDir;
		this.imageQueue = imageQueue;
	}

	public static String deriveFileFromUrl(String url) {

		if (url.indexOf(HTML_HTTP) == -1) {
			LOGGER.warn(String.format("Invalid URL: '%s'", url));

			return null;
		}

		int startIndex;
		String path;
		String directory;
		StringBuilder stringBuilder = new StringBuilder();

		startIndex = url.indexOf(HTML_HTTP);

		if (startIndex != -1) {
			startIndex = startIndex + 6 + 1;

			path = url.substring(startIndex);
			startIndex = path.indexOf("/");
			path = path.substring(startIndex + 1);

			List<String> tokens = new ArrayList<String>(Arrays.asList(path
					.split("/")));

			String fileName = tokens.get(tokens.size() - 1);
			tokens.remove(tokens.size() - 1);

			for (String token : tokens) {
				stringBuilder.append(token + "/");
			}

			directory = stringBuilder.toString();

			return directory + fileName;
		} else {
			return null;
		}
	}

	public static File downloadFile(String url, String filePath) {

		File file = new File(filePath);
		URL webUrl = null;
		ReadableByteChannel readableByteChannel = null;
		FileOutputStream fileOutputStream = null;

		if (file.exists()) {

			if (file.isFile()) {
				LOGGER.info(String.format("File '%s' already exists", filePath));
				return file;
			} else if (file.isDirectory()) {
				file.delete();
			}
		}

		try {

			webUrl = new URL(url);
			HttpURLConnection httpConnection = (HttpURLConnection) webUrl
					.openConnection();
			httpConnection.addRequestProperty("User-Agent", "Mozilla/5.0");
			readableByteChannel = Channels.newChannel(httpConnection
					.getInputStream());
			fileOutputStream = new FileOutputStream(filePath);
			fileOutputStream.getChannel().transferFrom(readableByteChannel, 0,
					Long.MAX_VALUE);

		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {

			if (fileOutputStream != null) {

				LOGGER.info(String.format("Downloaded file '%s'", filePath));

				try {
					fileOutputStream.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}

		return file;
	}

	@Override
	public void run() {
		try {

			File file;
			Image image;
			String imgUrl;
			String filename;
			String filePath;
			String parentDir;

			while (true) {

				imgUrl = this.imageQueue.take();

				filename = Harvester.deriveFileFromUrl(imgUrl);

				if (filename != null) {

					filePath = this.downloadDir + "/" + filename;

					file = new File(filePath);

					image = dbContext.getImageRepository().findByUrl(imgUrl);

					if (image == null) {
						image = new Image(imgUrl, false);
					}

					if (image.isDownloaded()) {

						if (file.exists()) {

							LOGGER.info(String
									.format("Image file [%s] already exists in File System. Skipping",
											filePath));
						} else {
							image.setDownloded(false);
						}
					}

					parentDir = filePath
							.substring(0, filePath.lastIndexOf("/"));
					(new File(parentDir)).mkdirs();

					if (image.isDownloaded() == false) {
						file = Harvester.downloadFile(imgUrl, filePath);

						if (!file.exists()) {
							LOGGER.error(String.format(
									"Failed to download file '%s'",
									file.getPath()));
						} else {
							image.setDownloded(true);
						}
					}

					dbContext.getImageRepository().save(image);
				}

				Thread.sleep(1000);
			}

		} catch (InterruptedException e) {
			LOGGER.error(e);
		}
	}

}
