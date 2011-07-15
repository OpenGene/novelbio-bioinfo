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
 * 专门读取UCSC的gene坐标文件,读取时从第二行读起
 * 读取完毕后可统计内含子外显子的数目
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
	 * 最底层读取gff的方法，本方法只能读取UCSCknown gene<br>
	 * 输入Gff文件，最后获得两个哈希表和一个list表,读取时从第二行读起<br/>
	 * 结构如下：<br/>
     * 输入Gff文件，最后获得两个哈希表和一个list表, 结构如下：<br>
     * <b>1.Chrhash</b><br>
     * （ChrID）--ChrList-- GeneInforList(GffDetail类)
     * 其中ChrID为小写，代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID, chr格式，全部小写 chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
    * 其中LOCID代表具体的条目编号，在UCSCkonwn gene里面没有转录本一说，
	 * 只有两个LOCID共用一个区域的情况，所以只能够两个不同的LOCID指向同一个GffdetailUCSCgene
     *  <b>3.LOCIDList</b><br>
     * （LOCID）--LOCIDList，按顺序保存LOCID,这里不考虑多个转录本，每一个转录本就是一个单独的LOCID <br>
     * <b>LOCChrHashIDList </b><br>
     *   LOCChrHashIDList中保存LOCID代表具体的条目编号,与Chrhash里的名字一致，将同一基因的多个转录本放在一起： NM_XXXX/NM_XXXX...<br>
	 * @throws Exception 
	 */
	public Hashtable<String, ArrayList<GffDetail>> ReadGffarray(String gfffilename) throws Exception{
		
		//实例化四个表
		locHashtable =new Hashtable<String, GffDetail>();//存储每个LOCID和其具体信息的对照表
		Chrhash=new Hashtable<String, ArrayList<GffDetail>>();//一个哈希表来存储每条染色体
		LOCIDList=new ArrayList<String>();//顺序存储每个基因号，这个打算用于提取随机基因号
		LOCChrHashIDList=new ArrayList<String>();
		
		TxtReadandWrite txtGffRead=new TxtReadandWrite();
		txtGffRead.setParameter(gfffilename,false, true);
		BufferedReader readGff=txtGffRead.readfile();
		
		ArrayList<GffDetail> LOCList=null ;//顺序存储每个loc的具体信息，一条染色体一个LOCList，最后装入Chrhash表中
		String content="";
		readGff.readLine();//跳过第一行
		String chrnametmpString="";
			//int mm=0;//计数的东西
		while ((content=readGff.readLine())!=null) 
		{
			String[] geneInfo=content.split("\t");
			String[] exonStarts=geneInfo[8].split(",");
			String[] exonEnds=geneInfo[9].split(",");
			chrnametmpString=geneInfo[1].toLowerCase();//小写的chrID
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//新的染色体
			if (!Chrhash.containsKey(chrnametmpString)) //新的染色体
			{
				if(LOCList!=null)//如果已经存在了LOCList，也就是前一个LOCList，那么先截短，然后将它按照gffGCtmpDetail.numberstart排序
				{
					LOCList.trimToSize();
					 //把peak名称顺序装入LOCIDList
					   for (GffDetail gffDetail : LOCList) {
						   LOCChrHashIDList.add(gffDetail.locString);
					   }
				}
				LOCList=new ArrayList<GffDetail>();//新建一个LOCList并放入Chrhash
				Chrhash.put(chrnametmpString, LOCList);
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//添加转录本
			//看本基因的转录起点是否小于上个基因的转录终点，如果小于，则说明本基因是上个基因的一个转录本
			GffDetailUCSCgene lastGffdetailUCSCgene;
			if(LOCList.size()>0 && Integer.parseInt(geneInfo[3]) < (lastGffdetailUCSCgene = (GffDetailUCSCgene)LOCList.get(LOCList.size()-1)).numberend )
			{
				//修改基因起点和终点
				if(Integer.parseInt(geneInfo[3])<lastGffdetailUCSCgene.numberstart)
					lastGffdetailUCSCgene.numberstart=Integer.parseInt(geneInfo[3]);
				if(Integer.parseInt(geneInfo[4])>lastGffdetailUCSCgene.numberend)
					lastGffdetailUCSCgene.numberend=Integer.parseInt(geneInfo[4]);
			/**	同一个转录本内还有正有负
				boolean test=false;
				if(geneInfo[2].equals("+"))
					test=true;
				if(test!=lastGffdetailUCSCgene.cis5to3)
				{
					mm++;
					System.out.println(lastGffdetailUCSCgene.locString);
				}
				*/
				//将本基因(转录本)的ID装入locString中
				lastGffdetailUCSCgene.locString = lastGffdetailUCSCgene.locString+"/"+geneInfo[0];
				lastGffdetailUCSCgene.addSplitName(geneInfo[0]);
				//添加一个转录本，然后将相应信息:
				//第一项是该转录本的Coding region start，第二项是该转录本的Coding region end,从第三项开始是该转录本的Exon坐标信息
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
				//将基因(转录本ID)装入LOCList
				LOCIDList.add(geneInfo[0]);
				//将locHashtable中相应的项目也修改，同时加入新的项目
				//因为UCSC里面没有转录本一说，只有两个LOCID共用一个区域的情况，所以只能够两个不同的LOCID指向同一个GffdetailUCSCgene
				String[] allLOCID=lastGffdetailUCSCgene.locString.split("/");
				for (int i = 0; i < allLOCID.length; i++) {
					locHashtable.put(allLOCID[i], lastGffdetailUCSCgene);
				}
				continue;
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//添加新基因
			GffDetailUCSCgene gffDetailUCSCgene=new GffDetailUCSCgene();
			gffDetailUCSCgene.ChrID=chrnametmpString;
			//正反向
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
			//添加一个转录本，然后将相应信息:
			//第一项是该转录本的Coding region start，第二项是该转录本的Coding region end,从第三项开始是该转录本的Exon坐标信息
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
	 * 	返回外显子总长度，内含子总长度等信息
	 * 有问题
	 * 为一个ArrayList-Integer
	 * 0: all5UTRLength <br>
	 * 1: all3UTRLength <br>
	 * 2: allExonLength 不包括5UTR和3UTR的长度 <br> 
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

		int errorNum=0;//看UCSC中有多少基因的TSS不是最长转录本的起点
		/////////////////////正   式   计   算//////////////////////////////////////////
		
		
		Iterator iter = Chrhash.entrySet().iterator();
		while (iter.hasNext()) 
		{
		    Map.Entry entry = (Map.Entry) iter.next();
		    //一条一条染色体的去检查内含子和外显子的长度
		    ArrayList<GffDetail> val = ( ArrayList<GffDetail>)entry.getValue();
		    int chrLOCNum=val.size();
		    allupLength=allupLength+chrLOCNum*upBp;
		    for (int i = 0; i < chrLOCNum; i++) 
			{
		    	long leftUTR=0;
		    	long rightUTR=0;
				GffDetailUCSCgene tmpUCSCgene=(GffDetailUCSCgene)val.get(i);
				
				allGeneLength=allGeneLength+(tmpUCSCgene.numberend-tmpUCSCgene.numberstart);
			//获得最长的转录本
				ArrayList<Object>  lstmpSplitInfo=tmpUCSCgene.getLongestSplit();
				ArrayList<Integer> lstmpSplit=(ArrayList<Integer>)lstmpSplitInfo.get(1);
				
				///////////////////////看UCSC中有多少基因的TSS不是最长转录本的起点//////////////////////////
				if ((tmpUCSCgene.cis5to3&&lstmpSplit.get(2)>tmpUCSCgene.numberstart) || ( !tmpUCSCgene.cis5to3&& lstmpSplit.get(lstmpSplit.size()-1)<tmpUCSCgene.numberend )){
					errorNum++;
				}
				
				
				/////////////////////////////////////////////////////////////////////////////////////////////////
				
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				///////////////////////// 内 含 子 加 和 ////////////////////////////////////////
				for (int j = 4; j < lstmpSplit.size(); j=j+2) //0,1   2,3  4,5  6,7  8,9
				{
					allIntronLength=allIntronLength+(lstmpSplit.get(j)-lstmpSplit.get(j-1));
				}
				//////////////////////////////5UTR、外显子、3UTR 加和////////////////////////////////////////////////////
				int exonSize=lstmpSplit.size();                  // start  2,3   4,0,5   6,7  8,9   10,1,11  12,13 end
				leftUTR=lstmpSplit.get(2)-tmpUCSCgene.numberstart;
				rightUTR=tmpUCSCgene.numberend-lstmpSplit.get(exonSize-1);
				for (int j = 3; j <exonSize;j=j+2) //0,1   2,3  4,5  6,7  8,9
				{
					//转录起点在外显子后
					if(lstmpSplit.get(j)<=lstmpSplit.get(0))
					{
						leftUTR=leftUTR+(lstmpSplit.get(j)-lstmpSplit.get(j-1));
						continue;
					}
					//转录起点在外显子中
					if (lstmpSplit.get(j)>lstmpSplit.get(0)&&lstmpSplit.get(j-1)<=lstmpSplit.get(0) ) 
					{
						leftUTR=leftUTR+(lstmpSplit.get(0)-lstmpSplit.get(j-1));
						//转录终点在同一个外显子中
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
					//转录起点在外显子前，转录终点在外显子后
					if(lstmpSplit.get(j-1)>lstmpSplit.get(0)&&lstmpSplit.get(j)<lstmpSplit.get(1))
					{
						allExonLength=allExonLength+(lstmpSplit.get(j)-lstmpSplit.get(j-1));
						continue;
					}
					
					
					//转录终点在外显子中
					if (lstmpSplit.get(j)>=lstmpSplit.get(1)&&lstmpSplit.get(j-1)<lstmpSplit.get(1)) 
					{    //转录起点在同一个外显子中
						if(lstmpSplit.get(j)>lstmpSplit.get(0)&&lstmpSplit.get(j-1)<=lstmpSplit.get(0))
						{
							continue;//上面已经计算过了
						}
						else 
						{
							rightUTR=rightUTR+(lstmpSplit.get(j)-lstmpSplit.get(1));
							allExonLength=allExonLength+(lstmpSplit.get(1)-lstmpSplit.get(j-1));
						}
						continue;
					}
					//转录终点在外显子前
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
		System.out.println("getGeneStructureLength: 看UCSC中有多少基因的TSS不是最长转录本的起点"+errorNum);
		return lsbackground;
		
	}
	
	/**
	 * 	返回外显子总长度，内含子总长度等信息
	 * 有问题
	 * 为一个ArrayList-Integer
	 * 0: all5UTRLength <br>
	 * 1: all3UTRLength <br>
	 * 2: allExonLength 不包括5UTR和3UTR的长度 <br> 
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

		int errorNum=0;//看UCSC中有多少基因的TSS不是最长转录本的起点
		/////////////////////正   式   计   算//////////////////////////////////////////
		
		
		Iterator iter = Chrhash.entrySet().iterator();
		while (iter.hasNext()) 
		{
		    Map.Entry entry = (Map.Entry) iter.next();
		    //一条一条染色体的去检查内含子和外显子的长度
		    ArrayList<GffDetail> val = ( ArrayList<GffDetail>)entry.getValue();
		    int chrLOCNum=val.size();
		    for (int i = 0; i < chrLOCNum; i++) 
			{
		    	int leftUTR=0;
		    	int rightUTR=0;
				GffDetailUCSCgene tmpUCSCgene=(GffDetailUCSCgene)val.get(i);
				
				allGeneLength.add(tmpUCSCgene.numberend-tmpUCSCgene.numberstart);
			//获得最长的转录本
				ArrayList<Object>  lstmpSplitInfo=tmpUCSCgene.getLongestSplit();
				ArrayList<Integer> lstmpSplit=(ArrayList<Integer>)lstmpSplitInfo.get(1);
				
				///////////////////////看UCSC中有多少基因的TSS不是最长转录本的起点//////////////////////////
				if ((tmpUCSCgene.cis5to3&&lstmpSplit.get(2)>tmpUCSCgene.numberstart) || ( !tmpUCSCgene.cis5to3&& lstmpSplit.get(lstmpSplit.size()-1)<tmpUCSCgene.numberend )){
					errorNum++;
				}
				
				
				/////////////////////////////////////////////////////////////////////////////////////////////////
				
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				///////////////////////// 内 含 子 加 和 ////////////////////////////////////////
				for (int j = 4; j < lstmpSplit.size(); j=j+2) //0,1   2,3  4,5  6,7  8,9
				{
					lsIntronLength.add(lstmpSplit.get(j)-lstmpSplit.get(j-1));
				}
				//////////////////////////////5UTR、外显子、3UTR 加和////////////////////////////////////////////////////
				int exonSize=lstmpSplit.size();                  // start  2,3   4,0,5   6,7  8,9   10,1,11  12,13 end
				leftUTR=lstmpSplit.get(2)-tmpUCSCgene.numberstart;
				rightUTR=tmpUCSCgene.numberend-lstmpSplit.get(exonSize-1);
				for (int j = 3; j <exonSize;j=j+2) //0,1   2,3  4,5  6,7  8,9
				{
					//转录起点在外显子后
					if(lstmpSplit.get(j)<=lstmpSplit.get(0))
					{
						leftUTR=leftUTR+(lstmpSplit.get(j)-lstmpSplit.get(j-1));
						continue;
					}
					//转录起点在外显子中
					if (lstmpSplit.get(j)>lstmpSplit.get(0)&&lstmpSplit.get(j-1)<=lstmpSplit.get(0) ) 
					{
						leftUTR=leftUTR+(lstmpSplit.get(0)-lstmpSplit.get(j-1));
						//转录终点在同一个外显子中
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
					//转录起点在外显子前，转录终点在外显子后
					if(lstmpSplit.get(j-1)>lstmpSplit.get(0)&&lstmpSplit.get(j)<lstmpSplit.get(1))
					{
						lsExonLength.add(lstmpSplit.get(j)-lstmpSplit.get(j-1));
						continue;
					}
					
					
					//转录终点在外显子中
					if (lstmpSplit.get(j)>=lstmpSplit.get(1)&&lstmpSplit.get(j-1)<lstmpSplit.get(1)) 
					{    //转录起点在同一个外显子中
						if(lstmpSplit.get(j)>lstmpSplit.get(0)&&lstmpSplit.get(j-1)<=lstmpSplit.get(0))
						{
							continue;//上面已经计算过了
						}
						else 
						{
							rightUTR=rightUTR+(lstmpSplit.get(j)-lstmpSplit.get(1));
							lsExonLength.add(lstmpSplit.get(1)-lstmpSplit.get(j-1));
						}
						continue;
					}
					//转录终点在外显子前
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
		System.out.println("getGeneStructureLength: 看UCSC中有多少基因的TSS不是最长转录本的起点"+errorNum);
		return lsbackground;
		
	}
}
