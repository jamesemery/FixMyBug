package server;

import java.util.Collections;
import java.util.List;

public class DatabaseEntryListWrapper {
    private List<DatabaseEntry> entryList;

    public DatabaseEntryListWrapper() {}

    public DatabaseEntryListWrapper(List<DatabaseEntry> list) {
        entryList = list;
//        for (DatabaseEntry e : list) {
//            e.convertToCleanString();
//        }//TODO add
    }
    public DatabaseEntryListWrapper(DatabaseEntry entry) {
        entryList = Collections.singletonList(entry);
    }

    public List<DatabaseEntry> getEntryList(){
        return entryList;
    }
}
