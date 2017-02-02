package client;

/* THIS MUST BE IDENTICAL TO ITS COUNTERPART IN THE CLIENT. IF YOU CHANGE ONE, CHANGE BOTH */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * We make a data structure specifically for each entry we decide to
 * return, formatted like the database contents themselves.
 *
 * (In the future, we might have harmonized_code instead of directly
 * returning fixed_code.)
 */
public class DatabaseEntry {
	private int id;
	//private int error_type;
	private String buggy_code;
	private String buggy_code_assignments;
	private String fixed_code;
	private String fixed_code_assignments;
	private double similarity;

	public DatabaseEntry() {}

	public DatabaseEntry(int id,  String buggy_code, String
			buggy_code_assignments, String fixed_code, String fixed_code_assignments) {

		this.id = id;
		//this.error_type = error_type;
		this.buggy_code = buggy_code;
		this.buggy_code_assignments = buggy_code_assignments;
		this.fixed_code = fixed_code;
		this.fixed_code_assignments = fixed_code_assignments;
	}

	public DatabaseEntry(ResultSet source) {
		// TODO see how this handles exceptions
		// TODO update this to handle a real entry
		try{
			if(!source.isAfterLast()&&!source.isBeforeFirst()) {
				this.id = source.getInt("id");
				//this.error_type = source.getInt("error_type");
				this.buggy_code = source.getString("buggy_code");
				this.buggy_code_assignments = source.getString("buggy_code_assignments");
				this.fixed_code = source.getString("fixed_code");
				this.fixed_code_assignments = source.getString("fixed_code_assignments");
//				this.unEscape();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int getId() { return this.id; }
	//public int getErrorType() { return this.error_type; }
	public String getBuggyCode() { return this.buggy_code; }
	public String getBuggyCodeAssignments() { return this.buggy_code_assignments; }
	public String getFixedCode() { return this.fixed_code; }
	public String getFixedCodeAssignments() { return this.fixed_code_assignments; }
	public double getSimilarity() { return this.similarity; }

	//Classes for ease of ascii interface use
	public List<Integer> getBuggyCodeAsList() {
		// return DBAscii.toIntegerListFromAscii(this.buggy_code);
		return null;
	}
	public List<Integer> getBuggyAssignmentsAsList() {
		// return DBAscii.toIntegerListFromAscii(this.buggy_code_assignments);
		return null;
	}
	public List<Integer> getFixedCodeAsList() {
		// return DBAscii.toIntegerListFromAscii(this.fixed_code);
		return null;
	}
	public List<Integer> getFixedAssignmentsAsList() {
		// return DBAscii.toIntegerListFromAscii(this.fixed_code_assignments);
		return null;
	}


	public void setId(int id) { this.id = id; }
	//public void setErrorType(int error_type) { this.error_type = error_type;	}
	public void setBuggyCode(String buggy_code) { this.buggy_code = buggy_code;	}
	public void setBuggyCodeAssignments(String assignment) { this.buggy_code_assignments =
			assignment;}
	public void setFixedCode(String fixed_code) { this.fixed_code = fixed_code;	}
	public void setFixedCodeAssignments(String assignment) { this.fixed_code_assignments =
			assignment;}
	public void setSimilarity(double v) { this.similarity = v;}

	public String toString() {
		return "(" + id + " | " +
				buggy_code + " | " +
				buggy_code_assignments + " | " +
				fixed_code + " | " +
				buggy_code_assignments  + ")";
	}

	public String toStringVerbose() {
		return toString();
		// return "(" + id + " | " +
		// 		DBAscii.toIntegerListFromAscii(buggy_code)+ " | " +
		// 		DBAscii.toIntegerListFromAscii(buggy_code_assignments)+ " | " +
		// 		DBAscii.toIntegerListFromAscii(fixed_code)+ " | " +
		// 		DBAscii.toIntegerListFromAscii(fixed_code_assignments)+ ")";
	}


	// Method that escapes dangerous ascii characters from ascii encoded fields for sql transmission
	public DatabaseEntry escape() {
		// StringBuilder b = new StringBuilder();
		// for(int i = 0; i < buggy_code.length(); i++) {
		// 	if (DatabaseServer.ESCAPE_CHARACTERS.containsKey(buggy_code.charAt(i))) {
		// 		b.append("\\" + DatabaseServer.ESCAPE_CHARACTERS.get(buggy_code.charAt(i)));
		// 	} else {
		// 		b.append(buggy_code.charAt(i));
		// 	}
		// }
		// buggy_code = b.toString();
		// b = new StringBuilder();
		//
		// for(int i = 0; i < buggy_code_assignments.length(); i++) {
		// 	if (DatabaseServer.ESCAPE_CHARACTERS.containsKey(buggy_code_assignments.charAt(i))) {
		// 		b.append("\\" + DatabaseServer.ESCAPE_CHARACTERS.get(buggy_code_assignments
		// 				.charAt(i)));
		// 	} else {
		// 		b.append(buggy_code_assignments.charAt(i));
		// 	}
		// }
		// buggy_code_assignments = b.toString();
		// b = new StringBuilder();
		//
		// for(int i = 0; i < fixed_code_assignments.length(); i++) {
		// 	if (DatabaseServer.ESCAPE_CHARACTERS.containsKey(fixed_code_assignments.charAt(i))) {
		// 		b.append("\\" + DatabaseServer.ESCAPE_CHARACTERS.get(fixed_code_assignments
		// 				.charAt(i)));
		// 	} else {
		// 		b.append(fixed_code_assignments.charAt(i));
		// 	}
		// }
		// fixed_code_assignments = b.toString();
		// b = new StringBuilder();
		//
		// for(int i = 0; i < fixed_code.length(); i++) {
		// 	if (DatabaseServer.ESCAPE_CHARACTERS.containsKey(fixed_code.charAt(i))) {
		// 		b.append("\\"+ DatabaseServer.ESCAPE_CHARACTERS.get(fixed_code.charAt(i)));
		// 	} else {
		// 		b.append(fixed_code.charAt(i));
		// 	}
		// }
		// fixed_code = b.toString();
		return this;
	}

	// Method that undoes our escape characters
	public DatabaseEntry unEscape() {
		// buggy_code = unescapeString(buggy_code);
		// buggy_code_assignments = unescapeString(buggy_code_assignments);
		// fixed_code_assignments = unescapeString(fixed_code_assignments);
		// fixed_code = unescapeString(fixed_code);
		return this;
	}

	// Logic that unescapes a single string and returns its form
	public static String unescapeString(String s) {
		// StringBuilder b = new StringBuilder();
		//
		// for(int i = 0; i < s.length(); i++) {
		// 	if ('\\'==s.charAt(i)) {
		// 		i++; //TODO check pathological case
		// 		b.append(DatabaseServer.SRETCARAHC_EPASCE.get(s.charAt(i)));
		// 	} else {
		// 		b.append(s.charAt(i));
		// 	}
		// }
		// return b.toString();
		return "";
	}
}
