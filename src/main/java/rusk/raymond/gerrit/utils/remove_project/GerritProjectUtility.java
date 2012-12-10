package rusk.raymond.gerrit.utils.remove_project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class GerritProjectUtility {

	private static String PROPERTIES_FILE = "gerrit.properties";
	
	private static Connection conn = null;
	
	public static String basePath = null;
	
	private Properties loadProperties() {
		// First look for properties file in working directory.
		Properties props = new Properties();
		String workingDir = System.getProperty("user.dir");
		File pf = new File(workingDir, PROPERTIES_FILE);
		if (pf.canRead()) {
			FileInputStream fin = null;
			try {
				fin = new FileInputStream(pf);
				props.load(fin);
				fin.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fin != null) {
					try {
						fin.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {  // Then look among class files
			InputStream is = this.getClass().getResourceAsStream(PROPERTIES_FILE);
			try {
				props.load(is);
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return props;
	}
	
	/**
	 * To be removed eventually.
	 * @return Connection
	 */
	public Connection getGerritDbConnectionFromProps() {

		if (conn != null) {
			return conn; // reuse old connection
		}
		
		// Read database parameters from properties file.
		Properties props = loadProperties();
		
		// Set up new database connection
		String driver   = props.getProperty("jdbc.driver");
		String dburl    = props.getProperty("jdbc.url");
		String username = props.getProperty("jdbc.username");
		String password = props.getProperty("jdbc.password");
		basePath = props.getProperty("gerrit.basepath");
		
		if (password == null || password.isEmpty()) {
			System.out.print("Enter Gerrit database password for account <" + username + ">: ");
			Scanner scan = new Scanner(System.in);
			password = scan.nextLine();
		}
			
		try {
			// Load JDBC driver
			Class.forName(driver);
			// Get DB connection
			conn = DriverManager.getConnection(dburl, username, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (conn == null) {
			System.out.print("DRIVER <" + driver + ">");
			System.out.println(" failed to connect to database with parameters:");
			System.out.println("DATABASE_URL = " + dburl);
			System.out.println("USERNAME     = " + username);
			System.out.println("PASSWORD     = " + password);
		}
		
		return conn;
	}
	
	public Connection getGerritDbConnectionFromGerritConfig() {

		if (conn != null) {
			return conn; // reuse old connection
		}
		
		// Read database parameters from Gerrit configuration files.
		ReadGerritConfig rc = new ReadGerritConfig();
		rc.init();
		
		// Set up new database connection
		String driver   = rc.getDbDrv();
		String dburl    = rc.getDbUrl();
		String username = rc.getUsername();
		String password = rc.getPassword();
		basePath = rc.getBasePath();
		
		if (password == null || password.isEmpty()) {
			System.out.print("Enter Gerrit database password for account <" + username + ">: ");
			Scanner scan = new Scanner(System.in);
			password = scan.nextLine();
		}
			
		try {
			// Load JDBC driver
			Class.forName(driver);
			// Get DB connection
			conn = DriverManager.getConnection(dburl, username, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (conn == null) {
			System.out.print("DRIVER <" + driver + ">");
			System.out.println(" failed to connect to database with parameters:");
			System.out.println("DATABASE_URL = " + dburl);
			System.out.println("USERNAME     = " + username);
			System.out.println("PASSWORD     = " + password);
		}
		
		return conn;
	}
	
	public List<String> getGerritProjectList() {
		List<String> projectList = new ArrayList<String>();
		
		if (conn == null) {
			conn = getGerritDbConnectionFromGerritConfig();
		}

		if (conn != null) {
			// Create Statement for querying the database
			ResultSet resultSet = null;
			Statement stmt = null;
			try {
				stmt = conn.createStatement();

				// Get list of projects in Gerrit database
				String columnName = "dest_project_name";
				String sql = "SELECT DISTINCT " + columnName +
						" FROM changes ORDER BY " +	columnName + " ASC";

				resultSet = stmt.executeQuery(sql);
				while (resultSet.next()) {
					projectList.add(resultSet.getString(columnName));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					resultSet.close();
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return projectList;
	}
	
	public static String selectProject(List<String> projectList) {

		if (projectList == null || projectList.isEmpty()) {
			return null;
		}
		
		System.out.println("Gerrit Projects:");
		for (int i = 0; i < projectList.size(); i++) {
			System.out.println("[" + i + "] "+projectList.get(i));
		}
			
		// Ask user for project to be removed from Gerrit
		System.out.print("Number of project to be deleted? [-1 to quit] ");
		Scanner scan = new Scanner(System.in);
		String input = scan.nextLine();
		int num = -1;
		try {
			num = Integer.parseInt(input);
		} catch (NumberFormatException ne) {
			System.out.println("You must enter an integer between 0 and " + 
					(projectList.size()-1));
		}
		if (num == -1) {
			return null;
		}
		if (num < -1 || num > projectList.size()-1) {
			System.out.println("Invalid project number.  Exiting...");
			return null;
		}
		
		// XXX this method doesn't seem to actually remove the project, so 
		// having this message printed here is a little bit misleading.
		System.out.print("Are you positive that you want to remove project '[" + num
				+ "] " + projectList.get(num) + "'? [y/N] ");
		input = scan.nextLine();
		if (input.equalsIgnoreCase("y")) {
			return projectList.get(num);
		}
		return null;
	}
	
	// From http://www.roseindia.net/tutorialhelp/comment/81393
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
	
	public Boolean removeGitRepo(String projName) {
		File gitRepo = new File(basePath+"/"+projName);
		// First try without .git suffix
		if (gitRepo.exists() && deleteDir(gitRepo)) {
			return true;
		}
		// then try with appended .git suffix
		gitRepo = new File(basePath+"/"+projName+".git");
		if (gitRepo.exists() && deleteDir(gitRepo)) {
			return true;
		}
		return false;
	}
	
	/**
	 * This method removes references to the specified project from the Gerrit database.
	 * It should be ran after the project's Git repo is removed; otherwise any changes
	 * to the project made after this method is ran will be missed.
	 * @param projName
	 * @return
	 */
	public Boolean removeProjectFromDb(String projName) {
		
		int count = 0;
		
		String[] tablesWithChangeIdFK = {"account_patch_reviews", "change_messages",
				"patch_comments", "patch_set_ancestors", "patch_set_approvals",
				"patch_sets", "starred_changes", "tracking_ids"};
		
		Statement stmt;
		try {
			stmt = conn.createStatement();
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		}
		String sql = null;
		for (int i = 0; i < tablesWithChangeIdFK.length; i++) {
			sql = "DELETE FROM " + tablesWithChangeIdFK[i] + " WHERE change_id IN " + 
					"(SELECT change_id FROM changes WHERE dest_project_name = '" + projName + "')";
			//System.out.println(sql);
			try {
				stmt.executeUpdate(sql);
				count++;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		try {
			sql = "DELETE FROM changes WHERE dest_project_name = '" + projName + "'";
			stmt.executeUpdate(sql);
			count++;
			sql = "DELETE FROM account_project_watches WHERE project_name = '" + projName + "'";
			stmt.executeUpdate(sql);
			count++;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if (count == tablesWithChangeIdFK.length + 2) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		GerritProjectUtility gp = new GerritProjectUtility();
		
		// Get list of projects in Gerrit database
		List<String> projectList = gp.getGerritProjectList();

		// Ask user for project to be removed from Gerrit
		String projName = selectProject(projectList);
		
		if (projName == null || projName.isEmpty()) {
			System.out.println("Quitting.");
			return;
		}
		
		if (!gp.removeGitRepo(projName)) {
			System.out.println("Failed to remove repo <"+basePath+"/"+projName+">.  Exiting.");
			System.out.println("Is the utility being run from the account used by Gerrit?");
			System.out.println("If the Gerrit repo resides on an NFS mount, the filesystem\n" +
					"may be locked.  Wait a couple of minutes and try again.");
			return;
		} else {
			System.out.println("Repo removed");
		}

		if (gp.removeProjectFromDb(projName)) {
			System.out.println("Database entries removed.\nCompleted successfully");
		} else {
			System.out.println("Failed to complete successfully");
		}

	}

}
