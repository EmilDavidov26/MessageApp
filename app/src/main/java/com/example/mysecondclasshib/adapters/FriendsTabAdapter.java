package com.example.mysecondclasshib.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FriendsTabAdapter extends FragmentStateAdapter {
    private final Fragment friendsListFragment;
    private final Fragment friendRequestsFragment;

    public FriendsTabAdapter(@NonNull Fragment fragment,
                             Fragment friendsListFragment,
                             Fragment friendRequestsFragment) {
        super(fragment);
        this.friendsListFragment = friendsListFragment;
        this.friendRequestsFragment = friendRequestsFragment;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? friendsListFragment : friendRequestsFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}