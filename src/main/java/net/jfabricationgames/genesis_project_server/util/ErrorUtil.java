package net.jfabricationgames.genesis_project_server.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class ErrorUtil {
	
	/**
	 * Get a StackTrace of a Throwable as a string
	 * 
	 * @param throwable
	 * @return
	 */
	public static String getStackTraceAsString(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		return sw.toString();
	}
}