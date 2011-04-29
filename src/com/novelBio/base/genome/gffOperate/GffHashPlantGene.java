package com.novelBio.base.genome.gffOperate;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
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
public class GffHashPlantGene extends GffHashGene{
	


	

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
	   boolean UTR5start = false; boolean UTR3start = false; boolean UTR5end = false; boolean UTR3end = false;
	   boolean CDSstart = false; boolean CDSend = false; boolean mRNAsplit = false;//�Ƿ������һ��mRNA
	   int cdsStart = -100; int cdsEnd = -100; int mRNAstart = -100;  int mRNAend = -100; 
	   GffDetailUCSCgene gffDetailLOC= null;
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
			   if (mRNAsplit) {
				   //����һ��mRNA����Ϣװ��
				   if (cdsStart < 0 && cdsEnd <0) {
					   cdsStart = mRNAend;
					   cdsEnd = mRNAend;
				   }
				   gffDetailLOC.addExon(0,cdsEnd,false); 
				   gffDetailLOC.addExon(0,cdsStart,false);
				   if (cdsStart <0 || cdsEnd<0 || cdsStart > cdsEnd) {
					   System.out.println("GffHashPlantGeneError: �ļ�  "+gfffilename+"  �����������������⣬cdsStart��cdsEnd����  " +gffDetailLOC.locString);
				   }
				   mRNAsplit = false;//ȫ�µĻ��򣬽����λfalse
			   }
			   /**
			    * ÿ������һ���µ�Chr����ô�ͽ����Chr�����ϣ��
			    * chr��ʽ��ȫ��Сд chr1,chr2,chr11
			    */
			   genematcher = genepattern.matcher(content);//���һ�������
       		   if(genematcher.find())//�ҵ���
       		   {
       			   gffDetailLOC=new GffDetailUCSCgene();//�½�һ��������
       			   gffDetailLOC.locString=genematcher.group(); 
       			   gffDetailLOC.numberstart=Integer.parseInt(ss[3].toLowerCase());gffDetailLOC.numberend=Integer.parseInt(ss[4]);//������ֹ
       			   gffDetailLOC.ChrID=chrnametmpString;
       		      /**
       		       * �����������
       		       */
       		      if( ss[6].equals("+"))
       		    	  gffDetailLOC.cis5to3=true;
       		      else if(ss[6].equals("-"))
       		    	  gffDetailLOC.cis5to3=false;
       		      LOCList.add(gffDetailLOC);//��ӽ���LOClist
       		      locHashtable.put(gffDetailLOC.locString, gffDetailLOC);//��ӽ���hash��LOCID��--GeneInforlist��ϣ��ȷ��������������ǵ���֮��Ĺ�ϵ    
       		      LOCIDList.add(gffDetailLOC.locString);
       		   }
       		   else {
       			   System.out.println("GffHashPlantGeneError: �ļ�  "+gfffilename+"  �ڱ��п���û��ָ���Ļ���ID  " +content);
       		   }
       		   //���ñ�ǩ����ʾ��5UTR��CDS��ǰ���ˣ���ô�ں��� if �����ĵľ��ǵ�һ��UTR���һ��CDS
       		   UTR5start = true; 
       		   CDSstart = true;
       	   }
		   /**
       	    * ����ȡ��mRNAʱ������˵�ǿɱ����ʱ�����һ���µĿɱ����list
       	    * ������ô�Ӷ��Ǵӵ�һ��cds��ʼ�ӵ����һ��cds������Ļ����Ǵ�С�ӵ��󣬷�����ǴӴ�ӵ�С��
       	    * 
       	    * һ��������mRNA����Ҫ��ʼָ��5UTR��3UTR��CDS��������ֹ
       	    *
       	    */
		   else if (ss[2].equals("mRNA")) 
		   {
			   if (mRNAsplit) {
				   //����һ��mRNA����Ϣװ��
				   if (cdsStart < 0 && cdsEnd <0) {
					   cdsStart = mRNAend;
					   cdsEnd = mRNAend;
				   }
				   gffDetailLOC.addExon(0,cdsEnd,false); 
				   gffDetailLOC.addExon(0,cdsStart,false);
				   if (cdsStart <0 || cdsEnd<0 || cdsStart >= cdsEnd) {
					   System.out.println("GffHashPlantGeneError: �ļ�  "+gfffilename+"  �����������������⣬cdsStart��cdsEnd����  " +gffDetailLOC.locString);
				   }
				   mRNAsplit =false;
			   }
			   mRNAmatcher = mRNApattern.matcher(content);
			   if(mRNAmatcher.find())
			   {
				   //ÿ����һ��mRNA�����һ���ɱ����,��Ҫ����ת��Ϊ����
				   gffDetailLOC.addSplitName(mRNAmatcher.group());
				   //���һ��ת¼����Ȼ����Ӧ��Ϣ:
				   //��һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region end,�ӵ����ʼ�Ǹ�ת¼����Exon������Ϣ
				   gffDetailLOC.addsplitlist();
				   //����UCSC�������������һ���Ǳ����mRNA����ôcdsStart = cdsEnd = mRNAend
				   mRNAstart = Integer.parseInt(ss[3]); mRNAend = Integer.parseInt(ss[4]); 
				   cdsStart = -100; cdsEnd = -100;
			   }
			   else {
				   System.out.println("GffHashPlantGeneError: �ļ�  "+gfffilename+"  �ڱ��п���û��ָ���Ļ���ID  " +content);
			   }
			   //���ñ�ǩ����ʾ��5UTR��CDS��ǰ���ˣ���ô�ں��� if �����ĵľ��ǵ�һ��UTR���һ��CDS
       		   UTR5start = true; 
       		   CDSstart = true;
     	   }
		   
		   //����5UTR
		   else if (ss[2].equals("five_prime_UTR")) 
		   {
			   //����˳������Ҫ��С�ӵ���
			   if (gffDetailLOC.cis5to3) 
			   {
				   gffDetailLOC.addExon(Integer.parseInt(ss[3]));//���������,��С�ӵ���
				   gffDetailLOC.addExon(Integer.parseInt(ss[4]));
			   }
			   else//����װ
			   {
				   gffDetailLOC.addExon(0,Integer.parseInt(ss[4]),false);
				   gffDetailLOC.addExon(0,Integer.parseInt(ss[3]),false);//���������,��С�ӵ���
				  
			   }
			   //5UTR��ȥ��
			   UTR5start = false;
			   UTR5end = true;//5UTR���н���
			   mRNAsplit = true;//ȫ�µĻ��򣬽����λfalse
			   CDSstart = true; 
		   }
		   else if (ss[2].equals("CDS"))
		   {
			   if (CDSstart)
			   {
				   if (UTR5end) 
				   {
					   if (gffDetailLOC.cis5to3) {
						   gffDetailLOC.addExon(-1,Integer.parseInt(ss[3]),true);
						   gffDetailLOC.addExon(Integer.parseInt(ss[4]));//���������,��С�ӵ���
					   }
					   else {
						   gffDetailLOC.addExon(0,Integer.parseInt(ss[4]),true);//��������꣬��С�ӵ���
						   gffDetailLOC.addExon(0,Integer.parseInt(ss[3]),false);
					   }
					   UTR5start = false;
					   UTR5end = false;
				   }
				   else {
					   if (gffDetailLOC.cis5to3) {
						   gffDetailLOC.addExon(Integer.parseInt(ss[3]));//���������,��С�ӵ���
						   gffDetailLOC.addExon(Integer.parseInt(ss[4]));
					   }
					   else {//����װ
						   gffDetailLOC.addExon(0,Integer.parseInt(ss[4]),false);
						   gffDetailLOC.addExon(0,Integer.parseInt(ss[3]),false);//���������,��С�ӵ���
					   }
				   }
				   CDSstart = false;
				   CDSend = true;
				   cdsStart = Integer.parseInt(ss[3]);
				   cdsEnd = Integer.parseInt(ss[4]);
			   }
			   else {
				   if (gffDetailLOC.cis5to3) {
					   gffDetailLOC.addExon(Integer.parseInt(ss[3]));//���������,��С�ӵ���
					   gffDetailLOC.addExon(Integer.parseInt(ss[4]));
					   cdsEnd = Integer.parseInt(ss[4]);
				   }
				   else {//����װ
					   gffDetailLOC.addExon(0,Integer.parseInt(ss[4]),false);
					   gffDetailLOC.addExon(0,Integer.parseInt(ss[3]),false);//���������,��С�ӵ���
					   cdsStart = Integer.parseInt(ss[3]);
				   }
				   CDSend = true;
			   }
			   mRNAsplit = true;//ȫ�µĻ��򣬽����λfalse
		   }
		   else if (ss[2].equals("three_prime_UTR")) 
		   {
			   if (UTR5end || CDSend) {//���������һ��CDS��
				   //����˳������Ҫ��С�ӵ���
				   if (gffDetailLOC.cis5to3) 
				   {
					   gffDetailLOC.addExon(-1,Integer.parseInt(ss[3]),true);//���������,��С�ӵ���
					   gffDetailLOC.addExon(Integer.parseInt(ss[4]));
				   }
				   else//����װ
				   {
					   gffDetailLOC.addExon(0,Integer.parseInt(ss[4]),true);
					   gffDetailLOC.addExon(0,Integer.parseInt(ss[3]),false);//���������,��С�ӵ���
				   }
				   UTR5end = false; 
				   CDSend = false; //�Ѿ����ǽ��������һ��CDS��
			   }
			   else {
				   //����˳������Ҫ��С�ӵ���
				   if (gffDetailLOC.cis5to3) 
				   {
					   gffDetailLOC.addExon(Integer.parseInt(ss[3]));//���������,��С�ӵ���
					   gffDetailLOC.addExon(Integer.parseInt(ss[4]));
				   }
				   else//����װ
				   {
					   gffDetailLOC.addExon(0,Integer.parseInt(ss[4]),false);
					   gffDetailLOC.addExon(0,Integer.parseInt(ss[3]),false);//���������,��С�ӵ���
				   }
			   }
			   //5UTR��ȥ��
			   UTR3start = false;
			   UTR3end = true;//5UTR���н���
			   mRNAsplit = true;//ȫ�µĻ��򣬽����λfalse
		   }
	   }
	   if (mRNAsplit) {
		   //����һ��mRNA����Ϣװ��
		   if (cdsStart < 0 && cdsEnd <0) {
			   cdsStart = mRNAend;
			   cdsEnd = mRNAend;
		   }
		   gffDetailLOC.addExon(0,cdsEnd,false); 
		   gffDetailLOC.addExon(0,cdsStart,false);
		   if (cdsStart <0 || cdsEnd<0 || cdsStart > cdsEnd) {
			   System.out.println("GffHashPlantGeneError: �ļ�  "+gfffilename+"  �����������������⣬cdsStart��cdsEnd����  " +gffDetailLOC.locString);
		   }
		   mRNAsplit = false;//ȫ�µĻ��򣬽����λfalse
	   }
	   
	   LOCList.trimToSize();
	   //��peak����˳��װ��LOCIDList
	   for (GffDetail gffDetail : LOCList) {
		   LOCChrHashIDList.add(gffDetail.locString);
	   }
	   txtgff.close();
	   return Chrhash;//�������LOCarray��Ϣ
   }
   
   /**
	 * 	�����������ܳ��ȣ��ں����ܳ��ȵ���Ϣ
	 * ������
	 * Ϊһ��ArrayList-Integer
	 * 0: all5UTRLength <br>
	 * 1: all3UTRLength <br>
	 * 2: allExonLength ������5UTR��3UTR�ĳ��� <br> 
	 * 3: allIntronLength <br>
	 * 4: allup2kLength <br>
	 * 5: allGeneLength <br>
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Long> getGeneStructureLength()
	{
		ArrayList<Long> lsbackground=new ArrayList<Long>();
		
		long ChrLength=0;
		long allGeneLength=0;
		long allIntronLength=0;
		long allExonLength=0;
		long all5UTRLength=0;
		long all3UTRLength=0;
		long allup2kLength=0;

		int errorNum=0;//��UCSC���ж��ٻ����TSS�����ת¼�������
		/////////////////////��   ʽ   ��   ��//////////////////////////////////////////
		
		
		Iterator iter = Chrhash.entrySet().iterator();
		while (iter.hasNext()) 
		{
		    Map.Entry entry = (Map.Entry) iter.next();
		    //һ��һ��Ⱦɫ���ȥ����ں��Ӻ������ӵĳ���
		    ArrayList<GffDetail> val = ( ArrayList<GffDetail>)entry.getValue();
		    int chrLOCNum=val.size();
		    allup2kLength=allup2kLength+chrLOCNum*2000;
		    for (int i = 0; i < chrLOCNum; i++) 
			{
		    	long leftUTR=0;
		    	long rightUTR=0;
				GffDetailUCSCgene tmpUCSCgene=(GffDetailUCSCgene)val.get(i);
				
				allGeneLength=allGeneLength+(tmpUCSCgene.numberend-tmpUCSCgene.numberstart);
			//������ת¼��
				ArrayList<Object>  lstmpSplitInfo=tmpUCSCgene.getLongestSplit();
				ArrayList<Integer> lstmpSplit=(ArrayList<Integer>)lstmpSplitInfo.get(1);
				
				///////////////////////��UCSC���ж��ٻ����TSS�����ת¼�������//////////////////////////
				if ((tmpUCSCgene.cis5to3&&lstmpSplit.get(2)>tmpUCSCgene.numberstart) || ( !tmpUCSCgene.cis5to3&& lstmpSplit.get(lstmpSplit.size()-1)<tmpUCSCgene.numberend )){
					errorNum++;
				}
				
				
				/////////////////////////////////////////////////////////////////////////////////////////////////
				
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				///////////////////////// �� �� �� �� �� ////////////////////////////////////////
				for (int j = 4; j < lstmpSplit.size(); j=j+2) //0,1   2,3  4,5  6,7  8,9
				{
					allIntronLength=allIntronLength+(lstmpSplit.get(j)-lstmpSplit.get(j-1));
				}
				//////////////////////////////5UTR�������ӡ�3UTR �Ӻ�////////////////////////////////////////////////////
				int exonSize=lstmpSplit.size();                  // start  2,3   4,0,5   6,7  8,9   10,1,11  12,13 end
				leftUTR=lstmpSplit.get(2)-tmpUCSCgene.numberstart;
				rightUTR=tmpUCSCgene.numberend-lstmpSplit.get(exonSize-1);
				for (int j = 3; j <exonSize;j=j+2) //0,1   2,3  4,5  6,7  8,9
				{
					//ת¼����������Ӻ�
					if(lstmpSplit.get(j)<=lstmpSplit.get(0))
					{
						leftUTR=leftUTR+(lstmpSplit.get(j)-lstmpSplit.get(j-1));
						continue;
					}
					//ת¼�������������
					if (lstmpSplit.get(j)>lstmpSplit.get(0)&&lstmpSplit.get(j-1)<=lstmpSplit.get(0) ) 
					{
						leftUTR=leftUTR+(lstmpSplit.get(0)-lstmpSplit.get(j-1));
						//ת¼�յ���ͬһ����������
						if (lstmpSplit.get(j)>=lstmpSplit.get(1)&&lstmpSplit.get(j-1)<lstmpSplit.get(1)) 
						{
							rightUTR=rightUTR+(lstmpSplit.get(j)-lstmpSplit.get(1));
							allExonLength=allExonLength+(lstmpSplit.get(1)-lstmpSplit.get(0));
						}
						else 
						{
							allExonLength=allExonLength+(lstmpSplit.get(j)-lstmpSplit.get(0));
						}
						continue;
					}
					//ת¼�����������ǰ��ת¼�յ��������Ӻ�
					if(lstmpSplit.get(j-1)>lstmpSplit.get(0)&&lstmpSplit.get(j)<lstmpSplit.get(1))
					{
						allExonLength=allExonLength+(lstmpSplit.get(j)-lstmpSplit.get(j-1));
						continue;
					}
					
					
					//ת¼�յ�����������
					if (lstmpSplit.get(j)>=lstmpSplit.get(1)&&lstmpSplit.get(j-1)<lstmpSplit.get(1)) 
					{    //ת¼�����ͬһ����������
						if(lstmpSplit.get(j)>lstmpSplit.get(0)&&lstmpSplit.get(j-1)<=lstmpSplit.get(0))
						{
							continue;//�����Ѿ��������
						}
						else 
						{
							rightUTR=rightUTR+(lstmpSplit.get(j)-lstmpSplit.get(1));
							allExonLength=allExonLength+(lstmpSplit.get(1)-lstmpSplit.get(j-1));
						}
						continue;
					}
					//ת¼�յ���������ǰ
					if (lstmpSplit.get(j-1)>=lstmpSplit.get(1)) 
					{
						rightUTR=rightUTR+(lstmpSplit.get(j)-lstmpSplit.get(j-1));
						continue;
					}
				}
				if (tmpUCSCgene.cis5to3) 
				{
					all5UTRLength=all5UTRLength+leftUTR;
					all3UTRLength=all3UTRLength+rightUTR;
				}
				else 
				{
					all5UTRLength=all5UTRLength+rightUTR;
					all3UTRLength=all3UTRLength+leftUTR;
				}
			}
		}
		lsbackground.add(all5UTRLength);
		lsbackground.add(all3UTRLength);
		lsbackground.add(allExonLength);
		lsbackground.add(allIntronLength);
		lsbackground.add(allup2kLength);
		lsbackground.add(allGeneLength);
		System.out.println("getGeneStructureLength: ��UCSC���ж��ٻ����TSS�����ת¼�������"+errorNum);
		return lsbackground;
		
	}
	
}



