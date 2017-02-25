package com.nostalgia.controller.creator;

import com.nostalgia.persistence.model.User;
import java.util.ArrayList;

/**
 * Created by Aidan on 4/22/16.
 */
public interface SharingListener {
    void onManyAdded(ArrayList<User> users);
    void onManyRemoved(ArrayList<User> users);
    void onPersonRemoved(User user);
    void onPersonAdded(User user);
}
