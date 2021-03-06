package com.novelbio.bioinfo.gffchr;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.ExceptionNbcFile;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.bioinfo.annotation.genanno.AnnoQuery.AnnoQueryDisplayInfo;
import com.novelbio.bioinfo.gff.GffCodGene;
import com.novelbio.bioinfo.gff.GffCodGeneDU;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.database.domain.modgeneid.GeneID;
import com.novelbio.database.domain.species.Species;
import com.novelbio.database.service.servgeneanno.ManageSpecies;
import com.novelbio.database.service.servgeneanno.ManageSpeciesDB;

/**
 * 主要做基因定位的工作
 * @author zong0jie
 *
 */
public class GffChrAnno extends RunProcess {
	private static final Logger logger = LoggerFactory.getLogger(GffChrAnno.class);
	
	GffChrAbs gffChrAbs;
	/** true查找peak的最高点，也就是找单个点，
	 * false查找peak两端，看夹住了什么基因
	 *  */
	boolean searchSummit = false;
	
	String txtExcel = "";
	int colChrID = 0;
	int colStart = 0;
	int colEnd = 0;
	int colSummit = -1;
	
	int[] tss = new int[]{-1500, 1500};
	int[] tes = null;
	boolean genebody = false;
	boolean UTR5 = false;
	boolean UTR3 = false;
	boolean exonFilter = false;
	boolean intronFilter = false;
	boolean filtertss = false;
	boolean filtertes = false;
	/** 是否仅提取lnc */
	boolean isOnlyGetLnc = false;
	
	List<String[]> lsGeneInfo = new ArrayList<String[]>();
	List<String[]> lsResult = new ArrayList<String[]>();
	
	public GffChrAnno() {
		// TODO Auto-generated constructor stub
	}
	
