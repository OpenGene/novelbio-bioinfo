package com.novelbio.analysis.seq.rnaseq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.broadinstitute.sting.jna.lsf.v7_0_6.LibBat.newDebugLog;
import org.broadinstitute.sting.utils.collections.CircularArray.Int;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.database.domain.kegg.noUseKGCentry2Ko2Gen;
import com.novelbio.database.model.modcopeid.CopedID;


public class IsoJunNum {
	
	TophatJunction tophatJunction = new TophatJunction();
	ArrayList<String> lsCod = new ArrayList<String>();
	GffHashGene gffHashGene = null;
	public static void main(String[] args) {
		IsoJunNum isoJunNum = new IsoJunNum();
		isoJunNum.setGffHashGene("/media/winE/NBC/Project/Project_FY_Lab/Result/tophat/cufflinkAlla15m1bf/novelbioModify_a15m1bf_All_high60.GTF");
		isoJunNum.readJunction("/media/winE/NBC/Project/Project_FY_Lab/Result/tophat/tophatDifParam/tophatK0a10m1/junctions.bed", "K0");
 		isoJunNum.readJunction("/media/winE/NBC/Project/Project_FY_Lab/Result/tophat/tophatDifParam/tophatK5a10m1/junctions.bed", "K5");
 		isoJunNum.getCufGeneIso("/media/winF/NBC/Project/Project_FY/chicken/scripture/cufflink/cuffDifK0vsK5/splicing.Out10.xls", 
				"/media/winF/NBC/Project/Project_FY/chicken/scripture/cufflink/cuffDifK0vsK5/splicing.Out10_modify.xls");
	}
	public void setGffHashGene(String gffFile) {
		gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_CUFFLINK_GTF, gffFile,true);
		
	}
	
	
	
	/**
	 * 从cufflink的整理结果中获得想要的转录本信息
	 */
	public void getCufGeneIso(String txtFile, String txtOutFile)
	{
		ArrayList<String[]> lsIsoInfo = ExcelTxtRead.readLsExcelTxt(txtFile, 1, -1, 1, -1);
		TxtReadandWrite txtOut = new TxtReadandWrite(txtOutFile, true);
		for (String[] strings : lsIsoInfo) {
			String geneID = CopedID.removeDot(strings[14].split(":")[0]);
			if (geneID.equals("K0")) {
				continue;
			}
			GffDetailGene gffDetailGene = gffHashGene.searchLOC(geneID);
			List<JunDetail> lsJun = getIso(gffDetailGene);
 
			String[] tmpResult = new String[lsCod.size()];
			if (lsJun.size() < 1) {
				for (int i = 0; i < tmpResult.length; i++) {
					tmpResult[i] = "";
				}
			}
			else {
				tmpResult[0] = lsJun.get(0).getGffIsoName() + "_IntronNum: " + lsJun.get(0).getIntronNum() + "JunNum:" + lsJun.get(0).getJunNum(lsCod.get(0));
				tmpResult[1] = lsJun.get(0).getGffIsoName() + "_IntronNum: " + lsJun.get(0).getIntronNum() + "JunNum:" + lsJun.get(0).getJunNum(lsCod.get(1));
				for (int i = 1; i < 3; i++) {
					if (lsJun.size() <= i+1) {
						break;
					}
					tmpResult[0] = tmpResult[0] + "\\" +  lsJun.get(i).getGffIsoName() + "_IntronNum: " + lsJun.get(i).getIntronNum() + "JunNum:" + lsJun.get(i).getJunNum(lsCod.get(0));
					tmpResult[1] = tmpResult[1] + "\\" +  lsJun.get(i).getGffIsoName() + "_IntronNum: " + lsJun.get(i).getIntronNum() + "JunNum:" + lsJun.get(i).getJunNum(lsCod.get(1));
				}
			}
			String[] result =  ArrayOperate.combArray(strings, tmpResult, -1);
			txtOut.writefileln(result);
		}
		txtOut.close();
	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * 设定junction文件和时期
	 * @param junFile
	 * @param cond
	 */
	public void readJunction(String junFile, String cond)
	{
		tophatJunction.setJunFile(junFile, cond);
		if (lsCod.contains(cond)) {
			return;
		}
		lsCod.add(cond);
	}
	/**
	 * 返回按照排序的Junction的差别情况
	 * 最后可以选取前三个重点研究
	 * @param gffDetailGene
	 * @return
	 */
	public List<JunDetail> getIso(GffDetailGene gffDetailGene) {
		if (gffDetailGene == null) {
			return new LinkedList<JunDetail>();
		}
//		HashMap<String, Integer> hashJunNum = new HashMap<String, Integer>();
		ArrayList<Integer> lsJunNumCod1 = new ArrayList<Integer>();
		ArrayList<Integer> lsJunNumCod2 = new ArrayList<Integer>();
		//去除重复的junction位点，和所有转录本都含有的JunDetail
		HashMap<String, Integer> hashJunSite = new HashMap<String,Integer>();
		//去除所有转录本都含有的JunDetail
		HashMap<String, JunDetail> hashJunInfo = new HashMap<String, JunDetail>();
		LinkedList<JunDetail> lsJunDetailsAll = new LinkedList<JunDetail>();
		for (GffGeneIsoInfo gffGeneIsoInfo : gffDetailGene.getLsCodSplit()) {
			LinkedList<JunDetail> lsJunDetailsTmp = new LinkedList<JunDetail>();
			ArrayList<int[]> lsIso = gffGeneIsoInfo.getIsoInfo();
			for (int i = 0; i < lsIso.size() - 1; i++) {
				if (hashJunSite.containsKey(lsIso.get(i)[1]+"_"+lsIso.get(i+1)[0])) {
					int num = hashJunSite.get(lsIso.get(i)[1]+"_"+lsIso.get(i+1)[0]);
					hashJunSite.put(lsIso.get(i)[1]+"_"+lsIso.get(i+1)[0], num+1);
					continue;
				}
				else {
					hashJunSite.put(lsIso.get(i)[1]+"_"+lsIso.get(i+1)[0], 1);
				}
				JunDetail junDetail = new JunDetail();
				//设定junction的信息
				junDetail.setGffGeneIsoInfo(gffGeneIsoInfo);
				junDetail.setStartLoc(lsIso.get(i)[1]);
				junDetail.setEndLoc(lsIso.get(i+1)[0]);
				junDetail.setIntronNum(i+1);
				//获得两个时期该位点Junction的数量
				int cod1Jun = tophatJunction.getJunctionSite(gffGeneIsoInfo.getChrID(), junDetail.getStartLoc(), junDetail.getEndLoc(), lsCod.get(0));
				int cod2Jun = tophatJunction.getJunctionSite(gffGeneIsoInfo.getChrID(), junDetail.getStartLoc(), junDetail.getEndLoc(), lsCod.get(1));
				junDetail.setJunNumCod( lsCod.get(0), cod1Jun);
				junDetail.setJunNumCod( lsCod.get(1), cod2Jun);
				//求该转录本的平均junction
				lsJunNumCod1.add(cod1Jun);
				lsJunNumCod2.add(cod2Jun);
				lsJunDetailsTmp.add(junDetail);
			}
			int junmean1 = getMean(lsJunNumCod1);
			int junmean2 = getMean(lsJunNumCod2);
			for (JunDetail junDetail : lsJunDetailsTmp) {
				junDetail.setJunNumCodMean(lsCod.get(0), junmean1);
				junDetail.setJunNumCodMean(lsCod.get(1), junmean2);
			}
			for (JunDetail junDetail : lsJunDetailsTmp) {
				hashJunInfo.put(junDetail.getStartLoc()+"_"+junDetail.getEndLoc(), junDetail);
			}
		}
		for (Entry<String, Integer> entry : hashJunSite.entrySet()) {
			String junInfo = entry.getKey();
			int junNum = entry.getValue();
			if (junNum >= gffDetailGene.getLsCodSplit().size()) {
				continue;
			}
			lsJunDetailsAll.add(hashJunInfo.get(junInfo));
		}
		sortJunction(lsJunDetailsAll,  lsCod.get(0), lsCod.get(1));
		return lsJunDetailsAll;
	}
	
	/**
	 * 将junction的序列进行排序，按照junction的比例和总junction数量等进行排序
	 * 从大到小排序
	 * @param lsJunDetails
	 */
	private void sortJunction(List<JunDetail> lsJunDetails, final String codTread, final String codCol)
	{
		Collections.sort(lsJunDetails, new Comparator<JunDetail>() {
			@Override
			public int compare(JunDetail jun1, JunDetail jun2) {
				Double result1 = jun1.getJunScore(codTread, codCol);
				Double result2 = jun2.getJunScore(codTread, codCol);
				return -result1.compareTo(result2);
			}
		});
		
	}
	
	

	private int getMean(ArrayList<Integer> lsJun)
	{
		if (lsJun.size() == 0) {
			return 0;
		}
		int sum = 0;
		for (Integer integer : lsJun) {
			sum = sum + integer;
		}
		return sum/lsJun.size();
	}
}
class JunDetail
{
	GffGeneIsoInfo gffGeneIsoInfo;
 
