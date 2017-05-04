package com.rohitsuratekar.NCBSinfo.activities.transport;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rohitsuratekar.NCBSinfo.R;
import com.rohitsuratekar.NCBSinfo.activities.transport.models.Route;
import com.rohitsuratekar.NCBSinfo.common.AppState;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Dexter for NCBSinfo .
 * Code is released under MIT license
 */

public class TransportFragment extends Fragment {

    private static final String ROUTE = "route";
    private AppState state = AppState.getInstance();

    public static TransportFragment newInstance(int route) {
        TransportFragment myFragment = new TransportFragment();
        Bundle args = new Bundle();
        args.putInt(ROUTE, route);
        myFragment.setArguments(args);
        return myFragment;
    }

    @BindView(R.id.tp_left_list)
    RecyclerView leftList;

    private Route currentRoute;
    private TransportAdapter adaper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.transport_fragment, container, false);
        ButterKnife.bind(this, rootView);
        currentRoute = state.getRouteList().get(getArguments().getInt(ROUTE));

        List<String> tripList = currentRoute.getUi().getLeftList();
        adaper = new TransportAdapter(tripList);
        leftList.setLayoutManager(new LinearLayoutManager(getContext()));
        leftList.setAdapter(adaper);

        return rootView;
    }
}
