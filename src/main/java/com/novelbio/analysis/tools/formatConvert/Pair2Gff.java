package com.novelbio.analysis.tools.formatConvert;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.dataOperate.ExcelOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * ���ϵ�NimbleScan��pair�ļ���ʽת��Ϊgff�ļ�����MEDME��ȡ�õ�
 * chromosome, probe ids, start and stop chromosomal positions, and score are expected in columns 1, 3, 4, 5 and 6 repectively.
 * @author zong0jie
 *
 */
public class Pair2Gff {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String parentFile = "/media/winE/NBC/Project/Microarray_WFL110423/������ ����оƬ/������ ����оƬ/chip result/����ͼ��ԭʼ����/";

		try {
			String pairFile = parentFile  + "C_vs_N_532.pair";
			String gffFile = parentFile + "C_vs_N_532.gff";
			pair2gff(pairFile, gffFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			String pairFile = parentFile  + "C_vs_N_635.pair";
			String gffFile = parentFile + "C_vs_N_635.gff";
			pair2gff(pairFile, gffFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		String parentFile = "/media/winE/NBC/Project/Microarray_WFL110423/������ ����оƬ/������ ����оƬ/chip result/����ͼ��ԭʼ����/MEDMEresultAbs/";
//
//		
//		try {
//			String gffPair = parentFile  + "C_vs_N_532.pair";
//			String gffRMA532 = parentFile + "C_vs_N_532RMS.gff";
//			String gffRMA635 = parentFile + "C_vs_N_635RMS.gff";
//			String excelResultFile = parentFile + "C_vs_N_Peak.xls";
//			String[] prix = new String[]{"532vs635","635vs532"};
//			
//			int contProbNum = 3;
//			double ratio = 2;
//			getMeDIP(gffPair, gffRMA532, gffRMA635, contProbNum, ratio, excelResultFile, prix);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	/**
	 * ���ϵ�NimbleScan��pair�ļ���ʽת��Ϊgff�ļ�
	 * chromosome, probe ids, start and stop chromosomal positions, and score are expected in columns 1, 3, 4, 5 and 6 repectively.
	 * @param pairFile
	 * @param gffFile
	 * @throws Exception 
	 */
	public static void pair2gff(String pairFile, String gffFile) throws Exception {
		TxtReadandWrite txtPair = new TxtReadandWrite();
		txtPair.setParameter(pairFile, false, true);
		TxtReadandWrite txtGff = new TxtReadandWrite();
		txtGff.setParameter(gffFile, true, false);
		
		BufferedReader reader = txtPair.readfile();
		String content = "";
		reader.readLine(); reader.readLine();
		String ssOld = null; String ssNew = null;
		String ssOld2 = null; String ssNew2 = null; //����chr10:97505105-97506105�ָ���Ԫ�أ���Ϊ�׻�����̽�볤����Ҫ����һ�м��㱾�еĳ��ȣ�������ҪOld��New�����洢
		//Gff�ĸ�ʽ���ڶ���MEDME����ȡ
		txtGff.writefile("chrID\tMATCH_INDEX\tprobID\tstart\tstop\tscore\n");
		while ((content = reader.readLine()) != null) {
			ssOld = ssNew;
			ssNew = content;
			if (ssOld == null) {
				continue;
			}
			String tmpEnd = "";
			String[] ssOldarray = ssOld.split("\t"); String[] ssNewarray = ssNew.split("\t");
			if (ssOldarray[1].equals("RANDOM") || ssOldarray[2].contains("random") )
				continue;
			
			ssOld2 = ssNew2;
			String[] ssOldarray2 = ssOldarray[2].split(":|-"); String[] ssNewarray2 = ssNewarray[2].split(":|-");//chr10:100017797-100018797
			if (ssOldarray2[1].equals(ssNewarray2[1])) {//�����ͬһ��̽�룬��ô��̽��Ľ�β������̽�����-1
				tmpEnd = Long.parseLong(ssNewarray[4]) - 1 + "";
			}
			else {//�������ͬһ��̽�룬��ô��̽��Ľ�β������̽���β
				tmpEnd = ssOldarray2[2];
			}
//			String tmpResult = ssOldarray2[0] + "\t" + ssOldarray[2] + "\t" + ssOldarray[3] + "\t" + ssOldarray[4] + "\t" + tmpEnd  +"\t"
//			+Math.log(Double.parseDouble(ssOldarray[9]))/Math.log(2) +"\n";
			String tmpResult = ssOldarray2[0] + "\t" + ssOldarray[2] + "\t" + ssOldarray[3] + "\t" + ssOldarray[4] + "\t" + tmpEnd  +"\t"+ssOldarray[9] +"\n";

			txtGff.writefile(tmpResult);
		}
		String tmpEnd = "";
		ssOld = ssNew;String[] ssOldarray = ssOld.split("\t"); String[] ssOldarray2 = ssOldarray[2].split(":|-"); 
		tmpEnd = ssOldarray2[2];
//		String tmpResult = ssOldarray2[0] + "\t" + ssOldarray[2] + "\t" + ssOldarray[3] + "\t" + ssOldarray[4] + "\t" + tmpEnd +"\t"
//		+ Math.log(Double.parseDouble(ssOldarray[9]))/Math.log(2) +"\n";
		String tmpResult = ssOldarray2[0] + "\t" + ssOldarray[2] + "\t" + ssOldarray[3] + "\t" + ssOldarray[4] + "\t" + tmpEnd +"\t"+ ssOldarray[9] +"\n";

		txtGff.writefile(tmpResult);
		txtGff.close();
		txtPair.close();
	}
	
	/**
	 * @throws Exception 
	 * @param gffPair ����̽���Ӧ��������Ϣ
	 * @param gffRMA532 ��ȡlog�ļ׻���оƬ��RMA��ֵ
	 * @param gffRMA635
	 * @param contProbNum ��������̽�볬����ֵ��һ��ȡ3�ȽϺ�
	 * @param ratio ��ֵratio��һ��ȡ2�ȽϺ�
	 * @param excelResultFile excel�Ľ���ļ�
	 * @param prix sting[2] 532��635��sheet����
	 * @throws Exception
	 */
	public static void getMeDIP(String gffPair,String gffRMA532,String gffRMA635,int contProbNum, double ratio,String excelResultFile,String[] prix) throws Exception {
		ArrayList<ArrayList<String[]>> lsPeak = getMeDIP(gffPair, gffRMA532, gffRMA635, contProbNum, ratio);
		ExcelOperate excelOperate = new ExcelOperate();
		excelOperate.openExcel(excelResultFile);
		excelOperate.WriteExcel(prix[0], 1, 1, lsPeak.get(0), true);
		excelOperate.WriteExcel(prix[1], 1, 1, lsPeak.get(1), true);
	}
	
	/**
	 * @throws Exception 
	 * @param gffPair ����̽���Ӧ��������Ϣ
	 * @param gffRMA532 ��ȡlog�ļ׻���оƬ��RMA��ֵ
	 * @param gffRMA635
	 * @param contProbNum ��������̽�볬����ֵ��һ��ȡ3�ȽϺ�
	 * @param ratio ��ֵratio��һ��ȡ2�ȽϺ�
	 * @throws Exception
	 * @return ����ArrayList����һ��532���ڶ���635
	 */
	private static ArrayList<ArrayList<String[]>> getMeDIP(String gffPair,String gffRMA532,String gffRMA635,int contProbNum, double ratio) throws Exception {
		TxtReadandWrite txtGffPair = new TxtReadandWrite();
		txtGffPair.setParameter(gffPair, false, true);
		
		TxtReadandWrite txtGffRMA532 = new TxtReadandWrite();
		txtGffRMA532.setParameter(gffRMA532, false, true);
		
		TxtReadandWrite txtGffRMA635 = new TxtReadandWrite();
		txtGffRMA635.setParameter(gffRMA635, false, true);
		//����̽��ID��LOC����Ϣ��Ϊ������������׻�����׼��
		//key:̽������value:0��LOC   1��̽�����      2: �ڼ���̽�룬��1��ʼ����
		HashMap<String, String[]> hashProb2Loc = new HashMap<String, String[]>();
		
		BufferedReader readerGffPair = txtGffPair.readfile();
		String content = "";
		int i = 1;//������������ڼ���̽��
		String lasLOChash = ""; //��һ��̽��������꣬�����õģ������̽�����һ��̽��������겻ͬ����������1

		while ((content = readerGffPair.readLine()) != null) {
			if (content.trim().startsWith("#") || content.trim().startsWith("IMAGE_ID")) {
				continue;
			}
			String[] ss = content.split("\t");
			
			if (ss[1].equals("RANDOM") || ss[2].contains("random") )
				continue;
			
			if(!ss[2].equals(lasLOChash))//̽�����겻ͬ
				i = 1;//��������1
			
			String[] tmpLOC = new String[3];
			tmpLOC[0] = ss[2];//:0��LOC
			tmpLOC[1] = ss[4];// 1��̽�����
			tmpLOC[2] = i +"";//2: �ڼ���̽�룬������ڼ�������������̽����Ϊ�Ǽ׻���λ��
			lasLOChash = ss[2];
			i++;
			hashProb2Loc.put(ss[3], tmpLOC);
			
		}
		BufferedReader readerGffRMA532 = txtGffRMA532.readfile();
		BufferedReader readerGffRMA635 = txtGffRMA635.readfile();
		String content532 = ""; String content635 = "";
		String content532New = ""; String content635New = "";
		String content532Old = ""; 
		int tmpContProbNum = 0; //�ϸ�̽�����ۼƵڼ���̽��
		String lastLoc = ""; //����̽���LOC
		int tmpProbNum = -10; //�ϸ�̽����LOC�ĵڼ���
		double allScore532 = 0;//�ܹ���score����������������̽���ƽ��ratio
		double allScore635 = 0;//�ܹ���score����������������̽���ƽ��ratio
		double lastRatio532 = 0;//��һ��532��ratioֵ������ϸ�ratio����2������ratioС��0.5��ͬ��Ҳ���ܼ���ͳ�Ƶ�
		double allRatio532 = 0;//���allRatio���õ���ƽ������ratio��ƽ������������score֮��������������ǵļ���ƽ��������
		double lastRatio635 = 0;//��һ��635��ratioֵ������ϸ�ratio����2������ratioС��0.5��ͬ��Ҳ���ܼ���ͳ�Ƶ�
		double allRatio635 = 0;//���allRatio���õ���ƽ������ratio��ƽ������������score֮��������������ǵļ���ƽ��������
		long peakStart = 0;//Peak����㣬Ϊ��һ��̽������
		long peakEnd = 0;//peak���յ㣬Ϊ���һ��̽����յ�+60
		boolean flag = false; //�Ƿ���Ҫ�ܽ���һ��̽��
		String[] LOCInfo = null; //��̽��ľ�����Ϣ value:0��LOC   1��̽�����      2: �ڼ���̽�룬��1��ʼ����
		ArrayList<String[]> lsTmp532 = new ArrayList<String[]>();//������
		ArrayList<String[]> lsTmp635 = new ArrayList<String[]>();
		readerGffRMA532.readLine();
		readerGffRMA635.readLine();
		while ((content532 = readerGffRMA532.readLine()) != null) {
			
			content635 = readerGffRMA635.readLine();
			content532Old = content532New; //��¼��һ�е���Ϣ
//			String content635Old = content635New; //��¼��һ�е���Ϣ
			content532New = content532;
			content635New = content635;
			
			
			String[] ss532 = content532New.split(" ");
			String[] ss635 = content635New.split(" ");
			
			LOCInfo = hashProb2Loc.get(ss532[2]);
			//��probe���ϸ�probe����ͬһ��LOC��    ���ǵ�̽���ǽ����ŵģ�   ���ǵ�532��635��ratio������ratio֮�� 
			double tmpratio532 = Double.parseDouble(ss532[5])/Double.parseDouble(ss635[5]);
			double tmpratio635 = Double.parseDouble(ss635[5])/Double.parseDouble(ss532[5]);
			
			//��������̽��û���ܽ��
			//��ʼ�ܽ�����̽������
			if (  flag  //��Ҫ���ܽ�
					&&
					(  !LOCInfo[0].equals(lastLoc) 
					|| (Integer.parseInt(LOCInfo[2]) - tmpProbNum != 1) //�����̽�������̽�벻�����ŵ�
					||(tmpratio532 < ratio && tmpratio635 < ratio)) //��̽��û�г�����ֵ
					|| ( (lastRatio532 >= ratio && tmpratio635 >= ratio) || (lastRatio635 >= ratio && tmpratio532 >= ratio) ) //��̽�볬����ֵ�ˣ����Ǻ���һ��̽�����ֵ�����Ƿ���
					)
			{
				//ֻ�е�����̽�����������趨��̽�������ܿ�ʼ���ܽ�
				if (tmpContProbNum >= contProbNum) {
					double avgScore532 = allScore532/tmpContProbNum;
					double avgScore635 = allScore635/tmpContProbNum;
					double avgRatio532 = allRatio532/tmpContProbNum;
					double avgRatio635 = allRatio635/tmpContProbNum;
					peakEnd = peakEnd + 60;
					String[] tmpResult = new String[7];
					tmpResult[0] = content532Old.split(" ")[0];
					tmpResult[1] = peakStart + "";
					tmpResult[2] = peakEnd + "";
					tmpResult[3] = avgScore532 + "";
					tmpResult[4] = avgScore635 + "";
					tmpResult[6] = tmpContProbNum + ""; //����̽����
					if (avgRatio532 >= ratio) {
						tmpResult[5] = avgRatio532 + "";
						lsTmp532.add(tmpResult);
					}
					else if (avgRatio635 >= ratio) {
						tmpResult[5] = avgRatio635 + "";
						lsTmp635.add(tmpResult);
					}
					flag = false;//�Ѿ��ܽ����
					//��������
					tmpContProbNum = 0;
					allScore532 = 0; allScore635 = 0; allRatio532 = 0; allRatio635 = 0;
				}
				tmpProbNum = -10;
				lastRatio532 = tmpratio532; lastRatio635 = tmpratio635;
			}
			
			if (  LOCInfo[0].equals(lastLoc) 
					&& (Integer.parseInt(LOCInfo[2]) - tmpProbNum == 1) //�����̽�������̽�����ŵ�
					&& ( (lastRatio532>=ratio && tmpratio532 >= ratio) || (lastRatio635>=ratio && tmpratio635 >= ratio) ) //��̽�볬����ֵ�ˣ����Һ���һ��̽�����ֵ��ͬ
		    	)
			{
				peakEnd = Long.parseLong(LOCInfo[1]);
				allScore532 = allScore532 + Double.parseDouble(ss532[5]);
				allScore635 = allScore635 + Double.parseDouble(ss635[5]);
				allRatio532 = allRatio532 + tmpratio532;
				allRatio635 = allRatio635 + tmpratio635;
				tmpContProbNum ++;
				flag = true;
				tmpProbNum = Integer.parseInt(LOCInfo[2]);
				lastRatio532 = tmpratio532; lastRatio635 = tmpratio635;
			}
			//�����һ��������ֵ��̽��
			else if (  
					(!LOCInfo[0].equals(lastLoc) 
					|| (Integer.parseInt(LOCInfo[2]) - tmpProbNum != 1) //�����̽�������̽�벻��
					||  ( (lastRatio532>=ratio && tmpratio635 >= ratio) || (lastRatio635>=ratio && tmpratio532 >= ratio))//��̽�����һ��̽���ratio�Ƿ��ŵ�
					)
					&&(tmpratio532 >= ratio || tmpratio635 >= ratio)	//��̽�볬����ֵ��
		        )
			{
				peakStart = Long.parseLong(LOCInfo[1]);
				peakEnd = Long.parseLong(LOCInfo[1]);
				allScore532 = Double.parseDouble(ss532[5]);
				allScore635 =  Double.parseDouble(ss635[5]);
				allRatio532 = allRatio532 + tmpratio532;
				allRatio635 = allRatio635 + tmpratio635;
				tmpContProbNum = 1;
				flag = true;
				tmpProbNum = Integer.parseInt(LOCInfo[2]);
				lastRatio532 = tmpratio532; lastRatio635 = tmpratio635;
				
			}
			lastLoc = LOCInfo[0];
		}
		//����ܽ�
		//ֻ�е�����̽�����������趨��̽�������ܿ�ʼ���ܽ�
		if (tmpContProbNum >= contProbNum) {
			double avgScore532 = allScore532/tmpContProbNum;
			double avgScore635 = allScore635/tmpContProbNum;
			double avgRatio532 = allRatio532/tmpContProbNum;
			double avgRatio635 = allRatio635/tmpContProbNum;
			peakEnd = peakEnd + 60;
			String[] tmpResult = new String[7];
			tmpResult[0] = content532New.split(" ")[0];
			tmpResult[1] = peakStart + "";
			tmpResult[2] = peakEnd + "";
			tmpResult[3] = avgScore532 + "";
			tmpResult[4] = avgScore635 + "";
			tmpResult[6] = tmpContProbNum + ""; //����̽����
			if (avgRatio532 >= ratio) {
				tmpResult[5] = avgRatio532 + "";
				lsTmp532.add(tmpResult);
			}
			else if (avgRatio635 >= ratio) {
				tmpResult[5] = avgRatio635 + "";
				lsTmp635.add(tmpResult);
			}
		}
		String[] title = new String[7];
		title[0] = "ChrID"; title[1] = "PeakStart"; title[2] = "PeakEnd";
		title[3] = "AvgScore532"; title[4] = "AvgScore635"; title[5] = "AvgRatio";
		title[6] = "ContinueProbNum";
		lsTmp532.add(0,title);
		lsTmp635.add(0,title);
		
		txtGffPair.close();
		txtGffRMA532.close();
		txtGffRMA635.close();
		
		ArrayList<ArrayList<String[]>> lsResult = new ArrayList<ArrayList<String[]>>();
		lsResult.add(lsTmp532);
		lsResult.add(lsTmp635);
		return lsResult;
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
