package com.novelbio.other.pixiv.execute;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelBio.base.dataOperate.TxtReadandWrite;
import com.novelBio.base.dataOperate.WebFetch;

 
/**
 * 比较这两个网站，哪个图多选择哪个网站
 * @author zong0jie
 *
 */
public class compDon2Chan {
	static String nameDon = "http://danbooru.donmai.us/post/index?tags=";
	static String nameChan = "http://chan.sankakucomplex.com/post/index?tags=";
	static WebFetch webDon = new WebFetch();
	static WebFetch webChan = new WebFetch();
	
	public static void getMoreNum(String inFile, String outFile,String reset)  {
		TxtReadandWrite txtIn = new TxtReadandWrite();
		TxtReadandWrite txtOut = new TxtReadandWrite();
		TxtReadandWrite txtreset = new TxtReadandWrite();
		txtIn.setParameter(inFile, false,true);
		txtOut.setParameter(outFile, true,false);
		txtreset.setParameter(reset, true,false);
		BufferedReader reader = null;
		try {
			reader = txtIn.readfile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String content = "";
		HashSet<String> hashName = new HashSet<String>();
		try {
			while ((content = reader.readLine()) != null) {
				String ss = content.split("=")[1].trim();
				hashName.add(ss);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String string : hashName) {
			int numDon = 0;	int numChan = 0;
			try {
				numDon = getDonNum(string);
				numChan = getChanNum(string);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					txtreset.writefile(string+"\n");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				continue;
			}
			
			String tmpResult = new String();
			tmpResult = nameDon+string+"\t"+numDon+"\t"+nameChan+string+"\t"+numChan+"\t";
			if (numDon>=numChan) {
				tmpResult=tmpResult+nameDon+string+"\n";
			}
			else {
				tmpResult=tmpResult+nameChan+string+"\n";
			}
			try {
				txtOut.writefile(tmpResult);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	
	
	
	
	
	public static int getDonNum(String name) throws Exception {
		Pattern pattern =Pattern.compile("post-count\">(\\d+)</span></li>", Pattern.CASE_INSENSITIVE);  //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
	    Matcher matcher;//matcher.groupCount() 返回此匹配器模式中的捕获组数。

		String url = nameDon+name;
		BufferedReader reader = webDon.GetFetch(url, true);
		String content = "";
		int Num = 0;
		while ((content = reader.readLine()) != null) 
		{
			if (content.contains("Nobody here but us chicken")) {
				break;
			}
			if (content.contains(name)&&content.contains("<li class=\"tag-type")) 
			{
				matcher = pattern.matcher(content);
				if (matcher.find()) {
					Num = Integer.parseInt(matcher.group(1));
				}
			}
		}
		return Num;
	}
	
	public static int getChanNum(String name) throws Exception {
		Pattern pattern =Pattern.compile("post-count\">(\\d+)</span>", Pattern.CASE_INSENSITIVE);  //flags - 匹配标志，可能包括 CASE_INSENSITIVE、MULTILINE、DOTALL、UNICODE_CASE、 CANON_EQ、UNIX_LINES、LITERAL 和 COMMENTS 的位掩码  // CASE_INSENSITIVE,大小写不敏感，MULTILINE 多行
	    Matcher matcher;//matcher.groupCount() 返回此匹配器模式中的捕获组数。
		String url = nameChan+name;
		BufferedReader reader = webChan.GetFetch(url, true);
		String content = "";
		int Num = 0;
		while ((content = reader.readLine()) != null) 
		{
			if (content.contains("Nothing to display")) {
				break;
			}
			if (content.contains(name)&&content.contains("<li class=\"tag-type")) 
			{
				matcher = pattern.matcher(content);
				if (matcher.find()) {
					Num = Integer.parseInt(matcher.group(1));
				}
			}
		}
		return Num;
	}
	
	
	
}
