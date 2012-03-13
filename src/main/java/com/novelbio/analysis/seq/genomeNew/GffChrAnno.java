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
 * ��Ҫ������λ�Ĺ���
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
	 * �����趨��Ҫע�͵�������tss��tes��genebody��
	 * ����txt���ļ�����Ⱦɫ���ţ�Ⱦɫ������յ㣬������ļ�����peak���ǵ�������ע�ͳ���
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
	 * ����list������ע�ͺ���Ϣ��list������title
	 * @param lsInfo ��һ���Ǳ�����
	 * @param colChrID ʵ����
	 * @param colStart ʵ����
	 * @param colEnd ʵ����
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
		//���title
		String[] title = ArrayOperate.copyArray(titleOld, titleOld.length + 4);
		title[title.length - 1] = "Location"; title[title.length - 2] = "Description"; title[title.length - 3] = "Symbol"; title[title.length - 4] = "AccID";
		lsResult.add(0,title);
		return lsResult;
	}
	
	/**
	 * ����Ⱦɫ��λ�ú����꣬����ע����Ϣ
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
		//����һ��gene��
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
	public ArrayList<String[]> getGenInfoFilter(String chrID, int startCod, int endCod) {
		GffCodGeneDU gffCodGeneDu = gffHashGene.searchLocation(chrID, startCod, endCod);
		if (gffCodGeneDu == null) {
			return null;
		}
		ArrayList<String[]> lsAnno = gffCodGeneDu.getAnno(tss, tes, genebody, UTR5, UTR3, exonFilter, intronFilter);
		return lsAnno;
	}

	
	
	/**
	 * ����txt���ļ�����Ⱦɫ���ţ�Ⱦɫ������յ㣬������ļ�����peak���ǵ�������ע�ͳ���
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
	 * ����������Ϣlist�����ظ���������Ӧ��mapinfo
	 * @param lsIn  string[2] �򷵻� chrID summit
	 * string[3] �򷵻�chrID start end
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
				logger.error("����δ֪ID��"+ tmp.trim());
			}
			lsResult.add(mapInfo);
		}
		return lsResult;
	}
	
	
	/**
	 * ���뵥������λ�㣬���ض�λ��Ϣ������ͳ��λ��Ķ�λ���,�������ӻ����ں���
	 * ֻ�ж��ת¼��
	 * @param mapInfo
	 * @param summit true����flagSite���ж�λ��false�������˽��ж�λ
	 * @return int[8]
	 * 0: UpNbp,N��setStatistic()������TSS����
	 * 1: Exon<br>
	 * 2: Intron<br>
	 * 3: InterGenic--�����<br>
	 * 4: 5UTR
	 * 5: 3UTR
	 * 6: GeneEnd���ڻ������β�� ��setStatistic()������GeneEnd����
	 * 7: Tss ����Tss�Ϻ�Tss�£���filterTss����
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
	 * ���뵥������λ�㣬���ض�λ��Ϣ������ͳ��λ��Ķ�λ���
	 * ֻ�ж��ת¼��
	 * @param mapInfo
	 * @return int[8]
	 * 0: UpNbp,N��setStatistic()������TSS����
	 * 1: Exon<br>
	 * 2: Intron<br>
	 * 3: InterGenic--�����<br>
	 * 4: 5UTR
	 * 5: 3UTR
	 * 6: GeneEnd���ڻ������β�� ��setStatistic()������GeneEnd����
	 * 7: Tss ����Tss�Ϻ�Tss�£���filterTss����
	 */
	private int[] searchSite(MapInfo mapInfo)
	{
		boolean flagIntraGenic = false;//��gene�ڵı��
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




