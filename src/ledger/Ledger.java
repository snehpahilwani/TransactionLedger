package ledger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ledger {
	static ArrayList<LedgerEntry> ledger = new ArrayList<>(); // ArrayList to
																// hold all
	// the ledger entries
	// for printing and
	// dumping

	static HashMap<String, Integer> balanceMap = new HashMap<>(); // HashMap to
																	// hold
	// users and their
	// balances
	static HashMap<String, ArrayList<Transaction>> refMap = new HashMap<>(); // HashMap
	// to
	// hold
	// the
	// referenced
	// transactions
	// and
	// vouts

	// Checking well-formedness of non-first transactions
	public static boolean wellFormed(String transaction) {
		if (transaction.matches("\\S{8};\\s*\\d+;\\s*(\\(\\S{8},\\s*\\d+\\))+;\\s*\\d+;\\s*(\\(\\S+,\\s*\\d+\\))+"))
			return true;
		return false;
	}

	// Checking well-formedness of first transaction
	public static boolean firstLineWellFormed(String transaction) {
		if (transaction.matches("\\S{8};\\s*\\d+;\\s*;\\s*\\d+;\\s*(\\(\\S+,\\s*\\d+\\))+"))
			return true;
		return false;
	}

	// Printing of interactive menu
	public static void printInteractiveMenu() {
		System.out.println(
				"[F]ile\n[T]ransaction\n[P]rint\n[H]elp\n[D]ump\n[W]ipe\n[I]nteractive\n[V]erbose\n[B]alance\n[E]xit\nSelect a command:	");
	}

	// To check if count satisfies number of transaction entries
	public static ArrayList<Transaction> transCountValid(String transString, int k, String regex) {
		int count = 0;
		Matcher m = Pattern.compile(regex).matcher(transString);
		ArrayList<Transaction> transArray = new ArrayList<>();
		while (m.find()) {
			String trans = m.group(0);
			trans = trans.replace("(", "");
			trans = trans.replace(")", "");
			String name = trans.split(",")[0].trim();
			int amount = Integer.valueOf(trans.split(",")[1].trim());
			Transaction trans1 = new Transaction(name, amount);
			transArray.add(trans1);
			count++;
		}
		if (count != k)
			return null;
		return transArray;

	}

	// Program start main method
	public static void main(String[] args) {
		@SuppressWarnings("resource")

		Scanner scan = new Scanner(System.in);
		int count = 0;
		while (true) {
			printInteractiveMenu();
			char input = scan.nextLine().charAt(0);
			switch (input) {
			case 'E':
				System.out.println("Exiting program. Goodbye.");
				System.exit(0);
				break;
			case 'T':
				System.out.println("Enter transaction: ");
				// Scanner scan1 = new Scanner(System.in);
				String inputTransaction = scan.nextLine();
				if (count == 0) { // First transaction
					if (!firstLineWellFormed(inputTransaction)) {
						System.out.println("ERROR: Wrong transaction format.");
					} else {
						String[] transComponents = inputTransaction.split(";");
						// for(int i = 0; i<transComponents.length; i++){
						// System.out.println(transComponents[i].trim());
						// }
						String regex = "\\s*\\(\\S+,\\s*\\d+\\)";
						int n = Integer.valueOf(transComponents[3].trim());
						if (Integer.valueOf(transComponents[1].trim()) == 0 && transComponents[2].trim().equals("")) {
							ArrayList<Transaction> outtrans = transCountValid(transComponents[4].trim(), n, regex);
							if (outtrans != null) {
								LedgerEntry entry = new LedgerEntry(transComponents[0].trim(), 0,
										new ArrayList<Transaction>(), n, outtrans);
								ledger.add(entry);

								for (Transaction t : outtrans) {
									balanceMap.put(t.name, t.amount);
								}
								System.out.println("SUCCESS: Transaction added in ledger.");
								count++;
							} else {
								System.out.println("ERROR: Not enough transactions for first transaction.");
							}
						} else {
							System.out.println("ERROR: No input transactions required for the first transaction!");
						}
					}
				} else {
					if (!wellFormed(inputTransaction)) {
						System.out.println("ERROR: Wrong transaction format.");
					} else {
						String[] transComponents = inputTransaction.split(";");
						String regex = "\\s*\\(\\S+,\\s*\\d+\\)";
						int m = Integer.valueOf(transComponents[1].trim());
						int n = Integer.valueOf(transComponents[3].trim());
						ArrayList<Transaction> intrans = transCountValid(transComponents[2].trim(), m, regex);
						ArrayList<Transaction> outtrans = transCountValid(transComponents[4].trim(), n, regex);

						if (intrans != null && outtrans != null) {
							LedgerEntry entry = new LedgerEntry(transComponents[0].trim(), m, intrans, n, outtrans);
							ledger.add(entry);
							System.out.println("SUCCESS: Transaction added in ledger.");
							count++;
						} else {
							System.out.println("ERROR: Not enough transactions for first transaction.");
						}
					}
				}
				// scan1.close();
				break;
			case 'P':
				if (!ledger.isEmpty()) {
					for (LedgerEntry entry : ledger) {
						System.out.println();
						System.out.print(entry.id + "; " + entry.M + "; ");
						for (Transaction t : entry.intrans) {
							System.out.print("(" + t.name + ", " + t.amount + ")");
						}
						System.out.print("; " + entry.N + "; ");
						for (Transaction t : entry.outtrans) {
							System.out.print("(" + t.name + ", " + t.amount + ")");
						}
					}
				} else {
					System.out.println("Ledger is empty.");
				}
				break;
			case 'B':
				System.out.println("Enter User: ");
				// Scanner scan1 = new Scanner(System.in);
				String user = scan.nextLine();
				if (balanceMap.get(user) != null) {
					System.out.println(user + " has " + balanceMap.get(user));
				} else {
					System.out.println("User does not exist!");
				}
				break;
			case 'W':
				ledger.clear();
				balanceMap.clear();
				refMap.clear();
				count = 0;
				break;
			default:
				System.out.println("Not a valid option. Select a valid option from above menu.");
				break;
			}

		}

	}

}
