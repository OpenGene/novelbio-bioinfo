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
 * �����ļ�
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
	 * ����lambda�����Ĭ������lambda�ģ���˼�Ƕ�̬����ѡpeak�����һ������reads�����ܶ࣬��ô�������peak��Լ���
	 * �����õĻ�����ȫ�ֵ�lambda��������ôreads��ĵط��϶���peak��
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
			logger.error("�ļ���ʽ����");
			return false;
		}
		String format = enumParam.get(fileformat);
		if (format == null) {
			logger.error("�ļ���ʽ����");
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
	 * Ĭ�ϲ�����-m 5, --mfold=200
	 * -p 1e-3
	 * @param thisPath jar �����ڵ���ַ��������.
	 * @param bedCol control�ļ�·����û�п��Բ���
	 * @param species ���� BedPeakMacs.SPECIES_ ����ѡ
	 * @param outFilePath ����ļ���
	 * @param prix ����ǰ׺
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
	 * ��Macs��peak�ļ���ӵ����У�Ϊcol_start+col_summitMid
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
