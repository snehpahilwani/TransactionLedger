package ledger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ledger {
	static LinkedHashMap<String, LedgerEntry> ledger = new LinkedHashMap<>(); // LinkedHashMap
																				// to
	// hold all
	// ledger
	// entries for
	// printing and
	// dumping

	static HashMap<String, Integer> balanceMap = new HashMap<>(); // HashMap to
																	// hold
	// users and their
	// balances
	static HashMap<String, Boolean> refMap = new HashMap<>(); // HashMap
																// to
	// hold
	// the
	// referenced
	// transactions
	// and
	// vouts

	public static void printHelpMenu() {
		System.out.println(
				"Welcome to Simplified Bitcoin Ledger.\nBelow mentioned are command summaries that you could use with this interactive ledger.");
		System.out.println("[F]ile - Supply filename to intake transactions into the ledger from a file.");
		System.out.println("[T]ransaction - Supply transaction on command line to enter into the ledger.");
		System.out.println("[P]rint - Prints the ledger on the command line.");
		System.out.println("[H]elp - You landed here by using this command for Help! :)");
		System.out.println("[D]ump - Supply filename to dump the ledger transactions into.");
		System.out.println("[W]ipe - Clears/wipes the ledger empty.");
		System.out.println("[I]nteractive - Toggles interactive mode for menu visibility.");
		System.out.println("[V]erbose - Helps with more information regarding transactions.");
		System.out.println("[B]alance - Supply username to check balance for that user.");
		System.out.println("[E]xit - Exit the program.");
		System.out.println();
	}

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
				"[F]ile\n[T]ransaction\n[P]rint\n[H]elp\n[D]ump\n[W]ipe\n[I]nteractive\n[V]erbose\n[B]alance\n[E]xit\nSelect a command:");
	}

	// To make input valid for SHA hash generation
	public static String makeValid(String inputTransaction) {
		inputTransaction = inputTransaction.replace(",", ", ");
		inputTransaction = inputTransaction.replace(";", "; ");
		inputTransaction = inputTransaction.replace(",  ", ", ");
		inputTransaction = inputTransaction.replace(";  ", "; ");
		return inputTransaction;
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

	// Generates SHA-1 for input transaction
	static String genSHA1(String input) throws NoSuchAlgorithmException {
		input = makeValid(input);
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		byte[] result = mDigest.digest(input.getBytes());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < result.length; i++) {
			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
		}

		return sb.toString().substring(0, 8).toLowerCase();
	}

	// Validates and enter genesis transaction in the ledger
	public static boolean enterFirstTransaction(String inputTransaction) {
		if (!firstLineWellFormed(inputTransaction)) {
			System.out.println("ERROR: Wrong transaction format.");
			return false;
		} else {
			String[] transComponents = inputTransaction.split(";");

			String regex = "\\s*\\(\\w+,\\s*\\d+\\)";
			int n = Integer.valueOf(transComponents[3].trim());
			if (Integer.valueOf(transComponents[1].trim()) == 0 && transComponents[2].trim().equals("")) {
				ArrayList<Transaction> outtrans = transCountValid(transComponents[4].trim(), n, regex);

				String txnID = transComponents[0].trim();
				String gentxnID = "";
				String[] trans = inputTransaction.split(";", 2);
				try {
					gentxnID = genSHA1(trans[1].trim() + "\n");
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (outtrans != null) {
					// System.out.println("Reached here with " + gentxnID);
					if (!txnID.equals(gentxnID)) {
						System.out.println(
								"WARN: Generating correct transaction ID " + gentxnID + " instead of " + txnID);
						txnID = gentxnID;
					}
					LedgerEntry entry = new LedgerEntry(txnID, 0, new ArrayList<Transaction>(), n, outtrans);
					ledger.put(txnID, entry);

					for (Transaction t : outtrans) {
						if (balanceMap.get(t.name) != null) {
							balanceMap.put(t.name, balanceMap.get(t.name) + t.amount);
						} else {
							balanceMap.put(t.name, t.amount);
						}

					}
					for (int i = 0; i < n; i++) {
						refMap.put(txnID + i, false);
					}
					System.out.println("SUCCESS: Transaction added in ledger.");
					// count++;
				} else {
					System.out.println("ERROR: Not enough transactions for first transaction.");
					return false;
				}
			} else {
				System.out.println("ERROR: No input transactions required for the first transaction!");
				return false;
			}
		}
		return true;
	}

	public static boolean enterTransaction(String inputTransaction) {
		if (!wellFormed(inputTransaction)) {
			System.out.println("ERROR: Wrong transaction format.");
			return false;
		} else {
			String[] transComponents = inputTransaction.split(";");
			String regex = "\\s*\\(\\w+,\\s*\\d+\\)";
			int m = Integer.valueOf(transComponents[1].trim());
			int n = Integer.valueOf(transComponents[3].trim());
			ArrayList<Transaction> intrans = transCountValid(transComponents[2].trim(), m, regex);
			ArrayList<Transaction> outtrans = transCountValid(transComponents[4].trim(), n, regex);
			String txnID = transComponents[0].trim();
			String gentxnID = "";
			String[] trans = inputTransaction.split(";", 2);
			try {
				gentxnID = genSHA1(trans[1].trim() + "\n");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (intrans != null && outtrans != null) {

				if (isTransEqual(intrans, outtrans)) {

					for (Transaction t : intrans) {
						String intransString = t.stringifyTransaction();
						if (refMap.get(intransString) == true) {
							System.out.println("ERROR: Input transaction (" + t.name + ", " + t.amount
									+ ") Transaction units spent.");
							return false;
						} else {
							LedgerEntry entry = ledger.get(t.name);
							Transaction entryTrans = entry.outtrans.get(t.amount);
							int subAmount = entryTrans.amount;
							balanceMap.put(entryTrans.name, balanceMap.get(entryTrans.name) - subAmount);
							refMap.put(intransString, true);
						}

					}

					for (Transaction t : outtrans) {
						if (balanceMap.get(t.name) != null) {
							balanceMap.put(t.name, balanceMap.get(t.name) + t.amount);
						} else {
							balanceMap.put(t.name, t.amount);
						}

					}
					if (!txnID.equals(gentxnID)) {
						System.out.println(
								"WARN: Generating correct transaction ID " + gentxnID + " instead of " + txnID);
						txnID = gentxnID;
					}
					LedgerEntry entry = new LedgerEntry(txnID, m, intrans, n, outtrans);
					ledger.put(txnID, entry);
					for (int i = 0; i < n; i++) {
						refMap.put(txnID + i, false);
					}
					// refMap.put(txnID, outtrans);

					System.out.println("SUCCESS: Transaction added in ledger.");
					// count++;
				} else {
					System.out.println(
							"ERROR: Output value and input value sums are not equal for transaction ID: " + txnID);
					return false;
				}
			} else {
				System.out.println("ERROR: Please provide correct number of transactions.");
				return false;
			}
		}
		return true;
	}

	// Checks for equal sum of intrans and outtrans
	public static boolean isTransEqual(ArrayList<Transaction> intrans, ArrayList<Transaction> outtrans) {
		int insum = 0, outsum = 0;
		for (Transaction t1 : intrans) {
			if (ledger.get(t1.name) != null && t1.amount < ledger.get(t1.name).outtrans.size()) {
				if (ledger.get(t1.name) != null && ledger.get(t1.name).outtrans.get(t1.amount) != null) {
					LedgerEntry entry = ledger.get(t1.name);
					ArrayList<Transaction> transList = entry.outtrans;
					Transaction entryTrans = transList.get(t1.amount);
					insum += entryTrans.amount;
				} else {
					System.out.println("ERROR: Wrong transaction (" + t1.name + ", " + t1.amount + ") referred.");
					return false;
				}
			} else {
				System.out.println("ERROR: Wrong transaction (" + t1.name + ", " + t1.amount + ") referred.");
				return false;
			}
		}
		for (Transaction t : outtrans) {
			outsum += t.amount;
		}

		if (insum != outsum) {
			return false;
		}
		return true;
	}

	// Program start main method
	public static void main(String[] args) {
		@SuppressWarnings("resource")

		Scanner scan = new Scanner(System.in);
		int count = 0, countInFile = 0;
		boolean toggleInteractive = false; // true is interactive, false is not
		while (true) {
			if (toggleInteractive) {
				printInteractiveMenu();
			}
			String input = scan.nextLine().toUpperCase();

			switch (input) {
			case "H":
			case "HELP":
				printHelpMenu();
				break;
			case "I":
			case "INTERACTIVE":
				toggleInteractive = !toggleInteractive;
				break;
			case "V":
			case "VERBOSE":
				break;
			case "E":
			case "EXIT":
				System.out.println("Exiting program. Goodbye.");
				scan.nextLine();
				System.exit(0);
				break;
			case "T":
			case "TRANSACTION":
				if (toggleInteractive) {
					System.out.println("Enter transaction: ");
				}
				// Scanner scan1 = new Scanner(System.in);
				String inputTransaction = scan.nextLine();
				if (count == 0) { // First transaction
					if (enterFirstTransaction(inputTransaction))
						count++;
				} else {
					if (enterTransaction(inputTransaction))
						count++;
				}
				// scan1.close();
				break;
			case "P":
			case "PRINT":
				if (!ledger.isEmpty()) {
					for (String txnID : ledger.keySet()) {
						LedgerEntry entry = ledger.get(txnID);
						System.out.print(entry.id + "; " + entry.M + "; ");
						for (Transaction t : entry.intrans) {
							System.out.print("(" + t.name + ", " + t.amount + ")");
						}
						System.out.print("; " + entry.N + "; ");
						for (Transaction t : entry.outtrans) {
							System.out.print("(" + t.name + ", " + t.amount + ")");
						}
						System.out.println();
					}
				} else {
					System.out.println("Ledger is empty.");
				}
				break;
			case "F":
			case "FILE":
				if (toggleInteractive) {
					System.out.println("Supply filename: ");
				}

				File filename = new File(scan.nextLine());

				try {
					Scanner scan2 = new Scanner(filename);
					// int countInFile = 0;
					while (scan2.hasNextLine()) {
						String line = scan2.nextLine();
						if (countInFile == 0) { // First transaction from file
							if (enterFirstTransaction(line))
								countInFile++;
						} else {
							if (enterTransaction(line))
								countInFile++;
						}
						// System.out.println(line);
					}
					scan2.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					System.out.println("ERROR: File not found.");
					e.printStackTrace();
				}

				break;
			case "B":
			case "BALANCE":
				if (toggleInteractive) {
					System.out.println("Enter User: ");
				}

				// Scanner scan1 = new Scanner(System.in);
				String user = scan.nextLine();
				if (balanceMap.get(user) != null) {
					System.out.println(user + " has " + balanceMap.get(user));
				} else {
					System.out.println("ERROR: User does not exist!");
				}
				break;
			case "W":
			case "WIPE":
				ledger.clear();
				balanceMap.clear();
				refMap.clear();

				count = 0;
				countInFile = 0;

				System.out.println("SUCCESS: Ledger wiped!");
				break;
			case "D":
			case "DUMP":
				if (toggleInteractive) {
					System.out.println("Supply filename: ");
				}
				File filename1 = new File(scan.nextLine());
				try (FileWriter fw = new FileWriter(filename1, false);
						BufferedWriter bw = new BufferedWriter(fw);
						PrintWriter out = new PrintWriter(bw)) {

					if (!ledger.isEmpty()) {
						for (String txnID : ledger.keySet()) {
							LedgerEntry entry = ledger.get(txnID);
							out.print(entry.id + "; " + entry.M + "; ");
							for (Transaction t : entry.intrans) {
								out.print("(" + t.name + ", " + t.amount + ")");
							}
							out.print("; " + entry.N + "; ");
							for (Transaction t : entry.outtrans) {
								out.print("(" + t.name + ", " + t.amount + ")");
							}
							out.println();
						}
					} else {
						System.out.println("Ledger is empty.");
					}
				} catch (IOException e) {
					System.out.println("ERROR: File not found.");
				}

				break;
			default:
				System.out.println("ERROR: Not a valid option. Select a valid option from above menu.");
				break;
			}

		}

	}

}
