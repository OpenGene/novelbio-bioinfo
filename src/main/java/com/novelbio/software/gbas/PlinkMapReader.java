package com.novelbio.software.gbas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.novelbio.base.ExceptionNbcParamError;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.fileOperate.SeekablePathInputStream;
import com.novelbio.base.util.IOUtil;
import com.novelbio.bioinfo.gff.GffGene;
import com.novelbio.bioinfo.gff.GffHashGene;
import com.novelbio.bioinfo.gff.GffIso;
import com.novelbio.bioinfo.gffchr.GffChrAbs;
import com.novelbio.software.snpanno.EnumVariantClass;
import com.novelbio.software.snpanno.SnpAnnoFactory;
import com.novelbio.software.snpanno.SnpInfo;
import com.novelbio.software.snpanno.SnpIsoHgvsp;
import com.novelbio.software.snpanno.VariantTypeDetector;

/** 按照基因读取PlinkMap
 * 一次读取一个基因中的全体snp
 * @author zong0jie
 * @data 2018年3月10日
 */
public class PlinkMapReader {
	private static final Logger logger = LoggerFactory.getLogger(PlinkMapReader.class);
	
	public static String FILTER_BY_GENE = "FilterByGene";
	public static String FILTER_BY_PVALUE = "FilterByPvalue";

	int tss = 1500;
	
	String filterCriteria = FILTER_BY_GENE;
	
	/**
	 * 每个位点的坐标信息<br>
	 * chrId name	other	location<br>
	 * 1	10100001579	0	1579<br>
	 * 1	10100003044	0	3044<br>
	 * 
	 * 其中第二列和第三列不用管，只需要根据第一列和第四列去提取信息即可
	 */
	String plinkMap;
	
	Map<String, List<GffGene>> mapChrId2LsGenes = new HashMap<>();
	
	TxtReadandWrite txtReadPlinkMap;
	Iterator<String> itPlinkMap;
	Iterator<GffGene> itGenes;
	Allele alleleLast;
	String chrIdTmp;
	int snpIndex = 1;
	
	boolean isFinish = false;
	
	GffGene geneCurrent;
	List<Allele> lsAlleleTmp = new ArrayList<>();
	List<Allele> lsAlleleCurrent = new ArrayList<>();
		
	SnpAnno snpAnno = new SnpAnno();
	public void setTss(int tss) {
		this.tss = tss;
	}
	
	/**
	 * @param filterCriteria
	 * {@link #FILTER_BY_GENE}
	 * {@link #FILTER_BY_PVALUE}
	 */
	public void setFilterCriteria(String filterCriteria) {
		this.filterCriteria = filterCriteria;
	}
	
	public void setGffChrAbs(String chrFile, String gffFile) {
		GffChrAbs gffChrAbs = new GffChrAbs();
		gffChrAbs.setChrFile(chrFile, null);
		gffChrAbs.setGffHash(new GffHashGene(gffFile));
		
		setGenes(gffChrAbs);
		snpAnno.setGffChrAbs(gffChrAbs);
	}
	
	public void setGffChrAbs(GffChrAbs gffChrAbs) {
		setGenes(gffChrAbs);
		snpAnno.setGffChrAbs(gffChrAbs);
	}
	
