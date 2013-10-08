package com.novelbio.analysis.seq.mapping;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.information.SoftWareInfo.SoftWare;
import com.novelbio.database.model.species.Species;

public class MapSplice implements MapRNA {
	String exePath;
	/** bowtie就是用来做索引的 */
	MapBowtie mapBowtie = new MapBowtie();
	String fileRefSep;
	String indexFile;
	String outFile;
	int indelLen = 6;
	int threadNum = 10;
	List<FastQ> lsLeftFq = new ArrayList<FastQ>();
	List<FastQ> lsRightFq = new ArrayList<FastQ>();
	int mismatch = 3;
	boolean fusion = false;
	int seedLen = 22;
	Species species;
	@Override
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		if (gffChrAbs != null && gffChrAbs.getSpecies() != null && gffChrAbs.getSpecies().getTaxID() != 0) {
			this.species = gffChrAbs.getSpecies();
		}
	}

	@Override
	public void setExePath(String exePath, String exePathBowtie) {
		this.exePath = exePath;
		mapBowtie.setExePathBowtie(exePathBowtie);
	}
	/** 是否检测fusion gene，检测fusion gene会获得比较少的junction reads */
	public void setFusion(boolean fusion) {
		this.fusion = fusion;
	}
	/** 这个输入的应该是一个包含分割Chr文件的文件夹 */
	@Override
	public void setRefIndex(String index) {
		mapBowtie.setChrIndex(index);
	}
	
	@Override
	public void setGtf_Gene2Iso(String gtfFile) {
		fileRefSep = gtfFile;
	}
	@Override
	public void setOutPathPrefix(String outPathPrefix) {
		this.outFile = outPathPrefix;
	}

	/** indel长度默认为6 */
	@Override
	public void setIndelLen(int indelLen) {
		this.indelLen = indelLen;
	}

	@Override
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	
	@Deprecated
	public void setStrandSpecifictype(StrandSpecific strandSpecifictype) {
		// TODO Auto-generated method stub
		
	}

	@Deprecated
	public void setInsert(int insert) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 设置左端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	public void setLeftFq(List<FastQ> lsLeftFastQs) {
		if (lsLeftFastQs == null) return;
		this.lsLeftFq = lsLeftFastQs;
	}
	/**
	 * 设置右端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	public void setRightFq(List<FastQ> lsRightFastQs) {
		if (lsRightFastQs == null) return;
		this.lsRightFq = lsRightFastQs;
	}

	@Override
	public void setMismatch(int mismatch) {
		this.mismatch = mismatch;
	}

	@Override
	public void mapReads() {
		mapBowtie.setSubVersion(getBowtieVersion());
		mapBowtie.IndexMake(false);
		
		CmdOperate cmdOperate = new CmdOperate(getLsCmd());
		cmdOperate.run();
	}
	private String[] getRefseq() {
		if (species != null) {
			fileRefSep = species.getChromSeqSep();
		}
		return new String[]{"-c", fileRefSep};
	}
	private String[] getIndex() {
		return new String[]{"-x", mapBowtie.getChrNameWithoutSuffix()};
	}
	private String[] getThreadNum() {
		return new String[]{"-p", threadNum + ""};
	}
	private String[] getOutPath() {
		return new String[]{"-o", outFile};
	}
	private String[] getSeedLen() {
		return new String[]{"-s", seedLen + ""};
	}
	private List<String> getIndelLen() {
		List<String> lsIndel = new ArrayList<>();
		lsIndel.add("--ins");
		lsIndel.add(6+"");
		lsIndel.add("--del");
		lsIndel.add(6+"");
		return lsIndel;
	}
	private List<String> getFqFile() {
		List<String> lsFqFileInfo = new ArrayList<>();
		lsFqFileInfo.add("-1");
		String fqLeft = lsLeftFq.get(0).getReadFileName();
		for (int i = 1; i < lsLeftFq.size(); i++) {
			fqLeft = fqLeft + "," + lsLeftFq.get(i);
		}
		lsFqFileInfo.add(fqLeft);
		if (lsRightFq.size() == 0)
			return lsFqFileInfo;
		
		lsFqFileInfo.add("-2");
		String fqRight = lsRightFq.get(0).getReadFileName();
		for (int i = 1; i < lsRightFq.size(); i++) {
			fqRight = fqRight + "," + lsRightFq.get(i);
		}
		lsFqFileInfo.add(fqRight);
		return lsFqFileInfo;
	}
	private List<String> getLsCmd() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("Python");
		lsCmd.add(exePath + "MapSplice1.py");
		ArrayOperate.addArrayToList(lsCmd, getRefseq());
		ArrayOperate.addArrayToList(lsCmd, getIndex());
		lsCmd.addAll(getFqFile());
		ArrayOperate.addArrayToList(lsCmd, getSeedLen());
		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
		lsCmd.addAll(getIndelLen());
		lsCmd.add("--non-canonical");
		if (fusion) {
			lsCmd.add("--fusion");
		}
		ArrayOperate.addArrayToList(lsCmd, getOutPath());
		return lsCmd;
	}
	@Override
	public SoftWare getBowtieVersion() {
		return SoftWare.bowtie;
	}


}
