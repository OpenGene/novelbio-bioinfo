package com.novelbio.other.pixiv.execute;



public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	 
		changPiixvName();
		try {
//			compDon2Chan();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	
		
		/**
		String AuthorUrl="http://www.pixiv.net/bookmark.php?type=user&rest=show&p=";
		String SavetxtPath="/home/zong0jie/桌面/pixiv.txt";
		PixivOperate aaa=new PixivOperate();
		aaa.getcookies();
		for(int i=1;i<=10;i++)
		{
			aaa.downloadPicture(AuthorUrl+i, SavetxtPath);
			System.out.println("ok");
		}
		System.out.print("finish");
		**/
		System.out.print("finish");
	}
	
	
	private static void changPiixvName() {
			PixivOperate aaa=new PixivOperate();
		aaa.getcookies();
		String ID="";
		//aaa.execute(ID);
		aaa.readfile("/Volumes/DATA/myData/Desktop/pixiv/picture", "/Volumes/DATA/myData/Pictures/Comic/pixiv_picture");
	}
	
	private static void compDon2Chan() throws Exception {
		compDon2Chan.getMoreNum("/home/zong0jie/桌面/pixiv/donmai.txt", "/home/zong0jie/桌面/pixiv/donmaiResult.txt", "/home/zong0jie/桌面/pixiv/donmaiReset.txt");
	}
}
