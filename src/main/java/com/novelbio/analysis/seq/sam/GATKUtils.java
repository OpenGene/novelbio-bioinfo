package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;

/** 多个vcf文件取交集或并集 */
public class GATKUtils {
	
	private static final Logger logger = Logger.getLogger(GATKUtils.class);

	/**
	 * GATK和samtools的calling结果之间取交集
	 * 
	 * @param gatkFilePath
	 *            GATK生成的calling结果文件路径
	 * @param samToolsFilePath
	 *            samTools生成的calling结果文件路径
	 * @param outputFilePath
	 *            输出的文件路径
	 * @return TreeMap<br>
	 *         key为 CHROM＠POS 形式<br>
	 *         value为 整行数据
	 */
	public static TreeMap<String, String> selectIntersectionFrom(String gatkFilePath, String samToolsFilePath) {
		TxtReadandWrite gatkReader = new TxtReadandWrite(gatkFilePath);
		TxtReadandWrite samReader = new TxtReadandWrite(samToolsFilePath);
		List<String> lsCompare = new ArrayList<String>();
		TreeMap<String, String> mapResult = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String key1, String key2) {
				int value = key1.split("@")[0].compareToIgnoreCase(key2.split("@")[0]);
				if (value == 0) {
					return String.valueOf(key1.split("@")[1]).compareTo(key2.split("@")[1]);
				}
				return value;
			}
		});
		for (String lines : samReader.readlines()) {
			// 忽略注释
			if (lines.trim().startsWith("#"))
				continue;
			// 按制表符分割
			String[] params = lines.trim().split("\t");
			// 取0 1 3 4几位进行对比
			lsCompare.add(params[0] + params[1] + params[3] + params[4]);
		}
		samReader.close();
		for (String lines : gatkReader.readlines()) {
			// 忽略注释
			if (lines.trim().startsWith("#"))
				continue;
			// 按制表符分割
			String[] params = lines.trim().split("\t");
			// 取0 1 3 4几位进行对比
			if (lsCompare.contains(params[0] + params[1] + params[3] + params[4])) {
				mapResult.put(params[0] + "@" + params[1], lines);
			}
		}
		gatkReader.close();
		return mapResult;
	}

	/**
	 * GATK和samtools的calling结果之间取并集
	 * 
	 * @param gatkFilePath
	 *            GATK生成的calling结果文件路径
	 * @param samToolsFilePath
	 *            samTools生成的calling结果文件路径
	 * @param outputFilePath
	 *            输出的文件路径
	 * @return TreeMap<br>
	 *         key为 CHROM＠POS 形式<br>
	 *         value为 整行数据
	 */
	public static TreeMap<String, String> selectUnionFrom(String gatkFilePath, String samToolsFilePath) {
		TxtReadandWrite gatkReader = new TxtReadandWrite(gatkFilePath);
		TxtReadandWrite samReader = new TxtReadandWrite(samToolsFilePath);
		TreeMap<String, String> mapResult = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String key1, String key2) {
				int value = key1.split("@")[0].compareToIgnoreCase(key2.split("@")[0]);
				if (value == 0) {
					return String.valueOf(key1.split("@")[1]).compareTo(key2.split("@")[1]);
				}
				return value;
			}
		});
		for (String lines : gatkReader.readlines()) {
			// 忽略注释
			if (lines.trim().startsWith("#"))
				continue;
			// 按制表符分割
			String[] params = lines.trim().split("\t");
			// 取0 1 3 4几位进行对比
			mapResult.put(params[0] + "@" + params[1], lines);
		}
		gatkReader.close();
		for (String lines : samReader.readlines()) {
			// 忽略注释
			if (lines.trim().startsWith("#"))
				continue;
			// 按制表符分割
			String[] params = lines.trim().split("\t");
			// 取0 1 3 4几位进行对比
			mapResult.put(params[0] + "@" + params[1], lines);
		}
		samReader.close();
		return mapResult;
	}

	/**
	 * GATK和samtools的calling结果之间取交集
	 * 
	 * @param gatkFilePath
	 *            GATK生成的calling结果文件路径
	 * @param samToolsFilePath
	 *            samTools生成的calling结果文件路径
	 * @param outputFilePath
	 *            输出的文件路径
	 * @return 输出的vcf文件路径
	 */
	public static String selectIntersectionFrom(String gatkFilePath, String samToolsFilePath, String outputFilePath) {
		TreeMap<String, String> mapResult = selectIntersectionFrom(gatkFilePath, samToolsFilePath);
		TxtReadandWrite txtOut = new TxtReadandWrite(outputFilePath, true);
		for (String content : mapResult.values()) {
			txtOut.writefileln(content);
		}
		txtOut.close();
		return outputFilePath;
	}

	/**
	 * GATK和samtools的calling结果之间取并集
	 * 
	 * @param gatkFilePath
	 *            GATK生成的calling结果文件路径
	 * @param samToolsFilePath
	 *            samTools生成的calling结果文件路径
	 * @return 输出的vcf文件路径
	 * 
	 */
	public static String selectUnionFrom(String gatkFilePath, String samToolsFilePath, String outputFilePath) {
		TxtReadandWrite resultReader = new TxtReadandWrite(outputFilePath);
		TreeMap<String, String> mapResult = selectUnionFrom(gatkFilePath, samToolsFilePath);
		for (String content : mapResult.values()) {
			resultReader.writefileln(content);
		}
		resultReader.close();
		return outputFilePath;
	}

}
