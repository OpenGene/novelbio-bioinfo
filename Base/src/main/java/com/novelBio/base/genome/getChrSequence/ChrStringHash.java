package com.novelBio.base.genome.getChrSequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException; 
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.fileOperate.FileOperate;




/**
 * ����ר������װȾɫ�����Ϣ
 */
class ChrString {
	public String chrname;
	public String chrsequence;
}



/**
 * ����������Ⱦɫ������֣�����װ��Ⱦɫ���࣬��������Hash����ʽ����
 * Ŀǰ�����н������о�̬����
 * ͬʱ������ȡĳ��λ�õ�����
 * ����ȡ�����ظ�����
 * ���ߣ��ڽ� 20090617
 */
public class ChrStringHash {

 
	
	/**
	 * ��Ⱦɫ����Ϣ�����ϣ��,����RandomAccessFile���棬������
	 * ��ϣ���ļ���Ⱦɫ�����ƣ�����Сд����ʽ�磺chr1��chr2��chr10
	 * ��ϣ����ֵ��Ⱦɫ������У������޿ո�
	 */
	static HashMap<String, RandomAccessFile> hashChrSeqFile;
	
	/**
	 * ��Ⱦɫ����Ϣ�����ϣ��,����BufferedReader���棬������
	 * ��ϣ���ļ���Ⱦɫ�����ƣ�����Сд����ʽ�磺chr1��chr2��chr10
	 * ��ϣ����ֵ��Ⱦɫ������У������޿ո�
	 */
	static HashMap<String, BufferedReader> hashBufChrSeqFile;
	
	
	/**
	 * Seq�ļ��ڶ��еĳ��ȣ�Ҳ����ÿ�����еĳ���+1��1�ǻس�
	 * �����Ǽ���Seq�ļ���һ�ж���>ChrID,�ڶ��п�ʼ����Seq������Ϣ
	 * ����ÿһ�е����ж��ȳ�
	 */
	static int lengthRow=0;
	
	/**
	 * ����chrID��chrLength�Ķ�Ӧ��ϵ
	 */
	static Hashtable<String, Long> hashChrLength=new Hashtable<String, Long>();
	
	
	/**
	 *  ���������Ա�
	 */
	 private static HashMap<Character, Character> compMap;//��������ϣ��
	
	 /**
	  * ��������չ�ϣ����ֵ
	  * Ŀǰ��A-T�� G-C��N-N �Ķ�Ӧ��ϵ�������˴�Сд�Ķ�Ӧ��
	  * ��������Ҫ�����µ� 
	  */
	 private static void compmapFill() {
		 compMap=new HashMap<Character, Character>();//��������ϣ��
		 compMap.put(Character.valueOf('A'), Character.valueOf('T'));
		 compMap.put(Character.valueOf('a'), Character.valueOf('t'));
		 compMap.put(Character.valueOf('T'), Character.valueOf('A'));
		 compMap.put(Character.valueOf('t'), Character.valueOf('a'));
		 compMap.put(Character.valueOf('G'), Character.valueOf('C'));
		 compMap.put(Character.valueOf('g'), Character.valueOf('c'));
		 compMap.put(Character.valueOf('C'), Character.valueOf('G'));
		 compMap.put(Character.valueOf('c'), Character.valueOf('g'));
		 compMap.put(Character.valueOf(' '), Character.valueOf(' '));
		 compMap.put(Character.valueOf('N'), Character.valueOf('N'));
		 compMap.put(Character.valueOf('n'), Character.valueOf('n'));
	}
	 
