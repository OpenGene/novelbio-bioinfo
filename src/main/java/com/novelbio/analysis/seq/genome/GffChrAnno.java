package com.novelbio.analysis.seq.genome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.omg.CosNaming._BindingIteratorImplBase;

import antlr.debug.TraceAdapter;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery.AnnoQueryDisplayInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene.GeneStructure;
import com.novelbio.analysis.seq.genome.mappingOperate.MapInfo;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.modgeneid.GeneID;
import com.novelbio.database.model.species.Species;
import com.novelbio.generalConf.NovelBioConst;

/**
 * ��Ҫ������λ�Ĺ���
 * @author zong0jie
 *
 */
public class GffChrAnno extends RunProcess<AnnoQueryDisplayInfo> {
	private static final Logger logger = Logger.getLogger(GffChrAnno.class);
	
	public static void main(String[] args) {
		Species species = new Species(39947);
		GffChrAbs gffChrAbs = new GffChrAbs(species);
		GffChrAnno gffChrAnno = new GffChrAnno(gffChrAbs);
		gffChrAnno.setColChrID(1);
		gffChrAnno.setColStartEnd(2, 3);
		gffChrAnno.annoFile("/home/zong0jie/Desktop/PApeak_peaks_filtered1.txt", "/home/zong0jie/Desktop/PApeak_peaks_filteredannoates.txt");
	}
	
	
	GffChrAbs gffChrAbs;
	/** true����peak����ߵ㣬Ҳ�����ҵ����㣬
	 * false����peak���ˣ�����ס��ʲô����
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
	
	ArrayList<String[]> lsGeneInfo = new ArrayList<String[]>();
	ArrayList<String[]> lsResult = new ArrayList<String[]>();
	
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
		searchSummit = false;
	}
	
	public void setColChrID(int colChrID) {
		this.colChrID = colChrID - 1;
	}
	public void setColSummit(int colSummit) {
		this.colSummit = colSummit - 1;
		searchSummit = true;
	}
	/** true����peak����ߵ㣬Ҳ�����ҵ����㣬
	 * false����peak���ˣ�����ס��ʲô����
	 * Ĭ��false
	 *  */
	public void setSearchSummit(boolean searchSummit) {
		this.searchSummit = searchSummit;
	}
	public void setLsGeneInfo(ArrayList<String[]> lsGeneInfo) {
		this.lsGeneInfo = lsGeneInfo;
	}
	public void setTss(int[] tss) {
		this.filtertss = true;
		this.tss = tss;
	}
	public void setTes(int[] tes) {
		this.filtertes = true;
		this.tes = tes;
	}
	public void setFilterGeneBody(boolean genebody, boolean exonFilter, boolean intronFilter) {
		this.genebody = genebody;
		this.exonFilter = exonFilter;
		this.intronFilter = intronFilter;
	}
	public void setFilterUTR(boolean utr5, boolean utr3) {
		this.UTR5 = utr5;
		this.UTR3 = utr3;
	}
	public void setFiltertss(boolean filtertss) {
		this.filtertss = filtertss;
	}
	public void setFiltertes(boolean filtertes) {
		this.filtertes = filtertes;
	}
	
	/**
	 * �����趨��Ҫע�͵�������tss��tes��genebody��
	 * ����txt���ļ�����Ⱦɫ���ţ�Ⱦɫ������յ㣬������ļ�����peak���ǵ�������ע�ͳ���
	 * @param txtFile
	 * @param colChrID
	 * @param colStart
	 * @param colEnd
	 * @param outTxtFile
	 */
	public void annoFile(String txtFile, String outTxtFile) {
		this.lsGeneInfo = ExcelTxtRead.readLsExcelTxt(txtFile, 1);
		ArrayList<String[]> lsResult = getAnno();
		TxtReadandWrite txtOut = new TxtReadandWrite(outTxtFile, true);
		txtOut.ExcelWrite(lsResult);
	}
	
