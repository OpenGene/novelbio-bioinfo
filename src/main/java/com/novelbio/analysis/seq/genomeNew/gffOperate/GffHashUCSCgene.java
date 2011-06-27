package com.novelbio.analysis.seq.genomeNew.gffOperate;

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
	public GffHashUCSCgene(String gfffilename) throws Exception {
		super(gfffilename);
		// TODO Auto-generated constructor stub
	}

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
	protected void ReadGffarray(String gfffilename) throws Exception{
		
		//实例化四个表
		locHashtable =new Hashtable<String, GffDetailAbs>();//存储每个LOCID和其具体信息的对照表
		Chrhash=new Hashtable<String, ArrayList<GffDetailAbs>>();//一个哈希表来存储每条染色体
		LOCIDList=new ArrayList<String>();//顺序存储每个基因号，这个打算用于提取随机基因号
		LOCChrHashIDList=new ArrayList<String>();
		
		TxtReadandWrite txtGffRead=new TxtReadandWrite();
		txtGffRead.setParameter(gfffilename,false, true);
		BufferedReader readGff=txtGffRead.readfile();
		
		ArrayList<GffDetailAbs> LOCList=null ;//顺序存储每个loc的具体信息，一条染色体一个LOCList，最后装入Chrhash表中
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
					   for (GffDetailAbs gffDetail : LOCList) {
						   LOCChrHashIDList.add(gffDetail.locString);
					   }
				}
				LOCList=new ArrayList<GffDetailAbs>();//新建一个LOCList并放入Chrhash
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
		for (GffDetailAbs gffDetail : LOCList) {
			LOCChrHashIDList.add(gffDetail.locString);
		}
		txtGffRead.close();
	}

	@Override
	public GffDetailUCSCgene LOCsearch(String LOCID) {
		return (GffDetailUCSCgene) locHashtable.get(LOCID);
	}

	@Override
	public GffDetailUCSCgene LOCsearch(String chrID, int LOCNum) {
		return (GffDetailUCSCgene) Chrhash.get(chrID).get(LOCNum);
	}

	@Override
	public GffCodInfoUCSCgene searchLoc(String chrID, int Coordinate) {
		return (GffCodInfoUCSCgene) searchLocation(chrID, Coordinate);
	}
	
}
