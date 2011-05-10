package com.novelbio.chIPSeq.preprocess;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.novelBio.base.dataOperate.ExcelTxtRead;
import com.novelBio.base.dataOperate.TxtReadandWrite;

/**
 * 计算各种指标，包括测序覆盖度，mapping率，每条chrID上的mapping率，评估测序深度
 * @author zong0jie
 *
 */
public class QualityCol {
	
	/**
	 * 测试一下
	 * 计算测序结果的coverage情况，以及reads在每条染色体上的分布情况，结果必须是排过序的.
	 * 
	 * 并且为bed文件，格式必须如下:<br>
	 * chrID \t loc1 \t loc2 \n<br>
	 * 并且loc1 < loc2<br>
	 * @param bedFile 如果是PE，那么bedFile必须是将两个reads合并为fragment的文件，如果是SE，那么就是reads的文件
	 * 必须是排过序的bed文件，格式必须如下:<br>
	 * chrID \t loc1 \t loc2 \n<br>
	 * 并且loc1 < loc2<br>
	 * @param chrLength 染色体长度的文件，计算得到的，格式为
	 * chrID \t chrLength<br>
	 * chr2 \t 243199374<br>
	 * chr1 \t 249250622<br>
	 * chr19 \t 59128984<br>
	 * <b>本文件中没有的chrID不会进入计算，譬如若本文件中没有chrm，那么就不会计算chrm的mapping率</b>
	 * @param allReadNum 所有reads的数量，这个由mapping的结果文件读出，然后进入该方法。<b>如果是PE，那么输入的allReadNum不要加倍，本方法不进行加倍操作</b>
	 * @param PE 是否是双端测序，如果是的话，由于输入的bedFile是将两个reads合并的文件，所以bed文件中mapping的reads要乘以2
	 * @param calFragLen 是否计算Fragment的长度分布
	 * @param 当calFragLen为true时才会生成，是fragment的分布文件，用R读取
	 * @return ArrayList-String <br>
	 * 第一行 	strMapTitle[0] = "Raw reads number";  strMapTitle[1] = "Unique mapped reads"; strMapTitle[2] = "Mapping rate (%)";
		strMapTitle[3] = "Effective reads length"; strMapTitle[4] = "Genome length"; strMapTitle[5] = "Coverage"; <br>
	 * 第二行，相应的值<br>
	 * 第三行 	title[0] = "CHRID";  title[1] = "mapping to CHR rate"; title[2] = "Back Ground"; <br>
	 * 第四行向后，相应的值<br>
	 */
	public static ArrayList<String[]> calCover(String bedFile,String chrLenFile,long allReadNum,boolean PE,boolean calFragLen, String FragmentFile) throws Exception
	{
		TxtReadandWrite txtbed = new TxtReadandWrite();
		txtbed.setParameter(bedFile, false, true);
		TxtReadandWrite txtFragmentFile = new TxtReadandWrite();
		if (calFragLen) {
			txtFragmentFile.setParameter(FragmentFile, false, true);
		}
		
		//chrID与该chrID所mapping到的reads数量
		LinkedHashMap<String, int[]> hashChrReadsNum = new LinkedHashMap<String,int[]>();
		int totalMappedReads = 0;
		int[] chrMappedReads = new int[2];//仅仅为了值传递，数据保存在[0]中
		int tmpLocStart = 0; int tmpLocEnd = 0;//用来计算coverage
		String content = ""; String chrID = "";
		BufferedReader readBed = txtbed.readfile();
		long coverage = 0;
		long depth = 0 ;
		while ((content = readBed.readLine()) != null) {
			String[] ss = content.split("\t");
			int Locstart = Integer.parseInt(ss[1]); int Locend = Integer.parseInt(ss[2]); 
			depth = depth + Locend - Locstart + 1;
			
			if (!ss[0].trim().equals(chrID)) {
				chrID = ss[0].trim();
				chrMappedReads = new int[2];
				if (chrID.equals("")) {//跳过最开始的chrID
					continue;
				}
				hashChrReadsNum.put(chrID.toUpperCase(), chrMappedReads);
				tmpLocStart = 0; tmpLocEnd = 0;//用来计算coverage
			}
			totalMappedReads ++; chrMappedReads[0]++;
			////////////////// fragment 的长度分布文件   ////////////////////////////////////////////////////////////////
			if (calFragLen) //如果要计算fragment的分布，那么就将fragment的长度记录在txt文本中，最后调用R来计算长度分布
			{
				txtFragmentFile.writefile(Locend - Locstart+""+"\n");
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			if (Locend <= tmpLocEnd) {
				continue;
			}
			else if (Locstart <= tmpLocEnd && Locend> tmpLocEnd ) {
				coverage = coverage + Locend - tmpLocEnd;
				tmpLocEnd = Locend;
			}
			else if (Locstart > tmpLocEnd ) {
				coverage = coverage + Locend - Locstart + 1;
				tmpLocEnd = Locend;
			}
		}
		//结果保存
		ArrayList<String[]> lsChrLen = new ArrayList<String[]>();
		
		
		int[] colID = {1,2};
		String[][] chrIDLen = ExcelTxtRead.readtxtExcel(chrLenFile, "\t", colID, 1, -1);
		
		long chrLengthAll = 0; long readsAll = 0;
		///////////////////////// 计算总数 ////////////////////////////////////////////////////////////////////////////
		for (String[] strings : chrIDLen) {
			String tmpChrID = strings[0].trim().toUpperCase();
			//如果chrLen文本中有这个ChrID而测序文件中没有，那么就跳过
			int[] tmpChrReads = hashChrReadsNum.get(tmpChrID);
			if (tmpChrReads == null) {
				continue;
			}
			chrLengthAll = chrLengthAll + Long.parseLong(strings[1]);
			readsAll = readsAll + tmpChrReads[0];
		}
		//////////////////////////  mapping 率计算  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
		String[] strMapTitle = new String[8];
		strMapTitle[0] = "Raw reads number";  strMapTitle[1] = "Unique mapped reads"; 
		strMapTitle[2] = "Mapping rate (%)";strMapTitle[3] = "Genome length";
		strMapTitle[4] = "Mapped reads length"; strMapTitle[5] = "Depth"; 
		strMapTitle[6] = "Effective reads length";  strMapTitle[7] = "Coverage";
		String[] strMap = new String[6];
		strMap[0] = allReadNum+ ""; 
//		if (PE) 
//			strMap[1] = totalMappedReads*2 + "";
//		else 
			strMap[1] = totalMappedReads + "";
		strMap[2] = Double.parseDouble(strMap[1])/allReadNum + ""; strMap[3] = chrLengthAll + "";  
		strMap[4] = depth + ""; strMap[5] = (double)depth/(double)chrLengthAll + ""; 
		strMap[6] = coverage + ""; strMap[7] = (double)coverage/(double)chrLengthAll + ""; 
		lsChrLen.add(strMapTitle);
		lsChrLen.add(strMap);
		/////////////////////// 每个chrID上的reads的mapping比率 /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		String[] title = new String[3];
		title[0] = "CHRID";  title[1] = "mapping to CHR rate"; title[2] = "Back Ground"; 
		lsChrLen.add(title);
		//计算比值并加入arraylist
		for (String[] strings : chrIDLen) {
			String tmpChrID = strings[0].trim().toUpperCase();
			//如果chrLen文本中有这个ChrID而测序文件中没有，那么就跳过
			int[] tmpChrReads = hashChrReadsNum.get(tmpChrID);
			if (tmpChrReads == null) {
				continue;
			}
			long chrLen = Long.parseLong(strings[1]);
			String[] tmpMapRate = new String[3];
			tmpMapRate[0] = tmpChrID; tmpMapRate[1] = (double)tmpChrReads[0]/(double)readsAll + ""; 
			tmpMapRate[2] = (double)chrLen/(double)chrLengthAll + "";
			lsChrLen.add(tmpMapRate);
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		txtbed.close();
		txtFragmentFile.close();
		return lsChrLen;
	}
	
	
	
	/**
	 * 梯度提取序列Gradient
	 * @param inFile
	 * @param percent
	 * @param outFile
	 * @throws Exception
	 */
	public static void getGradTxt(String inFile,int[] percent,String outFile) throws Exception {
		TxtReadandWrite txtIn = new TxtReadandWrite();
		txtIn.setParameter(inFile, false, true);
		for (int i = 0; i < percent.length; i++) {
			if (percent[i]>100) {
				percent[i] = 100;
			}
		}
		ArrayList<TxtReadandWrite> lstxtWrite = new ArrayList<TxtReadandWrite>();
		for (int i = 0; i < percent.length; i++) {
			TxtReadandWrite txtWrite = new TxtReadandWrite();
			txtWrite.setParameter(outFile+percent[i], true, false);
			lstxtWrite.add(txtWrite);
		}
		int rowAllNum = txtIn.ExcelRows();
		BufferedReader reader = txtIn.readfile();
		String content = "";
		int rowNum = 0;
		while ((content = reader.readLine()) != null) {
			for (int i = 0; i < percent.length; i++) {
				 int tmpNum =percent[i]*rowAllNum;
				if (rowNum<tmpNum/100) {
					lstxtWrite.get(i).writefile(content+"\n");
				}
			}
			rowNum++;
		}
		for (TxtReadandWrite txtReadandWrite : lstxtWrite) {
			txtReadandWrite.close();
		}
		txtIn.close();
	}
}
