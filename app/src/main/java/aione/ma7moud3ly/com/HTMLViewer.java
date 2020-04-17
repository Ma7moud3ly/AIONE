package aione.ma7moud3ly.com;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;

import androidx.appcompat.app.AppCompatActivity;


public class HTMLViewer extends AppCompatActivity {
    private WebView webView;
    private TextView urlView;
    private final String bootstrap = MainActivity.data_dir + "/bootstrap";
    private final String localhost = MainActivity.data_dir + "/localhost";
    private String name = "";
    private String directory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MainActivity.script_list.isEmpty()) MainActivity.init_list(this);
        setContentView(R.layout.html_viewer);
        urlView = findViewById(R.id.url);
        webView = findViewById(R.id.web_view);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(webViewClient);
        webView.clearCache(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        File f = new File(localhost);
        if (!f.exists()) f.mkdir();

        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        File page = new File(path);
        name = page.getName();
        directory = page.getParent();
        String run = intent.getStringExtra("run");
        if (run.equals("html")) webView.loadUrl("file:///" + path);
        else php(path);
    }

    private void php(String path) {
        String s1 = "export LD_LIBRARY_PATH=" + bootstrap + "/lib";
        String s2 = "PATH=$PATH:" + bootstrap + "/bin";
        String[] commands = {"sh", "-c", s1 + ";" + s2 + ";php-cgi -f " + path};
        new ShellCommands(commands, response).eval(false);
    }


    private ShellResponse response = new ShellResponse() {
        @Override
        public void onSuccess(final String s) {
            Toast.makeText(getApplicationContext(), "response", Toast.LENGTH_LONG).show();
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.reload();
                    if (s.contains("Parse error:"))
                        webView.loadData("<font color=\"red\">" + s + "</font>", "text/html", "utf-8");
                    else {
                        String file = localhost + "/" + "index.html";
                        Scripts.write(file, s);
                        webView.loadUrl("file:///" + file);
                    }

                }
            });
        }

        @Override
        public void onError(String s) {
            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
        }

        @Override
        public void set(HashMap<String, String> s) {
        }

        @Override
        public void onScript() {
        }
    };


    private final WebViewClient webViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Uri url = request.getUrl();
                    String path = url.getPath();
                    if (path.endsWith("php") && new File(path).exists()) {
                        php(path);
                        return false;
                    }
                    path = path.replace(localhost, directory);
                    path = path.replace("index.html", name);
                    String query = url.getQuery();
                    query = query.replace("&", " ");
                    path = path + " " + query;
                    php(path);
                    return false;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }


        @Override
        public void onPageStarted(WebView view, String u, Bitmap favicon) {
            int i;
            if ((i = u.indexOf("localhost/")) != -1) {
                u = u.substring(i);
                u = u.replace("index.html", name);
                urlView.setText(u);
            } else {
                urlView.setText("localhost/" + name);
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                webView.loadData("<font color=\"red\">" + error.getErrorCode() + "<br>" +
                        error.getDescription().toString() + "</font>", "text/html", "utf-8");
                urlView.setText("");
            } else*/
            super.onReceivedError(view, request, error);
        }
    };

    @Override
    public void onBackPressed() {
        /*if (webView.canGoBack()) {
            webView.goBack();
        } else*/
        super.onBackPressed();
    }

}