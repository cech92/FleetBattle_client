package com.mobile.fleetbattle;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class P2PGameService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_LAUNCH_GAME = "com.mobile.fleetbattle.LAUNCH_GAME";
    public static final String ACTION_SEND_ATTACK = "com.mobile.fleetbattle.SEND_ATTACK";
    public static final String ACTION_SEND_ATTACK_RESPONSE = "com.mobile.fleetbattle.SEND_ATTACK_RESPONSE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_ADDRESS = "go_host";
    public static final String EXTRAS_PORT = "go_port";

    public P2PGameService(String name) {
        super(name);
    }

    public P2PGameService() {
        super("P2PGameService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();
        System.out.println("INTENT: " + intent.getAction());
        System.out.println("ADDRESS: " + intent.getExtras().getString(EXTRAS_ADDRESS));
        Socket socket = null;

        if (intent.getAction().equals(ACTION_LAUNCH_GAME)) {
//            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_ADDRESS);

            int port = intent.getExtras().getInt(EXTRAS_PORT);
            OutputStream os = null;
            try {
                Log.d(P2PGameLauncher.TAG, "Opening client socket - ");
                socket = new Socket(host, port);
//                socket.bind(null);
//                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(P2PGameLauncher.TAG, "Client socket - " + socket.isConnected());

                os = socket.getOutputStream();

                byte[] b = new byte[1];
                b[0] = (byte)0;
                os.write(b);
                os.flush();

                os.close();
                socket.close();
                //OutputStream stream = socket.getOutputStream();
                //ContentResolver cr = context.getContentResolver();
                //DeviceDetailFragment.copyFile(is, stream);
                Log.d(P2PGameLauncher.TAG, "Client: Data written");
            } catch (IOException e) {
                Log.e(P2PGameLauncher.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

        }

        else if (intent.getAction().equals(ACTION_SEND_ATTACK)) {
//            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            final String host = intent.getExtras().getString(EXTRAS_ADDRESS);
            final int port = intent.getExtras().getInt(EXTRAS_PORT);
            OutputStream os = null;

            try {
                Log.d(P2PGameLauncher.TAG, "Opening client socket - ");
                socket = new Socket(host, port);

//                socket.bind(null);
//                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                System.out.println("1a");

                Log.d(P2PGameLauncher.TAG, "Client socket - " + socket.isConnected());

                System.out.println("2a");

                os = socket.getOutputStream();

                System.out.println("3a");

                byte[] b = intent.getExtras().getByteArray("bytes");
                os.write(b);
                os.flush();

                System.out.println("4a");

                os.close();
                socket.close();
                //OutputStream stream = socket.getOutputStream();
                //ContentResolver cr = context.getContentResolver();
                //DeviceDetailFragment.copyFile(is, stream);
                Log.d(P2PGameLauncher.TAG, "Client: Data written");
            } catch (IOException e) {
                Log.e(P2PGameLauncher.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

        }

        else if (intent.getAction().equals(ACTION_SEND_ATTACK_RESPONSE)) {
//            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_ADDRESS);
            int port = intent.getExtras().getInt(EXTRAS_PORT);
            OutputStream os = null;

            try {
                Log.d(P2PGameLauncher.TAG, "Opening client socket - ");
                socket = new Socket(host, port);

//                socket.bind(null);
//                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                System.out.println("1a");

                Log.d(P2PGameLauncher.TAG, "Client socket - " + socket.isConnected());

                System.out.println("2a");

                os = socket.getOutputStream();

                System.out.println("3a");

                byte[] b = intent.getExtras().getByteArray("bytes");
                os.write(b);
                os.flush();

                System.out.println("4a");

                os.close();
                socket.close();
                //OutputStream stream = socket.getOutputStream();
                //ContentResolver cr = context.getContentResolver();
                //DeviceDetailFragment.copyFile(is, stream);
                Log.d(P2PGameLauncher.TAG, "Client: Data written");
            } catch (IOException e) {
                Log.e(P2PGameLauncher.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

        }


//        if (intent.getAction().equals(ACTION_SEND_ATTACK_RESPONSE)) {
//            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
//            Socket socket = new Socket();
//            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
//            try {
//                Log.d(P2PGameLauncher.TAG, "Opening client socket - ");
//                socket.bind(null);
//                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
//
//                Log.d(P2PGameLauncher.TAG, "Client socket - " + socket.isConnected());
//
//                DataOutputStream mDataOutputStream = new DataOutputStream(socket.getOutputStream());
////
//                byte[] b = intent.getExtras().getByteArray("bytes");
//                String message = "ciao";
//                mDataOutputStream.write(b);
//                mDataOutputStream.writeUTF(message);
//                mDataOutputStream.flush();
//
////                OutputStream stream = socket.getOutputStream();
//////                ContentResolver cr = context.getContentResolver();
////                InputStream is = null;
////                //String stringToConvert = "This is my data";
////                //byte[] theByteArray = stringToConvert.getBytes();
////                stream.write(b);
////
////                try {
////                    System.out.println("BYTES: " + intent.getExtras().getByteArray("bytes").toString());
////                    is = new ByteArrayInputStream(intent.getExtras().getByteArray("bytes"));
////
////                } catch (Exception e) {
////                    Log.d(P2PGameLauncher.TAG, e.toString());
////                }
////                DeviceDetailFragment.copyFile(is, stream);
//
//                Log.d(P2PGameLauncher.TAG, "Client: Data written");
//            } catch (IOException e) {
//                Log.e(P2PGameLauncher.TAG, e.getMessage());
//            } finally {
//                if (socket != null) {
//                    if (socket.isConnected()) {
//                        try {
//                            socket.close();
//                        } catch (IOException e) {
//                            // Give up
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }
//        else if (intent.getAction().equals(ACTION_SEND_ATTACK)) {
//            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
//            Socket socket = new Socket();
//            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
//            try {
//                Log.d(P2PGameLauncher.TAG, "Opening client socket - ");
//                socket.bind(null);
//                socket.setReuseAddress(true);
//                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
//
//                Log.d(P2PGameLauncher.TAG, "Client socket - " + socket.isConnected());
//
//                DataOutputStream mDataOutputStream = new DataOutputStream(socket.getOutputStream());
////                InputStream is = null;
//
//                byte[] b = intent.getExtras().getByteArray("bytes");
//                mDataOutputStream.write(b);
//                mDataOutputStream.flush();
//
////                try {
////                    System.out.println("BYTES: " + intent.getExtras().getByteArray("bytes").toString());
////                    is = new ByteArrayInputStream(intent.getExtras().getByteArray("bytes"));
////                } catch (Exception e) {
////                    Log.d(P2PGameLauncher.TAG, e.toString());
////                }
////                DeviceDetailFragment.copyFile(is, mDataOutputStream);
//
//                Log.d(P2PGameLauncher.TAG, "Client: Data written");
//            } catch (IOException e) {
//                Log.e(P2PGameLauncher.TAG, e.getMessage());
//            } finally {
//                if (socket != null) {
//                    if (socket.isConnected()) {
//                        try {
//                            socket.close();
//                        } catch (IOException e) {
//                            // Give up
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }
//        else if (intent.getAction().equals(ACTION_LAUNCH_GAME)) {
//            //String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
//            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
//            Socket socket = new Socket();
//            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
//
//            try {
//                Log.d(P2PGameLauncher.TAG, "Opening client socket - ");
//                socket.bind(null);
//                socket.setReuseAddress(true);
//                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
//
//                Log.d(P2PGameLauncher.TAG, "Client socket - " + socket.isConnected());
//
//
//                DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
////                InputStream is = null;
//
//                byte[] b = new byte[1];
//                b[0] = (byte)0;
//                stream.write(b);
//
//                //socket.close();
//                stream.flush();
//
////                try {
////                    System.out.println("BYTES: " + intent.getExtras().getByteArray("bytes").toString());
////                    is = new ByteArrayInputStream(intent.getExtras().getByteArray("bytes"));
////                } catch (Exception e) {
////                    Log.d(P2PGameLauncher.TAG, e.toString());
////                }
////                DeviceDetailFragment.copyFile(is, stream);
//
//                Log.d(P2PGameLauncher.TAG, "Client: Data written");
//            } catch (IOException e) {
//                Log.e(P2PGameLauncher.TAG, e.getMessage());
//            } finally {
//                if (socket != null) {
//                    if (socket.isConnected()) {
//                        try {
//                            socket.close();
//                        } catch (IOException e) {
//                            // Give up
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }

//        }
    }
}