	int intronNum;
	int startLoc;
	int endLoc;
	/**
	 * 该位点两个时期jun的数量
	 */
	HashMap<String, Integer> hashCodJunNum = new HashMap<String, Integer>();
	/**
	 * 该位点两个时期所有jun的平均数量
	 */
	HashMap<String, Integer> hashCodJunNumMean = new HashMap<String, Integer>();
	public void setGffGeneIsoInfo(GffGeneIsoInfo gffGeneIsoInfo) {
		this.gffGeneIsoInfo = gffGeneIsoInfo;
	}
	public void setEndLoc(int endLoc) {
		this.endLoc = endLoc;
	}
	public void setIntronNum(int intronNum) {
		this.intronNum = intronNum;
	}
	public void setJunNumCod(String cod, int junNum) {
		hashCodJunNum.put(cod, junNum);
	}
	public void setJunNumCodMean(String cod, int junNum) {
		hashCodJunNumMean.put(cod, junNum);
	}
	public void setStartLoc(int startLoc) {
		this.startLoc = startLoc;
	}
	public int getEndLoc() {
		return endLoc;
	}
	public String getGffIsoName() {
		return gffGeneIsoInfo.getIsoName();
	}
	public int getIntronNum() {
		return intronNum;
	}
	public int getJunNum(String cod) {
		return hashCodJunNum.get(cod);
	}
	/**
	 * 返回该位点所有的reads数量
	 * @return
	 */
	public int getJunNumAll()
	{
		int result = 0;
		for (Integer integer : hashCodJunNum.values()) {
			result = result + integer;
		}
		return result;
	}
	public int getStartLoc() {
		return startLoc;
	}
	public double getRatioTreatVsCol(String treat, String col)
	{
		int treatJunNum = hashCodJunNum.get(treat);
		int treatJunMean = hashCodJunNumMean.get(treat);
		int colJunNum = hashCodJunNum.get(col);
		int colJunMean = hashCodJunNumMean.get(col);
		if (colJunNum == 0) {
			colJunNum ++;
		}
		if (colJunNum == 0) {
			colJunNum ++;
		}
		return treatJunNum/colJunNum;
	}
	/**
	 * 以log2返回
	 * @param treat
	 * @param col
	 * @return
	 */
	public double getRatioTreatVsColLog(String treat, String col)
	{
		int treatJunNum = hashCodJunNum.get(treat);
		int treatJunMean = hashCodJunNumMean.get(treat);
		int colJunNum = hashCodJunNum.get(col);
		int colJunMean = hashCodJunNumMean.get(col);
		if (colJunNum == 0) {
			colJunNum ++;
		}
		if (colJunNum == 0) {
			colJunNum ++;
		}
		return Math.log(treatJunNum/colJunNum)/Math.log(2);
	}
	
	
	public Double getJunScore(String treat, String col)
	{
		double ratio = getRatioTreatVsColLog(treat, col);
		Double result = getJunNumAll() * Math.abs(ratio);
		return result;
	}
}




