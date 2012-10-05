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
	public static void main(String[] args) {
		WebFetch webFetch = new WebFetch();
		webFetch.setUrl("http://www.baidu.com/img/baidu_jgylogo3.gif");
		webFetch.download("/Volumes/DATA/aaaa.gif");
	}
	public static final int HTTPTYPE_POST = 2;
	public static final int HTTPTYPE_GET = 4;
	public static final int HTTPTYPE_DOWNLOAD = 12;
	/**
	 * ���ػ���
	 */
	private final static int BUFFER = 1024;
	
	String url;
	DefaultHttpClient httpclient = new DefaultHttpClient();
	HttpParams httpParams;
	ArrayList<BasicHeader> lsHeaders = new ArrayList<BasicHeader>();
	
	HttpRequestBase httpRequest;
	UrlEncodedFormEntity postEntity;
	
	//���صĶ���
	HttpResponse response;
	/** ����httpclient���Զ�����cookie */
	CookieStore cookieStore;
	InputStream instream;
	
	int methodType = HTTPTYPE_GET;
	private Charset charset;
	
	public WebFetch() {
		initial();
		setHeader();
	}
	private void initial() {
		//�趨����
		httpclient.setHttpRequestRetryHandler(new MyRetryHandler());
		//�趨http query�Ĳ�����
		httpParams = new BasicHttpParams();
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
		lsHeaders.add(new BasicHeader("Accept", "text/html"));
		lsHeaders.add(new BasicHeader("Accept-Charset", "gb2312,utf-8,ISO-8859-1;q=0.7,*;q=0.7"));
		lsHeaders.add(new BasicHeader("UA-CPU", "x86"));
	}
	public void setHttpType(int httpType) {
		this.methodType = httpType;
	}
	/** ��Щ��վƩ��pixiv��������ͼƬʱ��Ҫ������ṩ������ʵ����ӣ����ұ�������ָ�������Ӳ������� */
	public void setRefUrl(String refUrl) {
		lsHeaders.add(new BasicHeader("Referer", refUrl));
	}
	/** ������ַ����ͷ���Բ���http:// */
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
	/** ��ȡ����ҳ��string��ʽ��û�оͷ���"" */
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
	/** �Ƿ�ɹ����� 
	 * @throws IOException 
	 * @throws ClientProtocolException */
	private boolean downloadExp(String fileName) throws ClientProtocolException, IOException {
		//httpstatus codes Ϊ4XX����5XX�ͱ�ʾ����
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
	/** ��ȡ��ҳ���������Ƿ�ɹ���ý�� */
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