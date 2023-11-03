package com.example.aplikasipresensizmg;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.aplikasipresensizmg.helper.sharedpreferences.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Animation fade_in, fade_out_permanent;
    TextView nama_tim,tv_kirim_ulang;
    RelativeLayout btn_kirim_foto_presensi, area_keterangan_sudah_presensi;
    ImageView preview_foto_presensi,img_hapus_foto,btn_logout;
    ConstraintLayout ly_ambil_foto_presensi, ly_sudah_berhasil_upload,ly_utama;
    private RequestQueue rQueue;
    Uri uriGambarAbsen;
    ProgressBar progress_kirim_absen, pb_logout;
    TextView label_kirim_absen;
    static String tag_json_obj = "json_obj_req";

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 101;
    private static final int COARSE_LOCATION_PERMISSION_REQUEST_CODE = 102;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 103;
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 104;
    private static final int TAKE_CAMERA_REQUEST_CODE = 200;


    private static final String URL_KIRIM_ABSENSI = BankURL.URL_KIRIM_ABSENSI;
    private static final String URL_LOGOUT = BankURL.URL_LOGOUT;



    /*START STATE AREA TENGAH ACTIVITY*/
    public static  int CURRENT_STATE_AREA_TENGAH = 0;
    private static final int STATE_AREA_TENGAH_SHOW_AREA_UPLOAD = 0;
    private static final int STATE_AREA_TENGAH_SHOW_PREVIEW_FOTO = 1;
    private static final int STATE_AREA_TENGAH_SHOW_BERHASIL_ABSEN = 2;
    /*END STATE AREA TENGAH ACTIVITY*/

    /*START STATE AREA BAWAH ACTIVITY*/
    public static  int CURRENT_STATE_AREA_BAWAH = 0;
    private static final int STATE_AREA_BAWAH_TAMPIL_TOMBOL_KIRIM_ABSEN = 0;
    private static final int STATE_AREA_BAWAH_TAMPIL_TULISAN_SUDAH_ABSEN = 1;
    /*END STATE AREA BAWAH ACTIVITY*/

    /*START STATE BUTTON KIRIM*/
    int CURRENT_STATE_KIRIM_ABSEN = 0;
    private static final int STATE_NOTALLOWED_KIRIM_ABSEN = 0;
    private static final int STATE_IDLE_KIRIM_ABSEN = 1;
    private static final int STATE_LOADING_KIRIM_ABSEN = 2;
    /*END STATE BUTTON KIRIM*/

    /*START ALL PERMISSION STATUS*/
    boolean isPermissionCoarseLocationGranted = false;
    boolean isPermissionFineLocationGranted = false;
    boolean isPermissionReadExternalStorageGranted = false;
    boolean isPermissionWriteExternalStorageGranted = false;
    boolean isPermissionCameraGranted = false;
    /*END ALL PERMISSION STATUS*/





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.include_activity_main);
        initAnimation();
        findViewByIdAllComponent();
        setListenerAllComponent();
        askFineLocationPermission();
        mekanismeCheckSemuaPermission();
        setNameFromPref();
    }

    private void mekanismeCheckSemuaPermission(){
        Log.d("18_agustus_2022","mekanismeCheckSemuaPermission()");
        if(checkCoarseLocationPermission() == true)
        {
            isPermissionCoarseLocationGranted = true;
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Aktifkan izin lokasimu terlebih dahulu",Toast.LENGTH_SHORT).show();
            askCoarseLocationPermission();
        }

        if(checkFineLocationPermission() == true)
        {
            isPermissionFineLocationGranted = true;
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Aktifkan izin lokasimu terlebih dahulu",Toast.LENGTH_SHORT).show();
            askFineLocationPermission();
        }

        if(checkWriteExternalStoragePermission() == true)
        {
            isPermissionWriteExternalStorageGranted = true;
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Aktifkan izin penyimpananmu terlebih dahulu",Toast.LENGTH_SHORT).show();
            askWriteExternalStoragePermission();
        }

        if(checkReadExternalStoragePermission() == true)
        {
            isPermissionReadExternalStorageGranted = true;
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Aktifkan izin penyimpananmu terlebih dahulu",Toast.LENGTH_SHORT).show();
            askReadExternalStoragePermission();
        }

        if(checkCameraPermission() == true)
        {
            isPermissionCameraGranted = true;
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Aktifkan izin kameramu terlebih dahulu",Toast.LENGTH_SHORT).show();
            askCameraPermission();
        }
    }

    private void initAnimation(){
        fade_in = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        fade_out_permanent = AnimationUtils.loadAnimation(this,R.anim.fade_out_permanent);
    }

    private void findViewByIdAllComponent(){
        nama_tim = findViewById(R.id.nama_tim);
        btn_kirim_foto_presensi = findViewById(R.id.btn_kirim_foto_presensi);
        preview_foto_presensi = findViewById(R.id.preview_foto_presensi);
        ly_ambil_foto_presensi = findViewById(R.id.ly_ambil_foto_presensi);
        progress_kirim_absen = findViewById(R.id.progress_kirim_absen);
        label_kirim_absen = findViewById(R.id.label_kirim_absen);
        ly_sudah_berhasil_upload = findViewById(R.id.ly_sudah_berhasil_upload);
        area_keterangan_sudah_presensi = findViewById(R.id.area_keterangan_sudah_presensi);
        tv_kirim_ulang = findViewById(R.id.tv_kirim_ulang);
        img_hapus_foto = findViewById(R.id.img_hapus_foto);
        btn_logout = findViewById(R.id.btn_logout);
        pb_logout = findViewById(R.id.pb_main_activity);
        ly_utama = findViewById(R.id.ly_utama);
    }

    private void setListenerAllComponent(){
        ly_ambil_foto_presensi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mekanismeAmbilFotoLewatKamera();
            }
        });
        btn_kirim_foto_presensi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mekanismeKirimAbsen();
            }
        });
        tv_kirim_ulang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetStatePresensi();
            }
        });
        img_hapus_foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetStatePresensi();
            }
        });
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopUpLogout();
            }
        });
    }



    private void resetStatePresensi(){
        uriGambarAbsen = null;
        toggleShowAreaTengah(STATE_AREA_TENGAH_SHOW_AREA_UPLOAD);
        toggleShowAreaBawah(STATE_AREA_BAWAH_TAMPIL_TOMBOL_KIRIM_ABSEN);
    }

    private void mekanismeKirimAbsen(){
        //hanya bisa mengirim ketika button kirim absen sedang idle
        if(CURRENT_STATE_KIRIM_ABSEN == STATE_IDLE_KIRIM_ABSEN)
            kirimAbsen(SharedPreferencesHelper.Companion.read(SharedPreferencesHelper.ACCESS_TOKEN,""),"absen",uriGambarAbsen,getBSSID());
    }


    private void mekanismeAmbilFotoLewatKamera(){

        Log.d("18_agustus_2022","mekanismeAmbilFotoLewatKamera()");
       if(checkSemuaPermissionGranted() == false)
       {
           mekanismeCheckSemuaPermission();

       }
       else if(checkGpsEnable() == false)
       {
           Toast.makeText(getApplicationContext(),"Aktifkan GPS mu terlebih dahulu",Toast.LENGTH_SHORT).show();
       }
       else if(checkWifiEnable() == false)
       {
           Toast.makeText(getApplicationContext(),"Aktifkan WIFI mu terlebih dahulu",Toast.LENGTH_SHORT).show();
       }
       else
       {
           Log.d("18_agustus_2022","mekanismeAmbilFotoLewatKamera() if");
           Log.d("18_agustus_2022","isPermissionCameraGranted: "+isPermissionCameraGranted);
           launchIntentTakePictureFromCamera();
       }

    }

    private boolean checkSemuaPermissionGranted(){
        boolean hasil = true;
        if(isPermissionCameraGranted == false)
        {
            hasil = false;
        }
        else if(isPermissionReadExternalStorageGranted == false)
        {
            hasil = false;
        }
        else if(isPermissionWriteExternalStorageGranted == false)
        {
            hasil = false;
        }
        else if(isPermissionFineLocationGranted == false)
        {
            hasil = false;
        }
        else if(isPermissionCoarseLocationGranted == false)
        {
            hasil = false;
        }
        return hasil;
    }

    private boolean checkCameraPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            return false;
        }
        else
        {
          return true;
        }
    }
    private void askCameraPermission(){
        Log.d("18_agustus_2022","ask camera permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_REQUEST_CODE);
            //launchIntentTakePictureFromCamera();
    }

    private boolean checkFineLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    private void askFineLocationPermission(){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},FINE_LOCATION_PERMISSION_REQUEST_CODE);
    }

    private boolean checkCoarseLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    private void askCoarseLocationPermission(){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},COARSE_LOCATION_PERMISSION_REQUEST_CODE);
    }


    private boolean checkWriteExternalStoragePermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    private void askWriteExternalStoragePermission(){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
    }

    private boolean checkReadExternalStoragePermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    private void askReadExternalStoragePermission(){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
    }

    private void launchIntentTakePictureFromCamera(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, TAKE_CAMERA_REQUEST_CODE);
        } catch (Exception e) {
            // display error state to the user
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            uriGambarAbsen = getUri(MainActivity.this, imageBitmap);
            toggleShowAreaTengah(STATE_AREA_TENGAH_SHOW_PREVIEW_FOTO);
            preview_foto_presensi.setImageBitmap(imageBitmap);
        }
    }

    private  Uri getUri(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    private void show_img_hapus_foto(){
        img_hapus_foto.setVisibility(View.VISIBLE);
    }

    private void hide_img_hapus_foto(){
        img_hapus_foto.setVisibility(View.GONE);
    }

    private void deleteUriInLocalStorage(Uri uri)
    {
        getContentResolver().delete(uri, null, null);
    }

    private void toggleShowAreaTengah(int PARAM_SHOW)
    {

        switch(PARAM_SHOW) {
            case STATE_AREA_TENGAH_SHOW_AREA_UPLOAD:{
                hide_img_hapus_foto();
            hideAreaTengahPreviewFotoPresensi();
            hideAreaTengahBerhasilAbsen();
                showAreaTengahAreaUpload();
                toggleStateBtnKirimAbsen(STATE_NOTALLOWED_KIRIM_ABSEN);
                CURRENT_STATE_AREA_TENGAH = STATE_AREA_TENGAH_SHOW_AREA_UPLOAD;
            break;
        }
            case STATE_AREA_TENGAH_SHOW_PREVIEW_FOTO:{
                hideAreaTengahBerhasilAbsen();
            hideAreaTengahAreaUpload();
                show_img_hapus_foto();
            showAreaTengahPreviewFotoPresensi();
            toggleStateBtnKirimAbsen(STATE_IDLE_KIRIM_ABSEN);
                CURRENT_STATE_AREA_TENGAH = STATE_AREA_TENGAH_SHOW_PREVIEW_FOTO;
            break;
        }
            case STATE_AREA_TENGAH_SHOW_BERHASIL_ABSEN:{
                hideAreaTengahAreaUpload();
                hide_img_hapus_foto();
                hideAreaTengahPreviewFotoPresensi();
                showAreaTengahBerhasilAbsen();
                toggleStateBtnKirimAbsen(STATE_NOTALLOWED_KIRIM_ABSEN);
                CURRENT_STATE_AREA_TENGAH = STATE_AREA_TENGAH_SHOW_BERHASIL_ABSEN;
                break;
            }

    }

    }

    private void toggleShowAreaBawah(int PARAM_SHOW)
    {

        switch(PARAM_SHOW) {
            case STATE_AREA_BAWAH_TAMPIL_TOMBOL_KIRIM_ABSEN:{
                showAreaBawahTombolKirimAbsen();
                hideAreaBawahTulisanSudahAbsen();
                CURRENT_STATE_AREA_BAWAH = STATE_AREA_BAWAH_TAMPIL_TOMBOL_KIRIM_ABSEN;
                break;
            }
            case STATE_AREA_BAWAH_TAMPIL_TULISAN_SUDAH_ABSEN:{
                hideAreaBawahTombolKirimAbsen();
                showAreaBawahTulisanSudahAbsen();
                CURRENT_STATE_AREA_BAWAH = STATE_AREA_BAWAH_TAMPIL_TULISAN_SUDAH_ABSEN;
                break;
            }
        }

    }

    private String getBSSID(){
        String bssid = "";
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo;

                wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                    bssid = wifiInfo.getBSSID();

                }
        return bssid;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERMISSION_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                isPermissionCameraGranted = true;
            }
        }
        if(requestCode == FINE_LOCATION_PERMISSION_REQUEST_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                isPermissionFineLocationGranted = true;
            }
        }
        if(requestCode == COARSE_LOCATION_PERMISSION_REQUEST_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                isPermissionCoarseLocationGranted = true;
            }
        }
        if(requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                isPermissionWriteExternalStorageGranted = true;
            }
        }
        if(requestCode == READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                isPermissionReadExternalStorageGranted = true;
            }
        }
    }

    private void showAreaTengahBerhasilAbsen(){
        ly_ambil_foto_presensi.startAnimation(fade_in);
        ly_sudah_berhasil_upload.setVisibility(View.VISIBLE);
    }

    private void hideAreaTengahBerhasilAbsen(){
        ly_sudah_berhasil_upload.setVisibility(View.GONE);
    }

    private void showAreaTengahAreaUpload()
    {
        ly_ambil_foto_presensi.startAnimation(fade_in);
        ly_ambil_foto_presensi.setVisibility(View.VISIBLE);
    }

    private void hideAreaTengahAreaUpload()
    {
        ly_ambil_foto_presensi.setVisibility(View.GONE);
    }

    private void showAreaTengahPreviewFotoPresensi(){
        preview_foto_presensi.startAnimation(fade_in);
        preview_foto_presensi.setVisibility(View.VISIBLE);
    }

    private void hideAreaTengahPreviewFotoPresensi(){
        preview_foto_presensi.setVisibility(View.GONE);
    }

    private void showAreaBawahTombolKirimAbsen(){
        btn_kirim_foto_presensi.startAnimation(fade_in);
        btn_kirim_foto_presensi.setVisibility(View.VISIBLE);
    }

    private void hideAreaBawahTombolKirimAbsen(){
        btn_kirim_foto_presensi.setVisibility(View.GONE);
    }



    private void showAreaBawahTulisanSudahAbsen(){
        area_keterangan_sudah_presensi.startAnimation(fade_in);
        area_keterangan_sudah_presensi.setVisibility(View.VISIBLE);
    }

    private void hideAreaBawahTulisanSudahAbsen(){
        area_keterangan_sudah_presensi.setVisibility(View.GONE);
    }

    private void setTextName(String name){
        nama_tim.setText(name);
    }

    private void setNameFromPref(){
            setTextName(SharedPreferencesHelper.Companion.read(SharedPreferencesHelper.NAME,""));
    }

    private boolean checkGpsEnable(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            return true;
        }else{
            return false;
        }
    }

    private boolean checkWifiEnable(){
        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled()){
            return true;
        }
        else
        {
            return false;
        }
    }

    //start area kirim foto
    private void kirimAbsen(String authorizationBearToken, String namaFileAbsen, Uri uriFileAbsen, String bssid) {
        if(CURRENT_STATE_KIRIM_ABSEN == STATE_LOADING_KIRIM_ABSEN)
        {
            return;
        }

        toggleStateBtnKirimAbsen(STATE_LOADING_KIRIM_ABSEN);
        String namaFileAbsen2 = getNameFromFile(uriFileAbsen);
        InputStream iStream = null;
        try {

            iStream = getContentResolver().openInputStream(uriFileAbsen);
            final byte[] inputData = getBytes(iStream);

            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, URL_KIRIM_ABSENSI,
                    new Response.Listener<NetworkResponse>() {
                        @Override
                        public void onResponse(NetworkResponse response) {
                            toggleStateBtnKirimAbsen(STATE_IDLE_KIRIM_ABSEN);
                            rQueue.getCache().clear();
                            try{
                                JSONObject jsonObject = new JSONObject(new String(response.data));
                                if(jsonObject.getInt("status") == 1)
                                {
                                    toggleShowAreaTengah(STATE_AREA_TENGAH_SHOW_BERHASIL_ABSEN);
                                    toggleShowAreaBawah(STATE_AREA_BAWAH_TAMPIL_TULISAN_SUDAH_ABSEN);
                                }
                                else if(jsonObject.getInt("status") == 0)
                                {
                                    String pesan = jsonObject.getString("message");
                                    Toast.makeText(getApplicationContext(), "message: "+pesan, Toast.LENGTH_SHORT).show();
                                    if(pesan != null && !pesan.equals("")) {
                                        Toast.makeText(getApplicationContext(), pesan, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            catch (Exception e){

                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            toggleStateBtnKirimAbsen(STATE_IDLE_KIRIM_ABSEN);
                           // Toast.makeText(MainActivity.this,"Terjadi kesalahan saat mengirim. Silahkan coba lagi",Toast.LENGTH_LONG).show();
                            //jika error code = 400, maka ada kemungkinan karena salah wifi, maka tampilkan pesan yang dibawah oleh server
                            if(error.networkResponse.statusCode == 400)
                            {
                                if(error.networkResponse.data!=null) {
                                  mekanismeFormatAndShowMessageFrom400ErrorCode(error.networkResponse.data);
                                }
                            }
                        }
                    }) {

                /*
                 * If you want to add more parameters with the image
                 * you can do it here
                 * here we have only one parameter with the image
                 * which is tags
                 * */
//                Parameter
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {


                    Map<String, String> params = new HashMap<>();
                    params.put("ssid", bssid);
                    Log.d("18_agustus_2022","value bssid: "+bssid);
                    // params.put("tags", "ccccc");  add string parameters
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headerMap = new HashMap<String, String>();
                    headerMap.put("Authorization", "Bearer " + authorizationBearToken);
                    return headerMap;
                }

                /*
                 *pass files using below method
                 * */
                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    params.put("file", new DataPart(namaFileAbsen2, inputData));

                    return params;
                }
            };


            volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            rQueue = Volley.newRequestQueue(MainActivity.this);
            rQueue.add(volleyMultipartRequest);


        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }
    //end area kirim foto

    private void mekanismeFormatAndShowMessageFrom400ErrorCode(byte[] networkData){
        try {
            String body = new String(networkData, StandardCharsets.UTF_8);
            JSONObject objResponse = new JSONObject(body);
            Toast.makeText(getApplicationContext(),objResponse.getString("message"),Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getNameFromFile(Uri uri)
    {

        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void toggleStateBtnKirimAbsen(int state){
        switch (state) {
            case STATE_NOTALLOWED_KIRIM_ABSEN: {
                Log.d("17_agustus_2022","state not allowed kirim absen");
                progress_kirim_absen.setVisibility(View.GONE);
                btn_kirim_foto_presensi.setBackgroundTintList(getResources().getColorStateList(R.color.disable_button));
                btn_kirim_foto_presensi.setClickable(false);
                label_kirim_absen.startAnimation(fade_in);
                label_kirim_absen.setVisibility(View.VISIBLE);
                CURRENT_STATE_KIRIM_ABSEN = STATE_NOTALLOWED_KIRIM_ABSEN;
                break;
            }
            case STATE_IDLE_KIRIM_ABSEN : {
                Log.d("17_agustus_2022","state idle kirim absen");
                progress_kirim_absen.setVisibility(View.GONE);
                btn_kirim_foto_presensi.setBackgroundTintList(null);
                btn_kirim_foto_presensi.setClickable(true);
                label_kirim_absen.startAnimation(fade_in);
                label_kirim_absen.setVisibility(View.VISIBLE);
                CURRENT_STATE_KIRIM_ABSEN = STATE_IDLE_KIRIM_ABSEN;
                break;
            }
            case STATE_LOADING_KIRIM_ABSEN: {
                Log.e("17_agustus_2022","state loading kirim absen");
                progress_kirim_absen.startAnimation(fade_in);
                btn_kirim_foto_presensi.setBackgroundTintList(null);
                btn_kirim_foto_presensi.setClickable(true);
                progress_kirim_absen.setVisibility(View.VISIBLE);
                label_kirim_absen.setVisibility(View.GONE);
                CURRENT_STATE_KIRIM_ABSEN = STATE_LOADING_KIRIM_ABSEN;
                break;
            }
        }
    }


    //    start popup logout
    private void showPopUpLogout(){
        AlertDialog.Builder dialogBuilder;
        AlertDialog dialog;
        TextView popup_logout_keluar, popup_logout_batal, popup_logout_username;
        dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        final View vPopUp = getLayoutInflater().inflate(R.layout.popup_logout,null);



        popup_logout_keluar = vPopUp.findViewById(R.id.popup_logout_keluar);
        popup_logout_batal = vPopUp.findViewById(R.id.popup_logout_batal);



        dialogBuilder.setView(vPopUp);
        dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        popup_logout_keluar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                signOut(SharedPreferencesHelper.Companion.read(SharedPreferencesHelper.ACCESS_TOKEN,""));
            }
        });

        popup_logout_batal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });



    }
//    end popup logout

    private void signOut(String authorizationBearToken){
        //start volley get nomor wa
        fadeOutLayoutUtama();
        showPbLogout();
        StringRequest strReq = new StringRequest(Request.Method.POST, URL_LOGOUT, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jObj = new JSONObject(response);

                    if (jObj.getInt("status") == 1) {
                        SharedPreferencesHelper.Companion.removesesion(getApplicationContext());
                        MainActivity.this.finish();
                        startActivity(new Intent(MainActivity.this,LoginActivity.class));
                    }
                    else{
                        fadeInLayoutUtama();
                        hidePbLogout();
                    }


                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    fadeInLayoutUtama();
                    hidePbLogout();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                fadeInLayoutUtama();
                hidePbLogout();
            }
        }) {


//            parameter

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headerMap = new HashMap<String, String>();
                headerMap.put("Authorization", "Bearer " + authorizationBearToken);
                return headerMap;
            }
        };


//        ini heandling requestimeout
        strReq.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 10000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 10000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

                Log.e("18_agustus", "VolleyError Error: " + error.getMessage());
//                eror_show();
            }
        });

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, tag_json_obj);
        //end volley get nomor wa
    }

    private void showPbLogout(){
        pb_logout.setVisibility(View.VISIBLE);
    }

    private void hidePbLogout(){
        pb_logout.setVisibility(View.GONE);
    }

    private void fadeOutLayoutUtama(){
        ly_utama.startAnimation(fade_out_permanent);
    }

    private void fadeInLayoutUtama(){
        ly_utama.startAnimation(fade_in);
    }

}

