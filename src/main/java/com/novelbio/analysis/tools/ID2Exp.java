package com.novelbio.analysis.tools;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.seq.genome.gffoperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffoperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffoperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffoperate.GffType;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.updatedb.database.MicroArrayBlast;

/**
 * 给定excel中的第一列ID，同一个cell中的ID用“//”隔开，同时又给定microarray文件，将microarray的值附加上去，并且加上该基因的tss坐标
 * @author zong0jie
 */
public class ID2Exp {
	
	public static void main(String[] args) {
		ID2Exp id2Exp = new ID2Exp();
		id2Exp.addTss();
	}
	
	GffHashGene gffHashGene = new GffHashGene();
	ArrayList<String[]> lsMethyInfo = new ArrayList<String[]>();
	ArrayList<String[]> lsMicroarrayInfo = new ArrayList<String[]>();
	int taxID = 10090;
	int range = 2000;
	public void setTaxID(int taxID) {
		this.taxID = taxID;
	}
	private void readMethy(String txtExcel) {
		lsMethyInfo = ExcelTxtRead.readLsExcelTxt(txtExcel, 0);	
	}
	
	/**
	 * 读取microarray数据
	 * @param geneExpInfo 表达谱数据
	 * @param colCondition 哪几列数据 1： geneID 之后，gene表达量
	 */
	private void readGeneExpTxt(String geneExpInfo, int[] colCondition) {
		lsMicroarrayInfo = ExcelTxtRead.readLsExcelTxt(geneExpInfo, colCondition, 1, -1);	
	}
	public void addTss() {
		String methyFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/allAnnotation/combineInfo.txt";
		String geneExpFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/expression/TKO-D4vsFH-D4_median.xls";
		int[] colCondition = new int[]{10,2,4}; 
		String outFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/allAnnotation/combineInfo_Tss.txt";
		readMethy(methyFile);
		readGeneExpTxt(geneExpFile, colCondition);
		ArrayList<String[]> lsResult = addTssInfo();
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		txtOut.ExcelWrite(lsResult);
	}
	public void combInfo() {
		String methyFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/allAnnotation/XLYall_table.txt";
		String geneExpFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/expression/TKO-D4vsFH-D4_median.xls";
		int[] colCondition = new int[]{10,2,4}; 
		String outFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/rawdata/allAnnotation/combineInfo.txt";
		readMethy(methyFile);
		readGeneExpTxt(geneExpFile, colCondition);
		ArrayList<String[]> lsResult = mergeInfo();
		TxtReadandWrite txtOut = new TxtReadandWrite(outFile, true);
		txtOut.ExcelWrite(lsResult);
	}
	/**
	 * 将给定的信息merge在一起
	 */
	private ArrayList<String[]> mergeInfo() {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		HashMap<GeneID, String[]> hashGene2Exp = new HashMap<GeneID, String[]>();
		for (String[] strings : lsMicroarrayInfo) {
			GeneID copedID = new GeneID(strings[0], taxID);
			hashGene2Exp.put(copedID, strings);
		}
		for (String[] strings : lsMethyInfo) {
			String[] ssID = strings[0].split("///");
			boolean flag = false;
			for (String string : ssID) {
				GeneID copedID = new GeneID(string, taxID);
				if (hashGene2Exp.containsKey(copedID)) {
					flag = true;
					String[] tmpResult = ArrayOperate.combArray(strings, hashGene2Exp.get(copedID), 0);
					lsResult.add(tmpResult);
					break;
				}
			}
			if (!flag) {
				String[] tmpResult = ArrayOperate.combArray(strings, new String[]{strings[0],"none","none"}, 0);
				lsResult.add(tmpResult);
			}
		}
		return lsResult;
	}
	private ArrayList<String[]> addTssInfo() {
		ArrayList<String[]> lsInfo = new ArrayList<String[]>();
		for (String[] strings : lsMethyInfo) {
			String[] ssID = strings[0].split("///");
			boolean flag = false;
			for (String string : ssID) {
				GffGeneIsoInfo gffGeneIsoInfo = gffHashGene.searchISO(string);
				if (gffGeneIsoInfo != null) {
					String[] tssInfo = new String[]{gffGeneIsoInfo.getRefIDlowcase(), gffGeneIsoInfo.getTSSsite() - range + "", gffGeneIsoInfo.getTSSsite() + range + ""};
					String[] tmpResult = ArrayOperate.combArray(strings, tssInfo, 0);
					lsInfo.add(tmpResult);
					flag = true;
				}
			}
			if (!flag) {
				String[] tmpResult = ArrayOperate.combArray(strings, new String[]{strings[0],"none","none"}, 0);
				lsInfo.add(tmpResult);
			}
		}
		return lsInfo;
	}
}
