package dk.elera.damnation.bungeebouncer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyManager {
	private static final String ASYMMETRIC_METHOD = "RSA";
	private static final String BUNGEEBOUNCER_PRIVATE_KEY = "bungeebouncer.private.key";
	private static final String BUNGEEBOUNCER_PUBLIC_KEY = "bungeebouncer.public.key";

	private static KeyPair generateNewKeys() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ASYMMETRIC_METHOD);
			keyGen.initialize(1024);
			KeyPair keyPair = keyGen.generateKeyPair();
			saveKeyPair(keyPair);
			return keyPair;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static PrivateKey getPrivate(){
		try {
			return LoadPrivateKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			return generateNewKeys().getPrivate();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static PublicKey getPublic(){
		try {
			return LoadPublicKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static PrivateKey LoadPrivateKey()
			throws IOException, NoSuchAlgorithmException,
			InvalidKeySpecException {
		// Read Private Key.
		File filePrivateKey = new File(BUNGEEBOUNCER_PRIVATE_KEY);
		FileInputStream fis = new FileInputStream(BUNGEEBOUNCER_PRIVATE_KEY);
		byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
		fis.read(encodedPrivateKey);
		fis.close();
 
		// Generate KeyPair.
		KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC_METHOD);
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
				encodedPrivateKey);
		
		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
 
		return privateKey;
	}

	private static PublicKey LoadPublicKey()
			throws IOException, NoSuchAlgorithmException,
			InvalidKeySpecException {
		// Read Public Key.
		File filePublicKey = new File(BUNGEEBOUNCER_PUBLIC_KEY);
		FileInputStream fis = new FileInputStream(BUNGEEBOUNCER_PUBLIC_KEY);
		byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
		fis.read(encodedPublicKey);
		fis.close();
 
		// Generate KeyPair.
		KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC_METHOD);
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
				encodedPublicKey);
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
 
		return publicKey;
	}

	private static void saveKeyPair(KeyPair keyPair) throws IOException {
		PrivateKey privateKey = keyPair.getPrivate();
		PublicKey publicKey = keyPair.getPublic();
 
		// Store Public Key.
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
				publicKey.getEncoded());
		FileOutputStream fos = new FileOutputStream(BUNGEEBOUNCER_PUBLIC_KEY);
		fos.write(x509EncodedKeySpec.getEncoded());
		fos.close();
 
		// Store Private Key.
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
				privateKey.getEncoded());
		fos = new FileOutputStream(BUNGEEBOUNCER_PRIVATE_KEY);
		fos.write(pkcs8EncodedKeySpec.getEncoded());
		fos.close();
	}

	
	private KeyManager() {}
}
