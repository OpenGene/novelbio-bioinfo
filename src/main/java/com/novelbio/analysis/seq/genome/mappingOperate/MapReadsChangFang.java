package com.novelbio.analysis.seq.genome.mappingOperate;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsChangFang.CGmethyType;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;

/**
 * ������Ǳߣ���������ɵ�
 * @author zong0jie
 *
 */
public class MapReadsChangFang extends MapReads {
	public static void main(String[] args) {
		logger.debug("a");
		logger.info("a");
	}
	private static Logger logger = Logger.getLogger(MapReadsChangFang.class);
	
	PatternOperate patternOperate = new PatternOperate("EviCode \"(.+?)\"", false);
	String GTFyanghongxing;
	
	 CGmethyType cGmethyType = CGmethyType.ALL;
	
	@Deprecated
	public void setBedSeq(String bedSeqFile) { }
	@Deprecated
	public void setAlignSeqReader(AlignSeq alignSeqReader) { }
	
	public void setGTFyanghongxing(String gTFyanghongxing) {
		GTFyanghongxing = gTFyanghongxing;
	}
	private void setTagLength() {
		tagLength = 1;//��ReadMapFile������ֵ
	}
	
	/** �趨Ҫͳ�Ƶ�CG���� */
	public void setcGmethyType(CGmethyType cGmethyType) {
		this.cGmethyType = cGmethyType;
	}
	
	@Override
	protected void ReadMapFileExp() throws Exception {
		setTagLength();
		allReadsNum = 0;
		int[] chrBpReads = null;//����ÿ��bp��reads�ۼ���
		String lastChr="";
		ChrMapReadsInfo chrMapReadsInfo = null;
		boolean flag = true;// ��û�и�Ⱦɫ��ʱ���Ϊfalse�����������и�Ⱦɫ���ϵ�����

		TxtReadandWrite txtGtfRead = new TxtReadandWrite(GTFyanghongxing, false);
		for (String gtfLines : txtGtfRead.readlines()) {
			if (gtfLines.split("\t")[2].equals("chromosome")) {
				continue;
			}
			GtfHongXingMethy gtfHongXingMethy = new GtfHongXingMethy(gtfLines, patternOperate);
			if (cGmethyType != CGmethyType.ALL && cGmethyType != gtfHongXingMethy.cgGmethyType) {
				continue;
			}
			String tmpChrID = gtfHongXingMethy.getRefID();
			if (!tmpChrID.equals(lastChr)) {
				if (!lastChr.equals("") && flag) { // ǰ���Ѿ�����һ��chrBpReads����ô��ʼ�ܽ����chrBpReads
					chrMapReadsInfo.sumChrBp(chrBpReads);
				}
				lastChr = tmpChrID;// ʵ�������³��ֵ�ChrID
				logger.debug(lastChr);
				
				Long chrLength = mapChrID2Len.get(lastChr.toLowerCase());
				flag = true;
				if (chrLength == null) {
					logger.error("����δ֪chrID "+lastChr);
					flag = false; continue;
				}

				chrBpReads = new int[(int) (chrLength + 1)];// ͬ��Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
				chrBpReads[0] = chrLength.intValue();
				chrMapReadsInfo = new ChrMapReadsInfo(lastChr, getChrLen(lastChr), invNum, summeryType, FormulatToCorrectReads);
				mapChrID2ReadsInfo.put(lastChr.toLowerCase(), chrMapReadsInfo);
			}
			
			if (flag == false) {//û�и�Ⱦɫ��������
				continue;
			}
			
			addLoc(gtfHongXingMethy, chrBpReads);
			allReadsNum = allReadsNum + gtfHongXingMethy.getReadsNum();
			suspendCheck();
			if (flagStop) {
				break;
			}
		}
		
		if (flag) {
			chrMapReadsInfo.sumChrBp(chrBpReads);
		}
	}
	
