package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.jexl2.parser.StringParser;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.novelbio.analysis.seq.mapping.MapLibrary;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.information.SoftWareInfo;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
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
	
	public Cuffdiff() {
		SoftWareInfo softWareInfo = new SoftWareInfo(SoftWare.cufflinks);
		exePath = softWareInfo.getExePathRun();
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
	
	private String[] getThreadNum() {
		return new String[]{"-p", threadNum+""};
	}
	
	private String[] getPathAndOtherParam() {
		return new String[]{"-N", "-b", seqFastaPath};
	}

	private String[] getMapLibrary() {
		//TODO 考虑返回建库方式
		return null;
	}
	
	private String getGtfFile() {
		return gtfFile;
	}
	
	private String[] getOutPath() {
		return new String[]{"-o", outPath};
	}
	
	/** 获得样本名称 */
	private String[] getSampleName(String treat, String control) {
		return new String[]{"-L", control + "," + treat};
	}
	
	private String[] getSamleFile(String treat, String control) {
		String[] ctrl2Treat = new String[2];
		
		List<String> lsSampleCol = lsmapPrefix2SetSample.get(control);
		List<String> lsSampleTreat = lsmapPrefix2SetSample.get(treat);
		
		String ctrl = lsSampleCol.get(0);	
		for (int i = 1; i < lsSampleCol.size(); i++) {
			ctrl = ctrl + "," + lsSampleCol.get(i);
		}
		
		String treatment = lsSampleTreat.get(0);
		for (int i = 1; i < lsSampleTreat.size(); i++) {
			treatment = treatment + "," + lsSampleTreat.get(i);
		}

		return new String[]{ctrl, treatment};
	}
	
	/** 运行 */
	public void runCuffDiff() {
		for (Entry<String, String> treat2value : hashmapTreat2Col.entries()) {
			List<String> lsCmd = new ArrayList<>();
			lsCmd.add(exePath + "cuffdiff");
			ArrayOperate.addArrayToList(lsCmd, getMapLibrary());
			ArrayOperate.addArrayToList(lsCmd, getPathAndOtherParam());
			ArrayOperate.addArrayToList(lsCmd, getThreadNum());
			ArrayOperate.addArrayToList(lsCmd, getOutPath());
			ArrayOperate.addArrayToList(lsCmd,  getSampleName(treat2value.getKey(), treat2value.getValue()));
			lsCmd.add(getGtfFile());
			ArrayOperate.addArrayToList(lsCmd, getSamleFile(treat2value.getKey(), treat2value.getValue()));
			CmdOperate cmdOperate = new CmdOperate(lsCmd);
			cmdOperate.run();
		}
	}
}
