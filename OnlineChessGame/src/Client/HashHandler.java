package Client;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashHandler {
	public String MD5(String plaintext){
		String result = null;
		try {
			MessageDigest messagedigest = MessageDigest.getInstance("MD5");
			messagedigest.update(plaintext.getBytes());
			byte[] bytes = messagedigest.digest();
			result = javax.xml.bind.DatatypeConverter.printHexBinary(bytes);		
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}
}