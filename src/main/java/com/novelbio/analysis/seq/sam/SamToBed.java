package com.novelbio.analysis.seq.sam;

import java.util.ArrayList;

import com.novelbio.analysis.seq.AlignRecord;
import com.novelbio.analysis.seq.bed.BedRecord;
import com.novelbio.analysis.seq.bed.BedSeq;
import com.novelbio.base.fileOperate.FileOperate;

public class SamToBed implements AlignmentRecorder {
	BedSeq bedSeq;
	boolean isPairEndConvert = false;
	/** 是否仅挑选unique mapping的reads */
	boolean isUniqueMapping = false;
	/** 如果不是unique mapping，获取的时候是选择全部reads还是仅选择一条reads */
	boolean isUniqueRandomSelect = true;
		
	int mapQuality = 10;
	
	public SamToBed(BedSeq bedSeq) {
		this.bedSeq = bedSeq;
	}
	/**
	 * 根据输入的samFile，自动改名
	 * @param samFile
	 */
	public SamToBed(SamFile samFile) {
		this.bedSeq = new BedSeq(FileOperate.changeFileSuffix(samFile.getFileName(), "", "bed"), true);
	}
	public SamToBed(String bedFile) {
		this.bedSeq = new BedSeq(bedFile, true);
	}
	
	/** 返回bedSeq，注意没有关闭 */
	public BedSeq getBedSeq() {
		return bedSeq;
	}
	
	public void setUniqueRandomSelectOneRead(boolean selected) {
		this.isUniqueRandomSelect = selected;
	}
	
	/**是否按照双端的结果进行分析 */
	public void setIsPairEndConvert(boolean isPairEndConvert) {
		this.isPairEndConvert = isPairEndConvert;
	}
	
	/** 设定mapquality的最低阈值 */
	public void setMapQuality(int mapQuality) {
		this.mapQuality = mapQuality;
	}
	
	@Override
	public void addAlignRecord(AlignRecord alignRecord) {
		toBedSE(alignRecord);
	}
	
	/** 单端生成bed文件 */
	private void toBedSE(AlignRecord alignRecord) {
		if (alignRecord instanceof SamRecord != true) {
			return;
		}
		SamRecord samRecord = (SamRecord)alignRecord;
		if (!samRecord.isMapped() || samRecord.getMapQuality() < mapQuality
				|| (isUniqueMapping && !samRecord.isUniqueMapping()) ) {
			return;
		}
		if (isUniqueRandomSelect) {
			BedRecord bedRecord = samRecord.toBedRecordSE();
			bedSeq.writeBedRecord(bedRecord);
			bedRecord = null;
		} else {
			ArrayList<BedRecord> lsBedRecord = samRecord.toBedRecordSELs();
			for (BedRecord bedRecord : lsBedRecord) {
				bedSeq.writeBedRecord(bedRecord);
			}
			lsBedRecord = null;
		}
		samRecord = null;
	}
	
	@Override
	public void summary() {
		
	}

}

