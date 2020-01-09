package net.jfabricationgames.genesis_project_server.util;

/**
 * 
 * @author Tobias Faßbender
 * @version Jun, 2015
 */
public class Cryptographer {
	
	private static final String encodingChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZäöüÄÖÜ1234567890,;.:-_#'+*~!\"§$%&/()=?ß`^°{[]}\\'/- \n\t";
	
	public static String encryptText(String text, String password) {
		String encryptedText = "";
		int charIndex;
		int passIndex;
		for (int i = 0; i < text.length(); i++) {
			charIndex = encodingChars.indexOf(text.charAt(i));
			passIndex = encodingChars.indexOf(password.charAt(i % password.length()));
			charIndex += passIndex;
			charIndex %= encodingChars.length();
			encryptedText += encodingChars.charAt(charIndex);
		}
		return encryptedText;
	}
	public static String decryptText(String text, String password) {
		String decryptedText = "";
		int charIndex;
		int passIndex;
		for (int i = 0; i < text.length(); i++) {
			charIndex = encodingChars.indexOf(text.charAt(i));
			passIndex = encodingChars.indexOf(password.charAt(i % password.length()));
			charIndex -= passIndex;
			charIndex += encodingChars.length();
			charIndex %= encodingChars.length();
			decryptedText += encodingChars.charAt(charIndex);
		}
		return decryptedText;
	}
}