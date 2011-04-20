# TODO: Add comment
# 
# Author: zong0jie
###############################################################################

wokSpace = "/media/winE/Bioinformatics/R/practice_script/platform/pvalue2fdr";
setwd(wokSpace)
pvalue = scan("pvalue.txt","BH") #在MathComput.pvalue2Fdr中修改相应选项
fdr = p.adjust(pvalue)
write.table(fdr,"fdr.txt",sep="\t")
