package dataModel;

import java.awt.EventQueue;
import java.util.*;


public class Main {
	public static void main(String[] args){
		EventQueue.invokeLater(new Runnable(){
			public void run(){
//				Client cl = new Client(1, new GregorianCalendar(2016, Calendar.JUNE, 14).getTime());
				Simulator S = new Simulator();
				System.out.println("OKKK");
			}
		});
	}
}
