package com.novelbio.analysis.seq.fasta;

import java.util.ArrayList;

import com.novelbio.base.dataStructure.PatternOperate;

public class SeqFastaMotifSearch {
	SeqFasta seqFasta;
	String regx;
	int site;
	protected SeqFastaMotifSearch(SeqFasta seqFasta) {
		this.seqFasta = seqFasta;
	}
	/**
	 * 可能不能精确到单碱基<b>同时搜索正反向</b>
	 * 给定motif，在序列上查找相应的正则表达式<br>
	 * 返回正向序列和反向序列查找的结果<br>
	 * List-string [4] <br>
	 * 0: seqName<br>
	 * 1: strand : + / -<br>
	 * 2: 具体的motif序列<br>
	 * 3: motif最后一个碱基与本序列site点的距离
	 * @param regex
	 * @param site 设定site为0
	 * 最后返回motif到site点，那么<b>负数</b>表示motif在site的上游，<b>正数</b>表示motif在site的下游
	 * @return
	 */
	public ArrayList<String[]> getMotifScanResult(String regex) {
		return getMotifScanResult(regex, 0);
	}
	/**
	 * 可能不能精确到单碱基<b>同时搜索正反向</b>
	 * 给定motif，在序列上查找相应的正则表达式<br>
	 * 返回正向序列和反向序列查找的结果<br>
	 * List-string [4] <br>
	 * 0: seqName<br>
	 * 1: strand : + / -<br>
	 * 2: 具体的motif序列<br>
	 * 3: motif最后一个碱基与本序列site点的距离
	 * @param regex
	 * @param site 距离该序列终点的位置，上游为负数，下游为正数。譬如该位点为tss，并且tss距离seq终点500bp，则site为-500。
	 * 也就是序列取到tss下游500bp。
	 * 最后返回motif到site点，那么<b>负数</b>表示motif在site的上游，<b>正数</b>表示motif在site的下游
	 * @return
	 */
	public ArrayList<String[]> getMotifScanResult(String regex, int site) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		ArrayList<String[]> lsTmpResultFor = PatternOperate.getPatLoc(seqFasta.toString(), regex, false);
		ArrayList<String[]> lsTmpResultRev = PatternOperate.getPatLoc(seqFasta.reservecom().toString(), regex, false);

		copeMotifResultToList(true, lsTmpResultFor, site, lsResult);
		copeMotifResultToList(false, lsTmpResultRev, site, lsResult);
		
		return lsResult;
	}
	
	/**
	 * @param strand 序列方向
	 * @param lsTmpResult 找到的motif信息，来自PatternOperate.getPatLoc()方法
	 * 最后返回motif到site点，那么<b>负数</b>表示motif在site的上游，<b>正数</b>表示motif在site的下游
	 * @param site 距离该序列终点的位置，上游为负数，下游为正数。譬如tss距离seq终点500bp，则site为-500。
	 * 也就是序列取到tss下游500bp。
	 * @param lsResult 返回的list
	 */
	private void copeMotifResultToList(boolean strand, ArrayList<String[]> lsTmpResultRev, int site, ArrayList<String[]> lsResult) {
		if (lsTmpResultRev != null && lsTmpResultRev.size() > 0) {
			for (String[] strings : lsTmpResultRev) {
				String[] tmpResult = new String[4];
				tmpResult[0] = seqFasta.getSeqName();
				if (strand) {
					tmpResult[1] = "+";
				} else {
					tmpResult[1] = "-";
				}
				
				tmpResult[2] = strings[0];//具体的motif序列
				
				String toEndSite = strings[2];
				if (!strand) {
					toEndSite = strings[1];
				}
				
				if (site != 0) {
					tmpResult[3] = (Integer.parseInt(toEndSite) + site) * -1 + "";
				} else {
					tmpResult[3] = toEndSite;
				}
				
				lsResult.add(tmpResult);
			}
		}
	}
}
