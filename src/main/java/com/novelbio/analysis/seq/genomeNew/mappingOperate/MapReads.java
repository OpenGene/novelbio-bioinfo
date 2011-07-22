package com.novelbio.analysis.seq.genomeNew.mappingOperate;


import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.attribute.standard.Sides;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;

import com.novelbio.analysis.seq.genome.getChrSequence.ChrSearch;
import com.novelbio.analysis.seq.genome.getChrSequence.ChrStringHash;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.MathComput;


/**
 * �������ڴ����Ƶı�
 * @author zong0jie
 *
 */
public class MapReads {
	private static Logger logger = Logger.getLogger(MapReads.class);
	/**
	 * ��������ÿ��Ⱦɫ���еĻ�������-invNum���������reads��Ŀ
	 * chrID(Сд)--short[]
	 * ����short[]��1��ʼ��0��¼��short�ĳ��ȣ����ǻ�������Բ�׼��
	 */
	 Hashtable<String, int[]> hashChrBpReads=new Hashtable<String, int[]>();
	
	/**
	 * ��������mapping�ļ��г��ֹ���ÿ��chr �ĳ���
	 */
	 ArrayList<String[]> lsChrLength=new ArrayList<String[]>();
	 int invNum=10;
	 int tagLength=300;//��ReadMapFile������ֵ
	 String sep = "\t";
	 /**
	  * ��mapping�ļ�mapping�ľ��������ļ���
	  */
	 String chrFilePath = "";
	 /**
	  * �����mapping�ļ�
	  */
	 String mapFile = "";
	 /**
	  * ChrID���ڵ���
	  */
	 int colChrID = 1;
	 /**
	  * ������ڵ���
	  */
	 int colStartNum = 2;
	 /**
	  * �յ����ڵ���
	  */
	 int colEndNum = 3;
	/**
	 * �趨˫��readsTagƴ�����󳤶ȵĹ���ֵ��Ŀǰsolexa˫���������ȴ����300bp������̫��ȷ
	 * Ĭ��300
	 * ����Ƿ�����getReadsDensity����reads�ܶȵĶ���
	 * @param readsTagLength
	 */
	public  void setTagLength(int thisTagLength) {
		tagLength=thisTagLength;
	}
	/**
	 * mapFile ������ʲô�ָ������зָ��ģ�Ĭ����"\t"
	 */
	public void setSep(String sep) {
		this.sep = sep;
	}
	/**
	 * �趨�����ļ���ChrID�� ��㣬�յ������
	 * @param colChrID ChrID���ڵ���
	 * @param colStartNum ������ڵ�
	 * @param colEndNum �յ����ڵ���
	 */
	public void setColNum(int colChrID,int colStartNum,int colEndNum)
	{
		this.colChrID = colChrID;
		this.colStartNum = colStartNum;
		this.colEndNum = colEndNum;
	}
	
