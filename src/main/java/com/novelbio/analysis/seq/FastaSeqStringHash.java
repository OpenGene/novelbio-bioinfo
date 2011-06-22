package com.novelbio.analysis.seq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.base.dataOperate.TxtReadandWrite;




/**
 * ������������ȡfasta�ı�������Hash��key-������-Сд��value-������Ϣ
 * Ŀǰ�����н������о�̬����
 * ���ߣ��ڽ� 20090617
 */

public class FastaSeqStringHash {

	/**
	 * ��������Ϣ�����ϣ������
	 * ��ϣ��ļ���������������������ı��Сд���ΪСд
	 * ��ϣ���ֵ�����У������޿ո�
	 */
	public static Hashtable<String,SeqInfo> hashSeq;
	
	/**
	 * ���������ư�˳�����list
	 */
	public static ArrayList<String> lsSeqName;
	
	/**
	 * ���򻥲���ϣ�� 
	 */
	private static HashMap<Character, Character> compmap=new HashMap<Character, Character>();//��������ϣ��

	 /**
	  * ��������չ�ϣ��ֵ
	  * Ŀǰ��A-T�� G-C��N-N �Ķ�Ӧ��ϵ�������˴�Сд�Ķ�Ӧ��
	  * ��������Ҫ����µ� 
	  */
	private static void SetCompMap() 
	{
		 compmap.put(Character.valueOf('A'), Character.valueOf('T'));
		 compmap.put(Character.valueOf('a'), Character.valueOf('T'));
		 compmap.put(Character.valueOf('T'), Character.valueOf('A'));
		 compmap.put(Character.valueOf('t'), Character.valueOf('A'));
		 compmap.put(Character.valueOf('G'), Character.valueOf('C'));
		 compmap.put(Character.valueOf('g'), Character.valueOf('C'));
		 compmap.put(Character.valueOf('C'), Character.valueOf('G'));
		 compmap.put(Character.valueOf('c'), Character.valueOf('G'));
		 compmap.put(Character.valueOf(' '), Character.valueOf(' '));
		 compmap.put(Character.valueOf('N'), Character.valueOf('N'));
		 compmap.put(Character.valueOf('n'), Character.valueOf('N'));	
	}
	
	
	
	/**
	 * ��ȡ�����ļ��������б�����Seqhash��ϣ��<br/>
	 * ��ȡ��Ϻ�����<br/>
	 * һ��listSeqName����������List<br/>
	 * һ��Seqhash��������--����HashTable<br/>
	 * ͬʱ����������һ��ͬ���Ĺ�ϣ��
	 * @param seqfilename
	 * @param CaseChange �������Ƿ�Ҫ�ı��Сд,true����ΪСд��false���Ĵ�Сд
	 * @param regx ��Ҫ��ȡ��fasta��ʽ��������������ʽ��""Ϊȫ�����֡����ûץ������ȫ��������Ϊ������
	 * @param append ������ͬ�������еĴ���true����������������У����ڵڶ������ֺ����"<"��Ϊ���
	 * false����������������У����ó�������ȥ�滻�̵�����
	 * @return
	 * @throws Exception 
	 */
	public static Hashtable<String,SeqInfo> readfile(String seqfilename,boolean CaseChange, String regx,boolean append) throws Exception
	{
		Pattern pattern =Pattern.compile(regx, Pattern.CASE_INSENSITIVE);  //flags - ƥ���־�����ܰ��� CASE_INSENSITIVE��MULTILINE��DOTALL��UNICODE_CASE�� CANON_EQ��UNIX_LINES��LITERAL �� COMMENTS ��λ����  // CASE_INSENSITIVE,��Сд�����У�MULTILINE ����
	    Matcher matcher;//matcher.groupCount() ���ش�ƥ����ģʽ�еĲ���������
	    
		
		 hashSeq=new Hashtable<String,SeqInfo>();//��list�����洢Ⱦɫ��
		 TxtReadandWrite txtSeqFile=new TxtReadandWrite();
		 txtSeqFile.setParameter(seqfilename, false, true);
	     StringBuilder SeqStringBuilder=new StringBuilder();
	     String content="";
	     BufferedReader reader=txtSeqFile.readfile();//open gff file
	     SeqInfo Seq=null;
	     String tmpSeqID="";
	     lsSeqName=new ArrayList<String>();
	     while ((content=reader.readLine())!=null)
	     {
	    	 if(content.trim().startsWith(">"))//������һ������ʱ��������������
	    	 {
	    		 if (Seq!=null)
	    		 {
	    			 Seq.SeqSequence=SeqStringBuilder.toString();
	    			 SeqStringBuilder=new StringBuilder();//���
	    			 SeqInfo tmpSeq=hashSeq.get(Seq.SeqName);//���Ƿ���ͬ�������г���
	    			 //���û��ͬ�����У�ֱ��װ��hash��
	    			 if (tmpSeq==null)
	    			 {
	    				 hashSeq.put(Seq.SeqName, Seq);
	    				 lsSeqName.add(Seq.SeqName);
	    			 }
	    			 else
	    			 {//������ͬ�������еĴ���true����������������У����ڵڶ������ֺ����"<"��Ϊ���
	    				 if (append) 
	    				 { //����������"<"ֱ��hash��û����������Ϊֹ��Ȼ��װ��hash��
	    					 while (hashSeq.containsKey(Seq.SeqName))
	    					 {
	 							Seq.SeqName=Seq.SeqName+"<";
	    					 }
	    					 hashSeq.put(Seq.SeqName, Seq);
	    					 lsSeqName.add(Seq.SeqName);
	    				 }
	    				 else 
	    				 {
							if (tmpSeq.SeqSequence.length()<Seq.SeqSequence.length()) 
							{
								hashSeq.put(Seq.SeqName, Seq);
								//��Ϊ�Ѿ�����ͬ�������У����� lsSeqName �в���Ҫ����µ�����
							}
							else 
								continue;
						}
	    			 }
	    		 }
	    		 Seq=new SeqInfo();
	    		 String tmpSeqName="";
	    		
	    		 ////////////////�Ƿ�ı��������ֵĴ�Сд//////////////////////////////////////////////
	    		 if(CaseChange)
	    			 tmpSeqName=content.trim().substring(1).trim().toLowerCase();//substring(1)��ȥ��>���ţ�Ȼ��ͳͳ�ĳ�Сд
	    		 else 
	    			 tmpSeqName=content.trim().substring(1).trim();//substring(1)��ȥ��>���ţ������Сд
	    		 /////////////////��������ʽץȡ�������е��ض��ַ�////////////////////////////////////////////////
	    		 if (regx.trim().equals("")) {
	    			 Seq.SeqName=tmpSeqName;
	    		 }
	    		 else {
	    			 matcher=pattern.matcher(tmpSeqName);
	    			 if (matcher.find()) {
	    				 Seq.SeqName=matcher.group();
	    			 }
	    			 else {
	    				 System.out.println(tmpSeqName);
	    				 Seq.SeqName=tmpSeqName;
	    			 }
	    		 }
	    		 
				continue;
		   	}
			SeqStringBuilder.append(content.replace(" ",""));
		  }
	     ///////////�뿪ѭ��������һ���ܽ�/////////////////////
		 Seq.SeqSequence=SeqStringBuilder.toString();
		 SeqInfo tmpSeq=hashSeq.get(Seq.SeqName);//���Ƿ���ͬ�������г���
		 //���û��ͬ�����У�ֱ��װ��hash��
		 if (tmpSeq==null)
		 {
			 hashSeq.put(Seq.SeqName, Seq);
			 lsSeqName.add(Seq.SeqName);
		 }
		 else
		 {//������ͬ�������еĴ���true����������������У����ڵڶ������ֺ����"<"��Ϊ���
			 if (append) 
			 { //����������"<"ֱ��hash��û����������Ϊֹ��Ȼ��װ��hash��
				 while (hashSeq.containsKey(Seq.SeqName))
				 {
						Seq.SeqName=Seq.SeqName+"<";
				 }
				 hashSeq.put(Seq.SeqName, Seq);
				 lsSeqName.add(Seq.SeqName);
			 }
			 else 
			 {
				if (tmpSeq.SeqSequence.length()<Seq.SeqSequence.length()) 
				{
					hashSeq.put(Seq.SeqName, Seq);
					//��Ϊ�Ѿ�����ͬ�������У����� lsSeqName �в���Ҫ����µ�����
				}
			}
		 }
	return hashSeq;
	}	
	

