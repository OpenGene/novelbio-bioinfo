package com.novelbio.other.pixiv.execute;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.StringFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.SimpleNodeIterator;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataOperate.WebFetch;
import com.novelbio.base.dataOperate.WebFetchOld;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;


public class PixivOperate {
	public static void main(String[] args) throws ParserException {
		
		PixivOperate pixivOperate = new PixivOperate();
		pixivOperate.setUrlAuther(338118);
		System.out.println(pixivOperate.getPageNum());
	}
	WebFetch webFetchPixiv=new WebFetch();
	String urlAuther;
	
	String name = "facemun";
	String password = "f12344321n";
	
	/** 本作者有多少图片 */
	int allPictureNum = 0;
	/** 总共几页 */
	int allPages = 0;
	
	/** 保存读取的图片队列 */
	ConcurrentLinkedQueue<PixivPicture> queuePixivPictures = new ConcurrentLinkedQueue<PixivPicture>();
	
	PixivOperate() {
		getcookies();
	}
	/**
	 * 获得pixiv的cookies
	 */
    private void getcookies() {
    		if (webFetchPixiv.getCookies() != null) {
    			return;
    		}
    		Map<String, String> mapPostKey2Value = new HashMap<String, String>();
    		mapPostKey2Value.put("mode", "login");
    		mapPostKey2Value.put("pixiv_id", name);
    		mapPostKey2Value.put("pass", password);
    		webFetchPixiv.setPostParam(mapPostKey2Value);
    		webFetchPixiv.setUrl("http://www.pixiv.net/index.php");
    		webFetchPixiv.readResponse();
   }
    /**
     * @param urlAuther 的id
     */
	public void setUrlAuther(int urlAutherid) {
		this.urlAuther = "http://www.pixiv.net/member_illust.php?id=" + urlAutherid;
	}
	/**
	 * 获得总共几页
	 * @return
	 * @throws ParserException 
	 */
	private void setPictureNum_And_PageNum() throws ParserException {
		webFetchPixiv.setUrl(urlAuther);
		webFetchPixiv.query();
		
		String pixivAutherInfo = webFetchPixiv.getResponse();
		Parser parser = new Parser(pixivAutherInfo);
		
		NodeFilter filterNum = new AndFilter(new TagNameFilter("a"), new HasAttributeFilter("class", "active_gray"));
		NodeList nodeListNum = parser.parse(filterNum);
		allPictureNum = getNodeAllPicture(nodeListNum);
		
		NodeFilter filterPage = new AndFilter(new TagNameFilter("div"), new HasAttributeFilter("class", "pages"));
		NodeList nodeListPage = parser.parse(filterPage);
		allPages = getNodeAllPage(nodeListPage);
	}
	/**
	 * 待测试
	 * @param nodeNumLsBefore
	 * @return
	 */
	private int getNodeAllPicture(NodeList nodeNumLsBefore) {
        SimpleNodeIterator iteratorPages = nodeNumLsBefore.elements();
        Node nodeNumBefore = null;
        if (iteratorPages.hasMoreNodes()) {
        	nodeNumBefore = iteratorPages.nextNode();
		}
        Node nodeNum = nodeNumBefore.getNextSibling();
        return Integer.parseInt(nodeNum.toPlainTextString());
	
	}
	private int getNodeAllPage(NodeList nodePage) {
		int pageNum = 1;
		//获得pages的子元素
        SimpleNodeIterator iteratorPages = nodePage.elements();
        NodeList nodePage1 = null;
        if (iteratorPages.hasMoreNodes()) {
        	nodePage1 = iteratorPages.nextNode().getChildren();
		}
        else {
			return 1;
		}
        //提取具体的page
		NodeFilter filterPage = new TagNameFilter("a");
        NodeList nodeListPages = nodePage1.extractAllNodesThatMatch(filterPage, true);
        SimpleNodeIterator iterator = nodeListPages.elements();  
        while (iterator.hasMoreNodes()) {  
            Node node = iterator.nextNode();
            String result = node.toPlainTextString();
            if (result.contains("下一个")) {
				break;
			}
            pageNum = Integer.parseInt(result);
        }
        return pageNum;
	}
	
	
	private void getAll() {
		
	}
    /**
     * 抓取网页中的作者和图片信息，返回二维数组
     * 0：图片
     * 1：作者
     * 如果没抓成功，返回null
     * @param ID
     */
	private String[] execute(String ID) {
		for (String content : webFetchPixiv.readResponse()) {
			
		}
		BufferedReader test=pixiv.GetFetch(url,true);
		String content;
		String[] name;
 	   try {
 		while((content=test.readLine())!=null)
 		   {
 			 //System.out.println(content);
 		/****/ 
 			 if(content.contains("<title>"))
 			 {
 				name=catchID(content);
 				pixiv.releaseConnection();
 				return name;
 			 }
 		   }
 	} catch (IOException e) {
 		e.printStackTrace();
 	}
 	 pixiv.releaseConnection();
 	 return null;
    }
    
