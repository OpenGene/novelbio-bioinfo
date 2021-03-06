package com.novelbio.bioinfo.sam;

import java.util.ArrayList;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.base.AlignRecord;
import com.novelbio.bioinfo.bed.BedFile;
import com.novelbio.bioinfo.bed.BedRecord;

public class SamToBed implements AlignmentRecorder {
	BedFile bedSeq;
	boolean isPairEndConvert = false;
	/** 是否仅挑选unique mapping的reads */
	boolean isUniqueMapping = false;
	/** 如果不是unique mapping，获取的时候是选择全部reads还是仅选择一条reads */
	boolean isUniqueRandomSelect = true;
		
	int mapQuality = 10;
	
	public SamToBed(BedFile bedSeq) {
		this.bedSeq = bedSeq;
	}
	/**
	 * 根据输入的samFile，自动改名
	 * @param samFile
	 */
	public SamToBed(SamFile samFile) {
		this.bedSeq = new BedFile(FileOperate.changeFileSuffix(samFile.getFileName(), "", "bed"), true);
	}
	public SamToBed(String bedFile) {
		this.bedSeq = new BedFile(bedFile, true);
	}
	
	/** 返回bedSeq，注意没有关闭 */
	public BedFile getBedFile() {
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
			bedSeq.writeBedRecordSimple(bedRecord);
			bedRecord = null;
		} else {
			ArrayList<BedRecord> lsBedRecord = samRecord.toBedRecordSELs();
			for (BedRecord bedRecord : lsBedRecord) {
				bedSeq.writeBedRecordSimple(bedRecord);
			}
			lsBedRecord = null;
		}
		samRecord = null;
	}
	
	@Override
	public void summary() {
		
	}
	@Override
	public Align getReadingRegion() {
		return null;
	}

}

