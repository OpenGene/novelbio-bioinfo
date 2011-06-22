package com.novelbio.analysis.seq.chipseq;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class BedPeakMacs extends BedSeq{

	public BedPeakMacs(String bedFile) {
		super(bedFile);
		// TODO Auto-generated constructor stub
	}
	
	public BedPeakMacs filter(String filterOut) throws Exception {
		BedSeq bedSeq = super.filter(filterOut);
		return new BedPeakMacs(bedSeq.getSeqFile());
	}
	
	
	
	/**
	 * 指定bed文件，以及需要排序的列数，产生排序结果
	 * @param chrID ChrID所在的列，从1开始记数，按照字母数字排序
	 * @param sortBedFile 排序后的文件全名
	 * @param arg 除ChrID外，其他需要排序的列，按照数字排序
	 * @throws Exception
	 */
	public BedPeakMacs sortBedFile(int chrID, String sortBedFile,int...arg) throws Exception {
		super.sortBedFile(chrID, sortBedFile, arg);
		return new BedPeakMacs(super.getSeqFile());
	}
	
	/**
	 * 默认参数：-m 5, --mfold=200
	 * -p 1e-3
	 * @param thisPath jar 包所在的网址
	 * @param bedCol control文件路径，没有可以不填
	 * @param species 物种 目前只能是 os mm hs ce dm
	 * @param outFilePath 输出文件夹
	 * @param prix 样本前缀
	 * @throws Exception
	 */
	public void peakCallling(String thisPath, String bedCol,
			String species, String outFilePath, String prix) throws Exception {
		String effge = "";
		String col = "";
		String name = "";
		String mfole = " -m 5,200 ";
		String pvalue = " -p 1e-3 ";
		if (species.equals("os")) {
			effge = " -g 2.6e8 ";
		}
		else {
			effge = " -g "+ species + " ";
		}
		if (bedCol != null && !bedCol.trim().equals("")) {
			col = " -c " + bedCol + " ";
		}
		if (prix !=null && !prix.trim().equals("")) {
			name = " -n "+prix;
		}
		String cmd = "macs14 -t "+getSeqFile() +col+name + effge + mfole + pvalue + "-w";
		TxtReadandWrite txtCmd = new TxtReadandWrite();
		txtCmd.setParameter(outFilePath+"/macs.sh", true, false);
		txtCmd.writefile(cmd);
		txtCmd.close();
		CmdOperate cmdOperate = new CmdOperate("sh "+outFilePath+"/macs.sh");
		cmdOperate.doInBackground();
		FileOperate.moveFile(thisPath+"/"+prix+"_peaks.xls", outFilePath,true);
		FileOperate.moveFile(thisPath+"/"+prix+"_peaks.bed", outFilePath+"/TmpPeakInfo/",true);
		FileOperate.moveFile(thisPath+"/"+prix+"_negative_peaks.xls", outFilePath+"/TmpPeakInfo/",true);
		FileOperate.moveFile(thisPath+"/"+prix+"_model.r", outFilePath+"/TmpPeakInfo/",true);
		FileOperate.moveFile(thisPath+"/"+prix+"_diag.xls", outFilePath+"/TmpPeakInfo/",true);
		FileOperate.moveFile(thisPath+"/"+prix+"_summits.bed", outFilePath+"/TmpPeakInfo/",true);
		FileOperate.moveFolder(thisPath+"/"+prix+"_MACS_wiggle", outFilePath+"/TmpPeakInfo/",true);
		FileOperate.delFile(outFilePath+"/macs.sh");
	}

}
