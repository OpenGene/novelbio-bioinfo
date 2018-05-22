package com.novelbio.analysis.seq.genome.gffoperate;

import java.util.LinkedHashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.listoperate.ListBin;
import com.novelbio.listoperate.ListCodAbs;
import com.novelbio.listoperate.ListCodAbsDu;
import com.novelbio.listoperate.ListHashSearch;

/**
 * 获得UCSC中CpG等Gff的条目信息,本类必须实例化才能使用<br/>
 * 输入Gff文件，最后获得两个哈希表和一个list表,
 * 结构如下：<br/>
 * 1.hash（ChrID）--ChrList--GeneInforList(GffDetail类)<br/>
 *   其中ChrID为小写，代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID
 * chr格式，全部小写 chr1,chr2,chr11<br/>
 * 
 * 2.hash（LOCID）--GeneInforlist，其中LOCID代表具体的基因编号,本类中定义为：107_chr1_CpG: 128_36568608 <br/>
	 * 具体为：#bin_chrom_name_chromStart
 * 
 * 3.list（LOCID）--LOCList，按顺序保存LOCID<br/>
 * 
 * 每个基因的起点终点和CDS的起点终点保存在GffDetailList类中<br/>
 */
public class GffHashCG extends ListHashSearch<GffDetailCG, ListCodAbs<GffDetailCG>, 
ListCodAbsDu<GffDetailCG,ListCodAbs<GffDetailCG>>, ListBin<GffDetailCG>> {

	@Override
	protected void ReadGffarrayExcep(String gfffilename) throws Exception {
		mapChrID2ListGff=new LinkedHashMap<String, ListBin<GffDetailCG>>();
		ListBin<GffDetailCG> LOCList=null ;
		
		TxtReadandWrite txtgff = new TxtReadandWrite(gfffilename, false);
		//从第二行开始读，因为第一行是title
		for (String content : txtgff.readlines()) {
			if (content.startsWith("#")) {
				continue;
			}
			String[] ss=content.split("\t");
			String chrID=ss[1].toLowerCase();
			//新的染色体
			if (!mapChrID2ListGff.containsKey(chrID))  {
				if(LOCList!=null) {
					LOCList.trimToSize();
				}
				LOCList=new ListBin<GffDetailCG>();//新建一个LOCList并放入Chrhash
				mapChrID2ListGff.put(chrID, LOCList);
			}
			
			// 每一行就是一个CG
			GffDetailCG gffGCtmpDetail = new GffDetailCG(chrID, ss[0] + "_" + ss[1] + "_" + ss[4] + "_" + ss[2], true);
			gffGCtmpDetail.setStartAbs(Integer.parseInt(ss[2]));
			gffGCtmpDetail.setEndAbs(Integer.parseInt(ss[3]));
			gffGCtmpDetail.lengthCpG = Integer.parseInt(ss[5]);
			gffGCtmpDetail.numCpG = Integer.parseInt(ss[6]);
			gffGCtmpDetail.numGC = Integer.parseInt(ss[7]);
			gffGCtmpDetail.perCpG = Double.parseDouble(ss[8]);
			gffGCtmpDetail.perGC = Double.parseDouble(ss[9]);
			gffGCtmpDetail.obsExp = Double.parseDouble(ss[10]);
			// 装入LOCList和locHashtable
			LOCList.add(gffGCtmpDetail);
		}
		 LOCList.trimToSize();
		txtgff.close();
	}
	
	/**
	 * 返回CG比例，以hash表形式返回
	 * @return
	 */
	public Integer getCpGLength() {
		int tmpLength = 0;
		for (ListBin<GffDetailCG> listGff : mapChrID2ListGff.values()) {
			for (GffDetailCG gffDetailCG : listGff) {
				tmpLength += gffDetailCG.getEndAbs() - gffDetailCG.getStartAbs();
			}
		}
		return tmpLength;
	}
}