	/**
	 * 
	 * @param invNum ÿ������λ����
	 * @param chrFilePath ����һ���ļ��У�����ļ������汣����ĳ�����ֵ�����Ⱦɫ��������Ϣ��<b>�ļ����������ν�Ӳ���"/"��"\\"</b>
	 * @param mapFile mapping�Ľ���ļ���һ��Ϊbed��ʽ
	 */
	public MapReads(int invNum, String chrFilePath, String mapFile) 
	{
		this.invNum = invNum;
		this.chrFilePath = chrFilePath;
		this.mapFile = mapFile;
	}
	/**
	 * 
	 * ������Ϊmacs��bed�ļ�ʱ���Զ�<b>����chrm��Ŀ</b><br>
	 * ��ȡMapping�ļ���������Ӧ��һά�������飬��󱣴���һ����ϣ���С�ע�⣬mapping�ļ��е�chrID��chrLengthFile�е�chrIDҪһ�£���������
	 * @param uniqReads ��reads mapping��ͬһ��λ��ʱ���Ƿ������һ��reads
	 * @return
	 * @throws Exception
	 */
	public  long  ReadMapFile(boolean uniqReads) throws Exception 
	{
		long ReadsNum = 0;
		//��ν�������˵ÿ��invNum��bp�Ͱ���invNumbp��ÿ��bp��Reads������ȡƽ������λ���������chrBpReads��
		colChrID--;colStartNum--;colEndNum--;
		/////////////////////////////////////////���ÿ��Ⱦɫ��ĳ��Ȳ�������hashChrLength��////////////////////////////////////////////////////
		ChrSearch.setChrFilePath(chrFilePath);
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		int[] chrBpReads=null;//����ÿ��bp��reads�ۼ���
		int[] SumChrBpReads=null;//ֱ�Ӵ�0��ʼ��¼��1����ڶ���invNum,Ҳ��ʵ����ͬ
		/////////////////���ļ���׼������///////////////////////////////////////////////////
		TxtReadandWrite txtmap=new TxtReadandWrite();
		txtmap.setParameter(mapFile,false, true);
		BufferedReader bufmap=txtmap.readfile();
		String content="";
		String lastChr="";
		int tmpStartOld = 0 ;
		int tmpEndOld = 0;
		int tmpStart = 0;
		int tmpEnd = 0;
		////////////////////////////////////////////////////////////////////////////////////////////////
		//�ȼ���mapping����Ѿ�����ã�����Ⱦɫ���Ѿ��ֿ��á�
		while ((content = bufmap.readLine()) != null) 
		{
			String[] tmp=content.split(sep);
			///////////////////��ÿ�����溬��chrID��ʱ��///////////////////////////////////
			if (colChrID>=0) 
			{
				if (tmp[colChrID].trim().toLowerCase().equals("chrm")) {
					continue;
				}
				if (!tmp[colChrID].trim().toLowerCase().equals(lastChr)) //�������µ�chrID����ʼ�����ϵ�chrBpReads,Ȼ���½�chrBpReads�����װ���ϣ��
				{
					if (!lastChr.equals("")) //ǰ���Ѿ�����һ��chrBpReads����ô��ʼ�ܽ����chrBpReads
					{
						sumChrBp(chrBpReads, 1, SumChrBpReads);
					}
					lastChr = tmp[colChrID].trim().toLowerCase();//ʵ�������³��ֵ�ChrID
					//////////////////�ͷ��ڴ棬�о���������е��ã������ڴ浽1.2g�����˺󽵵�990m///////////////////////////
					System.out.println(lastChr);
					chrBpReads=null;//�����ܲ����ͷŵ��ڴ�
					System.gc();//��ʽ����gc
					/////////chrBpReads�趨/////////////////////////
					int chrLength = (int) ChrSearch.getChrLength(lastChr);
					chrBpReads = new int[chrLength + 1];//ͬ��Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
					chrBpReads[0] = (int) chrLength;//��������Բ��ܿ�
					////////////SumChrBpReads�趨//////////////////////////////////
					//������Ǻܾ�ȷ�����һλ���ܲ�׼������ʵ��Ӧ��������ν��,
					//Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
					int SumLength=chrBpReads.length/invNum+1;//��֤���������������Ҫ��SumChrBpReads��һ��
					SumChrBpReads=new int[SumLength];//ֱ�Ӵ�0��ʼ��¼��1����ڶ���invNum,Ҳ��ʵ����ͬ
					 ////////////���³��ֵ�chrװ���ϣ��////////////////////////////////
					hashChrBpReads.put(lastChr, SumChrBpReads);//���³��ֵ�chrID���½���SumChrBpReadsװ��hash��
					
					/////////////��ÿһ�����г���װ��lsChrLength///////////////////
					String[] tmpChrLen = new String[2];
					tmpChrLen[0] = lastChr; tmpChrLen[1] = chrLength + "";
					lsChrLength.add(tmpChrLen);
				}
			}
			//////////////////////////����Ϊfasta��ʽ��ÿ��Ⱦɫ��һ��>chrID����������mapping����///////////////////////////////////
			else 
			{
				if(content.startsWith(">"))
				{
					 Pattern pattern =Pattern.compile("chr\\w+", Pattern.CASE_INSENSITIVE);  //flags - ƥ���־�����ܰ��� CASE_INSENSITIVE��MULTILINE��DOTALL��UNICODE_CASE�� CANON_EQ��UNIX_LINES��LITERAL �� COMMENTS ��λ����  // CASE_INSENSITIVE,��Сд�����У�MULTILINE ����
					 Matcher matcher;//matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������
					 matcher = pattern.matcher(content);     
					 if (matcher.find()) 
						 lastChr=matcher.group().toLowerCase();//Сд
					 else 
						System.out.println("error");
					
					 if (!hashChrBpReads.isEmpty()) //˵�������Ѿ��ж����ˣ���ô���ڿ�ʼ�ܽ�
					 {
						 sumChrBp(chrBpReads, 1, SumChrBpReads);
					 }

					//////////////////�ͷ��ڴ棬�о���������е��ã������ڴ浽1.2g�����˺󽵵�990m///////////////////////////
					System.out.println(lastChr);
					chrBpReads = null;// �����ܲ����ͷŵ��ڴ�
					System.gc();// ��ʽ����gc
					/////////chrBpReads�趨/////////////////////////

					/////////////chrBpReads�趨////////////////////////////////////////////////////////////////////////////////////

					 int chrLength=(int) ChrSearch.getChrLength(lastChr);
					 chrBpReads=new int[chrLength+1];//ͬ��Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
					 chrBpReads[0]=(int) chrLength;//��������Բ��ܿ�
					 ////////////SumChrBpReads�趨//////////////////////////////////
					 //������Ǻܾ�ȷ�����һλ���ܲ�׼������ʵ��Ӧ��������ν��,
					 //Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
					 int SumLength=chrBpReads.length/invNum+1;//��֤���������������Ҫ��SumChrBpReads��һ��
					 SumChrBpReads=new int[SumLength];//ֱ�Ӵ�0��ʼ��¼��1����ڶ���invNum,Ҳ��ʵ����ͬ
					 ////////////���³��ֵ�chrװ���ϣ��////////////////////////////////
					 hashChrBpReads.put(lastChr, SumChrBpReads);//���³��ֵ�chrID���½���SumChrBpReadsװ��hash��
					 /////////////��ÿһ�����г���װ��lsChrLength///////////////////
					 String[] tmpChrLen=new String[2];
					 tmpChrLen[0]=lastChr;tmpChrLen[1]=chrLength+"";
					 lsChrLength.add(tmpChrLen);
					 continue;
				}
			}
			////////////////////����λ��Ӻ�chrBpReads////////////////////////////////
			
			tmpStart=Integer.parseInt(tmp[colStartNum]);//��reads �����
			tmpEnd=Integer.parseInt(tmp[colEndNum]);//��reads���յ�
			//�����reads����һ��reads��ͬ������Ϊ����������������
			if (tmpStart == tmpStartOld && tmpEnd == tmpEndOld && uniqReads) {
				continue;
			}
			for (int i = tmpStart; i <= tmpEnd; i++) {//ֱ�Ӽ���ʵ������ʵ���յ�
				//���bed�ļ��е��������ref��������꣬��ô������
				if (i >= chrBpReads.length) {
					break;
				}
				chrBpReads[i]++;
				if (chrBpReads[i]<0) 
				{
					System.out.println("��������");
				}
			}
			tmpStartOld = tmpStart;
			tmpEndOld = tmpEnd;
			ReadsNum++;
		}
		
		///////////////////ѭ��������Ҫ�����һ�ε��������ܽ�////////////////////////////////////
		 sumChrBp(chrBpReads, 1, SumChrBpReads);
		 ////////////////////////////��lsChrLength����chrLen��С�����������/////////////////////////////////////////////////////////////////////////////
		  Collections.sort(lsChrLength,new Comparator<String[]>(){
	            public int compare(String[] arg0, String[] arg1)
	            {
	               if( Integer.parseInt(arg0[1])<Integer.parseInt(arg1[1]))
	            	   return -1;
	            else if (Integer.parseInt(arg0[1])==Integer.parseInt(arg1[1])) 
					return 0;
	             else 
					return 1;
	            }
	        });
		 //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		 return ReadsNum;
	}
	
