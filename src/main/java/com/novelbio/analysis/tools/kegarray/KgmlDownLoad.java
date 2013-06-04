package com.novelbio.analysis.tools.kegarray;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.PathNBCDetail;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.model.species.Species;

public class KgmlDownLoad {
	private static final Logger logger = Logger.getLogger(KgmlDownLoad.class);
	
	private static final String ncbiKeggIDurl = "http://www.genome.jp/dbget-bin/get_linkdb_list";
	private static final String keggHomeURL = "www.genome.jp/";
	private static final String pathWayHome = "www.genome.jp/kegg/pathway.html";
	/**一个链接，上面有全部物种，用来创建文件夹*/
	public static String speciseURL = "http://www.genome.jp/kegg-bin/get_htext?htext=br08601_map00600.keg&hier=5";

	//TODO 文件路径宗博设定
	public static String KGML_PATH = "/home/zong0jie/desktop/kgml/KGML.init";
	public String outPath = "";
	/** 装每个pathway的url的list */
	List<String> lsKGMLurl = new ArrayList<String>();
	List<String> lsSpeciesToDownload = new ArrayList<String>();
	
	
	public static void main(String[] args) {
		PathNBCDetail.setWorkSpace("/home/novelbio/桌面/project/database");
		KgmlDownLoad kgmlDownLoad = new KgmlDownLoad();
//		ArrayList<String> lsList = new ArrayList<String>();
//		lsList.add("ssc");
//		lsList.add("bta");
//		lsList.add("dre");
//		lsList.add("gga");
//		lsList.add("hiv");
//		lsList.add("pop");
//		lsList.add("osa");
//		lsList.add("sce");		
//		for (String string : lsList) {
//			kgmlDownLoad.kgmlMain(false, string);
//		}
		List<Species> lsSpecies = new ArrayList<Species>();
		lsSpecies.add(new Species(9606));
		kgmlDownLoad.setSpecies(lsSpecies);
		kgmlDownLoad.loadPathNameAndSpecies("/home/novelbio/桌面/project/KGML/kgmlModify", true);
		kgmlDownLoad.download();
	}
	
	/**
	 * **是否更新KGML
	 * 更新KGML需要比较长的时间
	 * specise 例如：hsa，mmu
	 * @param isUpdate
	 * @param speciseSim
	 */
	public void download() {
		for (String keggName : lsSpeciesToDownload) {
			String outSpeciesPath = FileOperate.addSep(outPath) + keggName + FileOperate.getSepPath();
			 FileOperate.createFolders(outSpeciesPath);
			 for (String kgmlUrl : lsKGMLurl) {
				HttpFetch httpFetch = HttpFetch.getInstance();
				httpFetch.setUri(kgmlUrl);
				if (httpFetch.query(10)) {
					for (String lines : httpFetch.readResponse()) {
						if (lines.contains("<img src=\'/Fig/get_htext/whiteSP.png\'>") && lines.contains(keggName)) {
							String speciseURL;
							try {
								speciseURL = lines.split("<a href=\"")[1].split("\">")[0];
							} catch (Exception e) {
								continue;
							}
							speciseURL = keggHomeURL + speciseURL;
							String KGMLdownLoad =  kgmlURL(speciseURL);
							HttpFetch httpFetch2 = HttpFetch.getInstance();
							httpFetch2.setUri(KGMLdownLoad);
							if (httpFetch2.query(10)) {
								String  fileName = speciseURL.split("\\?")[1] + ".xml";
								httpFetch2.download(outSpeciesPath + fileName);
							}
						}
					}
				}
			 }
			 downloadGeneID2KeggID(keggName, outSpeciesPath);
		}
	}
	
	public void setSpecies(List<Species> lsSpecies) {
		lsSpeciesToDownload = new ArrayList<String>();
		for (Species species : lsSpecies) {
			String keggName = species.getAbbrName();
			if (keggName != null && !keggName.equals("")) {
				lsSpeciesToDownload.add(keggName);
			} else {
				logger.error("出现未知kegg名字的物种, taxID：" + species.getTaxID() + "  name: ");
			}
		}
	}
	
	/**
	 * @param txtFileName 如果该文件不存在，或者为null，则从网上读取最新的url
	 * <b>当txtFileName为null时，update无意义，可以随便设置</b>
	 * @param update 如果为false，并且txtFileName存在，则直接从该文本中读取。否则从网上读取url，并且升级该文本
	 */
	public void loadPathNameAndSpecies(String txtFileName, boolean update) {
		lsKGMLurl = new ArrayList<String>();
		txtFileName = txtFileName + "/KGML.init";
		if (update) {
			if (FileOperate.isFileExist(txtFileName)) {
				FileOperate.delFile(txtFileName);
			}
		}
		
		if (FileOperate.isFileExistAndBigThanSize(txtFileName, 0)) {
			TxtReadandWrite txtRead = new TxtReadandWrite(txtFileName);
			for (String content : txtRead.readlines()) {
				lsKGMLurl.add(content);
			}
			txtRead.close();
			return;
		} else {
			fillLsKGMLurl();
		}
		
		if (update && txtFileName != null && !txtFileName.equals("")) {
			try {
				if(FileOperate.createFolders(FileOperate.getParentPathName(txtFileName))) {
					TxtReadandWrite txtWrite = new TxtReadandWrite(txtFileName, true);
					for (String href : lsKGMLurl) {
						txtWrite.writefileln(href);
					}
					txtWrite.close();
				}
			} catch (Exception e) {}
		}
	}
	
