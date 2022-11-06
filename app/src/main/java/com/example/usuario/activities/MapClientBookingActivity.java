package com.example.usuario.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usuario.R;
import com.example.usuario.models.Chat;
import com.example.usuario.providers.AuthProvider;
import com.example.usuario.providers.ClientBookingProvider;
import com.example.usuario.providers.ClientProvider;
import com.example.usuario.providers.ConductorProvider;
import com.example.usuario.providers.GeoFireProvider;
import com.example.usuario.providers.GoogleApiProvider;
import com.example.usuario.providers.TokenProvider;
import com.example.usuario.utils.AppBackgroundHelper;
import com.example.usuario.utils.CarMoveAnim;
import com.example.usuario.utils.DecodePoints;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapClientBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private AuthProvider mAuthProvider;
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private FusedLocationProviderClient mFusedLocation;
    private GeoFireProvider mGeofireProvider;
    private ClientBookingProvider mClientBookingProvider;
    private ConductorProvider mDriverProvider;
    private ImageView mImageViewBooking;
    private ClientProvider mClientProvider;



    private Marker mMarkerDriver;

    private boolean mIsFirstTime = true;

    private PlacesClient mPlaces;

    double lat, lng;

    private TokenProvider mTokenProvider;

    private String mOrigin;
    private LatLng mOriginLatLng;

  //  private String mDestination;
    //private LatLng mDestinationLatLng;
    private LatLng mDriverLatLng;

    private TextView mTextViewClientBooking;
    private TextView mTextViewEmailClientBooking;
    private TextView mTextViewOriginClientBooking;
    //private TextView mTextViewDestinationClientBooking;
    private TextView mTextViewStatusBooking;
    private CardView mCardviewChat;

    private GoogleApiProvider mGoogleApiProvider;
    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private ValueEventListener mListener;
    private String mIdDriver;

    private ValueEventListener mListenerStatus;

    SharedPreferences mPref;
    SharedPreferences.Editor mEditor;

    LatLng mStartLatLng;
    LatLng mEndLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client_booking);
        mGeofireProvider = new GeoFireProvider("drivers_working");
        mAuthProvider = new AuthProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mGoogleApiProvider = new GoogleApiProvider(MapClientBookingActivity.this);
        mDriverProvider = new ConductorProvider();
        mClientProvider = new ClientProvider();
        mTokenProvider = new TokenProvider();


        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mTextViewClientBooking = findViewById(R.id.textViewDriverBooking);
        mTextViewEmailClientBooking = findViewById(R.id.textViewEmailDriverBooking);
        mTextViewStatusBooking = findViewById(R.id.textViewStatusBooking);
        mTextViewOriginClientBooking = findViewById(R.id.textViewOriginDriverBooking);
       // mTextViewDestinationClientBooking = findViewById(R.id.textViewDestinationDriverBooking);
        mImageViewBooking = findViewById(R.id.imageViewClientBooking);
        mCardviewChat = findViewById(R.id.cardviewChat);

        mPref = getApplicationContext().getSharedPreferences("RideStatus", MODE_PRIVATE);
        mEditor = mPref.edit();
        mIdDriver = getIntent().getStringExtra("idDriver");





        getStatus();
        getClientBooking();

        mCardviewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getChat();

            }
        });

        createToken();


    }

    private void createToken() {
        mClientProvider.createToken(mAuthProvider.getId());
    }

    private void getChat() {
        Chat chat = new Chat();

        Intent intent = new Intent(MapClientBookingActivity.this, ChatActivity.class);
        intent.putExtra("idDriver", mIdDriver);
        intent.putExtra("idChat", chat.getId());
        Toast.makeText(MapClientBookingActivity.this, "IDChat: " + chat.getId() , Toast.LENGTH_LONG).show();

        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void getStatus() {
        mListenerStatus = mClientBookingProvider.getStatus(mAuthProvider.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String status = dataSnapshot.getValue().toString();
                    if (status.equals("accept")) {
                        mTextViewStatusBooking.setText("Estado: Aceptado");
                    }
                    if (status.equals("cancelado")) {
                        mTextViewStatusBooking.setText("Estado: Cancelado");
                        cancelBooking();
                    }
                    if (status.equals("start")) {
                        mTextViewStatusBooking.setText("Estado: Emergencia Iniciada");
                        String statusPref = mPref.getString("status", "");

                        if (!statusPref.equals("start")) {
                            startBooking();
                        }
                    } else if (status.equals("finish")) {
                        mTextViewStatusBooking.setText("Estado: Emergencia Finalizada");
                        finishBooking();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void startBooking() {
        mEditor.putString("status", "start");
        mEditor.putString("idDriver", mIdDriver);
        mEditor.apply();
        mMap.clear();
       // mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));

        if (mDriverLatLng != null) {
            mMarkerDriver = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(mDriverLatLng.latitude, mDriverLatLng.longitude))
                    .title("El bombero")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_topview)));

        }
       // drawRoute(mDestinationLatLng);

    }

    private void finishBooking() {
        mEditor.clear().commit();
        Intent intent = new Intent(MapClientBookingActivity.this, CalificationDriverActivity.class);
        startActivity(intent);
        finish();

    }

    private void cancelBooking() {
        mEditor.clear().commit();
        Intent intent = new Intent(MapClientBookingActivity.this, MapClientActivity.class);
        startActivity(intent);
        finish();

    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                   // String destination = dataSnapshot.child("destination").getValue().toString();
                    String origin = dataSnapshot.child("origin").getValue().toString();
                    String idDriver = dataSnapshot.child("idDriver").getValue().toString();
                    mIdDriver = idDriver;

                   // double destinatioLat = Double.parseDouble(dataSnapshot.child("destinationLat").getValue().toString());
                    //double destinatioLng = Double.parseDouble(dataSnapshot.child("destinationLng").getValue().toString());

                    double originLat = Double.parseDouble(dataSnapshot.child("originLat").getValue().toString());
                    double originLng = Double.parseDouble(dataSnapshot.child("originLng").getValue().toString());
                    mOriginLatLng = new LatLng(originLat, originLng);
                    //mDestinationLatLng = new LatLng(destinatioLat, destinatioLng);
                    mTextViewOriginClientBooking.setText("Emergencia en: " + origin);
                    //mTextViewDestinationClientBooking.setText("destino: " + destination);
                    mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Emergencia aqui").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
                    getDriver(idDriver);
                    getDriverLocation(idDriver);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getDriverLocation(String idDriver) {
        mListener = mGeofireProvider.getDriverLocation(idDriver).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    lat = Double.parseDouble(dataSnapshot.child("0").getValue().toString());
                    lng = Double.parseDouble(dataSnapshot.child("1").getValue().toString());
                    Log.e("error", "El  valor de lat y de lng " + lat + "  " + lng);
                    mDriverLatLng = new LatLng(lat, lng);

                    if (mIsFirstTime) {

                        mMarkerDriver = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lng))
                                .title("El bombero")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_topview)));
                        mIsFirstTime = false;
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(mDriverLatLng)
                                        .zoom(15f)
                                        .build()
                        ));

                        String status = mPref.getString("status", "");

                        if (status.equals("start")) {
                            startBooking();
                        } else {
                            mEditor.putString("status", "ride");
                            mEditor.putString("idDriver", mIdDriver);
                            mEditor.apply();
                            drawRoute(mOriginLatLng);
                        }
                    }

                    if (mStartLatLng != null) {
                        mEndLatLng = mStartLatLng;
                    }

                    mStartLatLng = new LatLng(lat, lng);

                    if (mEndLatLng != null) {
                        CarMoveAnim.carAnim(mMarkerDriver, mEndLatLng, mStartLatLng);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDriver(String idDriver) {
        mDriverProvider.getDriver(idDriver).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("image")) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.with(MapClientBookingActivity.this).load(image).into(mImageViewBooking);
                    }

                    if (dataSnapshot.hasChild("name")) {
                        if (dataSnapshot.hasChild("apellido")){
                            String name = dataSnapshot.child("name").getValue().toString();
                            String ape = dataSnapshot.child("apellido").getValue().toString();
                            mTextViewClientBooking.setText("Oficial: " + name + " " + ape);

                        }

                    }

                    if (dataSnapshot.hasChild("email")) {
                        String email = dataSnapshot.child("email").getValue().toString();
                        mTextViewEmailClientBooking.setText(email);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void drawRoute(LatLng latLng) {
        mGoogleApiProvider.getDirections(mDriverLatLng, latLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    JSONArray jsonArray = jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");
                    mPolylineList = DecodePoints.decodePoly(points);

                    mPolylineOptions = new PolylineOptions();
                    mPolylineOptions.color(Color.DKGRAY);
                    mPolylineOptions.width(13f);
                    mPolylineOptions.startCap(new SquareCap());
                    mPolylineOptions.jointType(JointType.ROUND);
                    mPolylineOptions.addAll(mPolylineList);
                    mMap.addPolyline(mPolylineOptions);

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);
                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration");
                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");

                } catch (Exception e) {
                    Log.d("Error", "Error encontrado" + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppBackgroundHelper.online(MapClientBookingActivity.this, true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppBackgroundHelper.online(MapClientBookingActivity.this, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null) {
            mGeofireProvider.getDriverLocation(mIdDriver).removeEventListener(mListener);
        }
        if (mListenerStatus != null) {
            mClientBookingProvider.getStatus(mAuthProvider.getId()).removeEventListener(mListenerStatus);
        }

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);


    }
}
