package com.novelbio.base.dataOperate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
//lang io collections db-pool 
/**
 * ʹ�ñ�����Գ���newһ������󣬽��������ר�ŵ���� һ����ַ����post����get�ύ ����˵���Ҫ������n����վ֮��������ύ�Ļ�����new
 * n������
 * 
 * 
 * 
 */
@SuppressWarnings("unused")
public class WebFetch {

	/**
	 * ���ӳ�ʱ
	 */
	private static int connectionTimeOut = 50000;

	private static int httpconnectionTimeOut = 50000;

	private static int acceptTimeout = 50000;

	private static int socketTimeOut = 50000;

	/**
	 * ò����ÿ�������ĵ�������ӣ�����ʲôҪ��֤
	 */
	private static int maxConnectionPerHost = 5;

	/**
	 * ���������
	 */
	private static int maxTotalConnections = 40;

	/**
	 * ÿ��post��ȴ���ʱ��
	 */
	public int postSleepTime = 4;

	/**
	 * ���ػ���
	 */
	private final static int BUFFER = 1024;

	/**
	 * �趨post�ض���Ĵ�����Ĭ���ض���5��
	 */
	public static int redirectNum = 5;

	// �õ���http��
	private HttpClient httpClient;
	private GetMethod get;
	private GetMethod redirect = new GetMethod();
	private HttpMethod httpFinally;
	/**
	 * ��ʵ����һ��postmethod ��ôÿ��post��Ҫpostͬһ����վ�ȽϺ�
	 */
	private PostMethod post = new PostMethod();
	private MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
	private HttpURL newURL = null;

	/**
	 * Ĭ�ϻ��¼post��get���ص�cookies
	 */
	private Cookie[] cookies;

	/**
	 * coockies���µ��ٶȣ��Է���Ϊ��λ
	 */
	public int CookiesChangeTime = 2;
	/**
	 * ���ڼ�¼ϵͳʱ��
	 */
	private long systemtime = System.currentTimeMillis(); // ��ȡ���ʱ��

	// httpclientĬ��ʹ��ISO-8859-1��ȡhttp��Ӧ�����ݣ���������а���
	// ���ֵĻ��͵ö��ó�ª��new String(str.getBytes("ISO-8859-1"),"GBK");�����

	/**
	 * get�������ݵĽ��뷽ʽ��Ĭ��Ϊ"UTF-8"<br/>
	 * ��get�������ʱ�����޸ģ�ֻҪ����Ҫ�Ľ��뷽ʽ����"GBK"�ȣ�ֱ�Ӹ�ֵ���ñ����ͺ�
	 */
	public String GET_CONTENT_CHARSET = "GBK";// httpclient getfetch
												// ������ȡ����ʱʹ�õ��ַ���

	/**
	 * Post�������ݵĽ��뷽ʽ��Ĭ��Ϊ"UTF-8"<br/>
	 * ��post�������ʱ�����޸ģ�ֻҪ����Ҫ�Ľ��뷽ʽ����"GBK"�ȣ�ֱ�Ӹ�ֵ���ñ����ͺ�
	 */
	public String POST_CONTENT_CHARSET = "UTF-8";// httpclient postfetch
													// ������ȡ����ʱʹ�õ��ַ���
	// private HttpHost proxy =null;// new HttpHost("10.60.8.20", 8080); //��������
	// �õ�����
	// private InputStream instream;

	// ��־��ʼ���Ƿ���ɵ�flag
	private static boolean initialed = false;

	/**
	 * ����httpclient��������������ʱ�� socket��ʱ���� ���������֮���
	 */
	private void SetPara() {
		manager.getParams().setConnectionTimeout(connectionTimeOut); // �����get��ָ��ò��������������get�ύ
		manager.getParams().setSoTimeout(socketTimeOut);
		manager.getParams().setDefaultMaxConnectionsPerHost(
				maxConnectionPerHost);
		manager.getParams().setMaxTotalConnections(maxTotalConnections);
		httpClient = new HttpClient(manager);
		// httpClient.getHostConfiguration().setProxy(hostName,port);�趨
		// ��������ǳ��򵥣�����HttpClient��setProxy�����Ϳ��ԣ������ĵ�һ�������Ǵ����������ַ���ڶ��������Ƕ˿ںš�����HttpClientҲ֧��SOCKS����

		/*
		 * //������Ҫ��֤ UsernamePasswordCredentials creds = new
		 * UsernamePasswordCredentials("chenlb", "123456");
		 * httpClient.getState().setProxyCredentials(AuthScope.ANY, creds);
		 */
		initialed = true;
	}

