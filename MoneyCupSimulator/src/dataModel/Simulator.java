package dataModel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


public class Simulator {
	
	public static final int NB_YEARS = 5;
	
	private static String sfilePath = "C:\\Users\\pdegouville\\Documents\\MoneyCup\\Simulations\\dailyOpenAccounts.csv";
	private static String sfileOutputPath = "C:\\Users\\pdegouville\\Documents\\MoneyCup\\Simulations\\Simul.csv";
	

	private static final String sep = ";";
	  
	private int[] dailyOpenAccounts;
	private double[] dailyCashMvt;
	private double[] dailyFees;
	private double[] dailySavings;
	private double[] dailyCallCenterCosts;
	private int[] dailyMvt;
	private int[] dailyClose;
	private int[] dailyStudent;
	private int[] dailycloseBeforeSaving;
	private int[] largeaccounts;
	
	
	// les fees et sont calculées et retirées le dernier jour du mois
	private ArrayList<Integer> feesDates = new ArrayList<Integer>();
	private Date startDate;
	private Date endDate;
	private int nbjours;
	
	public Simulator(){
		Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);
	    cal.set(Calendar.DAY_OF_MONTH, 1);
	    cal.set(Calendar.MONTH, 0);
	    cal.set(Calendar.YEAR, 2017);
	    startDate = cal.getTime();
	    cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)+NB_YEARS);
	    endDate = cal.getTime();
	    long diff = endDate.getTime() - startDate.getTime();
	    // le -2 vient pour avoir 364 jours sur 1 an (du premier au 31
	    nbjours = (int)TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)-1;
	    
	    MyLogger.myLogger.log(Level.INFO,"nbjours : " + nbjours);

	    dailyOpenAccounts = new int[nbjours];
	    dailyCashMvt = new double[nbjours];
	    dailyFees = new double[nbjours];
	    dailySavings = new double[nbjours];
	    dailyMvt = new int[nbjours];
	    dailyClose = new int[nbjours];
	    dailyStudent= new int[nbjours];
	    dailycloseBeforeSaving = new int[nbjours];
	    dailyCallCenterCosts = new double[nbjours];
	    
	    
	    initdailyOpenAccounts(sfilePath);
	    initfeesDates();
	    SimulatorParam param = new SimulatorParam();
	    largeaccounts = new int[feesDates.size()];
	    
	    for(int i=0; i<dailyOpenAccounts.length; i++){
	    	for(int j=0; j<dailyOpenAccounts[i]; j++){
		    	Client c = new Client(1, nbjours, i, param);
		    	c.computeCashMvt(feesDates);
		    	c.computeCallCenterCosts(feesDates);
		    	addTabs(dailyCashMvt, c.getcashMvt());
		    	addTabs(dailyFees, c.getfees());
		    	addTabs(dailySavings, c.getsavings());
		    	addTabs(dailyMvt, c.getMvtTab());
		    	addTabs(largeaccounts, c.getuseMonthlyFee());
		    	addTabs(dailyCallCenterCosts, c.getCallCenterCosts());
		    	if(c.getCloseDate()!=0) dailyClose[c.getCloseDate()] =dailyClose[c.getCloseDate()] +1;
		    	if(c.isStudent()) dailyStudent[i] = dailyStudent[i]+1;
		    	if(c.isclosedBeforeSaving()) dailycloseBeforeSaving[i] = dailycloseBeforeSaving[i] + 1;
//		    	System.out.println(c.toString());	    		
	    	}
	    }
	    writeToFile(sfileOutputPath);
	    System.out.println("dailyOpenClient");
	    System.out.println(Arrays.toString(dailyOpenAccounts));
	    System.out.println("dailyClose");
	    System.out.println(Arrays.toString(dailyClose));
	    System.out.println("dailyCashMvt");
	    System.out.println(Arrays.toString(dailyCashMvt));
	    System.out.println("dailySavings");
	    System.out.println(Arrays.toString(dailySavings));
	    System.out.println("dailyMvt");
	    System.out.println(Arrays.toString(dailyMvt));
	    System.out.println("dailyFees");
	    System.out.println(Arrays.toString(dailyFees));
	    System.out.println("dailyStudent");
	    System.out.println(Arrays.toString(dailyStudent));
	    System.out.println("dailycloseBeforeSaving");
	    System.out.println(Arrays.toString(dailycloseBeforeSaving));
	    System.out.println("large accounts");
	    System.out.println(Arrays.toString(largeaccounts));
	    System.out.println("CallCenter costs");
	    System.out.println(Arrays.toString(dailyCallCenterCosts));
	     
	}
	
	private double[] addTabs(double[] t, double[] s){
		for(int i = 0; i< s.length; i++){
			t[i] += s[i];
		}
		return t;
	}
		
	private int[] addTabs(int[] t, int[] s){
		for(int i = 0; i< s.length; i++){
			t[i] += s[i];
		}
		return t;		
	}
		
	private int[] addTabs(int[] t, boolean[] s){
		for(int i = 0; i< s.length; i++){
			t[i] += s[i]? 1 : 0;;
		}
		return t;		
	}

	private void initfeesDates(){
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		while (!(cal.getTime().after(endDate))){
			cal.add(Calendar.MONTH, 1);
			long diff = cal.getTime().getTime() - startDate.getTime();
			feesDates.add((int)TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)-2);
//			System.out.println((int)TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)-2);
		}
	}
	
	private void initdailyOpenAccounts(String filpePath){
		
		BufferedReader rd = null;
		try{
			rd = new BufferedReader(new FileReader(filpePath));
			String line = rd.readLine();
			int i = 0;
			if(line != null){			
				while(true){
					line = rd.readLine();
					if(line == null) break;
					String[] listItems = line.split(sep, -1);
					// The first element should be the asset code, the second one the same ShortNameas the one in the database
					// assets.add(new AssetData(Integer.parseInt(listItems[0]), listItems[1], listItems[2]));
					// creer la liste des utilisateurs
					if(i<dailyOpenAccounts.length){
						dailyOpenAccounts[i]= Integer.parseInt(listItems[1]);
					}
					i++;
				}
				for(int j = i; j<dailyOpenAccounts.length; j++){	
					dailyOpenAccounts[j] = dailyOpenAccounts[i-1];
				}
//				for(int j= 0; j < dailyOpenAccounts.length; j++){
//					System.out.println(j + ";" + dailyOpenAccounts[j]);
//				}
			}
		}catch (IOException ex){
			MyLogger.myLogger.log(Level.SEVERE,"Can't open the file. check the path and source name");
			ex.printStackTrace();
		}					
	}
	
	private String myToStringTat(String name, double[] tab){
		String s = name;
		for(int i = 0; i<tab.length; i++){
			s = s + sep + tab[i];
		}
		return s;
	}
	
	private String myToStringTat(String name, int[] tab){
		String s = name;
		for(int i = 0; i<tab.length; i++){
			s = s + sep + tab[i];
		}
		return s;
	}

	private void writeToFile(String ouputFilePath){
		
		String line;
		try{
			
			PrintWriter wr = new PrintWriter(new FileWriter(ouputFilePath));
			DateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy");
			
			line = "Date";
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			
			for(int i=0; i<nbjours; i++){
				 line = line + sep +  targetFormat.format(cal.getTime());
				 cal.add(Calendar.DATE, 1);
			}
			wr.println(line);
			wr.println(myToStringTat("openAccounts", dailyOpenAccounts));
			wr.println(myToStringTat("Students", dailyStudent));
			wr.println(myToStringTat("close", dailyClose));
			wr.println(myToStringTat("close before sav", dailycloseBeforeSaving));
			wr.println(myToStringTat("cash mvt", dailyCashMvt));
			wr.println(myToStringTat("nb mvt", dailyMvt));
			wr.println(myToStringTat("fees", dailyFees));
			wr.println(myToStringTat("large accounts", largeaccounts));
			wr.println(myToStringTat("callcenter costs", dailyCallCenterCosts));
			
			wr.close();
		}catch (IOException ex){
			MyLogger.myLogger.log(Level.SEVERE,"Problem with writing in the file. May be open");
			ex.printStackTrace();
		}
	}

	
}