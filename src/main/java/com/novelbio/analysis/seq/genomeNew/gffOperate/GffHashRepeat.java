package com.novelbio.analysis.seq.genomeNew.gffOperate;

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
	 * ��ײ��ȡgff�ķ�����������ֻ�ܶ�ȡUCSCRepeat�ļ�<br>
	 * ����Gff�ļ��������������ϣ���һ��list��,��ȡʱ�ӵڶ��ж���<br/>
	 * �ṹ���£�<br/>
     * ����Gff�ļ���<b>����peak���Բ�����˳�����У������ڲ��������</b>�������������ϣ���һ��list��, �ṹ���£�<br>
     * <b>1.Chrhash</b><br>
     * ��ChrID��--ChrList-- GeneInforList(GffDetail��)
     * ����ChrIDΪСд������Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID, chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
    * ����LOCID����������Ŀ��ţ���UCSCkonwn gene����û��ת¼��һ˵��
	 * ֻ������LOCID����һ����������������ֻ�ܹ�������ͬ��LOCIDָ��ͬһ��GffdetailUCSCgene
     *  <b>3.LOCIDList</b><br>
     * ��LOCID��--LOCIDList����˳�򱣴�LOCID,���ﲻ���Ƕ��ת¼����ÿһ��ת¼������һ��������LOCID <br>
     * <b>LOCChrHashIDList </b><br>
     *   LOCChrHashIDList�б���LOCID����������Ŀ���,��Chrhash�������һ��<br>
	 * @throws Exception 
	 */
	@Override
	protected void ReadGffarrayExcep(String gfffilename) throws Exception {
		   mapChrID2ListGff=new LinkedHashMap<String, ListBin<GffDetailRepeat>>();//һ����ϣ�����洢ÿ��Ⱦɫ��
		   TxtReadandWrite txtgff=new TxtReadandWrite(gfffilename,false);
		   ListBin<GffDetailRepeat> LOCList=null ;//˳��洢ÿ��loc�ľ�����Ϣ��һ��Ⱦɫ��һ��LOCList�����װ��Chrhash����
		   
		  for( String content : txtgff.readlines()) {
			  if (content.startsWith("#"))
				continue;
			
			  String[] ss=content.split("\t");
			  String chrID=ss[5];
			  //�µ�Ⱦɫ��
			   if (!mapChrID2ListGff.containsKey(chrID.toLowerCase()))  {
				   if(LOCList!=null)//����Ѿ�������LOCList��Ҳ����ǰһ��LOCList����ô�Ƚض̣�Ȼ��������gffGCtmpDetail.numberstart����
					   LOCList.trimToSize();
				   
				   LOCList=new ListBin<GffDetailRepeat>();//�½�һ��LOCList������Chrhash
				   mapChrID2ListGff.put(chrID.toLowerCase(), LOCList);
			   }
			   //ÿһ�о���һ��repeat
			   GffDetailRepeat gffRepeatmpDetail=new GffDetailRepeat(chrID, ss[5]+"_"+ss[6]+"_"+ss[9], ss[9].equals("+"));
			   gffRepeatmpDetail.setStartAbs(Integer.parseInt(ss[6]));
			   gffRepeatmpDetail.setEndAbs(Integer.parseInt(ss[7]));			   
			   gffRepeatmpDetail.repeatFamily=ss[12];
			   gffRepeatmpDetail.repeatClass=ss[11];
			   gffRepeatmpDetail.repeatName=ss[10];
			   LOCList.add(gffRepeatmpDetail);  
		   }

		  LOCList.trimToSize();
		   txtgff.close();
	}
	
	/**
	 * ���ظ���repeat�������Լ���Ӧ�ı�������hash����ʽ����
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
			if (hashRepeatLength.containsKey(tmprepeatClass)) //������֪��repeat�����repeat�ĳ����ۼ���ȥ
			{
				tmpLength=tmpLength+hashRepeatLength.get(tmprepeatClass);
				hashRepeatLength.put(tmprepeatClass, tmpLength);
			}
			else//����������µ�repeat�ӽ�ȥ 
			{
				hashRepeatLength.put(tmprepeatClass,tmpLength);
			}
		}
		return hashRepeatLength;
	}

}
