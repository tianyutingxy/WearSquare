package cz.destil.wearsquare.activity;

import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.List;

import cz.destil.wearsquare.R;
import cz.destil.wearsquare.adapter.CheckInListAdapter;
import cz.destil.wearsquare.event.CheckInVenueListEvent;
import cz.destil.wearsquare.event.ErrorEvent;
import cz.destil.wearsquare.event.ExitEvent;
import cz.destil.wearsquare.event.ImageLoadedEvent;

/**
 * Displays a list of nearby venues suitable for a check-in.
 *
 * @author David Vávra (david@vavra.me)
 */
public class CheckInListActivity extends ProgressActivity {

    WearableListView vList;
    private List<CheckInListAdapter.Venue> mVenues;
    private CheckInListAdapter mAdapter;

    @Override
    int getMainViewResourceId() {
        return R.layout.activity_fragment_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        finishOtherActivities();
        super.onCreate(savedInstanceState);
        vList = (WearableListView) getMainView().findViewById(R.id.list);
    }

    @Override
    public void startConnected() {
        super.startConnected();
        if (mVenues == null) {
            teleport().sendMessage("/check-in-list", null);
            showProgress();
        }
    }

    @Subscribe
    public void onVenueList(CheckInVenueListEvent event) {
        hideProgress();
        mVenues = event.getVenues();
        mAdapter = new CheckInListAdapter(CheckInListActivity.this, mVenues);
        vList.setAdapter(mAdapter);
        vList.setClickListener(new WearableListView.ClickListener() {
                                   @Override
                                   public void onClick(WearableListView.ViewHolder viewHolder) {
                                       String id = (String) viewHolder.itemView.getTag();
                                       String name = ((TextView) viewHolder.itemView.findViewById(R.id.text)).getText
                                               ().toString();
                                       CheckInActivity.call(CheckInListActivity.this, id, name);
                                   }

                                   @Override
                                   public void onTopEmptyRegionClick() {

                                   }
                               }
        );
    }

    @Subscribe
    public void onError(ErrorEvent event) {
        showError(event.getMessage());
    }

    @Subscribe
    public void onExit(ExitEvent event) {
        finish();
    }

    @Subscribe
    public void onImageDownloaded(ImageLoadedEvent event) {
        if (mVenues != null) {
            for (CheckInListAdapter.Venue venue : mVenues) {
                if (venue.getImageUrl() != null && venue.getImageUrl().equals(event.getImageUrl())) {
                    venue.setIcon(event.getBitmap());
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}