package br.com.fiap.mapline.fragment;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import br.com.fiap.mapline.activity.MainActivity;
import br.com.fiap.mapline.R;
import br.com.fiap.mapline.util.BitMapUtil;


public class ItemFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    boolean isFirstStart = true;

    static View view;
    Firebase mapRef;
    Firebase fireRef;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ItemFragment newInstance(int columnCount) {

        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_item_list, container, false);

        System.out.println("full nod111: " + fireRef);
        fireRef = new Firebase("https://torrid-fire-6287.firebaseio.com");
        mapRef = fireRef.child("map");
        // Set the adapter
        if (view instanceof RecyclerView) {

            System.out.println("RECYCLER");
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            
            recyclerView.setHasFixedSize(true);
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            FirebaseRecyclerAdapter<JsonNode, MyViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<JsonNode, MyViewHolder>(JsonNode.class, R.layout.fragment_item, MyViewHolder.class, mapRef) {

                @Override
                protected void populateViewHolder(MyViewHolder myViewHolder, JsonNode jsonNode, int i) {


                    System.out.println("NODEEE " + jsonNode);


                    JsonNode details = jsonNode.get("details");
                    JsonNode name = details.get("name");
                    JsonNode color = details.get("color");
                    JsonNode email = details.get("email");
                    JsonNode avatar = details.get("avatar");
                    JsonNode center = details.get("center");
                    Bitmap bitmap = null;


                    if (color != null) {
                        myViewHolder.textName.setTextColor(color.asInt());
                        myViewHolder.textEmail.setTextColor(color.asInt());
                    }

                    if (name != null) {
                        myViewHolder.textName.setText(name.textValue());
                    }

                    if (email != null) {
                        myViewHolder.textEmail.setText(email.textValue());

                    }

                    if (avatar != null) {
                        Picasso.with(getContext()).load(avatar.textValue()).into(myViewHolder.avatar);
                        bitmap = ((BitmapDrawable) myViewHolder.avatar.getDrawable()).getBitmap();



                    }

                    if (center != null) {
                        LatLng latLng = new Gson().fromJson(center.toString(), LatLng.class);
                        myViewHolder.latLng = latLng;
                        myViewHolder.customMarkerBitMap = BitMapUtil.getMarkerBitmapFromView(avatar.textValue(), getContext());


                        if (myViewHolder.isMapReady) {
                            myViewHolder.marker.remove();

                            myViewHolder.addMarker(latLng, bitmap);
                        }
                    }






                }
            };

            recyclerView.setAdapter(firebaseRecyclerAdapter);

        }
        return view;
    }




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback, GoogleMap.OnMapClickListener, View.OnClickListener {

        public TextView textName;
        public TextView textEmail;
        public ImageView avatar;
        public MapView mapView = (MapView) itemView.findViewById(R.id.map_view);
        public LatLng latLng;
        public boolean isMapReady = false;
        public GoogleMap googleMap;
        public Marker marker;
        final FloatingActionButton fabEmail;
        MainActivity mainActivity;
        public Bitmap customMarkerBitMap;

        public MyViewHolder(View itemView) {
            super(itemView);
            mainActivity = (MainActivity) view.getContext();
            mapView.onCreate(null);
            mapView.getMapAsync(this);
            textName = (TextView) itemView.findViewById(R.id.cardname);
            textEmail = (TextView) itemView.findViewById(R.id.cardemail);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            fabEmail = (FloatingActionButton) itemView.findViewById(R.id.fab_email);
            fabEmail.setOnClickListener(this);
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            isMapReady = true;
            googleMap.setOnMapClickListener(this);
            this.googleMap = googleMap;

            if (latLng != null) {


                if (this.customMarkerBitMap != null) {
                   marker = googleMap.addMarker(new MarkerOptions()
                            .position(this.latLng)
                            .icon(BitmapDescriptorFactory.fromBitmap(this.customMarkerBitMap)));
                }else {
                    marker =  googleMap.addMarker(new MarkerOptions().position(latLng));
                }



                CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(4);

                googleMap.moveCamera(center);
                googleMap.animateCamera(zoom);
            }

        }

        public void addMarker(LatLng latLng, Bitmap customMarkerBitMap) {
            this.latLng = latLng;
            googleMap.clear();


            if (customMarkerBitMap != null) {
                marker =  googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(customMarkerBitMap)));
            }else {
                marker =  googleMap.addMarker(new MarkerOptions().position(latLng));
            }


            CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(4);

            googleMap.moveCamera(center);
            googleMap.animateCamera(zoom);
        }

        @Override
        public void onMapClick(LatLng latLng) {
            mainActivity.showUserLineOnMap(this.latLng);
        }

        @Override
        public void onClick(View v) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", textEmail.getText().toString(), null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "FIAP - Android");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Github: https://github.com/vymajoris/Mapline");
            mainActivity.startActivity(Intent.createChooser(emailIntent, "Send email..."));

        }
    }


}
