package com.novelbio.base.dataOperate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ParseException;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 * 一次只能选择一项，要么post，要么get
 * 
 * 还没有设定获得网页的信息，譬如404或者200等
 * 
 * 首先query()，返回一个是否成功的标签。
 * 如果通过了则调用readResponse()或者download()
 * @author zongjie
 *
 */
public class WebFetch {
	public static void main(String[] args) {
		WebFetch webFetch = new WebFetch();
		webFetch.setUrl("http://www.baidu.com/img/baidu_jgylogo3.gif");
		webFetch.download("/Volumes/DATA/aaaa.gif");
	}
	public static final int HTTPTYPE_POST = 2;
	public static final int HTTPTYPE_GET = 4;
	public static final int HTTPTYPE_DOWNLOAD = 12;
	/**
	 * 下载缓冲
	 */
	private final static int BUFFER = 1024;
	
	String url;
	DefaultHttpClient httpclient = new DefaultHttpClient();
	HttpParams httpParams;
	ArrayList<BasicHeader> lsHeaders = new ArrayList<BasicHeader>();
	
	HttpRequestBase httpRequest;
	UrlEncodedFormEntity postEntity;
	
	//返回的东西
	HttpResponse response;
	/** 好像httpclient会自动保存cookie */
	CookieStore cookieStore;
	InputStream instream;
	
	int methodType = HTTPTYPE_GET;
	private Charset charset;
	
