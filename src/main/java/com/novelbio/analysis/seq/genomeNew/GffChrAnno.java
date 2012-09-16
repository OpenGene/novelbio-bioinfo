package com.novelbio.analysis.seq.genomeNew;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import antlr.debug.TraceAdapter;

import com.novelbio.analysis.annotation.genAnno.AnnoQuery.AnnoQueryDisplayInfo;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffCodGeneDU;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genomeNew.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
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
public class GffChrAnno extends RunProcess<AnnoQueryDisplayInfo>{
	private static final Logger logger = Logger.getLogger(GffChrAnno.class);
	
	public static void main(String[] args) {
		String txtFile = "/media/winE/NBC/Project/Project_CDG_Lab/ChIPSeq_CDG110921/WE.clean.fq/result/annotation/WE_peaks_summit.xls";
		GffChrAnno gffChrAnno = new GffChrAnno(new GffChrAbs(10090));
		gffChrAnno.setColChrID(1);
		gffChrAnno.setColStartEnd(2, 3);
		gffChrAnno.setColSummit(6);
		gffChrAnno.setSearchSummit(false);
		gffChrAnno.annoFile(txtFile, FileOperate.changeFileSuffix(txtFile, "_anno_summit", null));
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
		int start =  (int)Double.parseDouble(geneLocInfo[colStart]);
		int end =  (int)Double.parseDouble(geneLocInfo[colEnd]);
		int summit = 0;
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
		GffCodGeneDU gffCodGeneDu = gffChrAbs.getGffHashGene().searchLocation(chrID, startCod, endCod);
		if (gffCodGeneDu == null) {
			return null;
		}
		ArrayList<String[]> lsAnno = null;
		gffCodGeneDu.setExon(gffChrAbs.exonFilter);
		gffCodGeneDu.setGeneBody(gffChrAbs.genebody);
		gffCodGeneDu.setIntron(gffChrAbs.intronFilter);
		gffCodGeneDu.setTes(gffChrAbs.tes);
		gffCodGeneDu.setTss(gffChrAbs.tss);
		gffCodGeneDu.setUTR3(gffChrAbs.UTR3);
		gffCodGeneDu.setUTR5(gffChrAbs.UTR5);
		lsAnno = gffCodGeneDu.getAnno();
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
			getAnnoLocSumit(lsResultAnno, gffCodGene.getGffDetailUp().getLongestSplit(), summit);
		}
		if (gffCodGene.getGffDetailThis() != null) {
			getAnnoLocSumit(lsResultAnno, gffCodGene.getGffDetailThis().getLongestSplit(), summit);
		}
		if (gffCodGene.getGffDetailDown() != null) {
			getAnnoLocSumit(lsResultAnno, gffCodGene.getGffDetailDown().getLongestSplit(), summit);
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
	private void getAnnoLocSumit(ArrayList<String[]> lsAnno, GffGeneIsoInfo gffGeneIsoInfo, int coord) {
		if (gffGeneIsoInfo.isCodLocFilter(coord, gffChrAbs.filtertss, gffChrAbs.filtertes, gffChrAbs.genebody, gffChrAbs.UTR5, gffChrAbs.UTR3, gffChrAbs.exonFilter, gffChrAbs.intronFilter)) {
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
class siteLocInfo
{
	/**
	 * �Ƿ��ڲ�����promoter����������
	 */
	int ExonWithOutPromoter = 0;
	/**
	 * �Ƿ��ڲ�����promoter���ں�����
	 */
	int IntronWithOutPromoter = 0;
	/**
	 * �Ƿ���genebody��
	 */
	int geneBody = 0;
	/**
	 * �Ƿ��ڻ������Promoter��
	 */
	int PromoterOutGene = 0;
	/**
	 * �Ƿ��ڻ����ڵ�Promoter��
	 */
	int PromoterInGene = 0;
	/**
	 * �Ƿ��ڲ�����promoter����������
	 */
	int InterGenic = 0;
	/**
	 * �Ƿ���5��UTR��
	 */
	int UTR5 = 0;
	/**
	 * �Ƿ���3��UTR��
	 */
	int UTR3 = 0;
}