	private void setGenes(GffChrAbs gffChrAbs) {
		for (GffGene gffDetailGene : gffChrAbs.getGffHashGene().getLsGffDetailGenes()) {		
			List<GffGene> lsGenes = mapChrId2LsGenes.get(gffDetailGene.getChrId());
			if (lsGenes == null) {
				lsGenes = new ArrayList<>();
				mapChrId2LsGenes.put(gffDetailGene.getChrId(), lsGenes);
			}
			GffGene gffDetailGeneTss = new GffGene();
			gffDetailGeneTss.setChrId(gffDetailGene.getChrId());
			gffDetailGeneTss.setName(gffDetailGene.getName() + ".tss");
			gffDetailGeneTss.setCis5to3(gffDetailGene.isCis5to3());
			if (gffDetailGene.isCis5to3()) {
				int start = gffDetailGene.getStartAbs()-tss;
				if (start < 1) start = 1;
				gffDetailGeneTss.setStartAbs(start);
				gffDetailGeneTss.setEndAbs(gffDetailGene.getStartAbs());
				if (tss>0) {
					lsGenes.add(gffDetailGeneTss);
				}
				lsGenes.add(gffDetailGene);
			} else {
				if (tss>0) {
					lsGenes.add(gffDetailGeneTss);
				}
				gffDetailGeneTss.setStartAbs(gffDetailGene.getEndAbs());
				gffDetailGeneTss.setEndAbs(gffDetailGene.getEndAbs() + tss);
				lsGenes.add(gffDetailGene);
			}
		
		}
		for (List<GffGene> lsGenes : mapChrId2LsGenes.values()) {
			Collections.sort(lsGenes, (gene1, gene2) -> {return ((Integer)gene1.getStartAbs()).compareTo(gene2.getStartAbs());});
		}
	}
	
	public void setPlinkMap(String plinkMap) {
		this.plinkMap = plinkMap;
		txtReadPlinkMap = new TxtReadandWrite(plinkMap);
		itPlinkMap = txtReadPlinkMap.readlines().iterator();
	}
	
	public void initial() {
		lsAlleleTmp.clear();
		alleleLast = new Allele(itPlinkMap.next());
		alleleLast.setIndex(snpIndex++);
		chrIdTmp = alleleLast.getChrId();
		itGenes = mapChrId2LsGenes.get(alleleLast.getChrId()).iterator();
	}
	
	public GffGene getGeneCurrent() {
		return geneCurrent;
	}
	/**
	 * 读取一个基因中的全体snp
	 * 尚未测试
	 */
	protected List<Allele> getLsAllelesCurrent() {
		if (StringOperate.isRealNull(filterCriteria)) {
			return lsAlleleCurrent;
		} else if (filterCriteria.equals(FILTER_BY_GENE)) {
			return filterSnpByIso(lsAlleleCurrent, geneCurrent);
		} else if (filterCriteria.equals(FILTER_BY_PVALUE)) {
			return filterSnpByPvalue(lsAlleleCurrent);
		}
		throw new ExceptionNbcParamError("unknown filterCriteria: " + filterCriteria);
	}
	/**
	 * 读取一个基因中的全体snp
	 * 尚未测试
	 */
	public boolean readNext() {
		if (isFinish) {
			return false;
		}
		readNextLsAllele();
		return true;
	}
	
