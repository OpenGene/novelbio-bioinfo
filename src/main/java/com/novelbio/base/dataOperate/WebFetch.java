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
 * 使用本类可以尝试new一个对象后，将这个对象专门的针对 一个网址进行post或者get提交 就是说如果要连续做n个网站之间的数据提交的话，就new
 * n个对象
 * 
 * 
 * 
 */
@SuppressWarnings("unused")
public class WebFetch {

	/**
	 * 连接超时
	 */
	private static int connectionTimeOut = 50000;

	private static int httpconnectionTimeOut = 50000;

	private static int acceptTimeout = 50000;

	private static int socketTimeOut = 50000;

	/**
	 * 貌似是每个主机的的最大连接？具体什么要查证
	 */
	private static int maxConnectionPerHost = 5;

	/**
	 * 最大连接数
	 */
	private static int maxTotalConnections = 40;

	/**
	 * 每次post后等待的时间
	 */
	public int postSleepTime = 4;

	/**
	 * 下载缓冲
	 */
	private final static int BUFFER = 1024;

	/**
	 * 设定post重定向的次数，默认重定向5次
	 */
	public static int redirectNum = 5;

	// 用到的http类
	private HttpClient httpClient;
	private GetMethod get;
	private GetMethod redirect = new GetMethod();
	private HttpMethod httpFinally;
	/**
	 * 先实例化一个postmethod 那么每次post都要post同一个网站比较好
	 */
	private PostMethod post = new PostMethod();
	private MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
	private HttpURL newURL = null;

	/**
	 * 默认会记录post或get返回的cookies
	 */
	private Cookie[] cookies;

	/**
	 * coockies更新的速度，以分钟为单位
	 */
	public int CookiesChangeTime = 2;
	/**
	 * 用于记录系统时间
	 */
	private long systemtime = System.currentTimeMillis(); // 获取最初时间

	// httpclient默认使用ISO-8859-1读取http响应的内容，如果内容中包含
	// 汉字的话就得动用丑陋的new String(str.getBytes("ISO-8859-1"),"GBK");语句了

	/**
	 * get返回内容的解码方式，默认为"UTF-8"<br/>
	 * 当get结果乱码时进行修改，只要将想要的解码方式（如"GBK"等）直接赋值给该变量就好
	 */
	public String GET_CONTENT_CHARSET = "GBK";// httpclient getfetch
												// 方法读取内容时使用的字符集

	/**
	 * Post返回内容的解码方式，默认为"UTF-8"<br/>
	 * 当post结果乱码时进行修改，只要将想要的解码方式（如"GBK"等）直接赋值给该变量就好
	 */
	public String POST_CONTENT_CHARSET = "UTF-8";// httpclient postfetch
													// 方法读取内容时使用的字符集
	// private HttpHost proxy =null;// new HttpHost("10.60.8.20", 8080); //代理设置
	// 用到的流
	// private InputStream instream;

	// 标志初始化是否完成的flag
	private static boolean initialed = false;

	/**
	 * 设置httpclient参数，包括连接时间 socket超时设置 最大连接数之类的
	 */
	private void SetPara() {
		manager.getParams().setConnectionTimeout(connectionTimeOut); // 这里的get是指获得参数，不单单针对get提交
		manager.getParams().setSoTimeout(socketTimeOut);
		manager.getParams().setDefaultMaxConnectionsPerHost(
				maxConnectionPerHost);
		manager.getParams().setMaxTotalConnections(maxTotalConnections);
		httpClient = new HttpClient(manager);
		// httpClient.getHostConfiguration().setProxy(hostName,port);设定
		// 理服务器非常简单，调用HttpClient中setProxy方法就可以，方法的第一个参数是代理服务器地址，第二个参数是端口号。另外HttpClient也支持SOCKS代理

		/*
		 * //代理需要验证 UsernamePasswordCredentials creds = new
		 * UsernamePasswordCredentials("chenlb", "123456");
		 * httpClient.getState().setProxyCredentials(AuthScope.ANY, creds);
		 */
		initialed = true;
	}

	/**
	 * 设置提交的http头，目前模拟IE7.0的浏览器 设置成了默认的恢复策略，在发生异常时候将自动重试3次，但是不知道该怎么改掉
	 */
	private void HttpSetting(final HttpMethod method) {
		// 设置成了默认的恢复策略，在发生异常时候将自动重试3次
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler());

		// 模拟IE7.0浏览器
		method.setRequestHeader("Content-Type",
				"application/x-www-form-urlencoded");
		method.setRequestHeader("Accept-Language", "zh-cn,zh;q=0.5");
		// method.setRequestHeader("Host", "www.douban.com");
		method.setRequestHeader("Connection", "Keep-Alive");

