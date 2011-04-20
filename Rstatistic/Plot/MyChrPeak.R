# 读取染色体长度文件和总peak文件，以及画图的大小，最后画出染色体上Peak的密度分布图
# 直接用
# Author: zong0jie
###############################################################################
#chrLenInfo 读取染色体长度文件
#F,R  读取一条或两条链上的Peak
#binNum 频率直方图的分割块数
#chraMaxLen 最长chrLen的长度
#maxPicLength 最长图片长度
#ylength 图片y轴坐标高度

#####################################################################################
#参数设定
#染色体长度文件
chrLenInfo=read.table("/media/winG/bioinformation/GenomeData/ucsc_mm9/statistic/ChrLength.txt",he=T,sep="\t")
#读取和写入的文件夹为一个文件夹
savewd="/media/winG/NBC/Project/ChIP-SeqCDG20100911/result/PeakDistribution/NP/" 
#peak文件
F=read.table(paste(savewd,"NPFPeak Information.txt",sep=""),sep="\t",he=T)
#R=0 #没有R链时则设置 R=0
R=read.table(paste(savewd,"NPRPeak Information.txt",sep=""),sep="\t",he=T)
#Summit位点在第几行
sumNum=9
#最长一条染色体分为多少个柱子
binNum=700
ylength=20 #频率直方图图中柱子的高度
###################################################################################
myChrPeak = function(F,R=0,sumNum=9,chrLenInfo,savewd,picLength=2000,heigh=800,thiscol="blue",whichChain="",mainCol="green", file="PeakDensity")
{
	binvx=30 #x轴分的区域
	binvy=3 #y轴分的区域
	chrMaxLen=max(chrLenInfo[,2])
	bin=chrMaxLen/binNum #染色体的分区

	i=0
	if(length(R)>1) #R存在的时候
	{
		for(i in 1:length(levels(F[,1])))
		{
			chrID=levels(F[,1])[i]
			thisfile=paste(savewd,chrID,"_FR",file,".jpg",sep="") #文件名
			jpeg(filename = thisfile, width = picLength, height = heigh)#这样子才能画图并输出
			
			Fchr=F[F[,1]==chrID,sumNum] #第i条染色体上peak的坐标
			Rchr=R[R[,1]==chrID,sumNum] #第i条染色体上peak的坐标
			chrLen=chrLenInfo[tolower(chrLenInfo[,1])==tolower(chrID),2] #第i条染色体的长度,将Peak文件中的chrID转化为小写与ChrLengthFile中的小写chrID比较
			
			opar <- par(mfrow = c(2, 1), oma = c(0, 0, 1.1, 0),mar =c(5.2,5,5,1)) #设定画图的界面
			plot(c(0,chrMaxLen),c(0,ylength),col=rgb(0,0,0,0),xlab="",main="",ylab="",axes=FALSE) #按最长的染色体画出图像边框
			hist(Fchr,
					br=round(chrLen/bin),
					xlab="",
					bo=thiscol,#边框颜色
					col=thiscol,#柱状图填充颜色
					main="",ylab="",
					add=T)
			title(main=paste("Fchain ",chrID," Peak Distribution",sep=""),col="green",font.main=1,cex.main=2)
			title(ylab="FPeak Density",cex.lab=1.5)
			title(xlab=paste("Length of Chr",chrID,sep=""),cex.lab=1.5)
			points(c(0,chrLen),c(ylength,ylength),col="black",ty="h",lwd=5) #画出染色体边界
			##### 画 坐 标 轴 ##################################
			binNumX=round(chrMaxLen/binvx/100)*100 #每一个区间的具体距离，是100的整数倍
			if(binNumX<100)
			{binNumX=100}
			xrightNum=ceiling(chrLen/binNumX)
			xleftNum=0
			
			axis(si=1,xleftNum:xrightNum*binNumX,col="green",cex.axis=1) #x轴添加信息
			
			binNumY=round(ylength/binvy/3)*3 #每一个区间的具体距离，是3的整数倍
			if(binNumY<3)
			{binNumY=3}	
			yrightNum=ceiling(ylength/binNumY)
			axis(si=2,-yrightNum:yrightNum*binNumY,col="green",cex.axis=1) #y轴添加信息
			###################################################
			
			
			plot(c(0,chrMaxLen),c(0,ylength),col=rgb(0,0,0,0),xlab="",main="",ylab="",axes=FALSE) #按最长的染色体画出图像边框
			hist(Rchr,
					br=round(chrLen/bin),
					xlab="",
					bo=thiscol,
					col=thiscol,#柱状图填充颜色
					main="",ylab="",
					add=T)
			title(main=paste("Rchain ",chrID," Peak Distribution",sep=""),col="green",font.main=1,cex.main=2)
			title(ylab="FPeak Density",cex.lab=1.5)
			title(xlab=paste("Length of Chr",chrID,sep=""),cex.lab=1.5)
			points(c(0,chrLen),c(ylength,ylength),col="black",ty="h",lwd=5) #画出染色体边界
			##### 画 坐 标 轴 ##################################
			binNumX=round(chrMaxLen/binvx/100)*100 #每一个区间的具体距离，是100的整数倍
			if(binNumX<100)
			{binNumX=100}
			xrightNum=ceiling(chrLen/binNumX)
			xleftNum=0
			
			axis(si=1,xleftNum:xrightNum*binNumX,col="green",cex.axis=1) #x轴添加信息
			
			binNumY=round(ylength/binvy/3)*3 #每一个区间的具体距离，是3的整数倍
			if(binNumY<3)
			{binNumY=3}	
			yrightNum=ceiling(ylength/binNumY)
			axis(si=2,-yrightNum:yrightNum*binNumY,col="green",cex.axis=1) #y轴添加信息
			###################################################
			dev.off()
		}
		
	}else
	{
		for(i in 1:length(levels(F[,1])))
		{
			chrID=levels(F[,1])[i]
			thisfile=paste(savewd,chrID,"_",file,".jpg",sep="") #文件名
			jpeg(filename = thisfile, width = picLength, height = heigh)#这样子才能画图并输出
			Fchr=F[F[,1]==chrID,sumNum] #第i条染色体上peak的坐标
			chrLen=chrLenInfo[chrLenInfo[,1]==chrID,2] #第i条染色体的长度
			
			opar <- par(mfrow = c(2, 1), oma = c(0, 0, 1.1, 0),mar =c(5.2,5,5,1)) #设定画图的界面
			plot(c(0,chrMaxLen),c(0,ylength),col=rgb(0,0,0,0),xlab="",main="",ylab="",axes=FALSE) #按最长的染色体画出图像边框
			hist(Fchr,
					br=round(chrLen/bin),
					xlab="",
					bo=thiscol,
					col=thiscol,#柱状图填充颜色
					main="",ylab="",
					add=T)
			title(main=paste("Fchain ",chrID," Peak Distribution",sep=""),col="green",font.main=1,cex.main=2)
			title(ylab="FPeak Density",cex.lab=1.5)
			title(xlab=paste("Length of Chr",chrID,sep=""),cex.lab=1.5)
			points(c(0,chrLen),c(ylength,ylength),col="black",ty="h",lwd=5) #画出染色体边界
			##### 画 坐 标 轴 ##################################
			binNumX=round(chrMaxLen/binvx/100)*100 #每一个区间的具体距离，是100的整数倍
			if(binNumX<100)
			{binNumX=100}
			xrightNum=ceiling(chrLen/binNumX)
			xleftNum=0
			
			axis(si=1,xleftNum:xrightNum*binNumX,col="green",cex.axis=1) #x轴添加信息
			
			binNumY=round(ylength/binvy/3)*3 #每一个区间的具体距离，是3的整数倍
			if(binNumY<3)
			{binNumY=3}	
			yrightNum=ceiling(ylength/binNumY)
			axis(si=2,-yrightNum:yrightNum*binNumY,col="green",cex.axis=1) #y轴添加信息
			###################################################
			dev.off()
		}
	}
}
#################################################################################################
myChrPeak(F,R,sumNum,chrLenInfo,savewd=savewd)