	@VisibleForTesting
	protected void readNextLsAllele() {
		lsAlleleCurrent.clear();
		//读完一条染色体后，根据PlinkMap的内容换下一条染色体
		if (!itGenes.hasNext()) {
			lsAlleleTmp.clear();
			while (itPlinkMap.hasNext()) {
				alleleLast = new Allele(itPlinkMap.next());
				alleleLast.setIndex(snpIndex++);
				if (!chrIdTmp.equals(alleleLast.getChrId())) {
					chrIdTmp = alleleLast.getChrId();
					itGenes = mapChrId2LsGenes.get(alleleLast.getChrId()).iterator();
					lsAlleleTmp.clear();
					alleleLast = null;
					break;
				}
			}
		}
		if (!itPlinkMap.hasNext()) {
			isFinish = true;
			return;
 		}
		List<Allele> lsAllelesResult = new ArrayList<>();
		
		while (itGenes.hasNext()) {
			geneCurrent = itGenes.next();
			if (!lsAlleleTmp.isEmpty() && lsAlleleTmp.get(lsAlleleTmp.size()-1).getPosition() > geneCurrent.getStartAbs()) {
				for (Allele allele : lsAlleleTmp) {
					if (isAlleleLargerThanGene(allele, geneCurrent)) {
						lsAllelesResult.add(allele);
					}
					if (isAlleleInGene(allele, geneCurrent)) {
						lsAlleleCurrent.add(allele);
					}
				}
				lsAlleleTmp.clear();
			}
			if (alleleLast == null 
					|| !alleleLast.getChrId().equals(geneCurrent.getChrId())
					|| alleleLast.getPosition() <= geneCurrent.getEndAbs()
					|| !lsAllelesResult.isEmpty()
					) {
				break;
			}
			geneCurrent = null;
		}
		//说明itGenes已经空了，alleleTmp.getPosition() > 最后一个gene的终点
		if (geneCurrent == null && !isFinish) {
			readNextLsAllele();
			return;
		}
		if (isAlleleInGene(alleleLast, geneCurrent)) {
			lsAllelesResult.add(alleleLast);
			lsAlleleCurrent.add(alleleLast);
		} else if (alleleLast != null && alleleLast.getPosition() > geneCurrent.getEndAbs()) {
			lsAlleleTmp = lsAllelesResult;
			return;
		}

		alleleLast = null;
		while (itPlinkMap.hasNext()) {
			String content = itPlinkMap.next();
			Allele allele = new Allele(content);
			allele.setIndex(snpIndex++);
			if (!allele.getChrId().equals(geneCurrent.getChrId())) {
				alleleLast = allele;
				chrIdTmp = alleleLast.getChrId();
				itGenes = mapChrId2LsGenes.get(alleleLast.getChrId()).iterator();
				if (!lsAllelesResult.isEmpty()) {
					lsAlleleTmp = lsAllelesResult;
					return;
				}
				readNextLsAllele();
				return;
			}
			if (allele.getPosition() < geneCurrent.getStartAbs()) {
				continue;
			}
			if (allele.getPosition() > geneCurrent.getEndAbs()) {
				alleleLast = allele;
				break;
			}
			lsAllelesResult.add(allele);
			lsAlleleCurrent.add(allele);
		}
		lsAlleleTmp = lsAllelesResult;
	}
	
	private boolean isAlleleInGene(Allele allele, GffGene gene) {
		if (allele == null || !allele.getChrId().equals(gene.getChrId())) {
			return false;
		}
		return allele.getPosition() >= gene.getStartAbs() && allele.getPosition() <= gene.getEndAbs();
	}
	
	private boolean isAlleleLargerThanGene(Allele allele, GffGene gene) {
		if (allele == null || !allele.getChrId().equals(gene.getChrId())) {
			return false;
		}
		return allele.getPosition() >= gene.getStartAbs();
	}
	
	/**
	 * 获得改变iso的snp，以及相应的iso的名字
	 * 落在内含子中的snp就不要了
	 * 注意这里如果是tss区域的snp，则都要
	 * @param lsAllele
	 * @param gene
	 * @return
	 */
	private Map<Allele, Set<String>> getMapSnp2SetIsoName(List<Allele> lsAllele, GffGene gene) {
		Map<Allele, Set<String>> mapSnp2SetIsoName = new LinkedHashMap<>();
		for (Allele allele : lsAllele) {
			if (!isAlleleInGene(allele, gene)) {
				continue;
			}
			Set<String> setIsoName = null;
			if (gene.getName().endsWith(".tss")) {
				setIsoName = new HashSet<>();
				setIsoName.add(gene.getName());
			} else {
				setIsoName = snpAnno.getSetIsoName(allele, gene);
			}
			if (!setIsoName.isEmpty()) {
				mapSnp2SetIsoName.put(allele, setIsoName);
			}
		}
		return mapSnp2SetIsoName;
	}
	
	/**
	 * 落在内含子中的snp就不要了
	 * 注意这里如果是tss区域的snp，则都要
	 * @param lsAllele
	 * @param gene
	 * @return
	 */
	private List<Allele> filterSnpByIso(List<Allele> lsAllele, GffGene gene) {
		List<Allele> lsAlleleResult = new ArrayList<>();
		for (Allele allele : lsAllele) {
			if (!isAlleleInGene(allele, gene)) {
				continue;
			}
			if (gene.getName().endsWith(".tss")) {
				lsAlleleResult.add(allele);
			} else {
				Set<String> setIsoName = snpAnno.getSetIsoName(allele, gene);
				if (!setIsoName.isEmpty()) {
					lsAlleleResult.add(allele);
				}
			}
		}
		return lsAlleleResult;
	}
	
