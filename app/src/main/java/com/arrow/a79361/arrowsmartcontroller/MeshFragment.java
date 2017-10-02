package com.arrow.a79361.arrowsmartcontroller;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class MeshFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public static byte[] SET_RELIABLE = new byte[]{0x0F, 0x01, 0x01};
    public static byte[] SET_UNRELIABLE = new byte[]{0x0F, 0x01, 0x00};

    private Button bSetRel;
    private Button bSetUnrel;
    private Button bSetFade;
    private TextView tTemp;
    private TextView tLum;
    private TextView tCct;

    public MeshFragment() {
        // Required empty public constructor
    }


    public static MeshFragment newInstance() {
        MeshFragment fragment = new MeshFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_state_control, container, false);

        bSetRel = (Button) view.findViewById(R.id.bSetRel);
        bSetUnrel = (Button) view.findViewById(R.id.bSetUnrel);
        bSetFade = (Button) view.findViewById(R.id.bSetFade);

        /** bSetRel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainMenu.fMesh.sendMessage(SET_RELIABLE);
            }
        });

        bSetUnrel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainMenu.fMesh.sendMessage(SET_UNRELIABLE);
            }
        });
*/


        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
