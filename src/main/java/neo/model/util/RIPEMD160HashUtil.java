package neo.model.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * utilities having to do with the RIPEMD160 hash.
 *
 * @author coranos
 *
 */
public final class RIPEMD160HashUtil {

	/**
	 * returns the RIPEMD160 hash of the bytes.
	 *
	 * @param bytes
	 *            the bytes to hash.
	 * @return the hash.
	 */
	public static byte[] getRIPEMD160Hash(final byte[] bytes) {
		final String digestName = "RIPEMD160";
		final MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(digestName);
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException("exception getting MessageDigest \"" + digestName + "\"", e);
		}
		final byte[] hash = digest.digest(bytes);
		return hash;
	}

	/**
	 * the constructor.
	 */
	private RIPEMD160HashUtil() {

	}
}
