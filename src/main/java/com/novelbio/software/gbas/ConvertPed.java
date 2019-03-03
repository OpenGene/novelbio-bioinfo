package com.novelbio.software.gbas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.stat.correlation.Covariance;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.bioinfo.base.Align;
import com.novelbio.bioinfo.fasta.SeqHash;
import com.novelbio.software.coordtransform.CoordPair;
import com.novelbio.software.coordtransform.CoordTransformer;
import com.novelbio.software.coordtransform.CoordTransformerGenerator;
import com.novelbio.software.coordtransform.VarInfo;
import com.novelbio.software.snpanno.SnpInfo;

/**
 * 将水稻irgsp0.4的map和ped转化为irgsp1.0的
 * @author zong0jie
 * @data 2019年2月21日
 */
public class ConvertPed {

	CoordTransformer coordTransformer;
	
	String mapIn;
	String pedIn;
	
	List<String[]> lsChrId2PosRefAltQ = new ArrayList<>();
	List<String[]> lsChrId2PosRefAltS = new ArrayList<>();

	/** mapIn中哪些位点无法转换的，就需要过滤掉 */
	Set<String> setNotConvert = new HashSet<>();

	/** 缓存，给定Query chrId Pos Snp 然后应该变换成的snp */
	Map<String, String> mapChrPosSnp2Snp = new HashMap<>();
	
	/** 给定Subject chrId Pos Snp 然后获得这个位点的所有变异形式
	 * 这个仅用于最后写bim文件
	 */
	Map<String, Set<String>> mapChrPos2Alt = new HashMap<>();
	

	public static void main2(String[] args) {
		String chain = "/media/winE/mywork/nongkeyuan/coordtransform/IRGSPv4_vs_IRGSP1/irgspvs_vs_irgsp1.changechr.chain";
		String fastaQ = "/media/winE/mywork/nongkeyuan/coordtransform/reference/IRGSP-4.0.chrAll.fa";
		String fastaS = "/media/winE/mywork/nongkeyuan/coordtransform/reference/IRGSP-1.0.chrAll.fasta";
		String fileOut = "/media/winE/mywork/hongjun-gwas/wild-type/out/";
		List<String> lsPedFiles = FileOperate.getLsFoldFileName("/media/winE/mywork/hongjun-gwas/wild-type", "*", "ped");
		String parentPath = "/media/winE/mywork/hongjun-gwas/wild-type/";
		String pathOut = "/media/winE/mywork/hongjun-gwas/wild-type/out/";
		for (String pedIn : lsPedFiles) {
			String fileName = FileOperate.getFileNameWithoutSuffix(pedIn);
			String mapIn = parentPath + fileName + ".map";
			
			String pedOut = pathOut + fileName+".ped";
			String bimOut = pathOut + fileName + ".bim";
			if (FileOperate.isFileExistAndBigThan0(bimOut)) {
				continue;
			}
			SeqHash seqHashQ = new SeqHash(fastaQ);
			Map<String, List<CoordPair>> mapChrId2LsCoordPair = CoordTransformerGenerator.readChainFile(chain);
			CoordTransformer coordTransformer = CoordTransformerGenerator.generateTransformer(mapChrId2LsCoordPair, fastaS);
			
			ConvertPed convertPed = new ConvertPed();
			convertPed.setCoordTransformer(coordTransformer);
			convertPed.setPedIn(pedIn);
			convertPed.setMapIn(mapIn);
			convertPed.run(seqHashQ, pedOut, bimOut);
		}
	}
	
	public void setCoordTransformer(CoordTransformer coordTransformer) {
		this.coordTransformer = coordTransformer;
	}
	public void setMapIn(String mapIn) {
		this.mapIn = mapIn;
	}
	public void setPedIn(String pedIn) {
		this.pedIn = pedIn;
	}
	
	public void run(SeqHash seqHashQ, String pedOut, String bimOut) {
		readMapIn(seqHashQ);
		convertPed(pedOut);
		writeMap(bimOut);
	}
	
	/** 读取输入的map文件，并附上每个位点的ref<br>
	 * @return lsChrId2PosRefAlt<br>
	 * 0: chrId<br>
	 * 1: pos<br>
	 * 2: ref<br>
	 * 3: alt<br>
	 */
	public void readMapIn(SeqHash seqHashQ) {
		TxtReadandWrite txtReadMap = new TxtReadandWrite(mapIn);
		
		for (String content : txtReadMap.readlines()) {
			String[] ss = content.split("\t");
			String chrId = ss[0];
			int pos = Integer.parseInt(ss[3]);
			String ref = seqHashQ.getSeq(chrId, pos, pos).toString();
			SnpInfo snpInfo = coordTransformer.coordTransform(new SnpInfo(chrId, pos, ref, ref));
			if (snpInfo == null || !snpInfo.getRefId().equalsIgnoreCase(chrId)) {
				setNotConvert.add(chrId + "@" + pos);
			} else {
				String key = chrId+"@"+pos+"@"+ref;
				String value = snpInfo.getRefId()+"@"+snpInfo.getAlign().getStart()+"@"+snpInfo.getSeqRef();
				mapChrPosSnp2Snp.put(key.toUpperCase(),  value.toUpperCase());
				lsChrId2PosRefAltS.add(new String[] {snpInfo.getRefId(), snpInfo.getAlign().getStart()+"", snpInfo.getSeqRef().toUpperCase(), ""});
			}
			lsChrId2PosRefAltQ.add(new String[] {chrId, pos+"", ref, ""});
		}
		txtReadMap.close();
	}
	
