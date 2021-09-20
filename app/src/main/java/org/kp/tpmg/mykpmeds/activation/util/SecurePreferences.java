package org.kp.tpmg.mykpmeds.activation.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecurePreferences {

	private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
	private static final String KEY_TRANSFORMATION = "AES/CBC/PKCS5Padding";
	private static final String SECRET_KEY_HASH_TRANSFORMATION = "SHA-256";
	private static final String CHARSET = "UTF-8";
	private boolean mEncryptKeysFlg;
	private Cipher writer;
	private Cipher reader;
	private Cipher keyWriter;
	private SharedPreferences preferences;
	private String preferenceName;

	private Random random = new SecureRandom();
	private static byte[] bytearray = new byte[32];

	public synchronized String getPreferenceName() {
		return preferenceName;
	}

	public SecurePreferences(Context context, String preferenceName,
			String secureKey, boolean encryptKeys) {
		try {
			this.writer = Cipher.getInstance(TRANSFORMATION);
			this.reader = Cipher.getInstance(TRANSFORMATION);
			this.keyWriter = Cipher.getInstance(KEY_TRANSFORMATION);
			this.preferenceName = preferenceName;
			initCiphers(secureKey);
			this.preferences = context.getSharedPreferences(preferenceName,
					Context.MODE_PRIVATE);
			this.mEncryptKeysFlg = encryptKeys;
			random.nextBytes(bytearray);

		} catch (GeneralSecurityException | UnsupportedEncodingException e) {
			LoggerUtils.exception( e.getMessage());
		}
	}

	protected synchronized void initCiphers(String secureKey)
			throws UnsupportedEncodingException, NoSuchAlgorithmException,
			InvalidKeyException, InvalidAlgorithmParameterException {
		IvParameterSpec ivSpec = getIv();
		SecretKeySpec secretKey = getSecretKey(secureKey);
		writer.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
		reader.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
		keyWriter.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
	}

	protected synchronized IvParameterSpec getIv() {
		byte[] ivArray = new byte[writer.getBlockSize()];
		//System.arraycopy("fldsjfodasjifudslfjdsaofshaufihadsf".getBytes(), 0, ivArray, 0, writer.getBlockSize());
		System.arraycopy(bytearray, 0, ivArray, 0, writer.getBlockSize());
		return new IvParameterSpec(ivArray);
	}

	protected synchronized SecretKeySpec getSecretKey(String key)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		byte[] keyBytes = createKeyBytes(key);
		return new SecretKeySpec(keyBytes, TRANSFORMATION);
	}

	protected synchronized byte[] createKeyBytes(String key)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest
				.getInstance(SECRET_KEY_HASH_TRANSFORMATION);
		messageDigest.reset();
		byte[] keyBytes = messageDigest.digest(key.getBytes(CHARSET));
		return keyBytes;
	}

	public synchronized void putString(String key, String value) {
		if (value == null) {
			preferences.edit().remove(toKey(key)).apply();
		} else {
			putValue(toKey(key), value);
		}
	}

	public synchronized void removeValue(String key) {
		preferences.edit().remove(toKey(key)).apply();
	}

	public synchronized String getString(String key, String defaultVal) {
		if (preferences.contains(toKey(key))) {
			String securedEncodedValue = preferences.getString(toKey(key), "");
			return decrypt(securedEncodedValue);
		}
		return defaultVal;
	}

	public synchronized Set<String> getStringSet(String key, Set<String> defaultValue) {
		Set<String> decryptValues = new HashSet<>();
		if (preferences.contains(toKey(key))) {
			Set<String> securedEncodedValue = preferences.getStringSet(
					toKey(key), defaultValue);
			for (String value : securedEncodedValue) {
				decryptValues.add(decrypt(value));
			}
			return decryptValues;
		}
		return defaultValue;
	}

	public synchronized void putStringSet(String key, Set<String> values) {
		Set<String> encryptValues = new HashSet<>();
		for (String value : values) {
			encryptValues.add(encrypt(value, writer));

		}
		preferences.edit().putStringSet(toKey(key), encryptValues).apply();

	}

	public synchronized void clear() {
		preferences.edit().clear().commit();
	}

	private synchronized String toKey(String key) {
		if (mEncryptKeysFlg) {
			return encrypt(key, keyWriter);
		} else {
			return key;
		}
	}

	private synchronized void putValue(String key, String value) {
		String secureValueEncoded = encrypt(value, writer);
		preferences.edit().putString(key, secureValueEncoded).apply();
	}

	protected synchronized String encrypt(String value, Cipher writer) {
		byte[] secureValue = null;
		try {
			secureValue = convert(writer, value.getBytes(CHARSET));
		} catch (Exception e) {
			LoggerUtils.exception( e.getMessage());
		}
		String secureValueEncoded = Base64.encodeToString(secureValue,
				Base64.NO_WRAP);
		return secureValueEncoded;
	}

	protected synchronized String decrypt(String securedEncodedValue) {
		byte[] securedValue = Base64
				.decode(securedEncodedValue, Base64.NO_WRAP);
		byte[] value = convert(reader, securedValue);
		try {
			return new String(value, CHARSET);
		} catch (Exception e) {
			LoggerUtils.exception( e.getMessage());
		}
		return securedEncodedValue;
	}

	private synchronized static byte[] convert(Cipher cipher, byte[] byteArray) {
		try {
			return cipher.doFinal(byteArray);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			LoggerUtils.exception( e.getMessage());
			return byteArray;
		}

	}

	public String getDecryptedString(String value) {
		return decrypt(value);
	}

	public Map<String, ?> getAllKeys() {
		return preferences.getAll();
	}
}
