package com.novelbio.analysis.seq.genome.mappingOperate;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genome.GffChrAbs;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs.MapReadsProcessInfo;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.species.Species;

/**
 * 给马红那边，杨红星生成的
 * @author zong0jie
 *
 */
public class MapReadsChangFang extends MapReads {
	private static Logger logger = Logger.getLogger(MapReadsChangFang.class);
	
	String GTFyanghongxing;
	
	@Deprecated
	public void setBedSeq(String bedSeqFile) { }
	@Deprecated
	public void setAlignSeqReader(AlignSeq alignSeqReader) { }
	
	public void setGTFyanghongxing(String gTFyanghongxing) {
		GTFyanghongxing = gTFyanghongxing;
	}
	
	@Override
	protected void ReadMapFileExp() throws Exception {
		allReadsNum = 0;
		int[] chrBpReads = null;//保存每个bp的reads累计数
		String lastChr="";
		ChrMapReadsInfo chrMapReadsInfo = null;
		boolean flag = true;// 当没有该染色体时标记为false并且跳过所有该染色体上的坐标

		TxtReadandWrite txtGtfRead = new TxtReadandWrite(GTFyanghongxing, false);
		for (String gtfLines : txtGtfRead.readlines()) {
			if (gtfLines.split("\t")[2].equals("chromosome")) {
				continue;
			}
			GtfHongXingMethy gtfHongXingMethy = new GtfHongXingMethy(gtfLines);						
			String tmpChrID = gtfHongXingMethy.getRefID();
			if (!tmpChrID.equals(lastChr)) {				
				if (!lastChr.equals("") && flag) { // 前面已经有了一个chrBpReads，那么开始总结这个chrBpReads
					chrMapReadsInfo.sumChrBp(chrBpReads);
				}
				lastChr = tmpChrID;// 实际这是新出现的ChrID
				logger.error(lastChr);
				
				Long chrLength = mapChrID2Len.get(lastChr.toLowerCase());
				flag = true;
				if (chrLength == null) {
					logger.error("出现未知chrID "+lastChr);
					flag = false; continue;
				}

				chrBpReads = new int[(int) (chrLength + 1)];// 同样为方便，0位记录总长度。这样实际bp就是实际长度
				chrBpReads[0] = chrLength.intValue();
				chrMapReadsInfo = new ChrMapReadsInfo(lastChr, getChrLen(lastChr), invNum, summeryType, FormulatToCorrectReads);
				mapChrID2ReadsInfo.put(lastChr.toLowerCase(), chrMapReadsInfo);
			}
			
			if (flag == false) {//没有该染色体则跳过
				continue;
			}
			
			addLoc(gtfHongXingMethy, chrBpReads);
			
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
	 * 将杨红星类里面的信息添加到染色体上去
	 * @param gtfHongXingMethy
	 * @param chrBpReads 待添加的染色体
	 */
	protected void addLoc(GtfHongXingMethy gtfHongXingMethy, int[] chrBpReads) {
		int m = 0;
		int[] methInfo = gtfHongXingMethy.getMethyInfo();
		for (int i = gtfHongXingMethy.getStartAbs(); i <= gtfHongXingMethy.getEndAbs(); i++) {
			if (i >= chrBpReads.length) {
				logger.info("超出范围：" + i);
				break;
			}
			if (i < 0) {
				logger.info("超出范围：" + i);
				continue;
			}
			chrBpReads[i] = chrBpReads[i] + methInfo[m];
			m++;
		}
	}
}

class GtfHongXingMethy implements Alignment{
	boolean cis5to3;
	String chrID = "";
	int start = 0;
	int end = 0;
	String cpgType = "";
	
	/** 
	 * int 0:甲基化level (0-6之间)
	 * int 1:甲基化长度
	 */
	ArrayList<int[]> lsMethyLevel2Num = new ArrayList<int[]>();

	public GtfHongXingMethy(String gtfLines) {
		String[] ss = gtfLines.split("\t");
		chrID = ss[0];
		start = Integer.parseInt(ss[3]);
		cis5to3 = ss[6].equals("+");
		//TODO
	}

	public int[] getMethyInfo() {
		int methLen = 0;
		for (int[] methyLevel2Num : lsMethyLevel2Num) {
			methLen = methLen + methyLevel2Num[1];
		}
		int[] result = new int[methLen];
		int m = 0;//result的计数器
		for (int i = 0; i < lsMethyLevel2Num.size(); i++) {
			int[] methyLevel2Num = lsMethyLevel2Num.get(i);
			for (int j = 0; j < methyLevel2Num[1]; j++) {
				result[m] = methyLevel2Num[0];
				m++;
			}
		}
		return result;
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
	
	/** 默认返回小写 */
	@Override
	public String getRefID() {
		return chrID.toLowerCase();
	}
}
