package com.novelbio.analysis.tools.snvintersection;

import java.io.File;
import java.util.Date;

/**
 * 文件交集类
 * 
 * <pre>
 * 项目订制代码，其他比对功能慎用
 * </pre>
 * 
 * @author novelbio
 *
 */
public class FileIntersectionScript {

	/**
	 * <pre>
	 *  注意：
	 * 		该方法是根据当前项目订制的交集功能代码，其他项目不能通用
	 * 使用方法： 
	 *  		fileNameArr里放的是交集文件路径
	 *  结果文件：
	 *  	结果文件存放在第一个目录下
	 *  	文件名规则为{文件名1-文件名2-文件名3-*.intersection}
	 * </pre>
	 */
	public static void main(String[] args) {
//		String[] fileNameArr = new String[] {
//				"/home/novelbio/git/NBCService/service/src/test/java/com/novelbio/erp/analysis/intersection/TestFileIntersectionData-1.txt",
//				"/home/novelbio/git/NBCService/service/src/test/java/com/novelbio/erp/analysis/intersection/TestFileIntersectionData-2.txt" };
		run(args);
	}

	public static void run(String[] fileNameArr) {
		System.out.println("start: " + new Date());
		File[] fileArr = new File[fileNameArr.length];
		for (int i = 0; i < fileArr.length; i++) {
			fileArr[i] = new File(fileNameArr[i]);
		}
		EqualAndSortStrategy4Bll strategy = new EqualAndSortStrategy4Bll();
		FileIntersection.intersection(fileArr, strategy, strategy);
		System.out.println("finish: " + new Date());

	}
}
