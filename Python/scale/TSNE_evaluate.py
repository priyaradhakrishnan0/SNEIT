import sys
import pymongo
from pymongo import MongoClient
from random import shuffle
import math

#Validate the arguments
if len(sys.argv)!=2:
	print "Invalid arguments : python TSNE_evaluate.py <number_of_tsne_rows>";
	sys.exit(0);
numSamples=int(sys.argv[1]);

#Initialize the mongo client
conn=MongoClient('10.2.4.210',27017);
db=conn['snetDB'];
coll=db['snet'];

#Pick a random tweet
count=0;
cursor=coll.find();


tweetList = [];
for tweet in cursor:
	tweetList.append(tweet);
print "Total SNET rows = "+str(len(tweetList));
shuffle(tweetList);

accuracy = 0;
for i in range(0,numSamples):
	count+=1;
	print tweetList[i]['tweet']+"  --  "+str(tweetList[i]['neList']);
	label = raw_input("Enter 1(SNE) or 0(NOT) :: ");
	if label == "1":
		accuracy+=1;
acc = accuracy / count;
print "accuracy = "+str(accuracy)+"  and "+str(float(accuracy)/count);