package com.example.mysecondclasshib.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mysecondclasshib.fragments.FriendsListFragment;
import com.example.mysecondclasshib.fragments.FriendRequestsFragment;

public class FriendsViewPagerAdapter extends FragmentStateAdapter {

    public FriendsViewPagerAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? new FriendsListFragment() : new FriendRequestsFragment();
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs: Friends and Requests
    }
}