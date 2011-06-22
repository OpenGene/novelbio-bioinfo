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
	 * ָ��bed�ļ����Լ���Ҫ���������������������
	 * @param chrID ChrID���ڵ��У���1��ʼ������������ĸ��������
	 * @param sortBedFile �������ļ�ȫ��
	 * @param arg ��ChrID�⣬������Ҫ������У�������������
	 * @throws Exception
	 */
	public BedPeakMacs sortBedFile(int chrID, String sortBedFile,int...arg) throws Exception {
		super.sortBedFile(chrID, sortBedFile, arg);
		return new BedPeakMacs(super.getSeqFile());
	}
	
	/**
	 * Ĭ�ϲ�����-m 5, --mfold=200
	 * -p 1e-3
	 * @param thisPath jar �����ڵ���ַ
	 * @param bedCol control�ļ�·����û�п��Բ���
	 * @param species ���� Ŀǰֻ���� os mm hs ce dm
	 * @param outFilePath ����ļ���
	 * @param prix ����ǰ׺
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
