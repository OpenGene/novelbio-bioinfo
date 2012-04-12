package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.dataStructure.listOperate.ListAbs;
import com.novelbio.database.model.modcopeid.CopedID;


/**
 * 本开闭区间已经设定
 * UCSC的默认文件的起点是开区间，终点为闭区间。水稻拟南芥的还没有设定开闭区间
 * 专门读取UCSC的gene坐标文件,读取时从第二行读起
 * 读取完毕后可统计内含子外显子的数目
 * group:Genes and Gene Prediction Tracks
 * track:UCSC Genes
 * table:knownGene
 * output format:all fields from selected table
 * @author zong0jie
 *
 */
public class GffHashGeneUCSC extends GffHashGeneAbs{

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
	 * 会有有多个LOCID共用一个区域的情况，所以有多个不同的LOCID指向同一个GffdetailUCSCgene
     *  <b>3.LOCIDList</b><br>
     * （LOCID）这个List顺序存储每个基因号或条目号，这个打算用于提取随机基因号，实际上是所有条目按顺序放入，但是不考虑转录本(UCSC)或是重复(Peak) 这个ID与locHash一一对应，但是不能用它来确定某条目的前一个或后一个条目 <br>
     * <b>4.LOCChrHashIDList </b><br>
     *   LOCChrHashIDList中保存LOCID代表具体的条目编号,与Chrhash里的名字一致，将同一基因的多个转录本放在一起，用斜线分割"/"： NM_XXXX/NM_XXXX...<br>
	 * @throws Exception 
	 */
	protected void ReadGffarrayExcep(String gfffilename) throws Exception {
		setTaxID(gfffilename);
		// 实例化四个表
		Chrhash = new LinkedHashMap<String, ListAbs<GffDetailGene>>();// 一个哈希表来存储每条染色体
		locHashtable = new HashMap<String, GffDetailGene>();// 存储每个LOCID和其具体信息的对照表
		LOCIDList = new ArrayList<String>();// 顺序存储每个基因号，这个打算用于提取随机基因号

		TxtReadandWrite txtGffRead = new TxtReadandWrite(gfffilename, false);
		BufferedReader readGff = txtGffRead.readfile();

		ListAbs<GffDetailGene> LOCList = null;// 顺序存储每个loc的具体信息，一条染色体一个LOCList，最后装入Chrhash表中
		String content = "";
		readGff.readLine();// 跳过第一行
		String chrnametmpString = "";
		// int mm=0;//计数的东西
		while ((content = readGff.readLine()) != null) {
			content = content.replace("\"", "");
			String[] geneInfo = content.split("\t");
			String[] exonStarts = geneInfo[8].split(",");
			String[] exonEnds = geneInfo[9].split(",");
			chrnametmpString = geneInfo[1].toLowerCase();// 小写的chrID
			// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 新的染色体
			if (!Chrhash.containsKey(chrnametmpString)) // 新的染色体
			{
				if (LOCList != null)// 如果已经存在了LOCList，也就是前一个LOCList，那么先截短，然后将它按照gffGCtmpDetail.numberstart排序
				{
					LOCList.trimToSize();
				}
				LOCList = new ListAbs<GffDetailGene>();// 新建一个LOCList并放入Chrhash
				LOCList.setName(chrnametmpString);
				Chrhash.put(chrnametmpString, LOCList);
			}
			// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 添加转录本
			// 看本基因的转录起点是否小于上个基因的转录终点，如果小于，则说明本基因是上个基因的一个转录本
			GffDetailGene lastGffdetailUCSCgene = null; double[] overlapInfo = null;
			if (LOCList.size() > 0 ) {
				lastGffdetailUCSCgene = (GffDetailGene) LOCList.get(LOCList.size() - 1);
				overlapInfo = ArrayOperate.cmpArray(new double[]{Double.parseDouble(geneInfo[3]), Double.parseDouble(geneInfo[4])}, 
						new double[]{lastGffdetailUCSCgene.numberstart, lastGffdetailUCSCgene.numberend});	
			}
			
			if (LOCList.size() > 0 
					&& // 如果转录本方向不同，那就新开一个转录本
					geneInfo[2].equals("+") == lastGffdetailUCSCgene.cis5to3
					&&
					//将其改为重叠1/3以上才认为是同一个基因
					(overlapInfo[2] > 0.3 || overlapInfo[3] > 0.3)
					)
			{
				// 修改基因起点和终点
				if (Integer.parseInt(geneInfo[3])+startRegion < lastGffdetailUCSCgene.numberstart)
					lastGffdetailUCSCgene.numberstart = Integer.parseInt(geneInfo[3])+startRegion;
				if (Integer.parseInt(geneInfo[4])+endRegion > lastGffdetailUCSCgene.numberend)
					lastGffdetailUCSCgene.numberend = Integer.parseInt(geneInfo[4])+endRegion;

				// 将本基因(转录本)的ID装入locString中
				lastGffdetailUCSCgene.setName(lastGffdetailUCSCgene.getName() + ListAbs.SEP + geneInfo[0]);
				if (Math.abs(Integer.parseInt(geneInfo[5]) - Integer.parseInt(geneInfo[6])) <= 2) {
					lastGffdetailUCSCgene.addsplitlist(geneInfo[0], GffGeneIsoInfo.TYPE_GENE_MIRNA);
				}
				else {
					lastGffdetailUCSCgene.addsplitlist(geneInfo[0], GffGeneIsoInfo.TYPE_GENE_MRNA);
				}
				// 添加一个转录本，然后将相应信息:
				// 第一项是该转录本的Coding region start，第二项是该转录本的Coding region
				// end,从第三项开始是该转录本的Exon坐标信息
				lastGffdetailUCSCgene.addATGUAG(Integer.parseInt(geneInfo[5])+startRegion,Integer.parseInt(geneInfo[6])+endRegion);
				
				int exonCount = Integer.parseInt(geneInfo[7]);
				for (int i = 0; i < exonCount; i++) {
					lastGffdetailUCSCgene.addExonUCSCGFF(Integer.parseInt(exonStarts[i])+startRegion, Integer.parseInt(exonEnds[i])+endRegion);
				}
				// 将基因(转录本ID)装入LOCList
				LOCIDList.add(geneInfo[0]);
				// 将locHashtable中相应的项目也修改，同时加入新的项目
				// 因为UCSC里面没有转录本一说，只有两个LOCID共用一个区域的情况，所以只能够两个不同的LOCID指向同一个GffdetailUCSCgene
				String[] allLOCID = lastGffdetailUCSCgene.getName().split(ListAbs.SEP);
				for (int i = 0; i < allLOCID.length; i++) {
					locHashtable.put(allLOCID[i].toLowerCase(), lastGffdetailUCSCgene);
				}
				continue;
			}
			// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 添加新基因
			GffDetailGene gffDetailUCSCgene = new GffDetailGene(chrnametmpString, geneInfo[0], geneInfo[2].equals("+"));
			gffDetailUCSCgene.setTaxID(taxID);
			gffDetailUCSCgene.numberstart = Integer.parseInt(geneInfo[3])+startRegion;
			gffDetailUCSCgene.numberend = Integer.parseInt(geneInfo[4])+endRegion;
			if (Math.abs(Integer.parseInt(geneInfo[5]) - Integer.parseInt(geneInfo[6])) <= 1) {
				gffDetailUCSCgene.addsplitlist(geneInfo[0], GffGeneIsoInfo.TYPE_GENE_MIRNA);
			}
			else {
				gffDetailUCSCgene.addsplitlist(geneInfo[0], GffGeneIsoInfo.TYPE_GENE_MRNA);
			}
			// 添加一个转录本，然后将相应信息:
			// 第一项是该转录本的Coding region start，第二项是该转录本的Coding region
			// end,从第三项开始是该转录本的Exon坐标信息
			gffDetailUCSCgene.addATGUAG(Integer.parseInt(geneInfo[5])+startRegion,Integer.parseInt(geneInfo[6])+endRegion);
			int exonCount = Integer.parseInt(geneInfo[7]);
			for (int i = 0; i < exonCount; i++) {
				gffDetailUCSCgene.addExonUCSCGFF(Integer.parseInt(exonStarts[i])+startRegion,Integer.parseInt(exonEnds[i])+endRegion);
			}
			LOCList.add(gffDetailUCSCgene);
			LOCIDList.add(geneInfo[0]);
			locHashtable.put(geneInfo[0].toLowerCase(), gffDetailUCSCgene);
		}
		LOCList.trimToSize();
		txtGffRead.close();
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
	 * 会有有多个LOCID共用一个区域的情况，所以有多个不同的LOCID指向同一个GffdetailUCSCgene
     *  <b>3.LOCIDList</b><br>
     * （LOCID）这个List顺序存储每个基因号或条目号，这个打算用于提取随机基因号，实际上是所有条目按顺序放入，但是不考虑转录本(UCSC)或是重复(Peak) 这个ID与locHash一一对应，但是不能用它来确定某条目的前一个或后一个条目 <br>
     * <b>4.LOCChrHashIDList </b><br>
     *   LOCChrHashIDList中保存LOCID代表具体的条目编号,与Chrhash里的名字一致，将同一基因的多个转录本放在一起，用ListAbs.SEP分割： NM_XXXX/NM_XXXX...<br>
	 * @throws Exception 
	 */
	protected void ReadGffarrayExcep2(String gfffilename) throws Exception {
		setTaxID(gfffilename);
		// 实例化四个表
		Chrhash = new LinkedHashMap<String, ListAbs<GffDetailGene>>();// 一个哈希表来存储每条染色体
		locHashtable = new HashMap<String, GffDetailGene>();// 存储每个LOCID和其具体信息的对照表
		LOCIDList = new ArrayList<String>();// 顺序存储每个基因号，这个打算用于提取随机基因号

		TxtReadandWrite txtGffRead = new TxtReadandWrite(gfffilename, false);
		BufferedReader readGff = txtGffRead.readfile();

		ListAbs<GffDetailGene> LOCList = null;// 顺序存储每个loc的具体信息，一条染色体一个LOCList，最后装入Chrhash表中
		String content = "";
		readGff.readLine();// 跳过第一行
		String chrnametmpString = "";
		// int mm=0;//计数的东西
		while ((content = readGff.readLine()) != null) {
			content = content.replace("\"", "");
			String[] geneInfo = content.split("\t");
			String[] exonStarts = geneInfo[8].split(",");
			String[] exonEnds = geneInfo[9].split(",");
			chrnametmpString = geneInfo[1].toLowerCase();// 小写的chrID
			// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 新的染色体
			if (!Chrhash.containsKey(chrnametmpString)) // 新的染色体
			{
				if (LOCList != null)// 如果已经存在了LOCList，也就是前一个LOCList，那么先截短，然后将它按照gffGCtmpDetail.numberstart排序
				{
					LOCList.trimToSize();
				}
				LOCList = new ListAbs<GffDetailGene>();// 新建一个LOCList并放入Chrhash
				Chrhash.put(chrnametmpString, LOCList);
			}
			// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 添加转录本
			// 看本基因的转录起点是否小于上个基因的转录终点，如果小于，则说明本基因是上个基因的一个转录本
			GffDetailGene lastGffdetailUCSCgene;
			lastGffdetailUCSCgene = (GffDetailGene) LOCList.get(LOCList.size() - 1);
			double[] regionLast = new double[]{lastGffdetailUCSCgene.getStartAbs(), lastGffdetailUCSCgene.getEndAbs()};
			double[] regionThis = new double[]{Double.parseDouble(geneInfo[3])+startRegion, Double.parseDouble(geneInfo[4])+endRegion };
			double[] overlap = ArrayOperate.cmpArray(regionLast, regionThis);
			if (LOCList.size() > 0 
					&&
					overlap[2] > 0.5 || overlap[3] > 0.5
				)
			{
				// 修改基因起点和终点
				if (Integer.parseInt(geneInfo[3])+startRegion < lastGffdetailUCSCgene.numberstart)
					lastGffdetailUCSCgene.numberstart = Integer.parseInt(geneInfo[3])+startRegion;
				if (Integer.parseInt(geneInfo[4])+endRegion > lastGffdetailUCSCgene.numberend)
					lastGffdetailUCSCgene.numberend = Integer.parseInt(geneInfo[4])+endRegion;

				if (geneInfo[2].equals("+") != lastGffdetailUCSCgene.isCis5to3()) {
					lastGffdetailUCSCgene.cis5to3 = null;
				}
				
				
				// 将本基因(转录本)的ID装入locString中
				lastGffdetailUCSCgene.setName(lastGffdetailUCSCgene.getName() + ListAbs.SEP + geneInfo[0]);
				if (Math.abs(Integer.parseInt(geneInfo[5]) - Integer.parseInt(geneInfo[6])) <= 1) {
					lastGffdetailUCSCgene.addsplitlist(geneInfo[0], GffGeneIsoInfo.TYPE_GENE_MIRNA, geneInfo[2].equals("+"));
				}
				else {
					lastGffdetailUCSCgene.addsplitlist(geneInfo[0], GffGeneIsoInfo.TYPE_GENE_MRNA, geneInfo[2].equals("+"));
				}
				// 添加一个转录本，然后将相应信息:
				// 第一项是该转录本的Coding region start，第二项是该转录本的Coding region
				// end,从第三项开始是该转录本的Exon坐标信息
				lastGffdetailUCSCgene.addATGUAG(Integer.parseInt(geneInfo[5])+startRegion,Integer.parseInt(geneInfo[6])+endRegion);
				
				int exonCount = Integer.parseInt(geneInfo[7]);
				for (int i = 0; i < exonCount; i++) {
					lastGffdetailUCSCgene.addExonUCSCGFF(Integer.parseInt(exonStarts[i])+startRegion, Integer.parseInt(exonEnds[i])+endRegion);
				}
				// 将基因(转录本ID)装入LOCList
				LOCIDList.add(geneInfo[0]);
				// 将locHashtable中相应的项目也修改，同时加入新的项目
				// 因为UCSC里面没有转录本一说，只有两个LOCID共用一个区域的情况，所以只能够两个不同的LOCID指向同一个GffdetailUCSCgene
				String[] allLOCID = lastGffdetailUCSCgene.getName().split(ListAbs.SEP);
				for (int i = 0; i < allLOCID.length; i++) {
					locHashtable.put(allLOCID[i].toLowerCase(), lastGffdetailUCSCgene);
				}
				continue;
			}
			// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 添加新基因
			GffDetailGene gffDetailUCSCgene = new GffDetailGene(chrnametmpString, geneInfo[0], geneInfo[2].equals("+"));
			gffDetailUCSCgene.setTaxID(taxID);
			gffDetailUCSCgene.numberstart = Integer.parseInt(geneInfo[3])+startRegion;
			gffDetailUCSCgene.numberend = Integer.parseInt(geneInfo[4])+endRegion;
			if (Math.abs(Integer.parseInt(geneInfo[5]) - Integer.parseInt(geneInfo[6])) <= 1) {
				gffDetailUCSCgene.addsplitlist(geneInfo[0], GffGeneIsoInfo.TYPE_GENE_MIRNA);
			}
			else {
				gffDetailUCSCgene.addsplitlist(geneInfo[0], GffGeneIsoInfo.TYPE_GENE_MRNA);
			}
			// 添加一个转录本，然后将相应信息:
			// 第一项是该转录本的Coding region start，第二项是该转录本的Coding region
			// end,从第三项开始是该转录本的Exon坐标信息
			gffDetailUCSCgene.addATGUAG(Integer.parseInt(geneInfo[5])+startRegion,Integer.parseInt(geneInfo[6])+endRegion);
			int exonCount = Integer.parseInt(geneInfo[7]);
			for (int i = 0; i < exonCount; i++) {
				gffDetailUCSCgene.addExonUCSCGFF(Integer.parseInt(exonStarts[i])+startRegion,Integer.parseInt(exonEnds[i])+endRegion);
			}
			LOCList.add(gffDetailUCSCgene);
			LOCIDList.add(geneInfo[0]);
			locHashtable.put(geneInfo[0].toLowerCase(), gffDetailUCSCgene);
		}
		LOCList.trimToSize();
		txtGffRead.close();
	}
	
	private void setTaxID(String gffFile) {
		if (taxID != 0) {
			return;
		}
		TxtReadandWrite txtGffRead = new TxtReadandWrite(gffFile, false);
		ArrayList<String> lsInfo = null;
		try {
			lsInfo = txtGffRead.readFirstLines(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (String accID : lsInfo) {
			ArrayList<CopedID> lsCopedIDs = CopedID.getLsCopedID(
					accID.split("\t")[0], 0, false);
			if (lsCopedIDs.size() == 1 && lsCopedIDs.get(0).getIDtype() != CopedID.IDTYPE_ACCID) {
				taxID = lsCopedIDs.get(0).getTaxID();
				break;
			}
		}
		
	}
	
}
