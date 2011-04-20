# 传入染色体长度，染色体的reads情况，以及画图的大小，最后画出染色体上Reads的密度分布图
# 被ReadsDensity类调用
# Author: zong0jie
###############################################################################
#x, 染色体坐标
#y，该坐标下reads数目
#chrLen 本条chrLen的长度
#chraMaxLen 最长chrLen的长度
#maxPicLength 最长图片长度
#heigh 图像高度 
#ylength y轴的高度，当有值高于该点时，将该点删除
#thsicol 画图的线的颜色
#whichChain 哪条链，仅仅用于在图片上显示出来而已。默认为无
#mainCol 主字体的颜色
#file 保存的文件名，默认保存在R的工作空间，名字为PeakDensity
#####################################################################################
#参数设定
#待写入
setwd("/media/winG/bioinformation/R/practice_script/platform") #脚本所在文件夹
savewd="/media/winG/NBC/Project/2010070601ChenDG/results/peakDensity/readsChrDensity/" #图片保存路径
x=scan("readsx");
y=scan("readsy");
y2=0
if(file.exists("reads2y"))
	y2=scan("reads2y");
parameter=read.table("parameter",he=T)
#####################################################################################
chrLen=as.integer(as.character(parameter[1,2]))
chrMaxLen=as.integer(as.character(parameter[2,2]))
chrID=as.character(parameter[3,2])
#####################################################################################
myChrReads = function(x,y,y2=0,chrLen,chrMaxLen,chrID="",picLength=10000,heigh=1400,ylength=200,thiscol="blue",whichChain="",mainCol="green", file="ReadsDensity")
{
	binvx=30 #x轴分的区域
	binvy=15 #y轴分的区域
	thisfile=paste(savewd,file,chrID,".jpg",sep="") #文件名
	
	if(picLength>50000) #图片长度不能大于50000
	{
		picLength=50000
	}
	jpeg(filename = thisfile, width = picLength, height = heigh)#这样子才能画图并输出
	
	par( mar=c(15,15,15,15) )
	
	if(length(y2)==1)
	{
		lable=c(1:length(x))
		ytmp=y
		lable=lable[y>ylength]  #获得y中大于ylength的序号
		y[y>ylength]=ylength #把y中大的都截短
		
		plot(c(0,chrMaxLen),c(0,ylength),col=rgb(0,0,0,0),xlab="",main="",ylab="",axes=FALSE) #按最长的染色体画出图像边框
		
		points(c(0,chrLen),c(ylength,ylength),col="black",ty="h",lwd=5) #画出染色体边界
		
		
		points(x,y,col=thiscol,ty="h")
		######偏大的线加上标记###########################
		if(length(lable)>0)
		{
			ybigNum=length(lable) #y中大于ylength的数目
			ybias=c(1:ybigNum*(50/ybigNum)-50)
			text(x[lable]+2,y[lable]+ybias,round(ytmp[lable])) #x,y数据向量，labes：整数或字符串，默认labels=1:length(x) 这里实际上是批量加点，并且加的点都是整数
		}
		###############################################
		title(main=paste(whichChain,chrID," Reads Distribution",sep=" "),col="green",font.main=1,cex.main=5)
		#line:标题相对于坐标轴的偏移，远离图片方向的偏移
		title(ylab="Reads Density",cex.lab=5,line=10) 
		title(xlab=paste("Length of ",chrID,sep=""),cex.lab=5,line=10) 
		##### 画 坐 标 轴 ##################################
		binNumX=round(chrMaxLen/binvx/100)*100 #每一个区间的具体距离，是100的整数倍
		if(binNumX<100)
		{binNumX=100}
		xrightNum=ceiling(chrLen/binNumX)
		xleftNum=0
		
		axis(si=1,xleftNum:xrightNum*binNumX,col="green",cex.axis=2.5) #x轴添加信息
		
		binNumY=round(ylength/binvy/50)*50 #每一个区间的具体距离，是50的整数倍
		if(binNumY<50)
		{binNumY=50}	
		yrightNum=ceiling(ylength/binNumY)
		axis(si=2,0:yrightNum*binNumY,col="green",cex.axis=2.5) #y轴添加信息
		##################################################
	} else
	{
		y2=-y2#反向
		lable=c(1:length(x))
		lable2=c(1:length(x))
		
		ytmp=y
		ytmp2=y2
		
		lable=lable[y>ylength]  #获得y中大于ylength的序号
		y[y>ylength]=ylength #把y中大的都截短
		
		lable2=lable2[y2< (-ylength)]  #获得y2中大于ylength的序号，必须括号括起来，要不然<-变成赋值了
		y2[y2< (-ylength)]=-ylength #把y2中大的都截短
		
		plot(c(0,chrMaxLen),c(-ylength,ylength),col=rgb(0,0,0,0),xlab="",main="",ylab="",axes=FALSE) #按最长的染色体画出图像边框
		
		points(c(0,0,chrLen,chrLen),c(-ylength,ylength,-ylength,ylength),col="black",ty="h",lwd=5) #画出染色体边界
		
		
		points(x,y,col=thiscol,ty="h")
		points(x,y2,col=thiscol,ty="h")
		######偏大的线加上标记###########################
		if(length(lable)>0)
		{
			ybigNum=length(lable) #y中大于ylength的数目
			ybias=c(1:ybigNum*(50/ybigNum)-50)
			text(x[lable]+2,y[lable]+ybias,round(ytmp[lable])) #x,y数据向量，labes：整数或字符串，默认labels=1:length(x) 这里实际上是批量加点，并且加的点都是整数
		}
		if(length(lable2)>0)
		{
			ybigNum=length(lable2) #y中大于ylength的数目
			ybias=c(1:ybigNum*(50/ybigNum)-50)
			text(x[lable2]+2,y2[lable2]-ybias,-round(ytmp2[lable2])) #x,y2数据向量，labes2：整数或字符串，默认label2s=1:length(x) 这里实际上是批量加点，并且加的点都是整数
		}
		###############################################
		title(main=paste(chrID," Reads Distribution",sep=" "),col="green",font.main=1,cex.main=5)#两条链一起画了就不分F和R了
		#line:标题相对于坐标轴的偏移，远离图片方向的偏移
		title(ylab="Reads Density", cex.lab=5,line=10)
		title(xlab=paste("Length of ", chrID,sep=""),cex.lab=5,line=10) 
		##### 画 坐 标 轴 ##################################
		binNumX=round(chrMaxLen/binvx/100)*100 #每一个区间的具体距离，是100的整数倍
		if(binNumX<100)
		{binNumX=100}
		xrightNum=ceiling(chrLen/binNumX)
		xleftNum=0
		
		axis(si=1,xleftNum:xrightNum*binNumX,col="green",cex.axis=2.5) #x轴添加信息
		
		binNumY=round(ylength/binvy/50)*50 #每一个区间的具体距离，是50的整数倍
		if(binNumY<50)
		{binNumY=50}	
		yrightNum=ceiling(ylength/binNumY)
		axis(si=2,-yrightNum:yrightNum*binNumY,col="green",cex.axis=2.5) #y轴添加信息
		
		points(c(0,chrLen),c(0,0),col="darkblue",ty="l",lwd=5) #将两条染色体分开
		##################################################
	}
	dev.off()
}
##########################################################################################
if(file.exists("reads2y"))
{myChrReads(x,y,y2,chrLen,chrMaxLen,chrID);
}else
	myChrReads(x,y,y2=0,chrLen,chrMaxLen,chrID)











