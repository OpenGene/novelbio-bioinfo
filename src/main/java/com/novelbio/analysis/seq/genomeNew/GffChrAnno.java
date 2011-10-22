package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.generalConf.NovelBioConst;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;

/**
 * 主要做基因定位的工作
 * @author zong0jie
 *
 */
public class GffChrAnno extends GffChrAbs{
	private static final Logger logger = Logger.getLogger(GffChrAnno.class);
	public GffChrAnno(String gffType, String gffFile, String chrFile,String regx) {
		super(gffType, gffFile, chrFile, null, 0);
	}
	public GffChrAnno(String gffType, String gffFile) {
		super(gffType, gffFile, null, null,null, 0);
	}
	
	
	int[] tss = new int[]{-1500, 1500};
	int[] tes = null;
	boolean genebody = false;
	boolean UTR5 = false;
	boolean UTR3 = false;
	boolean exonFilter = false;
	boolean intronFilter = false;
	
	public void setFilterTssTes(int[] filterTss, int[] filterGenEnd) {
		this.tss = filterTss;
		this.tes = filterGenEnd;

	}
	public void setFilterGeneBody(boolean filterGeneBody, boolean filterExon, boolean filterIntron)
	{
		this.genebody = filterGeneBody;
		this.exonFilter = filterExon;
		this.intronFilter = filterIntron;
	}
	
	public void setFilterUTR(boolean filter5UTR, boolean filter3UTR)
	{
		this.UTR5 = filter5UTR;
		this.UTR3 = filter3UTR;
	}
	
	
	public static void main(String[] args) throws Exception {
		GffChrAnno gffChrAnno = new GffChrAnno(NovelBioConst.GENOME_GFF_TYPE_UCSC, 
				NovelBioConst.GENOME_PATH_UCSC_MM9_GFF_REFSEQ);
		String parentFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/result/PeakCalling/";
		String txtFile = parentFile + "2KseSort-W200-G600-E100.scoreisland";
		String out = parentFile + "2KSICERanno.txt";
		gffChrAnno.annotation(txtFile, 1, 2, 3, out);
	}
	
	
	
	
	
	
	
	
	
	public void annotation(String txtFile, int colChrID, int colStart, int colEnd, String outTxtFile) {
//		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(txtFile, false);
//		ArrayList<String[]> lsIn = txtReadandWrite.ExcelRead("\t", 1, 1, txtReadandWrite.ExcelRows(), -1, 0);
		ArrayList<String[]> lsIn = ExcelTxtRead.readLsExcelTxt(txtFile, 1);
		ArrayList<String[]> lsOut = getAnno(lsIn, colChrID, colStart, colEnd);
		TxtReadandWrite txtOut = new TxtReadandWrite(outTxtFile, true);
		txtOut.ExcelWrite(lsOut, "\t", 1, 1);
	}

	/**
	 * 给定list，返回注释好信息的list，包含title
	 * @param lsInfo 第一行是标题行
	 * @param colChrID 实际列
	 * @param colStart 实际列
	 * @param colEnd 实际列
	 */
	public ArrayList<String[]> getAnno(ArrayList<String[]> lsInfo, int colChrID, int colStart, int colEnd) {
		String[] titleOld = lsInfo.get(0);
		lsInfo.remove(0);
		colChrID--; colStart--; colEnd--;
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		
		for (String[] strings : lsInfo) {
			ArrayList<String[]> lsanno = getGenInfoFilter(strings[colChrID], Integer.parseInt(strings[colStart]), Integer.parseInt(strings[colEnd]));
			for (String[] strings2 : lsanno) {
				String[] tmpResult = new String[strings.length + strings2.length];
				for (int i = 0; i < strings.length; i++) {
					tmpResult[i] = strings[i];
				}
				for (int i = 0; i < strings2.length; i++) {
					tmpResult[i+strings.length] = strings2[i];
				}
				lsResult.add(tmpResult);
			}
		}
		//添加title
		String[] title = ArrayOperate.copyArray(titleOld, titleOld.length + 4);
		title[title.length - 1] = "Location"; title[title.length - 2] = "Description"; title[title.length - 3] = "Symbol"; title[title.length - 4] = "AccID";
		lsResult.add(0,title);
		return lsResult;
	}
	
	/**
	 * 给定染色体位置和坐标，返回注释信息
	 * @param chrID
	 * @param summit
	 * @return
	 */
	public String[][] getGenInfo(String chrID, int summit) {
		String[][] anno = new String[3][4];
		for (int i = 0; i < anno.length; i++) {
			for (int j = 0; j < anno[0].length; j++) {
				anno[i][j] = "";
			}
		}
		GffCodGene gffCodGene = gffHashGene.searchLocation(chrID, summit);
		if (gffCodGene == null) {
			return anno;
		}
		//在上一个gene内
		if (gffCodGene.getGffDetailUp() != null) {
			anno[0] = gffCodGene.getGffDetailUp().getInfo();
		}
		if (gffCodGene.getGffDetailThis() != null) {
			anno[1] = gffCodGene.getGffDetailThis().getInfo();
		}
		if (gffCodGene.getGffDetailDown() != null) {
			anno[2] = gffCodGene.getGffDetailDown().getInfo();
		}
		return anno;
	}
	
	public String[] getGenInfoFilter(String chrID, int summit) {
		String[] anno = new String[3];
		GffCodGene gffCodGene = gffHashGene.searchLocation(chrID, summit);
		if (gffCodGene.isInsideLoc()) {
			anno[1] = gffCodGene.getGffDetailThis().getLongestSplit().getCodLocStrFilter(tss, tes, genebody, UTR5, UTR3, exonFilter, intronFilter);
		}
		return anno;
	}
	/**
	 * peak注释
	 * @param chrID
	 * @param startCod
	 * @param endCod
	 * @return
	 * 0：accID<br>
	 * 1：symbol<br>
	 * 2：description<br>
	 * 3：两端是具体信息，中间是covered
	 */
	public ArrayList<String[]> getGenInfoFilter(String chrID, int startCod, int endCod) {
		GffCodGeneDU gffCodGeneDu = gffHashGene.searchLocation(chrID, startCod, endCod);
		ArrayList<String[]> lsAnno = gffCodGeneDu.getAnno(tss, tes, genebody, UTR5, UTR3, exonFilter, intronFilter);
		return lsAnno;
	}

}
