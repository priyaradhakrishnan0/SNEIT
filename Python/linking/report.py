import sys

if len(sys.argv) !=2:
	print('Missed specifying the file name.');
	sys.exit();

file=sys.argv[1];
lines=[];
with open(file) as f:
	lines=f.readlines();

res0=[0]*3;
res1=[0]*3;
res_avg=[0]*3;
numIterations=0;
for line in lines:
	line=line.strip();
	if len(line)!=0:
		if line.startswith('0'):
			split=line.split();
			res0[0]=res0[0]+float(split[1]);
			res0[1]=res0[1]+float(split[2]);
			res0[2]=res0[2]+float(split[3]);
			numIterations+=1;
		elif line.startswith('1'):
			split=line.split();
			res1[0]=res1[0]+float(split[1]);
			res1[1]=res1[1]+float(split[2]);
			res1[2]=res1[2]+float(split[3]);
		elif line.startswith('avg'):
			split=line.split();
			res_avg[0]=res_avg[0]+float(split[3]);
			res_avg[1]=res_avg[1]+float(split[4]);
			res_avg[2]=res_avg[2]+float(split[5]);

#Normalize the values
for i in range(3):
	res0[i]=(res0[i]/numIterations);

for i in range(3):
	res1[i]=(res1[i]/numIterations);

for i in range(3):
	res_avg[i]=(res_avg[i]/numIterations);

print "# Iterations ="+str(numIterations);
print '\tP\tR\tF'
print '0\t'+str(res0[0])+'\t'+str(res0[1])+'\t'+str(res0[2]);
print '1\t'+str(res1[0])+'\t'+str(res1[1])+'\t'+str(res1[2]);
print 'AVG\t'+str(res_avg[0])+'\t'+str(res_avg[1])+'\t'+str(res_avg[2]);