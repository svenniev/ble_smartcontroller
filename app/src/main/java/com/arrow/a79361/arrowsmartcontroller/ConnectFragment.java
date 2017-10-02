package com.arrow.a79361.arrowsmartcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConnectFragment extends Fragment {
    public static Button btnScanDevice;
    public static Button btnConnect;
    public static Button btnSend;
    public static ListView lvNewDevices;
    public static ListView lvDiscDevices;
    private TextView BleFeedback;
    public static List<String> mDeviceNames = new ArrayList<String>();

    public static ArrayList<ControlItem> adapter;
    public static ArrayList<ControlItem> adapterD;
    public static CustomListOfBleAdapter aAdapterD;

    private static final String TAG = "MyActivity";

    private static final long SCAN_PERIOD = 2000;
    public BluetoothDevice btDevSelected;

    public static ArrayList<String> adapterChar;
    public static ArrayAdapter<ControlItem> arAdapterD;
    public static final String[] aListAddress = {"eg", "ket"};

    private static int charnr = 0;

    public static boolean onoff = false;

    private byte[] bytes = new byte[0x2800];
    private final UUID BT_UUID_S = UUID.fromString("00031100-0000-1000-8000-00805f9b0131");
    private final UUID BT_UUID_C = UUID.fromString("00031101-0000-1000-8000-00805f9b0131");

    private OnFragmentInteractionListener mListener;

    private String userInputValue;

    public ConnectFragment() {
        // Required empty public constructor
    }

    public void showProcess(final String processMessage) {
        // BleFeedback.setText(processMessage);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BleFeedback.setText(processMessage);
            }
        });
    }

    public void addToAdapter(final ControlItem toBeAdded) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toBeAdded == null) {
                    if (adapter != null) {
                        adapter.clear();
                    }
                } else {
                    // check if already contains
                    boolean bContains = false;
                    for (ControlItem ciItem : adapter) {
                        if (ciItem.getAddress().equals(toBeAdded.getAddress())) { bContains = true; }
                    }

                    if (!bContains) { adapter.add(toBeAdded); }

                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connect, container, false);

        lvNewDevices = (ListView) view.findViewById(R.id.lvNewDevices);
        lvNewDevices.setChoiceMode(1);
        lvDiscDevices = (ListView) view.findViewById(R.id.lvDiscItems);
        lvDiscDevices.setChoiceMode(1);

        adapter = new ArrayList<ControlItem>();
        adapterD = new ArrayList<ControlItem>();
        aAdapterD = new CustomListOfBleAdapter(getContext(), adapterD);

        lvNewDevices.setAdapter(new CustomListOfBleAdapter(getContext(), adapter));
        lvNewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                btDevSelected = adapter.get(position).getDevice();

                final AlertDialog.Builder inputAlert = new AlertDialog.Builder(getContext());
                inputAlert.setTitle("Enter Password");
                inputAlert.setMessage("Device Password");
                final EditText userInput = new EditText(getContext());
                inputAlert.setView(userInput);
                inputAlert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userInputValue = userInput.getText().toString();
                        addElementDisc(position, userInputValue);

                    }
                });
                inputAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = inputAlert.create();
                alertDialog.show();


            }
        });

        lvDiscDevices.setAdapter(aAdapterD);

        lvDiscDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                btDevSelected = aAdapterD.getItem(position).getDevice();
            }
        });

        btnScanDevice = (Button) view.findViewById(R.id.buttonScan);
        BleFeedback = (TextView) view.findViewById(R.id.textViewBTState);

        btnScanDevice.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanLeDevice();
            }
        });

        return view;

    }



    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            String mDevAdd = device.getAddress();

            // Check if device address starts with specific value and is not already added
            if (mDevAdd.startsWith("00:A0:51")) {
                ControlItem newItem = new ControlItem(device);
                newItem.setRssi(rssi);
                addToAdapter(newItem);

                Log.d(TAG, mDevAdd);
            }
        }
    };

    private void addElementDisc(int position, String userInputValue) {
        if (userInputValue.length() > 2) {

                //adapter.get(position).setName(userInputValue);
                adapterD.add(adapter.get(position));
                adapter.remove(position);
            MainMenu.sPass = userInputValue;

            aAdapterD.notifyDataSetChanged();
        } else {
            Toast.makeText(getContext(), "Minimum 3 character", Toast.LENGTH_SHORT);
        }
    }

    private void scanLeDevice() {
        new Thread() {

            @Override
            public void run() {
                showProcess("Scanning...");
                addToAdapter(null);

                //if(android.os.Build.VERSION.SDK_INT<21)
                    MainMenu.btAdapter.startLeScan(leScanCallback);
                //else{
                //    MainMenu.bluetoothLeScanner.startScan(leScanCallbackLP);
                //}


                try {
                    Thread.sleep(SCAN_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                MainMenu.btAdapter.stopLeScan(leScanCallback);
                showProcess("Scanning complete");
            }
        }.start();
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
        void onFragmentInteraction(Uri uri);
    }



}
