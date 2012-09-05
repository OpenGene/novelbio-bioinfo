package com.novelbio.analysis.seq;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.fasta.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.Alignment;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.SiteInfo;
import com.novelbio.analysis.seq.mapping.Align;
 /**
  * BedSeqÿһ�е���Ϣ<br>
  * ���� bamToBed�� 12����Ϣ��ʽ
  * @author zong0jie
  *
  */
public class BedRecord extends SiteInfo implements AlignRecord{
	static private Logger logger = Logger.getLogger(BedRecord.class);
	
	static final int COL_CHRID = 0;
	static final int COL_START = 1;
	static final int COL_END = 2;
	static final int COL_NAME = 3;
	static final int COL_SCORE = 4;
	static final int COL_STRAND = 5;
	static final int COL_CIGAR = 6;
	public static final int COL_MAPNUM = 7;
	static final int COL_SEQ = 8;
	/** �Ƿ�Ϊunique mapping���� */
	static final int COL_MAPQ = 9;
	static final int COL_SPLIT_READS_LEN = 10;
	static final int COL_SPLIT_READS_START = 11;
	static final int COL_READSNUM = 12;

	/**
	 * �������ܹ��еļ���������������ӻ���ɾ�����У�����Ҫ��Ӧ������
	 */
	static final int ALL_COLNUM = 13;
	
	/**
	 * mapping���˼�����ȥ
	 */
	Integer mappingNum = null;
	/**
	 * ��λ��İ����˼���overlap������
	 */
	Integer readsNum = null;
	String CIGAR = null;
	Integer mapQuality = null;
	
	String readLineInfo = "";
	
	/** ����9,53,28 */
	String splitLen;
	/** 0,2134,11171 */
	String splitStart;
	