	 /**
	  * ���Ӳ�̶�ȡȾɫ���ļ��ķ���
	  * ע��
	  * ����һ���ļ��У�����ļ������汣����ĳ�����ֵ�����Ⱦɫ��������Ϣ��<b>�ļ����������ν�Ӳ���"/"��"\\"</b>
	  * һ���ı�����һ��Ⱦɫ�壬��fasta��ʽ���棬ÿ���ı���">"��ͷ��Ȼ�������ÿ�й̶��ļ����(��UCSCΪ50����TIGRRiceΪ60��)
	  * �ı��ļ���(�����Ǻ�׺������Ȼû�к�׺��Ҳ��)Ӧ���Ǵ����ҵ�chrID
	  * ���������һ��Hashtable--chrID(String)---SeqFile(RandomAccessFile)��<br>
	  * ��һ��Hashtable--chrID(String)---SeqFile(BufferedReader)��<br>
	  * ����chrIDһֱΪСд
	  * 
	  * @param chrFilePath
	 * @throws IOException 
	 * @throws FileNotFoundException
	  */
	 public static void setChrFilePath(String chrFilePath) throws Exception
	 {
		 if (compMap==null) {
			 compmapFill();
		}
		 if (!chrFilePath.endsWith(File.separator)) {  
			 chrFilePath = chrFilePath + File.separator;  
		 }  
		ArrayList<String[]> chrFile=FileOperate.getFoldFileName(chrFilePath, "\\bchr\\w*", "*");
		hashChrSeqFile=new HashMap<String, RandomAccessFile>();
		hashBufChrSeqFile=new HashMap<String, BufferedReader>();

		for (int i = 0; i < chrFile.size(); i++) 
		{
			RandomAccessFile  chrRAseq=null;
			TxtReadandWrite txtChrTmp=new TxtReadandWrite();
			BufferedReader bufChrSeq=null;
			String[] chrFileName=chrFile.get(i);
			String fileNam="";
			
			if(chrFileName[1].equals(""))	
				fileNam=chrFilePath+chrFileName[0];
			else 
				fileNam=chrFilePath+chrFileName[0]+"."+chrFileName[1];
			
			chrRAseq=new RandomAccessFile(fileNam, "r"); 	
			txtChrTmp.setParameter(fileNam, false,true);
			bufChrSeq=txtChrTmp.readfile();
 
			if (i==0) //����ÿһ���ļ���ÿһ��Seq�����
			{
				chrRAseq.seek(0);
				chrRAseq.readLine();
				String seqRow=chrRAseq.readLine();
				lengthRow=seqRow.length();//ÿ�м������
			}
			hashChrSeqFile.put(chrFileName[0].toLowerCase(), chrRAseq);
			hashBufChrSeqFile.put(chrFileName[0].toLowerCase(), bufChrSeq);
		}
		getChrLength();
	}
	 
	 private static void getChrLength() throws IOException {
		 Iterator iter = hashChrSeqFile.entrySet().iterator();
		 ArrayList<String[]> lsResult=new ArrayList<String[]>();//��������
		 while (iter.hasNext()) 
		 {
		     Map.Entry entry = (Map.Entry) iter.next();
		     String chrID = (String) entry.getKey();
		     RandomAccessFile chrRAfile = (RandomAccessFile) entry.getValue();
		     //�趨��0λ
		     chrRAfile.seek(0);
		     //���ÿ��Ⱦɫ��ĳ��ȣ��ļ�����-��һ�е�
		 	String fastaID=chrRAfile.readLine();
			int lengthChrID=-1;
			if (fastaID.contains(">")) 
				lengthChrID=fastaID.length();//��һ�У���>�ŵĳ���
 
		     long lengthChrSeq=chrRAfile.length();
		     long tmpChrLength=(lengthChrSeq-lengthChrID-1)/(lengthRow+1)*lengthRow+(lengthChrSeq-lengthChrID-1)%(lengthRow+1);
		    hashChrLength.put(chrID, tmpChrLength);
		 }
	}
	 
	 
	 /**
	  * ���趨Chr�ļ��󣬿��Խ����г���������ļ�
	  * ����ļ�Ϊ  chrID(Сд)+��\t��+chrLength+����
	  * ����˳�����
	  * @param outFile ��������ļ���������ȫ��·��
	 * @throws IOException 
	  */
	 public static void saveChrLengthToFile(String outFile)  
	 {
		 Iterator iter = hashChrLength.entrySet().iterator();
		 ArrayList<String[]> lsResult=new ArrayList<String[]>();//��������
		 while (iter.hasNext()) 
		 {
			 String[] tmpResult=new String[2];
		     Map.Entry entry = (Map.Entry) iter.next();
		     String chrID = (String) entry.getKey();
		     long lengthChrSeq = (Long) entry.getValue();
		     tmpResult[0]=chrID;
		     tmpResult[1]=lengthChrSeq+"";
		     lsResult.add(tmpResult);    
		 }
		 TxtReadandWrite txtChrLength=new TxtReadandWrite();
		 txtChrLength.setParameter(outFile, true,false);
		 try { 	txtChrLength.ExcelWrite(lsResult, "\t", 1, 1); 	} catch (Exception e) { 	e.printStackTrace(); 	}
	 }
	 
	 
	 
	 
	 
