package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

public class BedPeakMacs extends BedPeak implements PeakCalling{

	public static final String SPECIES_RICE = "os";
	public static final String SPECIES_HUMAN = "hs";
	public static final String SPECIES_C_ELEGAN = "ce";
	public static final String SPECIES_DROSOPHYLIA = "dm";
	public static final String SPECIES_MOUSE = "mm";
	
	public BedPeakMacs(String bedFile) {
		super(bedFile);
		// TODO Auto-generated constructor stub
	}
	
	public BedPeakMacs filterWYR(String filterOut) throws Exception {
		BedSeq bedSeq = super.filterWYR(filterOut);
		return new BedPeakMacs(bedSeq.getSeqFile());
	}
	/**
	 * 设置lambda，如果默认是有lambda的，意思是动态的挑选peak，如果一个区域reads数量很多，那么该区域的peak相对减分
	 * 不设置的话就用全局的lambda来处理，那么reads多的地方肯定就peak大
	 * @return
	 */
	String nolambda = "";
	public void setNoLambda()
	{
		nolambda = " --nolambda "; 
	}
	
	/**
	 * 指定bed文件，以及需要排序的列数，产生排序结果
	 * @param chrID ChrID所在的列，从1开始记数，按照字母数字排序
	 * @param sortBedFile 排序后的文件全名
	 * @param arg 除ChrID外，其他需要排序的列，按照数字排序
	 */
	public BedPeakMacs sortBedFile(int chrID, String sortBedFile,int...arg) {
		super.sortBedFile(chrID, sortBedFile, arg);
		return new BedPeakMacs(super.getSeqFile());
	}
	/**
	 * 指定bed文件，以及需要排序的列数，产生排序结果
	 * @param chrID ChrID所在的列，从1开始记数，按照字母数字排序
	 * @param sortBedFile 排序后的文件全名
	 * @param arg 除ChrID外，其他需要排序的列，按照数字排序
	 */
	public BedPeakMacs sortBedFile(String sortBedFile) {
		super.sortBedFile(sortBedFile);
		return new BedPeakMacs(super.getSeqFile());
	}
	/**
	 * 默认参数：-m 5, --mfold=200
	 * -p 1e-3
	 * @param thisPath jar 包所在的网址，可以用.
	 * @param bedCol control文件路径，没有可以不填
	 * @param species 物种 BedPeakMacs.SPECIES_ 里面选
	 * @param outFilePath 输出文件夹
	 * @param prix 样本前缀
	 * @throws Exception
	 */
	public void peakCallling(String bedCol, String species, String outFilePath, String prix) {
		String effge = "";
		String col = "";
		String name = "";
		String mfole = " -m 3,500 ";
		String pvalue = " -p 1e-2 ";
		if (species.equals("os")) {
			effge = " -g 2.6e8 ";
		}
		else {
			//物种 目前只能是 os mm hs ce dm
			effge = " -g "+ species + " ";
		}
		if (bedCol != null && !bedCol.trim().equals("")) {
			col = " -c " + bedCol + " ";
		}
		if (prix !=null && !prix.trim().equals("")) {
			name = " -n "+prix;
		}
		String cmd = "macs14 -t "+getSeqFile() +col+name + effge + mfole + pvalue + nolambda;//+ "-w";
		TxtReadandWrite txtCmd = new TxtReadandWrite( outFilePath+"/macs.sh", true);
		txtCmd.writefile(cmd);
		txtCmd.close();
		CmdOperate cmdOperate = new CmdOperate("sh "+outFilePath+"/macs.sh");
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
	 * 将Macs的peak文件添加第六列，为col_start+col_summitMid
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
