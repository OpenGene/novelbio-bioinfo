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
* ע�⣬���ڷ������ݳ���ʱû���ͷ����ӣ�����ÿ�ζ�Ҫ�ֶ�����close���������ͷ�����
* ע������ÿ��ץȥ��ҳʱ��Ҫ�Ա��뷽ʽ���в�������Ӧ��ͬ����ҳ
*/

public class WebFetch4 
{
		private static int connectionTimeOut = 10000;
		private static int httpconnectionTimeOut = 10000;
		private static int acceptTimeout = 5000;
		//private static int maxConnectionPerHost = 5; //�����ʱû�õ�
		private static int maxTotalConnections = 40;
		
		//�õ���http��
		private HttpClient httpClient; 
		private HttpGet httpGet;
		private HttpPost httpPost;
		private HttpResponse httpResponse;
		private HttpEntity httpEntity;
		private HttpParams params;
		private SchemeRegistry schemeRegistry;
		private ThreadSafeClientConnManager multithreadconnect;
		 //httpclientĬ��ʹ��ISO-8859-1��ȡhttp��Ӧ�����ݣ���������а���
	    //  ���ֵĻ��͵ö��ó�ª��new String(str.getBytes("ISO-8859-1"),"GBK");�����
		private static final String GET_CONTENT_CHARSET = "UTF-8";// httpclient getfetch ������ȡ����ʱʹ�õ��ַ���  
		private static final String POST_CONTENT_CHARSET="UTF-8";// httpclient postfetch ������ȡ����ʱʹ�õ��ַ���  
		private HttpHost proxy =null;// new HttpHost("10.60.8.20", 8080); //��������
		//�õ�����
		private InputStream instream;

	    
		
		private String url="";
		private String postContent="";
		//��־��ʼ���Ƿ���ɵ�flag
		private static boolean initialed = false;
	 
	public WebFetch4()
	{
		
		
	}
	
	
	//��ʼ��ConnectionManger�ķ���
	//public static void SetPara() {
	//manager.getParams().setConnectionTimeout(connectionTimeOut);
	//manager.getParams().setSoTimeout(socketTimeOut);
	//manager.getParams()
	//.setDefaultMaxConnectionsPerHost(maxConnectionPerHost);
	//manager.getParams().setMaxTotalConnections(maxTotalConnections);
	//initialed = true;
 
//	}

 /*
  * ����url
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
	
	
	
	
	/*http��������
	 * ���������������ڴ˽���
	 * ���ǽ����������������ʱ��ͱ��뷽ʽ�����ɷ�����¶����
	 * 
	 * 
	 */
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private void httpNormalSetting()
	{
		params = new BasicHttpParams();
		ConnManagerParams.setMaxTotalConnections(params, maxTotalConnections);  //���������
		ConnManagerParams.setTimeout(params, connectionTimeOut);//����ʱ��
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);  // HTTP Э��İ汾,1.1/1.0/0.9  
		HttpProtocolParams.setContentCharset(params, "UTF-8"); // �ַ���  


		ArrayList headers = new ArrayList();
		/*����HTTPͷ
		 * 
		 * αװ�����������  
		 * IE7 ��   HttpComponents/1.1
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

		HttpConnectionParams.setSoTimeout(params, acceptTimeout );//���ճ�ʱ
		HttpConnectionParams.setSocketBufferSize(params, 8*1024);
		HttpConnectionParams.setConnectionTimeout(params, httpconnectionTimeOut); //���ӳ�ʱ
		params.setBooleanParameter(CookieSpecPNames.SINGLE_COOKIE_HEADER, true);
		schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		multithreadconnect = new ThreadSafeClientConnManager(this.params, this.schemeRegistry);
	    httpClient = new DefaultHttpClient(multithreadconnect,params);
	    
	    //httpclientĬ��ʹ��ISO-8859-1��ȡhttp��Ӧ�����ݣ���������а���
	    //  ���ֵĻ��͵ö��ó�ª��new String(str.getBytes("ISO-8859-1"),"GBK");����ˡ� 
	  //  httpClient.getParams().setParameter(HttpProtocolParams.HTTP_CONTENT_CHARSET, CONTENT_CHARSET);  
	
	     
	    httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);  
	  
		
	}
	
	
	
	
	
	
	
	
	
	/*
	 * ͨ��Get����������ҳ
	 * 
	 * 
	 */
	public  BufferedReader GetFetch() 
	{	
		  
		httpNormalSetting();//�����������ã������Ѿ�new��һ��httpClient
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
	    	 //��������������������⣬ÿ���ֶ������޸ı��뷽ʽ
	         BufferedReader reader = new BufferedReader(new InputStreamReader(instream,GET_CONTENT_CHARSET ));
	       return reader;//����bufferreader���ļ������غ��ܶϿ�instream����Ȼ��û�����ˣ�Ȼ����û�ҵ���θ�������
	                     //��ô���ڴ��������ݺ���Ҫ�ֶ�����close�����ر���
	         
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
	 * �����÷�������ʽʱ����
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
	       return reader;//����bufferreader���ļ������غ��ܶϿ�instream����Ȼ��û�����ˣ�Ȼ����û�ҵ���θ�������
	                     //��ô���ڴ��������ݺ���Ҫ�ֶ�����close�����ر���
	         
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
		//httpNormalSetting();//�����������ã������Ѿ�new��һ��httpClient
		httpPost = new HttpPost(url);  
		StringEntity reqEntity=null;
		try {
			reqEntity = new StringEntity(postContent);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}  
		    // ��������  
		reqEntity.setContentType("application/x-www-form-urlencoded");  
	//	reqEntity.setChunked(true);
		     // �������������  
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
		} catch (ClientProtocolException e) //������쳣����֪��ɶ��˼
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
 * �ر�httpclient����
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
	      	 httpClient.getConnectionManager().shutdown();//ͨͨ�Ĺص�
            }
	    catch (Exception e) 
	    {
		  // TODO: handle exception	
	    }
	
	 
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	
	
	
	
	
	
}
