package com.novelBio.base.detailWebFetch;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelBio.base.dataOperate.WebFetch;


public class TairBlast extends WebFetch
{
   
   /**
    * tair��վblast����ַ
    */
   private String TairUrl="http://www.arabidopsis.org/cgi-bin/Blast/TAIRblast.pl";
 
   /**
    * post������
    */
   private String[][] postData=new String[23][2];
   
   /**
    * blast�Ľ�����ļ�
    */
   private BufferedReader tairReader;
   
   /**
    * �����e-value��Ĭ��С��-10
    */
   public int evalue=-10;
   
   /**
    * �ɼ���At�������,Ĭ��Ϊ3
    */
   public int Atcount=3;
   
   
   /**
    * post��blast�ӳ�ʱ�䣬Ĭ��Ϊ3��
    */
   public int blastdelaytime=3;
   
   
   public TairBlast()
   {
	  
   }
   /**
    * �ṩˮ�����У���Tair��վblast<br/>
    * ����evalueС����ֵ�Ļ���<br/>
    * ��ȡ��ҳ�д�����׳�<br/>
    * @throws IOException 
    * ��������,����ά����Atcount������Ĭ��Ϊ3����Ϊ����3��ͬԴ���Ͻ����<br/>
	* ��õ�At����Լ�evalue��string���鱣��<br/>
	* 0��At���<br/>
	* 1��evalue<br/>
	* ���[0][0]��Ϊwrong ˵������ҳ��ȡ������<br/>
    */
   public String[][] TairBlastPost(String seq) throws IOException
	{
       postSleepTime=blastdelaytime;	
	   GetUrl(TairUrl);//���TIGRpost��ַ
		/**
		 * ָ��post����
		 */
		postData[0][0]="Algorithm"; postData[0][1]="blastp";
		postData[1][0]="BlastTargetSet";postData[1][1]="ATH1_pep";
		postData[2][0]="textbox";postData[2][1]="seq";
		postData[3][0]="QueryText";postData[3][1]=seq;
		postData[4][0]="upl-file";         postData[4][1]="";
		postData[5][0]="QueryFilter";postData[5][1]="T";
		postData[6][0]="Matrix";postData[6][1]="Blosum62";
		postData[7][0]="MaxScores";postData[7][1]="100";
		postData[8][0]="Expectation";postData[8][1]="10";
		postData[9][0]="MaxAlignments";postData[9][1]="50";
		postData[10][0]="NucleicMismatch";postData[10][1]="-3";
		postData[11][0]="GappedAlignment";postData[11][1]="T";
		postData[12][0]="NucleicMatch";postData[12][1]="1";
		postData[13][0]="OpenPenalty";postData[13][1]="0 (use default)";
		postData[14][0]="ExtensionThreshold";postData[14][1]="0 (use default)";
		postData[15][0]="ExtendPenalty";postData[15][1]="0 (use default)";
		postData[16][0]="WordSize";postData[16][1]="0 (use default)";
		postData[17][0]="QueryGeneticCode";postData[17][1]="1";
		postData[18][0]="Comment";postData[18][1]="optional, will be added to output for your use";
		postData[19][0]="ReplyTo";postData[19][1]="";
		postData[20][0]="ReplyVia";postData[20][1]="BROWSER";
		postData[21][0]="ReplyFormat";postData[21][1]="HTML";
		postData[22][0]="PageType";postData[22][1]="JavaScr";

		GetPostContent(postData);
		tairReader= PostFetch();
		
		String[][] atidandvalue= getATlink(tairReader);
      return  atidandvalue;
	}
   
   
   /**
    * ����tair��buffer���ļ�������evalueС����ֵ�Ļ���
    * ��ȡ�д�����׳�
    * @throws IOException 
    * ��������
	* ��õ�At����Լ�evalue��string���鱣��
	* 0��At���
	* 1��evalue
	* ���[0][0]��Ϊwrong ˵������ҳ��ȡ������
    */
   private String[][] getATlink(BufferedReader tairReader) throws IOException
   {
	   String content="";
	   
	   /**
	    * ������ʽץȡAtID
	    */
	   Pattern AtPat= Pattern.compile("At\\dg\\d{5}",  Pattern.CASE_INSENSITIVE);
	   Matcher AtMat;
	   
	   /**
	    * ������ʽץȡe-Value
	    */
	   Pattern eValuePat= Pattern.compile("(?<=</a>   \\d{0,1}e)-\\d{1,3}",  Pattern.CASE_INSENSITIVE);
	   Matcher eValueMat;
	   
	   /**
	    * ���ҵ�At�������Ŀ
	    */
	   int atNum=0;
	   
	   /**
	    * ��õ�At����Լ�evalue��string���鱣��
	    * ǰһ�� At���
	    * ��һ�� evalue
	    */
	   String[][] AtIDevalue=new String[Atcount][2];
	   
	   boolean flag=false;//�Ƿ���������blast�ı�־
		   while((content=tairReader.readLine())!=null)
		   {
			   /**
			    * �ж��Ƿ���ȷ�Ķ�ȡ����ҳ
			    */
			   if(content.contains("Searching"))
			   {
				   flag=true;
			   }
			   /**
			    * flag���Ϊtrue
			    * �ҵ�С��Atcount��at���
			    * Ȼ���ҵ�At���
			    * �ҵ�e-value
			    */
			   if(flag && atNum<Atcount && (AtMat=AtPat.matcher(content)).find() && (eValueMat= eValuePat.matcher(content)).find() )
			   {
				 if (Integer.parseInt(eValueMat.group())<evalue)
				 {
					 AtIDevalue[atNum][0]=AtMat.group();
					 AtIDevalue[atNum][1]=eValueMat.group();
					 atNum++;
				 }
			   }
			   /**
			    * ���������
			    */
			   if(content.contains("><a name=")||content.contains("Posted date"))
			   {
				   break;
			   }
		   }
		   if(flag)
		   {
			   if(AtIDevalue[0][0]==null||!AtIDevalue[0][0].contains("AT"))
			   {
				   AtIDevalue[0][0]="û��ͬԴ�ԸߵĻ���";
			   }
			   System.out.println( AtIDevalue[0][0]+AtIDevalue[1][0]+AtIDevalue[2][0]);
			   return AtIDevalue;
		   }
		   else 
		   {
			AtIDevalue[0][0]="wrong";
			
			return AtIDevalue;
		   }
		  
   }
   
   public void close()
   {
	  closeall();
   }
   
   
   
   
   
   
   
   
   
   
}
