package com.novelBio.base.dataOperate;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpConnection;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.commons.*;

import org.apache.http.HttpEntity;
 
import org.apache.http.HttpVersion;
 
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.params.CookieSpecPNames;
import org.apache.http.entity.StringEntity;
 
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

import com.sun.org.apache.bcel.internal.generic.NEW;


/**
* @author steven modified by Zong Jie
* 注意，由于方法传递出来时没法释放连接，所以每次都要手动调用close（）方法释放连接
* 注意这里每次抓去网页时需要对编码方式进行测试以适应不同的网页
*/

public class WebFetch4 
{
		private static int connectionTimeOut = 10000;
		private static int httpconnectionTimeOut = 10000;
		private static int acceptTimeout = 5000;
		//private static int maxConnectionPerHost = 5; //这个暂时没用到
		private static int maxTotalConnections = 40;
		
		//用到的http类
		private HttpClient httpClient; 
		private HttpGet httpGet;
		private HttpPost httpPost;
		private HttpResponse httpResponse;
		private HttpEntity httpEntity;
		private HttpParams params;
		private SchemeRegistry schemeRegistry;
		private ThreadSafeClientConnManager multithreadconnect;
		 //httpclient默认使用ISO-8859-1读取http响应的内容，如果内容中包含
	    //  汉字的话就得动用丑陋的new String(str.getBytes("ISO-8859-1"),"GBK");语句了
		private static final String GET_CONTENT_CHARSET = "UTF-8";// httpclient getfetch 方法读取内容时使用的字符集  
		private static final String POST_CONTENT_CHARSET="UTF-8";// httpclient postfetch 方法读取内容时使用的字符集  
		private HttpHost proxy =null;// new HttpHost("10.60.8.20", 8080); //代理设置
		//用到的流
		private InputStream instream;

	    
		
		private String url="";
		private String postContent="";
		//标志初始化是否完成的flag
		private static boolean initialed = false;
	 
	public WebFetch4()
	{
		
		
	}
	
	
	//初始化ConnectionManger的方法
	//public static void SetPara() {
	//manager.getParams().setConnectionTimeout(connectionTimeOut);
	//manager.getParams().setSoTimeout(socketTimeOut);
	//manager.getParams()
	//.setDefaultMaxConnectionsPerHost(maxConnectionPerHost);
	//manager.getParams().setMaxTotalConnections(maxTotalConnections);
	//initialed = true;
 
//	}

 /*
  * 设置url
  */
	public void Url(String url )
	{
	  	this.url=url;
		return;
	}
	
	public void PostContent(String postContent )
	{
	  	this.postContent=postContent;
		return;
	}
	
	
	
	
	/*http基本设置
	 * 基本参数调整可在此进行
	 * 考虑将最大连接数和连接时间和编码方式等做成方法暴露出来
	 * 
	 * 
	 */
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private void httpNormalSetting()
	{
		params = new BasicHttpParams();
		ConnManagerParams.setMaxTotalConnections(params, maxTotalConnections);  //最大连接数
		ConnManagerParams.setTimeout(params, connectionTimeOut);//连接时间
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);  // HTTP 协议的版本,1.1/1.0/0.9  
		HttpProtocolParams.setContentCharset(params, "UTF-8"); // 字符集  


		ArrayList headers = new ArrayList();
		/*设置HTTP头
		 * 
		 * 伪装的浏览器类型  
		 * IE7 是   HttpComponents/1.1
		 * Mozilla/4.0 (compatible; MSIE 7.0b; Windows NT 6.0)  
		 * Firefox3.03 
		 * Mozilla/5.0 (Windows; U; Windows NT 5.2; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3  
		 * 
		 */
		headers.add(new BasicHeader("User-Agent","Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)")) ;
		headers.add(new BasicHeader("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*"));
	  // headers.add(new BasicHeader("Accept-Encoding", "gzip, deflate"));//"x-gzip, gzip, deflate
        headers.add(new BasicHeader("Accept-Language", "zh-cn,zh;q=0.5"));
		headers.add(new BasicHeader("Accept-Charset", "gb2312,utf-8,ISO-8859-1;q=0.7,*;q=0.7"));
		headers.add(new BasicHeader("UA-CPU", "x86"));
		
		params.setParameter(ClientPNames.DEFAULT_HEADERS, headers);

		HttpClientParams.setCookiePolicy(params, CookiePolicy.BROWSER_COMPATIBILITY);

		HttpConnectionParams.setSoTimeout(params, acceptTimeout );//接收超时
		HttpConnectionParams.setSocketBufferSize(params, 8*1024);
		HttpConnectionParams.setConnectionTimeout(params, httpconnectionTimeOut); //连接超时
		params.setBooleanParameter(CookieSpecPNames.SINGLE_COOKIE_HEADER, true);
		schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		multithreadconnect = new ThreadSafeClientConnManager(this.params, this.schemeRegistry);
	    httpClient = new DefaultHttpClient(multithreadconnect,params);
	    
