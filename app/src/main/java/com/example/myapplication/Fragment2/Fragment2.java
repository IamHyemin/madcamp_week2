package com.example.myapplication.Fragment2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.R;

import com.example.myapplication.Retrofit.myFile;
import com.example.myapplication.Retrofit.IMyService;
import com.example.myapplication.Retrofit.RetrofitClient;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.os.Looper.getMainLooper;

public class Fragment2 extends Fragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager linearLayoutManager;
    public static ArrayList<ImageInfo> mImages;
    public static ArrayList<ImageInfo> mImages_search;
    private ImageAdapter galleryRecyclerAdapter;
    private List<Integer> count;
    private int i = 0;
    private Context myContext ;
    private Bitmap storeImage;



    final IMyService retrofitClient = RetrofitClient.getApiService();

    public static Fragment2 newInstance(String param1, String param2) {
        Fragment2 fragment = new Fragment2();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private ArrayList<ImageInfo> getImagesFromStorage() {
        final ArrayList<ImageInfo> res = new ArrayList<>();
        /////////////////////////////// db에서 데이터 받아오기 /////////////////////////////////////
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<myFile> loginUser_res = retrofitClient.getAllFile().execute().body();
                    for (myFile elt : loginUser_res){
                        res.add(new ImageInfo(elt.getSaveFileName(), elt.getTitle(), elt.getDescription()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return res;
    }

    private void setImagesFromStorage() {
        final ArrayList<ImageInfo> res = new ArrayList<>();
        /////////////////////////////// db에서 데이터 받아오기 /////////////////////////////////////
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<myFile> loginUser_res = retrofitClient.getAllFile().execute().body();
                    for (myFile elt : loginUser_res){
                        res.add(new ImageInfo(elt.getSaveFileName(), elt.getTitle(), elt.getDescription()));
                    }
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mImages.clear();
                            mImages.addAll(res);
                            mImages_search = res;
                            galleryRecyclerAdapter.notifyDataSetChanged();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //////////////////////////////// action bar /////////////////////////////////////////
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayUseLogoEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setLogo(R.drawable.logo);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setElevation(0);
        //////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        Intent intent = Objects.requireNonNull(getActivity()).getIntent();
        final String email = Objects.requireNonNull(intent.getExtras()).getString("email");

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayUseLogoEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setLogo(R.drawable.logo);
        View v = inflater.inflate(R.layout.fragment2, container, false);

        recyclerView = v.findViewById(R.id.recyclerView);
        swipeRefreshLayout = v.findViewById(R.id.refresh_layout_fragment2);
        myContext = getContext();
        mImages = getImagesFromStorage();
        mImages_search = getImagesFromStorage();

        ////////////////////////////////////// 검색 //////////////////////////////////////////////
        final EditText editSearch = v.findViewById(R.id.editSearch);

        galleryRecyclerAdapter = new ImageAdapter(myContext, mImages, email);
        recyclerView.setAdapter(galleryRecyclerAdapter);

        linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editSearch.getText().toString();
                search(text);
            }
        });

        // -----------------------[당겨서 새로고침 리스너 추가]-----------------------

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 서버에서 파일들 다시 불러와야 됨
                setImagesFromStorage();

                // 새로고침 완료시,
                // 새로고침 아이콘이 사라질 수 있게 isRefreshing = false
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return v;
    }

    public void search(String charText) {
        mImages.clear();
        if (charText.length() == 0) {
            mImages.addAll(mImages_search);
        } else {
            // 리스트의 모든 데이터를 검색한다.
            for (int i = 0; i < mImages_search.size(); i++) {
                if (mImages_search.get(i).getImageMenu().toLowerCase().contains(charText.toLowerCase())) {
                    mImages.add(mImages_search.get(i));
                }
                if (mImages_search.get(i).getImageTitle().toLowerCase().contains(charText.toLowerCase())) {
                    mImages.add(mImages_search.get(i));
                }
            }
        }
        galleryRecyclerAdapter.notifyDataSetChanged();
    }

}

