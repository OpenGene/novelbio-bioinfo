package com.novelbio.chIPSeq.preprocess;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.novelBio.base.dataOperate.ExcelTxtRead;
import com.novelBio.base.dataOperate.TxtReadandWrite;

/**
 * �������ָ�꣬�������򸲸Ƕȣ�mapping�ʣ�ÿ��chrID�ϵ�mapping�ʣ������������
 * @author zong0jie
 *
 */
public class QualityCol {
	
	/**
	 * ����һ��
	 * �����������coverage������Լ�reads��ÿ��Ⱦɫ���ϵķֲ����������������Ź����.
	 * 
	 * ����Ϊbed�ļ�����ʽ��������:<br>
	 * chrID \t loc1 \t loc2 \n<br>
	 * ����loc1 < loc2<br>
	 * @param bedFile �����PE����ôbedFile�����ǽ�����reads�ϲ�Ϊfragment���ļ��������SE����ô����reads���ļ�
	 * �������Ź����bed�ļ�����ʽ��������:<br>
	 * chrID \t loc1 \t loc2 \n<br>
	 * ����loc1 < loc2<br>
	 * @param chrLength Ⱦɫ�峤�ȵ��ļ�������õ��ģ���ʽΪ
	 * chrID \t chrLength<br>
	 * chr2 \t 243199374<br>
	 * chr1 \t 249250622<br>
	 * chr19 \t 59128984<br>
	 * <b>���ļ���û�е�chrID���������㣬Ʃ�������ļ���û��chrm����ô�Ͳ������chrm��mapping��</b>
	 * @param allReadNum ����reads�������������mapping�Ľ���ļ�������Ȼ�����÷�����<b>�����PE����ô�����allReadNum��Ҫ�ӱ��������������мӱ�����</b>
	 * @param PE �Ƿ���˫�˲�������ǵĻ������������bedFile�ǽ�����reads�ϲ����ļ�������bed�ļ���mapping��readsҪ����2
	 * @param calFragLen �Ƿ����Fragment�ĳ��ȷֲ�
	 * @param ��calFragLenΪtrueʱ�Ż����ɣ���fragment�ķֲ��ļ�����R��ȡ
	 * @return ArrayList-String <br>
	 * ��һ�� 	strMapTitle[0] = "Raw reads number";  strMapTitle[1] = "Unique mapped reads"; strMapTitle[2] = "Mapping rate (%)";
		strMapTitle[3] = "Effective reads length"; strMapTitle[4] = "Genome length"; strMapTitle[5] = "Coverage"; <br>
	 * �ڶ��У���Ӧ��ֵ<br>
	 * ������ 	title[0] = "CHRID";  title[1] = "mapping to CHR rate"; title[2] = "Back Ground"; <br>
	 * �����������Ӧ��ֵ<br>
	 */
	public static ArrayList<String[]> calCover(String bedFile,String chrLenFile,long allReadNum,boolean PE,boolean calFragLen, String FragmentFile) throws Exception
	{
		TxtReadandWrite txtbed = new TxtReadandWrite();
		txtbed.setParameter(bedFile, false, true);
		TxtReadandWrite txtFragmentFile = new TxtReadandWrite();
		if (calFragLen) {
			txtFragmentFile.setParameter(FragmentFile, false, true);
		}
		
		//chrID���chrID��mapping����reads����
		LinkedHashMap<String, int[]> hashChrReadsNum = new LinkedHashMap<String,int[]>();
		int totalMappedReads = 0;
		int[] chrMappedReads = new int[2];//����Ϊ��ֵ���ݣ����ݱ�����[0]��
		int tmpLocStart = 0; int tmpLocEnd = 0;//��������coverage
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
				if (chrID.equals("")) {//�����ʼ��chrID
					continue;
				}
				hashChrReadsNum.put(chrID.toUpperCase(), chrMappedReads);
				tmpLocStart = 0; tmpLocEnd = 0;//��������coverage
			}
			totalMappedReads ++; chrMappedReads[0]++;
			////////////////// fragment �ĳ��ȷֲ��ļ�   ////////////////////////////////////////////////////////////////
			if (calFragLen) //���Ҫ����fragment�ķֲ�����ô�ͽ�fragment�ĳ��ȼ�¼��txt�ı��У�������R�����㳤�ȷֲ�
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
		//�������
		ArrayList<String[]> lsChrLen = new ArrayList<String[]>();
		
		
		int[] colID = {1,2};
		String[][] chrIDLen = ExcelTxtRead.readtxtExcel(chrLenFile, "\t", colID, 1, -1);
		
		long chrLengthAll = 0; long readsAll = 0;
		///////////////////////// �������� ////////////////////////////////////////////////////////////////////////////
		for (String[] strings : chrIDLen) {
			String tmpChrID = strings[0].trim().toUpperCase();
			//���chrLen�ı��������ChrID�������ļ���û�У���ô������
			int[] tmpChrReads = hashChrReadsNum.get(tmpChrID);
			if (tmpChrReads == null) {
				continue;
			}
			chrLengthAll = chrLengthAll + Long.parseLong(strings[1]);
			readsAll = readsAll + tmpChrReads[0];
		}
		//////////////////////////  mapping �ʼ���  //////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
		/////////////////////// ÿ��chrID�ϵ�reads��mapping���� /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		String[] title = new String[3];
		title[0] = "CHRID";  title[1] = "mapping to CHR rate"; title[2] = "Back Ground"; 
		lsChrLen.add(title);
		//�����ֵ������arraylist
		for (String[] strings : chrIDLen) {
			String tmpChrID = strings[0].trim().toUpperCase();
			//���chrLen�ı��������ChrID�������ļ���û�У���ô������
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
	 * �ݶ���ȡ����Gradient
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
