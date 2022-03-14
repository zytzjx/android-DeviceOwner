package com.fd.deviceadb;

import android.os.Environment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import android.os.AsyncTask;

public class FDLog {
    public static final int DISABLED = -1;
    public static final int ERROR = 0;
    public static final int INFO = 1;
    public static final int DEBUG = 2;
    public static boolean LOG_LEVEL = true;//Config.logging;

    private static final String LOG_FILE = "FDOpenAdbLog.txt";

    private static boolean FILE_DEBUG = false;
    private static boolean UDP_DEBUG = true;

    private static String TAG = "FDOpenAdb";

    private static final long SIZE_LIMIT = 5 * 1024 * 1024;

    public static boolean getLOG_LEVEL() {
        return LOG_LEVEL;
    }

    public static void setLOG_LEVEL(boolean log_LEVEL) {
        LOG_LEVEL = log_LEVEL;
    }

    public static void setTag(String tagName) {
        TAG = tagName;
    }

    public static boolean isFILE_DEBUG_Enabled() {
        return FILE_DEBUG;
    }

    public static void setFILE_DEBUG(boolean file_Debug) {
        FILE_DEBUG = file_Debug;
    }

    private static final  int port = 8888;
    private static final  String ip = "192.168.75.8";


    public static class UDPClient extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params)
        {
            String str = params[0];
            DatagramSocket ds = null;
            try
            {
                ds = new DatagramSocket();
                InetAddress sendAddress = InetAddress.getByName(ip);
                DatagramPacket dp;
                dp = new DatagramPacket(str.getBytes(), str.length(), sendAddress, port);
                ds.send(dp);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return "";
        }
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);
        }
    }

    public static class ClientSend implements Runnable {
        private  volatile  String s;
        public ClientSend(String ss){
            s = ss;
        }
        @Override
        public void run() {
            if (TextUtils.isEmpty(s)) {
                return;
            }
            try {
                DatagramSocket udpSocket = new DatagramSocket(port);
                InetAddress serverAddr = InetAddress.getByName(ip);
                byte[] buf = s.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, port);
                udpSocket.send(packet);
            } catch (SocketException e) {
                Log.e("Udp:", "Socket Error:", e);
            } catch (IOException e) {
                Log.e("Udp Send:", "IO Error:", e);
            }
        }

    }
    public static  void SendLog(String s){
        if (!UDP_DEBUG) return;
        if (TextUtils.isEmpty(s)) {
            return;
        }
        new UDPClient().execute(s);
    }



    public static void d(String message) {
        if (LOG_LEVEL)
            Log.d(TAG, "Debug: " + message);

        if (FILE_DEBUG) {
            writeToFile(TAG, message);
        }

        SendLog(message);
    }

    public static void e(String message) {
        if (LOG_LEVEL)
            Log.d(TAG, "Error: " + message);

        if (FILE_DEBUG) {
            writeToFile(TAG, message);
        }
        SendLog(message);
    }

    public static void i(String message) {
        if (LOG_LEVEL)
            Log.d(TAG, "Info: " + message);

        SendLog(message);
    }

    public static void d(String tag, String message) {
        if (LOG_LEVEL)
            Log.d(TAG, "Debug: " + message);

        if (FILE_DEBUG) {
            writeToFile(tag, message);
        }
        SendLog(message);
    }

    public static void e(String tag, String message) {
        if (LOG_LEVEL)
            Log.d(tag, "Error: " + message);

        if (FILE_DEBUG) {
            writeToFile(tag, message);
        }
        SendLog(message);
    }

    public static void i(String tag, String message) {
        if (LOG_LEVEL)
            Log.d(tag, "Info: " + message);

        if (FILE_DEBUG) {
            writeToFile(tag, message);
        }
        SendLog(message);
    }

    public static void writeToFile(String TAG, String msg) {
        try {
            String state = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                FileOutputStream dio = null;

                File file = new File(Environment.getExternalStorageDirectory(),
                        LOG_FILE);

                if (file.length() > SIZE_LIMIT) {

                    if (LOG_LEVEL) {
                        Log.d(TAG, "Log file is > " + SIZE_LIMIT
                                + " bytes, backup it and start new again.");
                    }
                }

                if (file != null) {
                    StringBuffer sf = new StringBuffer();
                    sf.append(TAG + "\t");
                    long time = System.currentTimeMillis();
                    sf.append(DateFormat.format("MMM dd, yyyy h:mmaa", time));
                    sf.append("\t" + msg + "\n");
                    try {
                        dio = new FileOutputStream(file, true);
                        dio.write(sf.toString().getBytes());
                        dio.close();
                        dio = null;

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {

                        if (dio != null) {
                            try {
                                dio.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (LOG_LEVEL) {
                Log.d(TAG, "Exception in writeToFile(): " + e.getMessage());
            }
        }
    }

    public static void copy(File src, File dst) {
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst, false);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception e) {
            if (LOG_LEVEL) {
                Log.d(TAG, "Exception in copy(): " + e.getMessage());
            }
        }
    }
}
