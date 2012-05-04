package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class BedPeakMacs extends BedPeak {

	
	public BedPeakMacs(String bedFile) {
		super(bedFile);
		// TODO Auto-generated constructor stub
	}
	

	
	public BedPeakMacs filterWYR(String filterOut) throws Exception {
		BedSeq bedSeq = super.filterWYR(filterOut);
		return new BedPeakMacs(bedSeq.getFileName());
	}
	/**
	 * ����lambda�����Ĭ������lambda�ģ���˼�Ƕ�̬����ѡpeak�����һ������reads�����ܶ࣬��ô�������peak��Լ���
	 * �����õĻ�����ȫ�ֵ�lambda��������ôreads��ĵط��϶���peak��
	 * @return
	 */
	String nolambda = "";
	public void setNoLambda()
	{
		nolambda = " --nolambda "; 
	}
	
	/**
	 * ָ��bed�ļ����Լ���Ҫ���������������������
	 * @param chrID ChrID���ڵ��У���1��ʼ������������ĸ��������
	 * @param sortBedFile �������ļ�ȫ��
	 * @param arg ��ChrID�⣬������Ҫ������У�������������
	 */
	public BedPeakMacs sortBedFile(int chrID, String sortBedFile,int...arg) {
		super.sortBedFile(chrID, sortBedFile, arg);
		return new BedPeakMacs(super.getFileName());
	}
	/**
	 * ָ��bed�ļ����Լ���Ҫ���������������������
	 * @param refID ChrID���ڵ��У���1��ʼ������������ĸ��������
	 * @param sortBedFile �������ļ�ȫ��
	 * @param arg ��ChrID�⣬������Ҫ������У�������������
	 */
	public BedPeakMacs sortBedFile(String sortBedFile) {
		super.sortBedFile(sortBedFile);
		return new BedPeakMacs(super.getFileName());
	}
	/**
	 * Ĭ�ϲ�����-m 5, --mfold=200
	 * -p 1e-3
	 * @param thisPath jar �����ڵ���ַ��������.
	 * @param bedCol control�ļ�·����û�п��Բ���
	 * @param species ���� BedPeakMacs.SPECIES_ ����ѡ
	 * @param outFilePath ����ļ���
	 * @param prix ����ǰ׺
	 * @throws Exception
	 */
	public void peakCallling(String bedCol, String species, String outFilePath, String prix) {
		String effge = "";
		String col = "";
		String name = "";
		String mfole = " -m 3,500 ";
		String pvalue = " -p 1e-2 ";
		
		double genomeSize = hashSpecies2GenomeSize.get(species)*effectiveGenomeSize;
		effge = " -g "+genomeSize + " ";
		
//		else {
//			//���� Ŀǰֻ���� os mm hs ce dm
//			effge = " -g "+ species + " ";
//		}
		if (bedCol != null && !bedCol.trim().equals("")) {
			col = " -c " + bedCol + " ";
		}
		if (prix !=null && !prix.trim().equals("")) {
			name = " -n "+prix;
		}
		String cmd = "macs14 -t "+getFileName() +col+name + effge + mfole + pvalue + nolambda;//+ "-w";
//		TxtReadandWrite txtCmd = new TxtReadandWrite( outFilePath+"/macs.sh", true);
//		txtCmd.writefile(cmd);
//		txtCmd.close();
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.doInBackground("macs");
		String peakFile = FileOperate.moveFile(PathDetail.getProjectPath()+"/"+prix+"_peaks.xls", outFilePath,true);
		FileOperate.moveFile(PathDetail.getProjectPath() + "/" + prix+"_peaks.bed", outFilePath+"/TmpPeakInfo",true);
		FileOperate.moveFile(PathDetail.getProjectPath() + "/" + prix+"_negative_peaks.xls", outFilePath+"/TmpPeakInfo"+prix+"/",true);
		FileOperate.moveFile(PathDetail.getProjectPath() + "/" + prix+"_model.r", outFilePath+"/TmpPeakInfo"+prix+"/",true);
		FileOperate.moveFile(PathDetail.getProjectPath() + "/" + prix+"_diag.xls", outFilePath+"/TmpPeakInfo"+prix+"/",true);
		FileOperate.moveFile(PathDetail.getProjectPath() + "/" + prix+"_summits.bed", outFilePath+"/TmpPeakInfo"+prix+"/",true);
		FileOperate.moveFolder(PathDetail.getProjectPath() + "/" + prix+"_MACS_wiggle", outFilePath+"/TmpPeakInfo"+prix+"/",true);
		FileOperate.delFile(outFilePath+"/macs.sh");
		copeMACSPeakFile(peakFile, FileOperate.changeFileSuffix(peakFile, "_summit", null));
	}
	
	/**
	 * ��Macs��peak�ļ���ӵ����У�Ϊcol_start+col_summitMid
	 */
	public static void	copeMACSPeakFile(String peakFile, String outPut)
	{
		TxtReadandWrite txtPeak = new TxtReadandWrite(peakFile, false);
		ArrayList<String> lsTmp =  txtPeak.readfileLs();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String string : lsTmp) {
			if (string == null || string.trim().startsWith("#") || string.trim().equals("")) {
				continue;
			}
			String[] ss = string.split("\t");

			String[] ss2 = new String[ss.length + 1];
			for (int i = 0; i < ss.length; i++) {
				if (i < 5) {
					ss2[i] = ss[i];
				}
				else {
					ss2[i+1] = ss[i];
				}
			}
			
			if (ss[1].equals("start")) {
				ss2[5] = "summit_mid";
				ss2[7] = "(-10*log10(pvalue))";
			}
			else {
				ss2[5] = Integer.parseInt(ss[1]) + Integer.parseInt(ss[4]) + "";
			}
			lsResult.add(ss2);
		}
		TxtReadandWrite txtOut = new TxtReadandWrite(outPut, true);
		txtOut.ExcelWrite(lsResult, "\t", 1, 1);
	}

 
	
	
	
}
