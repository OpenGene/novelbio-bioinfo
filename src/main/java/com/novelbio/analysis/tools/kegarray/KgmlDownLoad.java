package com.novelbio.analysis.tools.kegarray;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.database.domain.species.Species;
import com.novelbio.generalConf.PathDetailNBC;

public class KgmlDownLoad {
	private static final Logger logger = Logger.getLogger(KgmlDownLoad.class);
	
	private static final String ncbiKeggIDurl = "http://www.genome.jp/dbget-bin/get_linkdb_list";
	private static final String keggHomeURL = "www.genome.jp/";
	private static final String pathWayHome = "www.genome.jp/kegg/pathway.html";
	/**一个链接，上面有全部物种，用来创建文件夹*/
//	public static String speciseURL = "http://www.genome.jp/kegg-bin/get_htext?htext=br08601_map00600.keg&hier=5";

	//TODO 文件路径宗博设定
//	public static String KGML_PATH = "/home/zong0jie/desktop/kgml/KGML.init";
	public String outPath = "";
	/** 装每个pathway的url的list */
	List<String> lsKGMLurl = new ArrayList<String>();
	List<String> lsSpeciesToDownload = new ArrayList<String>();
	
	
	public static void main(String[] args) {
		KgmlDownLoad kgmlDownLoad = new KgmlDownLoad();	
		List<Species> lsSpecies = new ArrayList<Species>();
		lsSpecies.add(new Species(9606));
		kgmlDownLoad.setSpecies(lsSpecies);
		kgmlDownLoad.setOutPath("/media/winD/Bioinfor/genome/kgml");
		kgmlDownLoad.loadPathNameAndSpecies("/media/winD/Bioinfor/genome/kgml/kgmlModify", false);
		try {
			kgmlDownLoad.download();
		} catch (Exception e) {
			// TODO: handle exception
		}

//		HttpFetch httpFetch = HttpFetch.getInstance();
//		httpFetch.setUri("www.genome.jp/kegg-bin/show_pathway?map=map00053&show_description=show");
//		httpFetch.query();
//		for (String string : httpFetch.readResponse()) {
//			System.out.println(string);
//		}
	}
	
	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}
	
	/**
	 * **是否更新KGML
	 * 更新KGML需要比较长的时间
	 * specise 例如：hsa，mmu
	 * @param isUpdate
	 * @param speciseSim
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws ClientProtocolException 
	 * @throws ConnectException 
	 */
	public void download() throws ConnectException, ClientProtocolException, UnknownHostException, IOException {
		for (String keggName : lsSpeciesToDownload) {
			String outSpeciesPath = FileOperate.addSep(outPath) + keggName + FileOperate.getSepPath();
			 FileOperate.createFolders(outSpeciesPath);
			 for (String kgmlUrl : lsKGMLurl) {
				HttpFetch httpFetch = HttpFetch.getInstance();
				httpFetch.setUriGet(kgmlUrl);
				httpFetch.queryExp();
				for (String lines : httpFetch.readResponse()) {
					if (lines.contains("<img src=\'/Fig/get_htext/whiteSP.png\'>") && lines.contains(keggName)) {
						String speciseURL;
						try {
							speciseURL = lines.split("<a href=\"")[1].split("\">")[0];
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}
						speciseURL = keggHomeURL + speciseURL;
						String KGMLdownLoad =  kgmlURL(speciseURL);
						HttpFetch httpFetch2 = HttpFetch.getInstance();
						if (KGMLdownLoad == null) {
							continue;
						}
						httpFetch2.setUriGet(KGMLdownLoad);
						httpFetch2.queryExp();
						String  fileName = speciseURL.split("\\?")[1] + ".xml";
						httpFetch2.download(outSpeciesPath + fileName);
						httpFetch2.close();
					}
				}
				httpFetch.close();
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
		if (update) {
			if (FileOperate.isFileExistAndNotDir(txtFileName)) {
				FileOperate.deleteFileFolder(txtFileName);
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
			try {
				fillLsKGMLurl();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}
		
		if (update && txtFileName != null && !txtFileName.equals("")) {
			try {
				FileOperate.createFolders(FileOperate.getParentPathNameWithSep(txtFileName));
				TxtReadandWrite txtWrite = new TxtReadandWrite(txtFileName, true);
				for (String href : lsKGMLurl) {
					txtWrite.writefileln(href);
				}
				txtWrite.close();
			} catch (Exception e) {}
		}
	}
	
	/**根据KEGGPathway的首页获取url
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws ClientProtocolException 
	 * @throws ConnectException */
	private void fillLsKGMLurl() throws ConnectException, ClientProtocolException, UnknownHostException, IOException {
		HttpFetch httpFetch = HttpFetch.getInstance();
		httpFetch.setUriGet(pathWayHome);
		httpFetch.queryExp();
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
		httpFetch.close();
	}
	
	/** 根据首页点击进去的url，查找到KEGG Organisms的链接，也就是所有物种的链接，写入一个文件，避免以后继续下载的时候花费太长的时间
	 * 以后就从配置文件中读取，节约时间,如果需要更新，那么就需要重新下载配置文件
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws ClientProtocolException 
	 * @throws ConnectException 
	 * */
	private List<String> loadOrganism(String url) throws ConnectException, ClientProtocolException, UnknownHostException, IOException {
		List<String> lsOrganismURL = new ArrayList<String>();
		HttpFetch httpFetch = HttpFetch.getInstance();
		httpFetch.setUriGet(url);
		httpFetch.queryExp();
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
				break;
			}
		}
		httpFetch.close();
		return lsOrganismURL;
	}
	
	/**获取最后下载链接
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws ClientProtocolException 
	 * @throws ConnectException */
	private String kgmlURL(String URL) throws ConnectException, ClientProtocolException, UnknownHostException, IOException {
		HttpFetch httpFetch = HttpFetch.getInstance();
		httpFetch.setUriGet(URL);
		String KGMLdownLoad = null;
		httpFetch.queryExp();
		for(String  lines : httpFetch.readResponse()){
			if (lines.contains("Download KGML")) {
				KGMLdownLoad = lines.split("<a href=\"")[1].split("\">")[0];
				break;
			}
			if (lines.contains("form name=\"selmenu\" method=\"get\"")) {
				break;
			}
		}
		httpFetch.close();
		return KGMLdownLoad;
	}
	
	/**
	 * 下载所有的有机体
	 * @param savePath 保存的目录
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws ClientProtocolException 
	 * @throws ConnectException 
	 */
	private void downloadGeneID2KeggID(String keggName, String savePath) throws ConnectException, ClientProtocolException, UnknownHostException, IOException {
		List<String[]> lsParam = generateParam(keggName);
		HttpFetch httpFetch = HttpFetch.getInstance();
		httpFetch.setUriPost(ncbiKeggIDurl, lsParam);
		String filePath = FileOperate.addSep(savePath) + keggName + "_ncbi-geneid.list";
		httpFetch.queryExp();
		httpFetch.download(filePath);
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
