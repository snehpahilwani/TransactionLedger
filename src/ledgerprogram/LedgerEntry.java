package ledgerprogram;

import java.util.ArrayList;

public class LedgerEntry {
	String id;
	int M,N;
	ArrayList<Transaction> intrans,outtrans;
	
	public LedgerEntry(String id, int m, ArrayList<Transaction> intrans, int n, ArrayList<Transaction> outtrans) {
		
		this.id = id;
		M = m;
		N = n;
		this.intrans = intrans;
		this.outtrans = outtrans;
	}
}
