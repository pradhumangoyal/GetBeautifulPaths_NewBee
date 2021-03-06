package com.example.getallpossiblepaths;

import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getallpossiblepaths.Modules.DirectionFinder;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;


import com.example.getallpossiblepaths.Modules.DirectionFinder;
import com.example.getallpossiblepaths.Modules.DirectionFinderListener;
import com.example.getallpossiblepaths.Modules.Route;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener {

    private GoogleMap mMap;
    private FloatingActionButton btnFindPath;
    private EditText etOrigin;
    private EditText etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private String origin = "";
    private String dest = "";
    private int mMode = 0;
    private List<Route> routes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        setContentView(R.layout.activity_maps);
        Places.initialize(getApplicationContext(), "AIzaSyC8glAUHZbPM1gzikYcGm-wQIX3PS6MMkU");
        PlacesClient placesClient = Places.createClient(this);

        btnFindPath = (FloatingActionButton) findViewById(R.id.btnFindPath);
        etOrigin = (EditText) findViewById(R.id.etOrigin);
        etDestination = (EditText) findViewById(R.id.etDestination);

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteFragment.setHint("Enter start location");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
               origin = "" + place.getId();
                Log.i("Fragment", "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Fragment", "An error occurred: " + status);
            }
        });



        AutocompleteSupportFragment autocompleteFragment2 = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment2);
        autocompleteFragment2.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteFragment2.setHint("Enter destination");
        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
               dest = ""+ place.getId();
                Log.i("Fragment", "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Fragment", "An error occurred: " + status);
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });

        final FloatingActionButton mode0 = (FloatingActionButton) findViewById(R.id.mode0);
        final FloatingActionButton mode1 = (FloatingActionButton) findViewById(R.id.mode1);
        final FloatingActionButton mode2 = (FloatingActionButton) findViewById(R.id.mode2);

        mode0.setImageResource(R.drawable.baseline_access_time_black_18dp); // ETA
        mode1.setImageResource(R.drawable.baseline_height_white_18dp); // Distance
        mode2.setImageResource(R.drawable.baseline_favorite_white_18dp); // AQI

        mode0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMode = 0;
                mode0.setImageResource(R.drawable.baseline_access_time_black_18dp);
                mode1.setImageResource(R.drawable.baseline_height_white_18dp);
                mode2.setImageResource(R.drawable.baseline_favorite_white_18dp);
               // Toast.makeText(MapsActivity.this, "ETA selected", Toast.LENGTH_LONG).show();
            }
        });

        mode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMode = 1;
                mode0.setImageResource(R.drawable.baseline_access_time_white_18dp);
                mode1.setImageResource(R.drawable.baseline_height_black_18dp);
                mode2.setImageResource(R.drawable.baseline_favorite_white_18dp);
               // Toast.makeText(MapsActivity.this, "Distance selected", Toast.LENGTH_LONG).show();

            }
        });

        mode2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMode = 2;
                mode0.setImageResource(R.drawable.baseline_access_time_white_18dp);
                mode1.setImageResource(R.drawable.baseline_height_white_18dp);
                mode2.setImageResource(R.drawable.baseline_favorite_black_18dp);
               // Toast.makeText(MapsActivity.this, "AQI selected", Toast.LENGTH_LONG).show();

            }
        });


    }

    private void sendRequest() {
        String origin = this.origin;
        String destination = this.dest;
        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination, mMode).execute(); //mode
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
    }


    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding directions..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();
        Random rnd = new Random();
        this.routes = routes;

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            float sum = 0;
            for(int i: route.aqi) {
                sum = sum + i;
            }
            int avg = Math.round(sum / route.aqi.size());

            int color;

            switch(avg){
                case 0:
                    color = Color.GREEN;
                    break;
                case 1:
                    color = Color.YELLOW;
                    break;
                case 2:
                    color = getResources().getColor(R.color.orange);
                    break;
                case 3:
                    color = Color.RED;
                    break;
                case 4:
                    color = getResources().getColor(R.color.purple);
                    break;
                case 5:
                    color = getResources().getColor(R.color.maroon);
                    break;
                default:
                    color = Color.GRAY;
                    break;
            }


            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).color(color).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
        showBottomSheetDialogFragment();

    }
    public void showBottomSheetDialog() {
        View view = getLayoutInflater().inflate(R.layout.fragment_bottom_sheet_dialog, null);

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.show();
    }

    public void showBottomSheetDialogFragment() {
        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(this.routes);
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    static class CustomArrayAdapter extends ArrayAdapter<Route> {

        private Context context;
        private List<Route> route;

        //constructor, call on creation
        public CustomArrayAdapter(Context context, int resource, ArrayList<Route> objects) {
            super(context, resource, objects);

            this.context = context;
            this.route = objects;
        }

        //called when rendering the list
        public View getView(int position, View convertView, ViewGroup parent) {

            //get the property we are displaying
            Route property = route.get(position);

            //get the inflater and inflate the XML layout for each item
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.list_view_routes, null);
            TextView display_time = (TextView) view.findViewById(R.id.display_time);
            TextView eta = (TextView) view.findViewById(R.id.eta);
            TextView distance = (TextView) view.findViewById(R.id.distance);
            TextView aqi = (TextView) view.findViewById(R.id.aqiLevel);
            CircleImageView aqi_image = (CircleImageView) view.findViewById(R.id.aqi_image);

            float sum = 0;
            for(int i: property.aqi) {
                sum = sum + i;
            }
            int avg = Math.round(sum / property.aqi.size());
            String text = "";
            int color;

            switch(avg){
                case 0:
                    color = Color.GREEN;
                    text = "Good";
                    break;
                case 1:
                    color = Color.YELLOW;
                    text = "Satisfactory";
                    break;
                case 2:
                    color = getContext().getResources().getColor(R.color.orange);
                    text = "Moderately Polluted";
                    break;
                case 3:
                    color = Color.RED;
                    text = "Poor";
                    break;
                case 4:
                    color = getContext().getResources().getColor(R.color.purple);
                    text = "Very Poor";
                    break;
                case 5:
                    color = getContext().getResources().getColor(R.color.maroon);
                    text = "Severe";
                    break;
                default:
                    color = Color.GRAY;
                    break;
            }

            aqi_image.setCircleBackgroundColor(color);
            aqi.setText(String.valueOf(text));


            distance.setText(String.valueOf(property.distance.text));
            String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
            Calendar now = Calendar.getInstance();

            now.add(Calendar.SECOND, property.duration.value);
            String destTime = new SimpleDateFormat("HH:mm").format(now.getTime());
            eta.setText(String.valueOf(String.format("%.1f", property.duration.value / 60.0) + " min"));
            display_time.setText(currentTime + " - " + destTime);

            return view;
        }
    }
}

