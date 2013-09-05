package com.novelbio.analysis.seq.genome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.analysis.seq.bed.BedSeq;
import com.novelbio.analysis.seq.genome.gffOperate.GffCodGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffHashGene;
import com.novelbio.analysis.seq.genome.gffOperate.ListGff;
import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.AlignmentRecorder;
import com.novelbio.analysis.seq.sam.SamFile;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.database.model.species.Species;
/**
 * 直接在这个里面设定tss和tes
 * gffChrAbs里面的就不管他了
 * 中间输出的数量，也就是可当作进度条的数值，是每一行的字节数
 * @author zong0jie
 *
 */
public class GffChrStatistics extends RunProcess<GffChrStatistics.GffChrStatiscticsProcessInfo> implements Cloneable, AlignmentRecorder {
	private static final Logger logger = Logger.getLogger(GffChrAnno.class);
	
	GffChrAbs gffChrAbs;
	
	int[] tssRegion = new int[]{-2000, 2000};
	int[] tesRegion = new int[]{-100, 100};
	
	double UTR5num = 0;
	double UTR3num = 0;
	double exonNum = 0;
	double intronNum = 0;
	double tssNum = 0;
	double tesNum = 0;
	
	double interGenic = 0;
	double intraGenic = 0;

	int colChrID = 0;
	int colSummit = -1;
	/** 是否为bed文件 */
	boolean isAlignFile = true;
	
