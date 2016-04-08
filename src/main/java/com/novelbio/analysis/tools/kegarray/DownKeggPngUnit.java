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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
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
	private int maxTime = 10;
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
	File metaFile = null;

	/**
	 * 测试方法
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// String URL =
		// "http://www.genome.jp//kegg-bin/color_pathway_object?org_name=osa&file=40741&reference=gray";
		// String downloadPath = "/home/novelbio/yy-test/";
		// DownKeggPngUnit instance = new DownKeggPngUnit(URL, downloadPath);
		// instance.startDownload();
		String[][] arrUrl = {
				{
						"http://www.genome.jp//kegg-bin/color_pathway_object?org_name=mmu&file=17028&reference=gray",
						"/home/novelbio/yy/GvsD" },
				{
						"http://www.genome.jp//kegg-bin/color_pathway_object?org_name=mmu&file=51801&reference=gray",
						"/home/novelbio/yy/CvsB" },
				{
						"http://www.genome.jp//kegg-bin/color_pathway_object?org_name=mmu&file=100449&reference=gray",
						"/home/novelbio/yy/DvsB" },
				{
						"http://www.genome.jp//kegg-bin/color_pathway_object?org_name=mmu&file=122172&reference=gray",
						"/home/novelbio/yy/GvsC" } };
		ExecutorService runner = Executors.newFixedThreadPool(4);
		for (int i = 0; i < arrUrl.length; i++) {
			int index = i;
			runner.execute(() -> {
				DownKeggPngUnit instance = new DownKeggPngUnit(arrUrl[index][0], arrUrl[index][1]);
				instance.startDownload();
			});
		}
		runner.shutdown();
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
				throw new RuntimeException("不是文件夹: " + downloadPath);
			}
		} else {
			file.mkdirs();
		}
		Path metaPath = Paths.get(downloadPath, "meta.txt");
		metaFile = metaPath.toFile();

	}

	/**
	 * 解析页面，获取显示图片的页面地址
	 */
	void resolvePage() {
		fnResolvePage(searchingUrl);
	}

	/**
	 * 解析显示图片的页面地址，获取图片地址
	 */
	void resolveImagePage() {
		resolveImagePageLatch = new CountDownLatch(setImagePage.size());
		setImagePage.forEach((imagePage) -> {
			fnResolveImagePage(imagePage, (imagePageTemp) -> {
				resolveImagePageLatch.countDown();
			}, (imagePageTemp) -> {
				setFailedImagePage.add(imagePage);
				resolveImagePageLatch.countDown();
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
			fnDownloadImage(imageUrl, (imageUrlTemp) -> {
				downloadLatch.countDown();
			}, (imageUrlTemp) -> {
				logger.info("下载失败:" + imageUrlTemp);
				setFailedImage.add(imageUrl);
				downloadLatch.countDown();
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

		String info = "\n＝＝＝＝＝＝下载结束＝＝＝＝＝＝＝\n";
		info += searchingUrl + "\n";
		info += DateFormatUtils.format(new Date(), "yyyy-MM-dd hh:mm:ss") + "\n";
		info += "主页图片数量：" + setImagePage.size() + "\n";
		info += "主页图片获取失败数量：" + setFailedImagePage.size() + "\n";
		info += "解析成功图片数量：" + setImage.size() + "\n";
		info += "下载失败的图片数量：" + setFailedImage.size() + "\n";
		info += StringUtils.join(setFailedImage.stream().map((value) -> {
			return value + "\n";
		}).iterator(), "");
		info += "＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝\n";
		logger.info(info);
		try {
			FileUtils.write(metaFile, info, true);
		} catch (IOException e) {
			logger.error("写入meta信息失败：", e);
		}
		if (setFailedImagePage.size() != 0) {
			logger.error("图片页面和图片数量不匹配，请重新下载!");
		}
		if (setFailedImage.size() != 0) {
			logger.error("存在下载失败的图片，请重新下载!");
		}
	}

	private void fnResolvePage(String searchUrl) {
		int time = 0;
		boolean isFinish = false;
		while (!isFinish && time++ < this.maxTime) {
			try {
				logger.info("解析主页中:" + searchingUrl + " " + time);
				Document doc = Jsoup.connect(searchingUrl).timeout(1000 * 60 * 5).get();
				Elements lsA = doc.select("a");
				lsA.forEach((element) -> {
					String href = element.absUrl("href");
					if (href.endsWith(".args")) {
						setImagePage.add(href);
					}
				});
				if (setImagePage.size() == 0) {
					logger.error("没有找到可以下载的图片地址，请检查页面是否失效");
				}
				isFinish = true;
			} catch (IOException e) {
			}
		}
		if (!isFinish) {
			logger.error("请求页面失效:" + searchingUrl);
		}

	}

	private void fnResolveImagePage(String imagePageUrl, Consumer<String> successCallback,
			Consumer<String> errorCallback) {

		resolveImageExecutor.execute(() -> {
			int time = 0;
			boolean isFinish = false;
			while (!isFinish && time++ < this.maxTime) {
				logger.info("解析图片页面中:" + imagePageUrl + " " + time);
				try {
					Document doc = Jsoup.connect(imagePageUrl).timeout(1000 * 60 * 5).get();
					Elements lsImageElement = doc.select("img");
					lsImageElement.forEach((element) -> {
						String absUrl = element.absUrl("src");
						boolean isPng = absUrl.endsWith(".png");
						boolean hasModule = element.hasClass("module");
						if (!hasModule && isPng) {
							setImage.add(absUrl);
						}
					});
					isFinish = true;
				} catch (IOException e) {
				}
			}
			if (isFinish) {
				successCallback.accept(imagePageUrl);
			} else {
				errorCallback.accept(imagePageUrl);
			}
		});
	}

	private void fnDownloadImage(String imageUrl, Consumer<String> successCallback,
			Consumer<String> errorCallback) {
		String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
		Path path = Paths.get(downloadPath, filename);
		downloadExecutor.execute(() -> {
			int time = 0;
			boolean isFinish = false;
			while (!isFinish && time++ < this.maxTime) {
				logger.info("下载中:" + imageUrl + " " + time);
				File output = path.toFile();
				BufferedInputStream in = null;
				BufferedOutputStream os = null;
				try {
					in = new BufferedInputStream(new URL(imageUrl).openStream());
					os = new BufferedOutputStream(new FileOutputStream(output));
					int data = -1;
					while ((data = in.read()) > -1) {
						os.write(data);
					}
					isFinish = true;
				} catch (Exception e) {
				} finally {
					IOUtils.closeQuietly(in);
					IOUtils.closeQuietly(os);
				}
			}
			if (isFinish) {
				successCallback.accept(imageUrl);
			} else {
				errorCallback.accept(imageUrl);
			}
		});
	}

}