		method
				.setRequestHeader("User-Agent",
						"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)");// "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727)");
		method.setRequestHeader("Accept", "text/html");// "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8 ");//"image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
		// 下面这句加上百度就会变乱码
		// method.setRequestHeader("Accept-Encoding",
		// "gzip, deflate");//"x-gzip, gzip, deflate
		method.setRequestHeader("Accept-Charset",
				"gb2312,utf-8,ISO-8859-1;q=0.7,*;q=0.7");
		method.setRequestHeader("UA-CPU", "x86");
		method.setRequestHeader("Connection", "Keep-Alive");
	}

	/**
	 * Method提交的准备工作 ，可以通过修改本类的redirectNum属性来修改重定向的次数
	 */
	private HttpMethod MethodReady(HttpMethod methodready, boolean changCookies)// 这里都是引用传递，所以只要最后关掉其中一个就可以了
	{
		/**
		 * 设置输入的method 的Http头
		 */
		HttpSetting(methodready);

		/**
		 * 设置可能的重定向的Http头
		 */
		HttpSetting(redirect);

		/**
		 * 设置cookies
		 */
		if (cookies != null && changCookies) {
			httpClient.getState().addCookies(cookies);
			System.out.println("换cookies");
		}

		HttpMethod direct = methodready;// 这里都是引用传递，所以只要最后关掉其中一个就可以了

		String newlocation = null;
		int statusCode = -100;

		/**
		 * 转移次数
		 */
		int count = 0;

		boolean flag = false;

		do {
			count++;// 转移次数加1
			try {

				if (count == 1) {
					/**
					 * 第一次提交用输入的methodready提交，并返回提交情况
					 */
					statusCode = httpClient.executeMethod(direct);// 第一次用原来的提交
					// 更新cookies的条件是：没有cookies文件 或
					// 获得了新的cookies文件且距离上次更新cookies已经过了预定时间
					if (changCookies
							|| cookies == null
							|| (httpClient.getState().getCookies() != null && systemtime
									- System.currentTimeMillis() > CookiesChangeTime * 60 * 1000)) {
						cookies = httpClient.getState().getCookies();

						systemtime = System.currentTimeMillis();
					}
					/**
					 * 如果需要状态转移，则进行下面的操作。不需要状态转移的话，将flag设置为true就好
					 */
					if ((statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
							|| (statusCode == HttpStatus.SC_MOVED_PERMANENTLY)
							|| (statusCode == HttpStatus.SC_SEE_OTHER)
							|| (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
						/**
						 * 从头中取出转向的地址
						 */
						Header locationHeader = direct
								.getResponseHeader("location");
						newlocation = null;
						if (locationHeader != null) {
							/**
							 * 如果头不是空的，那么获得新的转移链接
							 */
							newlocation = locationHeader.getValue();
							if ((newlocation == null)
									|| (newlocation.equals(""))) {
								/**
								 * 如果没有从头里面获得连接，那么很可能就是没有链接<br/>
								 * 那么新的链接和老的链接相同
								 */
								newlocation = direct.getURI().toString();
							}
							newURL = new HttpURL(newlocation);
							/**
							 * blast的时候一般新的页面都是即时计算出来的，
							 * 为防止新的链接没有及时生成，那么就需要让程序等待一段时间
							 */
							Thread.sleep(1000 * postSleepTime);

							/**
							 * 这里不知道是第几次转移。在post提交时，如果没有转移，
							 * 那么direct就是PostMethod对象 就不能转变成GetMethod对象<br/>
							 * 如果已经转移过，那么就是direct就是GetMethod类，就能够赋给重定向对象redirect
							 */
							try {
								redirect = (GetMethod) direct;
								redirect.setURI(newURL);
							}
							/**
							 * 如果是第一次转移，那么就设定一个新的重定向对象
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
						 * 不需要状态转移的话，将flag设置为true就好
						 */
						flag = true;
						break;
					}
				}
				/**
				 * 第二次提交用redirect对象进行，并返回提交情况
				 */
				else {
					statusCode = httpClient.executeMethod(redirect);// 第二次用新的url地址
					/**
					 * 如果需要状态转移
					 */
					if ((statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
							|| (statusCode == HttpStatus.SC_MOVED_PERMANENTLY)
							|| (statusCode == HttpStatus.SC_SEE_OTHER)
							|| (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
						/**
						 * 从头中取出转向的地址
						 */
						Header locationHeader = redirect
								.getResponseHeader("location");
						newlocation = null;
						if (locationHeader != null) {
							newlocation = locationHeader.getValue();// 新的转移链接

							if ((newlocation == null)
									|| (newlocation.equals(""))) {
								/**
								 * 如果没有从头里面获得连接，那么很可能就是没有链接<br/>
								 * 那么新的链接和老的链接相同
								 */
								newlocation = redirect.getURI().toString();
							}
							newURL = new HttpURL(newlocation);
							redirect.setURI(newURL);
							// direct.releaseConnection();//可以将老的关闭
							// direct.abort();
							// HttpSetting(direct);//将新的准备好
							// direct=redirect;//将引用传递给老的

							// System.out.println("Redirect:"+
							// redirect.getStatusLine().toString());

						} else {
							System.err.println("Location field value is null.");
							return null;
						}
					} else {
						/**
						 * 不需要状态转移的话，将flag设置为true就好
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
		 * 如果已经不需要状态转移了
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
	 * 给定url链接，通过get提交，返回bufferreader流文件
	 * 
	 * @param url
	 * @param ChangeCookies
	 *            是否改变cookies
	 * @return
	 */
	public BufferedReader GetFetch(String url, boolean ChangeCookies) {
		/**
		 * 设置httpclient参数，包括连接时间 socket超时设置 最大连接数之类的
		 */
		SetPara();
		// httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		// httpClient.getHostConfiguration().setHost(LOGON_SITE, LOGON_PORT);
		get = new GetMethod(url);
		get.setFollowRedirects(true);
		try {
			// httpFinally = MethodReady(get);

			/**
			 * 获得最后一个direct的GetMethod对象
			 */
			get = (GetMethod) MethodReady(get, ChangeCookies);

			// httpClient.executeMethod( httpFinally );
			InputStream instream = get.getResponseBodyAsStream();
			// 在目标页面情况未知的条件下，不推荐使用getResponseBodyAsString()方法
			// String strGetResponseBody = post.getResponseBodyAsString();

			/**
			 * 读取信息
			 */
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					instream, GET_CONTENT_CHARSET)); // 添加解码方式
			return reader;
		}

		/**
		 * 有问题的话，就尝试释放连接
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
	 * 下载一些小的东西，图片之类的，给定网址和保存文件夹，自动下载完成。
	 * 
	 * @param url
	 *            待下载文件的网址
	 * @param changeCookies
	 *            是否装载新的cookies
	 * @param saveFilePath
	 *            待保存的文件夹，默认最后加上"/"，如"/media/"之类
	 */
	public void getDownLoad(String url, String saveFilePath,
			boolean changCookies) {
		// 提取url中的文件名，为最后"/"后的所有名字
		String[] stUrlSplit = url.split("/");
		String stFileName = stUrlSplit[stUrlSplit.length - 1];

		/**
		 * 设置httpclient参数，包括连接时间 socket超时设置 最大连接数之类的
		 */
		SetPara();
		get = new GetMethod(url);
		get.setFollowRedirects(true);

		try {
			/**
			 * 获得最后一个direct的GetMethod对象
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
		 * 有问题的话，就尝试释放连接
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
	 * 设置post提交参数
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
	 * Post提交，给定PostContent与url<br/>
	 * 最后返回bufferReader类型内容<br/>
	 * 返回后先人工查看下编码是否正确，不行的话修改本对象 POST_CONTENT_CHARSET 属性
	 * 
	 * @param postContent
	 *            获得所要post的内容，是一个二维string数组形式 0：post的项目 1：post的内容
	 * @param changeCookies
	 *            是否装载新的cookies
	 * @param url
	 *            待post的url
	 * @param 考虑添加参数
	 *            getCookies 是否获得cookies
	 * @return bufferReader流文件
	 */
	@SuppressWarnings("deprecation")
	public BufferedReader PostFetch(String[][] postContent, String url,
			boolean changeCookies) {
		/**
		 * 设置httpclient参数，包括连接时间 socket超时设置 最大连接数之类的
		 */
		SetPara();
		URI uri;
		try {
			uri = new URI(url);
			post.setURI(uri);
		} catch (URIException e2) {
		}
		/**
		 * 获取post的信息
		 */
		post = PostData(post, postContent);
		try {
			httpFinally = MethodReady(post, changeCookies);
			InputStream instream = httpFinally.getResponseBodyAsStream();
			// 在目标页面情况未知的条件下，不推荐使用getResponseBodyAsString()方法
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					instream, POST_CONTENT_CHARSET)); // 添加解码方式
			return reader;
		} catch (Exception e) {
			/**
			 * 啥也没干，是否需要关闭连接？
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
	 * 释放连接
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
