package net.jfabricationgames.genesis_project_server.user;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import net.jfabricationgames.genesis_project_server.database.CheckedSqlConsumer;
import net.jfabricationgames.genesis_project_server.database.DatabaseConnection;
import net.jfabricationgames.genesis_project_server.database.SqlExecutionType;
import net.jfabricationgames.genesis_project_server.exception.GameDataException;
import net.jfabricationgames.genesis_project_server.exception.GameDataException.Cause;

public class UserDataManager {
	
	/**
	 * Encryption key for passwords. Not loaded from configuration because it's a symmetric key.
	 */
	private String passwordEncryptionKey = "vcuh31250hvcsojnl312vcnlsgr329fdsip";
	private String salt = "ch48cho2nlc";
	
	private MessageDigest md5;
	
	private static UserDataManager instance;
	
	private String password;
	
	private UserDataManager() throws IllegalStateException {
		try {
			md5 = MessageDigest.getInstance("md5");
		}
		catch (NoSuchAlgorithmException nsae) {
			throw new IllegalStateException("hash encryptor couldn't be created", nsae);
		}
	}
	
	public static synchronized UserDataManager getInstance() throws IllegalStateException {
		if (instance == null) {
			instance = new UserDataManager();
		}
		return instance;
	}
	
	/**
	 * Creates a new user by a {@link Login} object.
	 * 
	 * @param login
	 *        The login for the user.
	 */
	public void createUser(Login login) throws GameDataException {
		String query = "INSERT INTO " + DatabaseConnection.getTable(DatabaseConnection.TABLE_USERS) + " (username, password) VALUES (?, ?)";
		
		CheckedSqlConsumer<PreparedStatement> variableSetter = ps -> {
			ps.setString(1, login.getUsername());
			//store a hash of the password
			ps.setString(2, getPasswordHash(login.getPassword()));
		};
		
		//create the new user
		DatabaseConnection.executeCheckedSQL(query, SqlExecutionType.UPDATE, variableSetter, null);
	}
	
	/**
	 * Updates a user (username and/or password)
	 * 
	 * @param logins
	 *        The users logins: The first has to be the valid current login; The second is the update.
	 */
	public void updateUser(Login current, Login update) throws GameDataException {
		//verify the users current login first
		if (verifyUser(current)) {
			//if the user is verified, update the login
			String query = "UPDATE " + DatabaseConnection.getTable(DatabaseConnection.TABLE_USERS)
					+ " SET username = ?, password = ? WHERE username = ?";
			
			CheckedSqlConsumer<PreparedStatement> variableSetter = ps -> {
				ps.setString(1, update.getUsername());
				//store a hash of the password
				ps.setString(2, getPasswordHash(update.getPassword()));
				ps.setString(3, current.getUsername());
			};
			
			//update the user
			DatabaseConnection.executeCheckedSQL(query, SqlExecutionType.UPDATE, variableSetter, null);
		}
		else {
			throw new GameDataException("user verification failed", Cause.NO_PERMISSION);
		}
	}
	
	/**
	 * Verifies a user's login.
	 * 
	 * @param login
	 *        The user's login (username and password) in JSON form.
	 * 
	 * @return Returns true if the login is correct. False otherwise.
	 */
	public boolean verifyUser(Login login) throws GameDataException {
		String query = "SELECT password FROM " + DatabaseConnection.getTable(DatabaseConnection.TABLE_USERS) + " WHERE username = ?";
		
		CheckedSqlConsumer<PreparedStatement> variableSetter = ps -> {
			ps.setString(1, login.getUsername());
		};
		CheckedSqlConsumer<ResultSet> resultConsumer = resultSet -> {
			if (resultSet.next()) {
				password = resultSet.getString(1);
			}
		};
		
		//load the user's password
		password = null;
		DatabaseConnection.executeCheckedSQL(query, SqlExecutionType.QUERY, variableSetter, resultConsumer);
		
		if (password == null) {
			throw new GameDataException("user couldn't be loaded", Cause.NOT_FOUND);
		}
		else if (password.equals(getPasswordHash(login.getPassword()))) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Get a password hash from an encrypted password. The password is first decrypted and then the hash is created.
	 * 
	 * @param password
	 *        The encrypted password String
	 * 
	 * @return The password hash (md5)
	 */
	private String getPasswordHash(String password) {
		try {
			byte[] decryptedPassword = getDecryptedPassword(password);
			byte[] decryptedPasswordWithSalt = new byte[decryptedPassword.length + salt.length()];
			System.arraycopy(decryptedPassword, 0, decryptedPasswordWithSalt, 0, decryptedPassword.length);
			System.arraycopy(salt.getBytes(), 0, decryptedPasswordWithSalt, decryptedPassword.length, salt.length());
			
			String hashedPassword = getHash(decryptedPasswordWithSalt);
			
			//delete the decrypted passwords
			for (int i = 0; i < decryptedPassword.length; i++) {
				decryptedPassword[i] = 0;
			}
			for (int i = 0; i < decryptedPasswordWithSalt.length; i++) {
				decryptedPasswordWithSalt[i] = 0;
			}
			
			return hashedPassword;
		}
		catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
			throw new IllegalStateException("decryption failed", e);
		}
	}
	
	/**
	 * Decrypts a password to a byte[] (so it can be overwritten after use)
	 */
	private byte[] getDecryptedPassword(String password)
			throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
		byte[] key = passwordEncryptionKey.getBytes();
		
		Cipher cipher = Cipher.getInstance("AES");
		SecretKeySpec k = new SecretKeySpec(key, "AES");
		cipher.init(Cipher.DECRYPT_MODE, k);
		byte[] data = cipher.doFinal(password.getBytes());
		
		return data;
	}
	
	/**
	 * Generate an MD5 hash for a password.
	 * 
	 * @param password
	 *        The password String
	 * 
	 * @return The password hash (md5)
	 */
	private String getHash(byte[] password) {
		byte[] inBuff = password;
		byte[] outBuff = md5.digest(inBuff);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < outBuff.length; i++) {
			sb.append(Integer.toString((outBuff[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}
}