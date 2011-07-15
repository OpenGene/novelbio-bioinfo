package com.novelbio.analysis.seq.genome.gffOperate;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.novelbio.base.dataOperate.TxtReadandWrite;


/**
 * ר�Ŷ�ȡUCSC��gene�����ļ�,��ȡʱ�ӵڶ��ж���
 * ��ȡ��Ϻ��ͳ���ں��������ӵ���Ŀ
 * group:Genes and Gene Prediction Tracks
 * track:UCSC Genes
 * table:knownGene
 * output format:all fields from selected table
 * @author zong0jie
 *
 */
public class GffHashUCSCgene extends GffHashGene
{
	/**
	 * @Override
	 * ��ײ��ȡgff�ķ�����������ֻ�ܶ�ȡUCSCknown gene<br>
	 * ����Gff�ļ��������������ϣ���һ��list��,��ȡʱ�ӵڶ��ж���<br/>
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
     * <b>LOCChrHashIDList </b><br>
     *   LOCChrHashIDList�б���LOCID����������Ŀ���,��Chrhash�������һ�£���ͬһ����Ķ��ת¼������һ�� NM_XXXX/NM_XXXX...<br>
	 * @throws Exception 
	 */
	public Hashtable<String, ArrayList<GffDetail>> ReadGffarray(String gfffilename) throws Exception{
		
		//ʵ�����ĸ���
		locHashtable =new Hashtable<String, GffDetail>();//�洢ÿ��LOCID���������Ϣ�Ķ��ձ�
		Chrhash=new Hashtable<String, ArrayList<GffDetail>>();//һ����ϣ�����洢ÿ��Ⱦɫ��
		LOCIDList=new ArrayList<String>();//˳��洢ÿ������ţ��������������ȡ��������
		LOCChrHashIDList=new ArrayList<String>();
		
		TxtReadandWrite txtGffRead=new TxtReadandWrite();
		txtGffRead.setParameter(gfffilename,false, true);
		BufferedReader readGff=txtGffRead.readfile();
		
		ArrayList<GffDetail> LOCList=null ;//˳��洢ÿ��loc�ľ�����Ϣ��һ��Ⱦɫ��һ��LOCList�����װ��Chrhash����
		String content="";
		readGff.readLine();//������һ��
		String chrnametmpString="";
			//int mm=0;//�����Ķ���
		while ((content=readGff.readLine())!=null) 
		{
			String[] geneInfo=content.split("\t");
			String[] exonStarts=geneInfo[8].split(",");
			String[] exonEnds=geneInfo[9].split(",");
			chrnametmpString=geneInfo[1].toLowerCase();//Сд��chrID
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//�µ�Ⱦɫ��
			if (!Chrhash.containsKey(chrnametmpString)) //�µ�Ⱦɫ��
			{
				if(LOCList!=null)//����Ѿ�������LOCList��Ҳ����ǰһ��LOCList����ô�Ƚض̣�Ȼ��������gffGCtmpDetail.numberstart����
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
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//���ת¼��
			//���������ת¼����Ƿ�С���ϸ������ת¼�յ㣬���С�ڣ���˵�����������ϸ������һ��ת¼��
			GffDetailUCSCgene lastGffdetailUCSCgene;
			if(LOCList.size()>0 && Integer.parseInt(geneInfo[3]) < (lastGffdetailUCSCgene = (GffDetailUCSCgene)LOCList.get(LOCList.size()-1)).numberend )
			{
				//�޸Ļ��������յ�
				if(Integer.parseInt(geneInfo[3])<lastGffdetailUCSCgene.numberstart)
					lastGffdetailUCSCgene.numberstart=Integer.parseInt(geneInfo[3]);
				if(Integer.parseInt(geneInfo[4])>lastGffdetailUCSCgene.numberend)
					lastGffdetailUCSCgene.numberend=Integer.parseInt(geneInfo[4]);
			/**	ͬһ��ת¼���ڻ������и�
				boolean test=false;
				if(geneInfo[2].equals("+"))
					test=true;
				if(test!=lastGffdetailUCSCgene.cis5to3)
				{
					mm++;
					System.out.println(lastGffdetailUCSCgene.locString);
				}
				*/
				//��������(ת¼��)��IDװ��locString��
				lastGffdetailUCSCgene.locString = lastGffdetailUCSCgene.locString+"/"+geneInfo[0];
				lastGffdetailUCSCgene.addSplitName(geneInfo[0]);
				//���һ��ת¼����Ȼ����Ӧ��Ϣ:
				//��һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region end,�ӵ����ʼ�Ǹ�ת¼����Exon������Ϣ
				lastGffdetailUCSCgene.addsplitlist();
				lastGffdetailUCSCgene.addExon(Integer.parseInt(geneInfo[5]));lastGffdetailUCSCgene.addExon(Integer.parseInt(geneInfo[6]));
				int exonCount=Integer.parseInt(geneInfo[7]);
				for (int i = 0; i < exonCount; i++) {
					lastGffdetailUCSCgene.addExon(Integer.parseInt(exonStarts[i]));
					lastGffdetailUCSCgene.addExon(Integer.parseInt(exonEnds[i]));
				}
				if(geneInfo[2].equals("+"))
				{
					lastGffdetailUCSCgene.addCis5to3(true);
				}
				else
				{
					lastGffdetailUCSCgene.addCis5to3(false);
				}
				//������(ת¼��ID)װ��LOCList
				LOCIDList.add(geneInfo[0]);
				//��locHashtable����Ӧ����ĿҲ�޸ģ�ͬʱ�����µ���Ŀ
				//��ΪUCSC����û��ת¼��һ˵��ֻ������LOCID����һ����������������ֻ�ܹ�������ͬ��LOCIDָ��ͬһ��GffdetailUCSCgene
				String[] allLOCID=lastGffdetailUCSCgene.locString.split("/");
				for (int i = 0; i < allLOCID.length; i++) {
					locHashtable.put(allLOCID[i], lastGffdetailUCSCgene);
				}
				continue;
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//����»���
			GffDetailUCSCgene gffDetailUCSCgene=new GffDetailUCSCgene();
			gffDetailUCSCgene.ChrID=chrnametmpString;
			//������
			if(geneInfo[2].equals("+"))
			{
				gffDetailUCSCgene.cis5to3=true;
				gffDetailUCSCgene.addCis5to3(true);
			}
			else
			{
				gffDetailUCSCgene.cis5to3=false;
				gffDetailUCSCgene.addCis5to3(false);
			}
				
			gffDetailUCSCgene.locString=geneInfo[0];
			gffDetailUCSCgene.numberstart=Integer.parseInt(geneInfo[3]);
			gffDetailUCSCgene.numberend=Integer.parseInt(geneInfo[4]);
			gffDetailUCSCgene.addSplitName(geneInfo[0]);
			//���һ��ת¼����Ȼ����Ӧ��Ϣ:
			//��һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region end,�ӵ����ʼ�Ǹ�ת¼����Exon������Ϣ
			gffDetailUCSCgene.addsplitlist();
			gffDetailUCSCgene.addExon(Integer.parseInt(geneInfo[5]));gffDetailUCSCgene.addExon(Integer.parseInt(geneInfo[6]));
			int exonCount=Integer.parseInt(geneInfo[7]);
			for (int i = 0; i < exonCount; i++) {
				gffDetailUCSCgene.addExon(Integer.parseInt(exonStarts[i]));
				gffDetailUCSCgene.addExon(Integer.parseInt(exonEnds[i]));
			}
			LOCList.add(gffDetailUCSCgene);  
			LOCIDList.add(geneInfo[0]);
			locHashtable.put(geneInfo[0], gffDetailUCSCgene);
		}
		LOCList.trimToSize();
		//System.out.println(mm);
		for (GffDetail gffDetail : LOCList) {
			LOCChrHashIDList.add(gffDetail.locString);
		}
		txtGffRead.close();
		return Chrhash;
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
	public ArrayList<Long> getGeneStructureLength(int upBp)
	{
		ArrayList<Long> lsbackground=new ArrayList<Long>();
		
		long ChrLength=0;
		long allGeneLength=0;
		long allIntronLength=0;
		long allExonLength=0;
		long all5UTRLength=0;
		long all3UTRLength=0;
		long allupLength=0;

		int errorNum=0;//��UCSC���ж��ٻ����TSS�����ת¼�������
		/////////////////////��   ʽ   ��   ��//////////////////////////////////////////
		
		
		Iterator iter = Chrhash.entrySet().iterator();
		while (iter.hasNext()) 
		{
		    Map.Entry entry = (Map.Entry) iter.next();
		    //һ��һ��Ⱦɫ���ȥ����ں��Ӻ������ӵĳ���
		    ArrayList<GffDetail> val = ( ArrayList<GffDetail>)entry.getValue();
		    int chrLOCNum=val.size();
		    allupLength=allupLength+chrLOCNum*upBp;
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
		lsbackground.add(allupLength);
		lsbackground.add(allGeneLength);
		System.out.println("getGeneStructureLength: ��UCSC���ж��ٻ����TSS�����ת¼�������"+errorNum);
		return lsbackground;
		
	}
	
	/**
	 * 	�����������ܳ��ȣ��ں����ܳ��ȵ���Ϣ
	 * ������
	 * Ϊһ��ArrayList-Integer
	 * 0: all5UTRLength <br>
	 * 1: all3UTRLength <br>
	 * 2: allExonLength ������5UTR��3UTR�ĳ��� <br> 
	 * 3: allIntronLength <br>
	 * 4: allGeneLength <br>
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<ArrayList<Integer>> getGeneStructureDestrib()
	{
		ArrayList<ArrayList<Integer>> lsbackground=new ArrayList<ArrayList<Integer>>();
		
		long ChrLength=0;
		ArrayList<Integer> allGeneLength= new ArrayList<Integer>();
		ArrayList<Integer> lsIntronLength = new ArrayList<Integer>();
		ArrayList<Integer> lsExonLength = new ArrayList<Integer>();
		ArrayList<Integer>  all5UTRLength= new ArrayList<Integer>();
		ArrayList<Integer>  all3UTRLength= new ArrayList<Integer>();

		int errorNum=0;//��UCSC���ж��ٻ����TSS�����ת¼�������
		/////////////////////��   ʽ   ��   ��//////////////////////////////////////////
		
		
		Iterator iter = Chrhash.entrySet().iterator();
		while (iter.hasNext()) 
		{
		    Map.Entry entry = (Map.Entry) iter.next();
		    //һ��һ��Ⱦɫ���ȥ����ں��Ӻ������ӵĳ���
		    ArrayList<GffDetail> val = ( ArrayList<GffDetail>)entry.getValue();
		    int chrLOCNum=val.size();
		    for (int i = 0; i < chrLOCNum; i++) 
			{
		    	int leftUTR=0;
		    	int rightUTR=0;
				GffDetailUCSCgene tmpUCSCgene=(GffDetailUCSCgene)val.get(i);
				
				allGeneLength.add(tmpUCSCgene.numberend-tmpUCSCgene.numberstart);
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
					lsIntronLength.add(lstmpSplit.get(j)-lstmpSplit.get(j-1));
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
							lsExonLength.add(lstmpSplit.get(1)-lstmpSplit.get(0));
						}
						else 
						{
							lsExonLength.add(lstmpSplit.get(j)-lstmpSplit.get(0));
						}
						continue;
					}
					//ת¼�����������ǰ��ת¼�յ��������Ӻ�
					if(lstmpSplit.get(j-1)>lstmpSplit.get(0)&&lstmpSplit.get(j)<lstmpSplit.get(1))
					{
						lsExonLength.add(lstmpSplit.get(j)-lstmpSplit.get(j-1));
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
							lsExonLength.add(lstmpSplit.get(1)-lstmpSplit.get(j-1));
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
					all5UTRLength.add(leftUTR);
					all3UTRLength.add(rightUTR);
				}
				else 
				{
					all5UTRLength.add(rightUTR);
					all3UTRLength.add(leftUTR);
				}
			}
		}
		lsbackground.add(all5UTRLength);
		lsbackground.add(all3UTRLength);
		lsbackground.add(lsExonLength);
		lsbackground.add(lsIntronLength);
		lsbackground.add(allGeneLength);
		System.out.println("getGeneStructureLength: ��UCSC���ж��ٻ����TSS�����ת¼�������"+errorNum);
		return lsbackground;
		
	}
}