	/**
	 * 根据pvalue排序，选择最靠前的10个snp
	 * @param lsAllele pvalue在other中
	 * @param gene
	 * @return
	 */
	private List<Allele> filterSnpByPvalue(List<Allele> lsAllele) {
		List<Allele> lsAlleleResult = new ArrayList<>();
		Collections.sort(lsAllele, new Comparator<Allele>() {
			@Override
			public int compare(Allele o1, Allele o2) {
				Double o1d = Double.parseDouble(o1.getOther());
				Double o2d = Double.parseDouble(o2.getOther());
				return o1d.compareTo(o2d);
			}
		});
		int i = 0;
		for (Allele allele : lsAllele) {
			if ( Double.parseDouble(allele.getOther()) > 0.9) {
				continue;
			}
//			if (i++ > 8) {
//				break;
//			}
			lsAlleleResult.add(allele);
		}
		Collections.sort(lsAlleleResult, new Comparator<Allele>() {
			@Override
			public int compare(Allele o1, Allele o2) {
				Integer o1d = o1.getIndex();
				Integer o2d = o2.getIndex();
				return o1d.compareTo(o2d);
			}
		});
		return lsAlleleResult;
	}
	
	public static void createPlinkMapIndex(String plinkmap) {
		if (FileOperate.isFileExistAndBigThan0(plinkmap+".index")) {
			return;
		}
		List<String[]> lsIndex = createPlinkMapIndexLs(plinkmap);
		TxtReadandWrite txtWrite = new TxtReadandWrite(plinkmap+".index", true);
		for (String[] contents : lsIndex) {
			txtWrite.writefileln(contents);
		}
		txtWrite.close();
	}
	public static Map<String, Integer> readPlinkMapIndexChr2Len(String plinkmap) {
		Map<String, Integer> mapChrId2Len = new HashMap<>();
		if (!plinkmap.endsWith("index")) {
			plinkmap = plinkmap+".index";
		}
		TxtReadandWrite txtReader = new TxtReadandWrite(plinkmap);
		for (String content : txtReader.readlines()) {
			String[] ss = content.split("\t");
			mapChrId2Len.put(ss[0], Integer.parseInt(ss[1]));
		}
		txtReader.close();
		return mapChrId2Len;
	}
	
	public static List<String[]> createPlinkMapIndexLs(String plinkmap) {
		try {
			return createPlinkMapIndexExp(plinkmap);
		} catch (Exception e) {
			throw new ExceptionNbcParamError("read "+plinkmap+"error", e);
		}
	}
	
	/**
	 * index 文件格式如下<br>
	 * chrId chrStart<br>
	 * chrId: 染色体号<br>
	 * chrStart 起点坐标<br>
	 * @param plinkmap 输入plinkmap文件
	 * @throws IOException
	 */
	@VisibleForTesting
	private static List<String[]> createPlinkMapIndexExp(String plinkmap) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(FileOperate.getInputStream(plinkmap)));
		
		//String[] 其中0:chrId  1:chrStart
		List<String[]> lsChr2Start = new ArrayList<>();
		//用来获取 1:SampleStart 2:SampleLocStart 的flag
		
		int result = 0;
		boolean isStart = true;
		
		StringBuilder stringBuilder = new StringBuilder();
		String chrId = null;
		String chrIdLast = null;
		
		long position = 0;
		while ((result = bufferedReader.read()) > 0) {
			char charInfo = (char)result;
			if (charInfo == '\t') {
				isStart = false;
			}
			if (charInfo == '\n') {
				isStart = true;
				stringBuilder = new StringBuilder();
				chrId = null;
			}
			if (isStart) {
				if (charInfo != '\n') {
					stringBuilder.append(charInfo);
				}
			} else if (chrId == null) {
				chrId = stringBuilder.toString();
				if (!chrId.equals(chrIdLast)) {
					chrIdLast = chrId;
					lsChr2Start.add(new String[]{chrId, (position-chrId.length()) + ""});
				}
			}
			position++;
		}
		bufferedReader.close();
		return lsChr2Start;
	}
}


