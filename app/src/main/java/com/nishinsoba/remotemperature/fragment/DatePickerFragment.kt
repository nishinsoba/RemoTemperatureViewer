package com.nishinsoba.remotemperature.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.nishinsoba.remotemperature.viewmodel.RemoTemperatureViewModel
import java.util.*

/**
 * 日付選択ツール
 */
class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    private val model by activityViewModels<RemoTemperatureViewModel>()

    /**
     * 日付選択ダイアログを表示するときに呼ばれる
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar :Calendar?
        //tagをチェックして、以前に入力された値を取り出してデフォルトの値とする
        if (tag == "FROM_DATE"){
            calendar = model.from.value
        }else if (tag == "TO_DATE"){
            calendar = model.to.value
        }else{
            //万が一tagが誤っていた場合は現在の日時をデフォルトとする
            calendar = Calendar.getInstance()
        }

        val year = calendar!!.get(Calendar.YEAR)
        val month = calendar!!.get(Calendar.MONTH)
        val day = calendar!!.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(this.context as Context, this, year, month, day)
    }

    /**
     * OKボタンを押したときに呼ばれる
     */
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        //tagをチェックして、FROMかTOの適切な方をアップデートする
        if (tag == "FROM_DATE"){
            val calendar = model.from.value
            calendar?.let {
                it.set(year,month,day)
                model.updateFromCalender(it)
            }
        }else if (tag == "TO_DATE"){
            val calendar = model.to.value
            calendar?.let {
                it.set(year,month,day)
                model.updateToCalender(it)
            }
        }
    }
}