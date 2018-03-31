package ledgerprogram;

import java.util.ArrayList;

public class LedgerEntry {
	String id;
	int M,N;
	ArrayList<Transaction> intrans,outtrans;
	String signature = null;
	
	public LedgerEntry(String id, int m, ArrayList<Transaction> intrans, int n, ArrayList<Transaction> outtrans) {
		
		this.id = id;
		M = m;
		N = n;
		this.intrans = intrans;
		this.outtrans = outtrans;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getM() {
		return M;
	}

	public void setM(int m) {
		M = m;
	}

	public int getN() {
		return N;
	}

	public void setN(int n) {
		N = n;
	}

	public String getSignature() {
		return signature;
	}

	public ArrayList<Transaction> getIntrans() {
		return intrans;
	}

	public void setIntrans(ArrayList<Transaction> intrans) {
		this.intrans = intrans;
	}

	public ArrayList<Transaction> getOuttrans() {
		return outtrans;
	}

	public void setOuttrans(ArrayList<Transaction> outtrans) {
		this.outtrans = outtrans;
	}
	
	public void setSignature(String signature){
		this.signature = signature;
	}
	
	public String stringifyEntry(){
		String entryString = this.id + "; " + this.M + "; " ;
		for (Transaction t : this.intrans) {
			entryString += "(" + t.name + ", " + t.amount + ")";
		}
		entryString += "; " + this.N + "; ";
		for (Transaction t : this.outtrans) {
			entryString += "(" + t.name + ", " + t.amount + ")";
		}
		
		return entryString;
	}
	
	
}
