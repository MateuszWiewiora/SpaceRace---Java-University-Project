package com.spacerace.core.track;

/** Available race tracks selectable from the setup screen. */
public final class TrackCatalog {

    public static final class Entry {
        public final String fileName;
        public final String displayName;
        public final String description;

        public Entry(String fileName, String displayName, String description) {
            this.fileName = fileName;
            this.displayName = displayName;
            this.description = description;
        }

        public String getMapPath() {
            return "maps/" + fileName;
        }
    }

    public static final Entry[] TRACKS = {
            new Entry(
                    "track_placeholder.tmx",
                    "Training Oval",
                    "Wide oval track for warm-up laps"
            ),
            new Entry(
                    "track_space.tmx",
                    "Space Loop",
                    "Fast stadium with long straights"
            ),
            new Entry(
                    "track_switchback.tmx",
                    "Switchback Canyon",
                    "Tight corners and sudden direction changes"
            ),
            new Entry(
                    "track_figure_eight.tmx",
                    "Figure Eight",
                    "Two loops crossing in the center"
            ),
    };

    private TrackCatalog() {}
}
