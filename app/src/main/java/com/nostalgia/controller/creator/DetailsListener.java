package com.nostalgia.controller.creator;

/**
 * Created by Aidan on 4/22/16.
 */
public interface DetailsListener {
    enum VisibilityLevel {
        ALL, SHARED, PERSONAL
    }
    enum DetailNames {
        DESCRIPTION
    }

    void onNameChange(String name);
    void onVisibilityChanged(VisibilityLevel visibility);
    void onGeneralChange(String param, String value);
}
