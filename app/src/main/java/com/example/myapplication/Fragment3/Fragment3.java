package com.example.myapplication.Fragment3;

//import android.support.v4.app.FragmentActivity;
//import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.Fragment1.Fragment1;
import com.example.myapplication.R;
import com.example.myapplication.Retrofit.IMyService;
import com.example.myapplication.Retrofit.RetrofitClient;
import com.example.myapplication.Retrofit.User;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Response;

public class Fragment3 extends Fragment implements OnMapReadyCallback {

    private SwipeRefreshLayout swipeRefreshLayout;

    final ArrayList<LatLng> friendLocation = new ArrayList<LatLng>();
    final ArrayList<String> friendName = new ArrayList<String>();
    final ArrayList<String> friendState = new ArrayList<String>();
    final ArrayList <MarkerOptions> MarkerArray = new ArrayList<>();


    GoogleMap mMap;
    final IMyService retrofitClient = RetrofitClient.getApiService();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //////////////////////////////////// action bar //////////////////////////////////
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayUseLogoEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setLogo(R.drawable.logo);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setElevation(0);
        //////////////////////////////////////////////////////////////////////////////////

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent intent = Objects.requireNonNull(getActivity()).getIntent();
        final String email = Objects.requireNonNull(intent.getExtras()).getString("email");
        final View v = inflater.inflate(R.layout.fragment3, null, false);

        ////////////////////////////////////////// for make map ////////////////////////////////////////////////////////
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ////////////////////////////////////////////// 사람 찾기 ////////////////////////////////////////////////
        ImageButton btn_search = v.findViewById(R.id.ic_search);
        final EditText search = v.findViewById(R.id.position_search);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findTheUser(search.getText().toString());
            }
        });

        // ---------------------------------[당겨서 새로고침 기능 추가]---------------------------------
        swipeRefreshLayout = v.findViewById(R.id.refresh_layout_fragment3);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 서버에서 데이터들 다시 불러와야 됨
                reloadData();
                // 새로고침 완료시,
                // 새로고침 아이콘이 사라질 수 있게 isRefreshing = false
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        return v;
    }

    private void findTheUser(String toString) {
            for (int i = 0; i < friendName.size(); i++) {
                System.out.println(friendName.get(i));
                if (friendName.get(i).toLowerCase().contains(toString.toLowerCase())) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(friendLocation.get(i)));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(20));
                    return;
                }
            }
            Toast.makeText(getActivity(), toString + " does not your friend", Toast.LENGTH_SHORT).show();
        }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        MyLocationMarker();
    }

    /////////////////////////////// 내 위치 찍기 //////////////////////////////////
    public void MyLocationMarker() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = Objects.requireNonNull(getActivity()).getIntent();
                    final String email = Objects.requireNonNull(intent.getExtras()).getString("email");
                    Response<User> loginUser_res = retrofitClient.getUser(email).execute();
                    final User loginUser = loginUser_res.body();
                    final Double[] position_get = loginUser.getPosition();
                    final String[] friendList = loginUser.getFriendsList();
                    final LatLng myLocation = new LatLng(position_get[0], position_get[1]);


                    for (String friend_email: friendList){
                        User friend = retrofitClient.getUser(friend_email).execute().body();
                        Double[] location  = friend.getPosition();
                        friendLocation.add(new LatLng(location[0], location[1]));
                        friendName.add(friend.getName());
                        friendState.add(friend.getState());
                    }


                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            MarkerOptions makerOptions = new MarkerOptions();
                            makerOptions.position(myLocation).title("내 위치")
                                    .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                                    .alpha(0.5f);


                            for (int i = 0; i < friendList.length; i++){
                                String text = "friend";
                                if (friendState.get(i).contains(text.toLowerCase())) {
                                    MarkerOptions friendOptions = new MarkerOptions();
                                    friendOptions.position(friendLocation.get(i)).title(friendName.get(i))
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                                            .alpha(0.4f)
                                            .snippet(friendState.get(i));
                                    mMap.addMarker(friendOptions);
                                }else{
                                    MarkerOptions friendOptions = new MarkerOptions();
                                    friendOptions.position(friendLocation.get(i)).title(friendName.get(i))
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                            .alpha(0.2f)
                                            .snippet(friendState.get(i));
                                    mMap.addMarker(friendOptions);
                                    MarkerArray.add(friendOptions);
                                }
                            }

                            mMap.addMarker(makerOptions);
//                            mMap.setOnInfoWindowClickListener(infoWindowClickListener);
//                            mMap.setOnMarkerClickListener(markerClickListener);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker) {
                                    Toast.makeText(getActivity(), "업데이트를 원하시면 연락처를 수정해주세요", Toast.LENGTH_LONG);
                                    return false;
                                }
                            });
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        }

    // refresh할 때 호출할 함수 - DB로부터 다시 유저 정보를 받아오고 어댑터에 담긴 친구 목록을 갱신해야 함.
    private void reloadData() {
        // TODO: 여기 채우기
    }

}