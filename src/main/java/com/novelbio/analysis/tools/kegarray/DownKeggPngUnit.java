package com.novelbio.analysis.tools.kegarray;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * 当用<b>KegArray</b>获得结果url之后，用该类来将所有标注好的pathway图片下载下来。
 * 
 * @author renyaoxiang
 *
 */
class DownKeggPngUnit {
	private static Logger logger = Logger.getLogger(DownKeggPngUnit.class);
	private int maxTime = 5;
	String searchingUrl;
	String downloadPath;
	Set<String> setImagePage = Collections.synchronizedSet(new HashSet<>());
	Set<String> setFailedImagePage = Collections.synchronizedSet(new HashSet<>());
	Set<String> setImage = Collections.synchronizedSet(new HashSet<>());
	Set<String> setFailedImage = Collections.synchronizedSet(new HashSet<>());
	ExecutorService resolveImageExecutor = Executors.newFixedThreadPool(10);
	ExecutorService downloadExecutor = Executors.newFixedThreadPool(10);
	CountDownLatch resolveImagePageLatch = null;
	CountDownLatch downloadLatch = null;

	/**
	 * 测试方法
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String URL = "http://www.genome.jp//kegg-bin/color_pathway_object?org_name=osa&file=40741&reference=gray";
		String downloadPath = "/home/novelbio/yy-test/";
		DownKeggPngUnit instance = new DownKeggPngUnit(URL, downloadPath);
		instance.startDownload();
	}

	public DownKeggPngUnit(String url, String downloadPath) {
		this.searchingUrl = url;
		this.downloadPath = downloadPath;
	}

	/**
	 * 单独使用时候的下载方法
	 */
	public void startDownload() {
		prepare();
		resolvePage();
		resolveImagePage();
		downloadImage();
		checkFinish();
	}

	/**
	 * 验证输入参数是否完成，验证文件夹路径是否正确
	 */
	void prepare() {
		Validate.notBlank(searchingUrl);
		Validate.notBlank(downloadPath);
		File file = new File(downloadPath);
		if (file.exists()) {
			if (!file.isDirectory()) {
				throw new RuntimeException(downloadPath + " is not directory!");
			}
		} else {
			file.mkdirs();
		}
	}

	/**
	 * 解析页面，获取显示图片的页面地址
	 */
	void resolvePage() {
		try {
			Document doc = Jsoup.connect(searchingUrl).timeout(10000).get();
			Elements lsA = doc.select("a");
			lsA.forEach((element) -> {
				String href = element.absUrl("href");
				if (href.endsWith(".args")) {
					setImagePage.add(href);
				}
			});
			if (setImagePage.size() == 0) {
				throw new RuntimeException("没有找到可以下载的图片地址，请检查页面是否失效");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析显示图片的页面地址，获取图片地址
	 */
	void resolveImagePage() {
		resolveImagePageLatch = new CountDownLatch(setImagePage.size());
		setImagePage.forEach((imagePage) -> {
			fnResolveImagePage(imagePage, 0, (imagePageTemp) -> {
				resolveImagePageLatch.countDown();
			}, (imagePageTemp) -> {
				resolveImagePageLatch.countDown();
				setFailedImagePage.add(imagePage);
			});
		});
		try {
			resolveImagePageLatch.await();
			resolveImageExecutor.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 下载图片
	 */
	void downloadImage() {
		downloadLatch = new CountDownLatch(setImage.size());
		setImage.forEach((imageUrl) -> {
			fnDownloadImage(imageUrl, 0, (imageUrlTemp) -> {
				downloadLatch.countDown();
			}, (imageUrlTemp) -> {
				downloadLatch.countDown();
				setFailedImage.add(imageUrl);
			});
		});
		try {
			downloadLatch.await();
			downloadExecutor.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 验证下载任务是否成功
	 */
	void checkFinish() {
		logger.info("主页图片数量：" + setImagePage.size());
		logger.info("主页图片获取失败数量：" + setFailedImagePage.size());
		logger.info("解析成功图片数量：" + setImage.size());
		logger.info("下载失败的图片数量：" + setFailedImage.size());
		if (setFailedImagePage.size() != 0) {
			throw new RuntimeException("图片页面和图片数量不匹配，请重新下载!");
		}
		if (setFailedImage.size() != 0) {
			throw new RuntimeException("存在下载失败的图片，请重新下载!");
		}
	}

	private void fnResolveImagePage(String imagePageUrl, int time,
			Consumer<String> successCallback, Consumer<String> errorCallback) {
		if (time > this.maxTime) {
			errorCallback.accept(imagePageUrl);
			return;
		}
		resolveImageExecutor.execute(() -> {
			try {
				Document doc = Jsoup.connect(imagePageUrl).timeout(10000).get();
				Elements lsImageElement = doc.select("img");
				if (lsImageElement.size() == 0) {
					throw new RuntimeException("无法获取图片，请检查是否链接失效");
				}
				lsImageElement.forEach((element) -> {
					String absUrl = element.absUrl("src");
					boolean isPng = absUrl.endsWith(".png");
					boolean hasModule = element.hasClass("module");
					if (!hasModule && isPng) {
						setImage.add(absUrl);
					}
				});
				successCallback.accept(imagePageUrl);
			} catch (IOException e) {
				fnResolveImagePage(imagePageUrl, time + 1, successCallback, errorCallback);
			}
		});
	}

	private void fnDownloadImage(String imageUrl, int time, Consumer<String> successCallback,
			Consumer<String> errorCallback) {
		if (time > this.maxTime) {
			errorCallback.accept(imageUrl);
			return;
		}
		downloadExecutor.execute(() -> {
			String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
			Path path = Paths.get(downloadPath, filename);
			File output = path.toFile();
			BufferedInputStream in = null;
			BufferedOutputStream os = null;
			try {
				in = new BufferedInputStream(new URL(imageUrl).openStream());
				os = new BufferedOutputStream(new FileOutputStream(output));
				byte data[] = new byte[5000];
				while (in.read(data) > -1) {
					os.write(data);
				}
				successCallback.accept(imageUrl);
			} catch (IOException e) {
				fnDownloadImage(imageUrl, time + 1, successCallback, errorCallback);
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(os);
			}
		});
	}

}
