package salience;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
//import version1.LinkClassifier;
import Variables.Variables;

public class TweetClassifier {
	String trainFile = Variables.LibDir.concat(Variables.LinkTrainVector);//csv");
	String modelFile = Variables.LibDir.concat(Variables.ModelVector);//linear_weighted_base.model");//rbf_w12_g8_nonNIL.model");//Solution.ds2.model");//Solution.model");
	public static svm_model liveModel;
	double[][] train;

	public TweetClassifier  (){
		liveModel = loadModel();
	}//constructor
	
	public void startUp() {
		//setDataCount
	    try {
	    	InputStream is = new BufferedInputStream(new FileInputStream(trainFile));
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        boolean endsWithoutNewLine = false;
	        while ((readChars = is.read(c)) != -1) {
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n')
	                    ++count;
	            }
	            endsWithoutNewLine = (c[readChars - 1] != '\n');
	        }
	        is.close();
	        if(endsWithoutNewLine) {
	            ++count;
	        } 
	        System.out.println("Loading size = "+count);
	        train = new double[count][];
	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	    //LoadTrainData		
		try {
			BufferedReader inp = new BufferedReader (new FileReader(new File(trainFile)));	
			String line = null;
			int j=0;
			while( (line = inp.readLine()) !=null  ){
				line = line.trim(); 
				double[] values = new double[5]; 
				for(String feature : line.split(",") ){
					String value; 
					int label;
					if(feature.equalsIgnoreCase("0") || feature.equalsIgnoreCase("1")){
						label = 0;
						value = feature;
					} else {
						label = Integer.valueOf(feature.split(":")[0].trim());
						value = feature.split(":")[1];
					}					
					values[label] = Double.valueOf(value);
				}	
				train[j] = values; System.out.println("Sample "+values.toString());
				++j;
			}	
			System.out.println("Loaded "+train.length+" samples.");
			
			inp.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//train the model
		svm_problem prob = new svm_problem();
	    int dataCount = train.length;
	    prob.y = new double[dataCount];
	    prob.l = dataCount;
	    prob.x = new svm_node[dataCount][];     

	    for (int i = 0; i < dataCount; i++){            
	        double[] features = train[i];
	        prob.x[i] = new svm_node[features.length-1];
	        for (int j = 0; j < (features.length-1); j++){
	            if(features[j]>0.0){
		            svm_node node = new svm_node(); //REVISIT - can this be constructed sparce?
		            node.index = j;
	            	node.value = features[j];
	            	prob.x[i][j] = node;
	            }	            
	        }           
	        prob.y[i] = features[0];
	    }               
	    System.out.println("Prob = "+prob.y);
	    svm_parameter param = new svm_parameter();
	    param.probability = 1;
	    param.gamma = 0.5;
	    param.nu = 0.5;
	    param.C = 1;
	    param.svm_type = svm_parameter.C_SVC;
	    param.kernel_type = svm_parameter.LINEAR;       
	    param.cache_size = 20000;
	    param.eps = 0.001;      

	    svm_model model = svm.svm_train(prob, param);

	    //save model into file 
		try {
			svm.svm_save_model(modelFile, model);
			System.out.println("Saved model");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}//startUp

	
	
	public double[] evaluate(double[] features, svm_model model) 
	{
	    int totalClasses = 2;  
		double[] retValues = new double[totalClasses + 1];
		//System.out.println(" Feature size = "+features.length);
	    svm_node[] nodes = new svm_node[features.length-1];
	    for (int i = 0; i < (features.length-1); i++)
	    {
	        svm_node node = new svm_node();
	        node.index = i;
        	node.value = features[i];
            nodes[i] = node;
	    }
     
	    int[] labels = new int[totalClasses];
	    svm.svm_get_labels(model,labels);

	    double[] prob_estimates = new double[totalClasses];
	    double v = svm.svm_predict_probability(model, nodes, prob_estimates);

	    for (int i = 0; i < totalClasses; i++){
	        System.out.println("(" + labels[i] + ":" + prob_estimates[i] + ")");
	        retValues[i] = prob_estimates[i];
	    }
	    System.out.println("(Actual:" + features[0] + " Prediction:" + v + ")");            
	    retValues[totalClasses] = v;
	    return retValues;
	}//evaluate
	
	public svm_model loadModel(){
		svm_model model = new svm_model();
		try {
			model = svm.svm_load_model(modelFile);
			System.out.println("Loaded model");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return model;
	}//loadModel
	
	public static void main(String[] args) {
		TweetClassifier tc = new TweetClassifier();
		//tc.startUp();  //only once to create model file
		tc.loadModel();
		double[] vals = {1, 0.14334878701979953, 0.10348673676084448, 0.04778292900659984, 0.10348673676084448 };
		System.out.println(vals.length);
		double[] a = tc.evaluate(vals, liveModel);
		System.out.println(a[2]);
	}


}//class
