package com.mobile.fleetbattle;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import com.mobile.fleetbattle.DeviceListFragment.DeviceActionListener;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */

public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    private static String clientAddress;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
                );
                ((DeviceActionListener) getActivity()).connect(config);

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent serviceIntent = new Intent(getActivity(), P2PGameService.class);
                        serviceIntent.setAction(P2PGameService.ACTION_LAUNCH_GAME);
                        //serviceIntent.putExtra(P2PGameService.EXTRAS_FILE_PATH, uri.toString());
                        serviceIntent.putExtra(P2PGameService.EXTRAS_GROUP_OWNER_ADDRESS,
                                info.groupOwnerAddress.getHostAddress());
                        serviceIntent.putExtra(P2PGameService.EXTRAS_GROUP_OWNER_PORT, 8988);
                        getActivity().startService(serviceIntent);
                        ((P2PGameLauncher) getActivity()).launchGame(1);
                    }
                });

        return mContentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has picked an image. Transfer it to group owner i.e peer using
        // P2PGameService.
        Uri uri = data.getData();
        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
        statusText.setText("Sending: " + uri);
        Log.d(P2PGameLauncher.TAG, "Intent----------- " + uri);
        Intent serviceIntent = new Intent(getActivity(), P2PGameService.class);
        serviceIntent.setAction(P2PGameService.ACTION_LAUNCH_GAME);
        //serviceIntent.putExtra(P2PGameService.EXTRAS_FILE_PATH, uri.toString());
        serviceIntent.putExtra(P2PGameService.EXTRAS_GROUP_OWNER_ADDRESS,
                info.groupOwnerAddress.getHostAddress());
        serviceIntent.putExtra(P2PGameService.EXTRAS_GROUP_OWNER_PORT, 8988);
        getActivity().startService(serviceIntent);
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());

        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed && info.isGroupOwner) {
            new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
                    .execute();
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case, we enable the
            // get file button.
            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
                    .getString(R.string.client_text));
        }

        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    /**
     * Updates the UI with device data
     *
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;
        private ServerSocket mServerSocket;
        private Socket mSocket;
        /**
         * @param context
         * @param statusText
         */
        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected String doInBackground(Void... params) {
            if (clientAddress == null) {
                try {
                    ServerSocket serverSocket = new ServerSocket(8988);
                    Log.d(P2PGameLauncher.TAG, "Server: Socket opened");
                    Socket client = serverSocket.accept();

                    Log.d(P2PGameLauncher.TAG, "Server: connection done");
                    InputStream inputStream = client.getInputStream();

                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                    int nRead;
                    byte[] data = new byte[1024];

                    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }

                    buffer.flush();

                    byte[] b = buffer.toByteArray();
                    //copyFile(inputstream, new FileOutputStream(f));
                    for (byte b1 : b) {
                        System.out.println((int) b1);
                    }
                    if ((int) b[0] == 0) {
                        System.out.println("ADDRESS: " + client.getInetAddress());
                        clientAddress = client.getInetAddress().toString().substring(1);
                        System.out.println("ADDRESS: " + clientAddress);
                        ((P2PGameLauncher) context).launchGame(0);
                    } else if ((int) b[0] == 1)
                        ((P2PGameLauncher) context).receiveAttack(b);
                    else if ((int) b[0] == 2)
                        ((P2PGameLauncher) context).responseAttack(b);
//                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
//                StringBuilder total = new StringBuilder();
//                String line;
//                while ((line = r.readLine()) != null) {
//                    total.append(line).append('\n');
//                }
                    //System.out.println("INPUTSTREAM: " + total.toString());
                    serverSocket.close();
                    //return f.getAbsolutePath();
                    return "";
                } catch (IOException e) {
                    Log.e(P2PGameLauncher.TAG, e.getMessage());
                    return null;
                }
            } else {
                try {
                    ServerSocket serverSocket = new ServerSocket(8988);

                    Socket ss= serverSocket.accept();
                    OutputStream os= ss.getOutputStream();

                    System.out.println("OUTPUT " + os.toString());
                    return os.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                    return  null;
                }
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            System.out.println("RESULT: " + result);
            if (result != null) {
                statusText.setText("Command received - " + result);
            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            statusText.setText("Opening a server socket");
        }

    }

    public void sendAttack(byte[] b) {
        Intent serviceIntent = new Intent(getActivity(), P2PGameService.class);
        serviceIntent.setAction(P2PGameService.ACTION_SEND_ATTACK);
        serviceIntent.putExtra("bytes", b);
        serviceIntent.putExtra(P2PGameService.EXTRAS_GROUP_OWNER_ADDRESS,
                info.groupOwnerAddress.getHostAddress());
        serviceIntent.putExtra(P2PGameService.EXTRAS_GROUP_OWNER_PORT, 8988);
        getActivity().startService(serviceIntent);
    }

    public void sendAttackResponse(byte[] b, int turn) {
        if (turn == 0) {
            Intent serviceIntent = new Intent(getActivity(), P2PGameService.class);
            serviceIntent.setAction(P2PGameService.ACTION_SEND_ATTACK_RESPONSE);
            serviceIntent.putExtra("bytes", b);
            serviceIntent.putExtra("turn", turn);
            System.out.println("ADDRESS 2: " + info.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(P2PGameService.EXTRAS_GROUP_OWNER_ADDRESS,
                    clientAddress);
            serviceIntent.putExtra(P2PGameService.EXTRAS_GROUP_OWNER_PORT, 8988);
            getActivity().startService(serviceIntent);
        }
        else {
            Intent serviceIntent = new Intent(getActivity(), P2PGameService.class);
            serviceIntent.setAction(P2PGameService.ACTION_SEND_ATTACK_RESPONSE);
            serviceIntent.putExtra("bytes", b);
            serviceIntent.putExtra("turn", turn);
            serviceIntent.putExtra(P2PGameService.EXTRAS_GROUP_OWNER_ADDRESS,
                    info.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(P2PGameService.EXTRAS_GROUP_OWNER_PORT, 8988);
            getActivity().startService(serviceIntent);
        }
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(P2PGameLauncher.TAG, e.toString());
            return false;
        }
        return true;
    }

}