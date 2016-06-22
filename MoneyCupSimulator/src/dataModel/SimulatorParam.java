package dataModel;

import java.util.ArrayList;
import java.util.Date;

public class SimulatorParam {
	
	private ArrayList<Bucket> initAmountTable = new ArrayList<Bucket>();
	private ArrayList<Bucket> initCBTable = new ArrayList<Bucket>();
	private ArrayList<Bucket> periodicTransferTable = new ArrayList<Bucket>();
	private ArrayList<Bucket> withdrawTable = new ArrayList<Bucket>();
	
	private double dAnnualClosingRate = 0.05;
	private double monthlyflatfee = 1.5;
	private double monthlyfee = 0.005;
	private double studentProba = 0.2;
	private double callCenterHourCost = 15;
	private double callCenterCallAtOpenPct = 0.05;
	private double callCenterCallMonthlyProba = 0.005;
	private double averageCallTime = 10.0/60;
	
	
	
	public SimulatorParam() {

		// fonction de répartition des montants initiaux
		initAmountTable.add(new Bucket(0.6 , 0));
		initAmountTable.add(new Bucket(0.8 , 10));
		initAmountTable.add(new Bucket(0.9 , 50));
		initAmountTable.add(new Bucket(0.95 , 100));
		initAmountTable.add(new Bucket(1.0 , 1000));
		
		// fonction de répartition du nombre de CB par semaine (ne peut contenir 0 CB par semaine à cause des frais)
		initCBTable.add(new Bucket(0.8 , 4));
		initCBTable.add(new Bucket(0.85 , 7));
		initCBTable.add(new Bucket(0.9 , 20));
		initCBTable.add(new Bucket(0.95 , 30));
		initCBTable.add(new Bucket(1.0 , 40));
		
		// fonction de répartition du montant viré périodiquement
		periodicTransferTable.add(new Bucket(0.7 , 0));
		periodicTransferTable.add(new Bucket(0.85 , 10));
		periodicTransferTable.add(new Bucket(0.90 , 50));
		periodicTransferTable.add(new Bucket(0.95 , 100));
		periodicTransferTable.add(new Bucket(1.0 , 500));
		
		// fonction de répartition du nombre de  virement par an
		withdrawTable.add(new Bucket(0.6 , 0));
		withdrawTable.add(new Bucket(0.8 , 2));
		withdrawTable.add(new Bucket(1.0 , 12));		
		
	}

	double getmonthlyflatfee(){
		return this.monthlyflatfee;
	}


	double getmonthlyfee(){
		return this.monthlyfee;
	}

	double getdAnnualClosingRate(){
		return this.dAnnualClosingRate;
	}
	
	double getstudentProba(){
		return this.studentProba;
	}
	double getCBRounding(int i){
		if(i==0){
			return Math.round(initCBTable.get(i).getiValue() * Math.random() /5 ) * 0.5;
		}else
			return Math.round(((initCBTable.get(i).getiValue()-initCBTable.get(i-1).getiValue()) * Math.random() + initCBTable.get(i).getiValue()) /5 ) * 0.5;
	}
	
	
	int getLowerBound(ArrayList<Bucket> tab, int level){
		int i = 0;
		while(true){			
			if(tab.get(i).getiValue() >= level) break;
			i++;
		}
		if(i>0){
			return tab.get(i-1).getiValue();
		}else{
			return tab.get(i).getiValue();
		}
	}
		

	int getUpperBound(ArrayList<Bucket> tab, int level){
		int i = 0;
		while(true){			
			if(tab.get(i).getiValue() >= level) break;
			i++;
		}
		return tab.get(i).getiValue();
	}

	int getcatCB(double dThreshold){
		int i = 0;
		while(true){			
			if(initCBTable.get(i).getdThreshold() >= dThreshold) break;
			i++;
		}
		return i;
	}
	
	int getValueFromTable(ArrayList<Bucket> tab, double dThreshold){
		int i = 0;
		while(true){			
			if(tab.get(i).getdThreshold() >= dThreshold) break;
			i++;
		}

		if(i==0){
			return (int)Math.round(Math.random()*tab.get(0).getiValue());
		}else{
			return (int)Math.round(Math.random()*(tab.get(i).getiValue()-tab.get(i-1).getiValue()-1) 
							  + tab.get(i-1).getiValue()+1);
		}
	}
	
	int getinitAmountTable(double dThreshold){
		return getValueFromTable(initAmountTable, dThreshold);	
		}
		
//	double getinitCBTable(double dThreshold){
//		return getValueFromTable(initCBTable, dThreshold);	
//		}

	int getPeriodicTransferTable(double dThreshold){
		return getValueFromTable(periodicTransferTable, dThreshold);	
		}
	
	int getWithdrawTable(double dThreshold){
		return getValueFromTable(withdrawTable, dThreshold);	
		}
	
	ArrayList<Bucket> getperiodicTransferTable(){
		return periodicTransferTable;
	}
	
	ArrayList<Bucket> getinitAmountTable(){
		return initAmountTable;
	}

	public double getCallCenterHourCost() {
		return callCenterHourCost;
	}

	public double getCallCenterCallAtOpenPct() {
		return callCenterCallAtOpenPct;
	}

	public double getCallCenterCallMonthlyProba() {
		return callCenterCallMonthlyProba;
	}

	public double getAverageCallTime() {
		return averageCallTime;
	}


}
