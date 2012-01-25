package com.novelbio.base.dataOperate;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.BasicManagedEntity;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.*;

public class WebFetch4 {
	
	public void test() throws NoSuchAlgorithmException, ConnectionPoolTimeoutException, InterruptedException
	{
		HttpParams params = new BasicHttpParams(); 
		
		SchemeRegistry sr = new SchemeRegistry(); 
		
		SSLSocketFactory sf = new SSLSocketFactory(SSLContext.getInstance("TLS")); 
		sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
		Scheme https = new Scheme("https", sf, 443); 
		Scheme http = new Scheme("http", 80, PlainSocketFactory.getSocketFactory()); 
		
		sr.register(http);
		
		
		
		BasicClientConnectionManager connMrg = new BasicClientConnectionManager(sr); // ���������ӡ��������һ���ܳ��Ĺ��̡�
		ClientConnectionRequest connRequest = connMrg.requestConnection(
		new HttpRoute(new HttpHost("localhost", 80)), null);
		
		ManagedClientConnection conn = connRequest.getConnection(10, TimeUnit.SECONDS);
				try {
				BasicHttpRequest request = new BasicHttpRequest("GET", "/"); 
				conn.sendRequestHeader(request);
				HttpResponse response = conn.receiveResponseHeader(); 
				conn.receiveResponseEntity(response);
				HttpEntity entity = response.getEntity(); if (entity != null) {
				BasicManagedEntity managedEntity = new BasicManagedEntity(entity, conn, true); // �滻ʵ�� response.setEntity(managedEntity);
				}
				// ʹ����Ӧ���������õ����顣����Ӧ���ݱ����ĺ�������ӽ����Զ��ͷš� } catch (IOException ex) {
				//��I/O error֮����ֹ���ӡ�
				conn.abortConnection();
				}
				catch (Exception e) {
					// TODO: handle exception
				}
		
	}
}