	int firstLine = 1;
	String fileName = "";
	/** 读取了多少文件，给进度条使用 */
	long allnumber = 0;
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		this.gffChrAbs = gffChrAbs;
	}
	public void setSpecies(Species species) {
		this.gffChrAbs = new GffChrAbs(species);
	}
	/** tss的区间，上游负数下游正数，可以设置为-2000，-1000 */
	public void setTssRegion(int[] tssRegion) {
		if (tssRegion != null) {
			this.tssRegion = tssRegion;
		}
	}
	/** tes的区间，上游负数下游正数，可以设置为100，200 */
	public void setTesRegion(int[] tesRegion) {
		if (tesRegion != null) {
			this.tesRegion = tesRegion;
		}
	}
	/**
	 * 可以自动设定为bed或bam文件
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/** 可以直接输入bed文件 */
	public void setBedFile(BedSeq bedSeq) {
		this.fileName = bedSeq.getFileName();
	}
	/** 设定第几列为summit，也就是这列为reads的中点，用这个中点来进行定位
	 * <b>每次读取都要重新设定</b>
	 */
	public void setColSummit(int colSummit) {
		this.colSummit = colSummit - 1;
		isAlignFile = false;
	}
	/**
	 * <b>每次读取都要重新设定</b>
	 * @param colChrID
	 */
	public void setColChrID(int colChrID) {
		this.colChrID = colChrID - 1;
		isAlignFile = false;
	}
	/**
	 * 从第几行开始读取，默认为1
	 * @param firstLine
	 */
	public void setFirstLine(int firstLine) {
		this.firstLine = firstLine;
	}
	
	@Override
	protected void running() {
		getSummitStatistic();
	}
	
	/**
	 * 每次运行新统计前要先清空
	 */
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
	 * 给定txt的文件，和染色体编号，染色体起点终点，和输出文件，将peak覆盖到的区域注释出来
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
	/** 可以读取sam/bam文件和bed文件 */
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
		for (AlignRecord alignRecord : seqFile.readLines(firstLine)) {
			addAlignRecord(alignRecord);
			allnumber = allnumber + alignRecord.getRawStringInfo().getBytes().length;
			if (i%1000 == 0) {
				GffChrStatiscticsProcessInfo anno = new GffChrStatiscticsProcessInfo((int)(allnumber/1000000));
				setRunInfo(anno);
			}
			i++;
			if (flagStop) break;
		}
		isAlignFile = true;
	}
	
	public void addAlignRecord(AlignRecord alignRecord) {
		List<Align> lsAligns = alignRecord.getAlignmentBlocks();
		for (Align align : lsAligns) {
			double prop = (double)1/lsAligns.size()/alignRecord.getMappedReadsWeight();
			searchSite(prop, align);
		}
	}

	private void readNormFile(String peakFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(peakFile, false);
		int i = 0;
		for (String readLine : txtRead.readlines(firstLine)) {
			Align align = readInfo(readLine.split("\t"));
			searchSite(1, align);
			
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
	 * 给定坐标信息list，返回该坐标所对应的mapinfo
	 * @param lsIn  string[2] 则返回 chrID summit
	 * string[3] 则返回chrID start end
	 * @return
	 */
	private Align readInfo(String[] readLine) {
		try {
			Align align = new Align(readLine[colChrID], Integer.parseInt(readLine[colSummit].trim()), Integer.parseInt(readLine[colSummit].trim()));
			return align;
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * 输入单个坐标位点，返回定位信息，用于统计位点的定位情况
	 * 只判断最长转录本
	 * @param prop 权重，意思本align占总reads的百分比
	 * 譬如一条reads可以同时mapping至多个位点，则比重下降。
	 * 一条reads有多个align，比重也下降
	 * @param align
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
	private void searchSite(double prop, Align align) {
		suspendCheck();
		
		if (align == null) {
			return;
		}
		boolean flagIntraGenic = false;//在gene内的标记
		GffCodGene gffCodGene = gffChrAbs.getGffHashGene().searchLocation(align.getRefID(), align.getMidSite());
		if (gffCodGene == null) {
			return;
		}
		if (gffCodGene.isInsideLoc()) {
			flagIntraGenic = setStatisticsNum(prop, gffCodGene.getGffDetailThis(), align.getMidSite());
		} else {
			flagIntraGenic = setStatisticsNum(prop, gffCodGene.getGffDetailUp(), gffCodGene.getGffDetailDown(), align.getMidSite());
		}
		if (flagIntraGenic) {
			intraGenic += 1*prop;
		} else {
			interGenic += 1*prop;
		}
	}
	/**
	 * 设定统计值，并返回是否在IntraGenic中，也就是基因内部
	 * @param gffDetailGene
	 * @param coord
	 * @return
	 */
	private boolean setStatisticsNum(double prop, GffDetailGene gffDetailGene, int coord) {
		gffDetailGene.setTssRegion(tssRegion);
		gffDetailGene.setTesRegion(tesRegion);
		GffGeneIsoInfo gffGeneIsoInfo = gffDetailGene.getLongestSplitMrna();
		boolean flagIntraGenic = true;
		//Tss Tes
		if (gffGeneIsoInfo.isCodInIsoTss(coord) ) {
			tssNum += 1*prop;
		}
		else if (gffGeneIsoInfo.isCodInIsoGenEnd(coord) ) {
			tesNum += 1*prop;
		}
		
		boolean isInExon = false;
		ArrayList<GffGeneIsoInfo> lsIso = gffDetailGene.getLsCodSplit();
		
		//Exon Intron
		// 每个转录本都查一遍
		for (GffGeneIsoInfo gffGeneIsoInfo2 : lsIso) {
			if (gffGeneIsoInfo2.getCodLoc(coord) == GffGeneIsoInfo.COD_LOC_EXON) {
				exonNum += 1*prop;
				isInExon = true;
				break;
			}
		}
		if (!isInExon && gffGeneIsoInfo.getCodLoc(coord) == GffGeneIsoInfo.COD_LOC_INTRON) {
			intronNum += 1*prop;
		}
		
		//UTR
		if (gffGeneIsoInfo.getCodLocUTRCDS(coord) == GffGeneIsoInfo.COD_LOCUTR_5UTR) {
			UTR5num += 1*prop;
		} else if (gffGeneIsoInfo.getCodLocUTRCDS(coord) == GffGeneIsoInfo.COD_LOCUTR_3UTR) {
			UTR3num += 1*prop;
		}
		return flagIntraGenic;
	}
	
	private boolean setStatisticsNum(double prop, GffDetailGene gffDetailGeneUp, GffDetailGene gffDetailGeneDown, int coord) {
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
			tssNum += 1*prop;
			flagIntraGenic =true;
		}
		//GeneEnd
		if ( (gffGeneIsoInfoUp != null && gffGeneIsoInfoUp.isCodInIsoGenEnd(coord) )
				|| ( gffGeneIsoInfoDown != null && gffGeneIsoInfoDown.isCodInIsoGenEnd(coord) )
			) {
			tesNum += 1*prop;
			flagIntraGenic =true;
		}
		return flagIntraGenic;
	}
	
	public ArrayList<String[]> getStatisticsResult() {
		GffChrStatistics gffChrStatistics = getStatisticsBackGround();
		ArrayList<String[]> lsTitle = new ArrayList<String[]>();
		lsTitle.add(new String[]{"Item", "Number", "BackGround"});
		lsTitle.add(new String[]{"UTR5", (long)UTR5num + "", (long)gffChrStatistics.UTR5num + ""});
		lsTitle.add(new String[]{"UTR3", (long)UTR3num + "", (long)gffChrStatistics.UTR3num + ""});
		lsTitle.add(new String[]{"Exon", (long)exonNum + "", (long)gffChrStatistics.exonNum + ""});
		lsTitle.add(new String[]{"Intron", (long)intronNum + "", (long)gffChrStatistics.intronNum + ""});
		lsTitle.add(new String[]{"Tss", (long)tssNum + "", (long)gffChrStatistics.tssNum + ""});
		lsTitle.add(new String[]{"Tes", (long)tesNum + "", (long)gffChrStatistics.tesNum + ""});
		lsTitle.add(new String[]{"InterGenic", (long)interGenic + "", (long)gffChrStatistics.interGenic + ""});
		lsTitle.add(new String[]{"IntraGenic", (long)intraGenic + "", (long)gffChrStatistics.intraGenic + ""});
		return lsTitle;
	}
	
	private GffChrStatistics getStatisticsBackGround() {
		GffChrStatistics gffChrStatistics = new GffChrStatistics();
		
		GffHashGene gffHashGene = gffChrAbs.getGffHashGene();
		int errorNum = 0;// 看UCSC中有多少基因的TSS不是最长转录本的起点

		for (Entry<String, ListGff> entry : gffHashGene.getMapChrID2LsGff().entrySet()) {
			ListGff listGff = entry.getValue();
			int chrLOCNum = listGff.size();
			// 一条一条染色体的去检查内含子和外显子的长度
			for (int i = 0; i < chrLOCNum; i++) {
				GffDetailGene tmpUCSCgene = listGff.get(i);
				GffGeneIsoInfo gffGeneIsoInfoLong = tmpUCSCgene.getLongestSplitMrna();
				gffChrStatistics.intraGenic = gffChrStatistics.intraGenic + gffGeneIsoInfoLong.getLen();
				// /////////////////////看UCSC中有多少基因的TSS不是最长转录本的起点//////////////////////////
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
		System.out.println("getGeneStructureLength: 看UCSC中有多少基因的TSS不是最长转录本的起点" + errorNum);
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


	@Override
	public void summary() {
		//Nothing to do 
	}
	@Override
	public Align getReadingRegion() {
		return null;
	}
}

