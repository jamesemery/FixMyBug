package client;

/* THIS MUST BE IDENTICAL TO ITS COUNTERPART IN THE SERVER. IF YOU CHANGE ONE, CHANGE BOTH */

/**
 * We make a data structure specifically for each entry we decide to
 * return, formatted like the database contents themselves.
 * 
 * (In the future, we might have harmonized_code instead of directly
 * returning fixed_code.)
 */
public class DatabaseEntry {
	private int id;
	private int error_type;
	private String buggy_code;
	private String fixed_code;
	private int count;

	public DatabaseEntry() {}

	public DatabaseEntry(int id, int error_type, String buggy_code, String fixed_code, int count) {
		this.id = id;
		this.error_type = error_type;
		this.buggy_code = buggy_code;
		this.fixed_code = fixed_code;
		this.count = count;
	}

	public int getId() { return this.id; }
	public int getErrorType() { return this.error_type; }
	public String getBuggyCode() { return this.buggy_code; }
	public String getFixedCode() { return this.fixed_code; }
	public int getCount() { return this.count; }

	public void setId(int id) { this.id = id; }
	public void setErrorType(int error_type) { this.error_type = error_type;	}
	public void setBuggyCode(String buggy_code) { this.buggy_code = buggy_code;	}
	public void setFixedCode(String fixed_code) { this.fixed_code = fixed_code;	}
	public void setCount(int count) { this.count = count; }

    public String toString() {
        return "(" + id + " | " + 
                     error_type + " | " + 
                     buggy_code + " | " + 
                     fixed_code + " | " + 
                     count + ")";
    }
}
