package com.novelBio.base.genome.gffOperate;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelBio.base.dataOperate.TxtReadandWrite;





/**
 * ���Gff�Ļ���������Ϣ,�������ʵ��������ʹ��<br/>
 * ����Gff�ļ��������������ϣ���һ��list��,
 * �ṹ���£�<br/>
 * 1.hash��ChrID��--ChrList--GffDetail(GffDetail��,ʵ����GffDetailGene����)<br/>
 *   ����ChrIDΪСд������Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID
 * chr��ʽ��ȫ��Сд chr1,chr2,chr11<br/>
 * 
 * 2.hash��LOCID��--GffDetail������LOCID�������Ļ����� <br/>
 * 
 * 3.list��LOCID��--LOCList����˳�򱣴�LOCID<br/>
 * 
 * ÿ�����������յ��CDS������յ㱣����GffDetailList����<br/>
 */
public class CopyOfGffHashPlantGene extends GffHash{
	


	

	/**
	 * �������ֵ����򣬿��Ըĳ�ʶ�������������,���������Ͻ棬Ĭ��  "AT\\w{1}G\\d{5}"
	 * ˮ���� "LOC_Os\\d{2}g\\d{5}";
	 */
	public String GeneName="AT\\w{1}G\\d{5}";
	
	/**
	 * �ɱ����mRNA������ˮ���ǣ�"(?<=LOC_Os\\d{2}g\\d{5}\\.)\\d";��Ĭ�����Ͻ�" (?<=AT\\w{1}G\\d{5}\\.)\\d"
	 */
	public String splitmRNA="(?<=AT\\w{1}G\\d{5}\\.)\\d";
	
