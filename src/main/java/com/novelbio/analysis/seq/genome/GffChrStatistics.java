package com.novelbio.analysis.seq.genome;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.w3c.dom.ls.LSSerializer;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.BedRecord;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.ListGff;
import com.novelbio.analysis.seq.genome.mappingOperate.SiteInfo;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.species.Species;
/**
 * ֱ������������趨tss��tes
 * gffChrAbs����ľͲ�������
 * �м������������Ҳ���ǿɵ�������������ֵ����ÿһ�е��ֽ���
 * @author zong0jie
 *
 */
public class GffChrStatistics extends RunProcess<GffChrStatistics.GffChrStatiscticsProcessInfo> implements Cloneable{
	private static final Logger logger = Logger.getLogger(GffChrAnno.class);
	
	GffChrAbs gffChrAbs;
	
	int[] tssRegion = new int[]{-2000, 2000};
	int[] tesRegion = new int[]{-100, 100};
	
	long UTR5num = 0;
	long UTR3num = 0;
	long exonNum = 0;
	long intronNum = 0;
	long tssNum = 0;
	long tesNum = 0;
	
	long interGenic = 0;
	long intraGenic = 0;

	int colChrID = 0;
	int colSummit = -1;
	/** �Ƿ�Ϊbed�ļ� */
	boolean isAlignFile = true;
	
