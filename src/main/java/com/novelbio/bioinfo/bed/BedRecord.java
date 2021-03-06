package com.novelbio.bioinfo.bed;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.base.AlignRecord;
import com.novelbio.bioinfo.base.Alignment;
import com.novelbio.bioinfo.fasta.SeqFasta;
import com.novelbio.bioinfo.fastq.FastQRecord;
 /**
  * BedSeq每一行的信息<br>
  * 兼容 bamToBed的 12行信息格式
  * @author zong0jie
  *
  */
public class BedRecord implements AlignRecord,Serializable {
	static private Logger logger = Logger.getLogger(BedRecord.class);
	static final int COL_CHRID = 0;
	static final int COL_START = 1;
	static final int COL_END = 2;
	static final int COL_NAME = 3;
	static final int COL_SCORE = 4;
	static final int COL_STRAND = 5;
	static final int COL_CIGAR = 6;
	static final int COL_MAPNUM = 7;
	static final int COL_SEQ = 8;
	/** 是否为unique mapping的列 */
	static final int COL_MAPQ = 9;
	static final int COL_SPLIT_READS_LEN = 10;
	static final int COL_SPLIT_READS_START = 11;
	static final int COL_READSNUM = 12;
	/** 该reads的权重，意思就是本reads在本文件中出现了几次，出现一次就是1 */
	public static final int COL_MAPWEIGHT = 13;
	
	/**
	 * 对上面总共列的计数，上面如果增加或者删减了列，这里要相应的修正
	 */
	static final int ALL_COLNUM = 14;
	
	Align align = new Align();
	String seqName;
	SeqFasta seqFasta;
	/** mapping到了几个上去 */
	Integer mappingNum = null;
	/** 该reads的权重，意思就是本reads在本文件中出现了几次，出现一次就是1 */
	Integer mappingWeight = null;
	double score;
	/**
	 * 本位点的包含了几条overlap的序列
	 */
	Integer readsNum = null;
	
	
	
	String CIGAR = null;
	Integer mapQuality = null;
	
	String readLineInfo = "";
	
	/** 类似9,53,28 */
	String splitLen;
	/** 0,2134,11171 */
	String splitStart;
	
	public BedRecord() {
	}
	public BedRecord(AlignRecord alignRecord) {
		align = new Align(alignRecord);
		seqName = alignRecord.getName();
		seqFasta = alignRecord.getSeqFasta();
		CIGAR = alignRecord.getCIGAR();
		mappingNum = alignRecord.getMappingNum();
		mappingWeight = alignRecord.getMappedReadsWeight();
		mapQuality = alignRecord.getMapQuality();
		setAlignmentBlocks(alignRecord.getAlignmentBlocks());
		score = alignRecord.getMapQuality();
	}
	
