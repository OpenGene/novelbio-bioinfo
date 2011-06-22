package com.novelbio.analysis.seq;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.web.ContactController;

/**
 * bed格式的文件，统统用来处理bed文件
 * @author zong0jie
 *
 */
public class BedSeq extends Seq{	
	private static Logger logger = Logger.getLogger(BedSeq.class);  
	
	public BedSeq(String bedFile) {
		super(bedFile, 1);
	}

	/**
	 * 指定bed文件，以及需要排序的列数，产生排序结果
	 * @param chrID ChrID所在的列，从1开始记数，按照字母数字排序
	 * @param sortBedFile 排序后的文件全名
	 * @param arg 除ChrID外，其他需要排序的列，按照数字排序
	 * @throws Exception
	 */
	public BedSeq sortBedFile(int chrID, String sortBedFile,int...arg) throws Exception {
		String path = FileOperate.getParentName(seqFile);
		//sort -k1,1 -k2,2n -k3,3n FT5.bed > FT5sort.bed #第一列起第一列终止排序，第二列起第二列终止按数字排序,第三列起第三列终止按数字排序
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
	 * 专门给王彦儒的GSM531964_PHF8.bed过滤的文件，
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
	 * 专门给王彦儒的GSM531964_PHF8.bed过滤的文件，
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
	 * 如果bed文件的坐标太短，根据正负链延长坐标至指定位置<br>
	 * 标准bed文件格式为：chr1  \t  7345  \t  7370  \t  25  \t  52  \t  - <br>
	 * 必须有第六列
	 * @throws Exception 
	 * @throws Exception 
	 */
	public void extend(int extendTo, String outFileName) throws Exception
	{
		txtSeqFile.setParameter(seqFile, false, true);
		BufferedReader reader = txtSeqFile.readfile();
		
		TxtReadandWrite txtOut = new TxtReadandWrite();
		txtOut.setParameter(outFileName, true, false);
		
		String content = "";
		while ((content = reader.readLine()) != null) {
			String[] ss = content.split("\t");
			int end = Integer.parseInt(ss[2]); int start = Integer.parseInt(ss[1]);
			if ((end - start )< 0) {logger.error("Bed 文件出错，有一列的终点小于起点"+content); }
			
			if ((end - start) < extendTo ) {
				if (ss[5].equals("+")) {
					ss[2] = start + extendTo + "";
				}
				else {
					ss[1] = end - extendTo + ""; 
				}
			}
			String contString = "";
			for (int i = 0; i < ss.length-1; i++) {
				contString = contString+ss[i] + "\t";
			}
			contString = contString + ss[ss.length -1];
			txtOut.writefile(contString+"\n");
		}
		
	}
	/**
	 * 没有实现，需要子类覆盖
	 * @param bedTreat 实验
	 * @param bedCol 对照
	 * @param species 物种，用于effective genome size，有hs，mm，dm，ce，os
	 * @param outFile 目标文件夹，不用加"/"
	 * @throws Exception 
	 */
	public void peakCallling( String bedTreat,String bedCol,String species, String outFilePath ,String prix) throws Exception
	{
	}
	
}
