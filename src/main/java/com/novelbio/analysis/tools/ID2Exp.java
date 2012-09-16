package com.novelbio.analysis.tools;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffHashGene;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.updatedb.database.MicroArrayBlast;
import com.novelbio.generalConf.NovelBioConst;

/**
 * ����excel�еĵ�һ��ID��ͬһ��cell�е�ID�á�//��������ͬʱ�ָ���microarray�ļ�����microarray��ֵ������ȥ�����Ҽ��ϸû����tss����
 * @author zong0jie
 */
public class ID2Exp {
	
	public static void main(String[] args) {
		ID2Exp id2Exp = new ID2Exp();
		id2Exp.addTss();
	}
	
	GffHashGene gffHashGene = new GffHashGene(NovelBioConst.GENOME_GFF_TYPE_UCSC, NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ);
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
	 * ��ȡmicroarray����
	 * @param geneExpInfo ���������
	 * @param colCondition �ļ������� 1�� geneID ֮��gene�����
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
	 * ����������Ϣmerge��һ��
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
					String[] tssInfo = new String[]{gffGeneIsoInfo.getChrID(), gffGeneIsoInfo.getTSSsite() - range + "", gffGeneIsoInfo.getTSSsite() + range + ""};
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