	/**
	 * ����������������Ϣ��ӵ�Ⱦɫ����ȥ
	 * @param gtfHongXingMethy
	 * @param chrBpReads ����ӵ�Ⱦɫ��
	 */
	protected void addLoc(GtfHongXingMethy gtfHongXingMethy, int[] chrBpReads) {
		int m = 0;
		int[] methInfo = gtfHongXingMethy.getMethyInfo();
		for (int i = gtfHongXingMethy.getStartAbs(); i <= gtfHongXingMethy.getEndAbs(); i++) {
			if (i >= chrBpReads.length) {
				logger.info("������Χ��" + i);
				break;
			}
			if (i < 0) {
				logger.info("������Χ��" + i);
				continue;
			}
			chrBpReads[i] = chrBpReads[i] + methInfo[m];
			m++;
		}
	}
	
	public static enum CGmethyType {
		CHG, CG, CHH, ALL
	}
}

class GtfHongXingMethy implements Alignment{
	private static Logger logger = Logger.getLogger(GtfHongXingMethy.class);
	boolean cis5to3;
	String chrID = "";
	int start = 0;
	int end = 0;
	String cpgType = "";
	int readsNum = 0;
	int methyScore = 0;
	CGmethyType cgGmethyType;
	PatternOperate patternOperate;

	/**
	 * @param gtfLines
	 * @param patternOperate һ�����趨Ϊ
	 * patterOperate = new PatternOperate("EviCode \"(.+?)\"", false);
	 */
	public GtfHongXingMethy(String gtfLines, PatternOperate patternOperate) {
		String[] ss = gtfLines.split("\t");
		chrID = ss[0];
		start = Integer.parseInt(ss[3]);
		end = start;
		cis5to3 = ss[6].equals("+");
		this.patternOperate = patternOperate;
		setEvidenceAndReadsNum(ss[8]);
		if (ss[7].equalsIgnoreCase("CG")) {
			cgGmethyType = CGmethyType.CG;
		} else if (ss[7].equalsIgnoreCase("CHG")) {
			cgGmethyType = CGmethyType.CHG;
		} else if (ss[7].equalsIgnoreCase("CHH")) {
			cgGmethyType = CGmethyType.CHH;
		} else {
			logger.error("����δ֪CG���ͣ�" + ss[7]);
		}
	}
	/**
	 * һ�����趨Ϊ
	 * patterOperate = new PatternOperate("EviCode \"(.+?)\"", false);
	 * @param patternOperate
	 */
	public void setPatternOperate(PatternOperate patternOperate) {
		this.patternOperate = patternOperate;
	}
	/**
	 * �������֣�Gene "AT1G01040"; GenePosition "-4691"; Site "CNNG(14):9,CNNG(15):7"; EviCode "6:16";
	 * @param evidenceStr
	 * @return
	 * 6��16����
	 */
	private void setEvidenceAndReadsNum(String evidenceStr) {
		String methyInfo = patternOperate.getPatFirst(evidenceStr, 1);
		String[] methyNum = methyInfo.split(",");
		for (String string : methyNum) {
			if (!string.contains(":")) {
				continue;
			}
			String[] methyEvidence2Num = string.split(":");
			int methyLevel = Integer.parseInt(methyEvidence2Num[0]);
			int readsNum = Integer.parseInt(methyEvidence2Num[1]);
			this.readsNum = this.readsNum + readsNum;
			this.methyScore = methyScore + methyLevel * readsNum;
		}
	}
	
	public int[] getMethyInfo() {
		int[] result = new int[1];
		result[0] = methyScore;
		return result;
	}
	public int getReadsNum() {
		return readsNum;
	}
	@Override
	public int getStartAbs() {
		return Math.min(start, end);
	}
	@Override
	public int getEndAbs() {
		return Math.max(start, end);
	}
	@Override
	public int getStartCis() {
		if (cis5to3) {
			return Math.min(start, end);
		} else {
			return Math.max(start, end);
		}
	}
	@Override
	public int getEndCis() {
		if (cis5to3) {
			return Math.max(start, end);
		} else {
			return Math.min(start, end);
		}
	}
	@Override
	public Boolean isCis5to3() {
		return cis5to3;
	}
	
	@Override
	public int Length() {
		return Math.abs(getStartAbs() - getEndAbs()) + 1;
	}
	
	/** Ĭ�Ϸ���Сд */
	@Override
	public String getRefID() {
		return chrID.toLowerCase();
	}
	
}
