package com.novelbio.base.genome.mappingOperate;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.base.dataOperate.TxtReadandWrite;


/**
 * �������ڴ����Ƶı�
 * @author zong0jie
 *
 */
public class ShortMapReads {
	
	/**
	 * ��������ÿ��Ⱦɫ���еĻ�������-invNum���������reads��Ŀ
	 * chrID(Сд)--short[]
	 * ����short[]��1��ʼ��0��¼��short�ĳ��ȣ����ǻ�������Բ�׼��
	 *  
	 */
	static Hashtable<String, short[]> hashChrBpReads=new Hashtable<String, short[]>();
	
	/**
	 * ��������ÿ��Ⱦɫ���Ӧ��chr����
	 */
	static Hashtable<String, Integer> hashChrLength=new Hashtable<String, Integer>();
	/**
	 * ��������mapping�ļ��г��ֹ���ÿ��chr �ĳ���
	 */
	static ArrayList<String[]> lsChrLength=new ArrayList<String[]>();
	static int invNum=0;
	static int tagLength=400;//��ReadMapFile������ֵ
	public static void setTagLength(int thisTagLength) {
		tagLength=thisTagLength;
	}
	/**
	 * �趨˫��readsTagƴ�����󳤶ȵĹ���ֵ��Ŀǰsolexa˫���������ȴ����400bp������̫��ȷ
	 * ����Ƿ�����getReadsDensity����reads�ܶȵĶ���
	 * @param readsTagLength
	 */
	public void setReadsTagLength(int readsTagLength)
	{
		this.tagLength=readsTagLength;
	}
	
	
	/**
	 * ��ȡMapping�ļ���������Ӧ��һά�������飬��󱣴���һ����ϣ���С�ע�⣬mapping�ļ��е�chrID��chrLengthFile�е�chrIDҪһ�£���������
	 * @param mapFile mapping�Ľ���ļ���һ��Ϊbed��ʽ
	 * @param chrLengthFile ���汣��ÿ��chr�ж೤�����߳�������ɶ೤��short���飬�ļ���ʽΪ chrID(Сд)+"\t"+chrLength(long)+����
	 * @param colChrID ChrID�ڵڼ��У���1��ʼ
	 * @param colStartNum mapping����ڵڼ��У���1��ʼ
	 * @param colEndNum mapping�յ��ڵڼ��У���1��ʼ
	 * @param invNum ÿ������λ����
	 * @throws Exception
	 */
	public static void  ReadMapFile(String mapFile,String chrLengthFile,String sep,int colChrID,int colStartNum,int colEndNum,int thisinvNum) throws Exception 
	{
		//��ν�������˵ÿ��invNum��bp�Ͱ���invNumbp��ÿ��bp��Reads������ȡƽ������λ���������chrBpReads��
		colChrID--;colStartNum--;colEndNum--;
		invNum=thisinvNum;
		
		/////////////////////////////////////////���ÿ��Ⱦɫ��ĳ��Ȳ�������hashChrLength��////////////////////////////////////////////////////
		TxtReadandWrite txtChrLength=new TxtReadandWrite();
		txtChrLength.setParameter(chrLengthFile,false, true);
		String[][] chrLengthInfo=txtChrLength.ExcelRead("\t", 1, 1, txtChrLength.ExcelRows(), 2);
		for (int i = 0; i < chrLengthInfo.length; i++) {
			hashChrLength.put(chrLengthInfo[i][0], Integer.parseInt(chrLengthInfo[i][1]));
		}
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		short[] chrBpReads=null;//����ÿ��bp��reads�ۼ���
		short[] SumChrBpReads=null;//ֱ�Ӵ�0��ʼ��¼��1����ڶ���invNum,Ҳ��ʵ����ͬ
		
		/////////////////���ļ���׼������///////////////////////////////////////////////////
		TxtReadandWrite txtmap=new TxtReadandWrite();
		txtmap.setParameter(mapFile, false,true);
		BufferedReader bufmap=txtmap.readfile();
		String content="";
		String lastChr="";
		////////////////////////////////////////////////////////////////////////////////////////////////
		//�ȼ���mapping����Ѿ�����ã�����Ⱦɫ���Ѿ��ֿ��á�
		while ((content=bufmap.readLine())!=null) 
		{
			String[] tmp=content.split(sep);
			
			///////////////////��ÿ�����溬��chrID��ʱ��///////////////////////////////////
			if (colChrID>=0) 
			{
				if (!tmp[colChrID].trim().toLowerCase().equals(lastChr)) //�������µ�chrID����ʼ�����ϵ�chrBpReads,Ȼ���½�chrBpReads�����װ���ϣ��
				{
					if (!lastChr.equals("")) //ǰ���Ѿ�����һ��chrBpReads����ô��ʼ�ܽ����chrBpReads
					{
						sumChrBp(chrBpReads, 1, SumChrBpReads);
					}
					
					lastChr=tmp[colChrID].trim().toLowerCase();//ʵ�������³��ֵ�ChrID
				
					
					
					//////////////////�ͷ��ڴ棬�о���������е��ã������ڴ浽1.2g�����˺󽵵�990m///////////////////////////
					System.out.println(lastChr);
					chrBpReads=null;//�����ܲ����ͷŵ��ڴ�
					System.gc();//��ʽ����gc
					/////////chrBpReads�趨/////////////////////////
					int chrLength;
					chrLength=hashChrLength.get(lastChr);
					chrBpReads=new short[chrLength+1];//ͬ��Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
					chrBpReads[0]=(short) chrLength;//��������Բ��ܿ�
					////////////SumChrBpReads�趨//////////////////////////////////
					//������Ǻܾ�ȷ�����һλ���ܲ�׼������ʵ��Ӧ��������ν��,
					//Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
					int SumLength=chrBpReads.length/invNum+1;//��֤���������������Ҫ��SumChrBpReads��һ��
					SumChrBpReads=new short[SumLength];//ֱ�Ӵ�0��ʼ��¼��1����ڶ���invNum,Ҳ��ʵ����ͬ
					
					
					 ////////////���³��ֵ�chrװ���ϣ��////////////////////////////////
					hashChrBpReads.put(lastChr, SumChrBpReads);//���³��ֵ�chrID���½���SumChrBpReadsװ��hash��
					
					/////////////��ÿһ�����г���װ��lsChrLength///////////////////
					String[] tmpChrLen=new String[2];
					tmpChrLen[0]=lastChr;tmpChrLen[1]=chrLength+"";
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
						chrBpReads=null;//�����ܲ����ͷŵ��ڴ�
						System.gc();//��ʽ����gc
						/////////chrBpReads�趨/////////////////////////
						
					 /////////////chrBpReads�趨////////////////////////////////////////////////////////////////////////////////////
					 int chrLength=hashChrLength.get(lastChr);
					 chrBpReads=new short[chrLength+1];//ͬ��Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
					 chrBpReads[0]=(short) chrLength;//��������Բ��ܿ�
					 ////////////SumChrBpReads�趨//////////////////////////////////
					 //������Ǻܾ�ȷ�����һλ���ܲ�׼������ʵ��Ӧ��������ν��,
					 //Ϊ���㣬0λ��¼�ܳ��ȡ�����ʵ��bp����ʵ�ʳ���
					 int SumLength=chrBpReads.length/invNum+1;//��֤���������������Ҫ��SumChrBpReads��һ��
					 SumChrBpReads=new short[SumLength];//ֱ�Ӵ�0��ʼ��¼��1����ڶ���invNum,Ҳ��ʵ����ͬ
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
			int tmpStart=Integer.parseInt(tmp[colStartNum]);//��reads �����
			int tmpEnd=Integer.parseInt(tmp[colEndNum]);//��reads���յ�
			for (int i = tmpStart; i <= tmpEnd; i++) {//ֱ�Ӽ���ʵ������ʵ���յ�
				chrBpReads[i]++;
				if (chrBpReads[i]<0) 
				{
					System.out.println("��������");
				}
			}
		}
		///////////////////ѭ��������Ҫ�����һ�ε��������ܽ�////////////////////////////////////
		 sumChrBp(chrBpReads, 1, SumChrBpReads);
		 //��lsChrLength����chrLen��С�����������
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
		 
		 
	}
	
	/**
	 * ����chrBpReads����chrBpReads�����ֵ����invNum����ŵ�SumChrBpReads����
	 * ��Ϊ�����ô��ݣ������޸���SumChrBpReads���������
	 * @param chrBpReads ÿ�������reads�ۼ�ֵ
	 * @param invNum ����
	 * @param type ȡֵ���ͣ���λ����ƽ��ֵ��0��λ����1��ֵ ������Ĭ����λ��
	 * @param SumChrBpReads ��ÿ�������ڵ�
	 */
	private static void sumChrBp(short[] chrBpReads,int type,short[] SumChrBpReads) 
	{
		 int SumLength=chrBpReads.length/invNum-1;//��֤�����������ΪjavaĬ�ϳ���ֱ�Ӻ���С����������������
		 for (int i = 0; i < SumLength; i++) 
		 {
			 short[] tmpSumReads=new short[invNum];//���ܵ�chrBpReads���ÿһ����ȡ����
			 int sumStart=i*invNum+1;int k=0;//k������tmpSumReads���±꣬ʵ���±���У�����-1
			 for (int j =sumStart; j <sumStart+invNum; j++) 
			 {
				 tmpSumReads[k]=chrBpReads[j];
				 k++;
			 }
			 if (type==0) //ÿ��һ������ȡ��������ÿ��10bpȡ����ȡ��λ��
				 SumChrBpReads[i]=(short) median(tmpSumReads);
			 else if (type==1) 
				 SumChrBpReads[i]=(short) mean(tmpSumReads);
			 else //Ĭ��ȡ��λ��
				 SumChrBpReads[i]=(short) median(tmpSumReads);
		 }
	}
	
	
	/**
	 * �������ݣ������λ��, ����10
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
	 * ���ڻ��ÿ10��bp��tag�ѻ�������λ��
	 * @return
	 */
	private static int mean(short[] unsortNum)
	{
		int length=unsortNum.length;
		int sum=0;
		for(int i=1;i<length;i++)
		{
			sum=sum+unsortNum[i];
		}
		int avg=sum/length;
		if(avg>32767)
			System.out.println("avg�ܽ�ʱ���");
		return avg;
	}
	
	
	
	/**
	 * �������ݣ������λ��, ����10
	 * ���ò������򷨣���˵����С��ģ����Ч�ʻ�����
	 * ���ڻ��ÿ10��bp��tag�ѻ�������λ��
	 * @return
	 */
	private static int median(short[] unsortNum)
	{
		int med=-100;
		int tmp=-10000;
		int length=unsortNum.length;
		for(int i=1;i<length;i++)
		{
			tmp=unsortNum[i];
			int j=i;
			for(;j>0;j--)
			{
				if(tmp<unsortNum[j-1])
				{
					unsortNum[j]=unsortNum[j-1];
				}
				else break;
			}
			unsortNum[j]=(short) tmp;
		}
		if (length%2==0) 
			med=(unsortNum[length/2-1]+unsortNum[length/2])/2;
		else 
			med=unsortNum[length/2];
		if(med>32767)
			System.out.println("med�ܽ�ʱ���");
		return med;
	}
	
	/**
	 * �����������䣬��ÿ�������bp�������ظö�������reads������
	 * ��λ�������˵����ڵ� ��ȡinvNum���䣬Ȼ������µ�invNum����
	 * @param thisInvNum ÿ��������������bp�������ڵ���invNum�������invNum�ı���
	 * @param chrID һ��ҪСд
	 * @param startNum ������꣬Ϊʵ�����
	 * @param endNum �յ����꣬Ϊʵ���յ�
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return
	 */
	public static double[] getRengeInfo(int thisInvNum,String chrID,int startNum,int endNum,int type)
	{
		int binNum=(endNum-startNum)/thisInvNum;
		return getRengeInfo( chrID, startNum, endNum, binNum,type);
	}
	
	
	/**
	 * �����������䣬��Ҫ���ֵĿ��������ظö�������reads������
	 * ��λ�������˵����ڵ� ��ȡinvNum���䣬Ȼ������µ�invNum����
	 * @param chrID һ��ҪСд
	 * @param startNum ������꣬Ϊʵ�����
	 * @param endNum �յ����꣬Ϊʵ���յ�
	 * @param binNum ���ָ��������Ŀ
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return
	 */
	public static double[] getRengeInfo(String chrID,int startNum,int endNum,int binNum,int type) 
	{
		short[] invNumReads=hashChrBpReads.get(chrID.toLowerCase());
		startNum--;endNum--;
		////////////////ȷ��Ҫ��ȡ�������˵���Ҷ˵�/////////////////////////////////
		int leftNum=0;//��invNumReads�е�ʵ�����
		int rightNum=0;//��invNumReads�е�ʵ���յ�

		leftNum=startNum/invNum;
		double leftBias=(double)startNum/invNum-leftNum;//����߷ָ������ľ����ֵ
		
		
		double rightBias=0;
		if (endNum%invNum==0) 
			rightNum=endNum/invNum-1;//ǰ����javaС��ת��intֱͨͨ��ȥ��С����
		else 
		{
			rightNum=endNum/invNum;//ǰ����javaС��ת��intֱͨͨ��ȥ��С����
			rightBias=(double)endNum/invNum-rightNum;//���ұ߷ָ����յ�ľ����ֵ
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////
		short[] tmpRegReads=new short[rightNum-leftNum+1];
		int k=0;
		for (int i = leftNum; i <= rightNum; i++) {
			tmpRegReads[k]=invNumReads[i];
			k++;
		}
		return mySpline(tmpRegReads, binNum,leftBias,rightBias,type);
	}
	
	
	
	
	/**
	 * ����һ����(��˳�������)�����ݸ����ķָ�����ָ����ü�Ȩƽ���������ָ���ָ�����������
	 * Ʃ��������int[20]��һ����������Ҫ����������С��int[10]���沢�ұ�������������Ǻϣ���ʱ���Ҳ��ü�Ȩƽ���ķ���
	 * �����һ�飬�о�����
	 * ���ڽ�500�����ݵĻ�����tag�ۼ�����С��100����
	 * @param treatNum invNum�����bp����ֵ
	 * @param binNum ����Ҫ���ɵķָ�Ŀ���
	 * @param startBias �����Ķ��ٿ�ʼ
	 * @param endBias ���յ�Ķ��ٽ���
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return
	 */
	private static double[] mySpline(short[] treatNum, int binNum,double startBias,double endBias,int type)
	{
		double rawlength=treatNum.length-startBias-endBias;
		double binlength=rawlength/binNum; //��ÿһ���ָ��ĳ��ȱ�׼��Ϊһ����ֵ����׼ΪinvNumΪ1 
		double[] resultBinValue=new double[binNum];
		for (int i = 1; i <= binNum; i++) 
		{
			//ĳ������treatNum���ߵ�һ��ֵ(�����߽�)���±�+1����Ϊ���鶼�Ǵ�0��ʼ��
			int leftTreatNum=(int) Math.ceil(binlength*(i-1)+startBias);
			//�����ֵ��Ȩ��
			double leftweight=leftTreatNum-binlength*(i-1)-startBias;
			////ĳ������treatNum���ұߵ�һ��ֵ(�������߽�)���±�+1����Ϊ���鶼�Ǵ�0��ʼ��
			int rightTreatNum=(int) Math.ceil(binlength*i+startBias);
			//���ұ�ֵ��Ȩ��
			int rightfloorNum=(int)Math.floor(binlength*i+startBias);
			double rightweight=binlength*(i)+startBias-rightfloorNum;
			
			//////////////////////������Ҷ˵㶼��һ�������ڣ���ô��Ȩƽ�������ֵ���ӺͶ����ڸ������ֵ/////////////////////////////////////////
			if (leftTreatNum>rightfloorNum) {
				resultBinValue[i-1]=treatNum[rightfloorNum];
				//////////���Ƿ�����ɾ//////////////////////
				if(leftTreatNum-rightfloorNum!=1)
					System.out.print("mySpline error");
				////////////////////////////////////////////////////////////
				continue;
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////
			//�м��м���ֵ
			int middleNum=rightfloorNum-leftTreatNum;
			
			double leftBinlength=-100000;
			if (leftTreatNum<1) 
				leftBinlength=0;
			else 
				leftBinlength=leftweight*treatNum[leftTreatNum-1];
			
			double rightBinlength=-100000;
			if (rightTreatNum>treatNum.length) 
				rightBinlength=0;
			else 
				rightBinlength=rightweight*treatNum[rightTreatNum-1];
			
			
			double treatNumInbinAll=leftBinlength+rightBinlength;
			double max=Math.max(leftBinlength, rightBinlength);

			for (int j = leftTreatNum; j < rightfloorNum; j++) {
				treatNumInbinAll=treatNumInbinAll+treatNum[j];
				max=Math.max(max,treatNum[j]);
			}
			//////////////////��������ѡ���Ȩƽ�������ֵ��Ӻ�////////////////////////////////////////////////////////////////
			double tmpValue;
			if (type==0)//��Ȩƽ��
				tmpValue=treatNumInbinAll/(leftweight+rightweight+middleNum);
			else if (type==1) //���ֵ
				tmpValue=max;
			else if (type==2)
				tmpValue=treatNumInbinAll;
			else //Ĭ�ϼ�Ȩƽ��
				tmpValue=treatNumInbinAll/(leftweight+rightweight+middleNum);
			//////////////////////////////////////////////////////////////////////////////////
			resultBinValue[i-1]=tmpValue;
		}
		return resultBinValue;
	}
	
	
	
	
	/**
	 * ����һ����(��˳�������)�����ݸ����ķָ�����ָ����ü�Ȩƽ���������ָ���ָ�����������
	 * Ʃ��������int[20]��һ����������Ҫ����������С��int[10]���沢�ұ�������������Ǻϣ���ʱ���Ҳ��ü�Ȩƽ���ķ���
	 * �����һ�飬�о�����
	 * ���ڽ�500�����ݵĻ�����tag�ۼ�����С��100����
	 * @param treatNum invNum�����bp����ֵ
	 * @param binNum ����Ҫ���ɵķָ�Ŀ���
	 * @param startBias �����Ķ��ٿ�ʼ
	 * @param endBias ���յ�Ķ��ٽ���
	 * @param type 0����Ȩƽ�� 1��ȡ���ֵ��2����Ȩ����ƽ��--Ҳ���ǼӺ�
	 * @return
	 */
	private static double[] mySpline(double[] treatNum, int binNum,double startBias,double endBias,int type)
	{
		double rawlength=treatNum.length-startBias-endBias;
		double binlength=rawlength/binNum; //��ÿһ���ָ��ĳ��ȱ�׼��Ϊһ����ֵ����׼ΪinvNumΪ1 
		double[] resultBinValue=new double[binNum];
		for (int i = 1; i <= binNum; i++) 
		{
			//ĳ������treatNum���ߵ�һ��ֵ(�����߽�)���±�+1����Ϊ���鶼�Ǵ�0��ʼ��
			int leftTreatNum=(int) Math.ceil(binlength*(i-1)+startBias);
			//�����ֵ��Ȩ��
			double leftweight=leftTreatNum-binlength*(i-1)-startBias;
			////ĳ������treatNum���ұߵ�һ��ֵ(�������߽�)���±�+1����Ϊ���鶼�Ǵ�0��ʼ��
			int rightTreatNum=(int) Math.ceil(binlength*i+startBias);
			//���ұ�ֵ��Ȩ��
			int rightfloorNum=(int)Math.floor(binlength*i+startBias);
			double rightweight=binlength*(i)+startBias-rightfloorNum;
			
			//////////////////////������Ҷ˵㶼��һ�������ڣ���ô��Ȩƽ�������ֵ���ӺͶ����ڸ������ֵ/////////////////////////////////////////
			if (leftTreatNum>rightfloorNum) {
				resultBinValue[i-1]=treatNum[rightfloorNum];
				//////////���Ƿ�����ɾ//////////////////////
				if(leftTreatNum-rightfloorNum!=1)
					System.out.print("mySpline error");
				////////////////////////////////////////////////////////////
				continue;
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////
			//�м��м���ֵ
			int middleNum=rightfloorNum-leftTreatNum;
			
			double leftBinlength=-100000;
			if (leftTreatNum<1) 
				leftBinlength=0;
			else 
				leftBinlength=leftweight*treatNum[leftTreatNum-1];
			
			double rightBinlength=-100000;
			if (rightTreatNum>treatNum.length) 
				rightBinlength=0;
			else 
				rightBinlength=rightweight*treatNum[rightTreatNum-1];
			
			
			double treatNumInbinAll=leftBinlength+rightBinlength;
			double max=Math.max(leftBinlength, rightBinlength);

			for (int j = leftTreatNum; j < rightfloorNum; j++) {
				treatNumInbinAll=treatNumInbinAll+treatNum[j];
				max=Math.max(max,treatNum[j]);
			}
			//////////////////��������ѡ���Ȩƽ�������ֵ��Ӻ�////////////////////////////////////////////////////////////////
			double tmpValue;
			if (type==0)//��Ȩƽ��
				tmpValue=treatNumInbinAll/(leftweight+rightweight+middleNum);
			else if (type==1) //���ֵ
				tmpValue=max;
			else if (type==2)
				tmpValue=treatNumInbinAll;
			else //Ĭ�ϼ�Ȩƽ��
				tmpValue=treatNumInbinAll/(leftweight+rightweight+middleNum);
			//////////////////////////////////////////////////////////////////////////////////
			resultBinValue[i-1]=tmpValue;
		}
		return resultBinValue;
	}
	
	
	
	
	
	
	/**
	 * ����Ⱦɫ�壬�������յ㣬���ظ�Ⱦɫ����tag���ܶȷֲ�
	 * @param chrID 
	 * @param startLoc ������꣬Ϊʵ�����
	 * @param endLoc ���յ�Ϊ-1ʱ����ֱ��Ⱦɫ��Ľ�β��
	 * @param binNum ���ָ�Ŀ���
	 * @return
	 */
	public static double[] getReadsDensity(String chrID,int startLoc,int endLoc,int binNum ) 
	{
		//���Ƚ�reads��׼��Ϊһ��400-500bp��Ĵ�飬ÿһ������Ӧ���Ǹ���������tags���������������������������ֵ
		//Ȼ�����ڴ������ͳ�ƣ�
		//��Ź�����һ�£������Ͽ����һ��tag��1.5����ʱ�������ȽϺ���
		int tagBinLength=(int)(tagLength*1.5);
		if (startLoc==0) 
			startLoc=1;
		if(endLoc==-1)
			endLoc=hashChrLength.get(chrID.trim().toLowerCase());
		
		
		Hashtable<String, short[]>aaaHashtable=hashChrBpReads;
		double[] tmpReadsNum= getRengeInfo(tagBinLength, chrID, startLoc, endLoc,1);
	/**	for (int i = 0; i < tmpReadsNum.length; i++) {
			if(tmpReadsNum[i]>1)
				System.out.println(tmpReadsNum[i]);
		}
		*/
		double[] resultTagDensityNum=mySpline(tmpReadsNum, binNum, 0, 0, 2);
		return resultTagDensityNum;
	}
	
	
	
	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻��ÿ��chr�ĳ���
	 * @param chrID
	 * @return
	 */
	public static int getChrLength(String chrID) 
	{
		return hashChrLength.get(chrID);
	}
	
	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻��������chr�ĳ���
	 * @param chrID
	 * @return int[]
	 * 0: ���chr����
	 * 1: �chr����
	 */
	public static int[] getThreshodChrLength()
	{
		int[] result=new int[2];
		result[0]=Integer.parseInt(lsChrLength.get(0)[1]);
		result[1]=Integer.parseInt(lsChrLength.get(lsChrLength.size()-1)[1]);
		return result;
	}
	
	
	
	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻������chr�ĳ�����Ϣ
	 * @param chrID
	 * @return ArrayList<String[]>
	 * 0: chrID
	 * 1: chr����
	 */
	public static ArrayList<String[]> getChrLengthInfo()
	{

		return lsChrLength;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
