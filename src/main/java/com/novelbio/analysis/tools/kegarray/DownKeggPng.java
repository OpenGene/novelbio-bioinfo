package com.novelbio.analysis.tools.kegarray;

import java.util.Set;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.novelbio.base.multithread.RunProcess;

/**
 * 当用<b>KegArray</b>获得结果url之后，用该类来将所有标注好的pathway图片下载下来。
 * 
 * @author ywd
 *
 */
public class DownKeggPng extends RunProcess<Integer> {

	private DownKeggPngUnit downloadUnit;

	public DownKeggPng(String url, String downloadPath) {
		downloadUnit = new DownKeggPngUnit(url, downloadPath);
	}

	@Override
	protected void running() {
		downloadUnit.resolveImagePage();
		downloadUnit.downloadImage();
	}

	/**
	 * 获取需要下载的图片数量,需要先运行 {@link #querKegArrayUrl}方法
	 * 
	 * @return
	 */
	public int getDownloadPicNum() {
		return downloadUnit.setImagePage.size();
	}

	/**
	 * 获取需要下载的图片
	 */
	public void querKegArrayUrl() {
		downloadUnit.prepare();
		downloadUnit.resolvePage();

	}

}
