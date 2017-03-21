package cis.gvsu.edu.traxy.dummy;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<DummyJournal> ITEMS = new ArrayList<DummyJournal>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, DummyJournal> ITEM_MAP = new HashMap<String, DummyJournal>();

    private static final int COUNT = 7;

//    static {
//        // Add some sample items.
//        for (int i = 1; i <= COUNT; i++) {
//            addItem(createDummyItem(i));
//        }
//    }

    static {
        DateTime today = DateTime.now();
        DummyJournal dj;

        dj = new DummyJournal("Winter Skiing", "Denver, CO", today.minusMonths(3));
        ITEMS.add(dj);
        dj = new DummyJournal("Spring Break", "Orlando, FL", today.minusMonths(7));
        ITEMS.add(dj);
        dj = new DummyJournal("Spring Break", "Orlando, FL", today.minusMonths(7));
        ITEMS.add(dj);
        dj = new DummyJournal("Winter Skiing", "Denver, CO", today.plusMonths(3));
        ITEMS.add(dj);
        dj = new DummyJournal("Spring Break", "Orlando, FL", today.plusMonths(7));
        ITEMS.add(dj);
        dj = new DummyJournal("Spring Break", "Orlando, FL", today.plusMonths(7));
        ITEMS.add(dj);
        dj = new DummyJournal("Right Now Boondoggle", "Waco, TX", today);
        ITEMS.add(dj);
    }


//    private static void addItem(DummyJournal item) {
//        ITEMS.add(item);
//        ITEM_MAP.put(item.id, item);
//    }
//
//    private static DummyJournal createDummyItem(int position) {
//        return new DummyJournal(String.valueOf(position), "Item " + position, makeDetails(position));
//        )
//    }
//
//    private static String makeDetails(int position) {
//        StringBuilder builder = new StringBuilder();
//        builder.append("Details about Item: ").append(position);
//        for (int i = 0; i < position; i++) {
//            builder.append("\nMore details information here.");
//        }
//        return builder.toString();
//    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyJournal {
        public final String name;
        public final String location;
        public final DateTime startDate;
        public final DateTime endDate;

        public DummyJournal(String name, String location, DateTime startDate) {
            this.name = name;
            this.location = location;
            this.startDate = startDate;
            this.endDate = startDate.plusDays(7);
        }
        @Override
        public String toString() {
            return name + ", " + location;
        }
    }
}