	/**
	 * �����ύ��httpͷ��Ŀǰģ��IE7.0������� ���ó���Ĭ�ϵĻָ����ԣ��ڷ����쳣ʱ���Զ�����3�Σ����ǲ�֪������ô�ĵ�
	 */
	private void HttpSetting(final HttpMethod method) {
		// ���ó���Ĭ�ϵĻָ����ԣ��ڷ����쳣ʱ���Զ�����3��
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler());

		// ģ��IE7.0�����
		method.setRequestHeader("Content-Type",
				"application/x-www-form-urlencoded");
		method.setRequestHeader("Accept-Language", "zh-cn,zh;q=0.5");
		// method.setRequestHeader("Host", "www.douban.com");
		method.setRequestHeader("Connection", "Keep-Alive");

		method
				.setRequestHeader("User-Agent",
						"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)");// "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)");
		method.setRequestHeader("Accept", "text/html");// "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8 ");//"image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
		// ���������ϰٶȾͻ������
		// method.setRequestHeader("Accept-Encoding",
		// "gzip, deflate");//"x-gzip, gzip, deflate
		method.setRequestHeader("Accept-Charset",
				"gb2312,utf-8,ISO-8859-1;q=0.7,*;q=0.7");
		method.setRequestHeader("UA-CPU", "x86");
		method.setRequestHeader("Connection", "Keep-Alive");
	}

	/**
	 * Method�ύ��׼������ ������ͨ���޸ı����redirectNum�������޸��ض���Ĵ���
	 */
	private HttpMethod MethodReady(HttpMethod methodready, boolean changCookies)// ���ﶼ�����ô��ݣ�����ֻҪ���ص�����һ���Ϳ�����
	{
		/**
		 * ���������method ��Httpͷ
		 */
		HttpSetting(methodready);

		/**
		 * ���ÿ��ܵ��ض����Httpͷ
		 */
		HttpSetting(redirect);

		/**
		 * ����cookies
		 */
		if (cookies != null && changCookies) {
			httpClient.getState().addCookies(cookies);
			System.out.println("��cookies");
		}

		HttpMethod direct = methodready;// ���ﶼ�����ô��ݣ�����ֻҪ���ص�����һ���Ϳ�����

		String newlocation = null;
		int statusCode = -100;

		/**
		 * ת�ƴ���
		 */
		int count = 0;

		boolean flag = false;

		do {
			count++;// ת�ƴ�����1
			try {

				if (count == 1) {
					/**
					 * ��һ���ύ�������methodready�ύ���������ύ���
					 */
					statusCode = httpClient.executeMethod(direct);// ��һ����ԭ�����ύ
					// ����cookies�������ǣ�û��cookies�ļ� ��
					// ������µ�cookies�ļ��Ҿ����ϴθ���cookies�Ѿ�����Ԥ��ʱ��
					if (changCookies
							|| cookies == null
							|| (httpClient.getState().getCookies() != null && systemtime
									- System.currentTimeMillis() > CookiesChangeTime * 60 * 1000)) {
						cookies = httpClient.getState().getCookies();

						systemtime = System.currentTimeMillis();
					}
					/**
					 * �����Ҫ״̬ת�ƣ����������Ĳ���������Ҫ״̬ת�ƵĻ�����flag����Ϊtrue�ͺ�
					 */
					if ((statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
							|| (statusCode == HttpStatus.SC_MOVED_PERMANENTLY)
							|| (statusCode == HttpStatus.SC_SEE_OTHER)
							|| (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
						/**
						 * ��ͷ��ȡ��ת��ĵ�ַ
						 */
						Header locationHeader = direct
								.getResponseHeader("location");
						newlocation = null;
						if (locationHeader != null) {
							/**
							 * ���ͷ���ǿյģ���ô����µ�ת������
							 */
							newlocation = locationHeader.getValue();
							if ((newlocation == null)
									|| (newlocation.equals(""))) {
								/**
								 * ���û�д�ͷ���������ӣ���ô�ܿ��ܾ���û������<br/>
								 * ��ô�µ����Ӻ��ϵ�������ͬ
								 */
								newlocation = direct.getURI().toString();
							}
							newURL = new HttpURL(newlocation);
							/**
							 * blast��ʱ��һ���µ�ҳ�涼�Ǽ�ʱ��������ģ�
							 * Ϊ��ֹ�µ�����û�м�ʱ���ɣ���ô����Ҫ�ó���ȴ�һ��ʱ��
							 */
							Thread.sleep(1000 * postSleepTime);

							/**
							 * ���ﲻ֪���ǵڼ���ת�ơ���post�ύʱ�����û��ת�ƣ�
							 * ��ôdirect����PostMethod���� �Ͳ���ת���GetMethod����<br/>
							 * ����Ѿ�ת�ƹ�����ô����direct����GetMethod�࣬���ܹ������ض������redirect
							 */
							try {
								redirect = (GetMethod) direct;
								redirect.setURI(newURL);
							}
							/**
							 * ����ǵ�һ��ת�ƣ���ô���趨һ���µ��ض������
							 */
							catch (Exception e) {

								redirect.setURI(newURL);
								// System.out.println(redirect.getURI().toString());
							}

						} else {
							System.err.println("Location field value is null.");
							return null;
						}
					} else {
						/**
						 * ����Ҫ״̬ת�ƵĻ�����flag����Ϊtrue�ͺ�
						 */
						flag = true;
						break;
					}
				}
				/**
				 * �ڶ����ύ��redirect������У��������ύ���
				 */
				else {
					statusCode = httpClient.executeMethod(redirect);// �ڶ������µ�url��ַ
					/**
					 * �����Ҫ״̬ת��
					 */
					if ((statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
							|| (statusCode == HttpStatus.SC_MOVED_PERMANENTLY)
							|| (statusCode == HttpStatus.SC_SEE_OTHER)
							|| (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
						/**
						 * ��ͷ��ȡ��ת��ĵ�ַ
						 */
						Header locationHeader = redirect
								.getResponseHeader("location");
						newlocation = null;
						if (locationHeader != null) {
							newlocation = locationHeader.getValue();// �µ�ת������

							if ((newlocation == null)
									|| (newlocation.equals(""))) {
								/**
								 * ���û�д�ͷ���������ӣ���ô�ܿ��ܾ���û������<br/>
								 * ��ô�µ����Ӻ��ϵ�������ͬ
								 */
								newlocation = redirect.getURI().toString();
							}
							newURL = new HttpURL(newlocation);
							redirect.setURI(newURL);
							// direct.releaseConnection();//���Խ��ϵĹر�
							// direct.abort();
							// HttpSetting(direct);//���µ�׼����
							// direct=redirect;//�����ô��ݸ��ϵ�

							// System.out.println("Redirect:"+
							// redirect.getStatusLine().toString());

						} else {
							System.err.println("Location field value is null.");
							return null;
						}
					} else {
						/**
						 * ����Ҫ״̬ת�ƵĻ�����flag����Ϊtrue�ͺ�
						 */
						flag = true;
						break;
					}
				}
			} catch (Exception e) {

				System.out.println(e.toString());

			}
		} while (count < redirectNum);
		/**
		 * ����Ѿ�����Ҫ״̬ת����
		 */
		if (flag) {
			if (count == 1) {
				return direct;
			}
			return redirect;
		}
		return null;
	}

	/**
	 * ����url���ӣ�ͨ��get�ύ������bufferreader���ļ�
	 * 
	 * @param url
	 * @param ChangeCookies
	 *            �Ƿ�ı�cookies
	 * @return
	 */
	public BufferedReader GetFetch(String url, boolean ChangeCookies) {
		/**
		 * ����httpclient��������������ʱ�� socket��ʱ���� ���������֮���
		 */
		SetPara();
		// httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		// httpClient.getHostConfiguration().setHost(LOGON_SITE, LOGON_PORT);
		get = new GetMethod(url);
		get.setFollowRedirects(true);
		try {
			// httpFinally = MethodReady(get);

			/**
			 * ������һ��direct��GetMethod����
			 */
			get = (GetMethod) MethodReady(get, ChangeCookies);

			// httpClient.executeMethod( httpFinally );
			InputStream instream = get.getResponseBodyAsStream();
			// ��Ŀ��ҳ�����δ֪�������£����Ƽ�ʹ��getResponseBodyAsString()����
			// String strGetResponseBody = post.getResponseBodyAsString();

			/**
			 * ��ȡ��Ϣ
			 */
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					instream, GET_CONTENT_CHARSET)); // ��ӽ��뷽ʽ
			return reader;
		}

		/**
		 * ������Ļ����ͳ����ͷ�����
		 */
		catch (Exception e) {
			try {
				get.releaseConnection();
				get.abort();
			} catch (Exception e2) {
			}

			return null;
		}
	}

	/**
	 * ����һЩС�Ķ�����ͼƬ֮��ģ�������ַ�ͱ����ļ��У��Զ�������ɡ�
	 * 
	 * @param url
	 *            �������ļ�����ַ
	 * @param changeCookies
	 *            �Ƿ�װ���µ�cookies
	 * @param saveFilePath
	 *            ��������ļ��У�Ĭ��������"/"����"/media/"֮��
	 */
	public void getDownLoad(String url, String saveFilePath,
			boolean changCookies) {
		// ��ȡurl�е��ļ�����Ϊ���"/"�����������
		String[] stUrlSplit = url.split("/");
		String stFileName = stUrlSplit[stUrlSplit.length - 1];

		/**
		 * ����httpclient��������������ʱ�� socket��ʱ���� ���������֮���
		 */
		SetPara();
		get = new GetMethod(url);
		get.setFollowRedirects(true);

		try {
			/**
			 * ������һ��direct��GetMethod����
			 */
			get = (GetMethod) MethodReady(get, changCookies);
			InputStream instream = get.getResponseBodyAsStream();
			FileOutputStream out = new FileOutputStream(new File(saveFilePath
					+ stFileName));

			byte[] b = new byte[BUFFER];
			int len = 0;
			while ((len = instream.read(b)) != -1) {
				out.write(b, 0, len);
			}
		}
		/**
		 * ������Ļ����ͳ����ͷ�����
		 */
		catch (Exception e) {
			try {
				get.releaseConnection();
				get.abort();
			} catch (Exception e2) {
			}
		}

	}

	/**
	 * ����post�ύ����
	 */
	private PostMethod PostData(PostMethod postMethods, String[][] postData) {
		int length = postData.length;
		NameValuePair[] httpPostData = new NameValuePair[length];
		for (int i = 0; i < length; i++) {
			httpPostData[i] = new NameValuePair(postData[i][0], postData[i][1]);
		}
		postMethods.setRequestBody(httpPostData);
		return postMethods;
	}

	/**
	 * Post�ύ������PostContent��url<br/>
	 * ��󷵻�bufferReader��������<br/>
	 * ���غ����˹��鿴�±����Ƿ���ȷ�����еĻ��޸ı����� POST_CONTENT_CHARSET ����
	 * 
	 * @param postContent
	 *            �����Ҫpost�����ݣ���һ����άstring������ʽ 0��post����Ŀ 1��post������
	 * @param changeCookies
	 *            �Ƿ�װ���µ�cookies
	 * @param url
	 *            ��post��url
	 * @param ������Ӳ���
	 *            getCookies �Ƿ���cookies
	 * @return bufferReader���ļ�
	 */
	@SuppressWarnings("deprecation")
	public BufferedReader PostFetch(String[][] postContent, String url,
			boolean changeCookies) {
		/**
		 * ����httpclient��������������ʱ�� socket��ʱ���� ���������֮���
		 */
		SetPara();
		URI uri;
		try {
			uri = new URI(url);
			post.setURI(uri);
		} catch (URIException e2) {
		}
		/**
		 * ��ȡpost����Ϣ
		 */
		post = PostData(post, postContent);
		try {
			httpFinally = MethodReady(post, changeCookies);
			InputStream instream = httpFinally.getResponseBodyAsStream();
			// ��Ŀ��ҳ�����δ֪�������£����Ƽ�ʹ��getResponseBodyAsString()����
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					instream, POST_CONTENT_CHARSET)); // ��ӽ��뷽ʽ
			return reader;
		} catch (Exception e) {
			/**
			 * ɶҲû�ɣ��Ƿ���Ҫ�ر����ӣ�
			 */
		}
		return null;
	}

	private static String ConverterStringCode(String source, String srcEncode,
			String destEncode) {
		if (source != null) {
			try {
				return new String(source.getBytes(srcEncode), destEncode);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return "";
			}
		} else {
			return "";
		}
	}

	/**
	 * �ͷ�����
	 */
	public void releaseConnection() {
		try {
			get.releaseConnection();
		} catch (Exception e) {
		}
		try {
			httpFinally.releaseConnection();
		} catch (Exception e) {
		}

	}

	public void close() {
		try {
			get.releaseConnection();
		} catch (Exception e) {
		}
		try {
			get.abort();
		} catch (Exception e) {
		}
		try {
			httpFinally.releaseConnection();
		} catch (Exception e) {
		}

		try {
			httpFinally.abort();
		} catch (Exception e) {
		}

	}

	public void closeall() {
		try {
			get.releaseConnection();
		} catch (Exception e) {
		}
		try {
			get.abort();
		} catch (Exception e) {
		}
		try {
			httpFinally.releaseConnection();
		} catch (Exception e) {
		}

		try {
			httpFinally.abort();
		} catch (Exception e) {
		}
		try {
			manager.shutdown();
		} catch (Exception e) {
		}
		try {
			((MultiThreadedHttpConnectionManager) httpClient
					.getHttpConnectionManager()).shutdown();
		} catch (Exception e) {
		}

	}

}
