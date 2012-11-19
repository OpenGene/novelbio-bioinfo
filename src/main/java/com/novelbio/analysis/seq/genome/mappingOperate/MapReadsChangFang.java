package com.novelbio.analysis.seq.genome.mappingOperate;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.AlignSeq;
import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.genome.mappingOperate.MapReadsAbs.MapReadsProcessInfo;
import com.novelbio.base.dataOperate.TxtReadandWrite;

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
		
		TxtReadandWrite txtGtfRead = new TxtReadandWrite(GTFyanghongxing, false);
		for (String gtfLines : txtGtfRead.readlines()) {
			if (gtfLines.split("\t")[2].equals("chromosome")) {
				continue;
			}
			GtfHongXingMethy gtfHongXingMethy = new GtfHongXingMethy(gtfLines);
			boolean flag = true;// 当没有该染色体时标记为false并且跳过所有该染色体上的坐标
						
			String tmpChrID = gtfHongXingMethy.getChrID().toLowerCase();
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
			if (flag == false) //没有该染色体则跳过
				continue;
			tmpOld = addLoc(alignRecord, uniqReads, tmpOld, startCod, FilteredStrand, chrBpReads,chrMapReadsInfo);
			
			suspendCheck();
			if (flagStop) {
				break;
			}
			readsSize = readsSize + alignRecord.getRawStringInfo().getBytes().length;
			if (readsNum%1000 == 0) {
				MapReadsProcessInfo mapReadsProcessInfo = new MapReadsProcessInfo(readsSize);
				setRunInfo(mapReadsProcessInfo);
			}
		}
		if (flag) {
			chrMapReadsInfo.sumChrBp(chrBpReads);
		}
		
	}


}

class GtfHongXingMethy {
	boolean cis5to3;
	String chrID;
	int start = 0;
	int end = 0;
	String cpgType = "";
	/** 0到6之间 */
	int methyLevel = 0;
	

	public GtfHongXingMethy(String gtfLines) {
		
	}
	
	public String getChrID() {
		return chrID;
	}
}
