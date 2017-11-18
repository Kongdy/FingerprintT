package com.kongdy.fingerprintt;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * @author kongdy
 * @date 2017/11/18 13:42
 * @describe 安卓系统自带指纹识别实现
 **/
public class FingerprintActivity extends AppCompatActivity {

    private AppCompatButton acbtn_do_finger;
    private FingerprintManagerCompat fingerprintManagerCompat;
    private AlertDialog fingerDialog;

    private final static int REQUEST_FINGER_PERMISSION_CODE = 0x000001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);

        fingerprintManagerCompat = FingerprintManagerCompat.from(this);


        acbtn_do_finger = findViewById(R.id.acbtn_do_finger);

        // init dialog
        fingerDialog = new AlertDialog.Builder(FingerprintActivity.this)
                .setView(R.layout.layout_dialog_finger)
                .setCancelable(false).create();
        fingerDialog.setCanceledOnTouchOutside(false);

        acbtn_do_finger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkFingerEnvironment()) {
                    startFinger();
                }
            }
        });
    }

    private void startFinger() {
        if (fingerDialog.isShowing())
            fingerDialog.dismiss();
        fingerDialog.show();
//        try {
//            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
//            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
//            keyStore.load(null);
//            SecretKey secretKey = (SecretKey) keyStore.getKey("androiddebugkey", null);
//            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//            FingerprintManagerCompat.CryptoObject cryptoObject = new FingerprintManagerCompat.CryptoObject(cipher);
            fingerprintManagerCompat.authenticate(null, 0, new CancellationSignal(), new FingerprintManagerCompat.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errMsgId, CharSequence errString) {
                            showTip("指纹出错，请重新设置系统指纹");
                            fingerDialog.dismiss();
                        }

                        @Override
                        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                     //       showTip("show help");
                    //        fingerDialog.dismiss();
                        }

                        @Override
                        public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                            showTip("指纹授权成功");
                            fingerDialog.dismiss();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            showTip("指纹授权失败");
                            fingerDialog.dismiss();
                        }
                    }, null
            );
//        } catch (CertificateException | IOException |
//                InvalidKeyException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | NoSuchPaddingException e) {
//            showTip("指纹加密对象创建失败，错误:" + e.toString());
//            fingerDialog.dismiss();
//        }
    }

    private boolean checkFingerEnvironment() {
        if (!fingerprintManagerCompat.isHardwareDetected()) {
            showTip("您的设备暂不支持指纹识别功能");
            return false;
        }
        if (!fingerprintManagerCompat.hasEnrolledFingerprints()) {
            showTip("暂无已录入指纹，请先录入指纹");
            return false;
        }
        // check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_FINGERPRINT}, REQUEST_FINGER_PERMISSION_CODE);
            return false;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.USE_FINGERPRINT)) {
            showTip("请在设置中打开对app的指纹授予权限，并再次尝试");
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FINGER_PERMISSION_CODE) {
            if (checkFingerEnvironment()) {
                startFinger();
            }
        }
    }

    private void showTip(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
