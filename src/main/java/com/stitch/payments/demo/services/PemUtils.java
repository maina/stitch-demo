//Copyright 2017 - https://github.com/lbalmaceda
//Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
//The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.stitch.payments.demo.services;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

@Slf4j
public class PemUtils {
	private static final SecureRandom secureRandom = new SecureRandom();
	public static final String CODE_CHALLENGE_METHOD = "S256";

	private static byte[] parsePEMFile(File pemFile) throws IOException {
		if (!pemFile.isFile() || !pemFile.exists()) {
			throw new FileNotFoundException(String.format("The file '%s' doesn't exist.", pemFile.getAbsolutePath()));
		}
		PemReader reader = new PemReader(new FileReader(pemFile));
		PemObject pemObject = reader.readPemObject();

		byte[] content = pemObject.getContent();
		reader.close();
		return content;
	}

	private static PublicKey getPublicKey(byte[] keyBytes, String algorithm) {
		PublicKey publicKey = null;
		try {
			KeyFactory kf = KeyFactory.getInstance(algorithm);
			EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
			publicKey = kf.generatePublic(keySpec);
		} catch (NoSuchAlgorithmException e) {
			log.info("Could not reconstruct the public key, the given algorithm could not be found.");
		} catch (InvalidKeySpecException e) {
			log.info("Could not reconstruct the public key");
		}

		return publicKey;
	}

	private static PrivateKey getPrivateKey(byte[] keyBytes, String algorithm) {
		PrivateKey privateKey = null;
		try {
			KeyFactory kf = KeyFactory.getInstance(algorithm);
			EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
			privateKey = kf.generatePrivate(keySpec);
		} catch (NoSuchAlgorithmException e) {
			log.info("Could not reconstruct the private key, the given algorithm could not be found.");
		} catch (InvalidKeySpecException e) {
			log.info("Could not reconstruct the private key");
		}

		return privateKey;
	}

	public static PublicKey readPublicKeyFromFile(String filepath, String algorithm) throws IOException {

		byte[] bytes = PemUtils.parsePEMFile(new File(filepath));
		return PemUtils.getPublicKey(bytes, algorithm);
	}

	public static PrivateKey readPrivateKeyFromFile(String filepath, String algorithm) throws IOException {
		byte[] bytes = PemUtils.parsePEMFile(new File(filepath));
		return PemUtils.getPrivateKey(bytes, algorithm);
	}

	public static String hmacSha256(String data, String key) throws InvalidKeyException, NoSuchAlgorithmException {
		final String ALGORITHM = "HmacSHA256";
		
		Mac sha256HMAC = Mac.getInstance(ALGORITHM);

		SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
		sha256HMAC.init(secretKey);

		return byteArrayToHex(sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
	}

	public static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder(a.length * 2);
		for (byte b : a)
			sb.append(String.format("%02x", b));
		return sb.toString();
	}
	
	
	public static String generateCodeChallenge(final String CODE_VERIFIER) {
		try {
			return createHash(CODE_VERIFIER);
		} catch (NoSuchAlgorithmException ignored) {
			throw new RuntimeException("Cannot create code challenge");
		}
	}

	public static String generateCodeVerifier() {
		byte[] codeVerifier = new byte[32];
		secureRandom.nextBytes(codeVerifier);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
	}
	
	private static String createHash(final String CODE_VERIFIER) throws NoSuchAlgorithmException {
		try {
			byte[] bytes = CODE_VERIFIER.getBytes(StandardCharsets.US_ASCII);
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(bytes, 0, bytes.length);
			byte[] digest = messageDigest.digest();
			return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String nonceGenerator(){
	   
	    StringBuilder stringBuilder = new StringBuilder();
	    for (int i = 0; i < 15; i++) {
	        stringBuilder.append(secureRandom.nextInt(10));
	    }
	    String randomNumber = stringBuilder.toString();
	    return randomNumber;
	}


}