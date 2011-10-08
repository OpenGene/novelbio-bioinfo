package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;


import com.novelbio.analysis.seq.genomeNew2.gffOperate.GtfDetailCufIso;
import com.novelbio.analysis.seq.genomeNew2.gffOperate.GtfHashCufIso;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class SplitCope {
	String splitFile = "";
	GtfHashCufIso gtfHashCufIso = null;
	/**splitFile
	 * 指定可变剪接的文件和剪接体文件
	 * @param splitFile
	 * @param fpkmTrackFile
	 */
	public SplitCope(String splitFile, String fpkmTrackFile) {
		gtfHashCufIso = new GtfHashCufIso(fpkmTrackFile);
		this.splitFile = splitFile;
	}
	/**
	 * 给定输出文件，将结果输出splitFile
	 * @param splitNewFile
	 * @throws Exception 
	 */
	public void copeSplit(String splitNewFile) throws Exception {
		TxtReadandWrite txtSplit = new TxtReadandWrite();
		txtSplit.setParameter(splitFile, false, true);

		TxtReadandWrite txtSplitNew = new TxtReadandWrite();
		txtSplitNew.setParameter(splitNewFile, true, false);

		ArrayList<String> lsSplit = txtSplit.readfileLs();
		ArrayList<String> lsExpName = GtfDetailCufIso.getExp();
		String ExpInfo = "";
		for (String string : lsExpName) {
			ExpInfo = ExpInfo+"\t"+string;
		}
		txtSplitNew.writefile(lsSplit.get(0) + ExpInfo+"\n");
		for (int i = 1; i < lsSplit.size(); i++) {
			String[] ss = lsSplit.get(i).split("\t");
			if (ss[2].equals("-")) {
				continue;
			}
			String result = getSplitInfo(ss[2],10);
			if (result == null) {
				continue;
			}
			txtSplitNew.writefile(lsSplit.get(i) + result+"\n");
		}
	}
	/**
	 * 指定geneID，返回该geneID的各个转录本的表达情况
	 * 这样的格式
	 * isoName:exp isoName:exp
	 * @param geneID
	 * @param filter 阈值，
	 * 两种情况至少满足一种才保留
	 * 1. 两组转录本里面有两个以上基因大于该阈值
	 * 2. 两组转录本中，一组有A基因大于该阈值，另一组B基因大于该阈值
	 * @return 如果没找到，就返回null
	 */
	private String getSplitInfo(String geneID,double filter) {
		
		GtfDetailCufIso gtfDetailCufIso = gtfHashCufIso.searchLOC(geneID);
		if (gtfDetailCufIso == null) {
			return null;
		}
		ArrayList<String[]> lsIsoExp = gtfDetailCufIso.getIsoExp();

		//这就是一个矩阵，每一行代表一个时期，每一列代表一种可变剪接。
		//那么如果要达到阈值必须所有行加起来，有两项大于阈值
		//并且所有列加起来，有两项大于阈值
		int[][] Info = new int[lsIsoExp.size()][lsIsoExp.get(0).length];//保存中间矩阵
		
		for (int i = 0; i < lsIsoExp.size(); i++) {//遍历每一行
			for (int j = 0; j < lsIsoExp.get(0).length; j++) {//遍历每一行中的每一格，相当于遍历每一列
				if (Double.parseDouble(lsIsoExp.get(i)[j].split(":")[1]) >= filter) {
					Info[i][j] = 1;
				}
			}
		}
		int addRow = 0;//大于1的有几行
		int addCol = 0;//大于1的有几列
		for (int i = 0; i < Info.length; i++) {
			for (int j = 0; j < Info[i].length; j++) {
				if (Info[i][j] == 1) {
					addRow++;
					break;
				}
			}
		}
		//和刚刚相反
		for (int i = 0; i < Info[0].length; i++) {
			for (int j = 0; j < Info.length; j++) {
				if (Info[j][i] == 1) {
					addCol++;
					break;
				}
			}
		}
		//不达标就跳过
		if (!(addCol>=2&&addRow>=2)) {
			return null;
		}
		String result = "";
		for (int i = 0; i < lsIsoExp.size(); i++) {
			String[] expStrings = lsIsoExp.get(i);
			String tmp = "";
			for (String string : expStrings) {
				if (tmp.equals("")) {
					tmp = string;
					continue;
				}
				tmp = tmp + "," + string;
			}
			result = result+"\t"+tmp;
		}
		return result;
	}
	
	
}
