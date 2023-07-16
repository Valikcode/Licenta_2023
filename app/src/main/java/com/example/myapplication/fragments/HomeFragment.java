package com.example.myapplication.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements OnMapReadyCallback {

    // Firebase auth
    FirebaseAuth firebaseAuth;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        context = getContext();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.MapsFragment);
        mapFragment.getMapAsync(this);

        return view;

    }

    private void checkUserStatus() {
        // Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            // User is signed in => stay here
            // Set email of logged in user
            //textViewProfil.setText(user.getEmail());
        } else {
            // User is not signed in => go to main activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    // To show the menu
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    // Inflate options menu option in fragment
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflating menu
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Handle menu item click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // get item it
        int id = item.getItemId();
        if (id == R.id.action_logut) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    double latitude = (double) snapshot.child("latitude").getValue();
                    double longitude = (double) snapshot.child("longitude").getValue();

                    boolean gym = snapshot.child("gym").getValue(Boolean.class);
                    boolean gaming = snapshot.child("gaming").getValue(Boolean.class);
                    boolean education = snapshot.child("education").getValue(Boolean.class);

                    if (gym) {
                        DatabaseReference gymRef = FirebaseDatabase.getInstance().getReference("Locations").child("Gym");
                        gymRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot gymSnapshot : snapshot.getChildren()) {
                                    LatLng location = new LatLng(gymSnapshot.child("Lat").getValue(Double.class), gymSnapshot.child("Long").getValue(Double.class));
                                    String name = gymSnapshot.child("Denumire").getValue(String.class);

                                    MarkerOptions markerOptions = new MarkerOptions()
                                            .position(location)
                                            .title(name)
                                            .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.gym));
                                    Marker marker = googleMap.addMarker(markerOptions);
                                    marker.setTag(location);

                                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                        @Override
                                        public boolean onMarkerClick(@NonNull Marker clickedMarker) {
                                            if(clickedMarker.equals(marker)) {
                                                marker.showInfoWindow();
                                            }
                                            return false;
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    if (gaming) {
                        DatabaseReference gamingRef = FirebaseDatabase.getInstance().getReference("Locations").child("Gaming");
                        gamingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot gymSnapshot : snapshot.getChildren()) {
                                    LatLng location = new LatLng(gymSnapshot.child("Lat").getValue(Double.class), gymSnapshot.child("Long").getValue(Double.class));
                                    String name = gymSnapshot.child("Denumire").getValue(String.class);

                                    MarkerOptions markerOptions = new MarkerOptions()
                                            .position(location)
                                            .title(name)
                                            .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.gaming));
                                    Marker marker = googleMap.addMarker(markerOptions);
                                    marker.setTag(location);

                                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                        @Override
                                        public boolean onMarkerClick(@NonNull Marker clickedMarker) {
                                            if(clickedMarker.equals(marker)) {
                                                marker.showInfoWindow();
                                            }
                                            return false;
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    if (education) {
                        DatabaseReference gamingRef = FirebaseDatabase.getInstance().getReference("Locations").child("Education");
                        gamingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot gymSnapshot : snapshot.getChildren()) {
                                    LatLng location = new LatLng(gymSnapshot.child("Lat").getValue(Double.class), gymSnapshot.child("Long").getValue(Double.class));
                                    String name = gymSnapshot.child("Denumire").getValue(String.class);

                                    MarkerOptions markerOptions = new MarkerOptions()
                                            .position(location)
                                            .title(name)
                                            .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.education));
                                    Marker marker = googleMap.addMarker(markerOptions);
                                    marker.setTag(location);

                                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                        @Override
                                        public boolean onMarkerClick(@NonNull Marker clickedMarker) {
                                            if(clickedMarker.equals(marker)) {
                                                marker.showInfoWindow();
                                            }
                                            return false;
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    LatLng myLocation = new LatLng(latitude, longitude);
//                    MarkerOptions userMarker = new MarkerOptions()
//                            .position(myLocation)
//                            .title("User Location")
//                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_maps_icon));
//                    googleMap.addMarker(userMarker);
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    googleMap.setMyLocationEnabled(true);
                    //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation,15));
                    LatLng aseLocation = new LatLng(44.446971658053776, 26.096558689090617);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(aseLocation,15));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        LatLng location = new LatLng(44.44817264568965, 26.098415041817958);
        googleMap.addMarker(new MarkerOptions().position(location).title("ASE"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        if(context != null) {
            Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
            vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
            Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }
        return null;
    }
}

