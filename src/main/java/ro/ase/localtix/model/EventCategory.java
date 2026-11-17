package ro.ase.localtix.model;

public enum EventCategory {
    CONCERT("Concert", "lt-badge-concert"),
    STANDUP("Stand-Up", "lt-badge-standup"),
    THEATRE("Theatre", "lt-badge-theatre");

    private final String displayName;
    private final String badgeClass;

    EventCategory(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() { return displayName; }
    public String getBadgeClass() { return badgeClass; }
}
