package com.novelbio.analysis.seq.chipseq;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.BedSeq;
import com.novelbio.analysis.seq.FormatSeq;
import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
/**
 * https://github.com/taoliu/MACS/blob/macs_v1/README.rst
 * 下载文件
 * @author zong0jie
 *
 */
public class PeakMacs extends PeakCalling {
	Logger logger = Logger.getLogger(PeakMacs.class);
	static EnumMap<FormatSeq, String> enumParam = new EnumMap<FormatSeq, String>(FormatSeq.class);
	static {
		enumParam.put(FormatSeq.BAM, "\"BAM\"");
		enumParam.put(FormatSeq.SAM, "\"SAM\"");
		enumParam.put(FormatSeq.BED, "\"BED\"");
		enumParam.put(FormatSeq.FASTQ, "\"FASTQ\"");
	}
	public PeakMacs(String File) {
		super(File);
	}
	/**
	 * 设置lambda，如果默认是有lambda的，意思是动态的挑选peak，如果一个区域reads数量很多，那么该区域的peak相对减分
	 * 不设置的话就用全局的lambda来处理，那么reads多的地方肯定就peak大
	 * @return
	 */
	String nolambda = "";
	String format = "";
	String species = "";
	public void setNoLambda() {
		nolambda = " --nolambda "; 
	}
	@Override
	public boolean setFileFormat(FormatSeq fileformat) {
		if (fileformat == FormatSeq.FASTQ) {
			logger.error("文件格式出错");
			return false;
		}
		String format = enumParam.get(fileformat);
		if (format == null) {
			logger.error("文件格式出错");
			return false;
		}
		this.format = " --format " + format;
		return true;
	}

	@Override
	public void setSpecies(String species) {
		this.species = species;
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
	public void peakCallling() {
		String effge = "";
		String col = "";
		String name = "";
		String mfole = " -m 3,500 ";
		String pvalue = " -p 1e-2 ";
		
		double genomeSize = hashSpecies2GenomeSize.get(species)*effectiveGenomeSize;
		effge = " -g "+genomeSize + " ";
		if (FileOperate.isFileExist(controlFile)) {
			col = " -c " + controlFile + " ";
		}
		if (outPrefix!= null && !outPrefix.equals("")) {
			name = " -n " + outPrefix;
		}
		String cmd = "macs14 -t "+file +col+name + effge + mfole + pvalue + nolambda + format;//+ "-w";
		CmdOperate cmdOperate = new CmdOperate(cmd, "macs");
		cmdOperate.run();
	}
	
	/**
	 * 将Macs的peak文件添加第六列，为col_start+col_summitMid
	 */
	private void copeMACSPeakFile(String peakFile, String outPut) {
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