/** plink的文件没有snp信息，需要把snp信息加上去 */
class PlinkResultAnno {
	private static final Logger logger = LoggerFactory.getLogger(PlinkResultAnno.class);

	String bimAnno;
	Map<String, Integer> mapChr2Len;
	
	String plinkResult;
	String plinkResultSort;
	
	String bimAnnoPvalue;
	
	public PlinkResultAnno(String bimAnno, String plinkResult) {
		this.bimAnno = bimAnno;
		this.plinkResult = plinkResult;
		this.plinkResultSort = FileOperate.changeFileSuffix(plinkResult, ".sorted", null);
	}
	
	public void sortFile() {
		PlinkMapReader.createPlinkMapIndex(bimAnno);
		mapChr2Len = PlinkMapReader.readPlinkMapIndexChr2Len(bimAnno);
		if (!FileOperate.isFileExistAndBigThan0(plinkResultSort)) {
			List<String> lsSortPlinkResult = Lists.newArrayList("sort", "-k", "2,2n", "-k", "4,4n", plinkResult, ">", plinkResultSort);
			CmdOperate cmdSortPlinkResult = new CmdOperate(lsSortPlinkResult);
			cmdSortPlinkResult.runWithExp();
		}
	}
	
	public void annoMap() {
		try {
			annoMapExp();
		} catch (Exception e) {
			throw new ExceptionNbcParamError("error", e);
		}
	}
	
	private void annoMapExp() throws IOException {
		String phynotype = FileOperate.getFileName(plinkResult);
		bimAnnoPvalue = FileOperate.changeFileSuffix(bimAnno, "."+phynotype, "anno");
		
		String mapAnnoPvalue = FileOperate.changeFileSuffix(bimAnnoPvalue, ".annoPvalue", null);
		TxtReadandWrite txtReadPlinkResult = new TxtReadandWrite(plinkResultSort);
		
		TxtReadandWrite txtWriteMap = new TxtReadandWrite(mapAnnoPvalue, true);
		
		SeekablePathInputStream is = FileOperate.getSeekablePathInputStream(bimAnno);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
		String mapContent = bufferedReader.readLine();
		String chrId = "";
		//TODO plinkmap的顺序是不能变的，因此需要对其建索引
		try {
			for (String content : txtReadPlinkResult.readlines(2)) {
				String[] plinkResultss = content.split("\t");
				int locPlinkResult = Integer.parseInt(plinkResultss[3]);
				while (true) {
					String[] mapss = mapContent.split("\t");
					int locMap = Integer.parseInt(mapss[3]);
					
					if (!mapss[0].equals(chrId) && !mapss[0].equals(plinkResultss[1])) {
						is.seek(mapChr2Len.get(plinkResultss[1]));
						bufferedReader = new BufferedReader(new InputStreamReader(is));
						mapContent = bufferedReader.readLine();
						mapss = mapContent.split("\t");
						locMap = Integer.parseInt(mapss[3]);
					}
					
					if (mapss[0].equals(plinkResultss[1]) && locPlinkResult < locMap) {
						throw new ExceptionNbcParamError("error on plinkResult line " + content
								+ "\nand map line " + mapContent);
					}
					
					boolean isSame = mapss[0].equals(plinkResultss[1]) &&locPlinkResult== locMap;
					if (isSame) {
						mapContent += "\t" + plinkResultss[4];
						txtWriteMap.writefileln(mapContent);
						mapContent = bufferedReader.readLine();
						break;
					} else {
						txtWriteMap.writefileln(mapContent+"\t"+1);
						mapContent = bufferedReader.readLine();
					}
				}
				chrId = plinkResultss[1];
			}
			
			txtWriteMap.writefileln(mapContent+"\t"+1);
			String content = "";
			while ((content = bufferedReader.readLine()) != null) {
				txtWriteMap.writefileln(content+"\t"+1);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtil.close(is);
			txtReadPlinkResult.close();
			txtWriteMap.close();
		}
		
		IOUtil.close(is);
		txtReadPlinkResult.close();
		txtWriteMap.close();
	}
	
}
