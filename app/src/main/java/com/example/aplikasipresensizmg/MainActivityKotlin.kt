package com.example.aplikasipresensizmg

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.SupplicantState
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.aplikasipresensizmg.helper.RedirectToTampilErrorActivity
import com.example.aplikasipresensizmg.helper.sharedpreferences.SharedPreferencesHelper
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.nio.charset.StandardCharsets


class MainActivityKotlin : AppCompatActivity() {

    lateinit var fade_in: Animation
    lateinit var fade_out_permanent: Animation
    lateinit var nama_tim: TextView
    lateinit var tv_kirim_ulang: TextView
    lateinit var btn_kirim_foto_presensi: RelativeLayout
    lateinit var area_keterangan_sudah_presensi: RelativeLayout
    lateinit var preview_foto_presensi: ImageView
    lateinit var img_hapus_foto: android.widget.ImageView
    lateinit var btn_logout: android.widget.ImageView
    lateinit var ly_ambil_foto_presensi: ConstraintLayout
    lateinit var ly_sudah_berhasil_upload: ConstraintLayout
    lateinit var ly_utama: ConstraintLayout
    lateinit var rQueue: RequestQueue
    var uriGambarAbsen: Uri? = null
    lateinit var progress_kirim_absen: ProgressBar
    lateinit var pb_logout: ProgressBar
    lateinit var label_kirim_absen: TextView
    var tag_json_obj = "json_obj_req"

    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private val FINE_LOCATION_PERMISSION_REQUEST_CODE = 101
    private val COARSE_LOCATION_PERMISSION_REQUEST_CODE = 102
    private val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 103
    private val WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 104
    private val TAKE_CAMERA_REQUEST_CODE = 200


    private val URL_KIRIM_ABSENSI = BankURL.URL_KIRIM_ABSENSI
    private val URL_LOGOUT = BankURL.URL_LOGOUT


    /*START STATE AREA TENGAH ACTIVITY*/
    var CURRENT_STATE_AREA_TENGAH = 0
    private val STATE_AREA_TENGAH_SHOW_AREA_UPLOAD = 0
    private val STATE_AREA_TENGAH_SHOW_PREVIEW_FOTO = 1
    private val STATE_AREA_TENGAH_SHOW_BERHASIL_ABSEN = 2
    /*END STATE AREA TENGAH ACTIVITY*/

    /*END STATE AREA TENGAH ACTIVITY*/ /*START STATE AREA BAWAH ACTIVITY*/
    var CURRENT_STATE_AREA_BAWAH = 0
    private val STATE_AREA_BAWAH_TAMPIL_TOMBOL_KIRIM_ABSEN = 0
    private val STATE_AREA_BAWAH_TAMPIL_TULISAN_SUDAH_ABSEN = 1
    /*END STATE AREA BAWAH ACTIVITY*/

    /*END STATE AREA BAWAH ACTIVITY*/ /*START STATE BUTTON KIRIM*/
    var CURRENT_STATE_KIRIM_ABSEN = 0
    private val STATE_NOTALLOWED_KIRIM_ABSEN = 0
    private val STATE_IDLE_KIRIM_ABSEN = 1
    private val STATE_LOADING_KIRIM_ABSEN = 2
    /*END STATE BUTTON KIRIM*/

    /*END STATE BUTTON KIRIM*/ /*START ALL PERMISSION STATUS*/
    var isPermissionCoarseLocationGranted = false
    var isPermissionFineLocationGranted = false
    var isPermissionReadExternalStorageGranted = false
    var isPermissionWriteExternalStorageGranted = false
    var isPermissionCameraGranted = false
    /*END ALL PERMISSION STATUS*/


