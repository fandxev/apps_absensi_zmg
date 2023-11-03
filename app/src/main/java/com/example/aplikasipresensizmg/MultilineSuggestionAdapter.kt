//package com.example.aplikasipresensizmg
//
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ArrayAdapter
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//
//class MultilineSuggestionAdapter(
//    context: AppCompatActivity,
//    resource: Int,
//    suggestions: Array<LoginActivity.SuggestModel>
//) : ArrayAdapter<LoginActivity.SuggestModel>(context, resource, suggestions) {
//    init {
//        Log.d("debug_02-nov-23","Multiline init")
//        for (i in suggestions.indices)
//        {
////            Log.d("debug_02-nov-23","nip MULTILINE:"+suggestions[i].nip)
////            Log.d("debug_02-nov-23","nama MULTILINE:"+suggestions[i].name)
////            Log.d("debug_02-nov-23","idUserInServer MULTILINE:"+suggestions[i].idUserInServer)
////            Log.d("debug_02-nov-23","role MULTILINE:"+suggestions[i].role)
//        }
//    }
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        Toast.makeText(context,"getView ok",Toast.LENGTH_SHORT).show()
//        //val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.layout_suggest_login_nip, parent, false)
//
//        val view: View
//        if (convertView == null) {
//            val inflater = LayoutInflater.from(context)
//            view = inflater.inflate(R.layout.layout_suggest_login_nip, null)
//        } else {
//            view = convertView
//        }
//
//        val item = getItem(position)
//        val tv_suggest_nip = view.findViewById<TextView>(R.id.tv_suggest_nip)
//        val tv_suggest_nama = view.findViewById<TextView>(R.id.tv_suggest_nama)
//
//
////        tv_suggest_nip.text = item?.nip
////        tv_suggest_nama.text = item?.name
//
//                tv_suggest_nip.text = item?.description
//        tv_suggest_nama.text = item?.itemText
//
//        return view
//    }
//
//
//
//}