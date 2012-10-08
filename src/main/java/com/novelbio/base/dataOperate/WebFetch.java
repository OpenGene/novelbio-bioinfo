package com.novelbio.base.dataOperate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

/**
 * һ��ֻ��ѡ��һ�Ҫôpost��Ҫôget
 * 
 * ��û���趨�����ҳ����Ϣ��Ʃ��404����200��
 * 
 * ����query()������һ���Ƿ�ɹ��ı�ǩ��
 * ���ͨ���������readResponse()����download()
 * @author zongjie
 *
 */
public class WebFetch {
	private static Logger logger = Logger.getLogger(WebFetch.class);
	
	public static final int HTTPTYPE_POST = 2;
	public static final int HTTPTYPE_GET = 4;
	public static final int HTTPTYPE_HEAD = 12;
	/**
	 * ���ػ���
	 */
	private final static int BUFFER = 1024;
	
	static PoolingClientConnectionManager cm;
	static {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
		cm = new PoolingClientConnectionManager(schemeRegistry);
		// Increase max total connection to 200
		cm.setMaxTotal(20);//setMaxTotalConnections(200);
		// Increase default max connection per route to 20
		cm.setDefaultMaxPerRoute(10);
		// Increase max connections for localhost:80 to 50
		HttpHost localhost = new HttpHost("locahost", 80);
		cm.setMaxPerRoute(new HttpRoute(localhost), 50);
	}
	
	ArrayList<BasicHeader> lsHeaders = new ArrayList<BasicHeader>();
	
	String url;
	DefaultHttpClient httpclient;
	
	HttpRequestBase httpRequest;
	UrlEncodedFormEntity postEntity;
	
	/** ����httpclient���Զ�����cookie */
	CookieStore cookieStore;

	InputStream instream;
	
	boolean querySucess;
	
	int methodType = HTTPTYPE_GET;
	private Charset charset;
	