		/**
		 * ����chrID,chrID���Զ�ת��ΪСд���Ͷ�ȡ������Լ��յ㣬���ض�ȡ������
		 * startNum=204;�ӵڼ��������ʼ��ȡ����1��ʼ������ע��234�Ļ���ʵ��Ϊ��234��ʼ��ȡ������substring����
		 * long endNum=254;//�����ڼ����������1��ʼ������ʵ�ʶ�����endNum�������
		 * ������ȡ����
		 * @throws IOException
		 */
		private static String getSeq(String chrID,long startlocation,long endlocation) throws IOException 
		{
			startlocation--;
			RandomAccessFile chrRASeqFile=hashChrSeqFile.get(chrID.toLowerCase());//�ж��ļ��Ƿ����
			if (chrRASeqFile==null)
			{
				return "�ײ�Ⱦɫ���ʽ��������޸�Ⱦɫ��";
			}
			
			
			int startrowBias=0;
			int endrowBias=0;
			//�趨��0λ
			
			
			chrRASeqFile.seek(0);
			String fastaID=chrRASeqFile.readLine();
			int lengthChrID=-1;
			if (fastaID.contains(">")) 
				lengthChrID=fastaID.length();//��һ�У���>�ŵĳ���

				
			
			
			long lengthChrSeq=chrRASeqFile.length();
			
			
			long rowstartNum=startlocation/lengthRow;
			startrowBias=(int) (startlocation%lengthRow);
			long rowendNum=endlocation/lengthRow;
			endrowBias=(int) (endlocation%lengthRow);
			//ʵ���������ļ��е����
			long startRealCod= (lengthChrID+1)  +  (lengthRow+1)  * rowstartNum+startrowBias;
			long endRealCod=(lengthChrID+1)  +  (lengthRow+1)  * rowendNum+endrowBias;
			/**
			 * ���λ�㳬���˷�Χ����ô����λ��
			 */
		        if (startlocation<1||startRealCod>=lengthChrSeq||endlocation<1||endRealCod>=lengthChrSeq)
		        {
		        	return "Ⱦɫ���������";
		        }
			   
		        if(endlocation<=startlocation)
		        {
		        	return "�������";
		        }
		        if(endlocation-startlocation>20000)
		        {
		        	return "�����ȡ20000bp";
		        }


			//����Ŀ������
			StringBuilder sequence=new StringBuilder();
			chrRASeqFile.seek(startRealCod);
			
			if (rowendNum-rowstartNum==0) 
			{
				String seqResult=chrRASeqFile.readLine();
				seqResult=seqResult.substring(0, endrowBias-startrowBias);
				return seqResult;
			}
			else
			{
				for (int i = 0; i < rowendNum-rowstartNum; i++) 
				{
					sequence.append(chrRASeqFile.readLine());
				}
				String endline=chrRASeqFile.readLine();
				endline=endline.substring(0, endrowBias);
				sequence.append(endline);
				String seqResult=sequence.toString();
				return seqResult;
			}
		}
	 
	 
	
	/**
	 * 	 * ����Ⱦɫ��list��Ϣ
	 * �������������Լ��Ƿ�Ϊ���򻥲�,����ChrIDΪ chr1��chr2��chr10����
	 * ��������
	 * @param cisseq ������
	 * @param chrID Ŀ��Ⱦɫ�����ƣ������ڹ�ϣ���в��Ҿ���ĳ��Ⱦɫ��
	 * @param startlocation �������
	 * @param endlocation �����յ�
	 * @return
	 */
	public static String getSeq(boolean cisseq,String chrID, long startlocation, long endlocation) 
	{
		String sequence=null;
		try { sequence = getSeq(chrID, startlocation, endlocation); } catch (IOException e) {e.printStackTrace();}
	    if (cisseq)
	    {
	    	return sequence;
	    }
	    else 
	    {
	    	return resCompSeq(sequence, compMap);	
		}
	}
	
