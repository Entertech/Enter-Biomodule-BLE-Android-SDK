package cn.entertech.flowtimeble.device

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import cn.entertech.flowtimeble.R

class BleFunctionListAdapter(private val functionList: MutableList<BleFunctionUiBean> = mutableListOf()) :
    RecyclerView.Adapter<BleFunctionListAdapter.BleFunctionListVH>() {

    interface IBleFunctionClick {
        fun onClick(bleFunctionFlag: BleFunction)
    }

    var bleFunctionClick: IBleFunctionClick? = null
    private val enabledFunctionSet = mutableSetOf<BleFunction>()

    class BleFunctionListVH(rootView: View, val btnBleFunctionName: Button?) :
        RecyclerView.ViewHolder(rootView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleFunctionListVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ble_function_list_item_layout, parent, false)
        val btnBleFunctionName = view.findViewById<Button>(R.id.btnBleFunctionName)
        return BleFunctionListVH(view, btnBleFunctionName)
    }

    override fun onBindViewHolder(holder: BleFunctionListVH, position: Int) {
        val item = functionList[position]
        val isEnabled = enabledFunctionSet.contains(item.functionFlag)
        val button = holder.btnBleFunctionName ?: return
        button.text = button.context.getString(item.functionFlag.desStringResId)
        button.isEnabled = isEnabled
        button.alpha = if (isEnabled) 1f else 0.45f
        button.setOnClickListener {
            bleFunctionClick?.onClick(item.functionFlag)
        }
    }

    override fun getItemCount(): Int {
        return functionList.size
    }

    fun setNewData(newData: List<BleFunctionUiBean>) {
        functionList.clear()
        functionList.addAll(newData)
        notifyDataSetChanged()
    }

    fun setEnabledFunctions(enabledFunctions: Set<BleFunction>) {
        enabledFunctionSet.clear()
        enabledFunctionSet.addAll(enabledFunctions)
        notifyDataSetChanged()
    }
}
