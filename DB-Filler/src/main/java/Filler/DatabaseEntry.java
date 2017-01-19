package Filler;

/* THIS MUST BE IDENTICAL TO ITS COUNTERPART IN THE CLIENT. IF YOU CHANGE ONE, CHANGE BOTH */

import Filler.Tokenizer.DBAscii;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
	private int count;
	private double similarity;

	public DatabaseEntry() {}

	public DatabaseEntry(int id, int error_type, String buggy_code, String
			buggy_code_assignments, String fixed_code, String fixed_code_assignments, int count) {
		this.id = id;
		//this.error_type = error_type;
		this.buggy_code = buggy_code;
		this.buggy_code_assignments = buggy_code_assignments;
		this.fixed_code = fixed_code;
		this.fixed_code_assignments = fixed_code_assignments;
		this.count = count;
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
				this.fixed_code_assignments = source.getString("fixed_code_assignmetns");
				this.count = source.getInt("count");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int getId() { return this.id; }
	//public int getErrorType() { return this.error_type; }
	public String getBuggyCode() { return this.buggy_code; }
	public String getFixedCode() { return this.fixed_code; }
	public int getCount() { return this.count; }
	public double getSimilarity() { return this.similarity; }

	//Classes for ease of ascii interface use
	public List<Integer> getBuggyCodeAsList() {
		return DBAscii.toIntegerListFromAscii(this.buggy_code);
	}
	public List<Integer> getBuggyAssignmentsAsList() {
		return DBAscii.toIntegerListFromAscii(this.buggy_code_assignments);
	}
	public List<Integer> getFixedCodeAsList() {
		return DBAscii.toIntegerListFromAscii(this.fixed_code);
	}
	public List<Integer> getFixedAssignmentsAsList() {
		return DBAscii.toIntegerListFromAscii(this.fixed_code_assignments);
	}


	public void setId(int id) { this.id = id; }
	//public void setErrorType(int error_type) { this.error_type = error_type;	}
	public void setBuggyCode(String buggy_code) { this.buggy_code = buggy_code;	}
	public void setFixedCode(String fixed_code) { this.fixed_code = fixed_code;	}
	public void setCount(int count) { this.count = count; }
	public void setSimilarity(double v) { this.similarity = v;}

	public String toString() {
		return "(" + id + " | " +
				buggy_code + " | " +
				buggy_code_assignments + " | " +
				fixed_code + " | " +
				buggy_code_assignments + " | " +
				count + ")";
	}
}
