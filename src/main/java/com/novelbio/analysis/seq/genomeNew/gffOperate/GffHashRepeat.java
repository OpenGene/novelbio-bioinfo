package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListBin;
import com.novelbio.base.dataStructure.listOperate.ListCodAbs;
import com.novelbio.base.dataStructure.listOperate.ListCodAbsDu;
import com.novelbio.base.dataStructure.listOperate.ListHashSearch;


public class GffHashRepeat extends ListHashSearch<GffDetailRepeat, ListCodAbs<GffDetailRepeat>, 
ListCodAbsDu<GffDetailRepeat, ListCodAbs<GffDetailRepeat>>, ListBin<GffDetailRepeat>> {

	/**
	 * 最底层读取gff的方法，本方法只能读取UCSCRepeat文件<br>
	 * 输入Gff文件，最后获得两个哈希表和一个list表,读取时从第二行读起<br/>
	 * 结构如下：<br/>
     * 输入Gff文件，<b>其中peak可以不按照顺序排列，本类内部会给排序</b>，最后获得两个哈希表和一个list表, 结构如下：<br>
     * <b>1.Chrhash</b><br>
     * （ChrID）--ChrList-- GeneInforList(GffDetail类)
     * 其中ChrID为小写，代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID, chr格式，全部小写 chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
    * 其中LOCID代表具体的条目编号，在UCSCkonwn gene里面没有转录本一说，
	 * 只有两个LOCID共用一个区域的情况，所以只能够两个不同的LOCID指向同一个GffdetailUCSCgene
     *  <b>3.LOCIDList</b><br>
     * （LOCID）--LOCIDList，按顺序保存LOCID,这里不考虑多个转录本，每一个转录本就是一个单独的LOCID <br>
     * <b>LOCChrHashIDList </b><br>
     *   LOCChrHashIDList中保存LOCID代表具体的条目编号,与Chrhash里的名字一致<br>
	 * @throws Exception 
	 */
	@Override
	protected void ReadGffarrayExcep(String gfffilename) throws Exception {
		   mapChrID2ListGff=new LinkedHashMap<String, ListBin<GffDetailRepeat>>();//一个哈希表来存储每条染色体
		   //为读文件做准备
		   TxtReadandWrite txtgff=new TxtReadandWrite(gfffilename,false);
		   BufferedReader reader=txtgff.readfile();//open gff file
	       
		   String[] ss = null;//存储分割数组的临时变量
		   String content="";
		   //临时变量
		   ListBin<GffDetailRepeat> LOCList=null ;//顺序存储每个loc的具体信息，一条染色体一个LOCList，最后装入Chrhash表中
		   String chrnametmpString=""; //染色体的临时名字
		   
		   reader.readLine();//跳过第一行
		   while((content=reader.readLine())!=null)//读到结尾
		   {
			   ss=content.split("\t");
			   chrnametmpString=ss[5].toLowerCase();//小写
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			    //新的染色体
			   if (!mapChrID2ListGff.containsKey(chrnametmpString)) //新的染色体
			   {
				   if(LOCList!=null)//如果已经存在了LOCList，也就是前一个LOCList，那么先截短，然后将它按照gffGCtmpDetail.numberstart排序
				   {
					   LOCList.trimToSize();
				   }
				   LOCList=new ListBin<GffDetailRepeat>();//新建一个LOCList并放入Chrhash
				   mapChrID2ListGff.put(chrnametmpString, LOCList);
			   }
			  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			   //每一行就是一个repeat
			   GffDetailRepeat gffRepeatmpDetail=new GffDetailRepeat(chrnametmpString, ss[5]+"_"+ss[6]+"_"+ss[9], ss[9].equals("+"));
			   gffRepeatmpDetail.setStartAbs(Integer.parseInt(ss[6]));
			   gffRepeatmpDetail.setEndAbs(Integer.parseInt(ss[7]));
			   //装入LOCList和locHashtable
			   
			   gffRepeatmpDetail.repeatFamily=ss[12];
			   gffRepeatmpDetail.repeatClass=ss[11];
			   gffRepeatmpDetail.repeatName=ss[10];
			   LOCList.add(gffRepeatmpDetail);  
		   }
		   /////////////////////////////////////////////////////////////////////////////////////////////
		   LOCList.trimToSize();

		   txtgff.close();
		 /////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	/**
	 * 返回各个repeat的种类以及相应的比例，以hash表形式返回
	 * @return
	 */
	public Hashtable<String, Integer> getLength() 
	{
		int LOCNum=lsNameNoRedundent.size();
		Hashtable<String, Integer> hashRepeatLength=new Hashtable<String, Integer>();
		
		for (int i = 0; i < LOCNum; i++) 
		{
			GffDetailRepeat gffDetailRepeat=mapName2DetailAbs.get(lsNameNoRedundent.get(i));
			int tmpLength=gffDetailRepeat.Length();
			String tmprepeatClass=gffDetailRepeat.repeatClass+"/"+gffDetailRepeat.repeatFamily;
			if (hashRepeatLength.containsKey(tmprepeatClass)) //含有已知的repeat，则把repeat的长度累加上去
			{
				tmpLength=tmpLength+hashRepeatLength.get(tmprepeatClass);
				hashRepeatLength.put(tmprepeatClass, tmpLength);
			}
			else//不含有则把新的repeat加进去 
			{
				hashRepeatLength.put(tmprepeatClass,tmpLength);
			}
		}
		return hashRepeatLength;
	}

}