	/**
	 * �������У��������ձ�
	 * ��÷��򻥲�����
	 */
	public static String resCompSeq(String sequence,HashMap<Character, Character> complementmap)
	{
		StringBuilder recomseq=new StringBuilder();
		int length=sequence.length();
		Character base;
		for(int i=length-1;i>=0;i--)
		{
			base=complementmap.get(sequence.charAt(i));
			if (base!=null)
			{
			recomseq.append(complementmap.get(sequence.charAt(i)));
			}
			else 
			{
			 return "����δ֪��� "+	sequence.charAt(i);
			}
		}	
		return recomseq.toString();
	}
	
	/**
	 * ���ÿ��Ⱦɫ���Ӧ��bufferedreader�࣬�����ͷ��ȡ
	 * @param chrID
	 * @return
	 */
	public static BufferedReader getBufChrSeq(String chrID)
	{
		return hashBufChrSeqFile.get(chrID.toLowerCase());
	}
	
	public static long getEffGenomeSize() throws IOException {
		long effGenomSize = 0;
		for(Map.Entry<String,BufferedReader> entry:hashBufChrSeqFile.entrySet())
		{
			String chrID = entry.getKey();
			BufferedReader chrReader = entry.getValue();
			String content = "";
			while ((content = chrReader.readLine()) != null) {
				if (content.startsWith(">")) {
					continue;
				}
				String tmp = content.trim().replace("N", "").replace("n", "");
				effGenomSize = effGenomSize + tmp.length();
			}
		}
		
		
		
		
		return effGenomSize;
	}
	
	/**
	 * ��Chr�ļ�ÿһ�еĳ���
	 * @return
	 */
	public static int getChrLineLength()
	{
		return lengthRow;
	}
	/**
	 * ����chrID��chrLength�Ķ�Ӧ��ϵ
	 * @return
	 */
	public static Hashtable<String, Long> getHashChrLength()
	{
		return hashChrLength;
	}
	
	/**
	 * �ڶ�ȡchr�����ļ��󣬿���ͨ���˻������chr�ĳ�����Ϣ
	 * @param chrID
	 * @return ArrayList<String[]>
	 * 0: chrID
	 * 1: chr����
	 * ���Ұ���chr���ȴ�С��������
	 */
	public static ArrayList<String[]> getChrLengthInfo()
	{
		 Iterator iter = hashChrLength.entrySet().iterator();
		 ArrayList<String[]> lsResult=new ArrayList<String[]>();//��������
		 while (iter.hasNext()) 
		 {
			 String[] tmpResult=new String[2];
		     Map.Entry entry = (Map.Entry) iter.next();
		     String chrID = (String) entry.getKey();
		     long lengthChrSeq = (Long) entry.getValue();
		     tmpResult[0]=chrID;
		     tmpResult[1]=lengthChrSeq+"";
		     lsResult.add(tmpResult);    
		 }
		 ////////////////////////////��lsChrLength����chrLen��С�����������/////////////////////////////////////////////////////////////////////////////
		  Collections.sort(lsResult,new Comparator<String[]>(){
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
		return lsResult;
	}
	
	/**
	 * ָ���Ⱦɫ���ֵ�����ذ�����ÿ��Ⱦɫ����Ӧֵ��Ⱦɫ�����������,resolution��int[resolution]�������ڻ�ͼ
	 * ��ôresolution���Ƿ��ص�int[]�ĳ���
	 * @param chrID
	 * @param maxresolution
	 */
	public static int[] getChrRes(String chrID,int maxresolution) throws Exception
	{
		 ArrayList<String[]> chrLengthArrayList= getChrLengthInfo();
		 int binLen=Integer.parseInt( chrLengthArrayList.get(chrLengthArrayList.size()-1)[1] )/maxresolution;
		 int resolution=(int) (hashChrLength.get(chrID)/binLen);

		Long chrLength =hashChrLength.get(chrID.toLowerCase());
		double binLength=(double)chrLength/resolution;
		int[] chrLengtharray=new int[resolution];
		for (int i = 0; i < resolution; i++) {
			chrLengtharray[i]=(int) ((i+1)*binLength);
		}
		return chrLengtharray;
	}
	
	
	
	
}