    /*END ALL PERMISSION STATUS*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.include_activity_main)
        initAnimation()
        findViewByIdAllComponent()
        setListenerAllComponent()
        askFineLocationPermission()
        mekanismeCheckSemuaPermission()
        setNameFromPref()
    }

    private fun mekanismeCheckSemuaPermission() {
        Log.d("18_agustus_2022", "mekanismeCheckSemuaPermission()")
        if (checkCoarseLocationPermission() == true) {
            isPermissionCoarseLocationGranted = true
        } else {
            Toast.makeText(
                applicationContext,
                "Aktifkan izin lokasimu terlebih dahulu",
                Toast.LENGTH_SHORT
            ).show()
            askCoarseLocationPermission()
        }
        if (checkFineLocationPermission() == true) {
            isPermissionFineLocationGranted = true
        } else {
            Toast.makeText(
                applicationContext,
                "Aktifkan izin lokasimu terlebih dahulu",
                Toast.LENGTH_SHORT
            ).show()
            askFineLocationPermission()
        }
        if (checkWriteExternalStoragePermission() == true) {
            isPermissionWriteExternalStorageGranted = true
        } else {
            Toast.makeText(
                applicationContext,
                "Aktifkan izin penyimpananmu terlebih dahulu",
                Toast.LENGTH_SHORT
            ).show()
            askWriteExternalStoragePermission()
        }
        if (checkReadExternalStoragePermission() == true) {
            isPermissionReadExternalStorageGranted = true
        } else {
            Toast.makeText(
                applicationContext,
                "Aktifkan izin penyimpananmu terlebih dahulu",
                Toast.LENGTH_SHORT
            ).show()
            askReadExternalStoragePermission()
        }
        if (checkCameraPermission() == true) {
            isPermissionCameraGranted = true
        } else {
            Toast.makeText(
                applicationContext,
                "Aktifkan izin kameramu terlebih dahulu",
                Toast.LENGTH_SHORT
            ).show()
            askCameraPermission()
        }
    }

    private fun initAnimation() {
        fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        fade_out_permanent = AnimationUtils.loadAnimation(this, R.anim.fade_out_permanent)
    }

    private fun findViewByIdAllComponent() {
        nama_tim = findViewById(R.id.nama_tim)
        btn_kirim_foto_presensi = findViewById(R.id.btn_kirim_foto_presensi)
        preview_foto_presensi = findViewById(R.id.preview_foto_presensi)
        ly_ambil_foto_presensi = findViewById(R.id.ly_ambil_foto_presensi)
        progress_kirim_absen = findViewById(R.id.progress_kirim_absen)
        label_kirim_absen = findViewById(R.id.label_kirim_absen)
        ly_sudah_berhasil_upload = findViewById<ConstraintLayout>(R.id.ly_sudah_berhasil_upload)
        area_keterangan_sudah_presensi =
            findViewById<RelativeLayout>(R.id.area_keterangan_sudah_presensi)
        tv_kirim_ulang = findViewById<TextView>(R.id.tv_kirim_ulang)
        img_hapus_foto = findViewById<ImageView>(R.id.img_hapus_foto)
        btn_logout = findViewById<ImageView>(R.id.btn_logout)
        pb_logout = findViewById<ProgressBar>(R.id.pb_logout)
        ly_utama = findViewById<ConstraintLayout>(R.id.ly_utama)
    }

    private fun setListenerAllComponent() {
        ly_ambil_foto_presensi!!.setOnClickListener { mekanismeAmbilFotoLewatKamera() }
        btn_kirim_foto_presensi!!.setOnClickListener { mekanismeKirimAbsen() }
        tv_kirim_ulang.setOnClickListener(View.OnClickListener { resetStatePresensi() })
        img_hapus_foto.setOnClickListener(View.OnClickListener { resetStatePresensi() })
        btn_logout.setOnClickListener(View.OnClickListener { showPopUpLogout() })
    }


    private fun resetStatePresensi() {
        uriGambarAbsen = null
        toggleShowAreaTengah(STATE_AREA_TENGAH_SHOW_AREA_UPLOAD)
        toggleShowAreaBawah(STATE_AREA_BAWAH_TAMPIL_TOMBOL_KIRIM_ABSEN)
    }

    private fun mekanismeKirimAbsen() {
        try {
            //hanya bisa mengirim ketika button kirim absen sedang idle
            if (CURRENT_STATE_KIRIM_ABSEN == STATE_IDLE_KIRIM_ABSEN) kirimAbsen(
                SharedPreferencesHelper.read(
                    SharedPreferencesHelper.ACCESS_TOKEN,
                    ""
                ), "absen", uriGambarAbsen, getBSSID()
            )
        }
        catch(e:Exception){
            Log.e("fandydebugkirimabsen","catch mekanismeKirimAbsen terpanggil karena ${e.message}")
            RedirectToTampilErrorActivity(
                this@MainActivityKotlin,
                "tc: ${e.message}",
                "mekanismeKirimAbsen"
            )
        }
    }


    private fun mekanismeAmbilFotoLewatKamera() {
        try {
            Log.d("18_agustus_2022", "mekanismeAmbilFotoLewatKamera()")
            if (checkSemuaPermissionGranted() == false) {
                mekanismeCheckSemuaPermission()
            } else if (checkGpsEnable() == false) {
                Toast.makeText(
                    applicationContext,
                    "Aktifkan GPS mu terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (checkWifiEnable() == false) {
                Toast.makeText(
                    applicationContext,
                    "Aktifkan WIFI mu terlebih dahulu",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Log.d("18_agustus_2022", "mekanismeAmbilFotoLewatKamera() if")
                Log.d(
                    "18_agustus_2022",
                    "isPermissionCameraGranted: $isPermissionCameraGranted"
                )
                launchIntentTakePictureFromCamera()
            }
        }
        catch (e:Exception){
            RedirectToTampilErrorActivity(
                this@MainActivityKotlin,
                "tc: ${e.message}",
                "mekanismeAmbilFotoLewatKamera"
            )
        }
    }

    private fun checkSemuaPermissionGranted(): Boolean {
        var hasil = true
        if (isPermissionCameraGranted == false) {
            hasil = false
        } else if (isPermissionReadExternalStorageGranted == false) {
            hasil = false
        } else if (isPermissionWriteExternalStorageGranted == false) {
            hasil = false
        } else if (isPermissionFineLocationGranted == false) {
            hasil = false
        } else if (isPermissionCoarseLocationGranted == false) {
            hasil = false
        }
        return hasil
    }

    private fun checkCameraPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            false
        } else {
            true
        }
    }

    private fun askCameraPermission() {
        Log.d("18_agustus_2022", "ask camera permission")
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )

        //launchIntentTakePictureFromCamera();
    }

    private fun checkFineLocationPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            false
        } else {
            true
        }
    }

    private fun askFineLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            FINE_LOCATION_PERMISSION_REQUEST_CODE
        )


    }

    private fun checkCoarseLocationPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            false
        } else {
            true
        }
    }

    private fun askCoarseLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            COARSE_LOCATION_PERMISSION_REQUEST_CODE
        )

    }


    private fun checkWriteExternalStoragePermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            false
        } else {
            true
        }
    }

    private fun askWriteExternalStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
        )

    }

    private fun checkReadExternalStoragePermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            false
        } else {
            true
        }
    }

    private fun askReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
        )

    }

    private fun launchIntentTakePictureFromCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, TAKE_CAMERA_REQUEST_CODE)
        } catch (e: Exception) {
            RedirectToTampilErrorActivity(
                this@MainActivityKotlin,
                "tc: ${e.message}",
                "launchIntentTakePictureFromCamera"
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == TAKE_CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
                val imageBitmap = data!!.extras!!["data"] as Bitmap?
                uriGambarAbsen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveImageInQ(imageBitmap)
                } else {
                    getUri(this@MainActivityKotlin, imageBitmap)
                }

                toggleShowAreaTengah(STATE_AREA_TENGAH_SHOW_PREVIEW_FOTO)
                preview_foto_presensi!!.setImageBitmap(imageBitmap)
            }
        }
        catch(e:Exception){
            RedirectToTampilErrorActivity(
                this@MainActivityKotlin,
                "tc: ${e.message}",
                "onActivityResult"
            )
        }
    }

    fun saveImageInQ(bitmap: Bitmap?): Uri? {
        Log.e("RFK", "saveImageInQ: ")
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        var imageUri: Uri? = null
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }

        //use application context to get contentResolver
        val contentResolver = application.contentResolver

        contentResolver.also { resolver ->
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver.openOutputStream(it) }
        }

        fos?.use { bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it) }


        return imageUri
    }


    private fun getUri(context: Context, bitmap: Bitmap?): Uri? {
        Log.e("RFK", "getUri: ")
        val bytes = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }

    private fun show_img_hapus_foto() {
        img_hapus_foto.setVisibility(View.VISIBLE)
    }

    private fun hide_img_hapus_foto() {
        img_hapus_foto.setVisibility(View.GONE)
    }

    private fun deleteUriInLocalStorage(uri: Uri) {
        contentResolver.delete(uri, null, null)
    }

    private fun toggleShowAreaTengah(PARAM_SHOW: Int) {
        when (PARAM_SHOW) {
            STATE_AREA_TENGAH_SHOW_AREA_UPLOAD -> {
                hide_img_hapus_foto()
                hideAreaTengahPreviewFotoPresensi()
                hideAreaTengahBerhasilAbsen()
                showAreaTengahAreaUpload()
                toggleStateBtnKirimAbsen(STATE_NOTALLOWED_KIRIM_ABSEN)
                CURRENT_STATE_AREA_TENGAH = STATE_AREA_TENGAH_SHOW_AREA_UPLOAD
            }
            STATE_AREA_TENGAH_SHOW_PREVIEW_FOTO -> {
                hideAreaTengahBerhasilAbsen()
                hideAreaTengahAreaUpload()
                show_img_hapus_foto()
                showAreaTengahPreviewFotoPresensi()
                toggleStateBtnKirimAbsen(STATE_IDLE_KIRIM_ABSEN)
                CURRENT_STATE_AREA_TENGAH = STATE_AREA_TENGAH_SHOW_PREVIEW_FOTO
            }
            STATE_AREA_TENGAH_SHOW_BERHASIL_ABSEN -> {
                hideAreaTengahAreaUpload()
                hide_img_hapus_foto()
                hideAreaTengahPreviewFotoPresensi()
                showAreaTengahBerhasilAbsen()
                toggleStateBtnKirimAbsen(STATE_NOTALLOWED_KIRIM_ABSEN)
                CURRENT_STATE_AREA_TENGAH = STATE_AREA_TENGAH_SHOW_BERHASIL_ABSEN
            }
        }
    }

    private fun toggleShowAreaBawah(PARAM_SHOW: Int) {
        when (PARAM_SHOW) {
            STATE_AREA_BAWAH_TAMPIL_TOMBOL_KIRIM_ABSEN -> {
                showAreaBawahTombolKirimAbsen()
                hideAreaBawahTulisanSudahAbsen()
                CURRENT_STATE_AREA_BAWAH = STATE_AREA_BAWAH_TAMPIL_TOMBOL_KIRIM_ABSEN
            }
            STATE_AREA_BAWAH_TAMPIL_TULISAN_SUDAH_ABSEN -> {
                hideAreaBawahTombolKirimAbsen()
                showAreaBawahTulisanSudahAbsen()
                CURRENT_STATE_AREA_BAWAH = STATE_AREA_BAWAH_TAMPIL_TULISAN_SUDAH_ABSEN
            }
        }
    }

    private fun getBSSID(): String {
        var bssid = ""
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val wifiInfo: WifiInfo
        wifiInfo = wifiManager.connectionInfo
        if (wifiInfo.supplicantState == SupplicantState.COMPLETED) {
            bssid = wifiInfo.bssid
        }
        return bssid
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isPermissionCameraGranted = true
                mekanismeCheckSemuaPermission()
            }
        }
        if (requestCode == FINE_LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isPermissionFineLocationGranted = true
                mekanismeCheckSemuaPermission()
            }
        }
        if (requestCode == COARSE_LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isPermissionCoarseLocationGranted = true
                mekanismeCheckSemuaPermission()
            }
        }
        if (requestCode == WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isPermissionWriteExternalStorageGranted = true
                mekanismeCheckSemuaPermission()
            }
        }
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isPermissionReadExternalStorageGranted = true
                mekanismeCheckSemuaPermission()
            }
        }
    }

    private fun showAreaTengahBerhasilAbsen() {
        ly_ambil_foto_presensi!!.startAnimation(fade_in)
        ly_sudah_berhasil_upload.setVisibility(View.VISIBLE)
    }

    private fun hideAreaTengahBerhasilAbsen() {
        ly_sudah_berhasil_upload.setVisibility(View.GONE)
    }

    private fun showAreaTengahAreaUpload() {
        ly_ambil_foto_presensi!!.startAnimation(fade_in)
        ly_ambil_foto_presensi!!.visibility = View.VISIBLE
    }

    private fun hideAreaTengahAreaUpload() {
        ly_ambil_foto_presensi!!.visibility = View.GONE
    }

    private fun showAreaTengahPreviewFotoPresensi() {
        preview_foto_presensi!!.startAnimation(fade_in)
        preview_foto_presensi!!.visibility = View.VISIBLE
    }

    private fun hideAreaTengahPreviewFotoPresensi() {
        preview_foto_presensi!!.visibility = View.GONE
    }

    private fun showAreaBawahTombolKirimAbsen() {
        btn_kirim_foto_presensi!!.startAnimation(fade_in)
        btn_kirim_foto_presensi!!.visibility = View.VISIBLE
    }

    private fun hideAreaBawahTombolKirimAbsen() {
        btn_kirim_foto_presensi!!.visibility = View.GONE
    }


    private fun showAreaBawahTulisanSudahAbsen() {
        area_keterangan_sudah_presensi.startAnimation(fade_in)
        area_keterangan_sudah_presensi.setVisibility(View.VISIBLE)
    }

    private fun hideAreaBawahTulisanSudahAbsen() {
        area_keterangan_sudah_presensi.setVisibility(View.GONE)
    }

    private fun setTextName(name: String?) {
        nama_tim!!.text = name
    }

    private fun setNameFromPref() {
        setTextName(SharedPreferencesHelper.read(SharedPreferencesHelper.NAME, ""))
    }

    private fun checkGpsEnable(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            true
        } else {
            false
        }
    }

    private fun checkWifiEnable(): Boolean {
        val wifi = getApplicationContext().getSystemService(WIFI_SERVICE) as WifiManager
        return if (wifi.isWifiEnabled) {
            true
        } else {
            false
        }
    }

    //start area kirim foto
    private fun kirimAbsen(
        authorizationBearToken: String?,
        namaFileAbsen: String,
        uriFileAbsen: Uri?,
        bssid: String
    ) {
        if (CURRENT_STATE_KIRIM_ABSEN == STATE_LOADING_KIRIM_ABSEN) {
            return
        }
        toggleStateBtnKirimAbsen(STATE_LOADING_KIRIM_ABSEN)
        try {
        val namaFileAbsen2 = getNameFromFile(uriFileAbsen)
        var iStream: InputStream? = null
            iStream = contentResolver.openInputStream(uriFileAbsen!!)
            val inputData = getBytes(iStream)
            val volleyMultipartRequest: VolleyMultipartRequest = object : VolleyMultipartRequest(
                Method.POST, URL_KIRIM_ABSENSI,
                Response.Listener { response ->
                    toggleStateBtnKirimAbsen(STATE_IDLE_KIRIM_ABSEN)
                    rQueue!!.cache.clear()
                    try {
                        val jsonObject = JSONObject(String(response.data))
                        if (jsonObject.getInt("status") == 1) {
                            toggleShowAreaTengah(STATE_AREA_TENGAH_SHOW_BERHASIL_ABSEN)
                            toggleShowAreaBawah(STATE_AREA_BAWAH_TAMPIL_TULISAN_SUDAH_ABSEN)
                        } else if (jsonObject.getInt("status") == 0) {
                            val pesan = jsonObject.getString("message")
                            Toast.makeText(
                                applicationContext,
                                "message: $pesan",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (pesan != null && pesan != "") {
                                Toast.makeText(applicationContext, pesan, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        RedirectToTampilErrorActivity(
                            this@MainActivityKotlin,
                            "tc: ${e.message}",
                            "kirimAbsen - inner catch"
                        )
                    }
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this@MainActivityKotlin,"Gagal mengirim foto absen, periksa koneksi internet anda",Toast.LENGTH_LONG).show()
                    toggleStateBtnKirimAbsen(STATE_IDLE_KIRIM_ABSEN)
                    // Toast.makeText(MainActivity.this,"Terjadi kesalahan saat mengirim. Silahkan coba lagi",Toast.LENGTH_LONG).show();
                    //jika error code = 400, maka ada kemungkinan karena salah wifi, maka tampilkan pesan yang dibawah oleh server
                    if (error.networkResponse?.statusCode == 400) {
                        if (error.networkResponse.data != null) {
                            mekanismeFormatAndShowMessageFrom400ErrorCode(error.networkResponse.data)
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
                @Throws(AuthFailureError::class)
                override fun getParams(): Map<String, String>? {
                    val params: MutableMap<String, String> = HashMap()
                    params["ssid"] = bssid
                    Log.d("18_agustus_2022", "value bssid: $bssid")
                    // params.put("tags", "ccccc");  add string parameters
                    return params
                }

                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headerMap: MutableMap<String, String> = HashMap()
                    headerMap["Authorization"] = "Bearer $authorizationBearToken"
                    return headerMap
                }

                /*
                 *pass files using below method
                 * */
                override fun getByteData(): Map<String, DataPart> {
                    val params: MutableMap<String, DataPart> = HashMap()
                    params["file"] = DataPart(namaFileAbsen2, inputData)
                    return params
                }
            }
            volleyMultipartRequest.retryPolicy = DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
            rQueue = Volley.newRequestQueue(this@MainActivityKotlin)
            rQueue.add(volleyMultipartRequest)
        } catch (e: FileNotFoundException) {
            RedirectToTampilErrorActivity(
                this@MainActivityKotlin,
                "tc: ${e.message}",
                "kirimAbsen - fileNotFoundException"
            )
        } catch (e: IOException) {
            RedirectToTampilErrorActivity(
                this@MainActivityKotlin,
                "tc: ${e.message}",
                "kirimAbsen - IOException"
            )
        }
    }
    //end area kirim foto

    //end area kirim foto
    private fun mekanismeFormatAndShowMessageFrom400ErrorCode(networkData: ByteArray) {
        try {
            val body = String(networkData, StandardCharsets.UTF_8)
            val objResponse = JSONObject(body)
            Toast.makeText(applicationContext, objResponse.getString("message"), Toast.LENGTH_SHORT)
                .show()
        } catch (e: Exception) {
            RedirectToTampilErrorActivity(
                this@MainActivityKotlin,
                "tc: ${e.message}",
                "mekanismeFormatAndShowMessageFrom400ErrorCode"
            )
        }

    }


    private fun getNameFromFile(uri: Uri?): String? {
        var result: String? = null
        if (uri!!.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {

                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
            catch(e:Exception){
                RedirectToTampilErrorActivity(
                    this@MainActivityKotlin,
                    "tc: ${e.message}",
                    "getNameFromFile"
                )
            }
            finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    @Throws(IOException::class)
    fun getBytes(inputStream: InputStream?): ByteArray {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len = 0
        while (inputStream!!.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }

    private fun toggleStateBtnKirimAbsen(state: Int) {
        when (state) {
            STATE_NOTALLOWED_KIRIM_ABSEN -> {
                Log.d("17_agustus_2022", "state not allowed kirim absen")
                progress_kirim_absen!!.visibility = View.GONE
                btn_kirim_foto_presensi!!.isClickable = false
                btn_kirim_foto_presensi.backgroundTintList =
                    resources.getColorStateList(R.color.disable_button)
                label_kirim_absen!!.startAnimation(fade_in)
                label_kirim_absen!!.visibility = View.VISIBLE
                CURRENT_STATE_KIRIM_ABSEN = STATE_NOTALLOWED_KIRIM_ABSEN
            }
            STATE_IDLE_KIRIM_ABSEN -> {
                Log.d("17_agustus_2022", "state idle kirim absen")
                progress_kirim_absen!!.visibility = View.GONE
                btn_kirim_foto_presensi!!.backgroundTintList = null
                btn_kirim_foto_presensi!!.isClickable = true
                label_kirim_absen!!.startAnimation(fade_in)
                label_kirim_absen!!.visibility = View.VISIBLE
                CURRENT_STATE_KIRIM_ABSEN = STATE_IDLE_KIRIM_ABSEN
            }
            STATE_LOADING_KIRIM_ABSEN -> {
                Log.e("17_agustus_2022", "state loading kirim absen")
                progress_kirim_absen!!.startAnimation(fade_in)
                btn_kirim_foto_presensi!!.backgroundTintList = null
                btn_kirim_foto_presensi!!.isClickable = true
                progress_kirim_absen!!.visibility = View.VISIBLE
                label_kirim_absen!!.visibility = View.GONE
                CURRENT_STATE_KIRIM_ABSEN = STATE_LOADING_KIRIM_ABSEN
            }
        }
    }


    //    start popup logout
    private fun showPopUpLogout() {
        val dialogBuilder: AlertDialog.Builder
        val dialog: AlertDialog
        val popup_logout_keluar: TextView
        val popup_logout_batal: TextView
        var popup_logout_username: TextView
        dialogBuilder = AlertDialog.Builder(this@MainActivityKotlin)
        val vPopUp = layoutInflater.inflate(R.layout.popup_logout, null)
        popup_logout_keluar = vPopUp.findViewById(R.id.popup_logout_keluar)
        popup_logout_batal = vPopUp.findViewById(R.id.popup_logout_batal)
        dialogBuilder.setView(vPopUp)
        dialog = dialogBuilder.create()
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
        popup_logout_keluar.setOnClickListener {
            dialog.dismiss()
            signOut(SharedPreferencesHelper.read(SharedPreferencesHelper.ACCESS_TOKEN, ""))
        }
        popup_logout_batal.setOnClickListener { dialog.dismiss() }
    }
//    end popup logout

    //    end popup logout
    private fun signOut(authorizationBearToken: String?) {
        //start volley get nomor wa
        fadeOutLayoutUtama()
        showPbLogout()
        val strReq: StringRequest = object : StringRequest(
            Method.POST, URL_LOGOUT,
            Response.Listener { response ->
                try {
                    val jObj = JSONObject(response)
                    if (jObj.getInt("status") == 1) {
                        SharedPreferencesHelper.removesesion(applicationContext)
                        this@MainActivityKotlin.finish()
                        startActivity(Intent(this@MainActivityKotlin, LoginActivity::class.java))
                    } else {
                        fadeInLayoutUtama()
                        hidePbLogout()
                    }
                } catch (e: JSONException) {
                    // JSON error
                    e.printStackTrace()
                    fadeInLayoutUtama()
                    hidePbLogout()
                    RedirectToTampilErrorActivity(
                        this@MainActivityKotlin,
                        "tc: ${e.message}",
                        "signOut catch"
                    )
                }
            },
            Response.ErrorListener { error ->
                fadeInLayoutUtama()
                hidePbLogout()
                RedirectToTampilErrorActivity(
                    this@MainActivityKotlin,
                    "tc: ${error.message}",
                    "signOut errorListener"
                )
            }) {
            //            parameter
            override fun getParams(): Map<String, String>? {
                // Posting parameters to login url
                return HashMap()
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headerMap: MutableMap<String, String> = HashMap()
                headerMap["Authorization"] = "Bearer $authorizationBearToken"
                return headerMap
            }
        }


//        ini heandling requestimeout
        strReq.retryPolicy = object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 10000
            }

            override fun getCurrentRetryCount(): Int {
                return 10000
            }

            @Throws(VolleyError::class)
            override fun retry(error: VolleyError) {
                Log.e("18_agustus", "VolleyError Error: " + error.message)
                //                eror_show();
            }
        }

        // Adding request to request queue
        MyApplication.getInstance().addToRequestQueue(strReq, tag_json_obj)
        //end volley get nomor wa
    }

    private fun showPbLogout() {
        pb_logout.setVisibility(View.VISIBLE)
    }

    private fun hidePbLogout() {
        pb_logout.setVisibility(View.GONE)
    }

    private fun fadeOutLayoutUtama() {
        ly_utama.startAnimation(fade_out_permanent)
    }

    private fun fadeInLayoutUtama() {
        ly_utama.startAnimation(fade_in)
    }


}