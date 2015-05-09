import sys
import numpy as np
from sklearn.pipeline import Pipeline
from sklearn import metrics
from sklearn.ensemble import RandomForestClassifier
from sklearn.ensemble import ExtraTreesClassifier
from sklearn.ensemble import AdaBoostClassifier
from sklearn import preprocessing

def numpyCreate(dataset):
	#create numpy arrays for scikit learn.
	feat=[];
	target=[];
	for sample in dataset:
		content=sample.strip().split(',');
		#content=sample.strip().split(' ');
		f=[];
		for i in range(len(content)-1):
			#f.append(float(content[i+1].split(':')[1]));
			f.append(float(content[i+1]));
		feat.append(np.array(f));
		target.append(content[0]);

	'''
	Feature normalization
	'''
	#Method 1
	feat=preprocessing.scale(feat);

	#Method 2
	#feat=preprocessing.normalize(feat,norm='l2');

	return (feat,target);

def classify(train_set,test_set):
	#Classification algorithm.
	train_feat,train_target=numpyCreate(train_set);
	test_feat,test_target=numpyCreate(test_set);

	#Build the pipeline
	clf=Pipeline([('clf', RandomForestClassifier(max_features=3,n_estimators=100))]);
	#clf=Pipeline([('clf', ExtraTreesClassifier(max_features=3,n_estimators=100))]);
	#clf=Pipeline([('clf', AdaBoostClassifier(n_estimators=100))]);


	#Training
	clf.fit(train_feat,train_target);

	#Testing
	predicted=clf.predict(test_feat);

	#print np.mean(predicted==test_target);	

	target_names=['0','1'];
	print(metrics.classification_report(test_target, predicted,target_names=target_names));

def main():
	#Artificial python main.
	if len(sys.argv)!=3:
		print("Missing arguments.\nFORMAT : python classify.py <file> <numfolds>");
		sys.exit();
	
	#Read the entire dataset from the specified file.
	file=sys.argv[1];
	dataset=[];
	with open(file) as f:
		dataset=f.readlines();

	#Do n fold cross folding.
	num_folds=int(sys.argv[2]);
	subset_size=len(dataset)/num_folds;
	for i in range(num_folds):
		test_set=dataset[i*subset_size:][:subset_size];
		train_set=dataset[:i*subset_size]+dataset[(i+1)*subset_size:];
		classify(train_set,test_set);

main();