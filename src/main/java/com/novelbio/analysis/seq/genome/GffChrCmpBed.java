package com.novelbio.analysis.seq.genome;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.MapInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReads;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
//TODO 没做好的类
/**
 * 比较两个bed文件之间的区别
 * 在com.novelbio.analysis.project.cdg.CmpTssMethy中使用
 * @author zong0jie
 * 
 */
public class GffChrCmpBed extends GffChrAbs {
	public static void main(String[] args) {
		
	}
	
	
	MapReads mapReads2;

	/**
	 * @param readsFile
	 *            mapping的结果文件，必须排过序，一般为bed格式
	 * @param binNum
	 *            每隔多少位计数，如果设定为1，则算法会变化，然后会很精确
	 */
	public void setMapReadsCmp(String readsFile2, int binNum) {
		if (FileOperate.isFileExist(readsFile2)) {
			mapReads2 = new MapReads();
			mapReads2.setBedSeq(readsFile2);
			mapReads2.setInvNum(binNum);
			mapReads2.setMapChrID2Len(super.species.getMapChromInfo());
//			mapReads2.setNormalType(mapNormType);
			mapReads2.running();
		}
	}

	/**
	 * 比较两个实验中某个基因的TSS区域reads数量变化情况
	 * @param txtExcelFile
	 * @param colAccID
	 * @param region 类似 int[]{-2000, 2000}
	 * @param filterValue
	 * @param bigThan 大于阈值还是小于阈值
	 * @param txtOutFile
	 */
	public void readGeneList(String txtExcelFile, int colAccID,
			int[] region, double filterValue, boolean bigThan, String txtOutFile) {
		colAccID--;
		TxtReadandWrite txtOut = new TxtReadandWrite(txtOutFile, true);
		ArrayList<String[]> lsResult = ExcelTxtRead.readLsExcelTxt(txtExcelFile, 1);
		//没有读去title
		List<String[]> lsTmp = lsResult.subList(1, lsResult.size());
		for (String[] strings : lsTmp) {
			String geneID = strings[colAccID].split("/")[0];
			double tmpCmpResult = compRegionTssXLY(geneID, region);
			if ((bigThan && tmpCmpResult < filterValue)
					||
				(!bigThan && tmpCmpResult > filterValue)
			) {
				continue;
			}
			String[] tmpResult = ArrayOperate.copyArray(strings,
					strings.length + 1);
			tmpResult[tmpResult.length - 1] = tmpCmpResult + "";
			txtOut.writefileln(tmpResult);
		}
		txtOut.close();
	}

	/**
	 * 直接Tss区域进行比较，设定TSS前后范围 提供比值
	 * @param geneID
	 * @param region 比较区域
	 * @return
	 */
	private double compRegionTssXLY(String geneID, int[] region) {
		ArrayList<int[]> lsDectect = new ArrayList<int[]>();
		lsDectect.add(region);
		GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(geneID);
		ArrayList<int[]> lsTmpTss = gffGeneIsoInfo.getRegionNearTss(lsDectect);
		return compRegion(gffGeneIsoInfo.getRefID(), lsTmpTss)[0];
	}

	/**
	 * 比较指定区域的reads的均值
	 * 
	 * @param arrayList
	 *            -int[2]，有几个int[2]，就比较几个区域
	 * @param filterRegion
	 *            阈值，比较的区域内至少有多少区域超过阈值才认为通过
	 * @param filterValue
	 *            阈值，每个区域的mean reads至少超过该值才认为通过
	 * @return 返回比较区域的结果，每个区域一个比值
	 */
	private double[] compRegion(String chrID, List<int[]> lsCmpRegion) {
		double[] result = new double[lsCmpRegion.size()];
		for (int i = 0; i < lsCmpRegion.size(); i++) {
			int[] is = lsCmpRegion.get(i);
			MapInfo mapInfo = new MapInfo(chrID, is[0], is[1]);
//			MapReads.CmpMapReg(mapReads, mapReads2, mapInfo);
			result[i] = mapInfo.getScore();
		}
		return result;
	}

}
