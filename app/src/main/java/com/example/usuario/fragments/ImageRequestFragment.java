package com.example.usuario.fragments;

import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.usuario.R;
import com.example.usuario.adapters.CardAdapter;

public class ImageRequestFragment extends Fragment {

    View mView;
    CardView mCardViewOptions2;
    ImageView mImageViewPicture;
    ImageView mImageViewBack;
    ImageView mImageViewSend;
    LinearLayout mLinearLayoutImagePagerRequest;
    EditText mEditTextComment;


    public static Fragment newInstance(int position, String imagePath, int size) {
        ImageRequestFragment fragment = new ImageRequestFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putInt("size", size);
        args.putString("image", imagePath);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_image_request, container, false);
        mCardViewOptions2 = mView.findViewById(R.id.cardViewRequest2);
        mImageViewPicture = mView.findViewById(R.id.imageViewPicture2);
        mImageViewBack = mView.findViewById(R.id.imageViewBack2);
        mLinearLayoutImagePagerRequest = mView.findViewById(R.id.linearLayoutViewPagerRequest);
        mEditTextComment = mView.findViewById(R.id.editTextComment2);
        mImageViewSend = mView.findViewById(R.id.imageViewSend2);
        mCardViewOptions2.setMaxCardElevation(mCardViewOptions2.getCardElevation() * CardAdapter.MAX_ELEVATION_FACTOR);



        return mView;

    }

    public CardView getCardView() {
        return mCardViewOptions2;
    }

}