	/**
	 * ��ײ��ȡgff�ķ�����������ֻ�ܶ�ȡUCSCknown gene<br>
	 * ����Gff�ļ��������������ϣ���һ��list��<br/>
	 * �ṹ���£�<br/>
     * ����Gff�ļ��������������ϣ���һ��list��, �ṹ���£�<br>
     * <b>1.Chrhash</b><br>
     * ��ChrID��--ChrList-- GeneInforList(GffDetail��)
     * ����ChrIDΪСд������Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID, chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
    * ����LOCID����������Ŀ��ţ���UCSCkonwn gene����û��ת¼��һ˵��
	 * ֻ������LOCID����һ����������������ֻ�ܹ�������ͬ��LOCIDָ��ͬһ��GffdetailUCSCgene
     *  <b>3.LOCIDList</b><br>
     * ��LOCID��--LOCIDList����˳�򱣴�LOCID,���ﲻ���Ƕ��ת¼����ÿһ��ת¼������һ��������LOCID <br>
     * <b>4. LOCChrHashIDList </b><br>
     *   LOCChrHashIDList�б���LOCID����������Ŀ���,��Chrhash�������һ�£���ͬһ����Ķ��ת¼������һ�� NM_XXXX/NM_XXXX...<br>
	 * @throws Exception 
	 */
   public  Hashtable<String, ArrayList<GffDetail>>  ReadGffarray(String gfffilename) throws Exception
   {

		//ʵ�����ĸ���
		locHashtable =new Hashtable<String, GffDetail>();//�洢ÿ��LOCID���������Ϣ�Ķ��ձ�
		Chrhash=new Hashtable<String, ArrayList<GffDetail>>();//һ����ϣ�����洢ÿ��Ⱦɫ��
		LOCIDList=new ArrayList<String>();//˳��洢ÿ������ţ��������������ȡ��������
		LOCChrHashIDList=new ArrayList<String>();
		
	   TxtReadandWrite txtgff=new TxtReadandWrite();
	   txtgff.setParameter(gfffilename, false,true);
	   BufferedReader reader=txtgff.readfile();//open gff file
	   
	   ArrayList<GffDetail> LOCList = null;//˳��洢ÿ��loc�ľ�����Ϣ��һ��Ⱦɫ��һ��LOCList�����װ��Chrhash����
	   //��������
	   Pattern genepattern =Pattern.compile(GeneName, Pattern.CASE_INSENSITIVE);//to catch the LOC
	   Matcher genematcher;
       
	   //mRNA�ɱ���ӵ����
	   Pattern mRNApattern =Pattern.compile(splitmRNA, Pattern.CASE_INSENSITIVE);//to catch the LOC
	   Matcher mRNAmatcher;
	   String content="";
	   String chrnametmpString=""; //Ⱦɫ�����ʱ����
	   while((content=reader.readLine())!=null)//������β
	   {
		   if(content.charAt(0)=='#')
		   {
			   continue;
		   }
		   String[] ss=content.split("\t");//����tab�ֿ�
		   chrnametmpString=ss[0].toLowerCase();//Сд��chrID
		 //�µ�Ⱦɫ��
			if (!Chrhash.containsKey(chrnametmpString)) //�µ�Ⱦɫ��
			{
				if(LOCList!=null)//����Ѿ�������LOCList��Ҳ����ǰһ��LOCList����ô�ض̲�װ��LOCChrHashIDList
				{
					LOCList.trimToSize();
					 //��peak����˳��װ��LOCIDList
					   for (GffDetail gffDetail : LOCList) {
						   LOCChrHashIDList.add(gffDetail.locString);
					   }
				}
				LOCList=new ArrayList<GffDetail>();//�½�һ��LOCList������Chrhash
				Chrhash.put(chrnametmpString, LOCList);
			}
			
		   /**
		    * ����ȡ��geneʱ�����Ƕ�����һ���µĻ�����ô������������㣬�յ��ÿ��CDS�ĳ��ȶ�����list������
		    */
		   if (ss[2].equals("gene")) //when read the # and the line contains gene, it means the new LOC
       	   {
      		    /**
      		     * ÿ������һ���µ�Chr����ô�ͽ����Chr�����ϣ��
       		     * chr��ʽ��ȫ��Сд chr1,chr2,chr11
      		     */
			  
			   genematcher = genepattern.matcher(content);//���һ�������
       		   if(genematcher.find())//�ҵ���
       		   {
       			   GffDetail LOC=new GffDetailUCSCgene();//�½�һ��������
       			   LOC.locString=genematcher.group(); 
       			   LOC.numberstart=Integer.parseInt(ss[3].toLowerCase());LOC.numberend=Integer.parseInt(ss[4]);//������ֹ
       			   LOC.ChrID=chrnametmpString;
       		      /**
       		       * �����������
       		       */
       		      if( ss[6].equals("+"))
       		    	  LOC.cis5to3=true;
       		      else if(ss[6].equals("-"))
       		    	  LOC.cis5to3=false;
       		      LOCList.add(LOC);//��ӽ���LOClist
       		      locHashtable.put(LOC.locString, LOC);//��ӽ���hash��LOCID��--GeneInforlist��ϣ��ȷ��������������ǵ���֮��Ĺ�ϵ    
       		      LOCIDList.add(LOC.locString);
       		   }
       	   }
      	   /**
       	    * ����ȡ��mRNAʱ������˵�ǿɱ����ʱ�����һ���µĿɱ����list
       	    * ������ô�Ӷ��Ǵӵ�һ��cds��ʼ�ӵ����һ��cds������Ļ����Ǵ�С�ӵ��󣬷�����ǴӴ�ӵ�С��
       	    */
		   else if (ss[2].equals("mRNA")) 
		   {
			   mRNAmatcher = mRNApattern.matcher(content);
			   GffDetailUCSCgene lastGffdetailUCSCgene =  (GffDetailUCSCgene) LOCList.get(LOCList.size()-1);
			   if(mRNAmatcher.find())
			   {
				   //ÿ����һ��mRNA�����һ���ɱ����,��Ҫ����ת��Ϊ����
				 
				   lastGffdetailUCSCgene.addSplitName(mRNAmatcher.group());
					//���һ��ת¼����Ȼ����Ӧ��Ϣ:
					//��һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region end,�ӵ����ʼ�Ǹ�ת¼����Exon������Ϣ
					lastGffdetailUCSCgene.addsplitlist();
					//����UCSC�������������һ���Ǳ����mRNA����ôcdsStart = cdsEnd = mRNAend
					int cdsStart = -100; int cdsEnd = -100; int mRNAstart = Integer.parseInt(ss[3]);  int mRNAend = Integer.parseInt(ss[4]); 
					
			   }
       	        while((content = reader.readLine())!=null
       	        		&&content.charAt(0)!='#'
       	        			&&!content.contains("three")
       	        			&&!content.contains("gene"))//������β���������������
       	        {
       	        	ss=content.split("\t");//����tab�ֿ�
       	        	if(ss[2].equals("CDS"))
       	        	{ 
       	        		if(LOCList.get(LOCList.size()-1).cis5to3)
       	        		{
       	        		 ((GffDetailGene) LOCList.get(LOCList.size()-1)).addcds(Integer.parseInt(ss[3]));//���������,��С�ӵ���
       	        		 ((GffDetailGene) LOCList.get(LOCList.size()-1)).addcds(Integer.parseInt(ss[4]));
       	        		}
       	        		else 
       	        		{
       	        			((GffDetailGene) LOCList.get(LOCList.size()-1)).addcds(Integer.parseInt(ss[4]));//���������,���żӣ��Ӵ�ӵ�С
       	        			((GffDetailGene) LOCList.get(LOCList.size()-1)).addcds(Integer.parseInt(ss[3]));
       	        		}
       	        	}
       	        	else 
       	        	{
       	        		continue;
       	        	}
       	        }
       	        if (content.contains("gene"))
    	        {
       	        	ss=content.split("\t");//����tab�ֿ�
    	         /**
    			     * ÿ������һ���µ�Chr����ô�ͽ����Chr�����ϣ��
    			     * chr��ʽ��ȫ��Сд chr1,chr2,chr11
    			     */
       	        	if(!chrnametmpString.equals(ss[0]))//ÿ������һ���µ�Chr����ô�ͽ����Chr�����ϣ��
    	  		   {
       	        		LOCList=new ArrayList<GffDetail>(); 
       	        		chrnametmpString=ss[0].toLowerCase();//�������¼����
       	        		Chrhash.put(chrnametmpString,LOCList);
	    	  		   }
       	        	genematcher = genepattern.matcher(content);
       	        	if(genematcher.find())
       	        	{
       	        		GffDetailGene LOC=new GffDetailGene();//�½�һ��������
       	        		LOC.locString=genematcher.group(); 
       	        		LOC.numberstart=Integer.parseInt(ss[3].toLowerCase());LOC.numberend=Integer.parseInt(ss[4]);//������ֹ
       	        		LOC.ChrID=chrnametmpString;
    			      
    			      /**
    			       * �����������
    			       */
    			      if( ss[6].equals("+"))
    			      {
    			    	  LOC.cis5to3=true;
    			      }
    			      else if(ss[6].equals("-"))
    		    	   {
    			    	  LOC.cis5to3=false;
    			       }
    			      LOCList.add(LOC);//��ӽ���LOClist
    			      locHashtable.put(LOC.locString, LOC);//��ӽ���hash��LOCID��--GeneInforlist��ϣ��ȷ��������������ǵ���֮��Ĺ�ϵ    
    			      LOCIDList.add(LOC.locString);
    			    }
    	        }
     	   }
       	}
   	return null;//�������LOCarray��Ϣ
   }
   

}



