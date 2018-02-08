package ledger;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.HashMap;

public class Trial {

	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		//String transaction = scan.nextLine();
		//System.out.println(transaction);
		String transaction = "f2cea539; 0; ; 1; (Alice, 1000)";
		// Ledger l = new Ledger();
		System.out.println(Ledger.firstLineWellFormed(transaction));
		
		
		ArrayList<Integer> a = new ArrayList<>();
		HashMap<Integer, ArrayList<Integer>> map = new HashMap<>();
		HashMap<Integer, Integer> map1 = new HashMap<>();
		a.add(1);
		a.add(2);
		a.set(0, 3);
		//map.put(1,null);
		if(map1.get(1)==null){
			System.out.println("Na");
		}
		//map.remove(2);
		ArrayList<Integer> something = map.get(2);
		System.out.println(something==null);
		
		for(int i : a){
			System.out.println(i);
		}
	}
}
