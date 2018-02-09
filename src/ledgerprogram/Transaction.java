package ledgerprogram;

public class Transaction {
	String name;
	int amount;
	
	public Transaction(String name, int amount){
		this.name = name;
		this.amount = amount;
	}
	
	public String stringifyTransaction(){
		String transactionString = this.name + this.amount;
		return transactionString;
	}
}
