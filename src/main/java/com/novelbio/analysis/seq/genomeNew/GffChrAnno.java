package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.generalConf.NovelBioConst;

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
		gffChrAnno.annoFile(txtFile, 1, 2, 3, out);
	}
	
	/**
	 * 首先设定需要注释的区域，如tss，tes，genebody等
	 * 给定txt的文件，和染色体编号，染色体起点终点，和输出文件，将peak覆盖到的区域注释出来
	 * @param txtFile
	 * @param colChrID
	 * @param colStart
	 * @param colEnd
	 * @param outTxtFile
	 */
	public void annoFile(String txtFile, int colChrID, int colStart, int colEnd, String outTxtFile) {
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
			if (lsanno == null) {
				continue;
			}
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
		if (gffCodGeneDu == null) {
			return null;
		}
		ArrayList<String[]> lsAnno = gffCodGeneDu.getAnno(tss, tes, genebody, UTR5, UTR3, exonFilter, intronFilter);
		return lsAnno;
	}

	
	
	/**
	 * 给定txt的文件，和染色体编号，染色体起点终点，和输出文件，将peak覆盖到的区域注释出来
	 * @param txtFile
	 * @param colChrID
	 * @param colStart
	 * @param colEnd
	 * @param outTxtFile
	 */
	public void getSummitStatistic(String txtFile, int colChrID, int colSummit, int rowStart, String outTxtFile) {
		ArrayList<String[]> lsIn = ExcelTxtRead.readLsExcelTxt(txtFile, new int[]{colChrID, colSummit}, rowStart, 0);
		ArrayList<MapInfo> lsTmpMapInfos = ReadInfo(lsIn);
		int[] region = getStatisticInfo(lsTmpMapInfos);
		TxtReadandWrite txtOut = new TxtReadandWrite(outTxtFile, true);
		txtOut.writefileln("Up" + tssUpBp +"bp\t"+region[0]);
		txtOut.writefileln("Exon\t"+region[1]);
		txtOut.writefileln("Intron\t"+region[2]);
		txtOut.writefileln("InterGenic\t"+region[3]);
		txtOut.writefileln("5UTR\t"+region[4]);
		txtOut.writefileln("3UTR\t"+region[5]);
		txtOut.writefileln("GeneEnd"+geneEnd3UTR+"\t"+region[6]);
		txtOut.writefileln("Tss\t"+region[7]);
		txtOut.close();
	}
	/**
	 * 给定坐标信息list，返回该坐标所对应的mapinfo
	 * @param lsIn  string[2] 则返回 chrID summit
	 * string[3] 则返回chrID start end
	 * @return
	 */
	protected ArrayList<MapInfo> ReadInfo(ArrayList<String[]> lsIn)
	{
		ArrayList<MapInfo> lsResult = new ArrayList<MapInfo>();
		for (String[] strings : lsIn) {
			MapInfo mapInfo = new MapInfo(strings[0]);
			if (strings.length == 2) {
				mapInfo.setFlagLoc(Integer.parseInt(strings[1].trim()));
			}
			else if (strings.length == 3) {
				int tmpStart = Integer.parseInt(strings[1].trim());
				int tmpEnd = Integer.parseInt(strings[2].trim());
				mapInfo.setStartLoc(Math.min(tmpStart, tmpEnd));
				mapInfo.setEndLoc(Math.max(tmpStart, tmpEnd));
			}
			else {
				String tmp = "";
				for (String string : strings) {
					tmp = tmp + "\t" + string;
				}
				logger.error("出现未知ID："+ tmp.trim());
			}
			lsResult.add(mapInfo);
		}
		return lsResult;
	}
	
	
	/**
	 * 输入单个坐标位点，返回定位信息，用于统计位点的定位情况,如外显子还是内含子
	 * 只判断最长转录本
	 * @param mapInfo
	 * @param summit true：用flagSite进行定位，false：用两端进行定位
	 * @return int[8]
	 * 0: UpNbp,N由setStatistic()方法的TSS定义
	 * 1: Exon<br>
	 * 2: Intron<br>
	 * 3: InterGenic--基因间<br>
	 * 4: 5UTR
	 * 5: 3UTR
	 * 6: GeneEnd，在基因外的尾部 由setStatistic()方法的GeneEnd定义
	 * 7: Tss 包括Tss上和Tss下，由filterTss定义
	 */
	public int[] getStatisticInfo(ArrayList<MapInfo> lsMapInfos) {
		int[] result = new int[8];
		for (MapInfo mapInfo : lsMapInfos) {
			int[] tmp = searchSite(mapInfo);
			for (int i = 0; i < tmp.length; i++) {
				result[i] = result[i] + tmp[i];
			}
		}
		return result;
	}
	
	
	
	/**
	 * 输入单个坐标位点，返回定位信息，用于统计位点的定位情况
	 * 只判断最长转录本
	 * @param mapInfo
	 * @return int[8]
	 * 0: UpNbp,N由setStatistic()方法的TSS定义
	 * 1: Exon<br>
	 * 2: Intron<br>
	 * 3: InterGenic--基因间<br>
	 * 4: 5UTR
	 * 5: 3UTR
	 * 6: GeneEnd，在基因外的尾部 由setStatistic()方法的GeneEnd定义
	 * 7: Tss 包括Tss上和Tss下，由filterTss定义
	 */
	private int[] searchSite(MapInfo mapInfo)
	{
		boolean flagIntraGenic = false;//在gene内的标记
		int[] result = new int[8];
		GffCodGene gffCodGene = gffHashGene.searchLocation(mapInfo.getChrID(), mapInfo.getFlagSite());
		if (gffCodGene.isInsideLoc()) {
			flagIntraGenic = true;
			//Tss
			if (gffCodGene.getGffDetailThis().getLongestSplit().getCod2Tss() < this.tss[1]) {
				result[7] ++;
			}
			//Exon
			if (gffCodGene.getGffDetailThis().getLongestSplit().getCodLoc() == GffGeneIsoInfo.COD_LOC_EXON) {
				result[1] ++;
			}
			else if (gffCodGene.getGffDetailThis().getLongestSplit().getCodLoc() == GffGeneIsoInfo.COD_LOC_INTRON) {
				result[2] ++;
			}
			//UTR
			if (gffCodGene.getGffDetailThis().getLongestSplit().getCodLoc() == GffGeneIsoInfo.COD_LOCUTR_5UTR) {
				result[4] ++;
			}
			if (gffCodGene.getGffDetailThis().getLongestSplit().getCodLoc() == GffGeneIsoInfo.COD_LOCUTR_3UTR) {
				result[5] ++;
			}
		}
		else {
			//UpNbp
			if (gffCodGene.getGffDetailUp() != null && gffCodGene.getGffDetailUp().isCodInPromoter()) {
				result[0]++;flagIntraGenic =true;
			}
			else if (gffCodGene.getGffDetailDown() != null && gffCodGene.getGffDetailDown().isCodInPromoter()) {
				result[0] ++;flagIntraGenic =true;
			}
			//GeneEnd
			if (gffCodGene.getGffDetailUp() != null && gffCodGene.getGffDetailUp().isCodInGenEnd()) {
				result[6] ++;flagIntraGenic =true;
			}
			else if ( gffCodGene.getGffDetailDown() != null && gffCodGene.getGffDetailDown().isCodInGenEnd()) {
				result[6] ++;flagIntraGenic =true;
			}
			//Tss
			if ( gffCodGene.getGffDetailUp() != null && !gffCodGene.getGffDetailUp().isCis5to3() && gffCodGene.getGffDetailUp().getLongestSplit().getCod2Tss() > this.tss[0]  ) {
				result[7] ++;flagIntraGenic =true;
			}
			else if (gffCodGene.getGffDetailDown() != null && gffCodGene.getGffDetailDown().isCis5to3() && gffCodGene.getGffDetailDown().getLongestSplit().getCod2Tss() > this.tss[0]) {
				result[7] ++;flagIntraGenic =true;
			}
		}
		if (flagIntraGenic == false) {
			result[3] ++;
		}
		return result;
	}
	
	
}

/**
 * 基因定位情况
 * @author zong0jie
 *
 */
class siteLocInfo
{
	/**
	 * 是否在不包含promoter的外显子中
	 */
	int ExonWithOutPromoter = 0;
	/**
	 * 是否在不包含promoter的内含子中
	 */
	int IntronWithOutPromoter = 0;
	/**
	 * 是否在genebody中
	 */
	int geneBody = 0;
	/**
	 * 是否在基因外的Promoter中
	 */
	int PromoterOutGene = 0;
	/**
	 * 是否在基因内的Promoter中
	 */
	int PromoterInGene = 0;
	/**
	 * 是否在不包含promoter的外显子中
	 */
	int InterGenic = 0;
	/**
	 * 是否在5‘UTR中
	 */
	int UTR5 = 0;
	/**
	 * 是否在3‘UTR中
	 */
	int UTR3 = 0;
}




