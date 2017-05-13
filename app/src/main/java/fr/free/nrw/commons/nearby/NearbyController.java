package fr.free.nrw.commons.nearby;

import static fr.free.nrw.commons.utils.LengthUtils.computeDistanceBetween;
import static fr.free.nrw.commons.utils.LengthUtils.formatDistanceBetween;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mapbox.mapboxsdk.annotations.BaseMarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;

import fr.free.nrw.commons.location.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;


public class NearbyController {
    private static final int MAX_RESULTS = 1000;

    private static List<Place> loadAttractionsFromLocation(LatLng curLatLng, Context context) {
        Timber.d("Loading attractions near %s", curLatLng);
        if (curLatLng == null) {
            return Collections.emptyList();
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        List<Place> places = prefs.getBoolean("useWikidata", true)
                ? NearbyPlaces.getInstance().getFromWikidataQuery(
                curLatLng, Locale.getDefault().getLanguage())
                : NearbyPlaces.getInstance().getFromWikiNeedsPictures();
        if (curLatLng != null) {
            Timber.d("Sorting places by distance...");
            final Map<Place, Double> distances = new HashMap<>();
            for (Place place: places) {
                distances.put(place, computeDistanceBetween(place.location, curLatLng));
            }
            Collections.sort(places,
                    new Comparator<Place>() {
                        @Override
                        public int compare(Place lhs, Place rhs) {
                            double lhsDistance = distances.get(lhs);
                            double rhsDistance = distances.get(rhs);
                            return (int) (lhsDistance - rhsDistance);
                        }
                    }
            );
        }
        return places;
    }

    /**
     * Loads attractions from location for list view, we need to return Place data type.
     * @param curLatLng users current location
     * @param context current activity
     * @return Place list that holds nearby places
     */

    public static List<Place> loadAttractionsFromLocationToPlaces(LatLng curLatLng,
                                                                  Context context) {

        List<Place> places = loadAttractionsFromLocation(curLatLng,context);
        places = places.subList(0, Math.min(places.size(), MAX_RESULTS));
        for (Place place: places) {
            String distance = formatDistanceBetween(curLatLng, place.location);
            place.setDistance(distance);
        }
        return places;
    }

    /**
     *Loads attractions from location for map view, we need to return BaseMarkerOption data type.
     * @param curLatLng users current location
     * @param context the activity
     * @return BaseMarkerOprions list that holds nearby places
     */
    public static List<BaseMarkerOptions> loadAttractionsFromLocationToBaseMarkerOptions(
            LatLng curLatLng,
            Context context) {
        List<BaseMarkerOptions> baseMarkerOptionses = new ArrayList<>();
        List<Place> places = loadAttractionsFromLocation(curLatLng,context);
        places = places.subList(0, Math.min(places.size(), MAX_RESULTS));
        for (Place place: places) {
            String distance = formatDistanceBetween(curLatLng, place.location);
            place.setDistance(distance);
            baseMarkerOptionses.add(new MarkerOptions()
                    .position(new com.mapbox.mapboxsdk.geometry
                    .LatLng(place.location.latitude,place.location.longitude))
                    .title(place.name)
                    .snippet(place.description));
        }
        return baseMarkerOptionses;
    }
}