package dataModel;

public class Bucket {
	
	private double dThreshold;
	private int iValue;
	
	public Bucket(double pThreshold, int pValue){
		dThreshold = pThreshold;
		iValue = pValue;
	}

	public double getdThreshold() {
		return dThreshold;
	}

	public void setdThreshold(double dThreshold) {
		this.dThreshold = dThreshold;
	}

	public int getiValue() {
		return iValue;
	}

	public void setiValue(int iValue) {
		this.iValue = iValue;
	}

}
