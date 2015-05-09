package utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map.Entry;

import Variables.Variables;

public class Logger {

	public static void main(String[] args) {
		Logger logger = new Logger();
		logger.logOut("Hello");
	}
	
	private static String outputDirectory;
	    
    public Logger() {
    	outputDirectory = Variables.OutputDir; 
    	//open this file in append mode
    }   
	
	public enum level {
	    SEVERE, MODERATE
	    //, MINOR, NOTIFY 
	}
	
	//Level level;
	public static void log(level l, String logMessage){
		switch(l){
		case SEVERE:
			System.out.println("Severe log : "+ logMessage);
			break;
		case MODERATE:
			System.out.println("Moderate log : "+ logMessage);
			break;		
		default:
			System.out.println(" log : "+ logMessage);
			break;
		}//End switch			
	}

	/*Append replab o/p to Replab_SysOut.csv*/
	public static void ReplabSysOut(StringBuilder soln) {    	
		try {
			File file = new File(Variables.OutputDir.concat("Replab_SysOut.csv"));
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);//open in append mode
			BufferedWriter bw = new BufferedWriter(fw);
			String content = soln.toString(); //System.out.println(content);
			bw.write(content);
			bw.close(); //System.out.println("Done");
 		} catch (IOException e) {
			e.printStackTrace();
		}
	}//End EvalOut

	/*Append replab gold o/p to Replab_GoldOut.csv*/
	public static void ReplabGoldOut(StringBuilder soln) {    	
		try {
			File file = new File(Variables.OutputDir.concat("Replab_GoldOut.csv"));
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);//open in append mode
			BufferedWriter bw = new BufferedWriter(fw);
			String content = soln.toString(); //System.out.println(content);
			bw.write(content);
			bw.close(); //System.out.println("Done");
 		} catch (IOException e) {
			e.printStackTrace();
		}
	}//End GoldOut


	
	
	
	/*Append STATUS message to EvalReport.csv*/
	public static void EvalOut(StringBuilder soln) {    	
		try {
			File file = new File(Variables.OutputDir.concat("EvalReport.csv"));
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);//open in append mode
			BufferedWriter bw = new BufferedWriter(fw);
			String content = soln.toString(); //System.out.println(content);
			bw.write(content);
			bw.close(); //System.out.println("Done");
 		} catch (IOException e) {
			e.printStackTrace();
		}
	}//End EvalOut

	/*Append STATUS message to StatusReport.csv*/
	public static void StatusOut(StringBuilder soln) {    	
		try {
			File file = new File(Variables.OutputDir.concat("StatusReport.csv"));
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);//open in append mode
			BufferedWriter bw = new BufferedWriter(fw);
			String content = soln.toString(); //System.out.println(content);
			bw.write(content);
			bw.close(); //System.out.println("Done");
 		} catch (IOException e) {
			e.printStackTrace();
		}
	}//End StatusOut

	/*Store PageRank to pageRank.csv*/
	public static boolean rankOut(StringBuilder soln) {
    	boolean writting = false;
		try {
			File file = new File(Variables.LibDir.concat("PageRank.csv"));
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			} 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());//open in append mode
			BufferedWriter bw = new BufferedWriter(fw);
			String content = soln.toString(); //System.out.println(content);
			if(content.length()>0){
				writting = true;
			}
			bw.write(content);
			bw.close(); //System.out.println("Done");
 		} catch (IOException e) {
			e.printStackTrace();
		}
		return writting;
    }//End rankOut

	/*Append Adjacency matrix to TrainVector.csv*/
	public static boolean trainsetOut(StringBuilder soln, boolean testData) {
    	boolean writting = false; String fileName;
    	if(testData){//true
    		fileName = "TestVector.csv";
    	} else {
    		fileName = "TrainVector.csv";
    	}
		try {
			File file = new File(Variables.OutputDir.concat(fileName));
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			} 
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			String content = soln.toString(); //System.out.println(content);
			if(content.length()>0){
				writting = true;
			}
			bw.write(content);
			bw.close(); //System.out.println("Done");
 		} catch (IOException e) {
			e.printStackTrace();
		}
		return writting;
    }//End trainsetOut

	
	
	/*Append Adjacency matrix to Adjacency.csv*/
	public static boolean solutionOut(StringBuilder soln) {
    	boolean writting = false;
		try {
			File file = new File(Variables.OutputDir.concat("Adjacency.csv"));
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			} 
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);//open in append mode
			BufferedWriter bw = new BufferedWriter(fw);
			String content = soln.toString(); //System.out.println(content);
			if(content.length()>0){
				writting = true;
			}
			bw.write(content);
			bw.close(); //System.out.println("Done");
 		} catch (IOException e) {
			e.printStackTrace();
		}
		return writting;
    }//End solutionOut
	
	public static boolean solutionTab(StringBuilder soln) {
    	boolean writting = false;
		try {
			File file = new File(Variables.OutputDir.concat("Solution.tab"));
			if (!file.exists()) {
				file.createNewFile();
			} 
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);//open in append mode
			BufferedWriter bw = new BufferedWriter(fw);
			String content = soln.toString(); //System.out.println(content);
			if(content.length()>0){
				writting = true;
			}
			bw.write(content);
			bw.close(); //System.out.println("Done");
 		} catch (IOException e) {
			e.printStackTrace();
		}
		return writting;
    }//End solutionTab
	

	/*Append wiki API results to wikiOut.txt*/
	public static boolean wikiOut(String content) {
    	boolean writting = false;
		try {
			File file = new File(Variables.OutputDir.concat("wikiOut.txt"));
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			} 
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);//open in append mode
			BufferedWriter bw = new BufferedWriter(fw);
			if(content.length()>0){
				writting = true;
			}
			bw.write(content);
			bw.close(); //System.out.println("Done");
 		} catch (IOException e) {
			e.printStackTrace();
		}
		return writting;
    }//End wikiOut
	

	/*Append disambiguation to disambiguations.txt*/
	public static boolean disambiguationOut(String a, ArrayList<String> disambiguation, char bPT) {
		String filename = null;
		switch (bPT) {
		case 'b':
			filename = Variables.OutputDir.concat("disambiguations.txt");	
			break;
		case 'P':
			filename = Variables.OutputDir.concat("PLdisambiguations.txt");	
			break;
		case 'T':
			filename = Variables.OutputDir.concat("tagmeDisambiguations.txt");	
			break;
		default:
			break;
		}
		
		
    	boolean writting = false;
		try {
			File file = new File(filename);
			// if file doesnt exists, then create it
			if (!file.exists()) { file.createNewFile(); } 
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);//open in append mode
			BufferedWriter bw = new BufferedWriter(fw);
			//System.out.println(a +"#"+ disambiguation);
			if(disambiguation.size()>0){
				writting = true;
			}
			bw.write(a + "#"+ disambiguation+"\n");
			bw.flush();
			bw.close(); //System.out.println("Done");
 		} catch (IOException e) {
			e.printStackTrace();
		}
		return writting;
    }//End disamOut
	
	/*Clear contents of a file*/
	public static void clearFile(String filename){
		try {
			File file = new File(filename);		
			PrintWriter writer = new PrintWriter(file);
			writer.print("");
			writer.close();
 		} catch (IOException e) {
			e.printStackTrace();
		}		
	}//clearFile
	
	
	/*log message in log.<timestamp> file*/
    public static boolean logOut(String message) {
    	boolean writting = false;
		try {
			java.util.Date date= new java.util.Date();
			//File file = new File(Variables.TACOutputDir.concat("log.").concat(new java.sql.Timestamp(date.getTime()).toString()));
			@SuppressWarnings("deprecation")
			File file = new File(Variables.OutputDir.concat("log.txt"));
			//File file = new File(Variables.TACOutputDir.concat("log.").concat(String.valueOf(Math.random())));
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			} 
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw);
			String content = message +"\n";
			//System.out.println(content);
			if(content.length()>0){
				writting = true;
			}
			bw.write(content);
			bw.close(); //System.out.println("Done");
 		} catch (IOException e) {
			e.printStackTrace();
		}
		return writting;
    }//End logOut

    /*print out a map*/
    public static void printOutMap(HashMap<String, Integer>myMap){
    	for(String a:myMap.keySet()){
    		System.out.println(a);//System.out.println(a+" "+myMap.get(a));
    	}
    }
    
    public static boolean flushAdjacencyMap(HashMap<String,ArrayList<String>> NEadjacencyListMap) {
    	boolean writting = false;
		try {
			File file = new File(Variables.OutputDir.concat(Variables.adjacencyMap));
			if (!file.exists()) {
				file.createNewFile();
			} 
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);//open in append mode
			BufferedWriter bw = new BufferedWriter(fw);
			for(Entry<String,ArrayList<String>> ne : NEadjacencyListMap.entrySet()){
				bw.write(ne.toString()+"\n");
				writting = true;
			}			
			bw.close(); //System.out.println("Done");
 		} catch (IOException e) {
			e.printStackTrace();
		}		
		return writting;
    }//End flushAdjacencyMap
    
    public static boolean storeNEMap(HashMap<String,Integer> NEMap) {
    	boolean writting = false;
		try {
			File file = new File(Variables.OutputDir.concat(Variables.neMap));
			if (!file.exists()) {
				file.createNewFile();
			} 
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);//open in append mode
			BufferedWriter bw = new BufferedWriter(fw);
			for(Entry<String,Integer> ne : NEMap.entrySet()){
				bw.write(ne.toString()+"\n");
				writting = true;
			}			
			bw.close(); //System.out.println("Done");
 		} catch (IOException e) {
			e.printStackTrace();
		}		
		return writting;
    }//End storeNEMap
	
}
