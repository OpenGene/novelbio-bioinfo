package com.novelBio.base.detailWebFetch;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelBio.base.dataOperate.WebFetch;


public class TairBlast extends WebFetch
{
   
   /**
    * tair网站blast的网址
    */
   private String TairUrl="http://www.arabidopsis.org/cgi-bin/Blast/TAIRblast.pl";
 
   /**
    * post的内容
    */
   private String[][] postData=new String[23][2];
   
   /**
    * blast的结果流文件
    */
   private BufferedReader tairReader;
   
   /**
    * 结果的e-value，默认小于-10
    */
   public int evalue=-10;
   
   /**
    * 采集的At编号数量,默认为3
    */
   public int Atcount=3;
   
   
   /**
    * post的blast延迟时间，默认为3秒
    */
   public int blastdelaytime=3;
   
   
   public TairBlast()
   {
	  
   }
   /**
    * 提供水稻序列，上Tair网站blast<br/>
    * 查找evalue小于阈值的基因<br/>
    * 读取网页有错误就抛出<br/>
    * @throws IOException 
    * 返回数组,数组维数由Atcount决定，默认为3，即为保存3个同源拟南芥基因<br/>
	* 获得的At编号以及evalue，string数组保存<br/>
	* 0：At编号<br/>
	* 1：evalue<br/>
	* 如果[0][0]号为wrong 说明本网页读取有问题<br/>
    */
   public String[][] TairBlastPost(String seq) throws IOException
	{
       postSleepTime=blastdelaytime;	
	   GetUrl(TairUrl);//获得TIGRpost网址
		/**
		 * 指定post内容
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
    * 给出tair的buffer流文件，查找evalue小于阈值的基因
    * 读取有错误就抛出
    * @throws IOException 
    * 返回数组
	* 获得的At编号以及evalue，string数组保存
	* 0：At编号
	* 1：evalue
	* 如果[0][0]号为wrong 说明本网页读取有问题
    */
   private String[][] getATlink(BufferedReader tairReader) throws IOException
   {
	   String content="";
	   
	   /**
	    * 正则表达式抓取AtID
	    */
	   Pattern AtPat= Pattern.compile("At\\dg\\d{5}",  Pattern.CASE_INSENSITIVE);
	   Matcher AtMat;
	   
	   /**
	    * 正则表达式抓取e-Value
	    */
	   Pattern eValuePat= Pattern.compile("(?<=</a>   \\d{0,1}e)-\\d{1,3}",  Pattern.CASE_INSENSITIVE);
	   Matcher eValueMat;
	   
	   /**
	    * 查找的At基因的数目
	    */
	   int atNum=0;
	   
	   /**
	    * 获得的At编号以及evalue，string数组保存
	    * 前一个 At编号
	    * 后一个 evalue
	    */
	   String[][] AtIDevalue=new String[Atcount][2];
	   
	   boolean flag=false;//是否正常进行blast的标志
		   while((content=tairReader.readLine())!=null)
		   {
			   /**
			    * 判断是否正确的读取了网页
			    */
			   if(content.contains("Searching"))
			   {
				   flag=true;
			   }
			   /**
			    * flag标记为true
			    * 找到小于Atcount个at编号
			    * 然后找到At编号
			    * 找到e-value
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
			    * 读完就跳出
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
				   AtIDevalue[0][0]="没有同源性高的基因";
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
