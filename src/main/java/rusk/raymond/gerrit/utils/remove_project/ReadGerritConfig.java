package rusk.raymond.gerrit.utils.remove_project;
/*
 *  Copyright (C) 2012 Raymond Rusk
 *  
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to
 *  deal in the Software without restriction, including without limitation the
 *  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 *  sell copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 *  IN THE SOFTWARE.
 */  

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadGerritConfig {

	private static String SITE_PATH="/home/rrusk/Notes/gerrit/";
	private static String CONFIG_FILE=SITE_PATH+"etc/gerrit.config";
	private static String SECURE_FILE=SITE_PATH+"etc/secure.config";
	
	private Boolean initialized = false;
	private String dbDrv = null;
	private String dbUrl = null;
	private String username = null;
	private String password = null;
	private String basePath = null;
	
	private String readFile(String pathname) throws FileNotFoundException {
		File file = new File(pathname);
		StringBuilder fileContents = new StringBuilder((int)file.length());
		Scanner scanner = new Scanner(file);
		String lineSeparator = System.getProperty("line.separator");

		while(scanner.hasNextLine()) {
			fileContents.append(scanner.nextLine() + lineSeparator);
		}
		scanner.close();
		return fileContents.toString();
	}
	
	private String getValue(String text, String pattern) {
		String value = null;
		Matcher matcher = Pattern.compile(pattern).matcher(text);
		if (matcher.find()) {
			value = matcher.group(1);
		}
		return value;
	}
	
	public void init() {
		ReadGerritConfig rc = new ReadGerritConfig();
		String stext = null;
		String text = null;
		try {
			stext = rc.readFile(SECURE_FILE);
		} catch (FileNotFoundException e) {
			System.out.println("The optional configuration file " + SECURE_FILE + " does not exist.");
		}
		try {
			text = rc.readFile(CONFIG_FILE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		basePath = rc.getValue(text,"(?i)(?m)gerrit][^\\]]*basePath\\s*=\\s*(.*)");
		String databaseType = rc.getValue(text,"(?i)(?m)database][^\\]]*type\\s*=\\s*(\\w+)");
		String hostname = rc.getValue(text,"(?i)(?m)database][^\\]]*hostname\\s*=\\s*(.*)");
		String port = rc.getValue(text,"(?i)(?m)database][^\\]]*port\\s*=\\s*(\\w+)");
		String database = rc.getValue(text,"(?i)(?m)database][^\\]]*database\\s*=\\s*(\\w+)");

		// secure.config overrides (or supplements) the settings supplied by gerrit.config
		if (stext != null && !stext.isEmpty()) {
			username = rc.getValue(stext,"(?i)(?m)database][^\\]]*username\\s*=\\s*(\\w+)");
			password = rc.getValue(stext,"(?i)(?m)database][^\\]]*password\\s*=\\s*(\\w+)");
		}
		if (username==null || username.isEmpty()) {
			username = rc.getValue(text,"(?i)(?m)database][^\\]]*username\\s*=\\s*(\\w+)");
		}
		if (password==null || password.isEmpty()) {
			password = rc.getValue(text,"(?i)(?m)database][^\\]]*password\\s*=\\s*(\\w+)");
		}
		if (hostname == null || hostname.isEmpty()) {
			hostname = "localhost";
		}
		if (port == null || port.isEmpty()) {
			if (databaseType.equalsIgnoreCase("mysql")) {
				port = "3306";
			} else if (databaseType.equalsIgnoreCase("postgresql")) {
				port = "5432";
			}
		}

		if (databaseType.equalsIgnoreCase("mysql")) {
			dbDrv = "com.mysql.jdbc.Driver";
			// jdbc:mysql://localhost:3306/reviewdb
			dbUrl = "jdbc:mysql://"+hostname+":"+port+"/"+database;
		}

		initialized = true;
	}
	
	public Boolean getInit() {
		return initialized;
	}
	
	public String getDbDrv() {
		if (!initialized) init();
		return dbDrv;
	}

	public String getDbUrl() {
		if (!initialized) init();
		return dbUrl;
	}
	
	public String getUsername() {
		if (!initialized) init();
		return username;
	}
	
	public String getPassword() {
		if (!initialized) init();
		return password;
	}
	
	public String getBasePath() {
		if (!initialized) init();
		return basePath;
	}
	
	public static void main(String[] args) {
		ReadGerritConfig rc = new ReadGerritConfig();
		rc.init();
		System.out.println("dbDrv="+rc.getDbDrv());
		System.out.println("dbUrl="+rc.getDbUrl());
		System.out.println("username="+rc.getUsername());
		System.out.println("password="+rc.getPassword());
		System.out.println("basePath="+rc.getBasePath());
	}
}