	    //httpclient默认使用ISO-8859-1读取http响应的内容，如果内容中包含
	    //  汉字的话就得动用丑陋的new String(str.getBytes("ISO-8859-1"),"GBK");语句了。 
	  //  httpClient.getParams().setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET, CONTENT_CHARSET);  
	
	     
	    httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);  
	  
		
	}
	
	
	
	
	
	
	
	
	
	/*
	 * 通过Get方法访问网页
	 * 
	 * 
	 */
	public  BufferedReader GetFetch() 
	{	
		  
		httpNormalSetting();//基本参数设置，其中已经new了一个httpClient
		httpGet = new HttpGet(url);
	    HttpProtocolParams.setUseExpectContinue(params, true); 
	    
	try{
	httpResponse = httpClient.execute(httpGet);
	httpEntity =httpResponse.getEntity();
	}
	catch (Exception e) {
		// TODO: handle exception
	}
	
	if (httpEntity!= null) 
	{
	    
	     try {
	    	 InputStream instream = httpEntity.getContent();
	    	 //最后在这里解决了乱码问题，每次手动尝试修改编码方式
	         BufferedReader reader = new BufferedReader(new InputStreamReader(instream,GET_CONTENT_CHARSET ));
	       return reader;//返回bufferreader流文件，返回后不能断开instream，不然就没东西了，然后我没找到如何复制流，
	                     //那么现在处理完内容后需要手动调用close方法关闭流
	         
	     } catch (IOException ex) {
	 
	         // In case of an IOException the connection will be released
	         // back to the connection manager automatically
	        
	         try{
		         instream.close();
		         throw ex;   
	         }
		         catch (Exception e) {
					// TODO: handle exception
				}
	         
	     } catch (RuntimeException ex) {
	 
	         // In case of an unexpected exception you may want to abort
	         // the HTTP request in order to shut down the underlying 
	         // connection and release it back to the connection manager.
	         
	         try{
	        	 httpGet.abort();
		         httpClient.getConnectionManager().shutdown();
	             instream.close();
	         }
	         catch (Exception e) {
				// TODO: handle exception
			}
	         throw ex;
	     } 
	    }
	httpGet.abort();
	try{
		
        instream.close();
        }
	catch (Exception e) {
		// TODO: handle exception
	}
	return null;
	}
	
	/*
	 * 测试用方法，正式时不用
	 * 
	 * 
	 */
	
	public  BufferedReader GetFetchshort() 
	{	
		httpClient = new DefaultHttpClient();
		
	    
	    	httpGet = new HttpGet(url);
	    
	try{
	httpResponse = httpClient.execute(httpGet);

	httpEntity =httpResponse.getEntity();
	}
	catch (Exception e) {
		// TODO: handle exception
	}

	if (httpEntity!= null) 
	{
	    
	     try {
	    	 InputStream instream = httpEntity.getContent();
	         BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
	       return reader;//返回bufferreader流文件，返回后不能断开instream，不然就没东西了，然后我没找到如何复制流，
	                     //那么现在处理完内容后需要手动调用close方法关闭流
	         
	     } catch (IOException ex) {
	 
	         // In case of an IOException the connection will be released
	         // back to the connection manager automatically
	        
	         try{
		         instream.close();
		         throw ex;   
	         }
		         catch (Exception e) {
					// TODO: handle exception
				}
	         
	     } catch (RuntimeException ex) {
	 
	         // In case of an unexpected exception you may want to abort
	         // the HTTP request in order to shut down the underlying 
	         // connection and release it back to the connection manager.
	         
	         try{
	        	 httpGet.abort();
		         httpClient.getConnectionManager().shutdown();
	             instream.close();
	         }
	         catch (Exception e) {
				// TODO: handle exception
			}
	         throw ex;
	     } 
	    }
	httpGet.abort();
	try{
		
	    instream.close();
	    }
	catch (Exception e) {
		// TODO: handle exception
	}
	return null;
	}

	
	public  BufferedReader PostFetch()
	{
		DefaultHttpClient httpClient = new DefaultHttpClient();  
		//httpNormalSetting();//基本参数设置，其中已经new了一个httpClient
		httpPost = new HttpPost(url);  
		StringEntity reqEntity=null;
		try {
			reqEntity = new StringEntity(postContent);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}  
		    // 设置类型  
		reqEntity.setContentType("application/x-www-form-urlencoded");  
	//	reqEntity.setChunked(true);
		     // 设置请求的数据  
		  List <NameValuePair> nvps = new ArrayList <NameValuePair>();                     
	        nvps.add(new BasicNameValuePair("db", "osa1r5"));   
	        nvps.add(new BasicNameValuePair("orf", "LOC_Os01g01110"));   
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}  
		try {
			 httpResponse= httpClient.execute(httpPost);
			
		     httpEntity = httpResponse.getEntity(); 
		} catch (ClientProtocolException e) //这里的异常处理不知道啥意思
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			httpClient.getConnectionManager().shutdown();
			httpPost.abort();
			return null;
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			httpPost.abort();
			httpClient.getConnectionManager().shutdown();
			
			return null;
		}  
		
		
		 try {
	    	
	         BufferedReader reader = new BufferedReader(new InputStreamReader(instream,POST_CONTENT_CHARSET));
	         return reader;
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				httpPost.abort();
				httpClient.getConnectionManager().shutdown();
				instream.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				httpPost.abort();
				httpClient.getConnectionManager().shutdown();
				instream.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		return null;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
/*
 * 关闭httpclient连接
 */
	public void Close()
	{
		 try{
	         instream.close();
	         }
	         catch (Exception e) {
				// TODO: handle exception
			}
	         
	 try{
         httpGet.abort();
         
         }
         catch (Exception e) {
			// TODO: handle exception
		}
         try{
             httpPost.abort();
             
             }
             catch (Exception e) {
    			// TODO: handle exception
    		}
      try{
	         instream.close();
	         }
	         catch (Exception e) {
				// TODO: handle exception
			}
	    try{
	      	 httpClient.getConnectionManager().shutdown();//通通的关掉
            }
	    catch (Exception e) 
	    {
		  // TODO: handle exception	
	    }
	
	 
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	
	
	
	
	
	
}
