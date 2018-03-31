package ledgerprogram;

public class Tuple {
	String txn;
	String signature;
	
	public Tuple(String txn, String signature){
		this.txn = txn;
		this.signature = signature;
	}

	public String getTxn() {
		return txn;
	}

	public void setTxn(String txn) {
		this.txn = txn;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	
}
