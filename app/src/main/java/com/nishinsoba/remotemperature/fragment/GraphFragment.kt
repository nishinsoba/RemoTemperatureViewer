package com.nishinsoba.remotemperature.fragment

import android.content.Context
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.nishinsoba.remotemperature.R
import com.nishinsoba.remotemperature.viewmodel.RemoTemperatureViewModel
import java.text.SimpleDateFormat
import kotlin.math.roundToInt


class GraphFragment : Fragment(R.layout.fragment_graph) {
    private val model by activityViewModels<RemoTemperatureViewModel>()
    lateinit var lineChartView: LineChart
    lateinit var progressView: ProgressBar
    private var mBackButtonCallback: OnBackPressedCallback? = null
    private var dialog:AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textView = view.findViewById<TextView>(R.id.text_view)
        //ローディング表示
        progressView = view.findViewById<ProgressBar>(R.id.progress_bar)

        //折れ線グラフ表示View
        lineChartView = view.findViewById<LineChart>(R.id.line_chart_view)
        lineChartView.visibility = View.INVISIBLE
        //説明文の部分を表示しない
        lineChartView.description.isEnabled = false

        //X軸のラベルの表示設定
        lineChartView.xAxis.apply {
            isEnabled = true
            labelRotationAngle = -45F   //X軸のラベルを斜め45度で表示する
            position = XAxis.XAxisPosition.BOTTOM_INSIDE    //X軸のラベルを底部の内側に表示する
        }
        //左Y軸の設定
        lineChartView.axisLeft.apply {
            isEnabled = true
            textColor = android.graphics.Color.BLACK
        }
        //右Y軸の設定
        lineChartView.axisRight.apply {
            isEnabled = false   //表示しない
        }

        lineChartView.setBackgroundColor(android.graphics.Color.WHITE)

        //ViewModel内のresultを監視
        model.result.observe(viewLifecycleOwner, Observer { resultPair ->
            if (resultPair?.first == 401 || resultPair?.first == 403){
                // トークンが正しくない場合

                //ローディング表示を消す
                progressView.visibility = View.INVISIBLE

                // ダイアログを二重表示しない
                if( dialog == null || dialog!!.isShowing() == false ) {
                    val sharedPref = requireActivity().getSharedPreferences("remo_viewer_setting", Context.MODE_PRIVATE)

                    //トークン入力ダイアログを表示する
                    val myEdit = EditText(activity)
                    dialog = AlertDialog.Builder(requireActivity())
                        .setTitle("トークンが正しくありません。再度入力してください")
                        .setView(myEdit)
                        .setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                            // OKボタン押したときの処理
                            val inputText = myEdit.getText().toString()
                            if (!inputText.isEmpty()) {
                                sharedPref.edit().putString("token", inputText).commit()
                            }
                        })
                        .show()
                }


            }else if (resultPair != null && resultPair.first != 200){
                showDialog()
            }
            resultPair?.second?.let { result ->
                if (result.resultMessage == "SUCCESS") {
                    //小数第一位まで四捨五入
                    val roundedOutDoorAverage =
                        (result.averageOutdoorTemperature * 10.0).roundToInt() / 10.0
                    val roundedRoomAverage =
                        (result.averageRoomTemperature * 10.0).roundToInt() / 10.0
                    textView.text =
                        "データ期間 : ${result.startDateTime} ~ ${result.endDateTime}\n外気温の平均 : $roundedOutDoorAverage ℃\n室内気温の平均 : $roundedRoomAverage ℃"

                    //グラフ描画のViewにデータを注入
                    lineChartView.data = model.buildLineData(result)

                    //X軸の日付 yyyy?MM/dd HH:mm:ss形式の文字列をMM/dd HH:mm表記にする
                    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    val sdf2 = SimpleDateFormat("MM/dd HH:mm")
                    val xStr = result.remoData.map { sdf2.format(sdf.parse(it.dateTime)) }

                    lineChartView.xAxis.valueFormatter = IndexAxisValueFormatter(xStr)

                    //グラフに設定を反映
                    lineChartView.invalidate()
                    //ローディング表示を消す
                    progressView.visibility = View.INVISIBLE
                    lineChartView.visibility = View.VISIBLE


                }
            }
        })

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBackButtonCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //イベントを一度だけ処理させるため、バックキーが押されたときnullを流してリセットする
                model.resetResult()
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this,
            mBackButtonCallback as OnBackPressedCallback
        )
    }

    override fun onDestroy() {
        mBackButtonCallback!!.remove()
        super.onDestroy()
    }


    private fun showDialog() {
        activity?.let {
            //ダイアログを二重表示しない
            if( dialog != null && dialog!!.isShowing() ) {
                return
            }
            dialog = AlertDialog.Builder(it)
                .setTitle("ERROR！")
                .setMessage("データ取得に失敗しました。")
                .setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                })
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            // 画面の回転はセンサーに従う
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        }
    }

}