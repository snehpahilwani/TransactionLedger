package ledgerprogram;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ledger {
	// LinkedHashMap to hold all ledger entries for printing and dumping to file
	static LinkedHashMap<String, LedgerEntry> ledgerMap = new LinkedHashMap<>();

	// HashMap to hold users and their balances(just like wallets)
	static HashMap<String, Integer> balanceMap = new HashMap<>();

	// HashMap to hold the referenced transactions and if they have been
	// referred before
	static HashMap<String, Boolean> refMap = new HashMap<>();

	// HashMap to keep a link between transaction ID(key) and name(val) 
	static HashMap<String, String> idMap = new HashMap<>();

	// HashMap to store public keys for the users
	static HashMap<String, PublicKey> pubKeyMap = new HashMap<>();

	// Toggle verbose variable
	static boolean verbose = false;

	public void printHelpMenu() {
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
		System.out
				.println("[R]ead Key File - Supply account name and public key file name for signature verification.");
		System.out.println(
				"[C]heck Transaction Signature - Supply transaction ID and verify if the signature is correct.");
		System.out.println(
				"[O]utput Transaction Block - Prints out all correctly signed transactions and places them in a block.");
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
	public void printInteractiveMenu() {
		System.out.println(
				"[F]ile\n[T]ransaction\n[P]rint\n[H]elp\n[D]ump\n[W]ipe\n[I]nteractive\n[R]ead Key File\n[V]erbose\n[B]alance\n[C]heck Transaction Signature\n[O]utput Transaction Block\n[E]xit\nSelect a command:");
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
	public boolean enterFirstTransaction(String inputTransaction, String signature) {
		if (!firstLineWellFormed(inputTransaction)) {
			System.out.println(inputTransaction.split(";")[0] + ": Bad");
			System.err.println("ERROR: Wrong transaction format.");

			if (verbose) {
				System.out.println("ERROR: Wrong transaction format. <TransID>; 0; ; N; (<AcctID>, <amount>)^N ");
			}

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
					e.printStackTrace();
				}

				if (outtrans != null) {
					// System.out.println("Reached here with " + gentxnID);
					if (!txnID.equals(gentxnID)) {
						System.out.println(txnID + ": Bad");
						if (verbose) {
							System.out.println(
									"WARN: Generating correct transaction ID " + gentxnID + " instead of " + txnID);
						}
						System.err.println(
								"WARN: Generating correct transaction ID " + gentxnID + " instead of " + txnID);
						txnID = gentxnID;
					}
					LedgerEntry entry = new LedgerEntry(txnID, 0, new ArrayList<Transaction>(), n, outtrans);
					entry.setSignature(signature);
					ledgerMap.put(txnID, entry);

					for (int i = 0; i < outtrans.size(); i++) {
						Transaction t = outtrans.get(i);
						idMap.put(txnID + i, t.name);

						if (balanceMap.get(t.name.toLowerCase()) != null) {
							balanceMap.put(t.name.toLowerCase(), balanceMap.get(t.name.toLowerCase()) + t.amount);
						} else {
							balanceMap.put(t.name.toLowerCase(), t.amount);
						}

					}

					for (int i = 0; i < n; i++) {
						refMap.put(txnID + i, false);
					}
					System.out.println(txnID + ": Good");
					if (verbose) {
						System.out.println("SUCCESS: Transaction added in ledger.");
					}
					// count++;
				} else {
					System.out.println(txnID + ": Bad");
					if (verbose) {
						System.out.println("ERROR: Not enough transactions for first transaction.");
					}
					System.err
							.println("ERROR: Not enough transactions provided. Please provide according to N values.");
					return false;
				}
			} else {
				System.out.println(inputTransaction.split(";")[0] + ": Bad");
				if (verbose) {
					System.out.println("ERROR: No input transactions required for the genesis transaction!");
				}
				System.err.println("ERROR: No input transactions required for the genesis transaction!");
				return false;
			}
		}
		return true;
	}

	// Validates and enters other transactions in ledger.
	public boolean enterTransaction(String inputTransaction, String signature) {
		if (!wellFormed(inputTransaction)) {
			System.out.println(inputTransaction.split(";")[0] + ": Bad");
			if (verbose) {
				System.out.println(
						"ERROR: Wrong transaction format. Format: <TransID>; M; (<TransID>, <vout>)^M; N; (<AcctID>, <amount>)^N ");
			}
			System.err.println("ERROR: Wrong transaction format.");
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
				e.printStackTrace();
			}
			if (intrans != null && outtrans != null) {
				String txnUser = intrans.get(0).stringifyTransaction();
				String txnUserName = "";
				if (idMap.get(txnUser) != null) {
					txnUserName = idMap.get(txnUser);
				}

				if (isTransEqual(intrans, outtrans)) {

					for (Transaction t : intrans) {
						String intransString = t.stringifyTransaction();
						// String inTxnID = t.name;

						if (idMap.get(intransString) == null || !idMap.get(intransString).equals(txnUserName)) {
							System.out.println(txnID + ": Bad");
							if (verbose) {
								System.out.println("Input UTXO needs to be from the same user.");
							}
							return false;

						}

						if (refMap.get(intransString) == true) {
							System.out.println(txnID + ": Bad");
							if (verbose) {
								System.out.println("ERROR: Input transaction (" + t.name + ", " + t.amount
										+ ") Transaction units spent.");
							}
							System.err.println("ERROR: Input transaction (" + t.name + ", " + t.amount
									+ ") Transaction units spent.");
							return false;
						} else {
							LedgerEntry entry = ledgerMap.get(t.name);
							Transaction entryTrans = entry.outtrans.get(t.amount);
							int subAmount = entryTrans.amount;
							balanceMap.put(entryTrans.name.toLowerCase(),
									balanceMap.get(entryTrans.name.toLowerCase()) - subAmount);
							refMap.put(intransString.toLowerCase(), true);
						}

					}

					if (!txnID.equals(gentxnID)) {
						System.out.println(txnID + ": Bad");
						if (verbose) {
							System.out.println(
									"WARN: Generating correct transaction ID " + gentxnID + " instead of " + txnID);
						}
						System.err.println(
								"WARN: Generating correct transaction ID " + gentxnID + " instead of " + txnID);
						txnID = gentxnID;
					}

					for (int i = 0; i < outtrans.size(); i++) {
						Transaction t = outtrans.get(i);
						idMap.put(txnID + i, t.name);

						if (balanceMap.get(t.name.toLowerCase()) != null) {
							balanceMap.put(t.name.toLowerCase(), balanceMap.get(t.name.toLowerCase()) + t.amount);
						} else {
							balanceMap.put(t.name.toLowerCase(), t.amount);
						}

					}

					LedgerEntry entry = new LedgerEntry(txnID, m, intrans, n, outtrans);
					entry.setSignature(signature);
					ledgerMap.put(txnID, entry);
					for (int i = 0; i < n; i++) {
						refMap.put(txnID + i, false);
					}
					// refMap.put(txnID, outtrans);
					System.out.println(txnID + ": Good");
					if (verbose) {
						System.out.println("SUCCESS: Transaction added in ledger.");
					}
					// count++;
				} else {
					System.out.println(txnID + ": Bad");
					if (verbose) {
						System.out.println(
								"ERROR: Output value and input value sums are not equal for transaction ID: " + txnID);
					}
					System.err.println("ERROR: Output value and input value sums are not equal.");
					return false;
				}
			} else {
				System.out.println(txnID + ": Bad");
				if (verbose) {
					System.out.println(
							"ERROR: Please provide correct number of transactions according to M, N values specified in transaction.");
				}
				System.err.println("ERROR: Please provide correct number of transactions.");
				return false;
			}
		}
		return true;
	}

	// Checks for equal sum of intrans and outtrans
	public static boolean isTransEqual(ArrayList<Transaction> intrans, ArrayList<Transaction> outtrans) {
		int insum = 0, outsum = 0;
		for (Transaction t1 : intrans) {
			if (ledgerMap.get(t1.name) != null && t1.amount < ledgerMap.get(t1.name).outtrans.size()) {
				if (ledgerMap.get(t1.name) != null && ledgerMap.get(t1.name).outtrans.get(t1.amount) != null) {
					LedgerEntry entry = ledgerMap.get(t1.name);
					ArrayList<Transaction> transList = entry.outtrans;
					Transaction entryTrans = transList.get(t1.amount);
					insum += entryTrans.amount;
				} else {
					if (verbose) {
						System.out.println("ERROR: Wrong transaction (" + t1.name + ", " + t1.amount
								+ ") referred. This was not included in the ledger as an entry.");
					}
					return false;
				}
			} else {
				if (verbose) {
					System.out.println("ERROR: Wrong transaction (" + t1.name + ", " + t1.amount
							+ ") referred. This was not included in the ledger as an entry.");
				}
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
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeyException,
			InvalidKeySpecException, SignatureException {
		@SuppressWarnings("resource")

		Scanner scan = new Scanner(System.in);
		ledger l = new ledger();
		int count = 0;// countInFile = 0;
		boolean toggleInteractive = false; // true is interactive, false is not
		while (true) {
			if (toggleInteractive) {
				l.printInteractiveMenu();
			}
			String input = scan.nextLine().toUpperCase();

			switch (input) {
			case "H":
			case "HELP":
				l.printHelpMenu();
				break;
			case "I":
			case "INTERACTIVE":
				toggleInteractive = !toggleInteractive;
				break;
			case "V":
			case "VERBOSE":
				verbose = !verbose;
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
				if (toggleInteractive)
					System.out.println("Enter transaction: ");
				String inputTransaction = scan.nextLine();
				if (toggleInteractive)
					System.out.println("Enter signature: ");
				String signature = scan.nextLine();
				if (count == 0 && l.enterFirstTransaction(inputTransaction, signature.trim())) { // First
																									// transaction
					count++;
				} else {
					if (l.enterTransaction(inputTransaction, signature.trim()))
						count++;
				}

				// scan1.close();
				break;
			case "P":
			case "PRINT":
				if (!ledgerMap.isEmpty()) {
					for (String txnID : ledgerMap.keySet()) {
						LedgerEntry entry = ledgerMap.get(txnID);
						System.out.println(entry.stringifyEntry());
						System.out.println(entry.getSignature());
						// System.out.println();
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
						String signature2 = scan2.nextLine();
						if (count == 0 && l.enterFirstTransaction(line, signature2.trim())) {
							// First transaction from file
							count++;
						} else {
							if (l.enterTransaction(line, signature2.trim()))
								count++;
						}
					}
					scan2.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					System.out.println("ERROR: File not found.");
					System.err.println("ERROR: File not found.");
					e.printStackTrace();
				}

				break;
			case "B":
			case "BALANCE":
				if (toggleInteractive) {
					System.out.println("Enter User: ");
				}
				String user = scan.nextLine();
				if (balanceMap.get(user.toLowerCase()) != null) {
					System.out.println(user + " has " + balanceMap.get(user.toLowerCase()));
				} else {
					System.out.println("ERROR: User does not exist!");
					System.err.println("ERROR: User does not exist!");
				}
				break;
			case "W":
			case "WIPE":
				ledgerMap.clear();
				//balanceMap.clear();
				//refMap.clear();

				count = 0;
				// countInFile = 0;

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

					if (!ledgerMap.isEmpty()) {
						for (String txnID : ledgerMap.keySet()) {
							LedgerEntry entry = ledgerMap.get(txnID);
							out.println(entry.stringifyEntry());
							out.println();
						}
					} else {
						System.out.println("Ledger is empty.");
					}
				} catch (IOException e) {
					System.out.println("ERROR: File not found.");
				}

				break;

			case "R":
			case "READ":
			case "READ KEY FILE":
				if (toggleInteractive)
					System.out.println("Supply account name: ");
				String account = scan.nextLine();
				if (toggleInteractive)
					System.out.println("Supply public key file: ");
				String pubKeyFileName = scan.nextLine();

				PublicKey pubKey = null;

				if (!pubKeyFileName.equals("")) {
					try {
						pubKey = getPubKey(pubKeyFileName);
						//System.out.println("Public key generated");
					} catch (Exception e) {
						System.out.println("ERROR: Public Key " + account + " " + pubKeyFileName + " not read");
						continue;

					}
					if (pubKeyMap.get(account) != null) {
						System.out.println("WARN: Key already exists. Replacing it with the new one.");
						System.err.println("WARN: Key already exists. Replacing it with the new one.");
					}
					pubKeyMap.put(account, pubKey);
					System.out.println("SUCCESS: Public Key " + account + " " + pubKeyFileName + " read");
				} else {
					System.out.println("ERROR: Please provide a value for public key.");

				}
				break;

			case "C":
			case "CHECK":
				if (verbose)
					System.out.println("Please supply transaction ID to check against.");
				String txnID = scan.nextLine();
				LedgerEntry entry = null;
				String txnSignToVerify = "";
				if (ledgerMap.get(txnID) != null) {
					entry = ledgerMap.get(txnID);
					txnSignToVerify = entry.getSignature();
				} else {
					System.out.println("ERROR: Transaction not present in ledger. Please supply correct ID.");
					continue;
				}

				ArrayList<Transaction> intrans = entry.getIntrans();
				String txnUserName = "";
				// Putting a check for genesis/coinbase transaction
				if (entry.getM() == 0) {
					txnUserName = entry.getOuttrans().get(0).getName();
				} else {
					String txnUser = intrans.get(0).stringifyTransaction();
					// System.out.println("Transaction user: " + txnUser);
					txnUserName = "";
					if (idMap.get(txnUser) != null) {
						txnUserName = idMap.get(txnUser);
					}
				}
				//System.out.println("TxnUserName " + txnUserName);
				PublicKey userPubKey = null;
				// Check if public key present
				if (pubKeyMap.get(txnUserName) != null) {
					userPubKey = pubKeyMap.get(txnUserName);
				} else {
					System.out.println("ERROR: Public key for user " + txnUserName + " not loaded.");
					continue;
				}

				String stringifiedEntry = entry.stringifyEntry();
				// System.out.println("Stringified Entry: " + stringifiedEntry);
				// System.out.println();
				if (verifySignature(userPubKey, txnSignToVerify, stringifiedEntry
						.substring(stringifiedEntry.indexOf(";") + 1, stringifiedEntry.length()).trim(), txnID)) {
					System.out.println("OK");
				} else {
					System.out.println("Bad");
				}
				break;
			case "O":
			case "OUTPUT":
				Iterator<Map.Entry<String,LedgerEntry>> iter = ledgerMap.entrySet().iterator();
				ArrayList<Tuple> ledgerEntryArrToPrint = new ArrayList<>();
				// String txnID = scan.nextLine();
				int countInBlock = 0;
				while(iter.hasNext()) {
					
					Map.Entry<String,LedgerEntry> entry1 = iter.next();
					String txnSignToVerify1 = "";
					String txnID1 = entry1.getKey();
					LedgerEntry ledgerEntry = entry1.getValue();
					txnSignToVerify1 = ledgerEntry.getSignature();

					ArrayList<Transaction> intrans1 = ledgerEntry.getIntrans();
					String txnUserName1 = "";
					// Putting a check for genesis/coinbase transaction
					if (ledgerEntry.getM() == 0) {
						txnUserName1 = ledgerEntry.getOuttrans().get(0).getName();
					} else {
						String txnUser = intrans1.get(0).stringifyTransaction();
						// System.out.println("Transaction user: " + txnUser);
						txnUserName1 = "";
						if (idMap.get(txnUser) != null) {
							txnUserName1 = idMap.get(txnUser);
						}
					}
				//	System.out.println("TxnUserName " + txnUserName1);
					PublicKey userPubKey1 = null;
					// Check if public key present
					if (pubKeyMap.get(txnUserName1) != null) {
						userPubKey1 = pubKeyMap.get(txnUserName1);
					} else {
						System.out.println("ERROR: Public key for user " + txnUserName1 + " not loaded.");
						continue;
					}

					String stringifiedEntry1 = ledgerEntry.stringifyEntry();
					// System.out.println("Stringified Entry: " +
					// stringifiedEntry);
					// System.out.println();
					if (verifySignature(userPubKey1, txnSignToVerify1, stringifiedEntry1
							.substring(stringifiedEntry1.indexOf(";") + 1, stringifiedEntry1.length()).trim(),txnID1)) {
						countInBlock++;
						Tuple tuple = new Tuple(stringifiedEntry1, txnSignToVerify1);
						ledgerEntryArrToPrint.add(tuple);
//						System.out.println(stringifiedEntry1);
//						System.out.println(txnSignToVerify1);
						iter.remove();
					}
				}
				
				System.out.println(countInBlock);
				for(Tuple tuple: ledgerEntryArrToPrint){
					System.out.println(tuple.getTxn());
					System.out.println(tuple.getSignature());
				}
				
				break;
			default:
				System.out.println("ERROR: Not a valid option. Select a valid option from above menu.");
				System.err.println("ERROR: Not a valid option. Select a valid option from above menu.");
				break;
			}

		}

	}

	private static PublicKey getPubKey(String pubKeyFileName) throws NoSuchAlgorithmException, InvalidKeySpecException {
		File f = new File(pubKeyFileName);
		FileInputStream fis;
		byte[] keyBytes = new byte[(int) f.length()];
		try {
			fis = new FileInputStream(f);
			DataInputStream dis = new DataInputStream(fis);

			dis.readFully(keyBytes);
			dis.close();
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: File not found.");
			System.err.println("ERROR: File not found.");
		} catch (IOException e) {
			System.out.println("ERROR: IO Exception.");
			System.err.println("ERROR: IO Exception.");
		}
		String temp = new String(keyBytes);
		String publicKeyPEM = temp.replace("-----BEGIN PUBLIC KEY-----\n", "");
		publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
		publicKeyPEM = publicKeyPEM.replace("\n", "");
		byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);

		X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
		KeyFactory kf = KeyFactory.getInstance("RSA");

		return kf.generatePublic(spec);
	}

	public static boolean verifySignature(PublicKey publicKey, String signature, String data, String txnID)
			throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, IOException,
			SignatureException {
		Signature sig = Signature.getInstance("SHA256withRSA");
		sig.initVerify(publicKey);
		sig.update(data.getBytes());

		if (signature.equals("")) {
			System.out.println("ERROR: Signature not provided for transaction " + txnID);
			return false;
		}
		try {
			if (sig.verify(Base64.getDecoder().decode(signature))) {
				return true;

			}
		} catch (Exception e) {
			System.out.println("ERROR: Incorrect signature provided for transaction " + txnID);
		}
		return false;

	}

}
