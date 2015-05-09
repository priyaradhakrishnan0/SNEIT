package salience.dataset;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Variables.Variables;

import com.google.gson.Gson;

public class DatasetRead {
	
	public static void main(final String[] argv) throws Exception {
		DatasetRead datasetRead = new DatasetRead();
		System.out.println("Size = "+datasetRead.getTrainigData().size());
		//datasetRead.evaluateNERs();
		datasetRead.showDatasetStatistics();
	}//main
	
	/*create training data from SEIMP dataset*/
	public HashMap<String, List<KBAnnotation>> getTrainigData(){
		HashMap<String, List<KBAnnotation>> trngData = new HashMap<String, List<KBAnnotation>>();
		//read seimp dataset
		try{
			final BufferedReader reader=new BufferedReader(new FileReader(Variables.OutputDir.concat(Variables.SEIMPfile)));
			String line=null, tweet = null;
			while((line=reader.readLine())!=null){
				final SeimpTrainingRow row = (SeimpTrainingRow) convertToPOJO(line,"salience.dataset.SeimpTrainingRow");
				List<Annotation> annList=row.getAnnotationList();
				if(annList!=null && annList.size()>0){					
					Annotation ann=annList.iterator().next();
					List<KBAnnotation> kbList=ann.getKbList();
					if(ann.getComments().trim().length()==0 && kbList!=null && kbList.size()>0){
						tweet = row.getText();
//						for(final KBAnnotation kb:kbList){
//							kb.getKbEntry();
//						}
						trngData.put(tweet, kbList);
					}
				}

			}
			reader.close();
		}catch(IOException | ClassNotFoundException io){
			io.printStackTrace();
		}
		//get salient mention
		//get feature vector
		return trngData;
	}//getTraining data

	
	private static final Gson gson = new Gson();

	public static String convertToJson(final Object content) {
		// Converts the pojo rep. to json rep.
		return gson.toJson(content);
	}

	public static Object convertToPOJO(final String content,
			final String pojoClassName) throws ClassNotFoundException {
		// Converts the content in JSON format to POJO.
		Class clazz = Class.forName(pojoClassName);
		return gson.fromJson(content, clazz);
	}

	/*evaluate NER performances with SEIMP dataset*/
	public void evaluateNERs(){
		HashMap<String, List<KBAnnotation>> trngData = new HashMap<String, List<KBAnnotation>>();
		int AR=0, GP=0, ST =0, Comb =0, Tot =0;
		//read seimp dataset
		try{
			final BufferedReader reader=new BufferedReader(new FileReader(Variables.OutputDir.concat(Variables.SEIMPfile)));
			String line=null, tweet = null;

			while((line=reader.readLine())!=null){
				final SeimpTrainingRow row = (SeimpTrainingRow) convertToPOJO(line,"salience.dataset.SeimpTrainingRow");
				List<NERList> nerLists=row.getNerList();
				if((nerLists != null) && (nerLists.size()>0)){
					NERList nerList;
					for(int i=0; i<nerLists.size(); ++i){
						nerList = nerLists.get(i);
						String nerMake = nerList.getName();
						if(nerMake.contains("ALAN_RITTER")){
							if(nerList.getNeList().size()>0){
								AR += nerList.getNeList().size();
							}
						}
						if(nerMake.contains("ARK_TWEET")){
							if(nerList.getNeList().size()>0){
								GP += nerList.getNeList().size();
							}
						}
						if(nerMake.contains("STANFORD_CRF")){
							if(nerList.getNeList().size()>0){
								ST += nerList.getNeList().size();
							}
						}						
					}//for nerList					
				}//if
				Comb += row.getMergedNeList().size();
				++Tot;
			}//while
			reader.close();
		}catch(IOException | ClassNotFoundException io){
			io.printStackTrace();
		}
		System.out.println("AR = "+AR+" AT = "+GP+" SF = "+ST);
		System.out.println("AVERAGE :: AR = "+1.0*AR/Tot+" AT = "+1.0*GP/Tot+" SF = "+1.0*ST/Tot+" Comb = "+1.0*Comb/Tot);
	}//evaluateNERs

	/*display training data parameters for SEIMP dataset*/
	public void showDatasetStatistics(){
		//HashMap<String, List<KBAnnotation>> trngData = new HashMap<String, List<KBAnnotation>>();
		//read seimp dataset
		int N = 0, D = 0, S = 0, P = 0, withNE = 0, withKB = 0;
		try{
			final BufferedReader reader=new BufferedReader(new FileReader(Variables.OutputDir.concat(Variables.SEIMPfile)));
			String line=null, tweet = null;			
			while((line=reader.readLine())!=null){
				++N;
				final SeimpTrainingRow row = (SeimpTrainingRow) convertToPOJO(line,"salience.dataset.SeimpTrainingRow");
				
				List<Annotation> annList=row.getAnnotationList();
				if(annList!=null && annList.size()>0){					
					Annotation ann = annList.iterator().next();
					String annotatorComment = ann.getComments().trim();
					if(annotatorComment.length()==1){
						if(annotatorComment.equalsIgnoreCase("D")) D++;
						else if(annotatorComment.equalsIgnoreCase("S")) S++;
						else if(annotatorComment.equalsIgnoreCase("P")) P++;
					}
					if(ann.getSneList() != null)
						withNE++;
					List<KBAnnotation> kbList = ann.getKbList();
					if(kbList!=null && kbList.size()>0){
						boolean hasKB=false;
						for(final KBAnnotation kb:kbList){
							if(!kb.getKbEntry().equalsIgnoreCase("none")){
								hasKB = true;
								break;
							}
						}
						if(hasKB) withKB++;
					}
				}

			}
			reader.close();
		}catch(IOException | ClassNotFoundException io){
			io.printStackTrace();
		}
		System.out.println("N = "+N+", D = "+D+", S = "+S+", P = "+P+", withNE = "+withNE+", withKB = "+withKB);
	}//getTraining data
	
	private static boolean isValidRecord(final SeimpTrainingRow row) {
        /*
         * Returns true if the record is valid one to process.
         */
        if(row.getAnnotationList()==null || row.getAnnotationList().size()==0) return false;
        final Annotation ann=row.getAnnotationList().iterator().next();
        return ann.getComments().trim().length()==0 && (ann.getKbList()!=null && ann.getKbList().size()!=0);
    }//isValidRecord

}
