package com.salience.collect;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Simplex {
	final static boolean isDebug=true; //Enable debug messages.
	
	public static void main(final String[] argv) throws Exception{
		boolean isMaxProb=false; //True if it's a maximization problem.
		int noOfConstraints=-1; //Count of constraints. 1 <= noOfConstraints <= 3
		Expression objectiveFn; //Coefficient of the objective function.
		final List<Expression> inputConstraints=new ArrayList<Expression>(); //Constraint matrix.
		final BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
		
		//Process the first line of the input.
		final String[] firstLineContent=reader.readLine().split(" ");
		if(firstLineContent[0].equals("MAX")) isMaxProb=true;
		noOfConstraints=Integer.parseInt(firstLineContent[1]);
		
		//Process the second line.
		final String[] secondLineContent=reader.readLine().split(" ");
		objectiveFn=new Expression();
		if(isMaxProb){
			objectiveFn.values.add(Integer.parseInt(secondLineContent[0]));
			objectiveFn.values.add(Integer.parseInt(secondLineContent[1]));
		} else {
			objectiveFn.values.add(-Integer.parseInt(secondLineContent[0]));
			objectiveFn.values.add(-Integer.parseInt(secondLineContent[1]));
		}
		
		//Process the remaining input.
		for(int i=0;i<noOfConstraints;i++){
			final String[] constraintContent=reader.readLine().split(" ");
			final Expression expression=new Expression();
			for(int j=0;j<constraintContent.length;j++)
				if(j==2) 
					expression.rhs=Double.parseDouble(constraintContent[j]);
				else
					expression.values.add(Integer.parseInt(constraintContent[j]));
			inputConstraints.add(expression);
			//objectiveFn.values.add(0);
		}
		
		//Execute Simplex algorithm.
		solve(isMaxProb,objectiveFn,inputConstraints);
		
		reader.close();		
	}
	
	public static List<Entry> startTableau(final boolean isMaxProb,final List<Entry> table,final Expression objectiveFn,final int noOfInputConstraints,final boolean printOutput){
		int rowSize=noOfInputConstraints;
		int colSize=objectiveFn.values.size();
		if(isDebug) printTable(table);
		
		while(true){
			int pivotColumn=-1;	
			double pivotValue=0.0;
			//Identify the pivot column.
			for(int colIndex=0;colIndex<colSize;colIndex++){
				double res=0.0;
				for(int rowIndex=0;rowIndex<rowSize;rowIndex++){
					final Entry curRow=table.get(rowIndex);
					res+=curRow.values.get(colIndex)*objectiveFn.values.get(curRow.label);
				}
				res=objectiveFn.values.get(colIndex)-res;
				if(res>0){
					if(pivotColumn==-1 || res>pivotValue){
						pivotColumn=colIndex;
						pivotValue=res;
					}
				}
			}			
			
			if(pivotColumn==-1){
				//Simplex - Termination - No valid pivot column.
				//Initialize the solution vector.
				double[] solVect=new double[objectiveFn.values.size()];
				for(int i=0;i<solVect.length;i++)
					solVect[i]=0;
				
				//Compute the result.				
				double result=0.0;				
				for(int rowIndex=0;rowIndex<rowSize;rowIndex++) {
					final Entry entry=table.get(rowIndex);
					result+=objectiveFn.values.get(entry.label)*entry.rhs;
					solVect[entry.label]=entry.rhs;
				}				
				if(isDebug) System.out.println("Can't find pivot column.");
				if(printOutput) System.out.printf("%.2f %.2f\n%.2f\n",solVect[0],solVect[1],(isMaxProb || result==0.0)?result:-1*(result));
				return table;
			}
			if(isDebug) System.out.println("Entering variable - "+(pivotColumn+1));
			
			//Identify the pivot row.
			int pivotRow=-1;
			pivotValue=0.0;
			for(int rowIndex=0;rowIndex<rowSize;rowIndex++) {
				final Entry entry=table.get(rowIndex);
				if(entry.values.get(pivotColumn)>0){
					double theta=entry.rhs/entry.values.get(pivotColumn);
					if(pivotRow==-1 || theta<pivotValue){
						pivotRow=rowIndex;
						pivotValue=theta;
					}
				}				
			}
			
			if(pivotRow==-1){
				//Simplex - Termination - No valid pivot row.
				//Compute the result.
				System.out.println("Solution region is Unbounded.");
				return null;
			}
			if(isDebug) System.out.println("Leaving variable - "+(1+table.get(pivotRow).label));
			
			//Divide the pivot row coefficients by the pivot value.
			pivotValue=table.get(pivotRow).values.get(pivotColumn);
			for(int colIndex=0;colIndex<colSize;colIndex++)
				table.get(pivotRow).values.set(colIndex,table.get(pivotRow).values.get(colIndex)/pivotValue);
			table.get(pivotRow).rhs=table.get(pivotRow).rhs/pivotValue;
			table.get(pivotRow).label=pivotColumn;
			
			//Do the changes for the other rows
			final Entry pivotRowEntry=table.get(pivotRow);
			for(int rowIndex=0;rowIndex<rowSize;rowIndex++) {
				if(rowIndex!=pivotRow){
					double pivotColumnValue=table.get(rowIndex).values.get(pivotColumn);
					for(int colIndex=0;colIndex<colSize;colIndex++){
						double newValue=table.get(rowIndex).values.get(colIndex);
						newValue=newValue-(pivotColumnValue*pivotRowEntry.values.get(colIndex));
						table.get(rowIndex).values.set(colIndex,newValue);
					}					
					table.get(rowIndex).rhs=table.get(rowIndex).rhs-(pivotColumnValue*pivotRowEntry.rhs);
				}
			}
			
			if(isDebug) printTable(table);				
		}
	}
	
	public static void solve(final boolean isMaxProb,final Expression objectiveFn,final List<Expression> inputConstraints){
		//Phase1 - Input selection phase. 
		//Returns the initial basic feasible solution.
		final List<Entry> table=new ArrayList<Entry>();
		
		//Check if all the RHS are +ve, then the +ve slack variables are its inital BFS.
		int count=countNegativeRHS(inputConstraints);
		if(count==0){			
			//Fill the constraint entries
			int start=2;
			for(int i=0;i<inputConstraints.size();i++)
				objectiveFn.values.add(0);
			for(final Expression exp:inputConstraints){
				final Entry entry=new Entry();
				entry.label=start;
				start++;
				entry.rhs=exp.rhs;
				for(int i=0;i<objectiveFn.values.size();i++){
					if(i<exp.values.size()){
						entry.values.add((double)exp.values.get(i));
					} else if(i==entry.label){
						entry.values.add(1.0);
					} else {
						entry.values.add(0.0);
					}
				}
				table.add(entry);
			}
			startTableau(isMaxProb,table,objectiveFn,inputConstraints.size(),true);
		} else {			
			//Input Selection Phase
			//Generate the new Objective fn and input constraints.
			final Expression newObjectiveFn=new Expression();
			newObjectiveFn.values.add(0);
			newObjectiveFn.values.add(0);
			final List<Integer> artSlackList=new ArrayList<Integer>();
			int totalSlacks=(inputConstraints.size()-count)+count*2;
			int curSlackIndex=2;
			for(int i=0;i<inputConstraints.size();i++){
				final Expression curExp=inputConstraints.get(i);
				final Expression newExp=new Expression();
				newExp.rhs=curExp.rhs;
				if(curExp.rhs<0){
					//Multiply the constraint coeff. by -1.
					for(final Integer coeff:curExp.values)
						newExp.values.add(-1*coeff);
					//Initialize slack entries
					for(int ii=0;ii<totalSlacks;ii++)
						newExp.values.add(0);
					//Add negative slack
					newObjectiveFn.values.add(0);
					newExp.values.set(curSlackIndex,-1);
					curSlackIndex++;
					//Add artificial slack
					newObjectiveFn.values.add(-1);
					newExp.values.set(curSlackIndex,1);
					artSlackList.add(curSlackIndex);
					curSlackIndex++;
					newExp.rhs=(-1*newExp.rhs);
				} else {
					for(final Integer coeff:curExp.values)
						newExp.values.add(coeff);
					//Initialize slack entries
					for(int ii=0;ii<totalSlacks;ii++)
						newExp.values.add(0);
					//Add positive slack
					newObjectiveFn.values.add(0);
					newExp.values.set(curSlackIndex,1);
					curSlackIndex++;
				}
				
				//Compose the entry for the table.
				final Entry newEntry=new Entry();
				newEntry.rhs=newExp.rhs;
				newEntry.label=curSlackIndex-1;
				for(final Integer coeff:newExp.values)
					newEntry.values.add((double)coeff);
				table.add(newEntry);				
			}
			
			if(isDebug) System.out.println("\nPHASE 1 START");
			final List<Entry> ansTable=startTableau(isMaxProb,table, newObjectiveFn, inputConstraints.size(),false);
			if(ansTable==null) //Solution region is unbounded. 
				return;
			
			//Check for infeasibility.
			for(int i=0;i<inputConstraints.size();i++)
				if(artSlackList.indexOf(table.get(i).label)!=-1) { //Infeasible when the solution contains art. slack variables.
					System.out.println("Soultion is infeasible.");
					return;
				}
			
			//Start the phase 2 with the obtained b.f.s.
			//Update the obj fn.
			newObjectiveFn.values.set(0, objectiveFn.values.get(0));
			newObjectiveFn.values.set(1, objectiveFn.values.get(1));
			for(int i=2;i<newObjectiveFn.values.size();i++)
				newObjectiveFn.values.set(i,0);
			
			//Remove the artificial variable entries from the table.
			//By column
			for(int i=0;i<ansTable.size();i++)
				for(int ii=artSlackList.size()-1;ii>=0;ii--)
					ansTable.get(i).values.set((int)artSlackList.get(ii),0.0);		
			
			if(isDebug) System.out.println("\nPHASE 2 START");
			startTableau(isMaxProb,ansTable, newObjectiveFn, inputConstraints.size(),true);			
		}
	}
	
	public static int countNegativeRHS(final List<Expression> inputConstraints){
		//returns true only if all the RHS of the constraints have +ve value.
		int count=0;
		for(final Expression exp:inputConstraints)
			if(exp.rhs<0)
				++count;
		return count;
	}	
	
	public static void printTable(final List<Entry> table){
		for(final Entry entry:table){
			System.out.print((entry.label)+"\t");
			for(final Double val:entry.values)
				System.out.printf("%.2f\t",val);
			System.out.println(entry.rhs);			
		}		
	}

}

class Entry{
	public int label=-1;
	public double rhs=-1;
	public List<Double> values=new ArrayList<Double>();		
}

class Expression{
	public List<Integer> values=new ArrayList<Integer>();
	public double rhs=-1;
}