    /**
     * 抓取文件信息
     * @param title
     * @return
     */
    private String[] catchID(String title) {
    	    String[] result=new String[2];
    	    Pattern pattern =Pattern.compile("「(.*?)」", Pattern.CASE_INSENSITIVE);  //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
    	    Matcher matcher;//matcher.groupCount() 返回此匹配器模式中的捕获组数。
    	  
    	    matcher = pattern.matcher(title);
    	    if(matcher.find())
    	    result[0]=matcher.group(1);
    	    if(matcher.find())
    	    result[1] = matcher.group(1);      
    	    return result;
	}
    
    /**
     * 输入旧文件夹和新文件夹，将旧文件夹里的图片名字到pixiv网上获得名字放到新文件夹里面，按作者放
     * @param filepath
     * @param newPath
     */
    public void readfile(String filepath,String newPath) {
    	 File a=new File(filepath);
         String[] file=a.list();
         //匹配文件名与后缀名
         Pattern pattern =Pattern.compile("(\\d*?)(_p\\d*){0,1}\\.(\\w*$)", Pattern.CASE_INSENSITIVE);
 	    Matcher matcher;//matcher.groupCount() 返回此匹配器模式中的捕获组数。
 	  
 	   String name="";
 	   String houzhuiming="";
 	   String namep="";
 	   ArrayList<String[]> lsFile = FileOperate.getFoldFileName(filepath);
 	    int ID;//pixiv文件名
 	    
 	    
 	    
        for (int i = 0; i < lsFile.size(); i++) 
        {
		   if (lsFile.get(i)[1] == null || lsFile.get(i)[1].trim().equals(""))
		   {
			  continue;
		   }
		   String oldFile = lsFile.get(i)[0]+"."+lsFile.get(i)[1];
		 //获取文件名与后缀名
		   matcher=pattern.matcher(oldFile);
		  if(matcher.find())
		  {
			  name=matcher.group(1);
			  houzhuiming=matcher.group(3);
			  namep=matcher.group(2);
			  if(namep==null)
			  {
				  namep="";
			  }
		  }
		  else {
			continue;
		}
		  //将文件名改为数字，这个只能pixiv的文件名才能改
		  try {
			   ID=Integer.parseInt(name.trim());
			  
		     } catch (Exception e) {
	
		    	 continue;
		     }  
		   String pixivID=ID+"";
		   String[] filename =  execute(pixivID);  
		   if(filename[0]==null)
		   {
			   continue;
		   }
		   
		   outName = name.replace("\\", "");
		   outName = name.replace("/", "");
		   outName = name.replace("\"", "");
		   outName = name.replace("*", "");
		   outName = name.replace("?", "");
		   outName = name.replace("<", "");
		   outName = name.replace(">", "");
		   outName = name.replace("|", "");
		   
		   
		   filename[1]=filename[1].replace("\\", "");
		   filename[1]=filename[1].replace("/", "");
		   filename[1]=filename[1].replace("\"", "");
		   filename[1]=filename[1].replace("*", "");
		   filename[1]=filename[1].replace("?", "");
		   filename[1]=filename[1].replace("<", "");
		   filename[1]=filename[1].replace(">", "");
		   filename[1]=filename[1].replace("|", "");
		   
		   String oldfilename=filepath+"/"+oldFile;
		   /////String newfilename= name+namep+"."+houzhuiming;
		   //FileOperate.changeFileName(oldfilename, newfilename);//文件改名
		   //FileOperate.moveFile(filepath+"/"+newfilename, newPath+"/"+filename[1]);//移动文件
		   
		   FileOperate.moveFile(oldfilename, newPath+"/"+filename[1],false);//移动文件
		   /////FileOperate.changeFileName(newPath+"/"+filename[1]+"/"+file[i], newfilename);//文件改名
		   
		   System.out.println(name+filename[0]);
		}
    
    }
    
