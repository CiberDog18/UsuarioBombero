package com.example.usuario.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usuario.R;
import com.example.usuario.models.ClientBooking;
import com.example.usuario.models.Info;
import com.example.usuario.providers.GoogleApiProvider;
import com.example.usuario.providers.ImagesProvider;
import com.example.usuario.providers.InfoProvider;
import com.example.usuario.utils.DecodePoints;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private GoogleApiProvider mGoogleApiProvider;
    private InfoProvider mInfoProvider;

    Options mOptions;
    ImagesProvider mImageProvider;

    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private double mExtraDriverLat;
    private double mExtraDriverLng;
    private String mExtraOrigin;
    private LatLng mOriginLatLng;

    private String mExtraDriverId;

    private List<LatLng> mPolylineList;
    private PolylineOptions mPolylineOptions;

    private Button mButtonRequest;
    private TextView mTextViewOrigin;

    private CircleImageView mCircleImageBack;
    LinearLayout mImageViewSelectPicturesRequest;
    RoundedImageView mCircleImagePhoto;


    ArrayList<String> mReturnValues = new ArrayList<>();
    File mImageFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_request);
        // MyToolbar.show(this, "TUS DATOS", true);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat", 0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);
       // mExtraDestinationLat = getIntent().getDoubleExtra("destination_lat", 0);
       // mExtraDestinationLng = getIntent().getDoubleExtra("destination_lng", 0);
        mExtraOrigin = getIntent().getStringExtra("origin");
       // mExtraDestination = getIntent().getStringExtra("destination");
        mExtraDriverId = getIntent().getStringExtra("idDriver");
        mExtraDriverLat = getIntent().getDoubleExtra("driver_lat", 0);
        mExtraDriverLng = getIntent().getDoubleExtra("driver_lng", 0);



        mCircleImageBack = findViewById(R.id.circleImageBack);
        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLng);
    //    mDestinationLatLng = new LatLng(mExtraDestinationLat, mExtraDestinationLng);

        mGoogleApiProvider = new GoogleApiProvider(DetailRequestActivity.this);
        mInfoProvider = new InfoProvider();
        mImageProvider = new ImagesProvider("token_image");

        //mTextViewOrigin = findViewById(R.id.textViewOrigin);
        mCircleImagePhoto = findViewById(R.id.circleImagePhoto);
        mButtonRequest = findViewById(R.id.btnRequestNow);
        mImageViewSelectPicturesRequest = findViewById(R.id.imageViewSelectPicturesRequest);

     //   mTextViewOrigin.setText(mExtraOrigin);


        mOptions = Options.init()
                .setRequestCode(100)
                .setCount(1)
                .setFrontfacing(true)
                .setPreSelectedUrls(mReturnValues)
                .setExcludeVideos(true)
                .setVideoDurationLimitinSeconds(0)
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)
                .setPath("/pix/images");

        //////////////////////////////////////////////////////

        mButtonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mImageFile != null){
                    // QUEREMOS ENVIARLE LA NOTIFICACION A UN CONDUCTOR ESPECIFICO
                    saveImage();
                }
                else {

                    Toast.makeText(DetailRequestActivity.this, "Debe subir una imagen de la emergerncia", Toast.LENGTH_SHORT).show();
                }



            }
        });

///////////////////////////////////////

        mCircleImageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

//////////////////////////////////////////////////////////////

        mImageViewSelectPicturesRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPix();
            }
        });
    }

    private void saveImage() {
        mImageProvider.save(DetailRequestActivity.this, mImageFile).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {
                    mImageProvider.getDownloadUri().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String url = uri.toString();
                            goToRequestDriver(url);

                        }
                    });
                }
                else {
                 //   mDialog.dismiss();
                    Toast.makeText(DetailRequestActivity.this, "No se pudo almacenar la imagen", Toast.LENGTH_SHORT).show();
                }

            }
        });




    }

    private void uploadImage(String url) {
        ClientBooking clientBooking = new ClientBooking();
        clientBooking.setImageRequest(url);


    }
    ////////////////////////////////////////

    private void startPix() {
        Pix.start(DetailRequestActivity.this, mOptions);
    }


    private void goToRequestDriver(String url) {

        Intent intent = new Intent(DetailRequestActivity.this, RequestDriverActivity.class);

        intent.putExtra("origin_lat", mOriginLatLng.latitude);
        intent.putExtra("origin_lng", mOriginLatLng.longitude);
        intent.putExtra("origin", mExtraOrigin);
        intent.putExtra("imageRequest", url);
        //intent.putExtra("destination", mExtraDestination);
        //intent.putExtra("destination_lat", mDestinationLatLng.latitude);
        //intent.putExtra("destination_lng", mDestinationLatLng.longitude);

        startActivity(intent);
        finish();
    }


    /*private void drawRoute() {
        mGoogleApiProvider.getDirections(mOriginLatLng, mDestinationLatLng).enqueue(new Callback<String>() {
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
                    mTextViewTime.setText(durationText + " " + distanceText);

                    String[] distanceAndKm = distanceText.split(" ");
                    double distanceValue = Double.parseDouble(distanceAndKm[0]);

                    String[] durationAndMins = durationText.split(" ");
                    double durationValue = Double.parseDouble(durationAndMins[0]);

                   // calculatePrice(distanceValue, durationValue);

                } catch (Exception e) {
                    Log.d("Error", "Error encontrado " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }*/

    /*private void calculatePrice(double distanceValue, double durationValue) {

        mInfoProvider.getInfo().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Info info = snapshot.getValue(Info.class);
                    double totalDistance = distanceValue * info.getKm();
                    double totalDuration = durationValue * info.getMin();
                    double tarifaMin = info.getTarifaMin();
                    double aprox = totalDistance + totalDuration;

                    double total = aprox - (aprox % 100) + 100;
                    DecimalFormat format = new DecimalFormat("0");

                    if (total < tarifaMin) {
                        total = tarifaMin + 100;

                    }

                    String auxTotal = format.format(Math.ceil(total));
                    auxTotal = auxTotal.replaceAll(",", "");


                    mTextViewPrice.setText("Aprox $" + auxTotal);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    */



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_red)));
       // mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_pin_blue)));

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(mOriginLatLng)
                        .zoom(14f)
                        .build()
        ));

     //   drawRoute();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 100) {
            mReturnValues = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
               mImageFile = new File(mReturnValues.get(0));
               mCircleImagePhoto.setImageBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Pix.start(DetailRequestActivity.this, mOptions);
            }else {
                Toast.makeText(DetailRequestActivity.this, "Por favor concede los permisos para acceder a la camara", Toast.LENGTH_LONG).show();
            }

        }

    }



}