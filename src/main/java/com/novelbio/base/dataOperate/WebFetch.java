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
 * �������Դ���
 * @author zong0jie
 *
 */
class MyRetryHandler implements HttpRequestRetryHandler
{

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