package ca.footeware.rcp.journal.ui.model;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Superstar {

	private static final String AES = "AES";
	private static final String MD5 = "MD5";

	public static String decrypt(String text, String pass) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		MessageDigest messageDigest = MessageDigest.getInstance(MD5);
		Key key = new SecretKeySpec(messageDigest.digest(pass.getBytes(StandardCharsets.UTF_8)), AES);
		Cipher cipher = Cipher.getInstance(AES);
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] encoded = text.getBytes(StandardCharsets.UTF_8);
		byte[] decoded = Base64.getDecoder().decode(encoded);
		byte[] decrypted = cipher.doFinal(decoded);
		return new String(decrypted, StandardCharsets.UTF_8);
	}

	public static String encrypt(String text, String pass) throws InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		MessageDigest messageDigest = MessageDigest.getInstance(MD5);
		Key key = new SecretKeySpec(messageDigest.digest(pass.getBytes(StandardCharsets.UTF_8)), AES);
		Cipher cipher = Cipher.getInstance(AES);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
		byte[] encrypted = cipher.doFinal(textBytes);
		byte[] encoded = Base64.getEncoder().encode(encrypted);
		return new String(encoded, StandardCharsets.UTF_8);
	}

	private Superstar() {
	}
}
