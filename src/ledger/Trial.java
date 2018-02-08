package ledger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashMap;

public class Trial {
	
    static String sha1(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
         
        return sb.toString().substring(0,8);
    }

    public static String makeValid(String inputTransaction){
    	inputTransaction = inputTransaction.replace(",", ", ");
    	System.out.println(inputTransaction);
    	inputTransaction = inputTransaction.replace(";", "; ");
    	System.out.println(inputTransaction);
    	inputTransaction = inputTransaction.replace(",  ", ", ");
    	System.out.println(inputTransaction);
    	inputTransaction = inputTransaction.replace(";  ", "; ");
    	System.out.println(inputTransaction);
    	return inputTransaction;
    }
   

	public static void main(String[] args) throws NoSuchAlgorithmException {
		Scanner scan = new Scanner(System.in);
		//String transaction = scan.nextLine();
		//System.out.println(transaction);
		//String transaction = "f2cea539; 0; ; 1; (Alice, 1000)";
		String transaction = "1; (f2cea539, 0); 3; (Bob, 150)(Alice, 845)(Gopesh, 5)\n";
		// Ledger l = new Ledger();
		//System.out.println(Ledger.firstLineWellFormed(transaction));
		String transaction1 = "1; (f2cea539, 0); 3; (Bob, 150)(Alice,845)(Gopesh,5)\n";
//		Transaction transaction1 = new Transaction("Hi", 123);
//		System.out.println(transaction1.amount);
//		Transaction transaction2 = new Transaction(transaction1.name, transaction1.amount);
//		transaction2.amount = 10;
//		
//		System.out.println(transaction1.amount + " " + transaction2.amount);
		
		//String[] trans = transaction.split(";",2);
		//System.out.println(trans[1]);
		//System.out.println(sha1(trans[1].trim() + "\n"));
//		for(String s: trans){
//			System.out.println(s.trim());
//		}
		
//		ArrayList<Integer> a = new ArrayList<>();
//		HashMap<Integer, ArrayList<Integer>> map = new HashMap<>();
//		HashMap<Integer, Integer> map1 = new HashMap<>();
//		a.add(1);
//		a.add(2);
//		a.set(0, 3);
//		//map.put(1,null);
//		if(map1.get(1)==null){
//			System.out.println("Na");
//		}
//		//map.remove(2);
//		ArrayList<Integer> something = map.get(2);
//		System.out.println(something==null);
//		
//		for(int i : a){
//			System.out.println(i);
//		}
//		
		System.out.println(sha1(transaction));
		System.out.println(sha1(makeValid(transaction1)));
//		String[] transComponents = transaction.split(";");
//		String txnID = transComponents[0].trim();
//		String gentxnID = "";
//		String[] trans = transaction.split(";", 2);
//		try {
//			gentxnID = sha1(trans[1].trim() + "\n");
//		} catch (NoSuchAlgorithmException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(gentxnID);
//		System.out.println(txnID);
//		System.out.println(gentxnID.equals(txnID));
//	    String str = "hi";
//	    String str2 = str;
//	    System.out.println(str=="hi");
//	    System.out.println("hi".equals(str2));
	}
}
