package com.novelbio.analysis.seq.genomeNew.gffOperate;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.database.model.modcopeid.CopedID;


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
public class GffHashGeneUCSC extends GffHashGeneAbs{

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
	protected void ReadGffarrayExcep(String gfffilename) throws Exception {
		setTaxID(gfffilename);
		// ʵ�����ĸ���
		Chrhash = new HashMap<String, ArrayList<GffDetailGene>>();// һ����ϣ�����洢ÿ��Ⱦɫ��
		locHashtable = new HashMap<String, GffDetailGene>();// �洢ÿ��LOCID���������Ϣ�Ķ��ձ�
		LOCIDList = new ArrayList<String>();// ˳��洢ÿ������ţ��������������ȡ��������
		LOCChrHashIDList = new ArrayList<String>();

		TxtReadandWrite txtGffRead = new TxtReadandWrite(gfffilename, false);
		BufferedReader readGff = txtGffRead.readfile();

		ArrayList<GffDetailGene> LOCList = null;// ˳��洢ÿ��loc�ľ�����Ϣ��һ��Ⱦɫ��һ��LOCList�����װ��Chrhash����
		String content = "";
		readGff.readLine();// ������һ��
		String chrnametmpString = "";
		// int mm=0;//�����Ķ���
		while ((content = readGff.readLine()) != null) {
			content = content.replace("\"", "");
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
					for (GffDetailGene gffDetail : LOCList) {
						LOCChrHashIDList.add(gffDetail.getLocString());
					}
				}
				LOCList = new ArrayList<GffDetailGene>();// �½�һ��LOCList������Chrhash
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
				lastGffdetailUCSCgene.setLocString(lastGffdetailUCSCgene.getLocString() + "/" + geneInfo[0]);
				if (Math.abs(Integer.parseInt(geneInfo[5]) - Integer.parseInt(geneInfo[6])) <= 1) {
					lastGffdetailUCSCgene.addsplitlist(geneInfo[0], GffGeneIsoInfo.TYPE_GENE_MIRNA);
				}
				else {
					lastGffdetailUCSCgene.addsplitlist(geneInfo[0], GffGeneIsoInfo.TYPE_GENE_MRNA);
				}
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
				String[] allLOCID = lastGffdetailUCSCgene.getLocString().split("/");
				for (int i = 0; i < allLOCID.length; i++) {
					locHashtable.put(allLOCID[i].toLowerCase(), lastGffdetailUCSCgene);
				}
				continue;
			}
			// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// ����»���
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
			locHashtable.put(geneInfo[0].toLowerCase(), gffDetailUCSCgene);
		}
		LOCList.trimToSize();
		// System.out.println(mm);
		for (GffDetailGene gffDetail : LOCList) {
			LOCChrHashIDList.add(gffDetail.getLocString());
		}
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
