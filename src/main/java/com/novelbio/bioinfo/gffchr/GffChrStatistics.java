package com.novelbio.bioinfo.gffchr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.multithread.RunProcess;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.base.AlignRecord;
import com.novelbio.bioinfo.base.AlignSeq;
import com.novelbio.bioinfo.base.FormatSeq;
import com.novelbio.bioinfo.bed.BedFile;
import com.novelbio.bioinfo.gff.GffCodGene;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.bioinfo.gff.ListGff;
import com.novelbio.bioinfo.gff.GffGene.GeneStructure;
import com.novelbio.bioinfo.sam.AlignmentRecorder;
import com.novelbio.bioinfo.sam.SamFile;
import com.novelbio.bioinfo.sam.SamRecord;
import com.novelbio.database.domain.species.Species;
/**
 * 直接在这个里面设定tss和tes
 * gffChrAbs里面的就不管他了
 * 中间输出的数量，也就是可当作进度条的数值，是每一行的字节数
 * @author zong0jie
 *
 */
public class GffChrStatistics extends RunProcess implements Cloneable, AlignmentRecorder {
	private static final Logger logger = Logger.getLogger(GffChrAnno.class);
	
	public static final String GeneStructureSuffix = ".gene_structure.txt";
	
	GffChrAbs gffChrAbs;
	
	int[] tssRegion = new int[]{-2000, 2000};
	int[] tesRegion = new int[]{-100, 100};
	
	double UTR5num = 0;
	double UTR3num = 0;
	double CDSnum = 0;
	double exonNum = 0;
	double exonNcRNA = 0;
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
	public void setBedFile(BedFile bedSeq) {
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
		exonNcRNA = 0;
		CDSnum = 0;
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
			seqFile = new BedFile(alignFile);
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
		if (!alignRecord.isMapped()) return;

		if (alignRecord instanceof SamRecord) {
			addSamRecord((SamRecord)alignRecord);
		} else {
			List<Align> lsAligns = alignRecord.getAlignmentBlocks();
			for (Align align : lsAligns) {
				double prop = (double)1/lsAligns.size()/alignRecord.getMappedReadsWeight();
				if(searchSite(prop, align)) {
				}
			}
		}
	}
	private void addSamRecord(SamRecord samRecord) {
		if (samRecord.getMappedReadsWeight() > 1 && samRecord.getMapIndexNum() != 1) {
			return;
		}
		
		List<Align> lsAligns = samRecord.getAlignmentBlocks();
		for (Align align : lsAligns) {
			double prop = (double)1/lsAligns.size();
			if(searchSite(prop, align)) {
			}
		}
	}