	/**
	 * ����chrBpReads����chrBpReads�����ֵ����invNum����ŵ�SumChrBpReads����
	 * ��Ϊ�����ô��ݣ������޸���SumChrBpReads���������
	 * @param chrBpReads ÿ�������reads�ۼ�ֵ
	 * @param invNum ����
	 * @param type ȡֵ���ͣ���λ����ƽ��ֵ��0��λ����1��ֵ ������Ĭ����λ��
	 * @param SumChrBpReads ��ÿ�������ڵ�
	 */
	private  void sumChrBp(int[] chrBpReads,int type,int[] SumChrBpReads) 
	{
		 int SumLength = chrBpReads.length/invNum - 1;//��֤�����������ΪjavaĬ�ϳ���ֱ�Ӻ���С����������������
		 if (invNum == 1) {
			for (int i = 0; i < SumLength; i++) {
				SumChrBpReads[i] = chrBpReads[i+1];
			}
			return;
		}
		 for (int i = 0; i < SumLength; i++)
		 {
			 int[] tmpSumReads=new int[invNum];//���ܵ�chrBpReads���ÿһ����ȡ����
			 int sumStart=i*invNum + 1; int k=0;//k������tmpSumReads���±꣬ʵ���±���У�����-1
			 for (int j = sumStart; j < sumStart + invNum; j++) 
			 {
				 tmpSumReads[k] = chrBpReads[j];
				 k++;
			 }
			 if (type==0) //ÿ��һ������ȡ��������ÿ��10bpȡ����ȡ��λ��
				 SumChrBpReads[i]=(int) MathComput.median(tmpSumReads);
			 else if (type==1) 
				 SumChrBpReads[i]=(int) MathComput.mean(tmpSumReads);
			 else //Ĭ��ȡ��λ��
				 SumChrBpReads[i]=(int) MathComput.median(tmpSumReads);
		 }
	}
	
