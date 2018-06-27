package com.novelbio.analysis.gwas.convertformat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 把mid文件转化为ped文件
 * mid文件格式为
 * chrId \t position \t RefBase \t SnpBase \t TotalHitNum \t Map \t RefNum \t SnpNum \t Sample \t Sample
 * @author zong0jie
 * @data 2018年6月12日
 */
public class Mid2Ped {
	private static final Logger logger = LoggerFactory.getLogger(Mid2Ped.class);
	
	/** 同时读多个样本，这样的话占点内存，但是可以少读几遍文本 */
	int consistentSampleNum = 80;
	/**
	 * 默认起点为8 
	 * Chromosome      Position        RefBase SnpBase TotalHitNum     MAF     RefNum  SnpNum  W0101   W0102   
	 * 样本之前有8个值
	 * 因为也用于我们的plink.pre转plink，所以plink.pre本参数设置为0
	 */
	int startNum = 8;
	int snpNum;
	
	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			printHelp();
			System.exit(1);
		}
		for (String string : args) {
			if (string.toLowerCase().contains("help")) {
				printHelp();
				System.exit(1);
			}
		}
	
		Options opts = new Options();
		opts.addOption("mid", true, "mid");
		opts.addOption("parallelNum", true, "parallelNum");
		CommandLine cliParser = null;
		try {
			cliParser = new GnuParser().parse(opts, args);
		} catch (Exception e) {
			printHelp();
			System.exit(1);
		}
		String mid = cliParser.getOptionValue("mid", "");
		String consistentSampleNum = cliParser.getOptionValue("parallelNum", "");

		String ped = FileOperate.changeFileSuffix(mid, "", "mid", "ped.gz");
		String map = FileOperate.changeFileSuffix(mid, "", "mid", "map");
		Mid2Ped mid2Ped = new Mid2Ped();
		if (!StringOperate.isRealNull(consistentSampleNum)) {
			mid2Ped.setConsistentSampleNum(Integer.parseInt(consistentSampleNum));
		}
		DateUtil dateUtil = new DateUtil();
		dateUtil.setStartTime();
		mid2Ped.convert2Ped(mid, map, ped);
		System.out.println(dateUtil.getElapseTime());
	}
	
	private static void printHelp() {
		System.err.println("java -jar mid2ped.jar --mid mid --parallelNum 20");
		System.err.println();
		System.err.println("example:");
		System.err.println("java -jar mid2ped.jar --mid /home/novelbio/my.mid");
	}
	
	public void setStartNum(int startNum) {
		this.startNum = startNum;
	}
	
	public void setConsistentSampleNum(int consistentSampleNum) {
		this.consistentSampleNum = consistentSampleNum;
	}
	public void convert2Ped(String mid, String map, String ped) {
		setSnpNumAndConvertMap(mid, map);
		logger.info("finish convert to map");
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(ped, true);
		TxtReadandWrite txtRead = new TxtReadandWrite(mid);
		String title = txtRead.readFirstLine();
		List<String> lsSamples = getLsSampleName(title);
		for (int i = 0; i < lsSamples.size(); i+=consistentSampleNum) {
			int numEnd = i + consistentSampleNum;
			if (numEnd > lsSamples.size()) {
				numEnd = lsSamples.size();
			}
			List<String> lsSampleSub = lsSamples.subList(i, numEnd);
			logger.info("start write sample {} to sample {}", lsSampleSub.get(0), lsSampleSub.get(lsSampleSub.size()-1));
			Map<String, char[]> mapSample2Snps = getMapSample2Snp(lsSampleSub);
			
			int snpNum = 0;
			for (String content : txtRead.readlines(2)) {
				if (content.contains(",")) {
					content = content.replace(",", "\t");
				}
				String[] ss = content.split("\t");
				for (int j = 0; j < numEnd-i; j++) {
					char[] charsnp = mapSample2Snps.get(lsSampleSub.get(j));
					charsnp[snpNum] = ss[i+startNum+j].toCharArray()[0];
				}
				snpNum++;
			}
			
			for (String sample : lsSampleSub) {
				char[] snps = mapSample2Snps.get(sample);
				txtWrite.writefile(sample+" "+sample+" 0 0 0 -9");
				for (char c : snps) {
					if (c=='-') {
						c = '0';
					}
					txtWrite.writefile(' ');
					txtWrite.writefile(c);
					txtWrite.writefile(' ');
					txtWrite.writefile(c);
				}
				txtWrite.writefile('\n');
			}
		}
		txtRead.close();
		txtWrite.close();
	}
	
	private void setSnpNumAndConvertMap(String mid, String map) {
		TxtReadandWrite txtRead = new TxtReadandWrite(mid);
		TxtReadandWrite txtWrite = null;
		if (startNum > 0) {
			txtWrite = new TxtReadandWrite(map, true);				
		}
				
		snpNum = 0;
		for (String content : txtRead.readlines(2)) {
			String[] ss = content.split("\t");
			snpNum++;
			if (txtWrite != null) {
				txtWrite.writefileln(ss[0] + "\t" + snpNum + "\t0\t" + ss[1]);
			}
		}
		txtRead.close();
		if (txtWrite != null) {
			txtWrite.close();
		}
	}
	
	private List<String> getLsSampleName(String title) {
		String[] ss = title.split("\t");
		List<String> lsSamples = new ArrayList<>();
		for (int i = startNum; i < ss.length; i++) {
			lsSamples.add(ss[i]);
		}
		return lsSamples;
	}
	
	private Map<String, char[]> getMapSample2Snp(List<String> lsSample) {
		Map<String, char[]> mapSample2Snps = new LinkedHashMap<>();
		for (String sample : lsSample) {
			mapSample2Snps.put(sample, new char[snpNum]);
		}
		return mapSample2Snps;
	}
}
