package com.novelbio.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

/**
 * MD5���㷨��RFC1321 �ж��� ��RFC 1321�У�������Test suite�����������ʵ���Ƿ���ȷ�� MD5 ("") =
 * d41d8cd98f00b204e9800998ecf8427e MD5 ("a") = 0cc175b9c0f1b6a831c399e269772661
 * MD5 ("abc") = 900150983cd24fb0d6963f7d28e17f72 MD5 ("message digest") =
 * f96b697d7cb7938d525a2f31aaf161d0 MD5 ("abcdefghijklmnopqrstuvwxyz") =
 * c3fcd3d76192e4007dfb496cca67e13b
 * 
 * @author haogj
 * 
 *         ���������һ���ֽ����� �����������ֽ������ MD5 ����ַ���
 */
public class MD5generate {
	private static Logger logger = Logger.getLogger(MD5generate.class);

	protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6','7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };  
     
	protected static MessageDigest messageDigest = null;  
	static {
		try {
			messageDigest = MessageDigest.getInstance("MD5");  
		} catch (NoSuchAlgorithmException nsaex) {  
			logger.error("��ʼ��ʧ�ܣ�MessageDigest��֧��MD5!");  
			nsaex.printStackTrace();  
		}
	}
   
	public static void main(String[] args) throws IOException {
		long begin = System.currentTimeMillis();  
		
		File big = new File("/media/winF/NBC/Project/Project_FY/20120920/RKO-con_L1_1.fq.gz");  
		String md5 = getFileMD5String(big);  
		//String md5 = getMD5String("a");  
		long end = System.currentTimeMillis();  
		System.out.println("md5:" + md5 + " time:" + ((end - begin) / 1000) + "s");  
	}
   
	public static String getFileMD5String(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);  
		FileChannel ch = in.getChannel();  
		
		//700000000 bytes are about 670M  
		int maxSize=700000000;  
		
		long startPosition=0L;  
		long step=file.length()/maxSize;  
		
		if(step == 0) {  
			MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0,file.length());  
			messageDigest.update(byteBuffer);  
			return bufferToHex(messageDigest.digest());  
		}
         
		for(int i=0;i<step;i++) {
			MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, startPosition,maxSize);  
			messageDigest.update(byteBuffer);  
			startPosition+=maxSize;  
		}
         
		if(startPosition==file.length()) {  
			return bufferToHex(messageDigest.digest());  
		}
   
		MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, startPosition,file.length()-startPosition);  
		messageDigest.update(byteBuffer);  
           
		return bufferToHex(messageDigest.digest());  
	}
	
	public static String getMD5String(String s) {  
		return getMD5String(s.getBytes());  
	}  
   
	public static String getMD5String(byte[] bytes) {  
		messageDigest.update(bytes);  
		return bufferToHex(messageDigest.digest());  
	}  
   
	private static String bufferToHex(byte bytes[]) {  
		return bufferToHex(bytes, 0, bytes.length);  
	}  
   
	private static String bufferToHex(byte bytes[], int m, int n) {  
		StringBuffer stringbuffer = new StringBuffer(2 * n);  
		int k = m + n;  
		for (int l = m; l < k; l++) {  
			appendHexPair(bytes[l], stringbuffer);  
		}  
		return stringbuffer.toString();  
	}  
   
	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {  
		char c0 = hexDigits[(bt & 0xf0) >> 4];  
		char c1 = hexDigits[bt & 0xf];  
		stringbuffer.append(c0);  
		stringbuffer.append(c1);  
	}  
   
	public static boolean checkPassword(String password, String md5PwdStr) {  
		String s = getMD5String(password);  
		return s.equals(md5PwdStr);  
	}  
       
	/**
	 * ֻ�ǿ����ģ���������һЩע��д�ıȽ������������ν���MD5�Ľ��
	 * @param source
	 * @return
	 */
	public static String getMD5(byte[] source) {
		String s = null;
		char hexDigits[] = { // �������ֽ�ת���� 16 ���Ʊ�ʾ���ַ�
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
				'e', 'f' };
		try {
			java.security.MessageDigest md = java.security.MessageDigest
					.getInstance("MD5");
			md.update(source);
			byte tmp[] = md.digest(); // MD5 �ļ�������һ�� 128 λ�ĳ�������
										// ���ֽڱ�ʾ���� 16 ���ֽ�
			char str[] = new char[16 * 2]; // ÿ���ֽ��� 16 ���Ʊ�ʾ�Ļ���ʹ�������ַ���
											// ���Ա�ʾ�� 16 ������Ҫ 32 ���ַ�
			int k = 0; // ��ʾת������ж�Ӧ���ַ�λ��
			for (int i = 0; i < 16; i++) { // �ӵ�һ���ֽڿ�ʼ���� MD5 ��ÿһ���ֽ�
											// ת���� 16 �����ַ���ת��
				byte byte0 = tmp[i]; // ȡ�� i ���ֽ�
				str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // ȡ�ֽ��и� 4 λ������ת��,
															// >>>
															// Ϊ�߼����ƣ�������λһ������
				str[k++] = hexDigits[byte0 & 0xf]; // ȡ�ֽ��е� 4 λ������ת��
			}
			s = new String(str); // ����Ľ��ת��Ϊ�ַ���

		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}
		
}
