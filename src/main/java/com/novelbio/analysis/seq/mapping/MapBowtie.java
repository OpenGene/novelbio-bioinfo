package com.novelbio.analysis.seq.mapping;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.novelbio.analysis.seq.fastq.FastQ;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.ExceptionCmd;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.information.SoftWareInfo.SoftWare;

//TODO 没写好
@Component
@Scope("prototype")
public class MapBowtie extends MapDNA {
	Logger logger = Logger.getLogger(MapBowtie.class);
	public static final int Sensitive_Very_Fast = 11;
	public static final int Sensitive_Fast = 12;
	public static final int Sensitive_Sensitive = 13;
	public static final int Sensitive_Very_Sensitive = 14;

	List<String> lsSampleGroup = new ArrayList<>();
	/** 非unique mapping的话，取几个 */
	int mappingNum = 0;
	
	/** 插入片段 pairend是500， mate pair就要很大了 */
	int insertMax = 500;
	
	int threadNum = 5;
	
	/** 一条reads最多比对到4个不同的位置上去 */
	int maxMultipHit = 4;
	
	int sensitive = Sensitive_Sensitive;
	boolean isLocal = true;
	/**
	 * pe -fr
	 * mp -rf
	 */
	MapLibrary mapLibrary = MapLibrary.PairEnd;
	
	public MapBowtie() {
		super(SoftWare.bowtie);
	}

	public void setLocal(boolean isLocal) {
		this.isLocal = isLocal;
	}

	public void setSensitive(int sensitive) {
		this.sensitive = sensitive;
	}
	
	public void setMapLibrary(MapLibrary mapLibrary) {
		this.mapLibrary = mapLibrary;
	}
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	
	/**
	 * 设置左端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	public void setLeftFq(List<FastQ> lsLeftFastQs) {
		this.lsLeftFq = lsLeftFastQs;
	}
	/**
	 * 设置右端的序列，设置会把以前的清空
	 * @param fqFile
	 */
	public void setRightFq(List<FastQ> lsRightFastQs) {
		this.lsRightFq = lsRightFastQs;
	}
	
	/**
	 * 返回输入的文件，根据是否为pairend，调整返回的结果
	 * @return
	 */
	private List<String> getLsFqFile() {
		List<String> lsCmd = new ArrayList<>();
		String lsFileName = lsLeftFq.get(0).getReadFileName();
		for (int i = 1; i < lsLeftFq.size(); i++) {
			lsFileName = lsFileName + "," + lsLeftFq.get(i).getReadFileName();
		}
		if (!isPairEnd()) {
			lsCmd.add("-U");
			lsCmd.add(lsFileName);
		} else {
			lsCmd.add("-1");
			lsCmd.add(lsFileName);
			lsCmd.add("-2");
			String lsFileName2 = lsRightFq.get(0).getReadFileName();
			for (int i = 1; i < lsLeftFq.size(); i++) {
				lsFileName2 = lsFileName2 + "," + lsRightFq.get(i).getReadFileName();
			}
			lsCmd.add(lsFileName2);
		}
		return lsCmd;
	}

	private String getOffset() {
		if (lsLeftFq.get(0).getOffset() == FastQ.FASTQ_ILLUMINA_OFFSET) {
			return "--phred64";
		}
		return "--phred33";
	}
	/** 非unique mapping，最多可以比对到多少地方上去，设定为10比较合适把 */
	private String[] getMappingNum() {
		if (mappingNum <= 0) {
			return null;
		}
		return new String[]{"-k", mappingNum + ""};
	}
	
	private String getMapLibrary() {
		if (!isPairEnd()) {
			return null;
		} else if (mapLibrary == MapLibrary.SingleEnd || mapLibrary == MapLibrary.PairEnd) {
			return "--fr";
		} else if (mapLibrary == MapLibrary.MatePair) {
			return "--rf";
		}
		return null;
	}
	
	private String[] getInsertSize() {
		if (isPairEnd()) {
			if (mapLibrary == MapLibrary.SingleEnd || mapLibrary == MapLibrary.PairEnd) {
				insertMax = 500;
			} else if (mapLibrary == MapLibrary.MatePair) {
				insertMax = 10000;
			} else if (mapLibrary == MapLibrary.MatePairLong) {
				insertMax = 25000;
			}
			return new String[]{"-X", insertMax + ""};
		}
		return null;
	}
	
	private String[] getMultiHit() {
		return new String[]{"-k", maxMultipHit + ""};
	}
	