	int firstLine = 1;
	String fileName = "";
	/** ��ȡ�˶����ļ�����������ʹ�� */
	long allnumber = 0;
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public void setSpecies(Species species) {
		this.gffChrAbs = new GffChrAbs(species);
	}
	/** tss�����䣬���θ���������������������Ϊ-2000��-1000 */
	public void setTssRegion(int[] tssRegion) {
		this.tssRegion = tssRegion;
	}
	/** tes�����䣬���θ���������������������Ϊ100��200 */
	public void setTesRegion(int[] tesRegion) {
		this.tesRegion = tesRegion;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/** ����ֱ������bed�ļ� */
	public void setBedFile(BedSeq bedSeq) {
		this.fileName = bedSeq.getFileName();
	}
	public void setColSummit(int colSummit) {
		this.colSummit = colSummit - 1;
		isAlignFile = false;
	}
	public void setColChrID(int colChrID) {
		this.colChrID = colChrID - 1;
		isAlignFile = false;
	}
	/**
	 * �ӵڼ��п�ʼ��ȡ��Ĭ��Ϊ1
	 * @param firstLine
	 */
	public void setFirstLine(int firstLine) {
		this.firstLine = firstLine;
	}
	
	@Override
	protected void running() {
		getSummitStatistic();
	}
	public void clean() {
		UTR5num = 0;
		UTR3num = 0;
		exonNum = 0;
		intronNum = 0;
		tssNum = 0;	
		tesNum = 0;
		interGenic = 0;
		intraGenic = 0;
	}
	/**
	 * ����txt���ļ�����Ⱦɫ���ţ�Ⱦɫ������յ㣬������ļ�����peak���ǵ�������ע�ͳ���
	 * @param txtFile
	 * @param colChrID
	 * @param colStart
	 * @param colEnd
	 * @param outTxtFile
	 */
	private void getSummitStatistic() {
		allnumber = 0;
		if (isAlignFile) {
			readAlignFile(fileName);
		} else {
			readNormFile(fileName);
		}
	}
	/** ���Զ�ȡsam/bam�ļ���bed�ļ� */
	private void readAlignFile(String alignFile) {
		FormatSeq formatSeq = FormatSeq.getFileType(alignFile);
		AlignSeq seqFile = null;
		if (formatSeq == FormatSeq.BED) {
			seqFile = new BedSeq(alignFile);
		}
		else if (formatSeq == FormatSeq.SAM || formatSeq == FormatSeq.BAM) {
			seqFile = new SamFile(alignFile);
		}
		
		int i = 0;
		for (AlignRecord bedRecord : seqFile.readLines(firstLine)) {
			
			ArrayList<SiteInfo> lsSiteInfos = getLsGetBedSiteInfo(bedRecord);
			for (SiteInfo siteInfo : lsSiteInfos) {
				searchSite(siteInfo);
			}
			
			allnumber = allnumber + bedRecord.getRawStringInfo().getBytes().length;
			if (i%1000 == 0) {
				GffChrStatiscticsProcessInfo anno = new GffChrStatiscticsProcessInfo((int)(allnumber/1000000));
				setRunInfo(anno);
			}
			i++;
			if (flagStop) break;
		}
	}
	private ArrayList<SiteInfo> getLsGetBedSiteInfo(AlignRecord bedRecord) {
		ArrayList<SiteInfo> lSiteInfos = new ArrayList<SiteInfo>();
		ArrayList<Align> lsAligns = bedRecord.getAlignmentBlocks();
		for (Align align : lsAligns) {
			SiteInfo siteInfo = new SiteInfo(bedRecord.getRefID(), align.getStartCis(), align.getEndCis());
			siteInfo.setFlagLoc( (align.getStartCis() + align.getEndCis())/2);
			lSiteInfos.add(siteInfo);
		}
		return lSiteInfos;
	}
	private void readNormFile(String peakFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(peakFile, false);
		int i = 0;
		for (String readLine : txtRead.readlines(firstLine)) {
			SiteInfo siteInfo = readInfo(readLine.split("\t"));
			searchSite(siteInfo);
			
			allnumber = allnumber + readLine.getBytes().length;
			if (i%1000 == 0) {
				GffChrStatiscticsProcessInfo anno = new GffChrStatiscticsProcessInfo((int)(allnumber/1000000));
				setRunInfo(anno);
			}
			i++;
			if (flagStop) break;
		}
	}
	/**
	 * ����������Ϣlist�����ظ���������Ӧ��mapinfo
	 * @param lsIn  string[2] �򷵻� chrID summit
	 * string[3] �򷵻�chrID start end
	 * @return
	 */
	private SiteInfo readInfo(String[] readLine) {
		try {
			SiteInfo siteInfo = new SiteInfo(readLine[colChrID]);
			siteInfo.setFlagLoc(Integer.parseInt(readLine[colSummit].trim()));
			return siteInfo;
		} catch (Exception e) {
			return null;
		}

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
	private void searchSite(SiteInfo siteInfo) {
		suspendCheck();
		
		if (siteInfo == null) {
			return;
		}
		boolean flagIntraGenic = false;//��gene�ڵı��
		GffCodGene gffCodGene = gffChrAbs.getGffHashGene().searchLocation(siteInfo.getRefID(), siteInfo.getFlagSite());
		if (gffCodGene == null) {
			return;
		}
		if (gffCodGene.isInsideLoc()) {
			flagIntraGenic = setStatisticsNum(gffCodGene.getGffDetailThis(), siteInfo.getFlagSite());
		}
		else {
			flagIntraGenic = setStatisticsNum(gffCodGene.getGffDetailUp(), gffCodGene.getGffDetailDown(), siteInfo.getFlagSite());
		}
		if (flagIntraGenic)
			intraGenic++;
		else
			interGenic++;
	}
	/**
	 * �趨ͳ��ֵ���������Ƿ���IntraGenic�У�Ҳ���ǻ����ڲ�
	 * @param gffDetailGene
	 * @param coord
	 * @return
	 */
	private boolean setStatisticsNum(GffDetailGene gffDetailGene, int coord) {
		gffDetailGene.setTssRegion(tssRegion);
		gffDetailGene.setTesRegion(tesRegion);
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
		boolean flagIntraGenic = true;
		//Tss Tes
		if (gffGeneIsoInfo.isCodInIsoTss(coord) ) {
			tssNum++;
		}
		else if (gffGeneIsoInfo.isCodInIsoGenEnd(coord) ) {
			tesNum++;
		}
		
		boolean isInExon = false;
		ArrayList<GffGeneIsoInfo> lsIso = gffDetailGene.getLsCodSplit();
		
		//Exon Intron
		// ÿ��ת¼������һ��
		for (GffGeneIsoInfo gffGeneIsoInfo2 : lsIso) {
			if (gffGeneIsoInfo2.getCodLoc(coord) == GffGeneIsoInfo.COD_LOC_EXON) {
				exonNum++;
				isInExon = true;
				break;
			}
		}
		if (!isInExon && gffGeneIsoInfo.getCodLoc(coord) == GffGeneIsoInfo.COD_LOC_INTRON) {
			intronNum++;
		}
		
		//UTR
		if (gffGeneIsoInfo.getCodLocUTRCDS(coord) == GffGeneIsoInfo.COD_LOCUTR_5UTR) {
			UTR5num++;
		}
		if (gffGeneIsoInfo.getCodLocUTRCDS(coord) == GffGeneIsoInfo.COD_LOCUTR_3UTR) {
			UTR3num++;
		}
		return flagIntraGenic;
	}
	
	private boolean setStatisticsNum(GffDetailGene gffDetailGeneUp, GffDetailGene gffDetailGeneDown, int coord) {
		boolean flagIntraGenic = false;
		if (gffDetailGeneUp != null ) {
			gffDetailGeneUp.setTssRegion(tssRegion);
			gffDetailGeneUp.setTesRegion(tesRegion);
		}
		if (gffDetailGeneDown != null) {
			gffDetailGeneDown.setTssRegion(tssRegion);
			gffDetailGeneDown.setTesRegion(tesRegion);
		}
		GffGeneIsoInfo gffGeneIsoInfoUp = null, gffGeneIsoInfoDown = null;
		if (gffDetailGeneUp != null) {
			gffGeneIsoInfoUp = gffDetailGeneUp.getLongestSplitMrna();
		}
		if (gffDetailGeneDown != null) {
			gffGeneIsoInfoDown = gffDetailGeneDown.getLongestSplitMrna();
		}
		
		//Tss Tes
		if ( ( gffGeneIsoInfoUp != null && gffGeneIsoInfoUp.isCodInIsoTss(coord) ) 
				|| ( gffGeneIsoInfoDown != null && gffGeneIsoInfoDown.isCodInIsoTss(coord) )
			) {
			tssNum++;
			flagIntraGenic =true;
		}
		//GeneEnd
		if ( (gffGeneIsoInfoUp != null && gffGeneIsoInfoUp.isCodInIsoGenEnd(coord) )
				|| ( gffGeneIsoInfoDown != null && gffGeneIsoInfoDown.isCodInIsoGenEnd(coord) )
			) {
			tesNum++;
			flagIntraGenic =true;
		}
		return flagIntraGenic;
	}
	
	public ArrayList<String[]> getStatisticsResult() {
		GffChrStatistics gffChrStatistics = getStatisticsBackGround();
		ArrayList<String[]> lsTitle = new ArrayList<String[]>();
		lsTitle.add(new String[]{"Item", "Number", "BackGround"});
		lsTitle.add(new String[]{"UTR5", UTR5num + "", gffChrStatistics.UTR5num + ""});
		lsTitle.add(new String[]{"UTR3", UTR3num + "", gffChrStatistics.UTR3num + ""});
		lsTitle.add(new String[]{"Exon", exonNum + "", gffChrStatistics.exonNum + ""});
		lsTitle.add(new String[]{"Intron", intronNum + "", gffChrStatistics.intronNum + ""});
		lsTitle.add(new String[]{"Tss", tssNum + "", gffChrStatistics.tssNum + ""});
		lsTitle.add(new String[]{"Tes", tesNum + "", gffChrStatistics.tesNum + ""});
		lsTitle.add(new String[]{"InterGenic", interGenic + "", gffChrStatistics.interGenic + ""});
		lsTitle.add(new String[]{"IntraGenic", intraGenic + "", gffChrStatistics.intraGenic + ""});
		return lsTitle;
	}
	
	private GffChrStatistics getStatisticsBackGround() {
		GffChrStatistics gffChrStatistics = new GffChrStatistics();
		
		GffHashGene gffHashGene = gffChrAbs.getGffHashGene();
		int errorNum = 0;// ��UCSC���ж��ٻ����TSS�����ת¼�������

		for (Entry<String, ListGff> entry : gffHashGene.getMapChrID2LsGff().entrySet()) {
			ListGff listGff = entry.getValue();
			int chrLOCNum = listGff.size();
			// һ��һ��Ⱦɫ���ȥ����ں��Ӻ������ӵĳ���
			for (int i = 0; i < chrLOCNum; i++) {
				GffDetailGene tmpUCSCgene = listGff.get(i);
				GffGeneIsoInfo gffGeneIsoInfoLong = tmpUCSCgene.getLongestSplitMrna();
				gffChrStatistics.intraGenic = gffChrStatistics.intraGenic + gffGeneIsoInfoLong.getLen();
				// /////////////////////��UCSC���ж��ٻ����TSS�����ת¼�������//////////////////////////
				if ((tmpUCSCgene.isCis5to3() && gffGeneIsoInfoLong.getTSSsite() > tmpUCSCgene.getStartAbs())
						|| (!tmpUCSCgene.isCis5to3() && gffGeneIsoInfoLong.getTSSsite() < tmpUCSCgene.getEndAbs())) {
					errorNum++;
				}
				gffChrStatistics.UTR5num = gffChrStatistics.UTR5num + gffGeneIsoInfoLong.getLenUTR5();
				gffChrStatistics.UTR3num = gffChrStatistics.UTR3num + gffGeneIsoInfoLong.getLenUTR3();
				gffChrStatistics.exonNum = gffChrStatistics.exonNum + gffGeneIsoInfoLong.getLenExon(0);
				gffChrStatistics.intronNum = gffChrStatistics.intronNum + gffGeneIsoInfoLong.getLenIntron(0);
				
				if (i > 0) {
					gffChrStatistics.interGenic = gffChrStatistics.interGenic + getIntergenic(gffGeneIsoInfoLong, listGff.get(i - 1).getLongestSplitMrna());
				}
				gffChrStatistics.tssNum = gffChrStatistics.tssNum + tssRegion[1] - tssRegion[0];
				gffChrStatistics.tesNum = gffChrStatistics.tesNum + tesRegion[1] - tesRegion[0];
			}
		}
		System.out.println("getGeneStructureLength: ��UCSC���ж��ٻ����TSS�����ת¼�������" + errorNum);
		return gffChrStatistics;
	}
	
	private int getIntergenic(GffGeneIsoInfo gffGeneIsoInfoThis, GffGeneIsoInfo gffGeneIsoInfoUp) {
		int upGeneEnd = 0;
		if (gffGeneIsoInfoUp == null) {
			return 0;
		}
		else{
			upGeneEnd = gffGeneIsoInfoUp.getEndAbs();
		}

		int thisGeneStart = gffGeneIsoInfoThis.getStartAbs();
		
		if (gffGeneIsoInfoThis.isCis5to3() && tssRegion[0] < 0) {
			thisGeneStart = thisGeneStart - Math.abs(tssRegion[0]);
		}
		else if (!gffGeneIsoInfoThis.isCis5to3() && tesRegion[1] > 0) {
			thisGeneStart = thisGeneStart - Math.abs(tesRegion[1]);
		}
		
		if (gffGeneIsoInfoUp.isCis5to3() && tesRegion[1] > 0) {
			thisGeneStart = upGeneEnd + Math.abs(tesRegion[1]);
		}
		else if (!gffGeneIsoInfoUp.isCis5to3() && tssRegion[0] < 0) {
			thisGeneStart = thisGeneStart + Math.abs(tssRegion[0]);
		}
		
		int result = thisGeneStart - upGeneEnd;
		if (result < 0) {
			return 0;
		}
		return result;
	}
	@Override
	protected GffChrStatistics clone() {
		GffChrStatistics gffChrStatisticsResult = null;
		try {
			gffChrStatisticsResult = (GffChrStatistics) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		gffChrStatisticsResult.isAlignFile = isAlignFile;
		gffChrStatisticsResult.colChrID = colChrID;
		gffChrStatisticsResult.colSummit = colSummit;
		gffChrStatisticsResult.exonNum = exonNum;
		gffChrStatisticsResult.fileName = fileName;
		gffChrStatisticsResult.firstLine = firstLine;
		gffChrStatisticsResult.gffChrAbs = gffChrAbs;
		gffChrStatisticsResult.interGenic = interGenic;
		gffChrStatisticsResult.intraGenic = intraGenic;
		gffChrStatisticsResult.intronNum = intronNum;
		return gffChrStatisticsResult;
	}
	
	
	public static class GffChrStatiscticsProcessInfo{
		int readsize;
		GffChrStatistics gffChrStatistics;
		public GffChrStatiscticsProcessInfo(int readsizes) {
			this.readsize = readsizes;
		}
		public void setGffChrStatistics(GffChrStatistics gffChrStatistics) {
			this.gffChrStatistics = gffChrStatistics.clone();
		}
		public int getReadsize() {
			return readsize;
		}
	}
}

