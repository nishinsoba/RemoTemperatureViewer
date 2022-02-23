package com.nishinsoba.remotemperature.fragment

import android.content.Context
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.nishinsoba.remotemperature.R
import com.nishinsoba.remotemperature.viewmodel.RemoTemperatureViewModel
import java.util.*

class StartFragment : Fragment(R.layout.fragment_start) {

    private val model by activityViewModels<RemoTemperatureViewModel>()
    var dialog : AlertDialog? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //GetRemoInfoを叩くためのトークン
        var token = checkTokenExist()

        //ユーザが入力した日（From）を表示するView
        val fromDateParameterView = view.findViewById<TextView>(R.id.from_date_parameter)
        //初期表示は現在の日
        fromDateParameterView.text = model.calenderToStr(Calendar.getInstance(),"yyyy/MM/dd")
        //変更ボタンが押されたときDatePickerを表示する
        view.findViewById<AppCompatButton>(R.id.from_date_button).apply {
            setOnClickListener {
                DatePickerFragment().show(parentFragmentManager, "FROM_DATE")
            }
        }

        //ユーザが入力した時間（From）を表示するView
        val fromTimeParameterView = view.findViewById<TextView>(R.id.from_time_parameter)
        //初期表示は現在の時間
        fromTimeParameterView.text = model.calenderToStr(Calendar.getInstance(),"HH:mm")
        //変更ボタンが押されたときTimePickerを表示する
        view.findViewById<AppCompatButton>(R.id.from_time_button).apply {
            setOnClickListener {
                TimePickerFragment().show(parentFragmentManager, "FROM_TIME")
            }
        }

        //ViewModel内のfromを監視
        model.from.observe(viewLifecycleOwner, Observer {from ->
            fromDateParameterView.text = model.calenderToStr(from,"yyyy/MM/dd")
            fromTimeParameterView.text = model.calenderToStr(from,"HH:mm")
        })

        //ユーザが入力した日（To）を表示するView
        val toDateParameterView = view.findViewById<TextView>(R.id.to_date_parameter)
        //初期表示は現在の日
        toDateParameterView.text = model.calenderToStr(Calendar.getInstance(),"yyyy/MM/dd")
        //変更ボタンが押されたときDatePickerを表示する
        view.findViewById<AppCompatButton>(R.id.to_date_button).apply {
            setOnClickListener {
                DatePickerFragment().show(parentFragmentManager, "TO_DATE")
            }
        }

        //ユーザが入力した時間（To）を表示するView
        val toTimeParameterView = view.findViewById<TextView>(R.id.to_time_parameter)
        //初期表示は現在の時間
        toTimeParameterView.text = model.calenderToStr(Calendar.getInstance(),"HH:mm")
        //変更ボタンが押されたときDatePickerを表示する
        view.findViewById<AppCompatButton>(R.id.to_time_button).apply {
            setOnClickListener {
                TimePickerFragment().show(parentFragmentManager, "TO_TIME")
            }
        }

        //ViewModel内のtoを監視
        model.to.observe(viewLifecycleOwner, Observer {to ->
            toDateParameterView.text = model.calenderToStr(to,"yyyy/MM/dd")
            toTimeParameterView.text = model.calenderToStr(to,"HH:mm")
        })


        //一発変更ボタン（24時間）が押されたときViewModel内の値を書き換える
        view.findViewById<AppCompatButton>(R.id.one_day_button).apply {
            setOnClickListener {
                val now = Calendar.getInstance()
                model.updateToCalender(now)
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH,-1)
                model.updateFromCalender(calendar)
            }
        }

        //一発変更ボタン（一週間）が押されたときViewModel内の値を書き換える
        view.findViewById<AppCompatButton>(R.id.one_week_button).apply {
            setOnClickListener {
                val now = Calendar.getInstance()
                model.updateToCalender(now)
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH,-7)
                model.updateFromCalender(calendar)
            }
        }

        //OKボタンを押したときAPIをたたきつつ次の画面へ遷移する
        view.findViewById<AppCompatButton>(R.id.ok_button).apply {
            setOnClickListener {

                if (!model.checkFromAndTo()){
                    activity?.let {
                        AlertDialog.Builder(it)
                            .setTitle("ERROR！")
                            .setMessage("日時の設定が間違っています。")
                            .show()
                    }
                }else if (token == null){
                    token = checkTokenExist()
                }else{
                    model.callApi(token!!)
                    val action = StartFragmentDirections.actionStartFragmentToGraphFragment()
                    findNavController().navigate(action)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            // 縦向き固定
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    /**
     * GetRemoInfoを叩くためのトークンを持っているかチェックし、持っていなければ入力ダイアログを表示する
     */
    private fun checkTokenExist(): String?{
        val sharedPref = requireActivity().getSharedPreferences("remo_viewer_setting",Context.MODE_PRIVATE)
        var token = sharedPref.getString("token",null)
        if (token == null){
            // ダイアログを二重表示しない
            if( dialog != null && dialog!!.isShowing() ) {
                return null
            }
            // トークン入力ダイアログを表示する
            val myEdit = EditText(activity)
            dialog = AlertDialog.Builder(requireActivity())
                .setTitle("トークンを入力してください")
                .setView(myEdit)
                .setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                    // OKボタン押したときの処理
                    val inputText = myEdit.getText().toString()
                    if (!inputText.isEmpty()) {
                        sharedPref.edit().putString("token", inputText).apply()
                        token = inputText
                    }
                })
                .show()
        }
        return token
    }
}