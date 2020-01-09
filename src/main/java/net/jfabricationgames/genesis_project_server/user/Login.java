package net.jfabricationgames.genesis_project_server.user;

import net.jfabricationgames.genesis_project_server.util.Cryptographer;

public class Login {
	
	private String username;
	private String password;
	
	public Login() {
		
	}
	
	@Override
	public String toString() {
		return "Login [username=" + username + ", password=" + password + "]";
	}
	
	public Login(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * Encrypt the password symmetrically to not send a clear-text password via an insecure connection.
	 */
	public void encryptPassword(String encryptionPassword) {
		password = Cryptographer.encryptText(password, encryptionPassword);
	}
	/**
	 * Decrypt the encrypted password.
	 */
	public void decryptPassword(String encryptionPassword) {
		password = Cryptographer.decryptText(password, encryptionPassword);
	}
}