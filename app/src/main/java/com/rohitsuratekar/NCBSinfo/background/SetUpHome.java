package com.rohitsuratekar.NCBSinfo.background;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;

import com.rohitsuratekar.NCBSinfo.activities.home.HomeObject;
import com.rohitsuratekar.NCBSinfo.database.AppData;
import com.rohitsuratekar.NCBSinfo.database.RouteData;
import com.rohitsuratekar.NCBSinfo.database.TripData;

import java.util.List;

/**
 * Created by Rohit Suratekar on 13-10-17 for NCBSinfo.
 * All code is released under MIT License.
 */

public class SetUpHome extends AsyncTask<Void, Void, Void> {

    private OnLoad onLoad;
    private AppData db;
    private HomeObject object;

    public SetUpHome(Context context, OnLoad onLoad) {
        this.onLoad = onLoad;
        this.db = AppData.getDatabase(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        List<RouteData> routeData = db.routes().getRouteNames();
        SparseArray<RouteData> routes = new SparseArray<>();
        SparseArray<List<TripData>> trips = new SparseArray<>();
        for (RouteData r : routeData) {
            routes.put(r.getRouteID(), r);
            trips.put(r.getRouteID(), db.trips().getTripsByRoute(r.getRouteID()));
        }
        if (routes.size() > 0) {
            this.object = new HomeObject(routes, trips);
        } else {
            this.object = null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        this.onLoad.loaded(this.object);
    }

    public interface OnLoad {
        void loaded(HomeObject homeObject);
    }
}