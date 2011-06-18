package com.novelbio.analysis.seq.chipseq.preprocess;

import java.io.BufferedReader;

import com.novelbio.analysis.tools.formatConvert.bedFormat.Soap2Bed;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.cmd.cmdOperate2;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * mapping��peak Calling �ķ�����
 * @author zong0jie
 *
 */
public class MapPeak {
	/**
	 * 
	 * ��soap����mapping
	 * @param soapPath soap�ĳ���·��
	 * @param inputFile1 ������������������˭�Ķ����ԣ�����������ʿ�Ŀǰֻ���solexa�����˱�д
	 * @param inputFile2 ˫�˵Ļ���������һ�������ļ���û�еĻ�������null
	 * @param indexFile genome�ļ�������
	 * @param outFile3 ����ļ�
	 * @param minInsert ��С����Ƭ�Σ�20����
	 * @param maxInsert ������Ƭ�Σ�400����
	 * @throws Exception
	 * @return ����reads��������Ҳ���ǲ�������<b>˫�˵Ļ�������2</b>
	 */
	public static long mapSoap(String soapPath, String inputFile1,String inputFile2,String indexFile,String outFile3,int minInsert, int maxInsert) throws Exception {
		TxtReadandWrite txtInput = new TxtReadandWrite();
		txtInput.setParameter(inputFile1, false, true);
		BufferedReader readInput = txtInput.readfile();
		String content = "";
		long readsNum = 0;
		while ((content = readInput.readLine()) != null ){
				readsNum++;
		}
		//ÿ����һ��fastQ��Ŀ
		readsNum = readsNum/4;
		String cmd = "";
		cmd = soapPath + " -a "+inputFile1;
		cmd = cmd + " -D " +indexFile; 
		cmd = cmd + " -o " +outFile3; 
		cmd = cmd +  " -r 0 -v 2 -p 7 ";
		if (inputFile2 != null && !inputFile2.trim().equals("")) {
			cmd = cmd + " -b " + inputFile2;
			cmd = cmd+ " -2 "+ outFile3+"_SEout "+" -m "+minInsert+" -x "+maxInsert;
			
		}
		System.out.println(cmd);
		CmdOperate cmdOperate = new CmdOperate(cmd);
		cmdOperate.doInBackground();
//		//˫�˼ӱ�
//		if (inputFile2 != null) {
//			readsNum = readsNum*2;
//		}
		return readsNum;
	}
	/**
	 * ָ��bed�ļ����Լ���Ҫ���������������������
	 * @param bedFile
	 * @param arg
	 * @throws Exception
	 */
	public static void sortBedFile(String path,String bedFile,int chrID, String sortBedFile,int...arg) throws Exception {
		//sort -k1,1 -k2,2n -k3,3n FT5.bed > FT5sort.bed #��һ�����һ����ֹ���򣬵ڶ�����ڶ�����ֹ����������,���������������ֹ����������
		String cmd = "sort";
		if (chrID != 0) {
			cmd = cmd + " -k"+chrID+","+chrID+" ";
		}
		for (int i : arg) {
			cmd = cmd + " -k"+i+","+i+"n ";
		}
		cmd = cmd + bedFile + " > " + sortBedFile;
		
		TxtReadandWrite txtcmd = new TxtReadandWrite();
		txtcmd.setParameter(path+"/.sort.sh", true, false);
		txtcmd.writefile(cmd);
		String cmd2 = "sh "+path+"/.sort.sh";
		
		CmdOperate cmdOperate = new CmdOperate(cmd2);
		cmdOperate.doInBackground();
		FileOperate.delFile(path+"/.sort.sh");
	}
	/**
	 * �������ڲ�����ascII����������
	 * ��soapת��Ϊbed�ļ���ֻ�е�Ϊpear-end��ʱ�򣬲�����Ҫ����˫���ֿ���ʱ����������
	 * �����˫��45bp
	 * @param SE ���˻���˫��
	 * @param soapFile 
	 * @param outPut1 ���#/1���е����꣬һ�����45bp��һ���յ�45bp
	 * @param outCombFile1 ���#/1���еĺϲ���һ������յ㹲fragment���ȣ����ں��滭ͼ
	 * @param outPut2 ���#/2���е����꣬һ�����45bp��һ���յ�45bp�����������������������Ϊnull
	 * @param outCombFile2 ���#/2���еĺϲ���һ������յ㹲fragment���ȣ����ں��滭ͼ�����������������������Ϊnull
	 * @param outError ���������Ϣ��Ҳ�������� #/1������#/2����һ������
	 * @throws Exception
	 */
	public static void copeMapSolexa(String fastQ,boolean SE, boolean sepChain, String soapFile,String outPut1, String outCombFile1, String outPut2,String outCombFile2,String outError) throws Exception {
		if (!SE && sepChain) {
			Soap2Bed.getBed2Macs(fastQ,soapFile, outPut1, outCombFile1, outPut2, outCombFile2, outError);
		}
		else if (SE && sepChain) {
			System.out.println("MapPeak.copeMapSolexa û����Ӧ�Ĵ���");
		}
		else {
			Soap2Bed.copeSope2Bed(fastQ,SE, soapFile, outPut1, outCombFile1, outError);
		}
	}
	
	/**
	 * 
	 * ��macs�ķ�������peak calling
	 * @param bedTreat ʵ��
	 * @param bedCol ����
	 * @param species ���֣�����effective genome size����hs��mm��dm��ce��os
	 * @param outFile Ŀ���ļ��У����ü�"/"
	 * @throws Exception 
	 */
	public static void peakCalMacs(String thisPath, String bedTreat,String bedCol,String species, String outFilePath ,String prix) throws Exception 
	{
		String effge = "";
		String col = "";
		String name = "";
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
		String cmd = "macs14 -t "+bedTreat +col+name + effge + "-w";
		TxtReadandWrite txtCmd = new TxtReadandWrite();
		txtCmd.setParameter(outFilePath+"/macs.sh", true, false);
		txtCmd.writefile(cmd);
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
