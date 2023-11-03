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
import com.android.volley.toolbox.Volley
import com.example.aplikasipresensizmg.helper.RedirectToTampilErrorActivity
import com.example.aplikasipresensizmg.helper.retrofit.ApiInterface
import com.example.aplikasipresensizmg.helper.sharedpreferences.SharedPreferencesHelper
import com.example.aplikasipresensizmg.helper.sqlite.DataHandler
import com.example.aplikasipresensizmg.helper.sqlite.DatabaseHelper
import com.example.aplikasipresensizmg.model.ChecklogModel
import com.example.aplikasipresensizmg.model.LoginModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.awaitResponse
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
    lateinit var pb_main_activity: ProgressBar
    lateinit var label_kirim_absen: TextView


    //SQLITE
    lateinit var context : Context
    lateinit var dbHelper : DatabaseHelper
    lateinit var dataHandler : DataHandler


    var tag_json_obj = "json_obj_req"

    val apiInterface = ApiInterface.create()

    private val TIPE_AKTIVITAS_KIRIMABSEN : Int = 0
    private val TIPE_AKTIVITAS_LOGOUT : Int = 1

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
        initSqlite()
        findViewByIdAllComponent()
        setListenerAllComponent()
        askFineLocationPermission()
        mekanismeCheckSemuaPermission()
        setNameFromPref()
        //jika baru saja dari halaman login, tidak perlu validateToken()
       checkIntentExtra()
    }

    fun checkIntentExtra(){
        if(intent.hasExtra("from_login")) {

        }
        else{
            validateToken()
        }
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
        pb_main_activity = findViewById<ProgressBar>(R.id.pb_main_activity)
        ly_utama = findViewById<ConstraintLayout>(R.id.ly_utama)

    }

    private fun setListenerAllComponent() {
        ly_ambil_foto_presensi!!.setOnClickListener { mekanismeAmbilFotoLewatKamera() }
        btn_kirim_foto_presensi!!.setOnClickListener { mekanismeKirimAbsen() }
        tv_kirim_ulang.setOnClickListener(View.OnClickListener { resetStatePresensi() })
        img_hapus_foto.setOnClickListener(View.OnClickListener { resetStatePresensi() })
        btn_logout.setOnClickListener(View.OnClickListener {
            showPopUpLogout() })
    }


    private fun resetStatePresensi() {
        uriGambarAbsen = null
        toggleShowAreaTengah(STATE_AREA_TENGAH_SHOW_AREA_UPLOAD)
        toggleShowAreaBawah(STATE_AREA_BAWAH_TAMPIL_TOMBOL_KIRIM_ABSEN)
    }

    private fun mekanismeKirimAbsen() {
        try {
            //validasi token dulu masih aktif/tidak
            //hanya bisa mengirim ketika button kirim absen sedang idle
            if (CURRENT_STATE_KIRIM_ABSEN == STATE_IDLE_KIRIM_ABSEN) kirimAbsen(
                getTokenFromSharedPref(), "absen", uriGambarAbsen, getBSSID()
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

    private fun getTokenFromSharedPref(): String? {
        return SharedPreferencesHelper.read(
            SharedPreferencesHelper.ACCESS_TOKEN,
            ""
        )
    }

    //contoh token expired untuk debugging
    private fun getExpiredToken(): String?  {
        return "3401|PfIPw01UutfbBpFpMIneyqLkhzpMtiawkHtkBRIZ"
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

    fun uriToFile(context: Context, uri: Uri): File? {
        val contentResolver = context.contentResolver
        val filePathColumn = arrayOf("_data")
        val cursor = contentResolver.query(uri, filePathColumn, null, null, null)

        return if (cursor != null) {
            cursor.moveToFirst()
            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val filePath = cursor.getString(columnIndex)
            cursor.close()
            File(filePath)
        } else {
            null
        }
    }

    private fun kirimAbsen(
        authorizationBearToken: String?,
        namaFileAbsen: String,
        uriFileAbsen: Uri?,
        bssid: String
    ){
        Log.d("debug_30-oct-23","authx: $authorizationBearToken")
        if (CURRENT_STATE_KIRIM_ABSEN == STATE_LOADING_KIRIM_ABSEN) {
            return
        }

        toggleStateBtnKirimAbsen(STATE_LOADING_KIRIM_ABSEN)


        try {
            val fileYangDikirim : File? = uriToFile(this@MainActivityKotlin,uriFileAbsen!!)
            var fileRequestBody : RequestBody = RequestBody.create(MediaType.parse("image/*"),fileYangDikirim)

            val filePart =
                MultipartBody.Part.createFormData("file", fileYangDikirim?.getName(), fileRequestBody)

            val textRequestBody: RequestBody = RequestBody.create(MultipartBody.FORM, bssid)
            //  val textRequestBody: RequestBody = RequestBody.create(MultipartBody.FORM, "wifi salah")

            //var call : Call<ChecklogModel?>? = apiInterface.uploadTextAndFile("Bearer 3401|PfIPw01UutfbBpFpMIneyqLkhzpMtiawkHtkBRIZ",filePart,textRequestBody)
            var call : Call<ChecklogModel?>? = apiInterface.uploadTextAndFile("Bearer $authorizationBearToken",filePart,textRequestBody)

            call?.enqueue(object : Callback<ChecklogModel?> {
                override fun onResponse(
                    call: Call<ChecklogModel?>,
                    response: retrofit2.Response<ChecklogModel?>
                ) {
                    Log.d("debug_30-oct-23","outer response status: ${response.body()?.status}")
                    Log.d("debug_30-oct-23","outer response message: ${response.body()?.message}")

                    if (response.isSuccessful) {
                        if(response.body()?.status == "1")
                        {
                            toggleShowAreaTengah(STATE_AREA_TENGAH_SHOW_BERHASIL_ABSEN)
                            toggleShowAreaBawah(STATE_AREA_BAWAH_TAMPIL_TULISAN_SUDAH_ABSEN)
                        }
                        else if(response.body()?.status == "0") //biasanya kesalahan saat mencoba absen dari WIFI yang salah
                        {
                            Toast.makeText(this@MainActivityKotlin,response.body()?.message,Toast.LENGTH_LONG).show()
                            toggleStateBtnKirimAbsen(STATE_IDLE_KIRIM_ABSEN)
                        }
                        Log.d("debug_30-oct-23","respon suksesz")
                        Log.d("debug_30-oct-23","respon body"+response.body())
                        Log.d("debug_30-oct-23","respon status "+response.body()?.status)
                        Log.d("debug_30-oct-23","respon message "+response.body()?.message)


                    } else {
                        //jika error respon = 401 (unaouthorized, besar kemungkinan karena bear token expired, maka lakukan forceLogout)
                        if(response.code() == 401)
                        {
                            processLogout(false)
                        }
                        Toast.makeText(this@MainActivityKotlin,"Error: ${response.code()}. Silahkan hubungi administrator",Toast.LENGTH_LONG).show()
                        toggleStateBtnKirimAbsen(STATE_IDLE_KIRIM_ABSEN)
                        Log.d("debug_30-oct-23","respon tidak suksesz")
                        Log.d("debug_30-oct-23","respon: "+response)
                        Log.d("debug_30-oct-23","messsage: "+response.message())
                        Log.d("debug_30-oct-23","error body: "+response.errorBody())
                        Log.d("debug_30-oct-23","res body: "+response.body())
                        Log.d("debug_30-oct-23","raw: "+response.raw())
                        Log.d("debug_30-oct-23","error code: "+response.code())
                        Log.d("debug_30-oct-23","headers: "+response.headers())



                    }
                }

                override fun onFailure(call: Call<ChecklogModel?>, t: Throwable?) {
                    Log.d("debug_30-oct-23","retrofit gagal karena: "+t?.message)
                    Toast.makeText(this@MainActivityKotlin,"Gagal Mengirim Presensi, pastikan internetmu aktif",Toast.LENGTH_LONG).show()
                    toggleStateBtnKirimAbsen(STATE_IDLE_KIRIM_ABSEN)
                }


            })

        }
        catch (e:Exception){

        }
    }

    private suspend fun getNewBearToken() : String { //jika 0 = kirimAbsen, 1 = logout
        Log.d("debug_31-oct-23","getNewBearTOken() ")
        val nip = SharedPreferencesHelper.read(
            SharedPreferencesHelper.NIP,
            ""
        )
        var newToken : String = ""


        if (nip != null) {
            Log.d("debug_31-oct-23","getNewBearTOken(). nip is not null ")
            //mengkolaborasikan retrofit dengan coroutine. Retrofit jika menggunakan coroutine, maka tidak lagi menggunakan callback function (onResponse,onFailure), sebagai gantinya, gunakan .await() untuk mendapatkan datanya dan .awaitResponse() untuk mendapatkan responsenya
            Log.d("debug_31-oct-23","getNewBearTOken() nip yang akan dikirim $nip")
            var response = apiInterface.login(nip).awaitResponse()
            if(response.isSuccessful){
                Log.d("debug_31-oct-23","getNewBearTOken() nip is not null, response is successful ")
                newToken = response.body()?.access_token ?: ""
                Log.d("debug_31-oct-23","responseNewToken: "+newToken)
            }
            else{
                Log.d("debug_31-oct-23","getNewBearTOken() nip is not null, response is NOT successful ")
                Log.d("debug_31-oct-23","message: "+response.message())
                Log.d("debug_31-oct-23","headers: "+response.headers())
                Log.d("debug_31-oct-23","code: "+response.code())
                Log.d("debug_31-oct-23","errorBody: "+response.errorBody())
                Log.d("debug_31-oct-23","raw: "+response.raw())
                Log.d("debug_31-oct-23","body: "+response.body())
                Log.d("debug_31-oct-23","response: "+response)

            }
        }

        else
        {
            Toast.makeText(this@MainActivityKotlin,"NIP is not store inside device",Toast.LENGTH_LONG).show()
        }
        Log.d("debug_31-oct-23","new token: $newToken")
        return newToken
    }


    fun saveDataToSharedPref(access_token:String,id_user:String,name:String,nip:String,role:String,status:String){
        try {
            with(SharedPreferencesHelper)
            {
                write(ACCESS_TOKEN,access_token)
                write(ID_USER,id_user)
                write(NAME,name)
                write(NIP,nip)
                write(ROLE,role)
                write(STATUS,status)
            }
        } catch (e: Exception) {
            RedirectToTampilErrorActivity(
                this@MainActivityKotlin,
                "tc: ${e.message}",
                "savedDataToSharedPref"
            )
        }
    }

    //start area kirim foto
    private fun kirimAbsenVolley(
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
                    Log.d("debug_30-oct-23","gagal kirim absen karena: "+error.message)
                    Log.d("debug_30-oct-23","network response: "+error.networkResponse)
                    Log.d("debug_30-oct-23","error: "+error)
                    Log.d("debug_30-oct-23","localized message: "+error.localizedMessage)
                    Log.d("debug_30-oct-23","stacktrace to string: "+error.stackTraceToString())
                    Log.d("debug_30-oct-23","stacktrace: "+error.stackTrace)
                    Log.d("debug_30-oct-23","cause: "+error.cause)
                    Log.d("debug_30-oct-23","print stacktrace: "+error.printStackTrace())
                    Log.d("debug_30-oct-23","status code: "+error.networkResponse?.statusCode)

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
                    headerMap["Content-Type"] = "multipart/form-data; charset=utf-8"
                    //headerMap["Authorization"] = "Bearer $authorizationBearToken"
                    headerMap["Authorization"] = "Bearer 3418|mU3QLUfo8sUdEFy1uxHfIu4rvOHjuaEEYo5cm6k8"
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
            signOutNormally(getTokenFromSharedPref())
        }
        popup_logout_batal.setOnClickListener { dialog.dismiss() }
    }
//    end popup logout

    //    end popup logout
    private fun signOutNormally(authorizationBearToken: String?) {
        Log.d("2-nov-23","signout()()")


        fadeOutLayoutUtama()
        showPbMainActivity()

        var call : Call<LoginModel> = apiInterface.logout("Bearer $authorizationBearToken")

        call.enqueue(object : Callback<LoginModel> {
            override fun onResponse(
                call: Call<LoginModel>,
                response: retrofit2.Response<LoginModel>
            ) {
                processLogout(true)
            }

            override fun onFailure(call: Call<LoginModel>, t: Throwable?) {
                processLogout(true)
            }


        })

    }

    private fun showPbMainActivity() {
        pb_main_activity.visibility = View.VISIBLE
    }

    private fun hidePbMainActivity() {
        pb_main_activity.visibility = View.GONE
    }

    private fun fadeOutLayoutUtama() {
        ly_utama.startAnimation(fade_out_permanent)
    }

    private fun fadeInLayoutUtama() {
        ly_utama.startAnimation(fade_in)
    }


    fun initSqlite() {
         context = this@MainActivityKotlin // Gantilah ini dengan konteks aplikasi Anda
         dbHelper = DatabaseHelper(context)
         dataHandler = DataHandler(context)
    }




    fun validateToken() { //true = token masih aktif, false = token sudah expired
        fadeOutLayoutUtama()
        showPbMainActivity()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response =
                    apiInterface.validateToken("Bearer ${getTokenFromSharedPref()}")?.awaitResponse()
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main){
                        fadeInLayoutUtama()
                        hidePbMainActivity()
                    }
                } else if (response.code() == 401) {
                    Log.d("debug_2-nov-23", "response 401x")
                    withContext(Dispatchers.Main){
                        fadeInLayoutUtama()
                        hidePbMainActivity()
                        processLogout(false)
                    }
                    //unauthorized, paksa logout pengguna agar login lagi dan mendapatkan token yang baru
                    //lanjut disini
                } else {
                    withContext(Dispatchers.Main) {
                        fadeInLayoutUtama()
                        hidePbMainActivity()
                    }

                    //gagal namun bukan unauthorized, tidak perlu di logout paksa
                }
            }
            catch (e:Exception){
                //Biasanya karena koneksi error internet
                withContext(Dispatchers.Main) {
                    fadeInLayoutUtama()
                    hidePbMainActivity()
                }
            }
        }
    }


    private fun processLogout(isSignOutNormally:Boolean) { //signout normal = true jika user logout dari menu logout, jika force logout maka = false
        val nip = SharedPreferencesHelper.read(SharedPreferencesHelper.NIP,"")
        SharedPreferencesHelper.removesesion(applicationContext)
        this@MainActivityKotlin.finish()
        if(isSignOutNormally) { //jika logout secara normal (lewat menu logout), maka TIDAK PERLU passing nip ke halaman login
            startActivity(Intent(this@MainActivityKotlin, LoginActivity::class.java))
        }
        else{
            //jika logout secara tidak normal (force logout), maka passing nip ke halaman login
            var intent:Intent = Intent(this@MainActivityKotlin, LoginActivity::class.java)
            intent.putExtra("nip_force_logout",nip)
            startActivity(intent)
        }
    }



}