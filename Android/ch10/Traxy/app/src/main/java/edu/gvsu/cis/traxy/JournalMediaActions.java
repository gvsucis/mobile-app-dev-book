package edu.gvsu.cis.traxy;

import edu.gvsu.cis.traxy.model.JournalEntry;

public interface JournalMediaActions {
    void editAction(JournalEntry e, String key);
    void viewAction(JournalEntry e);
}
