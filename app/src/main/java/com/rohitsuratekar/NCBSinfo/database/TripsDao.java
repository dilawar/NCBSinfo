package com.rohitsuratekar.NCBSinfo.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by Rohit Suratekar on 20-06-17 for NCBSinfo.
 * All code is released under MIT License.
 */

@Dao
public interface TripsDao {

    @Insert
    void insertTrips(TripData tripData);

    @Query("SELECT tripID FROM trips " +
            "WHERE routeID LIKE :routeID AND " +
            "day LIKE :day ")
    int getTripID(int routeID, int day);

    @Query("SELECT * FROM trips " +
            "WHERE routeID LIKE :routeID "
    )
    List<TripData> getTripsByRoute(int routeID);

    @Query("SELECT * FROM trips WHERE tripID LIKE :tripID")
    TripData getTrip(int tripID);
}