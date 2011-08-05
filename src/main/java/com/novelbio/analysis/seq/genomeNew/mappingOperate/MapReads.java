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
	 * ��ÿ��double[]���/double.length
	 * Ҳ���ǽ�ÿ������Ը�gene��ƽ���������
	 */
	public static final int NORMALIZATION_PER_GENE = 128;
	/**
	 * ��ÿ��double[]*1million/AllReadsNum
	 * Ҳ���ǽ�ÿ������Բ������
	 */
	public static final int NORMALIZATION_ALL_READS = 256;
	
	
	
	/**
	 * ��������ÿ��Ⱦɫ���еĻ�������-invNum���������reads��Ŀ
	 * chrID(Сд)--int[]
	 * ֱ�Ӵ�0��ʼ��¼��1�����ڶ���invNum,Ҳ��ʵ����ͬ
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
	  * ������,bed�ļ�һ���ڵ�����
	  */
	 int colCis5To3 = 6;
	 /**
	  * ����Ƿ�Ϊ������
	  * ��ͨ��Soap���ɵ�bed�ļ���0
	  * �����bed��1
	  */
	 int startRegion = 0;
	 /**
	  * �յ��Ƿ�Ϊ������
	  */
	 int endRegion = 0;
	 /**
	  * �Ƿ��м����У����û����С��0, ��bamת����bed�ļ��в��е��У���Ҫ��RNA-Seq��ʹ��
	  * Ϊ��11��
	  */
	 int colSplit = -1;
	 /**
	  * �ܹ��ж���reads������mapping�������ReadMapFile���ܵõ���
	  */
	 long allReadsNum = 0;
	 /**
	  * �ܹ��ж���reads������mapping�������ReadMapFile���ܵõ���
	  */
	 public long getAllReadsNum() {
		return allReadsNum;
	}
	 
	 
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
	  * ����Ƿ�Ϊ������
	  * ��ͨ��Soap���ɵ�bed�ļ���0
	  * �����bed��1
	  */
	public void setstartRegion(int startRegion) {
		this.startRegion = startRegion;
	}
	/**
	 * �Ƿ��м����У����û����С��0, ��bamת����bed�ļ��в��е��У���Ҫ��RNA-Seq��ʹ��
	 * Ϊ��11��
	 */
	public void setColSplit(int colSplit) {
		this.colSplit = colSplit;
	}
	/**
	 * �趨�����ļ���ChrID�� ��㣬�յ������
	 * @param colChrID ChrID���ڵ���
	 * @param colStartNum ������ڵ�
	 * @param colEndNum �յ����ڵ���
	 */
	public void setColNum(int colChrID,int colStartNum,int colEndNum, int colCis5To3)
	{
		this.colChrID = colChrID;
		this.colStartNum = colStartNum;
		this.colEndNum = colEndNum;
		this.colCis5To3 = colCis5To3;
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
	 * @param startCod ����㿪ʼ��ȡ����bp�������õ� С��0��ʾȫ����ȡ ����reads���ȵ�����Ըò���
	 * @return ��������mapping��reads����
	 * @throws Exception
	 */
	public  long  ReadMapFile(boolean uniqReads,int startCod) throws Exception 
	{
		if (startCod > 0 && colCis5To3 < 1) {
			logger.error("�����趨startCod����Ϊû���趨������");
			return -1;
		}
//		��һ��startRegion�Ƿ�������
		long ReadsNum = 0;
		//��ν�������˵ÿ��invNum��bp�Ͱ���invNumbp��ÿ��bp��Reads������ȡƽ������λ���������chrBpReads��
		colChrID--;colStartNum--;colEndNum--;colSplit--;colCis5To3--;
		/////////////////////////////////////////���ÿ��Ⱦɫ��ĳ��Ȳ�������hashChrLength��////////////////////////////////////////////////////
		ChrSearch.setChrFilePath(chrFilePath);
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		int[] chrBpReads=null;//����ÿ��bp��reads�ۼ���
		int[] SumChrBpReads=null;//ֱ�Ӵ�0��ʼ��¼��1�����ڶ���invNum,Ҳ��ʵ����ͬ
		/////////////////���ļ���׼������///////////////////////////////////////////////////
		TxtReadandWrite txtmap=new TxtReadandWrite();
		txtmap.setParameter(mapFile,false, true);
		BufferedReader bufmap=txtmap.readfile();
		String content="";
		String lastChr="";

		////////////////////////////////////////////////////////////////////////////////////////////////
		//�ȼ���mapping����Ѿ�����ã�����Ⱦɫ���Ѿ��ֿ��á�
		boolean flag = true;//��û�и�Ⱦɫ��ʱ���Ϊfalse�����������и�Ⱦɫ���ϵ�����
		int tmpStartOld = 0; int tmpEndOld = 0;
		while ((content = bufmap.readLine()) != null) 
		{
			String[] tmp=content.split(sep);
			///////////////////��ÿ�����溬��chrID��ʱ��///////////////////////////////////
			if (colChrID>=0) 
			{
//				if (tmp[colChrID].trim().toLowerCase().equals("chrm")) {
//					continue;
//				}
				if (!tmp[colChrID].trim().toLowerCase().equals(lastChr)) //�������µ�chrID����ʼ�����ϵ�chrBpReads,Ȼ���½�chrBpReads�����װ���ϣ��
				{
					if (!lastChr.equals("") && flag) //ǰ���Ѿ�����һ��chrBpReads����ô��ʼ�ܽ����chrBpReads
					{
						sumChrBp(chrBpReads, 1, SumChrBpReads);
					}
					lastChr = tmp[colChrID].trim().toLowerCase();//ʵ�������³��ֵ�ChrID
					//////////////////�ͷ��ڴ棬�о���������е��ã������ڴ浽1.2g�����˺󽵵�990m///////////////////////////
					System.out.println(lastChr);
					chrBpReads=null;//�����ܲ����ͷŵ��ڴ�
					System.gc();//��ʽ����gc
					int chrLength = 0;
					/////////chrBpReads�趨/////////////////////////
					try {
						chrLength = (int) ChrSearch.getChrLength(lastChr);
						flag = true;
					} catch (Exception e) {
						flag = false;
						continue;
					}
					
					chrBpReads = new int[chrLength + 1];//ͬ��Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
					chrBpReads[0] = (int) chrLength;//��������Բ��ܿ�
					////////////SumChrBpReads�趨//////////////////////////////////
					//������Ǻܾ�ȷ�����һλ���ܲ�׼������ʵ��Ӧ��������ν��,
					//Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
					int SumLength=chrBpReads.length/invNum+1;//��֤���������������Ҫ��SumChrBpReads��һ��
					SumChrBpReads=new int[SumLength];//ֱ�Ӵ�0��ʼ��¼��1�����ڶ���invNum,Ҳ��ʵ����ͬ
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
					 SumChrBpReads=new int[SumLength];//ֱ�Ӵ�0��ʼ��¼��1�����ڶ���invNum,Ҳ��ʵ����ͬ
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
			if (flag == false) {
				continue;
			}
			int tmpStart = 0;
			int tmpEnd = 0;
			int tmpStart2 = 0; int tmpEnd2 = 0;
			
			tmpStart=Integer.parseInt(tmp[colStartNum]) + startRegion;//��reads �����
			tmpEnd = Integer.parseInt(tmp[colEndNum]) + endRegion;//��reads���յ�
			//�����reads����һ��reads��ͬ������Ϊ����������������
			if (uniqReads && tmpStart == tmpStartOld && tmpEnd == tmpEndOld ) {
				continue;
			}
			else {
				tmpStartOld = tmpStart ;
				tmpEndOld =  tmpEnd;
			}
			boolean cis5to3 = true;
			if (colCis5To3 >= 0) {
				cis5to3 = tmp[colCis5To3].trim().equals("+");
			}
			//���û�пɱ����
			if (colSplit <= 0 ||  !tmp[colSplit].contains(",")) {
//				���˳ʽ
				if (cis5to3) {
					if (startCod > 0 && tmpStart + startCod - 1 < tmpEnd) {
						tmpEnd = tmpStart + startCod - 1;
					}
				}
				else {
					if (startCod > 0 && tmpEnd - startCod +1 > tmpStart) {
						tmpStart = tmpEnd - startCod +1;
					}
				}
			}
			else {
				String[] tmpSplit = tmp[colSplit].split(",");
				tmpEnd = tmpStart + Integer.parseInt(tmpSplit[0]) - startRegion;
				if (cis5to3) {
					if (startCod > 0 && tmpStart + startCod - 1 < tmpEnd) {
						tmpEnd = tmpStart + startCod - 1;
					}
					else {
						tmpEnd2 = Integer.parseInt(tmp[colEndNum]) + endRegion;
						tmpStart2 = tmpEnd2 - Integer.parseInt(tmpSplit[1]) - endRegion + startRegion;
						int remain = startCod - (tmpEnd - tmpStart + 1);
						if (startCod>0 && remain > 0) {
							if (tmpEnd2 - tmpStart2 + 1 > remain) {
								tmpEnd2 = tmpStart2 + remain - 1;
							}
						}
					}
				}
				else {
					tmpEnd2 = Integer.parseInt(tmp[colEndNum]) + endRegion;
					tmpStart2 = tmpEnd2 - Integer.parseInt(tmpSplit[1]) - endRegion + startRegion;
					if (startCod > 0 &&  tmpEnd2 - startCod +1 > tmpStart2) {
						tmpStart2 = tmpEnd2 - startCod +1;
						tmpStart = 0; tmpEnd = -1;
					}
					else {
						int remain = startCod - (tmpEnd2 - tmpStart2 + 1);
						if (startCod>0 && remain > 0) {
							if (tmpEnd - tmpStart + 1 > remain) {
								tmpStart = tmpEnd - remain + 1;
							}
						}
					}
				}
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
			if (tmpStart2 >0) {
				for (int i = tmpStart2; i <= tmpEnd2; i++) {//ֱ�Ӽ���ʵ������ʵ���յ�
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
			}
			
			ReadsNum++;
		}
		
		///////////////////ѭ��������Ҫ�����һ�ε��������ܽ�////////////////////////////////////
		if (flag) {
			sumChrBp(chrBpReads, 1, SumChrBpReads);
		}
		 
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
		  allReadsNum = ReadsNum;
		  return ReadsNum;
	}
	
	
	/**
	 * 
	 * ������Ϊmacs��bed�ļ�ʱ���Զ�<b>����chrm��Ŀ</b><br>
	 * ��ȡMapping�ļ���������Ӧ��һά�������飬��󱣴���һ����ϣ���С�ע�⣬mapping�ļ��е�chrID��chrLengthFile�е�chrIDҪһ�£���������
	 * @param uniqReads ��reads mapping��ͬһ��λ��ʱ���Ƿ������һ��reads
	 * @param startCod ����㿪ʼ��ȡ����bp�������õ� С��0��ʾȫ����ȡ ����reads���ȵ�����Ըò���
	 * @return ��������mapping��reads����
	 * @throws Exception
	 */
	public  long  ReadMapFileRepeat(boolean uniqReads,int startCod, int colRepeat) throws Exception 
	{
		if (startCod > 0 && colCis5To3 < 1) {
			logger.error("�����趨startCod����Ϊû���趨������");
			return -1;
		}
//		��һ��startRegion�Ƿ�������
		long ReadsNum = 0;
		//��ν�������˵ÿ��invNum��bp�Ͱ���invNumbp��ÿ��bp��Reads������ȡƽ������λ���������chrBpReads��
		colChrID--;colStartNum--;colEndNum--;colSplit--;colCis5To3--;colRepeat--;
		/////////////////////////////////////////���ÿ��Ⱦɫ��ĳ��Ȳ�������hashChrLength��////////////////////////////////////////////////////
		ChrSearch.setChrFilePath(chrFilePath);
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		double[] chrBpReads=null;//����ÿ��bp��reads�ۼ���
		int[] SumChrBpReads=null;//ֱ�Ӵ�0��ʼ��¼��1�����ڶ���invNum,Ҳ��ʵ����ͬ
		/////////////////���ļ���׼������///////////////////////////////////////////////////
		TxtReadandWrite txtmap=new TxtReadandWrite();
		txtmap.setParameter(mapFile,false, true);
		BufferedReader bufmap=txtmap.readfile();
		String content="";
		String lastChr="";

		////////////////////////////////////////////////////////////////////////////////////////////////
		//�ȼ���mapping����Ѿ�����ã�����Ⱦɫ���Ѿ��ֿ��á�
		boolean flag = true;//��û�и�Ⱦɫ��ʱ���Ϊfalse�����������и�Ⱦɫ���ϵ�����
		int tmpStartOld = 0; int tmpEndOld = 0;
		while ((content = bufmap.readLine()) != null) 
		{
			String[] tmp=content.split(sep);
				if (!tmp[colChrID].trim().toLowerCase().equals(lastChr)) //�������µ�chrID����ʼ�����ϵ�chrBpReads,Ȼ���½�chrBpReads�����װ���ϣ��
				{
					if (!lastChr.equals("") && flag) //ǰ���Ѿ�����һ��chrBpReads����ô��ʼ�ܽ����chrBpReads
					{
						sumChrBp(chrBpReads, 1, SumChrBpReads);
					}
					lastChr = tmp[colChrID].trim().toLowerCase();//ʵ�������³��ֵ�ChrID
					//////////////////�ͷ��ڴ棬�о���������е��ã������ڴ浽1.2g�����˺󽵵�990m///////////////////////////
					System.out.println(lastChr);chrBpReads=null;//�����ܲ����ͷŵ��ڴ�
					System.gc();//��ʽ����gc
					int chrLength = 0;
					/////////chrBpReads�趨/////////////////////////
					try {
						chrLength = (int) ChrSearch.getChrLength(lastChr);
						flag = true;
					} catch (Exception e) {
						flag = false;
						continue;
					}
					chrBpReads = new double[chrLength + 1];//ͬ��Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
					chrBpReads[0] = (int) chrLength;//��������Բ��ܿ�
					////////////SumChrBpReads�趨//////////////////////////////////
					//������Ǻܾ�ȷ�����һλ���ܲ�׼������ʵ��Ӧ��������ν��,
					//Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
					int SumLength=chrBpReads.length/invNum+1;//��֤���������������Ҫ��SumChrBpReads��һ��
					SumChrBpReads=new int[SumLength];//ֱ�Ӵ�0��ʼ��¼��1�����ڶ���invNum,Ҳ��ʵ����ͬ
					 ////////////���³��ֵ�chrװ���ϣ��////////////////////////////////
					hashChrBpReads.put(lastChr, SumChrBpReads);//���³��ֵ�chrID���½���SumChrBpReadsװ��hash��
					
					/////////////��ÿһ�����г���װ��lsChrLength///////////////////
					String[] tmpChrLen = new String[2];
					tmpChrLen[0] = lastChr; tmpChrLen[1] = chrLength + "";
					lsChrLength.add(tmpChrLen);
				}
			////////////////////����λ��Ӻ�chrBpReads////////////////////////////////
			if (flag == false) {//��û�и�Ⱦɫ��ʱ���Ϊfalse�����������и�Ⱦɫ���ϵ�����
				continue;
			}
			int tmpStart = 0;
			int tmpEnd = 0;
			int tmpStart2 = 0; int tmpEnd2 = 0;
			
			tmpStart = Integer.parseInt(tmp[colStartNum]) + startRegion;//��reads �����
			tmpEnd = Integer.parseInt(tmp[colEndNum]) + endRegion;//��reads���յ�
			//�����reads����һ��reads��ͬ������Ϊ����������������
			if (uniqReads && tmpStart == tmpStartOld && tmpEnd == tmpEndOld ) {
				continue;
			}
			else {
				tmpStartOld = tmpStart ;
				tmpEndOld =  tmpEnd;
			}
			boolean cis5to3 = true;
			if (colCis5To3 >= 0) {
				cis5to3 = tmp[colCis5To3].trim().equals("+");
			}
			//���û�пɱ����
			if (colSplit <= 0 ||  !tmp[colSplit].contains(",")) {
//				���˳ʽ
				if (cis5to3) {
					if (startCod > 0 && tmpStart + startCod - 1 < tmpEnd) {
						tmpEnd = tmpStart + startCod - 1;
					}
				}
				else {
					if (startCod > 0 && tmpEnd - startCod +1 > tmpStart) {
						tmpStart = tmpEnd - startCod +1;
					}
				}
			}
			else {
				String[] tmpSplit = tmp[colSplit].split(",");
				tmpEnd = tmpStart + Integer.parseInt(tmpSplit[0]) - startRegion;
				if (cis5to3) {
					if (startCod > 0 && tmpStart + startCod - 1 < tmpEnd) {
						tmpEnd = tmpStart + startCod - 1;
					}
					else {
						tmpEnd2 = Integer.parseInt(tmp[colEndNum]) + endRegion;
						tmpStart2 = tmpEnd2 - Integer.parseInt(tmpSplit[1]) - endRegion + startRegion;
						int remain = startCod - (tmpEnd - tmpStart + 1);
						if (startCod>0 && remain > 0) {
							if (tmpEnd2 - tmpStart2 + 1 > remain) {
								tmpEnd2 = tmpStart2 + remain - 1;
							}
						}
					}
				}
				else {
					tmpEnd2 = Integer.parseInt(tmp[colEndNum]) + endRegion;
					tmpStart2 = tmpEnd2 - Integer.parseInt(tmpSplit[1]) - endRegion + startRegion;
					if (startCod > 0 &&  tmpEnd2 - startCod +1 > tmpStart2) {
						tmpStart2 = tmpEnd2 - startCod +1;
						tmpStart = 0; tmpEnd = -1;
					}
					else {
						int remain = startCod - (tmpEnd2 - tmpStart2 + 1);
						if (startCod>0 && remain > 0) {
							if (tmpEnd - tmpStart + 1 > remain) {
								tmpStart = tmpEnd - remain + 1;
							}
						}
					}
				}
			}

			int repeat = Integer.parseInt(tmp[colRepeat]);
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
			if (tmpStart2 >0) {
				for (int i = tmpStart2; i <= tmpEnd2; i++) {//ֱ�Ӽ���ʵ������ʵ���յ�
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
			}
			
			ReadsNum++;
		}
		
		///////////////////ѭ��������Ҫ�����һ�ε��������ܽ�////////////////////////////////////
		if (flag) {
			sumChrBp(chrBpReads, 1, SumChrBpReads);
		}
		 
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
		  allReadsNum = ReadsNum;
		  return ReadsNum;
	}

	/**
	 * Chr1	5242	5444	A80W3KABXX:8:44:8581:122767#GGCTACAT/2	255	-	5242	5444	255,0,0	2	30,20,40	0,120,160
	 * @param start ������� 5242���������������
	 * @param end �յ����� 5444���������������
	 * @param col �ָ���� 30,20,40
	 * @return ����һ��start��end
	 * ���ݼ����������
	 */
	private ArrayList<int[]> getStartEndLoc(int start, int end, String split, String splitStart) {
		ArrayList<int[]> lsStartEnd = new ArrayList<int[]>();
		String[] splitLen = split.split(",");
		String[] splitLoc = splitStart.split(",");
		for (int i = 0; i < splitLen.length; i++) {
			int[] startend = new int[2];
			startend[0] = start + Integer.parseInt(splitLoc[i]);
			startend[1] = startend[0] + Integer.parseInt(splitLen[i]) - 1;
			lsStartEnd.add(startend);
		}
		return lsStartEnd;
	}
	/**
	 * �����������ȡ��Ӧ��������󷵻���Ҫ�ۼӵ�ArrayList<int[]>
	 * @param lsStartEnd
	 * @param cis5to3
	 * @return ���cis5to3 = True����ô���Ž�ȡstartCod���ȵ�����
	 * ���cis5to3 = False����ô���Ž�ȡstartCod���ȵ�����
	 */
	private ArrayList<int[]> setStartCod(ArrayList<int[]> lsStartEnd, int StartCodLen, boolean cis5to3) {
		ArrayList<int[]> lsResult = new ArrayList<int[]>();
		if (cis5to3) {
			for (int i = 0; i < lsStartEnd.size(); i++) {
				if (StartCodLen - (lsStartEnd.get(i)[1] - lsStartEnd.get(i)[0] +1) > 0) {
					lsResult.add(lsStartEnd.get(i));
					StartCodLen = StartCodLen - (lsStartEnd.get(i)[1] - lsStartEnd.get(i)[0] +1);
				}
				else {
					int[] last = new int[2];
					last[0] = lsStartEnd.get(i)[0];
					last[1] = lsStartEnd.get(i)[0] + StartCodLen - 1;
					lsResult.add(last);
					break;
				}
			}
		}
		else {
			for (int i = lsStartEnd.size() - 1; i >= 0; i--) {
				if (StartCodLen - (lsStartEnd.get(i)[1] - lsStartEnd.get(i)[0] +1) > 0) {
					lsResult.add(0,lsStartEnd.get(i));
					StartCodLen = StartCodLen - (lsStartEnd.get(i)[1] - lsStartEnd.get(i)[0] + 1);
				}
				else {
					int[] last = new int[2];
					last[1] = lsStartEnd.get(i)[1];
					last[0] = last[1] - StartCodLen + 1;
					lsResult.add(0,last);
				}
			}
		}
		return lsResult;
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
	 * ����chrBpReads����chrBpReads�����ֵ����invNum����ŵ�SumChrBpReads����
	 * ��Ϊ�����ô��ݣ������޸���SumChrBpReads���������
	 * @param chrBpReads ÿ�������reads�ۼ�ֵ
	 * @param invNum ����
	 * @param type ȡֵ���ͣ���λ����ƽ��ֵ��0��λ����1��ֵ ������Ĭ����λ��
	 * @param SumChrBpReads ��ÿ�������ڵ�
	 */
	private  void sumChrBp(double[] chrBpReads,int type,int[] SumChrBpReads) 
	{
		 int SumLength = chrBpReads.length/invNum - 1;//��֤�����������ΪjavaĬ�ϳ���ֱ�Ӻ���С����������������
		 if (invNum == 1) {
			for (int i = 0; i < SumLength; i++) {
				SumChrBpReads[i] = (int) Math.round(chrBpReads[i+1]);
			}
			return;
		}
		 for (int i = 0; i < SumLength; i++)
		 {
			 int[] tmpSumReads=new int[invNum];//���ܵ�chrBpReads���ÿһ����ȡ����
			 int sumStart=i*invNum + 1; int k=0;//k������tmpSumReads���±꣬ʵ���±���У�����-1
			 for (int j = sumStart; j < sumStart + invNum; j++) 
			 {
				 tmpSumReads[k] = (int) Math.round(chrBpReads[j]);
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
		if (startNum > endNum) {
			logger.error("��㲻�ܱ��յ��: "+chrID+" "+startNum+" "+endNum);
		}
		//����Ҫ�ָ���
		if (invNum == 1 && thisInvNum == 1) {
			double[] result = new double[endNum - startNum + 1];
			startNum--; endNum--;
			int[] invNumReads = hashChrBpReads.get(chrID.toLowerCase());
			if (invNumReads == null) {
				logger.error("û�и�Ⱦɫ�壺 " + chrID);
				return null;
			}
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
		if (startNum > endNum) {
			logger.error("��㲻�ܱ��յ��: "+chrID+" "+startNum+" "+endNum);
		}
		
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
		//���Ƚ�reads��׼��Ϊһ��400-500bp���Ĵ�飬ÿһ������Ӧ���Ǹ���������tags���������������������������ֵ
		//Ȼ�����ڴ������ͳ�ƣ�
		//��Ź�����һ�£������Ͽ�����һ��tag��1.5����ʱ�������ȽϺ���
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
	 * @param binNum ���ָ��������Ŀ<b>��binNumΪ-1ʱ���������ܽᣬֱ�ӷ���invNumͳ�ƵĽ�������صĽ�����ȳ�</b>
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @param lsIsoform �������ת¼�������<b>����int[0]&lt;int[1]������int[0]&gt;int[1]</b>
	 * @return ���صĶ������򣬽������Ҫ��������������������������
	 * ���û�ҵ�ת¼������chr1_random֮��ID�ģ��Ǿ�����
	 */
	public double[] getRengeInfo(String chrID,int binNum,int type,ArrayList<int[]> lsIsoform) {
		if (lsIsoform == null || lsIsoform.size() == 0) {
			return null;
		}
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
				break;
			}
			else {
				logger.error("ת¼���������ӵ������յ�����һ��"+is[0]+" "+is[1]);
			}
		}
		
		if (cis5to3) {
			for (int[] is : lsIsoform) {
				double[] isoInfo = getRengeInfo(invNum, chrID, is[0], is[1], type);
				if (isoInfo != null) {
					lsExonInfo.add(isoInfo);
				}
			}
		}
		else {
			for (int i = lsIsoform.size() - 1; i >= 0; i--) {
				double[] isoInfo = getRengeInfo(invNum, chrID, lsIsoform.get(i)[1], lsIsoform.get(i)[0], type);
				if (isoInfo != null) {
					lsExonInfo.add(isoInfo);
				}
			}
		}
		int num = 0;
		for (double[] ds : lsExonInfo) {
			if (ds == null) {
				logger.error("�޷����reads��Ϣ��Ⱦɫ��Ϊ: " + chrID);
				return null;
			}
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
		if (binNum <= 0) {
			return result;
		}
		
		double[] resultTagDensityNum=MathComput.mySpline(result, binNum, 0, 0, 0);
		return resultTagDensityNum;
	}
	
	
	/**
	 * �����doubleֱ���޸ģ������ء�<br>
	 * ���õ��Ľ����Ҫ���ֵ
	 * ����double���飬����reads�������б�׼��,reads�����ɶ�ȡ��mapping�ļ��Զ����<br>
	 * ����ȳ���1millionȻ���ٳ���ÿ��double��ֵ<br>
	 * @param doubleInfo
	 * @param NormType ����ѡ��MapReads��NORMALIZATION��
	 * @return 
	 */
	public void normDouble(double[] doubleInfo, int NormType) {
		if (NormType == NORMALIZATION_ALL_READS) {
			for (int i = 0; i < doubleInfo.length; i++) {
				doubleInfo[i] = doubleInfo[i]*1000000/allReadsNum;
			}
		}
		else if (NormType == NORMALIZATION_PER_GENE) {
			double avgSite = MathComput.mean(doubleInfo);
			for (int i = 0; i < doubleInfo.length; i++) {
				doubleInfo[i] = doubleInfo[i]/avgSite;
			}
		}
	}
	
	
	
	
	
	
}