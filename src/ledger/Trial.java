package ledger;

import java.util.Scanner;

public class Trial {

	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		//String transaction = scan.nextLine();
		//System.out.println(transaction);
		String transaction = "f2cea539; 0; ; 1; (Alice, 1000)";
		// Ledger l = new Ledger();
		System.out.println(Ledger.firstLineWellFormed(transaction));
	}
}
