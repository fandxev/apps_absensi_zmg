package com.example.aplikasipresensizmg

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import com.example.aplikasipresensizmg.helper.RedirectToTampilErrorActivity
import com.example.aplikasipresensizmg.helper.login.LoginHelper
import com.example.aplikasipresensizmg.model.LoginModel
import com.example.aplikasipresensizmg.helper.retrofit.ApiInterface
import com.example.aplikasipresensizmg.helper.sharedpreferences.SharedPreferencesHelper
import com.example.aplikasipresensizmg.helper.sqlite.DataHandler
import com.example.aplikasipresensizmg.helper.sqlite.DatabaseHelper
import com.example.aplikasipresensizmg.helper.sqlite.HistoriUserData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    val apiInterface = ApiInterface.create()
   lateinit var ic_error : ImageView
   lateinit var tv_keterangan_error : TextView
   lateinit var btn_login: RelativeLayout
   lateinit var fade_in: Animation
   lateinit var label_login : TextView
   lateinit var progress_login : ProgressBar



   var CURRENT_STATE_LOGIN : String = ""
   val STATE_LOADING_LOGIN : String = "loading"
    val STATE_IDLE_LOGIN : String = "idle"
   var DUMMY_NIP:String = "1234"

    //SQLITE
    lateinit var context : Context
    lateinit var dbHelper : DatabaseHelper
    lateinit var dataHandler : DataHandler

    lateinit var edt_nip : AutoCompleteTextView


    lateinit var sharedPreferences : android.content.SharedPreferences

    val suggestions = arrayOf("Apple", "Banana", "Cherry","Charade","Corot \n spesial Edition","Cicak", "Date", "Grape", "Lemon", "Orange", "Pear")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page_activity_login)
        initSqlite()
        try {
            initSharedPreferences()
            initAnimation()
            findViewByIdAllComponent()
            setListenerAllComponent()

            checkLogin()
            initAutoSuggest(getDataLoginSuggest())


            checkIntentExtra()
        }
        catch (e:Exception){
            RedirectToTampilErrorActivity(
                this@LoginActivity,
                "tc: ${e.message}",
                "onCreateLoginActivity"
            )
        }
    }

    fun initSharedPreferences() {
        try {
            SharedPreferencesHelper.init(applicationContext)
        } catch (e: Exception) {
            RedirectToTampilErrorActivity(
                this@LoginActivity,
                "tc: ${e.message}",
                "initSharedPref"
            )
        }
    }


    fun initAnimation(){
        fade_in = AnimationUtils.loadAnimation(this,R.anim.fade_in)
    }


    fun findViewByIdAllComponent(){
        edt_nip = findViewById<AutoCompleteTextView>(R.id.edt_nip)
        ic_error = findViewById<ImageView>(R.id.ic_error)
        tv_keterangan_error = findViewById<TextView>(R.id.tv_keterangan_error)
        btn_login = findViewById(R.id.btn_login)
        label_login = findViewById(R.id.label_login)
        progress_login = findViewById(R.id.progress_login)
    }

    fun initAutoSuggest(dataSuggest:ArrayList<HistoriUserData>){

        Log.d("debug_02-nov-23","initAutoSuggest()")
        var dataForAdapter: ArrayList<String> = ArrayList<String>()
        for (i in dataSuggest.indices)
        {
            Log.d("debug_3-nov_23","data dari db: "+dataSuggest[i].nip)
            dataForAdapter.add("nip: ${dataSuggest[i].nip}. ${dataSuggest[i].name}")
        }

        for (i in dataForAdapter.indices)
        {
            Log.d("debug_3-nov_23","data untuk adapter: "+ dataForAdapter[i])
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dataForAdapter)
        edt_nip.setAdapter(adapter)

        edt_nip.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            val nip = keepOnlyNumbers(selectedItem)
            edt_nip.setText(nip)

        }
    }

    fun keepOnlyNumbers(input:String):String{
        //Menggunakan reguler ekspresi untuk menghapus semua karakter selain angka
        return input.replace(Regex("[^0-9]"),"")
    }

    fun checkOnlyNumbersAllowed(input:String):Boolean{ //true = onlyNumber. false = contain other then number
        setTextError("")
        hideError()
        if (input.isBlank() || !input.matches(Regex("^[0-9]*\$"))) {
            // Input tidak valid, tampilkan peringatan
            setTextError("Harap masukkan nomor nip saja")
            showError()
            return false
        }
        return true
    }


    private fun getDataLoginSuggest():ArrayList<HistoriUserData>{
        return dataHandler.getFewNewestData(4)
    }

    fun showError(){
        tv_keterangan_error.startAnimation(fade_in)
        ic_error.startAnimation(fade_in)
        tv_keterangan_error.visibility = View.VISIBLE
        ic_error.visibility = View.VISIBLE
    }

    fun hideError(){
        tv_keterangan_error.visibility = View.GONE
        ic_error.visibility = View.GONE
    }

    fun setListenerAllComponent(){
        btn_login.setOnClickListener {
            if(checkOnlyNumbersAllowed(edt_nip.text.toString()))
            login(edt_nip.text.toString())
        }
    }

    fun toggleStateBtnLogin(state:String){
        try {
            when (state) {
                STATE_IDLE_LOGIN -> run {
                    progress_login.visibility = View.GONE
                    label_login.startAnimation(fade_in)
                    label_login.visibility = View.VISIBLE
                    CURRENT_STATE_LOGIN = STATE_IDLE_LOGIN
                }
                STATE_LOADING_LOGIN -> run {
                    progress_login.startAnimation(fade_in)
                    progress_login.visibility = View.VISIBLE
                    label_login.visibility = View.GONE
                    CURRENT_STATE_LOGIN = STATE_LOADING_LOGIN
                }
            }
        } catch (e: Exception) {
            RedirectToTampilErrorActivity(
                this@LoginActivity,
                "tc: ${e.message}",
                "toggleStateBtnLogin"
            )
        }
    }


    fun login(nip : String){
        try {

            Log.d("22_agustus_2022", "LoginActivity. nip: ${nip}")
            if (CURRENT_STATE_LOGIN.equals(STATE_LOADING_LOGIN)) {
                return
            }

            toggleStateBtnLogin(STATE_LOADING_LOGIN)
            hideError()
            apiInterface.login(nip).enqueue(object : Callback<LoginModel> {
                override fun onResponse(
                    call: Call<LoginModel>,
                    response: Response<LoginModel>
                ) {
                    toggleStateBtnLogin(STATE_IDLE_LOGIN)
                    if (response?.body() != null) {

                        val data = response.body()
                        var status: Int? = data?.status
                        if (status == 0) {
                            setTextError("Kesalahan respon dari server, silahkan hubungi administrator")
                            Log.d("22_agustus_2022", "LoginActivity. message: " + data?.message)
                            showError()
                        } else {

                            saveDataToSharedPref(
                                data!!.access_token,
                                data.user.id,
                                data.user.name,
                                data.user.nip,
                                data.user.role,
                                data.user.status
                            )
                            goToMainActivity()
                        }
                    } else {
                        Log.d("15_agustus", "response login null: ")
                        Log.d(
                            "22_agustus_2022",
                            "LoginActivity. status: " + response.body()?.status
                        )
                        setTextError("NIP yang anda masukkan tidak terdaftar")

                        showError()
                    }
                }

                override fun onFailure(call: Call<LoginModel>, t: Throwable) {
                    toggleStateBtnLogin(STATE_IDLE_LOGIN)
                    Log.d("15_agustus", "onFailure: " + t.message)
                    setTextError("Terjadi kesalahan. Periksa koneksi internet anda")
                    showError()
                }
            })
        }
        catch (e:Exception){
            RedirectToTampilErrorActivity(
                this@LoginActivity,
                "tc: ${e.message}",
                "login2"
            )
        }
    }

    fun goToMainActivity(){
        try {
            var intent = Intent(this,MainActivityKotlin::class.java)
            intent.putExtra("from_login",true)
            startActivity(intent)
        } catch (e: Exception) {
            RedirectToTampilErrorActivity(
                this@LoginActivity,
                "tc: ${e.message}",
                "goToMainActivity()"
            )
        }
    }

    fun checkLogin() {
        try {
            if (LoginHelper.checkLogin()) {
                val i = Intent(this, MainActivityKotlin::class.java)
                startActivity(i)
                finish()
            }
        } catch (e: Exception) {
            RedirectToTampilErrorActivity(
                this@LoginActivity,
                "tc: ${e.message}",
                "checkLogin"
            )
        }
    }

    fun insertToHistoriUserDB(idUserInServer:String,name:String,nip:String,role:String) {
// Menambahkan data ke database
        val insertedRowId = dataHandler.insertDataIfNotExists(idUserInServer,name,nip,role)
        if (insertedRowId > 0) {
            Log.d("debug_2-nov-23","Data berhasil ditambahkan dengan ID $insertedRowId")
        } else {
            Log.d("debug_2-nov-23","Gagal menambahkan data: $insertedRowId")
        }
    }

    fun setTextError(txt:String)
    {
        tv_keterangan_error.setText(txt)
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

                //set data history user yg pernah login untuk autosuggest
                val idUser= read(ID_USER,"")?:""
                val nip= read(NIP,"")?:""
                val name= read(NAME,"")?:""
                val role= read(ROLE,"")?:""
                insertToHistoriUserDB(idUser,name,nip,role)

            }
        } catch (e: Exception) {
            RedirectToTampilErrorActivity(
                this@LoginActivity,
                "tc: ${e.message}",
                "savedDataToSharedPref"
            )
        }
    }


    override fun onBackPressed() {
        finishAffinity()
        finish()
    }

    fun getHistoryUser(){
        val historyUser = dataHandler.getAllData()
    }

    fun initSqlite() {
        context = this@LoginActivity // Gantilah ini dengan konteks aplikasi Anda
        dbHelper = DatabaseHelper(context)
        dataHandler = DataHandler(context)
    }

    fun checkIntentExtra(){
        if(intent.hasExtra("nip_force_logout")) {
            edt_nip.setText(intent.getStringExtra("nip_force_logout"))
            setTextError("Sesi habis, silahkan login kembali")
            showError()
        }
    }





}