	/**
	 * ����������Ϣ��������,������
	 * ��������
	 * @param SeqID ��������
	 * @param chr ���в���֮�������������ڹ�ϣ���в��Ҿ���ĳ������
	 * @param cisseq���������򣬵������о���true
	 */
	public static String getsequence(String SeqID,boolean cisseq) 
	{
		String sequence=hashSeq.get(SeqID).SeqSequence;
	    if (cisseq)
	    {
	    	return sequence;
	    }
	    else 
	    {
		  return reservecomplement(sequence);	
		}
	}
	
	/**
	 * ��������������Ϣ��������-���������յ�
	 * ��������
	 * @param hashSeq ���еĹ�ϣ����Ϊ�������ƣ�ֵΪ��������
	 * @param chr ���в���֮�������������ڹ�ϣ���в��Ҿ���ĳ������
	 * @param startlocation �������
	 * @param endlocation �����յ�
	 * @param cisseq���������򣬵������о���true
	 */
	public static String getsequence(String SeqID, int startlocation, int endlocation,boolean cisseq) 
	{
		String sequence=getsequence(SeqID, startlocation, endlocation);
	    if (cisseq)
	    {
	    	return sequence;
	    }
	    else 
	    {
		  return reservecomplement(sequence);	
		}
	}

	
	/**
	 * ����������
	 * �����������꣬�����յ�
	 * ��������
	 */
	public static String getsequence(String seqID, int startlocation, int endlocation) 
	{ 
		SeqInfo targetChr=hashSeq.get(seqID);
        if (targetChr==null)
        	{
        	return "�ײ����и�ʽ��������޸�����";
        	}
	   /**
	    * ���λ�㳬���˷�Χ����ô����λ��
	    */
        int length=targetChr.SeqSequence.length();
	   if (startlocation<1||startlocation>=length||endlocation<1||endlocation>=length)
	   {
		   return "�����������";
	   }
	   
	   if(endlocation<=startlocation)
	   {
		   return "�������";
	   }
	   if(endlocation-startlocation>20000)
	   {
		   return "�����ȡ20000bp";
	   }

	 return   targetChr.SeqSequence.substring(startlocation-1, endlocation);//substring���������ҵ�������
	}
	
	/**
	 * �������У��������ձ�
	 * ��÷��򻥲�����
	 */
	public static String reservecomplement(String sequence)
	{
		if(compmap==null)
    	{
    		SetCompMap();
    	}
		
		StringBuilder recomseq=new StringBuilder();
		int length=sequence.length();
		Character base;
		for(int i=length-1;i>=0;i--)
		{
			base=compmap.get(sequence.charAt(i));
			if (base!=null)
			{
			recomseq.append(compmap.get(sequence.charAt(i)));
			}
			else 
			{
			 return "����δ֪��� "+	sequence.charAt(i);
			}
		}	
		return recomseq.toString();
	}	
}



