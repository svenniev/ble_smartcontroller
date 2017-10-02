package com.arrow.a79361.arrowsmartcontroller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Arrays;
import java.util.List;

/**
 * Command template for BLE
 * 6 byte pockets:
 *  AABBCC
 *
 *  AA: channel
 *  01 - diffuse
 *  02 - spot
 *  03 - both
 *
 *  BB: intensity
 *  2 byte
 *
 *  CC: CCT
 *  2 byte
 *  low: 3000K, high: 5700K
 *
 */


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StateControlFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StateControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StateControlFragment extends Fragment {

    public static byte[] BITES_TO_SEND = new byte[]{0x00, 0x00, 0x03};

    private byte mByteChannel = 0x01;
    private byte mByteIntensity = 0x01;
    private byte mByteCCT = 0x01;

    private byte mByteOnBrightness = 0x73;
    private byte mByteOnCct = 0x73;
    private byte mByteOffBrightness = 0x00;
    private byte mByteOffCct = 0x73;
    private byte mByteTypeAndLenghtOffset = 0x00;

    private int mIntMesh;

    private String sNewPass;

    private static final String TAG = "MyActivity";

    private ToggleButton switchOnOff;
    private ToggleButton tbFlood;
    private ToggleButton tbTask;
    private ToggleButton tbProx;
    private ToggleButton tbProxSet;

    private Spinner LuminaireSpinner;
    private SeekBar SbIntensity;
    private SeekBar SbCct;

    private Button btnChPass;

    private OnFragmentInteractionListener mListener;

    private ControlItem CiSelected;

    public StateControlFragment() {
        // Required empty public constructor
    }

    public static StateControlFragment newInstance() {
        StateControlFragment fragment = new StateControlFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }


    @Override
    public void onResume() {
        if (ConnectFragment.adapterD != null) {
            LuminaireSpinner.setAdapter(ConnectFragment.aAdapterD);
        }
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_state_control, container, false);

        //btnScanDevice = (Button) view.findViewById(R.id.buttonScan);
        switchOnOff = (ToggleButton) view.findViewById(R.id.tbSwitch);
        tbFlood = (ToggleButton) view.findViewById(R.id.tbFlood);
        tbTask = (ToggleButton) view.findViewById(R.id.tbTask);
        tbProx = (ToggleButton) view.findViewById(R.id.tbProx);
        tbProxSet = (ToggleButton) view.findViewById(R.id.tbProxSet);

        btnChPass = (Button) view.findViewById((R.id.btnChPass));

        SbIntensity = (SeekBar) view.findViewById(R.id.SbIntensity);
        SbIntensity.setMax(255);
        SbCct = (SeekBar) view.findViewById(R.id.SbCct);
        SbCct.setMax(255);

        LuminaireSpinner = (Spinner) view.findViewById(R.id.lumspinner);
        LuminaireSpinner.setAdapter(ConnectFragment.aAdapterD);

        btnChPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder inputAlert = new AlertDialog.Builder(getContext());
                inputAlert.setTitle("Enter New Password");
                inputAlert.setMessage("Device Password");
                final EditText userInput = new EditText(getContext());
                inputAlert.setView(userInput);
                inputAlert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sNewPass = userInput.getText().toString();

                        // TODO
                        setNewPass(sNewPass);

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


        /**
         * Luminaire spinner, other ControlItem selected
         */
        LuminaireSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                CiSelected = ConnectFragment.aAdapterD.getItem(position);

                // Set node / mesh offset value
                if (CiSelected.getType() == 80) {
                    mByteTypeAndLenghtOffset = 0x79;
                } else {
                    mByteTypeAndLenghtOffset = 0x00;
                }

                btConnect(CiSelected.getDevice());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // not quite possible, have to handle
            }

        });

        /**
         *  Turn the luminaire ON / OFF
         */
        switchOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if(isChecked)
                {
                    mIntMesh = 128;
                }
                else
                {
                    mIntMesh = 0;
                }
            }
        });

        /**
         *  Proximity sensor enable / disable
         */
        tbProx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if(isChecked)
                {
                    BITES_TO_SEND = new byte[]{(byte)(mIntMesh + 7), 0x01, 0x01};
                    sendMessage(BITES_TO_SEND);
                }
                else
                {
                    BITES_TO_SEND = new byte[]{(byte)(mIntMesh + 7), 0x01, 0x00};
                    sendMessage(BITES_TO_SEND);
                }
            }
        });

        /**
         *  Set intensity
         */
        SbIntensity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mByteIntensity = (byte)(progress);

                // Proximity checked:
                if (tbProx.isChecked()) {
                    // On brightness
                    if (tbProxSet.isChecked()) {
                        if (tbFlood.isChecked()) {
                            BITES_TO_SEND = new byte[]{(byte)(mIntMesh + 2), 0x01, mByteIntensity};
                        }
                        if (tbTask.isChecked()) {
                            BITES_TO_SEND = new byte[]{(byte)(mIntMesh + 3), 0x01, mByteIntensity};
                        }
                        if (tbFlood.isChecked()) {
                            if (tbFlood.isChecked()) {
                                BITES_TO_SEND = new byte[]{(byte)(mIntMesh + 2), 0x02, mByteIntensity, mByteIntensity};
                            }
                        }
                    } else {
                        if (tbFlood.isChecked()) {
                            BITES_TO_SEND = new byte[]{(byte)(mIntMesh + 4), 0x01, mByteIntensity};
                        }
                        if (tbTask.isChecked()) {
                            BITES_TO_SEND = new byte[]{(byte)(mIntMesh + 5), 0x01, mByteIntensity};
                        }
                        if (tbFlood.isChecked()) {
                            if (tbTask.isChecked()) {
                                BITES_TO_SEND = new byte[]{(byte)(mIntMesh + 4), 0x02, mByteIntensity, mByteIntensity};
                            }
                        }
                    }
                } else {
                    if (tbFlood.isChecked()) {
                        BITES_TO_SEND = new byte[]{(byte)(mIntMesh + 17), 0x01, mByteIntensity};
                    }
                    if (tbTask.isChecked()) {
                        BITES_TO_SEND = new byte[]{(byte)(mIntMesh + 18), 0x01, mByteIntensity};
                    }
                    if (tbFlood.isChecked()) {
                        if (tbTask.isChecked()) {
                            BITES_TO_SEND = new byte[]{(byte)(mIntMesh + 17), 0x02, mByteIntensity, mByteIntensity};
                        }
                    }
                }
                sendMessage(BITES_TO_SEND);
            }

        });

        SbCct.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mByteCCT = (byte) (255 - progress);


                // Proximity checked:
                if (tbProx.isChecked()) {
                    if (tbProxSet.isChecked()) {
                        BITES_TO_SEND = new byte[]{(byte)(mIntMesh + 0), 0x01, mByteCCT};
                    } else {
                        BITES_TO_SEND = new byte[]{(byte)(mIntMesh + 1), 0x01, mByteCCT};
                    }
                } else {
                    BITES_TO_SEND = new byte[]{(byte)(mIntMesh + 16), 0x01, mByteCCT};
                }
                sendMessage(BITES_TO_SEND);
            }

        });

        return view;
    }

    public final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
            byte[] data = characteristic.getValue();

            if (data != null) {
                //showProcess("sent: " + data);
                //Log.d(TAG, "Message: " + data.toString());
            }
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                setStateOfControlItemByAddress(gatt.getDevice().getAddress(), 2);
                //Toast.makeText(getContext(), "connected", Toast.LENGTH_SHORT).show();
                //Log.i(TAG, "CONNECTED");
                gatt.discoverServices();

                updateLists();
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
            List<BluetoothGattService> services = gatt.getServices();
            //Log.i("onServicesDiscovered", services.toString());

            for (BluetoothGattService iBtGattService : services) {
                iBtGattService.getCharacteristics();
                Log.d(TAG, "Service: " + iBtGattService.getUuid().toString());
            }
            //MainMenu.fConn.aAdapterD.notifyAll();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            //Log.i("onCharacteristicRead", characteristic.toString());
            byte[] value=characteristic.getValue();
            String v = new String(value);
            //Log.i("onCharacteristicRead", "Value: " + v);
            //gatt.disconnect();
        }


    };

    public void updateLists() {
        LuminaireSpinner.setAdapter(ConnectFragment.aAdapterD);
        LuminaireSpinner.notify();
        ConnectFragment.lvDiscDevices.invalidate();
    }


    public void setStateOfControlItemByAddress(String mAddress, int mState) {
        ControlItem ci;
        int nElements = ConnectFragment.aAdapterD.getCount();

        for (int i = 0; i < nElements; i++) {
            ci = ConnectFragment.aAdapterD.getItem(i);
            if (ci.getAddress() == mAddress) {
                ci.setState(mState);
            } else {
                if (ci.getStateInt() == mState) {
                    ci.setState(0);
                }
                if (mState == 1) {
                    if (ci.getStateInt() == 2) {
                        ci.setState(0);
                    }
                }
            }
        }
    }

    public void sendMessage(byte[] message) {
        BluetoothGattCharacteristic btGattCharSendData;
        BluetoothGattCharacteristic btGattCharPassword;
        //String sPass = "password";

        if (MainMenu.btGatt == null) {

        } else {

            if (MainMenu.btGatt.getServices().size() > 0) {
                // send password
                btGattCharPassword = MainMenu.btGatt.getServices().get(3).getCharacteristics().get(0);
                btGattCharPassword.setValue(MainMenu.sPass.getBytes());
                MainMenu.btGatt.writeCharacteristic(btGattCharPassword);
                Log.i(TAG, Arrays.toString(message));

                try
                {
                    Thread.sleep(500);
                }
                catch(InterruptedException ex)
                {
                    Thread.currentThread().interrupt();
                }

                // send data
                btGattCharSendData = MainMenu.btGatt.getServices().get(2).getCharacteristics().get(0);
                btGattCharSendData.setValue(message);
                MainMenu.btGatt.writeCharacteristic(btGattCharSendData);
                Log.i(TAG, Arrays.toString(message));
            } else {
                Toast.makeText(getContext(), "No device connected", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void setNewPass(String sNewPassToSet) {
        BluetoothGattCharacteristic btGattCharSendData;
        BluetoothGattCharacteristic btGattCharPassword;
        //String sPass = "password";

        if (MainMenu.btGatt == null) {

        } else {

            if (MainMenu.btGatt.getServices().size() > 0) {
                // send password
                btGattCharPassword = MainMenu.btGatt.getServices().get(3).getCharacteristics().get(0);
                btGattCharPassword.setValue(MainMenu.sPass.getBytes());
                MainMenu.btGatt.writeCharacteristic(btGattCharPassword);
                //Log.i(TAG, Arrays.toString(message));

                try
                {
                    Thread.sleep(500);
                }
                catch(InterruptedException ex)
                {
                    Thread.currentThread().interrupt();
                }

                // send data
                btGattCharSendData = MainMenu.btGatt.getServices().get(3).getCharacteristics().get(1);
                btGattCharSendData.setValue(sNewPassToSet.getBytes());
                MainMenu.btGatt.writeCharacteristic(btGattCharSendData);
                MainMenu.sPass = sNewPassToSet;
                //Log.i(TAG, Arrays.toString(message));
            } else {
                Toast.makeText(getContext(), "No device connected", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void btConnect(BluetoothDevice dConnect) {
        // disconnect from previous
        try {
            MainMenu.btGatt.disconnect();
        } catch(NullPointerException e)
        {
            System.out.print("NullPointerException caught");
        }

        // joining
        Toast.makeText(getContext(), "Connecting to " + dConnect.getAddress(), Toast.LENGTH_SHORT).show();
        setStateOfControlItemByAddress(dConnect.getAddress().toString(), 1);

        // join to selected participant
        MainMenu.btGatt = dConnect.connectGatt(getContext(), false, btleGattCallback);
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