	public GffChrAnno(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public void setSpecies(int taxID) {
		this.gffChrAbs = new GffChrAbs(taxID);
	}
	public void setSpecies(Species species) {
		this.gffChrAbs = new GffChrAbs(species);
	}
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public GffChrAbs getGffChrAbs() {
		return gffChrAbs;
	}
	public void setColStartEnd(int colStart, int colEnd) {
		this.colStart = colStart - 1;
		this.colEnd = colEnd - 1;
	}
	/** 实际列 */
	public void setColChrID(int colChrID) {
		this.colChrID = colChrID - 1;
	}
	/** 实际列 */
	public void setColSummit(int colSummit) {
		this.colSummit = colSummit - 1;
	}
	/** true查找peak的最高点，也就是找单个点，
	 * false查找peak两端，看夹住了什么基因
	 * 默认false
	 */
	public void setSearchSummit(boolean searchSummit) {
		this.searchSummit = searchSummit;
	}
	public void setLsGeneInfo(List<String[]> lsGeneInfo) {
		this.lsGeneInfo = lsGeneInfo;
	}
	/**
	 * @param tss 默认 -1500 到 1500
	 */
	public void setTss(int[] tss) {
		this.filtertss = true;
		this.tss = tss;
	}
	public void setTes(int[] tes) {
		this.filtertes = true;
		this.tes = tes;
	}
	/**
	 * @param genebody 是否注释genebody，默认是false
	 * @param exonFilter 当genebody为false时起作用，表示是否注释exon
	 * @param intronFilter 当genebody为false时起作用，表示是否注释intron
	 */
	public void setFilterGeneBody(boolean genebody, boolean exonFilter, boolean intronFilter) {
		this.genebody = genebody;
		this.exonFilter = exonFilter;
		this.intronFilter = intronFilter;
	}
	public void setFilterUTR(boolean utr5, boolean utr3) {
		this.UTR5 = utr5;
		this.UTR3 = utr3;
	}
	/** 默认false */
	public void setFiltertss(boolean filtertss) {
		this.filtertss = filtertss;
	}
	/** 默认false */
	public void setFiltertes(boolean filtertes) {
		this.filtertes = filtertes;
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
	public void annoFileExcel(String txtFile, String outTxtFile) {
		this.flagStop = false;
		this.lsGeneInfo = ExcelTxtRead.readLsExcelTxt(txtFile, 1);
		
		String outTmp = FileOperate.changeFileSuffix(outTxtFile, ".tmp", null);

		TxtReadandWrite txtOut = new TxtReadandWrite(outTmp, true);

		List<String[]> lsGeneInfoTmp = lsGeneInfo.subList(1, lsGeneInfo.size());
		txtOut.writefileln(getTitleGeneInfoFilterAnno(lsGeneInfo.get(0)));
		int i = 1;
		for (String[] strings : lsGeneInfoTmp) {
			i++;
			ArrayList<String[]> lsTmpResult = getGeneInfoAnno(strings, i);
			for (String[] strings2 : lsTmpResult) {
				txtOut.writefileln(strings2);
			}			
			suspendCheck();
		}
		txtOut.close();
		FileOperate.moveFile(true, outTmp, outTxtFile);
		this.flagStop = true;
	}
	
	/**
	 * 给定list，返回注释好信息的list，包含title
	 * @param lsInfo 第一行是标题行
	 * @param colChrID 实际列
	 * @param colStart 实际列
	 * @param colEnd 实际列
	 */
	public void annoFileTxt(String txtFile, String outTxtFile) {
		this.flagStop = false;
		TxtReadandWrite txtRead = new TxtReadandWrite(txtFile);
		String title = txtRead.readFirstLine();
		
		String outTmp = FileOperate.changeFileSuffix(outTxtFile, ".tmp", null);
		
		TxtReadandWrite txtOut = new TxtReadandWrite(outTmp, true);
		txtOut.writefileln(getTitleGeneInfoFilterAnno(title.split("\t")));
		int i = 1;
		for (String content : txtRead.readlines(2)) {
			i++;
			ArrayList<String[]> lsTmpResult = getGeneInfoAnno(content.split("\t"), i);
			for (String[] strings2 : lsTmpResult) {
				txtOut.writefileln(strings2);
			}			
			suspendCheck();
		}
		txtRead.close();
		txtOut.close();
		FileOperate.moveFile(true, outTmp, outTxtFile);
		this.flagStop = true;
	}
	
	@Override
	protected void running() {
		lsResult = getAnno();
	}
	public List<String[]> getLsResult() {
		return lsResult;
	}

	
	/**
	 * 给定list，返回注释好信息的list，包含title
	 * @param lsInfo 第一行是标题行
	 * @param colChrID 实际列
	 * @param colStart 实际列
	 * @param colEnd 实际列
	 */
	public ArrayList<String[]> getAnno() {
		List<String[]> lsGeneInfoTmp = lsGeneInfo.subList(1, lsGeneInfo.size());
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(0,getTitleGeneInfoFilterAnno(lsGeneInfo.get(0)));
		
		int count = 1;
		for (String[] strings : lsGeneInfoTmp) {
			count++;
			ArrayList<String[]> lsTmpResult = getGeneInfoAnno(strings, count);
			lsResult.addAll(lsTmpResult);
			
			suspendCheck();
			if (flagStop) return lsResult;
			for (String[] strings2 : lsTmpResult) {
				AnnoQueryDisplayInfo annoQueryDisplayInfo = new AnnoQueryDisplayInfo();
				annoQueryDisplayInfo.setCountNum(count);
				annoQueryDisplayInfo.setTmpInfo(strings2);
				setRunInfo(annoQueryDisplayInfo);
			}
		}
		return lsResult;
	}
	
	@Deprecated
	public String[] getTitleGeneInfoFilterAnno() {
		List<String> lsTitle = new ArrayList<>();
		for (String string : lsGeneInfo.get(0)) {
			lsTitle.add(string);
		}
		lsTitle.add("AccID");
		if (ManageSpecies.getInstance() instanceof ManageSpeciesDB) {
			lsTitle.add("Symbol");
			lsTitle.add("Description");
		}

		lsTitle.add("Location");
		return lsTitle.toArray(new String[0]);
	}
	
	public static String[] getTitleGeneInfoFilterAnno(String[] title) {
		List<String> lsTitle = new ArrayList<>();
		for (String string : title) {
			lsTitle.add(string);
		}
		lsTitle.add("AccID");
		if (ManageSpecies.getInstance() instanceof ManageSpeciesDB) {
			lsTitle.add("Symbol");
			lsTitle.add("Description");
		}

		lsTitle.add("Location");
		return lsTitle.toArray(new String[0]);
	}
	
	/**
	 * 给定一个基因定位文件的一行，返回注释信息
	 * @param geneLocInfo
	 * @return
	 */
	private ArrayList<String[] > getGeneInfoAnno(String[] geneLocInfo, int lineNum) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		String chrID = null;
		try {
			chrID = geneLocInfo[colChrID];
		} catch (Exception e) {
			return lsResult;
		}
		int start = 0, end = 0, summit = 0;
		if (! searchSummit) {
			try {
				start =  (int)Double.parseDouble(geneLocInfo[colStart]);
				end =  (int)Double.parseDouble(geneLocInfo[colEnd]);
				
			} catch (Exception e) { 	
				logger.warn("summit col contains wrong value, omit this line:" + geneLocInfo[colStart] + " " + geneLocInfo[colEnd]);
				return lsResult;
			}
		} else {
			try {
				summit = colSummit >= 0 ?  (int)Double.parseDouble(geneLocInfo[colSummit]) : ((start + end)/2);
			} catch (Exception e) {
				logger.error("line {} has wrong summit value, omit this line:" + ArrayOperate.cmbString(geneLocInfo, "\t"), lineNum + "");
				throw new ExceptionNbcFile("line " + lineNum + "has wrong summit value: " + ArrayOperate.cmbString(geneLocInfo, "\t"));
			}
		}
	
		ArrayList<String[]> lsanno = null;
		if (searchSummit) {
			lsanno = getGenInfoFilterSummitSingle(chrID, summit);
		} else {
			lsanno = getGenInfoFilterPeakSingle(chrID, start, end);
		}
		if (lsanno == null) {
			return lsResult;
		}
		for (String[] strings2 : lsanno) {
			String[] tmpResult = new String[geneLocInfo.length + strings2.length];
			for (int i = 0; i < geneLocInfo.length; i++) {
				tmpResult[i] = geneLocInfo[i];
			}
			for (int i = 0; i < strings2.length; i++) {
				tmpResult[i+geneLocInfo.length] = strings2[i];
			}
			lsResult.add(tmpResult);
		}
		return lsResult;
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
	private ArrayList<String[]> getGenInfoFilterPeakSingle(String chrID, int startCod, int endCod) {
		GffCodGeneDU gffCodGeneDu = gffChrAbs.getGffHashGene().searchLocation(chrID, startCod, endCod);

		if (gffCodGeneDu == null) {
			return null;
		}
		ArrayList<String[]> lsAnno = null;
		gffCodGeneDu.setExon(exonFilter);
		gffCodGeneDu.setGeneBody(genebody);
		gffCodGeneDu.setIntron(intronFilter);
		gffCodGeneDu.setTes(tes);
		gffCodGeneDu.setTss(tss);
		gffCodGeneDu.setUTR3(UTR3);
		gffCodGeneDu.setUTR5(UTR5);
		try {
			lsAnno = gffCodGeneDu.getAnno();

		} catch (Exception e) {
			logger.error(chrID + " " + startCod, e);
		}
		return lsAnno;
	}
	/**
	 * 单个坐标的中间位点定位
	 * 给定染色体位置和坐标，返回注释信息
	 * @param chrID
	 * @param summit
	 * @return
	 */
	private ArrayList<String[]> getGenInfoFilterSummitSingle(String chrID, int summit) {
		ArrayList<String[]> lsResultAnno = new ArrayList<String[]>();
		GffCodGene gffCodGene = gffChrAbs.getGffHashGene().searchLocation(chrID, summit);
		if (gffCodGene == null) {
			return lsResultAnno;
		}
		//在上一个gene内
		if (gffCodGene.getAlignUp() != null) {
			getAnnoLocSumit(lsResultAnno, gffCodGene.getAlignUp(), summit);
		}
		if (gffCodGene.getAlignThis() != null) {
			getAnnoLocSumit(lsResultAnno, gffCodGene.getAlignThis(), summit);
		}
		if (gffCodGene.getAlignDown() != null) {
			getAnnoLocSumit(lsResultAnno, gffCodGene.getAlignDown(), summit);
		}
		return lsResultAnno;
	}
	/**
	 * 注释
	 * @param gffGeneIsoInfoCis
	 * @param coord
	 * @return 0：symbol
	 * 1：description
	 * blast ：2 evalue 3 symol 4 description
	 * location
	 */
	private void getAnnoLocSumit(ArrayList<String[]> lsAnno, GffGene gffDetailGene, int coord) {
		GffIso gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
		if (!gffGeneIsoInfo.isCodLocFilter(coord, filtertss, tss, filtertes, tes, genebody, UTR5, UTR3, exonFilter, intronFilter)) {
			return;
		}
		List<String> lsTmpAnno = new ArrayList<String>();
		lsTmpAnno.add(gffGeneIsoInfo.getName());
		
		if (ManageSpecies.getInstance() instanceof ManageSpeciesDB) {
			GeneID geneID = new GeneID(gffGeneIsoInfo.getName(), gffDetailGene.getTaxID());
			lsTmpAnno.add(geneID.getSymbol());
			lsTmpAnno.add(geneID.getDescription());
		}
		lsTmpAnno.add(gffGeneIsoInfo.toStringCodLocStr(tss, coord));

		lsAnno.add(lsTmpAnno.toArray(new String[0]));
	}

}

/**
 * 基因定位情况
 * @author zong0jie
 *
 */
class siteLocInfo {
	/** 是否在不包含promoter的外显子中 */
	int ExonWithOutPromoter = 0;
	/** 是否在不包含promoter的内含子中 */
	int IntronWithOutPromoter = 0;
	/** 是否在genebody中 */
	int geneBody = 0;
	/** 是否在基因外的Promoter中 */
	int PromoterOutGene = 0;
	/** 是否在基因内的Promoter中 */
	int PromoterInGene = 0;
	/** 是否在不包含promoter的外显子中 */
	int InterGenic = 0;
	/** 是否在5‘UTR中 */
	int UTR5 = 0;
	/** 是否在3‘UTR中 */
	int UTR3 = 0;
}




