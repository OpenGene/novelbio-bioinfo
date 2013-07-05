package com.novelbio.analysis.seq.sam.pileup;

import java.util.Queue;

import com.novelbio.analysis.seq.mapping.Align;
import com.novelbio.analysis.seq.sam.SamReader;
import com.novelbio.analysis.seq.sam.SamRecord;
import com.novelbio.base.multithread.RunProcess;

public class SamPileupReading extends RunProcess<Integer> {
	Queue<SamRecord> queueSamRecord;
	Align readRegion;
	Align snpRegion;
	SamReader samReader;
	boolean changeChrome = false;
	String chrThis = "";
	
	
	public SamPileupReading(String samFile) {
		samReader = new SamReader(samFile);
	}
	public void setQueueSamRecord(Queue<SamRecord> queueSamRecord) {
		this.queueSamRecord = queueSamRecord;
	}
	/** 读取的区域，不设定就从头读到尾 */
	public void setReadRegion(Align align) {
		this.readRegion = align;
	}
	/** 本次待分析的区域，应该要比实际分析区域向后延长个200bp
	 * 只能从前向后设定，不能反着来 
	 */
	public void setSnpRegion(Align snpRegion) {
		this.snpRegion = snpRegion;
	}
	
	@Override
	protected void running() {
		if (readRegion != null) {
			for (SamRecord samRecord : samReader.readLinesOverlap(readRegion.getRefID(), readRegion.getStartAbs(), readRegion.getEndAbs())) {
				suspendCheck();
				if (flagStop) break;
				
				waitForReading(samRecord);
			}
		} else {
			for (SamRecord samRecord : samReader.readLines()) {
				suspendCheck();
				if (flagStop) break;
				
				waitForReading(samRecord);
			}
		}
	}
	
	/** 如果读取record超过了范围，则等待处理 */
	private void waitForReading(SamRecord samRecord) {
		if (!samRecord.getRefID().equals(snpRegion.getRefID())) {
			changeChrome = true;
			chrThis = samRecord.getRefID();
			while (!samRecord.getRefID().equals(snpRegion.getRefID())) {
				try { Thread.sleep(10); } catch (InterruptedException e) { }
				if (flagStop) break;
			}
		}
		
		if (samRecord.getEndAbs() < snpRegion.getStartAbs()) return;
		
		while (samRecord.getStartAbs() > snpRegion.getEndAbs()) {
			try { Thread.sleep(10); } catch (InterruptedException e) { }
			if (flagStop) break;
		}
		queueSamRecord.add(samRecord);
	}
	
	/** 步进，向后读取一部分reads */
	public void extendRegionRead(int numExtend) {
		if (changeChrome) {
			snpRegion.setChrID(chrThis);
			snpRegion.setStart(0);
			snpRegion.setEnd(numExtend);
		} else {
			snpRegion.setStart(snpRegion.getEndAbs() - 1);
			snpRegion.setEnd(snpRegion.getEndAbs() + numExtend);
		}
	}
}