package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.listOperate.ListDetailAbs;
import com.novelbio.base.dataStructure.listOperate.ListAbsSearch;
import com.novelbio.base.dataStructure.listOperate.ListHashSearch;



/**
 * ���UCSC��CpG��Gff����Ŀ��Ϣ,�������ʵ��������ʹ��<br/>
 * ����Gff�ļ��������������ϣ���һ��list��,
 * �ṹ���£�<br/>
 * 1.hash��ChrID��--ChrList--GeneInforList(GffDetail��)<br/>
 *   ����ChrIDΪСд������Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID
 * chr��ʽ��ȫ��Сд chr1,chr2,chr11<br/>
 * 
 * 2.hash��LOCID��--GeneInforlist������LOCID�������Ļ�����,�����ж���Ϊ��107_chr1_CpG: 128_36568608 <br/>
	 * ����Ϊ��#bin_chrom_name_chromStart
 * 
 * 3.list��LOCID��--LOCList����˳�򱣴�LOCID<br/>
 * 
 * ÿ�����������յ��CDS������յ㱣����GffDetailList����<br/>
 */
public class GffHashCG extends ListHashSearch<GffDetailCG>
{	

	/**
	 * ��ײ��ȡgff�ķ���<br>
	 * ����Gff�ļ��������������ϣ���һ��list��,
	 * �ṹ���£�<br/>
	 * @3.LOCIDList
	 * ��LOCID��--LOCIDList����˳�򱣴�LOCID<br/>
	 ** <b>1.Chrhash</b><br>
     * ��ChrID��--ChrList-- GeneInforList(GffDetail��,,ʵ����GffDetailCG����)
     * ����ChrIDΪСд������Ⱦɫ�����֣������get����ȡ��Ӧ��ChrList��ʱ��Ҫ����Сд��ChrID, chr��ʽ��ȫ��Сд chr1,chr2,chr11<br>
     *  <b>2.locHashtable</b><br>
     * ��LOCID��--GffDetail������LOCID����������Ŀ���,,�����ж���Ϊ��107_chr1_CpG: 128_36568608 <br/>
     *  * ����Ϊ��#bin_chrom_name_chromStart<br>
     *  <b>3.LOCIDList</b><br>
     * ��LOCID��--LOCIDList����˳�򱣴�LOCID<br>
     * <b>LOCChrHashIDList </b><br>
     *  LOCChrHashIDList�б���LOCID����������Ŀ���,��Chrhash�������һ��<br>
     *
	 * @throws Exception 
	 */
	public void ReadGffarrayExcep(String gfffilename) throws Exception 
 {
		GffHashCG gffHashCG = new GffHashCG();
		// ʵ����������
		locHashtable = new HashMap<String, GffDetailCG>();// �洢ÿ��LOCID���������Ϣ�Ķ��ձ�
		Chrhash = new LinkedHashMap<String, ListAbsSearch<GffDetailCG>>();// һ����ϣ�����洢ÿ��Ⱦɫ��
		LOCIDList = new ArrayList<String>();// ˳��洢ÿ������ţ��������������ȡ��������
		// Ϊ���ļ���׼��
		TxtReadandWrite txtgff = new TxtReadandWrite(gfffilename, false);
		BufferedReader reader = txtgff.readfile();// open gff file

		String[] ss = null;// �洢�ָ��������ʱ����
		String content = "";
		// ��ʱ����
		ListAbsSearch<GffDetailCG> LOCList = null;// ˳��洢ÿ��loc�ľ�����Ϣ��һ��Ⱦɫ��һ��LOCList�����װ��Chrhash����
		String chrnametmpString = ""; // Ⱦɫ�����ʱ����

		reader.readLine();// ������һ��
		while ((content = reader.readLine()) != null)// ������β
		{
			ss = content.split("\t");
			chrnametmpString = ss[1].toLowerCase();// Сд
			// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// �µ�Ⱦɫ��
			if (!Chrhash.containsKey(chrnametmpString)) // �µ�Ⱦɫ��
			{
				if (LOCList != null)// ����Ѿ�������LOCList��Ҳ����ǰһ��LOCList����ô�Ƚض̣�Ȼ��������gffGCtmpDetail.numberstart����
				{
					// ���ռ���һ��list/array����ķ������ܼ�����
					Collections.sort(LOCList, new Comparator<ListDetailAbs>() {
						public int compare(ListDetailAbs arg0,
								ListDetailAbs arg1) {
							int Compareresult;
							if (arg0.getStartAbs() < arg1.getStartAbs())
								Compareresult = -1;
							else if (arg0.getStartAbs() == arg1.getStartAbs())
								Compareresult = 0;
							else
								Compareresult = 1;
							return Compareresult;
						}
					});
					// ��������CG��װ��LOCIDList
					for (GffDetailCG gffDetail : LOCList) {
						LOCIDList.add(gffDetail.getName());
					}
				}
				LOCList = new ListAbsSearch<GffDetailCG>();// �½�һ��LOCList������Chrhash
				Chrhash.put(chrnametmpString, LOCList);
			}
			// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// ÿһ�о���һ��CG
			GffDetailCG gffGCtmpDetail = new GffDetailCG(chrnametmpString, ss[0] + "_" + ss[1] + "_" + ss[4] + "_" + ss[2], true);
			gffGCtmpDetail.setStartAbs(Integer.parseInt(ss[2]));
			gffGCtmpDetail.setEndAbs(Integer.parseInt(ss[3]));
			gffGCtmpDetail.lengthCpG = Integer.parseInt(ss[5]);
			gffGCtmpDetail.numCpG = Integer.parseInt(ss[6]);
			gffGCtmpDetail.numGC = Integer.parseInt(ss[7]);
			gffGCtmpDetail.perCpG = Double.parseDouble(ss[8]);
			gffGCtmpDetail.perGC = Double.parseDouble(ss[9]);
			gffGCtmpDetail.obsExp = Double.parseDouble(ss[10]);
			// װ��LOCList��locHashtable
			LOCList.add(gffGCtmpDetail);
			locHashtable.put(gffGCtmpDetail.getName(), gffGCtmpDetail);
		}
		// ///////////////////////////////////////////////////////////////////////////////////////////
		LOCList.trimToSize();
		// �����������Ÿ���
		Collections.sort(LOCList, new Comparator<GffDetailCG>() {
			public int compare(GffDetailCG arg0, GffDetailCG arg1) {
				int Compareresult;
				if (arg0.getStartAbs() < arg1.getStartAbs())
					Compareresult = -1;
				else if (arg0.getStartAbs() == arg1.getStartAbs())
					Compareresult = 0;
				else
					Compareresult = 1;
				return Compareresult;
			}
		});
		// �������װ��LOCIDList
		for (ListDetailAbs gffDetail : LOCList) {
			LOCIDList.add(gffDetail.getName());
		}
		txtgff.close();
		// ///////////////////////////////////////////////////////////////////////////////////////////////
	}

	/**
	 * ����CG��������hash����ʽ����
	 * @return
	 */
	public Hashtable<String, Integer> getLength() 
	{
		int LOCNum=LOCIDList.size();
		Hashtable<String, Integer> hashCGLength=new Hashtable<String, Integer>();
		
		for (int i = 0; i < LOCNum; i++) 
		{
			GffDetailCG gffDetailCG= locHashtable.get(LOCIDList.get(i));
			int tmpLength=gffDetailCG.getEndAbs() - gffDetailCG.getStartAbs();
			String tmpCGClass="CpG";//��ֻ��һ��CpG
			if (hashCGLength.containsKey(tmpCGClass)) //������֪��repeat�����repeat�ĳ����ۼ���ȥ
			{
				tmpLength=tmpLength+hashCGLength.get(tmpCGClass);
				hashCGLength.put(tmpCGClass, tmpLength);
			}
			else//����������µ�repeat�ӽ�ȥ 
			{
				hashCGLength.put(tmpCGClass,tmpLength);
			}
		}
		return hashCGLength;
	}
}