	/**
	 * 本次mapping的组，所有参数都不能有空格
	 * @param sampleID 
	 * @param LibraryName
	 * @param SampleName
	 * @param Platform
	 */
	public void setSampleGroup(String sampleID, String LibraryName, String SampleName, String Platform) {
		if (sampleID == null || sampleID.equals("")) {
			return;
		}
		lsSampleGroup.clear();
		lsSampleGroup.add("--rg-id"); lsSampleGroup.add(sampleID);
		if (SampleName != null && !SampleName.trim().equals("")) {
			lsSampleGroup.add("--rg");
			lsSampleGroup.add("SM:" + SampleName.trim());
		} else {
			lsSampleGroup.add("--rg");
			lsSampleGroup.add("SM:" + sampleID.trim());
		}
		if (LibraryName != null && !LibraryName.trim().equals("")) {
			lsSampleGroup.add("--rg");
			lsSampleGroup.add("LB:" + LibraryName.trim());
		}
		
		if (Platform != null && !Platform.trim().equals("")) {
			lsSampleGroup.add("--rg");
			lsSampleGroup.add("PL:" + Platform);
		} else {
			lsSampleGroup.add("--rg");
			if (mapLibrary == MapLibrary.MatePair) {
				lsSampleGroup.add("PL:IonProton");
			} else {
				lsSampleGroup.add("PL:Illumina");
			}
		}
	}
	
	private List<String> getSampleGroup() {
		return lsSampleGroup;
	}
	private String[] getThreadNum() {
		if (threadNum <= 0) {
			return null;
		}
		return new String[]{"-p", threadNum + ""};
	}
	
	protected boolean isPairEnd() {
		if (lsLeftFq.size() == 0|| lsRightFq.size() == 0) {
			return false;
		}
		return true;
	}
	
	protected SamFile mapping() {
		List<String> lsCmd = getLsCmdMapping();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setGetCmdInStdStream(true);
		cmdOperate.setRedirectInToTmp(true);
		for (FastQ fqL : lsLeftFq) {
			cmdOperate.addCmdParamInput(fqL.getReadFileName());
		}
		for (FastQ fqR : lsRightFq) {
			cmdOperate.addCmdParamInput(fqR.getReadFileName());
		}
		
		Thread thread = new Thread(cmdOperate);
		thread.setDaemon(true);
		thread.start();
		InputStream inputStream = cmdOperate.getStreamStd();
		SamFile samResult = copeSamStream(true, inputStream, isNeedSort);
		if (!cmdOperate.isFinishedNormal()) {
			throw new ExceptionCmd("bowtie2 mapping error:\n" + cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut());
		}
		if (samResult != null && !cmdOperate.isRunning()) {
			return samResult;
		} else {
			deleteFailFile();
			return null;
		}
	}
	
	/** 目前只能做bowtie2的mapping */
	private List<String> getLsCmdMapping() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add(indexMaker.getExePath() + "bowtie2");
		if (isLocal) {
			lsCmd.add("--local");
			if (sensitive == Sensitive_Very_Fast) {
				lsCmd.add("--very-fast-local");
			} else if (sensitive == Sensitive_Fast) {
				lsCmd.add("--fast-local");
			} else if (sensitive == Sensitive_Sensitive) {
				lsCmd.add("--sensitive-local");
			} else if (sensitive == Sensitive_Very_Sensitive) {
				lsCmd.add("--very-sensitive-local");
			} else {
				lsCmd.add("--sensitive-local");
			}
		} else {
			lsCmd.add("--end-to-end");
			if (sensitive == Sensitive_Very_Fast) {
				lsCmd.add("--very-fast");
			} else if (sensitive == Sensitive_Fast) {
				lsCmd.add("--fast");
			} else if (sensitive == Sensitive_Sensitive) {
				lsCmd.add("--sensitive");
			} else if (sensitive == Sensitive_Very_Sensitive) {
				lsCmd.add("--very-sensitive");
			} else {
				lsCmd.add("--sensitive");
			}
		}
		
		lsCmd.add("--mm");
		lsCmd.add(getOffset());
		ArrayOperate.addArrayToList(lsCmd, getMappingNum());
		addListStr(lsCmd, getMapLibrary());
		lsCmd.addAll(getSampleGroup());
		ArrayOperate.addArrayToList(lsCmd, getThreadNum());
		ArrayOperate.addArrayToList(lsCmd, getInsertSize());
		ArrayOperate.addArrayToList(lsCmd, getMultiHit());
		lsCmd.add("-x"); lsCmd.add(indexMaker.getIndexName());
		lsCmd.addAll(getLsFqFile());
//		ArrayOperate.addArrayToList(lsCmd, getOutFileName());
		return lsCmd;
	}
	
	private void addListStr(List<String> lsCmd, String param) {
		if (param == null) return;
		lsCmd.add(param);
	}
	
	@Override
	public List<String> getCmdExeStr() {
		List<String> lsResult = new ArrayList<>();
		lsResult.add(SoftWare.bowtie.toString() + " version: " + indexMaker.getVersion());
		List<String> lsCmd = getLsCmdMapping();
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		lsResult.add(cmdOperate.getCmdExeStr());
		return lsResult;
	}
	
	
	public static Map<String, Integer> getMapSensitive() {
		Map<String, Integer> mapSensitive = new LinkedHashMap<>();
		mapSensitive.put("Sensitive", Sensitive_Sensitive);
		mapSensitive.put("Very Sensitive", Sensitive_Very_Sensitive);
		mapSensitive.put("Fast", Sensitive_Fast);
		mapSensitive.put("VeryFast", Sensitive_Very_Fast);
		return mapSensitive;
	}


}