	public void writeMap(String bimOut) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(bimOut, true);
		for (String[] ss : lsChrId2PosRefAltS) {
			String key = ss[0]+"@"+ss[1];
			Set<String> setInfo = mapChrPos2Alt.get(key.toUpperCase());
			if (setInfo == null) {
				ss[3] = "0";
			} else {
				List<String> lsAlt = new ArrayList<>(setInfo);
				lsAlt.remove(ss[2].toUpperCase());
				if (lsAlt.isEmpty()) {
					ss[3] = "0";
				} else {
					ss[3] = ArrayOperate.cmbString(lsAlt, ",");
				}
			}
			txtWrite.writefileln(ss);
		}
		txtWrite.close();
	}
	
	public void convertPed(String pedOut) {
		TxtReadandWrite txtReadPed = new TxtReadandWrite(pedIn);
		TxtReadandWrite txtWritePed = new TxtReadandWrite(pedOut, true);
		for (String content : txtReadPed.readlines()) {
			String[] ss = content.split(" ");
			txtWritePed.writefile(getPedLineHead(ss));
			
			for (int i = 0; i < lsChrId2PosRefAltQ.size(); i++) {
				String[] chrId2Pos = lsChrId2PosRefAltQ.get(i);
				if (setNotConvert.contains(chrId2Pos[0]+"@"+chrId2Pos[1])) {
					continue;
				}
				
				String allele1 = ss[i*2+6].toUpperCase();
				String allele2 = ss[i*2+7].toUpperCase();
				if (allele1.equals("0") && allele2.equals("0")) {
					txtWritePed.writefile(" 0 0");
					continue;
				}
				String allele1S = copeAllele(chrId2Pos, allele1);
				String allele2S = copeAllele(chrId2Pos, allele2);
				txtWritePed.writefile(" " + allele1S + " " + allele2S);
			}
			txtWritePed.writefileln();
		}
		txtReadPed.close();
		txtWritePed.close();
	}
	
	private String copeAllele(String[] chrId2Pos, String allele) {
		String key = chrId2Pos[0]+"@"+chrId2Pos[1]+"@"+allele;
		if (!mapChrPosSnp2Snp.containsKey(key)) {
			SnpInfo snpAllele1Q = new SnpInfo(chrId2Pos[0], Integer.parseInt(chrId2Pos[1]), chrId2Pos[2], allele);
			SnpInfo snpAllele1S = coordTransformer.coordTransform(snpAllele1Q);
			mapChrPosSnp2Snp.put(key, snpAllele1S.getRefId()+"@"+snpAllele1S.getAlign().getStart()+"@"+snpAllele1S.getSeqAlt().toUpperCase());
		}
		String convert = mapChrPosSnp2Snp.get(key);
		Set<String> setAlt = mapChrPos2Alt.get(getChrPosStr(convert));
		if (setAlt == null) {
			setAlt = new HashSet<>();
			mapChrPos2Alt.put(getChrPosStr(convert), setAlt);
		}
		String alleleConvert = getAlt(convert);
		setAlt.add(alleleConvert);
		return alleleConvert;
	}
	
	/**
	 * 给定 chr1@2345@A@T
	 * 返回 chr1@2345@A
	 * @param convert
	 * @return
	 */
	private String getChrPosStr(String convert) {
		String[] convss = convert.split("@");
		String[] ss = new String[convss.length-1];
		for (int i = 0; i < ss.length; i++) {
			ss[i] = convss[i];
		}
		return ArrayOperate.cmbString(ss, "@");
	}
	/**
	 * 给定 chr1@2345@A@T
	 * 返回 T
	 * @param convert
	 * @return
	 */
	private String getAlt(String convert) {
		String[] convss = convert.split("@");
		return convss[convss.length-1];
	}
	
	/** 获得ped文件的头部
	 * W0101 W0101 0 0 0 -9
	 * 尾部没有空格
	 * @return
	 */
	private String getPedLineHead(String[] ss) {
		List<String> lsHead = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			lsHead.add(ss[i]);
		}
		return ArrayOperate.cmbString(lsHead, " ");
	}
}
