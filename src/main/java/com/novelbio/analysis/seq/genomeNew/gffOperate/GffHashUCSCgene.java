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
	public GffHashUCSCgene(String gfffilename) throws Exception {
		super(gfffilename);
		// TODO Auto-generated constructor stub
	}

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
	protected void ReadGffarray(String gfffilename) throws Exception{
		
		//ʵ�����ĸ���
		locHashtable =new Hashtable<String, GffDetailAbs>();//�洢ÿ��LOCID���������Ϣ�Ķ��ձ�
		Chrhash=new Hashtable<String, ArrayList<GffDetailAbs>>();//һ����ϣ�����洢ÿ��Ⱦɫ��
		LOCIDList=new ArrayList<String>();//˳��洢ÿ������ţ��������������ȡ��������
		LOCChrHashIDList=new ArrayList<String>();
		
		TxtReadandWrite txtGffRead=new TxtReadandWrite();
		txtGffRead.setParameter(gfffilename,false, true);
		BufferedReader readGff=txtGffRead.readfile();
		
		ArrayList<GffDetailAbs> LOCList=null ;//˳��洢ÿ��loc�ľ�����Ϣ��һ��Ⱦɫ��һ��LOCList�����װ��Chrhash����
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
					   for (GffDetailAbs gffDetail : LOCList) {
						   LOCChrHashIDList.add(gffDetail.locString);
					   }
				}
				LOCList=new ArrayList<GffDetailAbs>();//�½�һ��LOCList������Chrhash
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
