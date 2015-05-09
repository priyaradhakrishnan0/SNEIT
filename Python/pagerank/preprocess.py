import sys
import networkx as nx
import pymongo
from pymongo import MongoClient
import math
import datetime

#datetime.datetime.now()

#Initialize the graph
G=nx.DiGraph();

#Initialize the mongo client
conn=MongoClient('10.2.4.210',27017);
db=conn['tweetDB'];
coll=db['tweets'];

#Extract the distinct topics.
cursor=coll.distinct('topic')
masterTopics=[];
for topic in cursor:
	masterTopics.append(topic);

#Validate the arguments
if len(sys.argv)!=3:
	print "Invalid arguments : python pageRankMaster.py <minExperts> <topic>";
	sys.exit(0);
minExperts=int(sys.argv[1]);
topic=sys.argv[2];

'''
for topic in topicList.split(','):
	if topic not in masterTopics:
		print "Invalid arguments. Topic "+topic+" is not found in DB.";
		sys.exit(0);

custTopics=topicList.split(',');
'''

#Read the expertCount per topic
expertList=[];
#print "Topic = "+topic;
with open("topic-expert.txt", "r") as ins:
	for line in ins:		
		key=line.split('[')[0];
		#print key;
		if key==topic:
			#print "Found experts of "+topic;
			val=len(line.split(','))+1;
			for user in line[line.index('[')+1:len(line)-1].split(','):
				expertList.append(user);

#print len(expertList);
#f1Name = topic+'.tweets'
#f2Name = topic+'.pgrk'
#f1 = open('%s' %f1Name, 'w')
#f2 = open('%s' %f2Name, 'w')

#Populate the graph
count=0;
cursor=coll.find();
neCountMap={};
edgeCountMap={};
tweetCount=0;
#tweetList=[];
for tweet in cursor:
	count+=1;
	#if count%100000==0:
		#print "Processed "+str(count);

	if tweet['expert'] in expertList and len(tweet['neList'])!=0:		
		tweetCount+=1;
		#if tweetCount%10==0:
		#	tweetList.append(tweet['tweet']);
			#print tweet['tweet'];
		#else:
		expert=tweet['expert'];

		#read all the ne's
		neList=[];
		for i in range(len(tweet['neList'])):
			neList.append(tweet['neList'][i]['ne']);

		
		#Initialize the nodes
		for ne in neList:
			#G.add_node(ne);
			#print G[ne];
			if ne in neCountMap:
				neCountMap[ne]=neCountMap[ne]+1;
			else:
				neCountMap[ne]=1;

			#G[ne]['nodeCountExperts']=1;
			'''
			if G[ne].get('nodeCountExperts')==None:
				G[ne]['nodeCountExperts']=1;
			else:
				G[ne]['nodeCountExperts']=G[ne]['nodeCountExperts']+1;
			'''
		

		#Intialize the edges
		for i in range(0,len(neList)-1):
			for j in range(i+1,len(neList)):
				G.add_edge(neList[i],neList[j]);
				key='';
				if neList[i]<neList[j]:
					key=neList[i]+'###'+neList[j];
				else:
					key=neList[j]+'###'+neList[i];
				if key in edgeCountMap:
					edgeCountMap[key]=edgeCountMap[key]+1;
				else:
					edgeCountMap[key]=1;
				num=len(expertList)*edgeCountMap[key];
                                den=neCountMap[neList[i]]*neCountMap[neList[j]];


				G[neList[i]][neList[j]]['weight']=(num/den);
				'''
				if G[neList[i]][neList[j]].get('edgeCountExperts')==None:
					G[neList[i]][neList[j]]['edgeCountExperts']=1;
				else:
					G[neList[i]][neList[j]]['edgeCountExperts']=G[neList[i]][neList[j]]['edgeCountExperts']+1;
				num=expertCountPerTopic[tweet['topic']]*G[neList[i]][neList[j]]['edgeCountExperts'];
				den=G[neList[i]]['nodeCountExperts']*G[neList[j]]['nodeCountExperts'];
				G[neList[i]][neList[j]]['weight']=(num/den);
				#G.add_edge(neList[i],neList[j],weight=(num/den));
				'''

#print graph config (before pruning)
#print "BEFORE PRUNING"
#print "Node Count = "+str(len(G));
#print "Edge Count = "+str(G.size());

#Delete the nodes with nodeCountExperts<minExperts
nodeToBeDeleted=[];
for n,d in G.nodes_iter(data=True):
	if neCountMap[n]<minExperts:
		nodeToBeDeleted.append(n);
G.remove_nodes_from(nodeToBeDeleted);


#print graph config (after pruning)
#print "AFTER PRUNING"
#print "Node Count = "+str(len(G));
#print "Edge Count = "+str(G.size());
#print "Total tweets = "+str(tweetCount);
#datetime.datetime.now();
#print "Selected tweets size = "+str(len(tweetList));

print (nx.pagerank(G));
#for a in tweetList:
	#f1.write(a.encode('utf-8')+"\n");
	#print a.encode('utf-8');