	private void readNormFile(String peakFile) {
		TxtReadandWrite txtRead = new TxtReadandWrite(peakFile, false);
		int i = 0;
		for (String readLine : txtRead.readlines(firstLine)) {
			if (readLine.startsWith("#")) continue;
			
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
		txtRead.close();
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
	private boolean searchSite(double prop, Align align) {
		suspendCheck();
		
		if (align == null) {
			return false;
		}
		boolean flagIntraGenic = false;//在gene内的标记
		GffCodGene gffCodGene = gffChrAbs.getGffHashGene().searchLocation(align.getChrId(), align.getMidSite());
		if (gffCodGene == null) {
			return false;
		}
		if (gffCodGene.isInsideLoc()) {
			flagIntraGenic = setStatisticsNum(prop, gffCodGene.getAlignThis(), align.getMidSite());
		} else {
			flagIntraGenic = setStatisticsNum(prop, gffCodGene.getAlignUp(), gffCodGene.getAlignDown(), align.getMidSite());
		}
		if (flagIntraGenic) {
			intraGenic += prop*1;
			return false;
		} else {
			interGenic += prop*1;
			return true;
		}
	}
	/**
	 * 设定统计值，并返回是否在IntraGenic中，也就是基因内部
	 * @param gffDetailGene
	 * @param coord
	 * @return
	 */
	private boolean setStatisticsNum(double prop, GffGene gffDetailGene, int coord) {
		boolean flagIntraGenic = true;

		GffIso gffGeneIsoInfo = gffDetailGene.getLongestmRNAIso(coord, GeneStructure.CDS);
		Set<GeneStructure> setGeneStructures = gffGeneIsoInfo.getLsCoordOnGeneStructure(coord, null, null);
		if (setGeneStructures.contains(GeneStructure.TSS)) {
			tssNum += prop*1;
		} else if (setGeneStructures.contains(GeneStructure.TES)) {
			tesNum += prop*1;
		}
		
		if (setGeneStructures.contains(GeneStructure.EXON)) {
			exonNum += prop*1;
		} else if (setGeneStructures.contains(GeneStructure.INTRON)) {
			intronNum += prop*1;
		}
		
		if (setGeneStructures.contains(GeneStructure.UTR5)) {
			UTR5num += prop*1;
		} else if (setGeneStructures.contains(GeneStructure.UTR3)) {
			UTR3num += prop*1;
		} else if (setGeneStructures.contains(GeneStructure.CDS)) {
			CDSnum += prop*1;
		}
		
		if (!gffGeneIsoInfo.isCodInIsoExtend(null, null, coord)) {
			flagIntraGenic = false;
		}
		
		return flagIntraGenic;
	}
	
	private boolean setStatisticsNum(double prop, GffGene gffDetailGeneUp, GffGene gffDetailGeneDown, int coord) {
		boolean flagIntraGenic = false;
		GffIso gffGeneIsoInfoUp = null, gffGeneIsoInfoDown = null;
		if (gffDetailGeneUp != null) {
			gffGeneIsoInfoUp = gffDetailGeneUp.getLongestSplitMrna();
		}
		if (gffDetailGeneDown != null) {
			gffGeneIsoInfoDown = gffDetailGeneDown.getLongestSplitMrna();
		}
		
		//Tss Tes
		if ( ( gffGeneIsoInfoUp != null && gffGeneIsoInfoUp.isCodInIsoTss(tssRegion, coord) ) 
				|| ( gffGeneIsoInfoDown != null && gffGeneIsoInfoDown.isCodInIsoTss(tssRegion, coord) )
			) {
			tssNum += prop*1;
			if (gffGeneIsoInfoUp != null && gffGeneIsoInfoUp.isCodInIsoExtend(null, null, coord) || gffGeneIsoInfoDown != null && gffGeneIsoInfoDown.isCodInIsoExtend(null, null, coord)) {
				throw new RuntimeException("find coord cannot in gene but in gene:" + coord 
						+ "  geneNameUp:" + gffGeneIsoInfoUp.getName() + "  geneNameDown:" + gffGeneIsoInfoDown.getName() );
			}
		}
		//GeneEnd
		if ( (gffGeneIsoInfoUp != null && gffGeneIsoInfoUp.isCodInIsoGenEnd(tesRegion, coord) )
				|| ( gffGeneIsoInfoDown != null && gffGeneIsoInfoDown.isCodInIsoGenEnd(tesRegion, coord) )
			) {
			tesNum += prop*1;
			if (gffGeneIsoInfoUp != null && gffGeneIsoInfoUp.isCodInIsoExtend(null, null, coord) || gffGeneIsoInfoDown != null && gffGeneIsoInfoDown.isCodInIsoExtend(null, null, coord)) {
				throw new RuntimeException("find coord cannot in gene but in gene:" + coord 
						+ "  geneNameUp:" + gffGeneIsoInfoUp.getName() + "  geneNameDown:" + gffGeneIsoInfoDown.getName() );
			}
		}
		return flagIntraGenic;
	}
	/**
	 * 不包含BG的统计结果<br>
	 * string[3]<br>
	 * 0: Iterm 类似 UTR5<br>
	 * 1: UTR5Num<br>
	 * 2: BackGroud UTR5Num<br>
	 * @return
	 */
	public ArrayList<String[]> getStatisticsResult() {
		ArrayList<String[]> lsTitle = new ArrayList<String[]>();
		lsTitle.add(new String[]{"Item", "Number"});
		lsTitle.add(new String[]{"UTR5", (long)UTR5num + ""});
		lsTitle.add(new String[]{"UTR3", (long)UTR3num + ""});
		lsTitle.add(new String[]{"CDS", (long)CDSnum + ""});//
		lsTitle.add(new String[]{"ExonNCRNA", (long)exonNcRNA + ""});
		lsTitle.add(new String[]{"ExonAll", (long)exonNum + ""});
		lsTitle.add(new String[]{"Intron", (long)intronNum + ""});
		lsTitle.add(new String[]{"Tss", (long)tssNum + ""});
		lsTitle.add(new String[]{"Tes", (long)tesNum + ""});
		lsTitle.add(new String[]{"InterGenic", (long)interGenic + ""});
		lsTitle.add(new String[]{"IntraGenic", (long)intraGenic + ""});
		return lsTitle;
	}
	/**
	 * 包含BG的统计结果<br>
	 * string[3]<br>
	 * 0: Iterm 类似 UTR5<br>
	 * 1: UTR5Num<br>
	 * 2: BackGroud UTR5Num<br>
	 * @return
	 */
	public ArrayList<String[]> getStatisticsResultWithBG() {
		GffChrStatistics gffChrStatistics = getStatisticsBackGround(gffChrAbs.getGffHashGene(), tssRegion, tesRegion);
		long allLen = 0;
		Map<String, Long> mapChrId2Len = null;
		if (gffChrAbs.getSeqHash() == null) {
			//这是只输入了gtf文件，从gtf文件中获取染色体的长度
			mapChrId2Len = gffChrAbs.getGffHashGene().getChrID2LengthForRNAseq();
		} else {
			mapChrId2Len = gffChrAbs.getSeqHash().getMapChrLength();
		}
		for (long chrLen : mapChrId2Len.values()) {
			allLen += chrLen;
		}
		long allNum = (long) (intraGenic + interGenic);
		ArrayList<String[]> lsTitle = new ArrayList<String[]>();
		lsTitle.add(new String[]{"Item", "Number", "NumberRatio", "BackGround", "BackGroupRatio"});
		lsTitle.add(new String[]{"UTR5", (long)UTR5num + "", (double)UTR5num/allNum + "", (long)gffChrStatistics.UTR5num + "", (double)gffChrStatistics.UTR5num/allLen + ""});
		lsTitle.add(new String[]{"UTR3", (long)UTR3num + "", (double)UTR3num/allNum + "", (long)gffChrStatistics.UTR3num + "", (double)gffChrStatistics.UTR3num/allLen + ""});
		lsTitle.add(new String[]{"CDS", (long)CDSnum + "", (double)CDSnum/allNum + "", (long)gffChrStatistics.CDSnum + "", (double)gffChrStatistics.CDSnum/allLen + ""});//
		lsTitle.add(new String[]{"ExonNCRNA", (long)exonNcRNA + "", (double)exonNcRNA/allNum + "", (long)gffChrStatistics.exonNcRNA + "", (double)gffChrStatistics.exonNcRNA/allLen + ""});
		lsTitle.add(new String[]{"ExonAll", (long)exonNum + "", (double)exonNum/allNum + "", (long)gffChrStatistics.exonNum + "", (double)gffChrStatistics.exonNum/allLen + ""});
		lsTitle.add(new String[]{"Intron", (long)intronNum + "", (double)intronNum/allNum + "", (long)gffChrStatistics.intronNum + "", (double)gffChrStatistics.intronNum/allLen + ""});
		lsTitle.add(new String[]{"Tss", (long)tssNum + "", (double)tssNum/allNum + "", (long)gffChrStatistics.tssNum + "", (double)gffChrStatistics.tssNum/allLen + ""});
		lsTitle.add(new String[]{"Tes", (long)tesNum + "", (double)tesNum/allNum + "", (long)gffChrStatistics.tesNum + "", (double)gffChrStatistics.tesNum/allLen + ""});
		lsTitle.add(new String[]{"InterGenic", (long)interGenic + "", (double)interGenic/allNum + "", (long)gffChrStatistics.interGenic + "", (double)gffChrStatistics.interGenic/allLen + ""});
		lsTitle.add(new String[]{"IntraGenic", (long)intraGenic + "", (double)intraGenic/allNum + "", (long)gffChrStatistics.intraGenic + "", (double)gffChrStatistics.intraGenic/allLen + ""});
		return lsTitle;
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
		gffChrStatisticsResult.exonNcRNA = exonNcRNA;
		gffChrStatisticsResult.fileName = fileName;
		gffChrStatisticsResult.firstLine = firstLine;
		gffChrStatisticsResult.gffChrAbs = gffChrAbs;
		gffChrStatisticsResult.interGenic = interGenic;
		gffChrStatisticsResult.intraGenic = intraGenic;
		gffChrStatisticsResult.intronNum = intronNum;
		return gffChrStatisticsResult;
	}
	
	
	public static class GffChrStatiscticsProcessInfo {
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
	
	/**
	 * 从给定的gffhash中获得背景信息，如ut多少个，exon多少个这种
	 * @param gffHashGene
	 * @param tssRegion
	 * @param tesRegion
	 * @return
	 */
	public static GffChrStatistics getStatisticsBackGround(GffHashGene gffHashGene, int[] tssRegion, int[] tesRegion) {
		GffChrStatistics gffChrStatistics = new GffChrStatistics();
		int errorNum = 0;// 看UCSC中有多少基因的TSS不是最长转录本的起点
		
		for (Entry<String, ListGff> entry : gffHashGene.getMapChrID2LsGff().entrySet()) {
			ListGff listGff = entry.getValue();
			int chrLOCNum = listGff.size();
			// 一条一条染色体的去检查内含子和外显子的长度
			for (int i = 0; i < chrLOCNum; i++) {
				GffGene tmpUCSCgene = listGff.get(i);
				GffIso gffGeneIsoInfoLong = tmpUCSCgene.getLongestSplitMrna();
				gffChrStatistics.intraGenic = gffChrStatistics.intraGenic + gffGeneIsoInfoLong.getLen();
				int exonLen = gffGeneIsoInfoLong.getLenExon(0);
				int intronLen = gffGeneIsoInfoLong.getLenIntron(0);
				
				// /////////////////////看UCSC中有多少基因的TSS不是最长转录本的起点//////////////////////////
				if ((tmpUCSCgene.isCis5to3() && gffGeneIsoInfoLong.getTSSsite() > tmpUCSCgene.getStartAbs())
						|| (!tmpUCSCgene.isCis5to3() && gffGeneIsoInfoLong.getTSSsite() < tmpUCSCgene.getEndAbs())) {
					errorNum++;
				}
				if (gffGeneIsoInfoLong.ismRNA()) {
					gffChrStatistics.UTR5num = gffChrStatistics.UTR5num + gffGeneIsoInfoLong.getLenUTR5();
					gffChrStatistics.UTR3num = gffChrStatistics.UTR3num + gffGeneIsoInfoLong.getLenUTR3();
					double cdsLen = gffGeneIsoInfoLong.getLenExon(0) - gffGeneIsoInfoLong.getLenUTR5() - gffGeneIsoInfoLong.getLenUTR3();
					if (cdsLen > 0) {
						gffChrStatistics.CDSnum = gffChrStatistics.CDSnum + cdsLen;
					}
				} else {
//					gffChrStatistics.exonNcRNA = gffChrStatistics.exonNcRNA + exonLen;
				}
				gffChrStatistics.exonNum = gffChrStatistics.exonNum + exonLen;
				gffChrStatistics.intronNum = gffChrStatistics.intronNum + intronLen;
				if (i > 0) {
					gffChrStatistics.interGenic = gffChrStatistics.interGenic + getIntergenic(gffGeneIsoInfoLong, listGff.get(i - 1).getLongestSplitMrna(), tssRegion, tesRegion);
				}
				gffChrStatistics.tssNum = gffChrStatistics.tssNum + tssRegion[1] - tssRegion[0];
				gffChrStatistics.tesNum = gffChrStatistics.tesNum + tesRegion[1] - tesRegion[0];
			}
		}
		System.out.println("getGeneStructureLength: 看UCSC中有多少基因的TSS不是最长转录本的起点" + errorNum);
		return gffChrStatistics;
	}
	
	private static int getIntergenic(GffIso gffGeneIsoInfoThis, GffIso gffGeneIsoInfoUp, int[] tssRegion, int[] tesRegion) {
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
}