	public WebFetch() {
		initial();
		setHeader();
	}
	private void initial() {
		//设定重试
		httpclient.setHttpRequestRetryHandler(new MyRetryHandler());
		//设定http query的参数等
		httpParams = new BasicHttpParams();
		HttpProtocolParamBean paramsBean = new HttpProtocolParamBean(httpParams); 
		paramsBean.setVersion(HttpVersion.HTTP_1_1);
		paramsBean.setContentCharset("UTF8");
		paramsBean.setUseExpectContinue(true);
		httpclient.setParams(httpParams);
		httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		httpclient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
		//重定向的策略，遇到301或者302也继续重定向
		 httpclient.setRedirectStrategy(new DefaultRedirectStrategy() {                
		        public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)  {
		            boolean isRedirect=false;
		            try {
		                isRedirect = super.isRedirected(request, response, context);
		            } catch (ProtocolException e) {
		                // TODO Auto-generated catch block
		                e.printStackTrace();
		            }
		            if (!isRedirect) {
		                int responseCode = response.getStatusLine().getStatusCode();
		                if (responseCode == 301 || responseCode == 302) {
		                    return true;
		                }
		            }
		            return isRedirect;
		        }
		    });

	}
	private void setHeader() {
		lsHeaders.clear();
		lsHeaders.add(new BasicHeader("ContentType", "application/x-www-form-urlencoded"));
		lsHeaders.add(new BasicHeader("Accept-Language", "zh-cn,zh;q=0.5"));
		lsHeaders.add(new BasicHeader("Connection", "Keep-Alive"));
		lsHeaders.add(new BasicHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)"));
		lsHeaders.add(new BasicHeader("Accept", "text/html"));
		lsHeaders.add(new BasicHeader("Accept-Charset", "gb2312,utf-8,ISO-8859-1;q=0.7,*;q=0.7"));
		lsHeaders.add(new BasicHeader("UA-CPU", "x86"));
	}
	public void setHttpType(int httpType) {
		this.methodType = httpType;
	}
	/** 有些网站譬如pixiv，在下载图片时需要浏览器提供最近访问的链接，而且必须是其指定的链接才能下载 */
	public void setRefUrl(String refUrl) {
		lsHeaders.add(new BasicHeader("Referer", refUrl));
	}
	/** 输入网址，开头可以不加http:// */
	public void setUrl(String url) {
		if (url.startsWith("//")) {
			url = "http:" + url;
		} else if (url.startsWith("/")) {
			url = "http:/" + url;
		} else if (url.startsWith("http://")) {
			//Nothing will be do
		} else {
			url = "http://" + url;
		}
		this.url = url;
	}
	/** 设定post提交的参数，设定后默认改为post method */
	public void setPostParam(List<String[]> lsKey2Value) {
		Map<String, String> mapKey2Value = new HashMap<String, String>();
		for (String[] strings : lsKey2Value) {
			mapKey2Value.put(strings[0], strings[1]);
		}
		setPostParam(mapKey2Value);
	}
	/** 设定post提交的参数，设定后默认改为post method */
	public void setPostParam(Map<String, String> mapKey2Value) {
		try {
			setPostParamExp(mapKey2Value);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	private void setPostParamExp(Map<String, String> mapKey2Value) throws UnsupportedEncodingException {
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		for (Entry<String, String> key2value : mapKey2Value.entrySet()) {
			nvps.add(new BasicNameValuePair(key2value.getKey(), key2value.getValue()));
		}
		postEntity = new UrlEncodedFormEntity(nvps);
		methodType = HTTPTYPE_POST;
	}
	public void setCookies(CookieStore cookieStore) {
		httpclient.setCookieStore(cookieStore);
	}
	/** 运行之后获得cookies */
	public CookieStore getCookies() {
		return cookieStore;
	}
	/** 读取的网页的string格式，没有就返回"" */
	public String getResponse() {
		String result = "";
		for (String content : readResponse()) {
			result = result + content + "\n";
		}
		return result;
	}
	public Iterable<String> readResponse() {
		try {
			return readResponseExp();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 迭代读取返回的结果
	 * @param filename
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	private Iterable<String> readResponseExp() throws Exception {
		 final BufferedReader bufread =  getResponseReader();
		return new Iterable<String>() {
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					public boolean hasNext() {
						return line != null;
					}
					public String next() {
						String retval = line;
						line = getLine();
						return retval;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
					String getLine() {
						String line = null;
						try {
							line = bufread.readLine();
						} catch (IOException ioEx) {
							line = null;
						}
						if (line == null) {
							closeStream();
						}
						return line;
					}
					String line = getLine();
				};
			}
		};
	}
	/** 获得返回的bufferReader类
	 * 貌似会自动重定向，如果不会的话，可以解析HttpResponse的头文件，获得重定向的url，然后再次get或者post
	 *  */	
	private BufferedReader getResponseReader() throws ClientProtocolException, IOException {
		if (instream == null) {
			return null;
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(instream, charset));
		return reader;
	}
	public boolean download(String fileName) {
		try {
			return downloadExp(fileName);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	/** 是否成功下载 
	 * @throws IOException 
	 * @throws ClientProtocolException */
	private boolean downloadExp(String fileName) throws ClientProtocolException, IOException {
		//httpstatus codes 为4XX或者5XX就表示出错
		if (instream == null) {
			return false;
		}
		FileOutputStream out = new FileOutputStream(new File(fileName));
		byte[] b = new byte[BUFFER];
		int len = 0;
		while ((len = instream.read(b)) != -1) {
			out.write(b, 0, len);
		}
		return true;
	}
	/** 读取网页，并返回是否成功获得结果 */
	public boolean query() {
		int httpStatus = 404;
		try {
			httpStatus = getResponseExp();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (httpStatus/100 == 4 || httpStatus/100 == 5) {
			return false;
		}
		return true;
	}

	private int getResponseExp() throws ClientProtocolException, IOException {
		closeStream();
		HttpResponse httpResponse = httpclient.execute(getQuery());
		int httpstatus = httpResponse.getStatusLine().getStatusCode();
		cookieStore = httpclient.getCookieStore();
		HttpEntity entity = httpResponse.getEntity();
        ContentType contentType = ContentType.getOrDefault(entity);
        charset = contentType.getCharset();
		if (entity != null) {
			instream = entity.getContent();
		}
		return httpstatus;
	}
	private HttpUriRequest getQuery() {
		if (methodType == HTTPTYPE_GET) {
			httpRequest = new HttpGet(url);
		} else if (methodType == HTTPTYPE_POST) {
			httpRequest = new HttpPost(url);
			((HttpPost)httpRequest).setEntity(postEntity);
			methodType = HTTPTYPE_GET;
			postEntity = null;
		}
		httpRequest.setHeaders(lsHeaders.toArray(new BasicHeader[1]));
		httpRequest.setParams(httpParams);
		return httpRequest;
	}
	
	/** 除了httpclient 其他都关掉 */
	private void closeStream() {
		try { instream.close(); } catch (Exception e) { }
		try { httpRequest.releaseConnection(); } catch (Exception e) { }
		try { httpRequest.abort(); } catch (Exception e) { }
	}
	
	public void close() {
		closeStream();
		try { httpclient.getConnectionManager().shutdown(); } catch (Exception e) { }
	}
}
/**
 * 请求重试处理
 * @author zong0jie
 *
 */
class MyRetryHandler implements HttpRequestRetryHandler {

	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
		   if (executionCount >= 5) {
			      // 如果超过最大重试次数,那么就不要继续了
			      return false;
			   }
			   if (exception instanceof NoHttpResponseException) {
			      // 如果服务器丢掉了连接,那么就重试
			      return true;
			   }
			   if (exception instanceof SSLHandshakeException) {
			      // 不要重试SSL握手异常
			      return false;
			   }
			   HttpRequest request = (HttpRequest) context.getAttribute(
			ExecutionContext.HTTP_REQUEST);
			   boolean idempotent = !(request instanceof
			   HttpEntityEnclosingRequest);
			   if (idempotent) {
			      // 如果请求被认为是幂等的,那么就重试
			      return true;
			   }

		return false;
	}
	
}