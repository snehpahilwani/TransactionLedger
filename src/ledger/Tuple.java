package ledger;

import java.util.ArrayList;

public class Tuple {
	boolean isValid;
	ArrayList<Transaction> transArray;
	
	public Tuple(boolean isValid, ArrayList<Transaction> transArray) {
		
		this.isValid = isValid;
		this.transArray = transArray;
	}
	
	
}
