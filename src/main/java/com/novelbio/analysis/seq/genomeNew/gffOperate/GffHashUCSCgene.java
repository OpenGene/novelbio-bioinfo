package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import com.novelbio.base.dataOperate.TxtReadandWrite;


/**
 * �����������Ѿ��趨
 * UCSC��Ĭ���ļ�������ǿ����䣬�յ�Ϊ�����䡣ˮ�����Ͻ�Ļ�û���趨��������
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
	 * �����ж��LOCID����һ�����������������ж����ͬ��LOCIDָ��ͬһ��GffdetailUCSCgene
     *  <b>3.LOCIDList</b><br>
     * ��LOCID�����List˳��洢ÿ������Ż���Ŀ�ţ��������������ȡ�������ţ�ʵ������������Ŀ��˳����룬���ǲ�����ת¼��(UCSC)�����ظ�(Peak) ���ID��locHashһһ��Ӧ�����ǲ���������ȷ��ĳ��Ŀ��ǰһ�����һ����Ŀ <br>
     * <b>4.LOCChrHashIDList </b><br>
     *   LOCChrHashIDList�б���LOCID����������Ŀ���,��Chrhash�������һ�£���ͬһ����Ķ��ת¼������һ����б�߷ָ�"/"�� NM_XXXX/NM_XXXX...<br>
	 * @throws Exception 
	 */
	public void ReadGffarray(String gfffilename) throws Exception {

		// ʵ�����ĸ���
		Chrhash = new HashMap<String, ArrayList<GffDetailAbs>>();// һ����ϣ�����洢ÿ��Ⱦɫ��
		locHashtable = new HashMap<String, GffDetailAbs>();// �洢ÿ��LOCID���������Ϣ�Ķ��ձ�
		LOCIDList = new ArrayList<String>();// ˳��洢ÿ������ţ��������������ȡ��������
		LOCChrHashIDList = new ArrayList<String>();

		TxtReadandWrite txtGffRead = new TxtReadandWrite();
		txtGffRead.setParameter(gfffilename, false, true);
		BufferedReader readGff = txtGffRead.readfile();

		ArrayList<GffDetailAbs> LOCList = null;// ˳��洢ÿ��loc�ľ�����Ϣ��һ��Ⱦɫ��һ��LOCList�����װ��Chrhash����
		String content = "";
		readGff.readLine();// ������һ��
		String chrnametmpString = "";
		// int mm=0;//�����Ķ���
		while ((content = readGff.readLine()) != null) {
			String[] geneInfo = content.split("\t");
			String[] exonStarts = geneInfo[8].split(",");
			String[] exonEnds = geneInfo[9].split(",");
			chrnametmpString = geneInfo[1].toLowerCase();// Сд��chrID
			// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// �µ�Ⱦɫ��
			if (!Chrhash.containsKey(chrnametmpString)) // �µ�Ⱦɫ��
			{
				if (LOCList != null)// ����Ѿ�������LOCList��Ҳ����ǰһ��LOCList����ô�Ƚض̣�Ȼ��������gffGCtmpDetail.numberstart����
				{
					LOCList.trimToSize();
					// ��peak����˳��װ��LOCIDList
					for (GffDetailAbs gffDetail : LOCList) {
						LOCChrHashIDList.add(gffDetail.locString);
					}
				}
				LOCList = new ArrayList<GffDetailAbs>();// �½�һ��LOCList������Chrhash
				Chrhash.put(chrnametmpString, LOCList);
			}
			// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// ���ת¼��
			// ���������ת¼����Ƿ�С���ϸ������ת¼�յ㣬���С�ڣ���˵�����������ϸ������һ��ת¼��
			GffDetailGene lastGffdetailUCSCgene;
			if (LOCList.size() > 0 
					&& // ���ת¼������ͬ���Ǿ��¿�һ��ת¼��
					geneInfo[2].equals("+") == (lastGffdetailUCSCgene = (GffDetailGene) LOCList.get(LOCList.size() - 1)).cis5to3
					&&
					Integer.parseInt(geneInfo[3])+startRegion < lastGffdetailUCSCgene.numberend)
			{
				// �޸Ļ��������յ�
				if (Integer.parseInt(geneInfo[3])+startRegion < lastGffdetailUCSCgene.numberstart)
					lastGffdetailUCSCgene.numberstart = Integer.parseInt(geneInfo[3])+startRegion;
				if (Integer.parseInt(geneInfo[4])+endRegion > lastGffdetailUCSCgene.numberend)
					lastGffdetailUCSCgene.numberend = Integer.parseInt(geneInfo[4])+endRegion;

				// ��������(ת¼��)��IDװ��locString��
				lastGffdetailUCSCgene.locString = lastGffdetailUCSCgene.locString + "/" + geneInfo[0];
				lastGffdetailUCSCgene.addsplitlist(geneInfo[0]);
				// ���һ��ת¼����Ȼ����Ӧ��Ϣ:
				// ��һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region
				// end,�ӵ����ʼ�Ǹ�ת¼����Exon������Ϣ
				lastGffdetailUCSCgene.addATGUAG(Integer.parseInt(geneInfo[5])+startRegion,Integer.parseInt(geneInfo[6])+endRegion);
				
				int exonCount = Integer.parseInt(geneInfo[7]);
				for (int i = 0; i < exonCount; i++) {
					lastGffdetailUCSCgene.addExonUCSC(Integer.parseInt(exonStarts[i])+startRegion, Integer.parseInt(exonEnds[i])+endRegion);
				}
				// ������(ת¼��ID)װ��LOCList
				LOCIDList.add(geneInfo[0]);
				// ��locHashtable����Ӧ����ĿҲ�޸ģ�ͬʱ�����µ���Ŀ
				// ��ΪUCSC����û��ת¼��һ˵��ֻ������LOCID����һ����������������ֻ�ܹ�������ͬ��LOCIDָ��ͬһ��GffdetailUCSCgene
				String[] allLOCID = lastGffdetailUCSCgene.locString.split("/");
				for (int i = 0; i < allLOCID.length; i++) {
					locHashtable.put(allLOCID[i], lastGffdetailUCSCgene);
				}
				continue;
			}
			// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// ����»���
			GffDetailGene gffDetailUCSCgene = new GffDetailGene(chrnametmpString, geneInfo[0], geneInfo[2].equals("+"));
			gffDetailUCSCgene.numberstart = Integer.parseInt(geneInfo[3])+startRegion;
			gffDetailUCSCgene.numberend = Integer.parseInt(geneInfo[4])+endRegion;
			gffDetailUCSCgene.addsplitlist(geneInfo[0]);
			// ���һ��ת¼����Ȼ����Ӧ��Ϣ:
			// ��һ���Ǹ�ת¼����Coding region start���ڶ����Ǹ�ת¼����Coding region
			// end,�ӵ����ʼ�Ǹ�ת¼����Exon������Ϣ
			gffDetailUCSCgene.addATGUAG(Integer.parseInt(geneInfo[5])+startRegion,Integer.parseInt(geneInfo[6])+endRegion);
			int exonCount = Integer.parseInt(geneInfo[7]);
			for (int i = 0; i < exonCount; i++) {
				gffDetailUCSCgene.addExonUCSC(Integer.parseInt(exonStarts[i])+startRegion,Integer.parseInt(exonEnds[i])+endRegion);
			}
			LOCList.add(gffDetailUCSCgene);
			LOCIDList.add(geneInfo[0]);
			locHashtable.put(geneInfo[0], gffDetailUCSCgene);
		}
		LOCList.trimToSize();
		// System.out.println(mm);
		for (GffDetailAbs gffDetail : LOCList) {
			LOCChrHashIDList.add(gffDetail.locString);
		}
		txtGffRead.close();
	}

	@Override
	public GffDetailGene searchLOC(String LOCID) {
		return (GffDetailGene) locHashtable.get(LOCID);
	}

	@Override
	public GffDetailGene searchLOC(String chrID, int LOCNum) {
		return (GffDetailGene) Chrhash.get(chrID).get(LOCNum);
	}

	@Override
	protected GffCodGene setGffCodAbs(String chrID, int Coordinate) {
		return new GffCodGene(chrID, Coordinate);
	}

	
}