	@Override
	protected void running() {
		lsResult = getAnno();
	}
	public ArrayList<String[]> getLsResult() {
		return lsResult;
	}
	/**
	 * ����list������ע�ͺ���Ϣ��list������title
	 * @param lsInfo ��һ���Ǳ�����
	 * @param colChrID ʵ����
	 * @param colStart ʵ����
	 * @param colEnd ʵ����
	 */
	public ArrayList<String[]> getAnno() {
		List<String[]> lsGeneInfoTmp = lsGeneInfo.subList(1, lsGeneInfo.size());
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		lsResult.add(0,getTitleGeneInfoFilterAnno());
		
		int count = 0;
		for (String[] strings : lsGeneInfoTmp) {
			ArrayList<String[]> lsTmpResult = getGeneInfoAnno(strings);
			lsResult.addAll(lsTmpResult);
			
			count++;
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
	

	/**
	 * ����һ������λ�ļ���һ�У�����ע����Ϣ
	 * @param geneLocInfo
	 * @return
	 */
	private ArrayList<String[] > getGeneInfoAnno(String[] geneLocInfo) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		String chrID = geneLocInfo[colChrID];
		int start = 0, end = 0, summit = 0;
		try {
			start =  (int)Double.parseDouble(geneLocInfo[colStart]);
			end =  (int)Double.parseDouble(geneLocInfo[colEnd]);
		} catch (Exception e) { 	}
		try {
			summit =  (int)Double.parseDouble(geneLocInfo[colSummit]);
		} catch (Exception e) {
			summit = (start + end)/2;
		}
		ArrayList<String[]> lsanno = null;
		if (searchSummit) {
			lsanno = getGenInfoFilterSummitSingle(chrID, summit);
		}
		else {
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
	
	public String[] getTitleGeneInfoFilterAnno() {
		String[] titleOld = lsGeneInfo.get(0);
		//���title
		String[] title = ArrayOperate.copyArray(titleOld, titleOld.length + 4);
		title[title.length - 1] = "Location"; title[title.length - 2] = "Description"; title[title.length - 3] = "Symbol"; title[title.length - 4] = "AccID";
		return title;
	}
	/**
	 * peakע��
	 * @param chrID
	 * @param startCod
	 * @param endCod
	 * @return
	 * 0��accID<br>
	 * 1��symbol<br>
	 * 2��description<br>
	 * 3�������Ǿ�����Ϣ���м���covered
	 */
	private ArrayList<String[]> getGenInfoFilterPeakSingle(String chrID, int startCod, int endCod) {
		if (startCod == 5943970) {
			logger.error("stop");
		}
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
			logger.error(chrID + " " + startCod);
		}
		return lsAnno;
	}
	/**
	 * ����������м�λ�㶨λ
	 * ����Ⱦɫ��λ�ú����꣬����ע����Ϣ
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
		//����һ��gene��
		if (gffCodGene.getGffDetailUp() != null) {
			getAnnoLocSumit(lsResultAnno, gffCodGene.getGffDetailUp(), summit);
		}
		if (gffCodGene.getGffDetailThis() != null) {
			getAnnoLocSumit(lsResultAnno, gffCodGene.getGffDetailThis(), summit);
		}
		if (gffCodGene.getGffDetailDown() != null) {
			getAnnoLocSumit(lsResultAnno, gffCodGene.getGffDetailDown(), summit);
		}
		return lsResultAnno;
	}
	/**
	 * ע��
	 * @param gffGeneIsoInfo
	 * @param coord
	 * @return 0��symbol
	 * 1��description
	 * blast ��2 evalue 3 symol 4 description
	 * location
	 */
	private void getAnnoLocSumit(ArrayList<String[]> lsAnno, GffDetailGene gffDetailGene, int coord) {
		gffDetailGene.setTssRegion(tss);
		gffDetailGene.setTesRegion(tes);
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
		if (!gffGeneIsoInfo.isCodLocFilter(coord, filtertss, filtertes, genebody, UTR5, UTR3, exonFilter, intronFilter)) {
			return;
		}
		
		String[] tmpAnno = null;
		tmpAnno = new String[4];
		
		tmpAnno[0] = gffGeneIsoInfo.getName();
		GeneID geneID = gffGeneIsoInfo.getGeneID();
		tmpAnno[1] = geneID.getSymbol();
		tmpAnno[2] = geneID.getDescription();
		tmpAnno[3] = gffGeneIsoInfo.toStringCodLocStr(coord);
		
		lsAnno.add(tmpAnno);
	}

}

/**
 * ����λ���
 * @author zong0jie
 *
 */
class siteLocInfo {
	/** �Ƿ��ڲ�����promoter���������� */
	int ExonWithOutPromoter = 0;
	/** �Ƿ��ڲ�����promoter���ں����� */
	int IntronWithOutPromoter = 0;
	/** �Ƿ���genebody�� */
	int geneBody = 0;
	/** �Ƿ��ڻ������Promoter�� */
	int PromoterOutGene = 0;
	/** �Ƿ��ڻ����ڵ�Promoter�� */
	int PromoterInGene = 0;
	/** �Ƿ��ڲ�����promoter���������� */
	int InterGenic = 0;
	/** �Ƿ���5��UTR�� */
	int UTR5 = 0;
	/** �Ƿ���3��UTR�� */
	int UTR3 = 0;
}




