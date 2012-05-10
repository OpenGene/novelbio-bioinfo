package com.novelbio.analysis.seq;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genomeNew.getChrSequence.SeqFasta;
import com.novelbio.analysis.seq.genomeNew.mappingOperate.MapInfo;
 /**
  * BedSeq每一行的信息
  * @author zong0jie
  *
  */
public class BedRecord extends MapInfo {
	static private Logger logger = Logger.getLogger(BedRecord.class);
	
	
	static final int COL_CHRID = 0;
	static final int COL_START = 1;
	static final int COL_END = 2;
	static final int COL_NAME = 3;
	static final int COL_SCORE = 4;
	static final int COL_STRAND = 5;
	static final int COL_SEQ = 6;
	public static final int COL_MAPNUM = 7;
	static final int COL_CIGAR = 8;
	static final int COL_MAPQ = 9;
	static final int COL_READSNUM = 10;

	/**
	 * 对上面总共列的计数，上面如果增加或者删减了列，这里要相应的修正
	 */
	static final int ALL_COLNUM = 11;
	
	/**
	 * mapping到了几个上去
	 */
	Integer mappingNum = null;
	/**
	 * 本位点的包含了几条overlap的序列
	 */
	Integer readsNum = null;
	String CIGAR = null;
	Integer mapQuality = null;
	
	
	
	public BedRecord() {
		super(null);
	}
	public BedRecord(String bedline) {
		super();
		String[] ss = bedline.split("\t");
		setRefID(ss[COL_CHRID]);
		setStartEndLoc(Integer.parseInt(ss[COL_START]), Integer.parseInt(ss[COL_END]));
		if (ss.length > COL_NAME && ss[COL_NAME] != null && !ss[COL_NAME].equals("")) {
			setName(ss[COL_NAME]);
		}
		if (ss.length > COL_SCORE && ss[COL_SCORE] != null && !ss[COL_SCORE].equals("")) {
			try { score = Double.parseDouble(ss[COL_SCORE]); } catch (Exception e) {}
		}
		if (ss.length > COL_STRAND && ss[COL_STRAND] != null && !ss[COL_STRAND].equals("")) {
			setCis5to3(ss[COL_STRAND]);
		}
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
	}
	
	public void setMappingNum(int mappingNum) {
		this.mappingNum = mappingNum;
	}
	public void setReadsNum(int readsNum) {
		this.readsNum = readsNum;
	}
	public String getCIGAR() {
		return CIGAR;
	}
	public Integer getMappingNum() {
		return mappingNum;
	}
	public Integer getMapQuality() {
		return mapQuality;
	}

	/**
	 * 如果没有这一项，则返回1
	 * @return
	 */
	public int getReadsNum() {
		if (readsNum == null) {
			return 1;
		}
		return readsNum;
	}
	/**
	 * "+"或"-"
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
//			logger.equals("出现未知strand");
		}
	}
	/**
	 * 必须存在该字符，不能为空
	 * "+"或"-"
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
			logger.equals("出现未知strand");
		}
	}
	public void setCIGAR(String cIGAR) {
		CIGAR = cIGAR;
	}
	public void setMapQuality(Integer mapQuality) {
		this.mapQuality = mapQuality;
	}
	/**
	 * 返回本bedrecord所对应的line信息
	 * 如果出错，则返回空字符串""
	 */
	@Override
	public String toString() {
		String[] strings = new String[ALL_COLNUM];
		strings[COL_CHRID] = refID;
		strings[COL_START] = startLoc + "";
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
		if (cis5to3 != null) {
			if (cis5to3) {
				strings[COL_STRAND] = "+";
			}
			else {
				strings[COL_STRAND] = "-";
			}
		}
		
		//获得本列最长的非null列数
		int resultColNum = 0;
		for (int i = strings.length - 1; i >= 0; i--) {
			if (strings[i] != null && !strings[i].equals("null")) {
				resultColNum = i + 1;
				break;
			}
		}
		//出错，就是本行没东西
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