	/**根据KEGGPathway的首页获取url*/
	private void fillLsKGMLurl() {
		HttpFetch httpFetch = HttpFetch.getInstance();
		httpFetch.setUri(pathWayHome);
		httpFetch.query();
		for (String lines : httpFetch.readResponse()) {
			if (lines.contains("href=\"/kegg-bin")) {
				String url = lines.split("\">")[0].split("\"/")[1];
				url = keggHomeURL + url;
				List<String> lsOrganismURL = loadOrganism(url);
				for (String string : lsOrganismURL) {
					lsKGMLurl.add(string);
				}
			}
		}
	}
	
	/** 根据首页点击进去的url，查找到KEGG Organisms的链接，也就是所有物种的链接，写入一个文件，避免以后继续下载的时候花费太长的时间
	 * 以后就从配置文件中读取，节约时间,如果需要更新，那么就需要重新下载配置文件
	 * */
	private List<String> loadOrganism(String url) {
		List<String> lsOrganismURL = new ArrayList<String>();
		HttpFetch httpFetch = HttpFetch.getInstance();
		httpFetch.setUri(url);
		httpFetch.query();
		for (String lines : httpFetch.readResponse()) {
			if (lines.contains("Organism menu")) {
				String organismURL = null;
				try {
					organismURL = lines.split("\">")[0].split("\"/")[1];
				} catch (Exception e) {
					continue;
				}
				organismURL = keggHomeURL + organismURL;
				System.out.println(organismURL);
				lsOrganismURL.add(organismURL);
			}
		}
		return lsOrganismURL;
	}

	/**初始化文件夹，把所有物种的文件夹都建好
	 * 已经存在就不需要创建
	 * */
	private void mkdirFolder() {
		String KGML_XMLPath = KGML_PATH + "KGML_XML";
		FileOperate.createFolders(KGML_XMLPath);
		HttpFetch httpFetch = HttpFetch.getInstance();
		httpFetch.setUri(speciseURL);
		httpFetch.query();
		for (String lines : httpFetch.readResponse()) {
			if (lines.contains("<img src=\'/Fig/get_htext/whiteSP.png\'>")) {
				String speciseF; 
				if (lines.contains("</a>")) {
					speciseF  = lines.split("<img src=\'/Fig/get_htext/whiteSP.png\'>")[1].split("</a>")[0].split("\">")[1];
				}else {
					speciseF = lines.split("<img src=\'/Fig/get_htext/whiteSP.png\'>")[1].split("  ")[0];
				}
				FileOperate.createFolders(KGML_XMLPath + "/" + speciseF);
				System.out.println(speciseF);
			}
		}
	}
	
	/**获取最后下载链接*/
	private String kgmlURL(String URL) {
		HttpFetch httpFetch = HttpFetch.getInstance();
		httpFetch.setUri(URL);
		String KGMLdownLoad = null;
		if (httpFetch.query(10)) {
			for(String  lines : httpFetch.readResponse()){
				if (lines.contains("Download KGML")) {
					KGMLdownLoad = lines.split("<a href=\"")[1].split("\">")[0];
					continue;
				}
			}
		}
		return KGMLdownLoad;
	}
	
	/**
	 * 下载所有的有机体
	 * @param savePath 保存的目录
	 */
	private void downloadGeneID2KeggID(String keggName, String savePath) {
		List<String[]> lsParam = generateParam(keggName);
		HttpFetch httpFetch = HttpFetch.getInstance();
		httpFetch.setUri(ncbiKeggIDurl);
		httpFetch.setPostParam(lsParam);
		String filePath = FileOperate.addSep(savePath) + keggName + "_ncbi-geneid.list";
		if (httpFetch.query()) {
			if(httpFetch.download(filePath)){
				logger.info("下载" + filePath + "成功!");
			}else {
				logger.error("下载" + filePath + "失败!");
			}
		}
	}
	
	/** 用于post提交的信息 */
	private List<String[]> generateParam(String keggName) {
		List<String[]> lsKey2Value = new ArrayList<String[]>(); 
		lsKey2Value.add(new String[] { "page", "download" });
		lsKey2Value.add(new String[] { "u", "uniq" });
		lsKey2Value.add(new String[] { "t", "ncbi-geneid" });
		lsKey2Value.add(new String[] { "targetformat", "" });
		lsKey2Value.add(new String[] { "m", keggName});
		return lsKey2Value;
	}
}
