package com.poepoemyintswe.specialtips.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.poepoemyintswe.specialtips.Config;
import com.poepoemyintswe.specialtips.R;
import com.poepoemyintswe.specialtips.adapter.FeedAdapter;
import com.poepoemyintswe.specialtips.models.Feed;
import com.poepoemyintswe.specialtips.service.GetFeed;
import com.poepoemyintswe.specialtips.util.NetworkConnectivityCheck;
import java.util.ArrayList;
import java.util.List;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by poepoe on 10/21/14.
 */
public class FeedFragment extends Fragment {

  @InjectView(R.id.feed) ListView feedListView;
  @InjectView(R.id.swipe_to_refresh_list) SwipeRefreshLayout swipeRefreshLayout;

  private final String TAG = FeedFragment.class.getSimpleName();

  private ArrayList<Feed> feedItems;

  public FeedFragment() {

  }

  @Override
  public void onStart() {
    super.onStart();
    loadTips();
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_feed, container, false);

    ButterKnife.inject(this, rootView);
    swipeRefreshLayout.setColorSchemeResources(R.color.swipe_refresh_color1,
        R.color.swipe_refresh_color2, R.color.swipe_refresh_color3, R.color.swipe_refresh_color4);

    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        loadTips();
      }
    });

    return rootView;
  }

  public void StartRefreshing() {
    if (swipeRefreshLayout != null) {
      swipeRefreshLayout.setRefreshing(true);
    }
  }

  public void StopRefreshing() {
    if (swipeRefreshLayout != null) {
      swipeRefreshLayout.setRefreshing(false);
    }
  }

  public void loadTips() {
    if (NetworkConnectivityCheck.getInstance(getActivity()).isConnected()) {
      feedItems = new ArrayList<Feed>();
      StartRefreshing();

      RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(Config.BASE_URL)
          .setLogLevel(RestAdapter.LogLevel.BASIC)
          .build();

      GetFeed getFeed = restAdapter.create(GetFeed.class);

      getFeed.getTips(new Callback<List<Feed>>() {
        @Override
        public void success(List<Feed> feeds, Response response) {
          feedItems = (ArrayList<Feed>) feeds;
          Log.d(TAG, feeds.size() + "");
          FeedAdapter adapter = new FeedAdapter(getActivity(), feedItems);
          feedListView.setAdapter(adapter);
          StopRefreshing();
        }

        @Override
        public void failure(RetrofitError error) {
          Log.d(TAG, error.toString());
          Toast.makeText(getActivity(), "Please Try Again....", Toast.LENGTH_SHORT).show();
          StopRefreshing();

          ReplaceCurrentFragment();
        }
      });
    } else {
      Toast.makeText(getActivity(), "Please Check Your Connection....", Toast.LENGTH_SHORT).show();
      ReplaceCurrentFragment();
    }
  }

  public void ReplaceCurrentFragment() {
    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
    SwipeFragment fragment = new SwipeFragment();
    fragmentTransaction.addToBackStack(null);
    fragmentTransaction.hide(FeedFragment.this);
    fragmentTransaction.add(R.id.container, fragment).commit();
  }
}