    /**
     * 给定网址，获得所有作者的菜单
     * @param AuthorUrl pixiv某个作者的网址
     * @param SavePath 保存txt文本
     */
    public void downloadPicture(String AuthorUrl,String SavetxtPath) {
    	
    	BufferedReader pixivauther=pixiv.GetFetch(AuthorUrl,true);
    	
    	
    	String tmString="member.php?id=\\d*";
    	TxtReadandWrite pixivtxt=new TxtReadandWrite();
    	pixivtxt.setParameter(SavetxtPath, true,false);
    	String pixivurl="http://www.pixiv.net/";
    	String pixivauthorurl="";
    	//正则表达式抓url
    	Pattern pattern =Pattern.compile("member\\.php\\?id=\\d*", Pattern.CASE_INSENSITIVE);  //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
    	Matcher matcher;//matcher.groupCount() 返回此匹配器模式中的捕获组数。    	
    	try {
    		while( (tmString=pixivauther.readLine())!=null) {
				if(tmString.contains("<a href=\"member.php?id="))
				{
				
					matcher = pattern.matcher(tmString);   
					if(matcher.find())
						pixivauthorurl=pixivurl+matcher.group();  //  返回抓到的字符串
					pixivtxt.writefile(pixivauthorurl);
					pixivtxt.writefile("\r\n");
				}
			}
  
		} catch (Exception e) {}
	}
    
    /**
     * 给定网址，获得所有作者的菜单
     * @param AuthorUrl pixiv某个作者的网址
     * @param SavePath 保存txt文本
     * @throws IOException 
     */
    public void downloadPictureDirectlyTest() throws IOException 
    {
    	pixiv.GetFetch("http://www.pixiv.net/member_illust.php?mode=medium&illust_id=28353378", true);
    	
//    	BufferedReader bufferedReader = pixiv.GetFetch("http://www.pixiv.net/member_illust.php?mode=big&illust_id=28353378", true);
//    	String content = "";
//    	while ((content = bufferedReader.readLine()) != null) {
//    		System.out.println(content);
//		}
    	pixiv.getDownLoad("http://i2.pixiv.net/img50/img/banri620/28353378.jpg", "/media/winF/NBC/Project/Project_ZDB_Lab/HY/BZ_20120521/mappingresult/", true, "http://www.pixiv.net/member_illust.php?mode=big&illust_id=28353378");
	}
}

class PixivPicture {
	/** 我们给每个pixiv的编号，从大到小排列，为了是浏览图片的时候可以方便按照顺序显示图片 */
	int pictureNum;
	/** pixiv的图片ID */
	int pictureID;
	/** 是否是连环画，有子图片 */
	boolean subPicture;
	/** 如果是连环画，子图片的ID*/
	int subID;
	
	String pictureUrl;
	String refUrl;
	
	String savePath;

	String name;
	String auther;
	
	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}
	public void setRefUrl(String refUrl) {
		this.refUrl = refUrl;
	}
	public void setAuther(String auther) {
		this.auther = auther;
	}
	public void setName(String name) {
		this.name = name;
	}
	/** pixiv的图片ID */
	public void setPictureID(int pictureID) {
		this.pictureID = pictureID;
	}
	/** 是否是连环画，有子图片 */
	public void setSubPicture(boolean subPicture) {
		this.subPicture = subPicture;
	}
	/** 我们给每个pixiv的编号，从大到小排列，为了是浏览图片的时候可以方便按照顺序显示图片 */
	public void setPictureNum(int pictureNum) {
		this.pictureNum = pictureNum;
	}
	/** 如果是连环画，子图片的ID*/
	public void setSubID(int subID) {
		this.subID = subID;
	}
	
	 /**
	  * 因为pixiv中的作者名或文件名里面总是有各种奇怪的字符，有些不能成为文件夹名，所以要将他们替换掉
     * 输入旧文件名，将其转变为新文件名
     * @param filepath
     * @param newPath
     */
    private String generateoutName(String name) {
    		String outName;
    		outName = name.replace("\\", "");
    		outName = outName.replace("/", "");
    		outName= outName.replace("\"", "");
    		outName = outName.replace("*", "");
    		outName = outName.replace("?", "");
    		outName = outName.replace("<", "");
    		outName = outName.replace(">", "");
    		outName = outName.replace("|", "");
    		return outName;
    }
    private String getSavePath() {
		String downLoadPath = FileOperate.addSep(savePath) + generateoutName(auther) + FileOperate.getSepPath();
		FileOperate.createFolders(downLoadPath);
		return downLoadPath;
    }
    private String getSaveName() {
    		String saveName = pictureNum + "_" + name + "_" + pictureID;
    		if (subPicture) {
    			saveName = saveName + "_" + subID;
    		}
    		return getSavePath() + saveName;
    }
    public boolean downloadPicture(WebFetch webFetch) {
    		webFetch.setUrl(pictureUrl);
    		webFetch.setRefUrl(refUrl);
    		boolean sucessQuery = webFetch.query();
    		boolean sucessSave = webFetch.download(getSaveName());
    		return sucessQuery && sucessSave;
    }
    
    
}