	public BedRecord(String bedline) {
		super();
		readLineInfo = bedline;
		String[] ss = bedline.split("\t");
		align.setChrId(ss[COL_CHRID]);
		//Bed的起点一般要加上1
		if (ss.length < COL_END+1) {
			align.setStartAbs(Integer.parseInt(ss[COL_START]) + 1);
			align.setEndAbs(-100000);
			align.setCis5to3(true);
		} else {
			align.setStartEndLoc(Integer.parseInt(ss[COL_START]) + 1, Integer.parseInt(ss[COL_END]));
		}
		if (ss.length > COL_NAME && ss[COL_NAME] != null && !ss[COL_NAME].equals("")) {
			seqName = ss[COL_NAME];
		}
		if (ss.length > COL_SCORE && ss[COL_SCORE] != null && !ss[COL_SCORE].equals("")) {
			try { score = Double.parseDouble(ss[COL_SCORE]); } catch (Exception e) {}
		}
		if (ss.length > COL_STRAND && ss[COL_STRAND] != null && !ss[COL_STRAND].equals("")) {
			setCis5to3(ss[COL_STRAND]);
		}
		//将序列装入bedrecord，并且不进行反向
		if (ss.length > COL_SEQ && ss[COL_SEQ] != null && !ss[COL_SEQ].equals("")) {
			try { seqFasta =new SeqFasta("", ss[COL_SEQ]); } catch (Exception e) {  }
		}
		if (ss.length > COL_MAPNUM && ss[COL_MAPNUM] != null && !ss[COL_MAPNUM].equals("")) {
			try { 	mappingNum = Integer.parseInt(ss[COL_MAPNUM]); } catch (Exception e) { 	}
		}
		if (ss.length > COL_CIGAR && ss[COL_CIGAR] != null && !ss[COL_CIGAR].equals("")) {
			try { CIGAR = ss[COL_CIGAR]; 	} catch (Exception e) { }
		}
		if (ss.length > COL_MAPWEIGHT && ss[COL_MAPWEIGHT] != null && !ss[COL_MAPWEIGHT].equals("")) {
			try { mappingWeight = Integer.parseInt(ss[COL_MAPWEIGHT]); } catch (Exception e) {  }
		}
		if (ss.length > COL_MAPQ && ss[COL_MAPQ] != null && !ss[COL_MAPQ].equals("")) {
			try { mapQuality = Integer.parseInt(ss[COL_MAPQ]); } catch (Exception e) { }
		}
		if (ss.length > COL_READSNUM && ss[COL_READSNUM] != null && !ss[COL_READSNUM].equals("")) {
			try { readsNum = Integer.parseInt(ss[COL_READSNUM]); } catch (Exception e) { }
		}
		
		if (ss.length > COL_SPLIT_READS_LEN && ss[COL_SPLIT_READS_LEN] != null && !ss[COL_SPLIT_READS_LEN].equals("")) {
			try { splitLen = ss[COL_SPLIT_READS_LEN]; } catch (Exception e) {  }
		}
		if (ss.length > COL_SPLIT_READS_START && ss[COL_SPLIT_READS_START] != null && !ss[COL_SPLIT_READS_START].equals("")) {
			try { splitStart = ss[COL_SPLIT_READS_START]; } catch (Exception e) {  }
		}
	}
	/** 判定是否为bed文件，只需要判定chrID，start，end即可 */
	public static boolean isBedRecord(String bedline) {
		if (bedline == null) {
			return false;
		}
		String[] ss = bedline.split("\t");
		try {
			if (ss[COL_CHRID].trim().equals("")) {
				return false;
			}
			Integer.parseInt(ss[COL_START]);
			Integer.parseInt(ss[COL_END]);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public Align getAlign() {
		return align;
	}
	public void setAlign(Align align) {
		this.align = align;
	}
	public void setChrId(String chrId) {
		align.setChrId(chrId);
	}
	public void setStartEndLoc(int startLoc, int endLoc) {
		align.setStartEndLoc(startLoc, endLoc);
	}
	/** 是否为unique mapping，不是的话mapping到了几个不同的位点上去 */
	public void setMappingNum(int mappingNum) {
		this.mappingNum = mappingNum;
	}
	public void setMappingWeight(int mapWeight) {
		this.mappingWeight = mapWeight;
	}
	public void setReadsNum(int readsNum) {
		this.readsNum = readsNum;
	}
	public String getCIGAR() {
		return CIGAR;
	}
	
	/**
	 * 按照方向进行延长
	 * 如果序列比设定的长度要长，则跳过
	 * @param length
	 */
	public void extendTo(int length) {
		align.extendTo(length);
	}
	
	/** 是否为unique mapping，不是的话mapping到了几个不同的位点上去 */
	public Integer getMappingNum() {
		if (mappingNum == null) {
			return 1;
		}
		return mappingNum;
	}
	
	public int getMappedReadsWeight() {
		if (mappingWeight == null) {
			return 1;
		}
		return mappingWeight;
	}
	
	public Integer getMapQuality() {
		if (mapQuality == null) {
			return 30;
		}
		return mapQuality;
	}
	/** 该bed文件是否被割成了一段一段的 */
	public boolean isJunctionCovered() {
		if (splitLen != null && !splitLen.equals("") && splitLen.contains(",")
				&& splitStart != null && !splitStart.equals("") && splitStart.contains(",")
				) {
			return true;
		}
		return false;
	}
	public void setAlignmentBlocks(ArrayList<? extends Alignment> lsAlign) {
		if (lsAlign.size() <= 0) {
			return;
		}
		
		StringBuilder sbSplitLen = new StringBuilder(lsAlign.get(0).getLength());
		StringBuilder sbSplitStart = new StringBuilder(0);

		for (int i = 1; i < lsAlign.size(); i++) {
			Alignment alignment = lsAlign.get(i);
			sbSplitStart.append("," + (alignment.getStartAbs() - getStartAbs()));
			sbSplitLen.append("," + alignment.getLength());
		}
		splitLen = sbSplitLen.toString();
		splitStart = sbSplitStart.toString();
	}
	/** 如果是mapping到junction上去，一条bed文件记录会被切成被切成的几块的样子保存在这里。
	 * 也就是一段一段的bed，那么返回每一段的信息，
	 * 都是绝对坐标，从1开始
	 * @return
	 */
	public ArrayList<Align> getAlignmentBlocks() {
		ArrayList<Align> lsStartEnd = new ArrayList<Align>();
		if (splitLen == null || splitLen.equals("") || !splitLen.contains(",")) {
			Align align = new Align(getChrId(), getStartCis(), getEndCis());
			align.setCis5to3(isCis5to3());
			lsStartEnd.add(align);
			return lsStartEnd;
		}
		String[] splitLenArray = splitLen.trim().split(",");
		String[] splitLocArray = splitStart.trim().split(",");
		for (int i = 0; i < splitLenArray.length; i++) {
			int start = getStartAbs() + Integer.parseInt(splitLocArray[i]);
			int end = start + Integer.parseInt(splitLenArray[i]) - 1;
			Align align = new Align(getChrId(), start, end);
			align.setCis5to3(isCis5to3());
			lsStartEnd.add(align);
		}
		return lsStartEnd;
	}
	/**
	 * 本位点的包含了几条overlap的序列
	 * 如果没有这一项，则返回1
	 * @return
	 */
	public int getReadsNum() {
		if (readsNum == null) {
			return 1;
		}
		return readsNum;
	}
	public void setCis5to3(Boolean strand) {
		align.setCis5to3(strand);
	}
	/**
	 * "+"或"-"
	 * @param strand
	 */
	public void setCis5to3(String strand) {
		if (strand == null || strand.equals("") ) {
			align.setCis5to3(null);
		} else if ( strand.equals("+") ) {
			align.setCis5to3(true);
		} else if (strand.equals("-")) {
			align.setCis5to3(false);
		} else {
			throw new RuntimeException("unknown strand " + strand);
		}
	}
	/**
	 * 必须存在该字符，不能为空
	 * "+"或"-"
	 * @param strand
	 */
	public void setCis5to3(char strand) {
		if ( strand == '+') {
			align.setCis5to3(true);
		} if (strand == '-') {
			align.setCis5to3(false);
		} else {
			throw new RuntimeException("unknown strand " + strand);
		}
	}
	public void setSeqFasta(SeqFasta seqFasta) {
		this.seqFasta = seqFasta;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public void setSeqName(String seqName) {
		this.seqName = seqName;
	}
	public void setCIGAR(String cIGAR) {
		CIGAR = cIGAR;
	}
	public void setMapQuality(Integer mapQuality) {
		this.mapQuality = mapQuality;
	}
	/** 返回原始文件中读取时的信息 */
	public String getRawStringInfo() {
		return readLineInfo;
	}
	/**
	 * 返回本bedrecord所对应的line信息
	 * 如果出错，则返回空字符串""
	 */
	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toStringSimple() {
		return toString(true);
	}
	
	/**
	 * 返回本bedrecord所对应的line信息
	 * 如果出错，则返回空字符串""
	 */
	private String toString(boolean isSimple) {
		String[] strings = new String[ALL_COLNUM];
		strings[COL_CHRID] = getChrId();
		//Bed的起点是从0开始计算的，所以实际位点要减去1
		strings[COL_START] = (align.getStartAbs() - 1) + "";
		if (align.getEndCis() < 0) {
			strings[COL_END] = "";
		} else {
			strings[COL_END] = align.getEndAbs() +"";
		}
		strings[COL_CIGAR] = CIGAR;
		strings[COL_MAPNUM] = mappingNum + "";
		
		if (isSimple || getSeqFasta() == null || getSeqFasta().toString() == null || getSeqFasta().toString().equals("")) {
			strings[COL_SEQ] = "";
		} else {
			strings[COL_SEQ] = getSeqFasta().toString();
		}
		
		if (!isSimple) {
			strings[COL_NAME] = seqName;
			strings[COL_MAPWEIGHT] = mappingWeight + "";
			strings[COL_MAPQ] = mapQuality + "";
		}
		
		strings[COL_SCORE] = score + "";
		strings[COL_READSNUM] = readsNum + "";
		strings[COL_SPLIT_READS_LEN] = splitLen + "";
		strings[COL_SPLIT_READS_START] = splitStart + "";
		
		if (align.isCis5to3() != null) {
			if (align.isCis5to3() && !StringOperate.isRealNull(strings[3]) || !StringOperate.isRealNull(strings[4])) {
				strings[COL_STRAND] = "+";
			} else {
				strings[COL_STRAND] = "-";
			}
		}
		
		//获得本列最长的非null列数
		int resultColNum = 0;
		for (int i = strings.length - 1; i >= 0; i--) {
			if (!StringOperate.isRealNull(strings[i])) {
				resultColNum = i + 1;
				break;
			}
		}
		//出错，就是本行没东西
		if (resultColNum < 2) {
			return "";
		}
		
		for (int i = 1; i < strings.length; i++) {
			if (StringOperate.isRealNull(strings[i])) {
				strings[i] = "";
			}
		}
		
		String result  = ArrayOperate.cmbString(strings, "\t").trim();
		return result;
	}
	@Override
	public BedRecord clone() {
		try {
			BedRecord bedRecord = (BedRecord) super.clone();
			bedRecord.align = align.clone();
			bedRecord.seqFasta = seqFasta.clone();
			bedRecord.CIGAR = CIGAR;
			bedRecord.mappingNum = mappingNum;
			bedRecord.mapQuality = mapQuality;
			bedRecord.readsNum = readsNum;
			bedRecord.mappingWeight = mappingWeight;
			bedRecord.splitLen = splitLen;
			bedRecord.splitStart = splitStart;
			bedRecord.readLineInfo = readLineInfo;
			return bedRecord;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	/**
	 * 返回第一个记载的bedrecord 没有mapping上就返回null
	 * */
	public FastQRecord toFastQRecord() {
		FastQRecord fastQRecord = new FastQRecord();
		fastQRecord.setName(getName());
		fastQRecord.setSeq(getSeqFasta().toString());
		fastQRecord.setModifyQuality(true);
		return fastQRecord;
	}
	/**
	 * 提取获得的序列和fastq是一致的
	 */
	@Override
	public SeqFasta getSeqFasta() {
		return seqFasta;
	}

	@Override
	public boolean isMapped() {
		return true;
	}
	@Override
	public boolean isUniqueMapping() {
		if (getMappingNum() == 1) {
			return true;
		}
		return false;
	}
	@Override
	public int getLengthReal() {
		SeqFasta seqFasta = getSeqFasta();
		if (seqFasta != null) {
			return seqFasta.Length();
		}
		return 0;
	}
	@Override
	public int getStartAbs() {
		return align.getStartAbs();
	}
	@Override
	public int getEndAbs() {
		return align.getEndAbs();
	}
	@Override
	public int getStartCis() {
		return align.getStartCis();
	}
	@Override
	public int getEndCis() {
		return align.getEndCis();
	}
	@Override
	public Boolean isCis5to3() {
		return align.isCis5to3();
	}
	@Override
	public int getLength() {
		return align.getLength();
	}
	@Override
	public String getChrId() {
		return align.getChrId();
	}
	@Override
	public String getName() {
		return align.getName();
	}
	
}
