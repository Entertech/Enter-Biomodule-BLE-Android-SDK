package cn.entertech.flowtimeble.skin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.entertech.flowtimeble.R

class SkinConductivityRecordActivity : AppCompatActivity() {


    private var rvSkinDataRecord:RecyclerView?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.skin_conductivity_record_activity)
        initView()
        initData()
    }

    private fun initData(){
        val fileList=SkinConductivityHelper
            .getSaveFileDirectory(this).listFiles()?: emptyArray()
        val adapter=SkinConductivityRecordAdapter(fileList,this)
        rvSkinDataRecord?.adapter=adapter
    }

    private fun initView(){
        rvSkinDataRecord=findViewById(R.id.rvSkinDataRecord)
        rvSkinDataRecord?.layoutManager=LinearLayoutManager(this)
    }
}