package client;

import java.util.Collections;
import java.util.List;

public class DatabaseEntryListWrapper {
    private List<DatabaseEntry> entryList;

    public DatabaseEntryListWrapper(List<DatabaseEntry> list) {
        entryList = list;
    }
    public DatabaseEntryListWrapper(DatabaseEntry entry) {
        entryList = Collections.singletonList(entry);
    }

    public List<DatabaseEntry> getEntryList(){
        return entryList;
    }
}
