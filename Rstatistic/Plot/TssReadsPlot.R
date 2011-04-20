# TODO: Add comment
# 
# Author: zong0jie
###############################################################################


setwd("/media/winG/NBC/项目/2010070601陈德桂/results/peakDensity/Reads精度Tss")
Resolution=10
region=c(-10000,10000)
a=scan("tss.txt")
binv=10 #x轴总共约10个标尺
PicLength=500
heigh=500

regLen=region[2]-region[1] #画图区域长度
len=length(a)
############画图###############
x=c(1:len*10+min(region))
#plot(c(min(region),max(region)),c(0,max(a)),axes=F)
#points(x,a,col="blue",ty="l",lwd=2,main="",ylab="",xlab="")
plot(x,a,col="blue",ty="l",lwd=2,main="",ylab="",xlab="",axes=F)
##############################
title(main="Reads NearBy Tss",col="green",font.main=1,cex.main=2)
title(ylab="Reads Counts",cex.lab=1.5)
title(xlab="Region NearBy Tss",cex.lab=1.5)
#########计算x轴坐标########################################
binNum=round(regLen/binv/100)*100 #每一个区间的具体距离，是100的整数倍
xrightNum=ceiling(region[2]/binNum)
xleftNum=floor(region[1]/binNum)

axis(si=1,xleftNum:xrightNum*binNum,col="green") #x轴添加信息
axis(si=2,col="green") #y轴添加信息
############################################################

dev.print(bmp, file="/home/zong0jie/桌面/tss.bmp", width=PicLength, height=heigh)

