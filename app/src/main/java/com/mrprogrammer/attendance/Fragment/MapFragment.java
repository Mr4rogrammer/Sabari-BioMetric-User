package com.mrprogrammer.attendance.Fragment;

// Fragment1.java

import android.Manifest;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mrprogrammer.Utils.Interface.CompleteHandler;
import com.mrprogrammer.Utils.Realm.RealmManager;
import com.mrprogrammer.Utils.Widgets.ProgressButton;
import com.mrprogrammer.attendance.DataUtils;
import com.mrprogrammer.attendance.Model.AttdanceTrack;
import com.mrprogrammer.attendance.PostAttdance;
import com.mrprogrammer.attendance.R;
import com.mrprogrammer.attendance.Utils;
import com.mrprogrammer.mrshop.ObjectHolder.ObjectHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import info.mrprogrammer.admin_bio.Model.AttdanceModel;
import io.realm.Realm;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MapFragment extends Fragment implements OnMapReadyCallback , AdapterView.OnItemSelectedListener{
    SupportMapFragment supportMapFragment;

    Double lat= 11.496213;
    Double lon=  77.276925;
    private GoogleMap googleMapView = null;
    FusedLocationProviderClient mFusedLocationClient;
    Location location = null;
    List<Marker> markers = new ArrayList<>();

    protected LatLng start = null;
    protected LatLng end = null;

    int PERMISSION_ID = 44;

    ProgressButton button;

    CircleOptions  circleOptions = new CircleOptions();

    private Circle mCircle;

    boolean isFirst = true;

    ImageView mylocation, collage;

    View root;
    private CancellationSignal cancellationSignal = null;
    private BiometricPrompt.AuthenticationCallback authenticationCallback;

    private Spinner spinner;
    private String spinnerStatus;

    private void spinner() {
        spinner = root.findViewById(R.id.spinner);
        String[] items = {"Day", "Specific"};
        ArrayAdapter<String> adapter = new ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root =  inflater.inflate(R.layout.map_fragment, container, false);
        try {
            supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapView);
            supportMapFragment.getMapAsync(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        spinner();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        button = root.findViewById(R.id.button);
        mylocation = root.findViewById(R.id.mylocation);
        collage = root.findViewById(R.id.collage);

        mylocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Check()){
                    showMyLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            }
        });

        collage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Check()) {
                    LatLng l = new LatLng(lat, lon);
                    showMyLocation(l);
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkTodayStatus()) {
                    ObjectHolder.Companion.getInstance().MrToast().warning(requireActivity(),"Already done", Toast.LENGTH_LONG);
                    return;
                }

                if(Check()) {
                    HashMap<String, String> map =  checkIamInside(location);
                    String status = map.get("status");
                    String toast = map.get("toast");
                    if(Objects.equals(status, "0")) {
                        ObjectHolder.Companion.getInstance().MrToast().warning(requireActivity(),toast, Toast.LENGTH_LONG);
                    }else {
                        markMyAttdance();
                    }
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            authenticationCallback = new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(
                        int errorCode, CharSequence errString)
                {
                    super.onAuthenticationError(errorCode, errString);
                    notifyUser("Authentication Error : " + errString);
                }
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result)
                {
                    super.onAuthenticationSucceeded(result);
                    post();
                }
            };
        }
        ObjectHolder.Companion.getInstance().getAttdanceModel().observe(getViewLifecycleOwner(), attdanceModel -> {
            if(attdanceModel != null && attdanceModel.getRadius() != null) {
                updateCircleRadius(Integer.valueOf(attdanceModel.getRadius()));
            }
        });

        Utils.Companion.getDataAndStoreLocally(attdanceModel -> {
            updateCircleRadius(Integer.valueOf(attdanceModel.getRadius()));
            return null;
        });
        return root;
    }

    private void saveStatus() {
        AttdanceTrack attdanceTrack = new AttdanceTrack(Long.valueOf(Utils.Companion.getTrackDate()));
        RealmManager.getInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(attdanceTrack);
            }
        });
    }


    private boolean checkTodayStatus() {
        Long today = Long.valueOf(Utils.Companion.getTrackDate());
        AttdanceTrack attdanceTrack = RealmManager.getInstance().where(AttdanceTrack.class).equalTo("day", today).findFirst();
        return attdanceTrack != null;
    }

    private void checkForUserName() {
        View dialogView = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_login, null);
        EditText usernameEditText = dialogView.findViewById(R.id.edit_text_username);
        EditText passwordEditText = dialogView.findViewById(R.id.edit_text_password);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(dialogView)
                .setTitle("Login")
                .setPositiveButton("Login", (dialog, which) -> {
                    String username = usernameEditText.getText().toString();
                    String password = passwordEditText.getText().toString();

                    DataUtils.Companion.getDataAndSpAttdanceModel(new Function1<AttdanceModel, Unit>() {
                        @Override
                        public Unit invoke(AttdanceModel attdanceModel) {
                            if (attdanceModel.getUsername().equals(username) && attdanceModel.getPassword().equals(password)) {
                                postAtta();
                            } else {
                                ObjectHolder.Companion.getInstance().MrToast().success(requireActivity(),"Invalid Login",Toast.LENGTH_LONG);
                            }
                            return null;
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void postAtta() {
        PostAttdance.Companion.post(requireContext(), new CompleteHandler() {
            @Override
            public void onSuccess(@NonNull Object o) {
                saveStatus();
                ObjectHolder.Companion.getInstance().MrToast().success(requireActivity(),"Success",Toast.LENGTH_LONG);
            }

            @Override
            public void onFailure(@NonNull String s) {
                notifyUser(s);
            }
        });
    }

    private void post() {
        String[] items = {"Day", "Specific"};
        if (Objects.equals(spinnerStatus, items[0])) {
            postAtta();
        } else {
            checkForUserName();
        }
    }

    private void notifyUser(String message)
    {
        Utils.Companion.showDialog(requireContext(),message);

    }
    private CancellationSignal getCancellationSignal()
    {
        cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(
                new CancellationSignal.OnCancelListener() {
                    @Override public void onCancel()
                    {
                        notifyUser("Authentication was Cancelled by the user");
                    }
                });
        return cancellationSignal;
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private Boolean checkBiometricSupport()
    {
        KeyguardManager keyguardManager = (KeyguardManager) requireContext().getSystemService(Context.KEYGUARD_SERVICE);
        if (!keyguardManager.isDeviceSecure()) {
            notifyUser("Fingerprint authentication has not been enabled in settings");
            return false;
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.USE_BIOMETRIC)!= PackageManager.PERMISSION_GRANTED) {
            notifyUser("Fingerprint Authentication Permission is not enabled");
            return false;
        }
        if (requireActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            return true;
        }
        else
            return true;
    }


    private void markMyAttdance() {
        BiometricPrompt biometricPrompt = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            biometricPrompt = new BiometricPrompt
                    .Builder(requireActivity().getApplicationContext())
                    .setTitle("Title of Prompt")
                    .setSubtitle("Subtitle")
                    .setDescription("Uses FP")
                    .setNegativeButton("Cancel", requireActivity().getMainExecutor(), new DialogInterface.OnClickListener() {
                        @Override
                        public void
                        onClick(DialogInterface dialogInterface, int i)
                        {
                            notifyUser("Authentication Cancelled");
                        }
                    }).build();

            biometricPrompt.authenticate(
                    getCancellationSignal(),
                    requireActivity().getMainExecutor(),
                    authenticationCallback);
        }
    }


    private boolean Check() {
        if(location == null) {
            ObjectHolder.Companion.getInstance().MrToast().warning(requireActivity(),"Please wait while processing....",Toast.LENGTH_LONG);
            return  false;
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                               @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    private void getLastLocation() {
        if (isLocationEnabled()) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions();
            }
            mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                Location location = task.getResult();
                if (location == null) {
                    requestNewLocationData();
                } else {
                    try {
                        requestNewLocationData();
                    } catch (Exception e) {
                        locationData(location);
                    }
                }
            });
        } else {
            Toast.makeText(requireContext(), "Please turn on your location...", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    private void requestNewLocationData() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            locationData(mLastLocation);
        }
    };

    private void locationData(Location locations) {
        location = locations;
        updateLocationForUser();
    }

    private void updateLocationForUser() {
        googleMapView.clear();
        LatLng locations = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(locations)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        googleMapView.addMarker(markerOptions);
        collagePoint();
        if(isFirst) {
            showMyLocation(new LatLng(location.getLatitude(), location.getLongitude()));
            isFirst = false;
        }
    }


    void showMyLocation(LatLng location) {
        googleMapView.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
    }

    private void updateLocationForLocation(LatLng latLng) {
        LatLng locations = new LatLng(latLng.latitude, latLng.longitude);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(locations)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        googleMapView.addMarker(markerOptions);
    }

    private void drawCircle(LatLng point){
        googleMapView.clear();
        circleOptions.center(point);
        circleOptions.strokeColor(0xffff0000);
        circleOptions.fillColor(0x44ff0000);
        circleOptions.strokeWidth(8);
        googleMapView.addCircle(circleOptions);
        mCircle = googleMapView.addCircle(circleOptions);
    }


    private void updateCircleRadius(int value) {
        circleOptions.radius(value);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMapView = googleMap;
        collagePoint();
        getLastLocation();
    }

    private void collagePoint() {
        drawCircle(new LatLng(lat, lon));
        updateLocationForLocation(new LatLng(lat, lon));
    }
    private HashMap<String, String> checkIamInside(Location location) {
        HashMap<String, String> map = new HashMap<>();
        map.put("status", "0");
        map.put("toast", "Please wait wait Processing");

        float[] distance = new float[2];
        if(mCircle == null || location == null ){
            return map;
        }

        Location.distanceBetween( location.getLatitude(), location.getLongitude(), mCircle.getCenter().latitude, mCircle.getCenter().longitude, distance);

        if( distance[0] > mCircle.getRadius() ){
            map.put("status", "0");
            map.put("toast", "Outside, from Access Point.");
        } else {
            map.put("status", "1");
            map.put("toast", "Inside, Access Point.");
        }
        return map;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String[] items = {"Day", "Specific"};
        spinnerStatus = items[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
