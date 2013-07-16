package com.novelbio.analysis.seq.denovo;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.fasta.SeqHash;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;


/** 用CD-Hit来对序列进行去重复工作
 *  */
public class ClusterSeq {
	public static void main(String[] args) {
		ClusterSeq clusterSeq = new ClusterSeq();
		clusterSeq.setInFileName("/media/winE/NBC/Project/Project_WH/Trinity.fasta");
		clusterSeq.setOutFileName("/media/winE/NBC/Project/Project_WH/Trinity_cluster.fasta");
		clusterSeq.setIdentityThrshld(85);
		clusterSeq.setThreadNum(6);
		clusterSeq.run();
	}
	/** 是否为蛋白序列 */
	private boolean isProtein;
	protected String inFileName;
	private String outFileName;
	/** 相似度阈值 */
	private int identityThrshld = 90;
	private int memoryBeUse = 2000;
	
	/** 数据是否非常大，无法读取内存 */
	private boolean veryBigData = false;
	/** 是否为精确但是慢的模式 */
	private boolean accurateMode = false;
	
	private int threadNum = 2;
	
	List<List<String>> lsCluster = new ArrayList<List<String>>();
	/** 
	 * 设定100以内的数字
	 * 设定相似度阈值，默认为90
	 * 氨基酸设定范围大于40
	 * 核算设定范围大于75
	 *  */
	public void setIdentityThrshld(int identityThrshld) {
		this.identityThrshld = identityThrshld;
	}
	/** 设定内存使用，默认使用2G内存 */
	public void setMemoryBeUse(int memoryBeUse) {
		this.memoryBeUse = memoryBeUse;
	}
	/** 数据是否非常大，无法读取内存，默认false，也就是会全部读入内存 */
	public void setVeryBigData(boolean veryBigData) {
		this.veryBigData = veryBigData;
	}
	/** 是否为精确但是慢的模式，默认false */
	public void setAccurateMode(boolean accurateMode) {
		this.accurateMode = accurateMode;
	}
	/** 线程数，默认为2 */
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	public void setInFileName(String inFileName) {
		this.inFileName = inFileName;
		try {
			this.isProtein = (SeqHash.getSeqType(inFileName) == SeqFasta.SEQ_PRO);
		} catch (Exception e) {}
	}
	public void setOutFileName(String outFileName) {
		this.outFileName = outFileName;
	}
	
	/**
	 * -n 和 -c 有联系<br>
	 * aa时<br>
-n 5 for thresholds 0.7 ~ 1.0<br>
-n 4 for thresholds 0.6 ~ 0.7<br>
-n 3 for thresholds 0.5 ~ 0.6<br>
-n 2 for thresholds 0.4 ~ 0.5<br>
<br>
nr时<br>
-n 8,9,10 for thresholds 0.90-1.0<br>
-n 7 for thresholds 0.88-0.9<br>
-n 6 for thresholds 0.85-0.88<br>
-n 5 for thresholds 0.80-0.85<br>
-n 4 for thresholds 0.75-0.80<br>
	 */
	private String getThreshold() {
		double similar = (double)identityThrshld/100;
		int wordLen = 5;
		if (isProtein) {
			if (similar > 0.7) {
				wordLen = 5;
			} else if (similar > 0.6) {
				wordLen = 4;
			} else if (similar > 0.5) {
				wordLen = 3;
			} else {
				wordLen = 2;
			}
		} else {
			if (similar > 0.98) {
				wordLen = 10;
			} else if (similar > 0.95) {
				wordLen = 9;
			} else if (similar > 0.9) {
				wordLen = 8;
			} else if (similar > 0.88) {
				wordLen = 7;
			} else if (similar > 0.85) {
				wordLen = 6;
			}  else if (similar > 0.80) {
				wordLen = 5;
			} else {
				wordLen = 4;
			} 
		}
		return " -c " + similar + " -n " + wordLen + " ";
	}
	
	private String getBigData() {
		if (veryBigData) {
			return " -B 1 ";
		} else {
			return " -B 0 ";
		}
	}
	private String getTreadNum() {
		return " -T " + threadNum + " ";
	}
	private String getMemoryUse() {
		return " -M " + memoryBeUse + " ";
	}
	private String getInFileName() {
		return " -i " + inFileName + " ";
	}
	private String getOutFileName() {
		return " -o " + outFileName + " ";
	}
	private String isAccurate() {
		if (accurateMode) {
			return " -g 1 "; 
		} else {
			return " -g 0 ";
		}
	}
	public void run() {
		String cmd = "";
		if (isProtein) {
			cmd = "cd-hit";
		} else {
			cmd = "cd-hit-est";
		}
		cmd = cmd + getBigData() + getInFileName() + getMemoryUse() + getOutFileName() + getThreshold() + getTreadNum() + isAccurate();
		CmdOperate cmdOperate = new CmdOperate(cmd, "cd-hit");
		cmdOperate.run();
		lsCluster.clear();
	}
	
	public List<List<String>> getLsCluster() {
		if (lsCluster.size() == 0) {
			loadResult();
		}
		return lsCluster;
	}
	
	/** 将结果读取并整理至list中 */
	private void loadResult() {
		lsCluster = new ArrayList<List<String>>();
		List<String> lsTmpCluster = null;
		TxtReadandWrite txtRead = new TxtReadandWrite(outFileName);
		for (String content : txtRead.readlines()) {
			if (content.startsWith(">")) {
				if (lsTmpCluster != null && lsTmpCluster.size() > 0) {
					lsCluster.add(lsTmpCluster);
				}
				lsTmpCluster = new ArrayList<String>();
				continue;
			}
			
			String tmpName = null;
			tmpName = content.split("\t")[1];
			tmpName = tmpName.split(">", 2)[1];
			tmpName = tmpName.split("\\.\\.\\.")[0];
			lsTmpCluster.add(tmpName);
		}
		txtRead.close();
		
		if (lsTmpCluster != null && lsTmpCluster.size() > 0) {
			lsCluster.add(lsTmpCluster);
		}
	}
}
