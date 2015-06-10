SNEIT: Salient Named Entity Identification in Tweets
-----------------------------------------------------
This paper proposes a novel solution to the problem of identifying Salient Named Entities (SNEs) from a tweet. SNEs are a subset of named entities identified from a tweet, that are central to a tweet's content and capture the tweet author's intention. Automatic identification of SNEs can aid several Information Retrieval tasks including filtering, clustering, user modeling, trend prediction and content analysis. The problem is challenging due to the  subjective nature of the SNEs and the unique characteristics of the tweet text. In this paper, we propose a two-phased approach to identify the SNEs. Firstly, we identify salient mentions in a tweet using an unsupervised algorithm. Secondly, a supervised classifier, built using a robust set of features, is used to disambiguate and link salient mention to a Knowledge Base
entry. We show that our method gives 12% higher precision compared to the baseline method. This performance is consistent when evaluated on a standard dataset for filtering task, with tweets spanning across four domains. We demonstrate that a dataset created by our method can scale up
well. We also make the human-annotated dataset we created for our experiments, publicly available to the research community.

Folder Structure
-------------------
Datasets - Contains the different datasets created and used in the paper. 
Java - Contains all the system utilities coded in Java.
Python - Contains all the system utilities coded in Python.

Compiling the code:
------------------------
1. Mongo : Download the Datasets in this package. Setup mongodb with this using TweetIndexer class in Java.
2. twitter4j.properties : Populate this file with twitter API credentials and place in the execution directory
3. AppGlobals.java : Populate the location of NER servers here, as shown in example.
4. Variables.java : The path to all the dataset , db, server and APIs should be populated here, as shown in examples.

How to run this code:
-----------------------
PREPROCESSING: Creates page rank vector
> python preprocess.py <page rank node repetition factor> <Cognos topic>

Sample Usage :-
python preprocess.py 2 "mit" > ../Pagerank/mit.pgrk


EVALUATION :
> java -jar ReplabData.jar <replab query>

Sample Usage :-
> java -jar ReplabData.jar "Ferrari"


SCALING :
1. Create page rank vector and tweets for SNEIT dataset
> python scaler.py <page rank node repetition factor> <Cognos topic>
2. Create SNIET dataset
> java -jar Scaler.jar <Cognos Topic> <Sne repetition factor> 

Sample Usage :-
> pythom scaler.py 2 "barclays" 
> java -jar Scaler.jar "barclays" "2"


EVALUATION of TSNE dataset :
>python TSNE_evaluate.py <number_of_tsne_rows>

Sample usage :
python TSNE_evaluate.py 10

This will randomly show TSNE dataset samples as 
<Tweet> -- [ranked list of SNEs]
For each sample, one can enter 1(SNE) or 0(NOT) and system outputs accuracy of TSNE dataset.

SETTING UP SERVERS:
------------------------------------
A. SEIMP Annotator Setup

Pre-requisites:
1. Install latest Java (http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
2. Install latest Apache Tomcat Server (http://tomcat.apache.org/).
3. Download the war file from machine (10.2.4.38) in the path (/var/lib/tomcat8/webapps).
4. Install latest Mongo DB (https://www.mongodb.org/downloads). Make sure the required collection is already loaded in to the DB. (Access collections from https://github.com/priyaradhakrishnan0/SNEIT/tree/master/Datasets)
NOTE - All the above listed actions should be performed on the same machine.

Instructions:
1. Stop the tomcat (if needed).
2. Copy the war to $TOMCAT_SRC/webapps
3. Start the tomcat (You should see a new folder with the same name as war getting created)
4. Hit the following URL in the browser. (http://localhost:8080/SEIMPAnnotator/)
5. Enter the password 'abracadabra'.
6. Enjoy!

B. Tweet Linker Setup

Pre-requisites
1. Install python flask as 'root'
2. Generate training_data.csv using SNEIT/Java/SalientNamedEntity/src/salience/dataset/TrainingData.java
3. Download SNEIT/Python/linking/linker.py

Instructions
1. python linker.py trainVector.csv
2. Access the linker over the web. Example "http://localhost:5050/classify?feat=1,1,1,1,1,1,1,0"