	public static WebFetch getInstance() {
		return new WebFetch();
	}
	/** ���ع���һ�����ӳص�webFetch
	 * @param num ֵ������ڵ���1
	 *  */
	public static ArrayList<WebFetch> getInstanceLs(int num) {
		ArrayList<WebFetch> lsResult = new ArrayList<WebFetch>();
		WebFetch webFetch = new WebFetch();
		lsResult.add(webFetch);
		for (int i = 0; i < num - 1; i++) {
			WebFetch webFetch2 = new WebFetch(webFetch.httpclient);
			lsResult.add(webFetch2);
		}
		return lsResult;
	}
	/** �����������webFetch����ͬһ�����ӳص�webFetch */
	public static WebFetch getInstance(WebFetch webFetch) {
		return new WebFetch(webFetch.httpclient);
	}
	private WebFetch() {
		initial(null);
		setHeader();
	}
	private WebFetch(DefaultHttpClient httpClient) {
		initial(httpClient);
		setHeader();
	}
	private void initial(DefaultHttpClient httpClient) {
		if (httpClient != null) {
			this.httpclient = httpClient;
			return;
		}
		httpclient = new DefaultHttpClient(cm);
		//�趨����
		httpclient.setHttpRequestRetryHandler(new MyRetryHandler());
		//�趨http query�Ĳ�����
		HttpParams httpParams = new BasicHttpParams();
		HttpProtocolParamBean paramsBean = new HttpProtocolParamBean(httpParams); 
		paramsBean.setVersion(HttpVersion.HTTP_1_1);
		paramsBean.setContentCharset("UTF8");
		paramsBean.setUseExpectContinue(true);
		httpclient.setParams(httpParams);
		httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		httpclient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
		//�ض���Ĳ��ԣ�����301����302Ҳ�����ض���
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
		lsHeaders.add(new BasicHeader("Accept", "text/html, Accept:image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, application/x-silverlight, */* "));
		lsHeaders.add(new BasicHeader("Accept-Charset", "gb2312,utf-8,ISO-8859-1;q=0.7,*;q=0.7"));
		lsHeaders.add(new BasicHeader("UA-CPU", "x86"));
	}
	/** Ӧ�ò���Ҫ���ã��ڲ����Զ��ж� */
	public void setHttpType(int httpType) {
		this.methodType = httpType;
	}
	/** ��Щ��վƩ��pixiv��������ͼƬʱ��Ҫ������ṩ������ʵ����ӣ����ұ�������ָ�������Ӳ������� */
	public void setRefUrl(String refUrl) {
		lsHeaders.add(new BasicHeader("Referer", refUrl));
	}
	/** ������ַ����ͷ���Բ���http:// */
	public void setUrl(String url) {
		if (url == null) {
			return;
		}
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
		querySucess = false;
	}
	/** �趨post�ύ�Ĳ������趨��Ĭ�ϸ�Ϊpost method */
	public void setPostParam(List<String[]> lsKey2Value) {
		Map<String, String> mapKey2Value = new HashMap<String, String>();
		for (String[] strings : lsKey2Value) {
			mapKey2Value.put(strings[0], strings[1]);
		}
		setPostParam(mapKey2Value);
	}
	/** �趨post�ύ�Ĳ������趨��Ĭ�ϸ�Ϊpost method */
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
	/** ����֮����cookies */
	public CookieStore getCookies() {
		return cookieStore;
	}
	/** �Ƿ�ɹ�query */
	public boolean isQuerySucess() {
		return querySucess;
	}
	/** ��ȡ����ҳ��string��ʽ����ȡ�����򷵻�null */
	public String getResponse() {
		String result = "";
		if (!querySucess) {
			return null;
		}
		for (String content : readResponse()) {
			result = result + content + "\n";
		}
		closeStream();
		return result;
	}
	/** ��������ж�һ���Ƿ�Ϊnull
	 * ���Ϊnull��ʾû�ж�ȡ�ɹ�
	 * @return
	 */
	public Iterable<String> readResponse() {
		try {
			return readResponseExp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * ������ȡ���صĽ��
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
	/** ��÷��ص�bufferReader��
	 * ò�ƻ��Զ��ض����������Ļ������Խ���HttpResponse��ͷ�ļ�������ض����url��Ȼ���ٴ�get����post
	 *  */	
	private BufferedReader getResponseReader() throws ClientProtocolException, IOException {
		if (!querySucess) {
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
	/** �Ƿ�ɹ����� 
	 * @throws IOException 
	 * @throws ClientProtocolException */
	private boolean downloadExp(String fileName) throws ClientProtocolException, IOException {
		if (!querySucess) {
			return false;
		}
		FileOutputStream out = new FileOutputStream(new File(fileName));
		byte[] b = new byte[BUFFER];
		int len = 0;
		while ((len = instream.read(b)) != -1) {
			out.write(b, 0, len);
		}
		instream.close();
		return true;
	}
	/** Ĭ������2�ε�query */
	public boolean query() {
		return query(2);
	}
	/** �������ɴ�,��0-100֮�� */
	public boolean query(int retryNum) {
		if (retryNum <= 0 || retryNum > 100) {
			retryNum = 2;
		}
		try {
			int queryNum = 0;
			while (!querySucess) {
				getResponseExp();
				queryNum ++;
				if (queryNum > retryNum) {
					break;
				}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return querySucess;
	}
	/**
	 * ����null ��ʾû�гɹ�
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private void getResponseExp() throws ClientProtocolException, IOException {
		querySucess = false;
		closeStream();
		instream = null;
		HttpResponse httpResponse = httpclient.execute(getQuery());
		int httpStatus = httpResponse.getStatusLine().getStatusCode();
		
		if (httpStatus/100 == 4 || httpStatus/100 == 5) {
			querySucess = false;
		}
		synchronized (this) {
			cookieStore = httpclient.getCookieStore();
		}
		HttpEntity entity = httpResponse.getEntity();
        ContentType contentType = ContentType.getOrDefault(entity);
        charset = contentType.getCharset();
		if (entity != null) {
			instream = entity.getContent();
		}
		querySucess = true;
	}
	private HttpUriRequest getQuery() {
		if (methodType == HTTPTYPE_GET) {
			httpRequest = new HttpGet(url);
		} else if (methodType == HTTPTYPE_POST) {
			httpRequest = new HttpPost(url);
			((HttpPost)httpRequest).setEntity(postEntity);
			methodType = HTTPTYPE_GET;
			postEntity = null;
		} else if (methodType == HTTPTYPE_HEAD) {
			httpRequest = new HttpHead(url);
		}
		
		httpRequest.setHeaders(lsHeaders.toArray(new BasicHeader[1]));
		return httpRequest;
	}
	
	/** ����httpclient �������ص� */
	private void closeStream() {
		try { instream.close(); } catch (Exception e) { }
		try { httpRequest.releaseConnection(); } catch (Exception e) { }
		try { httpRequest.abort(); } catch (Exception e) { }
	}
	
	public void close() {
		closeStream();
		try { httpclient.getConnectionManager().shutdown(); } catch (Exception e) { }
	}
	/** html���뻹�ܱ��� */
	public static String decode(String inputUrl) {
		String result = "";
		try {
			result = URLDecoder.decode(inputUrl, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error("�������" + inputUrl);
		}
		result = inputUrl.replace("&amp;", "&");
		result = result.replace("&nbsp;", " ");
		return result;
	}
}
/**
 * �������Դ���
 * @author zong0jie
 *
 */
class MyRetryHandler implements HttpRequestRetryHandler {

	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
		   if (executionCount >= 5) {
			      // �������������Դ���,��ô�Ͳ�Ҫ������
			      return false;
			   }
			   if (exception instanceof NoHttpResponseException) {
			      // �������������������,��ô������
			      return true;
			   }
			   if (exception instanceof SSLHandshakeException) {
			      // ��Ҫ����SSL�����쳣
			      return false;
			   }
			   HttpRequest request = (HttpRequest) context.getAttribute(
			ExecutionContext.HTTP_REQUEST);
			   boolean idempotent = !(request instanceof
			   HttpEntityEnclosingRequest);
			   if (idempotent) {
			      // ���������Ϊ���ݵȵ�,��ô������
			      return true;
			   }

		return false;
	}
	
}

class WebFetchIdleConnectionMonitorThread extends Thread {
	private final ClientConnectionManager connMgr;
	private volatile boolean shutdown;

	public WebFetchIdleConnectionMonitorThread(ClientConnectionManager connMgr) {
		super();
		this.connMgr = connMgr;
	}

	@Override
	public void run() {
		try {
			while (!shutdown) {
				synchronized (this) {
					wait(5000);
					// �رչ�������
					connMgr.closeExpiredConnections();
					// ��ѡ�أ��رտ��г���30�������
					connMgr.closeIdleConnections(300, TimeUnit.SECONDS);
				}
			}
		} catch (InterruptedException ex) {
			// ��ֹ
		}
	}

	public void shutdown() {
		shutdown = true;
		synchronized (this) {
			notifyAll();
		}
	}
}
