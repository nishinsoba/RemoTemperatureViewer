package com.nishinsoba.remotemperature.fragment

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.nishinsoba.remotemperature.viewmodel.RemoTemperatureViewModel
import java.util.*

/**
 * 時刻選択ツール
 */
class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener{
    private val model by activityViewModels<RemoTemperatureViewModel>()

    /**
     * 時刻選択ダイアログを表示する時に呼ばれる
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar :Calendar?
        //tagをチェックして、以前に入力された値を取り出してデフォルトの値とする
        if (tag == "FROM_TIME"){
            calendar = model.from.value
        }else if (tag == "TO_TIME"){
            calendar = model.to.value
        }else{
            //万が一tagが誤っていた場合は現在の日時をデフォルトとする
            calendar = Calendar.getInstance()
        }
        val hour = calendar!!.get(Calendar.HOUR_OF_DAY)
        val minute = calendar!!.get(Calendar.MINUTE)
        return TimePickerDialog(this.context as Context, this, hour, minute, DateFormat.is24HourFormat(activity))
    }

    /**
     * ユーザが時刻を入力したとき呼ばれる
     */
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        //tagをチェックして、FROMかTOの適切な方をアップデートする
        if (tag == "FROM_TIME"){
            val calendar = model.from.value
            calendar?.let {
                it.set(Calendar.HOUR_OF_DAY,hourOfDay)
                it.set(Calendar.MILLISECOND,minute)
                model.updateFromCalender(it)
            }
        }else if (tag == "TO_TIME"){
            val calendar = model.to.value
            calendar?.let {
                it.set(Calendar.HOUR_OF_DAY,hourOfDay)
                it.set(Calendar.MILLISECOND,minute)
                model.updateToCalender(it)
            }
        }
    }
}