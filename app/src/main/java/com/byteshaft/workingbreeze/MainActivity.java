package com.byteshaft.workingbreeze;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.security.Signature;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {

    WebView webView;
    String URL = "https://online.standardchartered.com/pk/breeze/#brz-login";
    private BiometricPrompt biometricPrompt;
    Signature signature;
    ProgressDialog progressDialog;
    String js = "javascript:document.getElementById('username').value='android';" +
             "javascript:document.getElementById('password').value='android';";
    String loginCLick = "javascript:document.getElementById('button_login').click()";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0");
        webView.setWebViewClient(new CustomWebViewClient());
        webView.loadUrl(URL);
        showProgressDialog(MainActivity.this, "Please wait...");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showProgressDialog(Context context, String message) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void displayBiometricDialog() {
        biometricPrompt = new BiometricPrompt.Builder(MainActivity.this)
                .setDescription("Authorize your Fingerprint")
                .setTitle("Fingerprint Authentication")
                .setNegativeButton("Cancel", getMainExecutor(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).build();
        CancellationSignal cancellationSignal = getCancellationSignal();
        BiometricPrompt.AuthenticationCallback authenticationCallback = getAuthenticationCallback();

            System.out.println("Working");
            biometricPrompt.authenticate(new BiometricPrompt.CryptoObject(signature), cancellationSignal, getMainExecutor(), authenticationCallback);

    }

    private CancellationSignal getCancellationSignal() {
        // With this cancel signal, we can cancel biometric prompt operation
        CancellationSignal cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                //handle cancel result
                Log.i(TAG, "Canceled");
            }
        });
        return cancellationSignal;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private BiometricPrompt.AuthenticationCallback getAuthenticationCallback() {
        // Callback for biometric authentication result
        return new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                super.onAuthenticationHelp(helpCode, helpString);
            }
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                Log.i(TAG, "onAuthenticationSucceeded");
                super.onAuthenticationSucceeded(result);
                signature = result.getCryptoObject().getSignature();

            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        };
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return super.shouldOverrideUrlLoading(view, url);
        }

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public void onPageFinished(final WebView view, String url) {
            super.onPageFinished(view, url);
            dismissProgressDialog();
            ensureUsernameVisible(view);
            displayBiometricDialog();
        }

        private void ensureUsernameVisible(final WebView view) {
            view.evaluateJavascript("(function() { var element = document.getElementById('username'); return element.id; })();", value -> {
                if (value.equals("null")) {
                    new Handler().postDelayed(() -> ensureUsernameVisible(webView), 100);
                } else {
                    view.evaluateJavascript(js, value1 -> ensureSubmitClickableAndSend(view));
                }
            });
        }

        private void ensureSubmitClickableAndSend(WebView view) {
            view.evaluateJavascript(loginCLick, resultCallback -> {
                view.evaluateJavascript("javascript:document.getElementById('okBtn').click()", value1 -> {
                    new Handler().postDelayed(() -> view.evaluateJavascript("(function() { var element = document.getElementById('button_login'); return element.className; })();", value2 -> {
                        System.out.println(value2);
                        view.evaluateJavascript(loginCLick, value3 -> {

                        });
                    }), 2000);
                });
            });
        }

        private void clickLoginAgain(WebView view) {

        }
    }
}

