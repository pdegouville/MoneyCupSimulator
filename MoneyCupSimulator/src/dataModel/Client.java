package dataModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;

public class Client {
	
	
	private int id;
	private int initialAmount;
	private int periodicAmount;
	private int closeDate;
	private int catCB;
	private SimulatorParam param;
	
	private int startIndex;
	
	private double[] cashMvt;
	private double[] savings;
	private double[] fees;
	private double[] callCenterCosts;
	private int[] MvtTab;
	private boolean[] useMonthlyFee;
	private boolean student;
	private boolean closedBeforeSaving = false;
	
	private int withdrawPeriod;
	private int[] withdrawDates;
	
	public Client(int id, int size, int startIndex, SimulatorParam param){
		this.id = id;
		this.startIndex = startIndex;
		this.param = param;
		cashMvt = new double[size];
		fees = new double[size];
		savings = new double[size];
		fees = new double[size];
		MvtTab = new int[size];
		callCenterCosts = new double[size];
		
		initialAmount = param.getinitAmountTable(Math.random());
		periodicAmount = param.getPeriodicTransferTable(Math.random());
		withdrawPeriod = param.getWithdrawTable(Math.random());
		catCB = param.getcatCB(Math.random());
		
		student = Math.random()<param.getstudentProba();
		
//		MyLogger.myLogger.log(Level.INFO, "init Amount = " + initialAMount + "("+ param.getLowerBound(param.getinitAmountTable(), initialAMount) + "," 
//		                                                                   + param.getUpperBound(param.getinitAmountTable(), initialAMount) + ")" +
//		                                  "; periodic Amount = " + periodicAmount + "("+ param.getLowerBound(param.getperiodicTransferTable(), periodicAmount) + "," 
//		                                                                   + param.getUpperBound(param.getperiodicTransferTable(), periodicAmount) + ")");
	
		closeDate = calcCloseDate();
		initWithdrawsDates();
	}

	public void computeCashMvt(ArrayList<Integer> feesDates){
		
		cashMvt[startIndex] = initialAmount;
		savings[startIndex] = initialAmount;
		MvtTab[startIndex]= 1;
		useMonthlyFee = new boolean[feesDates.size()];
		double buffer = 0;
		double epargne = initialAmount;
		int idRet = 0;
		int idFees = 0;
		while(feesDates.get(idFees)< startIndex) idFees++;
		for(int i =startIndex+1; i<cashMvt.length; i++){
			//cloture du compte
			if(i==closeDate){
				MvtTab[i]= 1;
				if(epargne==0){
					closedBeforeSaving = true;
				}
				cashMvt[i] = -epargne;
				savings[i] -= epargne;
				// on divise par 2 pour estimer le pro raté sur la période. Le client paie des fees jusqu'au moment où il sort
				if(student){
					fees[i] = epargne*param.getmonthlyfee()/12/2;
				}else{
					fees[i] = Math.max(epargne*param.getmonthlyfee()/12, param.getmonthlyflatfee())/2;
				}	
				break;
			}
			//virement periodique
			if(periodicAmount != 0){
				if((int)((i-startIndex)/30)*30 == i-startIndex){
					buffer += periodicAmount;
				}
			}
			//arrondis
			buffer += param.getCBRounding(catCB);
			// retraits
			if(idRet < withdrawDates.length){
				if(withdrawDates[idRet] == i){
					double retrait = -(int)(epargne*Math.random());
					buffer += retrait;
					idRet++;
				}
			}
			// vidage du buffer
			if(buffer>=5 || buffer < 0){
				epargne += buffer;
				savings[i] = epargne;
				cashMvt[i] = buffer;
				MvtTab[i] = 1;
				buffer=0;
			}
			// fees
			if(i==feesDates.get(idFees)){
				if(student){
					fees[i] = epargne*param.getmonthlyfee()/12;
				}else{
					fees[i] = Math.max(epargne*param.getmonthlyfee()/12, param.getmonthlyflatfee());
				}
				//l'épargne du client est > à 3600€
				if(fees[i]>param.getmonthlyflatfee()) useMonthlyFee[idFees] = true;
				cashMvt[i] -= fees[i];
				MvtTab[i] = 1;
				epargne -= fees[i];
				savings[i] = epargne;
				idFees++;
			}
			if(i>0 && savings[i]== 0){
				savings[i] = savings[i-1];
			}
		}
	}
	
	public void computeCallCenterCosts(ArrayList<Integer> feesDates){
		
		int idFees = 0;
		if(Math.random()<=param.getCallCenterCallAtOpenPct()) callCenterCosts[startIndex] = param.getAverageCallTime()*param.getCallCenterHourCost();
		while(feesDates.get(idFees)< startIndex) idFees++;
		for(int i =startIndex+1; i<callCenterCosts.length; i++){
			if(i==feesDates.get(idFees)){
				if(Math.random()<=param.getCallCenterCallMonthlyProba()) callCenterCosts[i] = param.getAverageCallTime()*param.getCallCenterHourCost();
				idFees++;
			}
		}
		
	}
	private void initWithdrawsDates(){
		// on ajoute une année pour les effets de bord
		// Il y a donc des dates qui débordent
		int nban = (int)((cashMvt.length-startIndex)/365)+1;
		withdrawDates = new int[nban*withdrawPeriod];
		for(int i=0; i< nban; i++){
			for(int j=0; j<withdrawPeriod; j++){
				withdrawDates[i * withdrawPeriod + j] = startIndex + 365 * i + (int)(365/withdrawPeriod*j) + (int)(Math.random() *365/withdrawPeriod); 
			}
		}
		for(int i =0; i< withdrawDates.length; i++){
//			System.out.print(withdrawDates[i]+ " ");
		}
//		System.out.println("s :" +withdrawDates.length);
	}

	/**
	 * La fonction calcul la date de cloture s'il y en a une
	 * chaque année on tire la proba d'avoir une cloture
	 * on tire ensuite une deuxième fois pour avoir la date dans l'année
	 * @return index de fermeture
	 */
	private int calcCloseDate(){
		
		boolean bOpen = true;
		int res = 0;
		
		int i = 0;

		while ((startIndex + i*365 <cashMvt.length) && bOpen){
			if(Math.random() < param.getdAnnualClosingRate()){				
				res = startIndex + 1+(int)(Math.round(Math.random()*364) +365*i);
				bOpen = false;
			}
			i++;
		}
		if(res>=cashMvt.length) res = 0;
		return res;
	}

	public boolean[] getuseMonthlyFee(){
		return useMonthlyFee;
	}
	
	public boolean isStudent(){
		return student;
	}
	
	public boolean isclosedBeforeSaving(){
		return closedBeforeSaving;
	}
	
	public int getCloseDate(){
		return closeDate;
	}
	
	public double[] getcashMvt(){
		return cashMvt;
	}
	
	public double[] getsavings(){
		return savings;
	}
	
	public double[] getCallCenterCosts(){
		return callCenterCosts;
	}
	
	public int[] getMvtTab(){
		return MvtTab;
	}

	public double[] getfees(){
		return fees;
	}

	
	public String toString(){
		
		String s = "Start;" + startIndex + ";Close;" + closeDate + ";initAmout;"+ initialAmount
				   +";periodic;"+periodicAmount+";retraitper;"+withdrawPeriod+";catCB;"+catCB;
		for(int i=0; i< cashMvt.length; i++)
			s = s + ";" + cashMvt[i];
		return s;
	}
}