	/**
	 * �����������䣬��ÿ�������bp�������ظö�������reads������
	 * ��λ�������˵����ڵ� ��ȡinvNum���䣬Ȼ������µ�invNum���䣬�����Ⱦɫ����mappingʱ�򲻴��ڣ��򷵻�null
	 * @param thisInvNum ÿ��������������bp�������ڵ���invNum�������invNum�ı���
	 * ���invNum ==1 && thisInvNum == 1�������ܾ�ȷ
	 * @param chrID һ��ҪСд
	 * @param startNum ������꣬Ϊʵ�����
	 * @param endNum �յ����꣬Ϊʵ���յ�
	 * ���(endNum - startNum + 1) / thisInvNum >0.7����binNum����Ϊ1
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return
	 */
	public  double[] getRengeInfo(int thisInvNum,String chrID,int startNum,int endNum,int type)
	{
		//����Ҫ�ָ���
		if (invNum == 1 && thisInvNum == 1) {
			double[] result = new double[endNum - startNum + 1];
			startNum--; endNum--;
			int[] invNumReads = hashChrBpReads.get(chrID.toLowerCase());
			int k = 0;
			for (int i = startNum; i <= endNum; i++) {
				result[k] = invNumReads[i];
				k++;
			}
			return result;
		}
		double binNum = (double)(endNum - startNum + 1) / thisInvNum;
		int binNumFinal = 0;
		if (binNum - (int)binNum >= 0.7) {
			binNumFinal = (int)binNum + 1;
		}
		else {
			binNumFinal = (int)binNum;
		}
		return getRengeInfo( chrID, startNum, endNum, binNumFinal,type);
	}
	
	
	/**
	 * �����������䣬��Ҫ���ֵĿ��������ظö�������reads�����顣�����Ⱦɫ����mappingʱ�򲻴��ڣ��򷵻�null
	 * ��λ�������˵����ڵ� ��ȡinvNum���䣬Ȼ������µ�invNum����
	 * @param chrID һ��ҪСд
	 * @param startNum ������꣬Ϊʵ�����
	 * @param endNum �յ����꣬Ϊʵ���յ�
	 * @param binNum ���ָ��������Ŀ
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return
	 */
	public  double[] getRengeInfo(String chrID,int startNum,int endNum,int binNum,int type) 
	{
		int[] invNumReads = hashChrBpReads.get(chrID.toLowerCase());
		if (invNumReads == null) 
		{
			return null;
		}
		startNum--; endNum--;
		////////////////ȷ��Ҫ��ȡ�������˵���Ҷ˵�/////////////////////////////////
		int leftNum = 0;//��invNumReads�е�ʵ�����
		int rightNum = 0;//��invNumReads�е�ʵ���յ�

		leftNum = startNum/invNum;
		double leftBias = (double)startNum/invNum-leftNum;//����߷ָ������ľ����ֵ
		double rightBias = 0;
		if (endNum%invNum==0) 
			rightNum = endNum/invNum-1;//ǰ����javaС��ת��intֱͨͨ��ȥ��С����
		else 
		{
			rightNum = endNum/invNum;//ǰ����javaС��ת��intֱͨͨ��ȥ��С����
			rightBias = rightNum + 1 - (double)endNum/invNum;//���ұ߷ָ����յ�ľ����ֵ
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		int[] tmpRegReads=new int[rightNum - leftNum + 1];
		int k=0;
		try {
			for (int i = leftNum; i <= rightNum; i++) {
				tmpRegReads[k] = invNumReads[i];
				k++;
			}
		} catch (Exception e) {
			logger.error("�±�Խ��"+e.toString());
		}
		return MathComput.mySpline(tmpRegReads, binNum,leftBias,rightBias,type);
	}
	/**
	 * ����Ⱦɫ�壬�������յ㣬���ظ�Ⱦɫ����tag���ܶȷֲ��������Ⱦɫ����mappingʱ�򲻴��ڣ��򷵻�null
	 * @param chrID 
	 * @param startLoc ������꣬Ϊʵ�����
	 * @param endLoc ���յ�Ϊ-1ʱ����ֱ��Ⱦɫ��Ľ�β��
	 * @param binNum ���ָ�Ŀ���
	 * @return
	 */
	public  double[] getReadsDensity(String chrID,int startLoc,int endLoc,int binNum ) 
	{
		//���Ƚ�reads��׼��Ϊһ��400-500bp��Ĵ�飬ÿһ������Ӧ���Ǹ���������tags���������������������������ֵ
		//Ȼ�����ڴ������ͳ�ƣ�
		//��Ź�����һ�£������Ͽ����һ��tag��1.5����ʱ�������ȽϺ���
		int tagBinLength=(int)(tagLength*1.5);
		if (startLoc==0) 
			startLoc=1;
		if(endLoc==-1)
			endLoc=(int) ChrSearch.getChrLength(chrID);
		double[] tmpReadsNum= getRengeInfo(tagBinLength, chrID, startLoc, endLoc,1);
	/**	for (int i = 0; i < tmpReadsNum.length; i++) {
			if(tmpReadsNum[i]>1)
				System.out.println(tmpReadsNum[i]);
		}
		*/
		if (tmpReadsNum==null) {
			return null;
		}
		double[] resultTagDensityNum=MathComput.mySpline(tmpReadsNum, binNum, 0, 0, 2);
		return resultTagDensityNum;
	}
	/**
	 * ���Mapping�ļ���������chr�ĳ���
	 * @param chrID
	 * @return int[]
	 * 0: ���chr����
	 * 1: �chr����
	 */
	public  int[] getLimChrLength()
	{
		int[] result=new int[2];
		result[0]=Integer.parseInt(lsChrLength.get(0)[1]);
		result[1]=Integer.parseInt(lsChrLength.get(lsChrLength.size()-1)[1]);
		return result;
	}
	
	/**
	 *  ����mRNA�ļ���
	 * �����������䣬��Ҫ���ֵĿ��������ظö�������reads�����顣�����Ⱦɫ����mappingʱ�򲻴��ڣ��򷵻�null
	 * ��λ�������˵����ڵ� ��ȡinvNum���䣬Ȼ������µ�invNum����
	 * @param chrID һ��ҪСд
	 * @param startNum ������꣬Ϊʵ����� <b>��������������</b>
	 * @param endNum �յ����꣬Ϊʵ���յ� <b>�������ں�����</b>
	 * @param binNum ���ָ��������Ŀ<b>��binNumΪ-1ʱ���������ܽᣬֱ�ӷ���invNumͳ�ƵĽ�������صĽ�����ȳ�</b>
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @param lsIsoform �������ת¼�������<b>�������������ڸ�ת¼������������</b>
	 * @return �����Ҫ����������������
	 */
	public double[] getRengeInfo(String chrID,int startNum,int endNum,int binNum,int type,ArrayList<int[]> lsIsoform) {
		boolean cis5to3 = true;
		ArrayList<double[]> lsExonInfo = new ArrayList<double[]>();
		/**
		 * ���lsIsoform��int[0]<int[1]˵������
		 * ���lsIsoform��int[1]<int[0]˵������
		 */
		for (int[] is : lsIsoform) {
			if (is[0] < is[1]) {
				break;
			}
			else if (is[0] > is[1]) {
				cis5to3 = false;
			}
			else {
				logger.error("ת¼���������ӵ������յ�����һ��"+is[0]+" "+is[1]);
			}
		}
		if (cis5to3) {
			for (int[] is : lsIsoform) {
				double[] range = null;
				// 0---start----1-----end
				if (startNum > is[1]) {
					continue;
				}
				else if (startNum >= is[0] && startNum <= is[1] && endNum > is[1]) {
					range = getRengeInfo(invNum, chrID,startNum, is[1], type);
				}
				else if (startNum >= is[0] && endNum <= is[1]) {
					range = getRengeInfo(invNum, chrID,startNum, endNum, type);
				}
				else if (startNum < is[0] && endNum > is[1]) {
					range = getRengeInfo(invNum, chrID,is[0], is[1], type);
				}
				else if (startNum < is[0] && endNum >= is[0] && endNum <= is[1]) {
					range = getRengeInfo(invNum, chrID,is[0], endNum, type);
				}
				else if (endNum < is[0]) {
					break;
				}
				lsExonInfo.add(range);
			}
		}
		else {
			for (int i = lsIsoform.size() - 1; i >= 0; i--) {
				int[] is = lsIsoform.get(i);
				double[] range = null;
				if (startNum > is[0]) {
					continue;
				}
				else if (startNum >= is[1] && startNum <= is[0] && endNum > is[0]) {
					range = getRengeInfo(invNum, chrID,startNum, is[0], type);
				}
				else if (startNum >= is[1] && endNum <= is[0]) {
					range = getRengeInfo(invNum, chrID,startNum, endNum, type);
				}
				else if (startNum < is[1] && endNum > is[0]) {
					range = getRengeInfo(invNum, chrID,is[1], is[0], type);
				}
				else if (startNum < is[1] && endNum >= is[1] && endNum <= is[0]) {
					range = getRengeInfo(invNum, chrID,is[1], endNum, type);
				}
				else if (endNum < is[1]) {
					break;
				}
				lsExonInfo.add(range);
			}
		}
		int num = 0;
		for (double[] ds : lsExonInfo) {
			num = num + ds.length;
		}
		double[] result = new double[num];
		int i = 0;
		for (double[] ds : lsExonInfo) {
			for (double d : ds) {
				result[i] = d;
				i++;
			}
		}
		if (binNum == -1) {
			return result;
		}
		
		double[] resultTagDensityNum=MathComput.mySpline(result, binNum, 0, 0, 0);
		return resultTagDensityNum;
	}
	
	
	
	
	
	
	
	
	
	
}
