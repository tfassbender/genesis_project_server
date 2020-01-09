package net.jfabricationgames.genesis_project_server.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class CryptographerTest {
	
	@Test
	public void testEncryption() {
		String text = "a_text_that_is_encrypted";
		String password = "secure_password";
		String encrypted = Cryptographer.encryptText(text, password);
		assertNotEquals(text, encrypted);
	}
	
	@Test
	public void testDecryption() {
		String text = "a_text_that_is_encrypted";
		String password = "secure_password";
		String encrypted = Cryptographer.encryptText(text, password);
		String decrypted = Cryptographer.decryptText(encrypted, password);
		assertNotEquals(text, encrypted);
		assertEquals(text, decrypted);
	}
}
