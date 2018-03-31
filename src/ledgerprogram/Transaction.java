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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	
}
