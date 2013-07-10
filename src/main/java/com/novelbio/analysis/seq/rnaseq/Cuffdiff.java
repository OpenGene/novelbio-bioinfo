package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multimap;
import com.novelbio.analysis.seq.mapping.MapLibrary;
import com.novelbio.base.HashMapLsValue;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;

public class Cuffdiff {
	public static void main(String[] args) {
		Species species = new Species();
		System.out.println(species.getCommonName());
		for (String version : species.getVersionAll()) {
			System.out.println(version);
		}
	}
	String seqFastaPath = "";
	String gtfFile = "";
	
	String outPath = "";
	
	//cuffdiff的参数
	String exePath = "";
	int threadNum = 4;
	/** 
	 * 装载输入样本Sam的文件
	 * 每个子类listSam表示一系列的重复
	 *  */
	ArrayListMultimap<String, String> lsmapPrefix2SetSample = ArrayListMultimap.create();
	/** 不重复的比较，前面为treat，后面为control */
	HashMultimap<String, String> hashmapTreat2Col = HashMultimap.create();
	/** 样本名 */
	ArrayList<String> lsSampleName;
	
	MapLibrary mapLibrary;
	
	/**
	 * 设定cuffdiff所在的文件夹以及待比对的路径
	 * @param exePath 如果在根目录下则设置为""或null
	 */
	public void setExePath(String exePath) {
		if (exePath == null || exePath.trim().equals("")) {
			this.exePath = "";
		} else {
			this.exePath = FileOperate.addSep(exePath);
		}
	}
	public void setLsSample2Prefix(ArrayList<String[]> lsSample2Prefix) {
		for (String[] strings : lsSample2Prefix) {
			lsmapPrefix2SetSample.put(strings[1], strings[0]);
		}
	}
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	/**
	 * 设定比较
	 * @param mapPrefix2Prefix
	 */
	public void setCompare(ArrayList<String[]> lsTreat2Col) {
		for (String[] strings : lsTreat2Col) {
			hashmapTreat2Col.put(strings[0], strings[1]);
		}
	}
	public void setSeqFasta(String seqFastaPath) {
		this.seqFastaPath = seqFastaPath;
	}
	/** 必须是cuffcompare处理过的gtf文件 */
	public void setGtfFile(String gtfFile) {
		this.gtfFile = gtfFile;
	}
	
	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}
	
	private String getThreadNum() {
		return " -p " + threadNum + " ";
	}
	
	private String getPathAndOtherParam() {
		return " -N -b " + CmdOperate.addQuot(seqFastaPath) + " ";
	}

	private String getMapLibrary() {
		//TODO 考虑返回建库方式
		return "";
	}
	
	private String getGtfFile() {
		return CmdOperate.addQuot(gtfFile);
	}
	
	private String getOutPath() {
		return " -o " + CmdOperate.addQuot(outPath) + " ";
	}
	
	/** 获得样本名称 */
	private String getSampleName(String treat, String control) {
		String out = " -L " + CmdOperate.addQuot(control) + "," + CmdOperate.addQuot(treat);
		return out + " ";
	}
	
	private String getSamleFile(String treat, String control) {
		String sampleFile = "";
		
		List<String> lsSampleCol = lsmapPrefix2SetSample.get(control);
		List<String> lsSampleTreat = lsmapPrefix2SetSample.get(treat);
		
		sampleFile = sampleFile + " " + CmdOperate.addQuot(lsSampleCol.get(0));	
		for (int i = 1; i < lsSampleCol.size(); i++) {
			sampleFile = sampleFile + "," + CmdOperate.addQuot(lsSampleCol.get(i));
		}	
		
		
		sampleFile = sampleFile + " " + CmdOperate.addQuot(lsSampleTreat.get(0));
		for (int i = 1; i < lsSampleTreat.size(); i++) {
			sampleFile = sampleFile + "," + CmdOperate.addQuot(lsSampleTreat.get(i));
		}

		return sampleFile + " ";
	}
	
	/** 运行 */
	public void runCuffDiff() {
		for (Entry<String, String> treat2value : hashmapTreat2Col.entries()) {
			String cmd = exePath + "cuffdiff " + getMapLibrary() + getPathAndOtherParam() + getThreadNum() + getOutPath() + getSampleName(treat2value.getKey(), treat2value.getValue());
			cmd = cmd + getGtfFile() + getSamleFile(treat2value.getKey(), treat2value.getValue());
			CmdOperate cmdOperate = new CmdOperate(cmd, "cuffdiff");
			cmdOperate.run();
		}
	}
}
