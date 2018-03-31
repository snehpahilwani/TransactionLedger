package ledgerprogram;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;

public class wallet {

	// HashMap to store private keys for the users
	static HashMap<String, PrivateKey> privKeyMap = new HashMap<>();

	// Printing of interactive menu
	public static void printInteractiveMenu() {
		System.out.println("[R]ead Key File\n[S]ign Transaction\n[E]xit\nSelect a command:");
	}

	public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		// TODO Auto-generated method stub
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		while (true) {
			printInteractiveMenu();
			String input = scan.nextLine().toUpperCase();

			switch (input) {
			case "R":
			case "READ":

				System.out.println("Supply account name: ");
				String account = scan.nextLine();

				System.out.println("Supply public key file: ");
				String privKeyFileName = scan.nextLine();

				PrivateKey privKey = null;

				if (!privKeyFileName.equals("")) {
					try {
						privKey = getPrivKey(privKeyFileName);
						// System.out.println("Public key generated");
					} catch (Exception e) {
						System.out.println("ERROR: Public Key " + account + " " + privKeyFileName + " not read");
						continue;

					}
					if (privKeyMap.get(account) != null) {
						System.out.println("WARN: Key already exists. Replacing it with the new one.");
						System.err.println("WARN: Key already exists. Replacing it with the new one.");
					}
					privKeyMap.put(account, privKey);
					System.out.println("SUCCESS: Private Key " + account + " " + privKeyFileName + " read");
				} else {
					System.out.println("ERROR: Please provide a value for private key.");

				}
				break;
			case "S":
			case "SIGN":
				System.out.println("Enter username who wants to sign:");
				String account1 = scan.nextLine();
				if(privKeyMap.get(account1) == null){
					System.out.println("ERROR: Private key for user " + account1 + " not loaded.");
					continue;
				}
				System.out.println("Enter transaction to sign:");
				String data = scan.nextLine();
				data = makeValid(data);
				data = data.substring(data.indexOf(";") + 1, data.length()).trim();
				String signature = sign(data, privKeyMap.get(account1));
				
				System.out.println("Signature: " + signature);
				break;
			case "E":
			case "EXIT":
				System.out.println("Exiting program. Goodbye.");
				scan.nextLine();
				System.exit(0);
				break;
			default:
				System.out.println("ERROR: Not a valid option. Select a valid option from above menu.");
				System.err.println("ERROR: Not a valid option. Select a valid option from above menu.");
				break;
			}

		}

	}
	
	// To make input valid for SHA hash generation
	public static String makeValid(String inputTransaction) {
		inputTransaction = inputTransaction.replace(",", ", ");
		inputTransaction = inputTransaction.replace(";", "; ");
		inputTransaction = inputTransaction.replace(",  ", ", ");
		inputTransaction = inputTransaction.replace(";  ", "; ");
		return inputTransaction;
	}

	public static PrivateKey getPrivKey(String filename)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		String privateKeyContent = new String(Files.readAllBytes(Paths.get(filename)));
		privateKeyContent = privateKeyContent.replaceAll("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "");
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
		PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);
		return privKey;
	}

	public static String sign(String data, PrivateKey privKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		//String data = "1; (cbdf7b37, 0); 3; (Gopesh, 100)(Bob, 45)(Bob, 5)";
		Signature rsa = Signature.getInstance("SHA256withRSA");
		//String pubFile = "C:\\Users\\snehc\\workspace\\ledger\\src\\bobpublic_key.pem";
		rsa.initSign(privKey);
		rsa.update(data.getBytes());
		byte[] signature = rsa.sign();
		String base64Signature = Base64.getEncoder().encodeToString(signature);
		return base64Signature;
	}

}
