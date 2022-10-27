package com.bignerdranch.android.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import java.io.File;

public class PhotoViewerDialog extends DialogFragment {
        private static final String ARG_PHOTO_FILE = "photoFile";

        private ImageView mPhotoView;
        private File mPhotoFile;

        public static PhotoViewerDialog newInstance(File photoFile) {
            Bundle args = new Bundle();
            args.putSerializable(ARG_PHOTO_FILE, photoFile);

            PhotoViewerDialog fragment = new PhotoViewerDialog();
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mPhotoFile = (File) getArguments().getSerializable(ARG_PHOTO_FILE);

            View view = inflater.inflate(R.layout.dialog_photo, container, false);

            mPhotoView = (ImageView) view.findViewById(R.id.photo_view_dialog);

            if (mPhotoFile == null || !mPhotoFile.exists()) {
                mPhotoView.setImageDrawable(null);
            } else {
                Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
                mPhotoView.setImageBitmap(bitmap);
            }

            return view;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //use this to modify dialog characteristics
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.requestWindowFeature(Window.FEATURE_SWIPE_TO_DISMISS);
            return dialog;
        }

//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        mPhotoFile = (File) getArguments().getSerializable(ARG_PHOTO_FILE);
//
//        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_photo, null);
//        mPhotoView = (ImageView) v.findViewById(R.id.photo_view_dialog);
//
//        Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
//        mPhotoView.setImageBitmap(bitmap);
//
////        return new AlertDialog.Builder(getActivity())
//        return new AlertDialog.Builder(getActivity())
//            .setView(v)
//            .create();
//    }
}