package com.novelbio.analysis.seq.mapping;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * bed��ʽ���ļ���ͳͳ��������bed�ļ�
 * @author zong0jie
 *
 */
public class BedSeq extends Seq{	
	private static Logger logger = Logger.getLogger(BedSeq.class);  
	
	public BedSeq(String bedFile) {
		super(bedFile, 1);
	}

	/**
	 * ָ��bed�ļ����Լ���Ҫ���������������������
	 * @param chrID ChrID���ڵ��У���1��ʼ������������ĸ��������
	 * @param sortBedFile �������ļ�ȫ��
	 * @param arg ��ChrID�⣬������Ҫ������У�������������
	 * @throws Exception
	 */
	public BedSeq sortBedFile(int chrID, String sortBedFile,int...arg) throws Exception {
		String path = FileOperate.getParentName(seqFile);
		//sort -k1,1 -k2,2n -k3,3n FT5.bed > FT5sort.bed #��һ�����һ����ֹ���򣬵ڶ�����ڶ�����ֹ����������,���������������ֹ����������
		String cmd = "sort";
		if (chrID != 0) {
			cmd = cmd + " -k"+chrID+","+chrID+" ";
		}
		for (int i : arg) {
			cmd = cmd + " -k"+i+","+i+"n ";
		}
		cmd = cmd + seqFile + " > " + sortBedFile;
		TxtReadandWrite txtcmd = new TxtReadandWrite();
		txtcmd.setParameter(path+"/.sort.sh", true, false);
		txtcmd.writefile(cmd);
		String cmd2 = "sh "+path+"/.sort.sh";
		CmdOperate cmdOperate = new CmdOperate(cmd2);
		cmdOperate.doInBackground();
		FileOperate.delFile(path+"/.sort.sh");
		BedSeq bedSeq = new BedSeq(sortBedFile);
		return bedSeq;
	}
	
	
	/**
	 * ר�Ÿ��������GSM531964_PHF8.bed���˵��ļ���
	 * @throws Exception 
	 */
	public BedSeq filter(String filterOut) throws Exception {
		txtSeqFile.setParameter(seqFile, false, true);
		BufferedReader reader   = txtSeqFile.readfile();
		
		TxtReadandWrite txtOut = new TxtReadandWrite();
		txtOut.setParameter(filterOut, true, false);
		
		String content = "";
		while ((content = reader.readLine())!=null) {
			String[] ss = content.split("\t");
			int a = 0;
			try {
				a = Integer.parseInt(ss[3]);
				if (a >= 23) {
					txtOut.writefile(content+"\n");
				}
			} catch (Exception e) {
				if (filterBigNum(ss[3], 23)) {
					txtOut.writefile(content+"\n");
				}
			}
		}
		BedSeq bedSeq = new BedSeq(filterOut);
		return bedSeq;
	}
	/**
	 * ר�Ÿ��������GSM531964_PHF8.bed���˵��ļ���
	 * @throws Exception 
	 */
	private boolean filterBigNum(String str,int filter)
	{
		String[] ss = str.split("\\D");
		for (String string : ss) {
			int a =0;
			try {
				a = Integer.parseInt(string);
			} catch (Exception e) {
				continue;
			}
			if (a>=filter) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * û��ʵ�֣���Ҫ���า��
	 * @param bedTreat ʵ��
	 * @param bedCol ����
	 * @param species ���֣�����effective genome size����hs��mm��dm��ce��os
	 * @param outFile Ŀ���ļ��У����ü�"/"
	 * @throws Exception 
	 */
	public void peakCallling( String bedTreat,String bedCol,String species, String outFilePath ,String prix) throws Exception
	{
	}
	
}
