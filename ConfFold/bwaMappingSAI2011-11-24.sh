bwa samse  -r "@RG\tID:ExomeA\tLB:ExomeA\tSM:ExomeA\tPL:ILLUMINA" -n 1 /media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/A_Filter_1.sai /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/rawdata/A_Filter.fq > /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/A_BWA_map_R.sam


bwa aln -n 0.05 -o 1 -e 10 -l 25 -t 4 -O 10 -I /media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/rawdata/B_Filter.fq > /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/B_Filter_1.sai

bwa samse  -r "@RG\tID:ExomeA\tLB:ExomeA\tSM:ExomeA\tPL:ILLUMINA" -n 1 /media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/B_Filter_1.sai /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/rawdata/B_Filter.fq > /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/B_BWA_map_R.sam

bwa aln -n 0.05 -o 1 -e 10 -l 25 -t 4 -O 10 -I /media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/rawdata/C_Filter.fq > /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/C_Filter_1.sai

bwa samse  -r "@RG\tID:ExomeA\tLB:ExomeA\tSM:ExomeA\tPL:ILLUMINA" -n 1 /media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/C_Filter_1.sai /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/rawdata/C_Filter.fq > /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/C_BWA_map_R.sam

bwa aln -n 0.05 -o 1 -e 10 -l 25 -t 4 -O 10 -I /media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/rawdata/D_Filter.fq > /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/D_Filter_1.sai

bwa samse  -r "@RG\tID:ExomeA\tLB:ExomeA\tSM:ExomeA\tPL:ILLUMINA" -n 1 /media/winE/Bioinformatics/GenomeData/human/ucsc_hg19/Index/bwa_chromFa/UCSC_hg19.fa /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/D_Filter_1.sai /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/rawdata/D_Filter.fq > /media/winE/NBC/Project/Project_HXW_Lab/exome_capture/mapping/D_BWA_map_R.sam


