package com.nishinsoba.remotemperature.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.nishinsoba.remotemperature.dataclass.GetRemoInfoResponse
import com.nishinsoba.remotemperature.repository.GetRemoInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RemoTemperatureViewModel : ViewModel() {

    //APIからのレスポンス
    private val mResult = MutableLiveData<Pair<Int,GetRemoInfoResponse?>?>()
    //取得対象日時（From）
    private val mFrom = MutableLiveData<Calendar>(Calendar.getInstance())
    //取得対象日時（To）
    private val mTo = MutableLiveData<Calendar>(Calendar.getInstance())

    val result: LiveData<Pair<Int,GetRemoInfoResponse?>?> = mResult
    val from: LiveData<Calendar> = mFrom
    val to: LiveData<Calendar> = mTo

    /**
     * ViewModel内のfromを更新する
     */
    fun updateFromCalender(entered : Calendar){
        mFrom.postValue(entered)
    }

    /**
     * ViewModel内のtoを更新する
     */
    fun updateToCalender(entered : Calendar){
        mTo.postValue(entered)
    }

    /**
     * APIをたたく
     */
    fun callApi(token: String) {
        val repository = GetRemoInfoRepository()
        viewModelScope.launch(Dispatchers.IO) {
            if (from.value != null && to.value != null){
                val fromStr = calenderToStr(from.value!!,"yyyyMMddHHmmss")
                val toStr = calenderToStr(to.value!!,"yyyyMMddHHmmss")
                val response =
                    repository.get(fromStr,toStr,token)

                mResult.postValue(response)
            }
        }
    }

    /**
     * 棒グラフの準備をする
     */
    fun buildLineData(getRemoInfoResponse: GetRemoInfoResponse): LineData {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val x = getRemoInfoResponse.remoData.map { sdf.parse(it.dateTime) }

        //折れ線として表示したいデータの集合のListを作る
        //y軸1 外気温
        val y1 = getRemoInfoResponse.remoData.map { it.outdoorTemperature }

        //y軸2 室内気温
        val y2 = getRemoInfoResponse.remoData.map { it.roomTemperature }

        //y軸3 エアコン設定温度 isUsingAirconがfalseのときはエアコンを使用していない = 設定温度0℃とする
        val y3 = getRemoInfoResponse.remoData.map { if (it.isUsingAircon.equals(1)){ it.airconTemperature} else 0F }

        //EntryのListにグラフとして表示するデータのインデックスと値を紐づける
        val outdoorTemperatureEntryList = mutableListOf<Entry>()
        val roomTemperatureEntryList = mutableListOf<Entry>()
        val airconTemperatureEntryList = mutableListOf<Entry>()
        for (i in x.indices) {
            outdoorTemperatureEntryList.add(Entry(i.toFloat(), y1[i])) //インデックス, 外気温
            roomTemperatureEntryList.add(Entry(i.toFloat(), y2[i])) //インデックス, 室内気温
            airconTemperatureEntryList.add(Entry(i.toFloat(), y3[i]))   //インデックス, エアコン設定温度

        }

        //EntryのList（折れ線）とそのLabel（説明）を紐づける
        val outdoorLineSet = LineDataSet(outdoorTemperatureEntryList, "外気温")
        //折れ線の表示設定
        outdoorLineSet.color = android.graphics.Color.BLUE  //線の色
        outdoorLineSet.lineWidth = 2F   //線の太さ
        outdoorLineSet.setDrawCircles(false)    //プロットの円を表示しない

        val roomLineSet = LineDataSet(roomTemperatureEntryList, "室内気温")
        roomLineSet.color = android.graphics.Color.RED
        roomLineSet.lineWidth = 2F
        roomLineSet.setDrawCircles(false)


        val airconLineSet = LineDataSet(airconTemperatureEntryList, "エアコン設定温度 ※0℃表示の時はエアコンをつけていない")
        airconLineSet.color = android.graphics.Color.GREEN
        airconLineSet.lineWidth = 2F
        airconLineSet.setDrawCircles(false)

        val lineDataSets = mutableListOf<ILineDataSet>(outdoorLineSet, roomLineSet, airconLineSet)

        return LineData(lineDataSets)

    }

    /**
     * Calenderクラスをpatternで指定した形の文字列に変換する
     */
    fun calenderToStr(calendar: Calendar, pattern: String) :String{
        val sdf = SimpleDateFormat(pattern)
        return sdf.format(calendar.time)
    }

    /**
     * イベントを一度だけ処理させるため、nullを流してリセットする
     */
    fun resetResult(){
        mResult.postValue(null)
    }

    /**
     * FromとToの時系列が矛盾していないかチェックする
     * trueであれば矛盾していない
     */
    fun checkFromAndTo() :Boolean{
        return from.value!!.compareTo(to.value!!) <= 0
    }
}