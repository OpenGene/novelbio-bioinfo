package com.novelbio.base.dataOperate;

import java.io.IOException;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

public class WebFetch {
	public static void main(String[] args) {
		try {
			getMethod();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static void getMethod() throws ClientProtocolException, IOException
	{
		  HttpClient httpclient = new DefaultHttpClient();
	        try {
	            HttpGet httpget = new HttpGet("http://www.google.com/");

	            System.out.println("executing request " + httpget.getURI());

	            // Create a response handler
	            ResponseHandler<String> responseHandler = new BasicResponseHandler();
	            String responseBody = httpclient.execute(httpget, responseHandler);
	            System.out.println("----------------------------------------");
	            System.out.println(responseBody);
	            System.out.println("----------------------------------------");

	        } finally {
	            // When HttpClient instance is no longer needed,
	            // shut down the connection manager to ensure
	            // immediate deallocation of all system resources
	            httpclient.getConnectionManager().shutdown();
	        }
	}
}
/**
 * 请求重试处理
 * @author zong0jie
 *
 */
class MyRetryHandler implements HttpRequestRetryHandler
{

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