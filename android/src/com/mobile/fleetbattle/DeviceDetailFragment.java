package com.mobile.fleetbattle;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobile.fleetbattle.DeviceListFragment.DeviceActionListener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

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
    private final static String p2pInt = "p2p-p2p0";
    public static final String IP_SERVER = "192.168.49.1";
    public static int PORT = 8988;
    private static boolean server_running = false;

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
                        serviceIntent.putExtra(P2PGameService.EXTRAS_ADDRESS,
                                info.groupOwnerAddress.getHostAddress());
                        serviceIntent.putExtra(P2PGameService.EXTRAS_PORT, PORT);
                        getActivity().startService(serviceIntent);
                        ((P2PGameLauncher) getActivity()).launchGame(1);
                    }
                });

        return mContentView;
    }

    private static String getDottedDecimalIP(byte[] ipAddr) {
        String ipAddrStr = "";
        for (int i=0; i<ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i]&0xFF;
        }
        return ipAddrStr;
    }

    public static String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();

                    String iface = intf.getName();
                    if(iface.matches(".*" +p2pInt+ ".*")){
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return getDottedDecimalIP(inetAddress.getAddress());
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("", "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            Log.e("", "getLocalIPAddress()", ex);
        }
        return null;
    }

    public static String getIPFromMac(String MAC) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {

                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    // Basic sanity check
                    String device = splitted[5];
                    if (device.matches(".*" +p2pInt+ ".*")){
                        String mac = splitted[3];
                        if (mac.matches(MAC)) {
                            return splitted[0];
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("RESULT CODE: " + resultCode);
        System.out.println("REQUEST CODE: " + requestCode);
        System.out.println("DATA: " + data);
        String localIP = getLocalIPAddress();
        // Trick to find the ip in the file /proc/net/arp
        String client_mac_fixed = new String(device.deviceAddress).replace("99", "19");
        String clientIP = getIPFromMac(client_mac_fixed);

        Uri uri = data.getData();
        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
        statusText.setText("Sending: " + uri);
        Log.d(P2PGameLauncher.TAG, "Intent----------- " + uri);
        Intent serviceIntent = new Intent(getActivity(), P2PGameService.class);
        serviceIntent.setAction(P2PGameService.ACTION_LAUNCH_GAME);
        //serviceIntent.putExtra(P2PGameService.EXTRAS_FILE_PATH, uri.toString());
        if(localIP.equals(IP_SERVER)){
            serviceIntent.putExtra(P2PGameService.EXTRAS_ADDRESS, clientIP);
        }else{
            serviceIntent.putExtra(P2PGameService.EXTRAS_ADDRESS, IP_SERVER);
        }
        serviceIntent.putExtra(P2PGameService.EXTRAS_PORT, PORT);
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

        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);

//        if (!server_running){
//            new ServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text)).execute();
//            server_running = true;
//        }

        if (info.groupFormed && info.isGroupOwner) {
            new ServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
                    .execute();
            server_running = true;
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
    public static class ServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;
        private ServerSocket mServerSocket;
        private Socket mSocket;
        /**
         * @param context
         * @param statusText
         */
        public ServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected String doInBackground(Void... params) {
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
                        ((P2PGameLauncher) context).launchGame(0);
                    } else if ((int) b[0] == 1)
                        ((P2PGameLauncher) context).receiveAttack(b);
                    else if ((int) b[0] == 2)
                        ((P2PGameLauncher) context).responseAttack(b);

                    //OutputStream os = client.getOutputStream();
                    //os.write(ret);

                    //copyFile(inputStream, os);

//                    OutputStream outputStream = client.getOutputStream();

                    serverSocket.close();
                    inputStream.close();
                    buffer.close();
                    server_running = false;
                    return "";
                } catch (IOException e) {
                    Log.e(P2PGameLauncher.TAG, e.getMessage());
                    return null;
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
//        serviceIntent.putExtra(P2PGameService.EXTRAS_GROUP_OWNER_ADDRESS,
//                info.groupOwnerAddress.getHostAddress());
        String localIP = getLocalIPAddress();
        String client_mac_fixed = new String(device.deviceAddress).replace("99", "19");
        String clientIP = getIPFromMac(client_mac_fixed);
        if(localIP.equals(IP_SERVER)){
            serviceIntent.putExtra(P2PGameService.EXTRAS_ADDRESS, clientIP);
        }else{
            serviceIntent.putExtra(P2PGameService.EXTRAS_ADDRESS, IP_SERVER);
        }
        serviceIntent.putExtra(P2PGameService.EXTRAS_PORT, PORT);
        getActivity().startService(serviceIntent);
    }

    public void sendAttackResponse(byte[] b, int turn) {
        if (turn == 0) {
            Intent serviceIntent = new Intent(getActivity(), P2PGameService.class);
             serviceIntent.setAction(P2PGameService.ACTION_SEND_ATTACK_RESPONSE);
            serviceIntent.putExtra("bytes", b);
            serviceIntent.putExtra("turn", turn);
            System.out.println("ADDRESS 2: " + info.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(P2PGameService.EXTRAS_ADDRESS,
                    clientAddress);
            serviceIntent.putExtra(P2PGameService.EXTRAS_PORT, PORT);
            getActivity().startService(serviceIntent);
        }
        else {
            Intent serviceIntent = new Intent(getActivity(), P2PGameService.class);
            serviceIntent.setAction(P2PGameService.ACTION_SEND_ATTACK_RESPONSE);
            serviceIntent.putExtra("bytes", b);
            serviceIntent.putExtra("turn", turn);
            serviceIntent.putExtra(P2PGameService.EXTRAS_ADDRESS,
                    info.groupOwnerAddress.getHostAddress());
            serviceIntent.putExtra(P2PGameService.EXTRAS_PORT, PORT);
            getActivity().startService(serviceIntent);
        }
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            if (inputStream != null) {
                while ((len = inputStream.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
                inputStream.close();
            }
            out.close();
        } catch (IOException e) {
            Log.d(P2PGameLauncher.TAG, e.toString());
            return false;
        }
        return true;
    }

}