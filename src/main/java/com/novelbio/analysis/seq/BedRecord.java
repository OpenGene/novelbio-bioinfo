package com.novelbio.analysis.seq;

import org.apache.log4j.Logger;
 /**
  * BedSeqÿһ�е���Ϣ
  * @author zong0jie
  *
  */
public class BedRecord {
	static private Logger logger = Logger.getLogger(BedRecord.class);
	
	
	static final int COL_CHRID = 0;
	static final int COL_START = 1;
	static final int COL_END = 2;
	static final int COL_NAME = 3;
	static final int COL_SCORE = 4;
	static final int COL_STRAND = 5;
	static final int COL_MAPNUM = 6;
	static final int COL_CIGAR = 7;
	static final int COL_MAPQ = 8;
	/**
	 * �������ܹ��еļ���������������ӻ���ɾ�����У�����Ҫ��Ӧ������
	 */
	static final int ALL_COLNUM = 10;
	/**
	 * chrID
	 */
	String refID = "";
	int start = 0;
	int end = 0;
	/**
	 * null��ʾû�з���
	 */
	Boolean strand = null;
	String readsName = null;
	/**
	 * �����
	 */
	Double score = null;
	
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
	
	
	
	public BedRecord() { }
	public BedRecord(String bedline) {
		String[] ss = bedline.split("\t");
		refID = ss[COL_CHRID];
		start = Integer.parseInt(ss[COL_START]);
		end = Integer.parseInt(ss[COL_END]);
		if (ss.length > COL_NAME && ss[COL_NAME] != null && !ss[COL_NAME].equals("")) {
			readsName = ss[COL_NAME];
		}
		if (ss.length > COL_SCORE && ss[COL_SCORE] != null && !ss[COL_SCORE].equals("")) {
			score = Double.parseDouble(ss[COL_SCORE]);
		}
		if (ss.length > COL_STRAND && ss[COL_STRAND] != null && !ss[COL_STRAND].equals("")) {
			setStrand(ss[COL_STRAND]);
		}
		if (ss.length > COL_MAPNUM && ss[COL_MAPNUM] != null && !ss[COL_MAPNUM].equals("")) {
			mappingNum = Integer.parseInt(ss[COL_MAPNUM]);
		}
		if (ss.length > COL_CIGAR && ss[COL_CIGAR] != null && !ss[COL_CIGAR].equals("")) {
			CIGAR = ss[COL_CIGAR];
		}
		if (ss.length > COL_MAPQ && ss[COL_MAPQ] != null && !ss[COL_MAPQ].equals("")) {
			mapQuality = Integer.parseInt(ss[COL_MAPQ]);
		}
	}
	
	public void setEnd(int end) {
		this.end = end;
	}
	public void setMappingNum(int mappingNum) {
		this.mappingNum = mappingNum;
	}
	public void setReadsName(String readsName) {
		this.readsName = readsName;
	}
	public void setReadsNum(int readsNum) {
		this.readsNum = readsNum;
	}
	public void setRefID(String refID) {
		this.refID = refID;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public void setStrand(Boolean strand) {
		this.strand = strand;
	}
	public String getCIGAR() {
		return CIGAR;
	}
	public int getEnd() {
		return end;
	}
	public Integer getMappingNum() {
		return mappingNum;
	}
	public Integer getMapQuality() {
		return mapQuality;
	}
	public String getReadsName() {
		return readsName;
	}
	public Integer getReadsNum() {
		return readsNum;
	}
	public String getRefID() {
		return refID;
	}
	public Double getScore() {
		return score;
	}
	public int getStart() {
		return start;
	}
	public Boolean isCis() {
		return strand;
	}
	public int getMiddle() {
		return (start + end)/2;
	}
	/**
	 * "+"��"-"
	 * @param strand
	 */
	public void setStrand(String strand) {
		if (strand == null || strand.equals("") ) {
			this.strand = null;
		}
		else if ( strand.equals("+") ) {
			this.strand = true;
		}
		else if (strand.equals("-")) {
			this.strand = false;
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
	public void setStrand(char strand) {
		if ( strand == '+') {
			this.strand = true;
		}
		if (strand == '-') {
			this.strand = false;
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
	/**
	 * ���շ�������ӳ�
	 * ������б��趨�ĳ���Ҫ����������
	 * @param length
	 */
	public void extend(int length) {
		if (Length() >= length) {
			return;
		}
		if (strand == null || strand) {
			end = start + length;
		}
		else {
			start = end - length;
		}
	}
	public int Length() {
		return Math.abs(end - start);
	}
	/**
	 * ���ر�bedrecord����Ӧ��line��Ϣ
	 * ��������򷵻ؿ��ַ���""
	 */
	@Override
	public String toString() {
		String[] strings = new String[ALL_COLNUM];
		strings[COL_CHRID] = refID;
		strings[COL_START] = start + "";
		strings[COL_END] = end+"";
		strings[COL_CIGAR] = CIGAR;
		strings[COL_MAPNUM] = mappingNum + "";
		strings[COL_MAPQ] = mapQuality + "";
		strings[COL_NAME] = readsName;
		strings[COL_SCORE] = score + "";
		
		if (strand != null) {
			if (strand) {
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
}