	public BedRecord() {
		super(null);
	}
	public BedRecord(String bedline) {
		super();
		readLineInfo = bedline;
		String[] ss = bedline.split("\t");
		setRefID(ss[COL_CHRID]);
		//Bed�����һ��Ҫ����1
		setStartEndLoc(Integer.parseInt(ss[COL_START]) + 1, Integer.parseInt(ss[COL_END]));
		if (ss.length > COL_NAME && ss[COL_NAME] != null && !ss[COL_NAME].equals("")) {
			setName(ss[COL_NAME]);
		}
		if (ss.length > COL_SCORE && ss[COL_SCORE] != null && !ss[COL_SCORE].equals("")) {
			try { score = Double.parseDouble(ss[COL_SCORE]); } catch (Exception e) {}
		}
		if (ss.length > COL_STRAND && ss[COL_STRAND] != null && !ss[COL_STRAND].equals("")) {
			setCis5to3(ss[COL_STRAND]);
		}
		//������װ��bedrecord�����Ҳ����з���
		if (ss.length > COL_SEQ && ss[COL_SEQ] != null && !ss[COL_SEQ].equals("")) {
			try { setSeq(new SeqFasta("", ss[COL_SEQ]), false); } catch (Exception e) {  }
		}
		if (ss.length > COL_MAPNUM && ss[COL_MAPNUM] != null && !ss[COL_MAPNUM].equals("")) {
			try { 	mappingNum = Integer.parseInt(ss[COL_MAPNUM]); } catch (Exception e) { 	}
		}
		if (ss.length > COL_CIGAR && ss[COL_CIGAR] != null && !ss[COL_CIGAR].equals("")) {
			try { CIGAR = ss[COL_CIGAR]; 	} catch (Exception e) { }
			
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
	/** �Ƿ�Ϊunique mapping�����ǵĻ�mapping���˼�����ͬ��λ����ȥ */
	public void setMappingNum(int mappingNum) {
		this.mappingNum = mappingNum;
	}
	public void setReadsNum(int readsNum) {
		this.readsNum = readsNum;
	}
	public String getCIGAR() {
		return CIGAR;
	}
	/** �Ƿ�Ϊunique mapping�����ǵĻ�mapping���˼�����ͬ��λ����ȥ */
	public Integer getMappingNum() {
		if (mappingNum == null) {
			return 1;
		}
		return mappingNum;
	}
	public Integer getMapQuality() {
		if (mapQuality == null) {
			return 30;
		}
		return mapQuality;
	}
	/** ��bed�ļ��Ƿ񱻸����һ��һ�ε� */
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
		
		splitLen = lsAlign.get(0).Length() + "";
		splitStart = "0";
		for (int i = 1; i < lsAlign.size(); i++) {
			Alignment alignment = lsAlign.get(i);
			splitStart = splitStart + "," + (alignment.getStartAbs() - getStartAbs());
			splitLen = splitLen + "," + alignment.Length();
		}
	}
	/** �����mapping��junction��ȥ��һ��bed�ļ���¼�ᱻ�гɱ��гɵļ�������ӱ��������
	 * Ҳ����һ��һ�ε�bed����ô����ÿһ�ε���Ϣ��
	 * ���Ǿ������꣬��1��ʼ
	 * @return
	 */
	public ArrayList<Align> getAlignmentBlocks() {
		ArrayList<Align> lsStartEnd = new ArrayList<Align>();
		if (splitLen == null || splitLen.equals("") || !splitLen.contains(",")) {
			Align align = new Align(getRefID(), getStartCis(), getEndCis());
			align.setCis5to3(isCis5to3());
			lsStartEnd.add(align);
			return lsStartEnd;
		}
		String[] splitLenArray = splitLen.trim().split(",");
		String[] splitLocArray = splitStart.trim().split(",");
		for (int i = 0; i < splitLenArray.length; i++) {
			int start = getStartAbs() + Integer.parseInt(splitLocArray[i]);
			int end = start + Integer.parseInt(splitLenArray[i]) - 1;
			Align align = new Align(getRefID(), start, end);
			align.setCis5to3(isCis5to3());
			lsStartEnd.add(align);
		}
		return lsStartEnd;
	}
	/**
	 * ��λ��İ����˼���overlap������
	 * ���û����һ��򷵻�1
	 * @return
	 */
	public int getReadsNum() {
		if (readsNum == null) {
			return 1;
		}
		return readsNum;
	}
	/**
	 * "+"��"-"
	 * @param strand
	 */
	public void setCis5to3(String strand) {
		if (strand == null || strand.equals("") ) {
			this.cis5to3 = null;
		}
		else if ( strand.equals("+") ) {
			this.cis5to3 = true;
		}
		else if (strand.equals("-")) {
			this.cis5to3 = false;
		}
		else {
			logger.equals("����δ֪strand");
		}
	}
	/**
	 * ������ڸ��ַ�������Ϊ��
	 * "+"��"-"
	 * @param strand
	 */
	public void setCis5to3(char strand) {
		if ( strand == '+') {
			this.cis5to3 = true;
		}
		if (strand == '-') {
			this.cis5to3 = false;
		}
		else {
			logger.equals("����δ֪strand");
		}
	}
	public void setCIGAR(String cIGAR) {
		CIGAR = cIGAR;
	}
	public void setMapQuality(Integer mapQuality) {
		this.mapQuality = mapQuality;
	}
	/** ����ԭʼ�ļ��ж�ȡʱ����Ϣ */
	public String getRawStringInfo() {
		return readLineInfo;
	}
	/**
	 * ���ر�bedrecord����Ӧ��line��Ϣ
	 * ��������򷵻ؿ��ַ���""
	 */
	@Override
	public String toString() {
		String[] strings = new String[ALL_COLNUM];
		strings[COL_CHRID] = refID;
		//Bed������Ǵ�0��ʼ����ģ�����ʵ��λ��Ҫ��ȥ1
		strings[COL_START] = (startLoc - 1) + "";
		strings[COL_END] = endLoc +"";
		strings[COL_CIGAR] = CIGAR;
		strings[COL_MAPNUM] = mappingNum + "";
		
		if (getSeqFasta() == null || getSeqFasta().toString() == null || getSeqFasta().toString().equals(""))
			strings[COL_SEQ] = null;
		else
			strings[COL_SEQ] = getSeqFasta().toString();
		
		strings[COL_MAPQ] = mapQuality + "";
		strings[COL_NAME] = name;
		strings[COL_SCORE] = score + "";
		strings[COL_READSNUM] = readsNum + "";
		strings[COL_SPLIT_READS_LEN] = splitLen + "";
		strings[COL_SPLIT_READS_START] = splitStart + "";
		
		if (cis5to3 != null) {
			if (cis5to3) {
				strings[COL_STRAND] = "+";
			}
			else {
				strings[COL_STRAND] = "-";
			}
		}
		
		//��ñ�����ķ�null����
		int resultColNum = 0;
		for (int i = strings.length - 1; i >= 0; i--) {
			if (strings[i] != null && !strings[i].equals("null")) {
				resultColNum = i + 1;
				break;
			}
		}
		//�������Ǳ���û����
		if (resultColNum < 3) {
			return "";
		}
		String result  = strings[0];
		for (int i = 1; i < resultColNum; i++) {
			if (strings[i] != null && !strings[i].equals("null")) {
				result = result + "\t" + strings[i];
			}
			else
				result = result + "\t" + "";
		}
		return result;
	}
	
	@Override
	public BedRecord clone() {
		BedRecord bedRecord = (BedRecord) super.clone();
		bedRecord.CIGAR = CIGAR;
		bedRecord.mappingNum = mappingNum;
		bedRecord.mapQuality = mapQuality;
		bedRecord.readsNum = readsNum;
		return bedRecord;
	}
}
