package practice.application.graphviewtutorial1;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;



class DownloadFromURL extends AsyncTask<String, String, String> {


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    private Context context;

    public DownloadFromURL(Context context) {
        this.context = context;
    }


    @Override
    protected String doInBackground(String... f_url) {
        int count;
        try {

            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            } };

            // trust all hosts and certs
            final SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);


            URL url = new URL(f_url[0]);

            URLConnection c = (HttpsURLConnection)url.openConnection();
            c.connect();

            int lenghtOfFile = c.getContentLength();

            //set streams
            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            OutputStream output = new FileOutputStream("/Android/Data/CSE535_ASSIGNMENT2_DOWN");

            byte data[] = new byte[10240];

            long total = 0;

            while ((count = input.read(data)) != -1) {

                total += count;
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return null;
    }


    protected void onProgressUpdate(String... progress) {

    }


    @Override
    protected void onPostExecute(String file_url) {

        Toast.makeText(context, "Download Done!", Toast.LENGTH_LONG).show();
    }

}