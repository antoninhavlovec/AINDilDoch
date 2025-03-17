package com.example.AINDilDoch

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.AINDilDoch.R

class EmployeeAdapter(private val employees: List<Employee>) :
    RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.employee_list_item, parent, false)
        return EmployeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val employee = employees[position]
        holder.name.text = employee.name
        holder.department.text = employee.department
        holder.startDate.text = employee.startDate
        holder.endDate.text = employee.endDate
        holder.reference.text = employee.reference
        holder.cisloZakazky.text = employee.cisloZakazky

        // Dynamické obarvování podle hodnoty pole "department"
        val department = employee.department
        val endDate = employee.endDate
        val cislozakazky = employee.cisloZakazky

        val backgroundColor = when (department) {
            "Lékař" -> Color.parseColor("#C1E1FF") // Světle modrá pro Lékař
            "Nemoc" -> Color.parseColor("#C1E1FF") // Světle modrá pro Nemoc
            "Dovolená" -> Color.parseColor("#E3FFC1") // Světle zelená pro Dovolená
            else -> Color.TRANSPARENT // Výchozí barva
        }
        holder.itemView.setBackgroundColor(backgroundColor)

        // Podmínky pro změnu barvy textu
//        if (endDate.isNotBlank() && endDate != "N/A" && cislozakazky.equals("0000006", ignoreCase = true)) {
//            holder.name.setTextColor(Color.parseColor("#4A148C"))
////            holder.name.setTextColor(Color.RED)//červená
//            //holder.department.setTextColor(Color.RED)
////            holder.startDate.setTextColor(Color.RED)
////            holder.endDate.setTextColor(Color.RED)
////            holder.cisloZakazky.setTextColor(Color.RED)
//        } else {
//            // Výchozí barva textu
//            holder.name.setTextColor(Color.parseColor("#009900"))//zelená
//            //holder.department.setTextColor(Color.GREEN)
//           // holder.startDate.setTextColor(Color.GREEN)
//            //holder.endDate.setTextColor(Color.GREEN)
//        }

        if /*prestavka*/(cislozakazky.equals("0000006", ignoreCase = true)) {
            holder.name.setTextColor(Color.parseColor("#4A148C"))
//            holder.name.setTextColor(Color.RED)//červená
                holder.department.setTextColor(Color.parseColor("#4A148C"))
//            holder.startDate.setTextColor(Color.RED)
//            holder.endDate.setTextColor(Color.RED)
           holder.cisloZakazky.setTextColor(Color.parseColor("#4A148C"))
        } else /*obed*/
            if (cislozakazky.equals("0000005", ignoreCase = true)) {
                holder.name.setTextColor(Color.parseColor("#00BFA5"))
    //            holder.name.setTextColor(Color.RED)//červená
                holder.department.setTextColor(Color.parseColor("#00BFA5"))
    //            holder.startDate.setTextColor(Color.RED)
    //            holder.endDate.setTextColor(Color.RED)
                holder.cisloZakazky.setTextColor(Color.parseColor("#00BFA5"))
                }else /*skoleni*/
                    if (cislozakazky.equals("0000007", ignoreCase = true)) {
                    holder.name.setTextColor(Color.parseColor("#FF5722"))
                    //            holder.name.setTextColor(Color.RED)//červená
                    holder.department.setTextColor(Color.parseColor("#FF5722"))
                    //            holder.startDate.setTextColor(Color.RED)
                    //            holder.endDate.setTextColor(Color.RED)
                    holder.cisloZakazky.setTextColor(Color.parseColor("#FF5722"))}
                    else /*lekar*/
                        if (cislozakazky.equals("0000012", ignoreCase = true)) {
                            holder.name.setTextColor(Color.parseColor("#D50000"))
                            //            holder.name.setTextColor(Color.RED)//červená
                            holder.department.setTextColor(Color.parseColor("#D50000"))
                            //            holder.startDate.setTextColor(Color.RED)
                            //            holder.endDate.setTextColor(Color.RED)
                            holder.cisloZakazky.setTextColor(Color.parseColor("#D50000"))}
                        else /*uklid*/
                            if (cislozakazky.equals("0000017", ignoreCase = true)) {
                                holder.name.setTextColor(Color.parseColor("#00BCD4"))
                                //            holder.name.setTextColor(Color.RED)//červená
                                holder.department.setTextColor(Color.parseColor("#00BCD4"))
                                //            holder.startDate.setTextColor(Color.RED)
                                //            holder.endDate.setTextColor(Color.RED)
                                holder.cisloZakazky.setTextColor(Color.parseColor("#00BCD4"))}
                            else /*pracuje*/
                                 {  holder.name.setTextColor(Color.parseColor("#2196F3"))
                                    //            holder.name.setTextColor(Color.RED)//červená
                                    holder.department.setTextColor(Color.parseColor("#2196F3"))
                                    //            holder.startDate.setTextColor(Color.RED)
                                    //            holder.endDate.setTextColor(Color.RED)
                                    holder.cisloZakazky.setTextColor(Color.parseColor("#2196F3"))
                                 }
        /*
        *#2196F3 příchod/odchod
        * #D50000 lekar 0000012
        * #00BFA5 obed 0000005
        * #FF5722 skoleni 0000007
        * #4A148C pauza 0000006
        * #00BCD4 uklid 0000017
        * */
    }

    override fun getItemCount(): Int = employees.size

    class EmployeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.employeeName)
        val department: TextView = itemView.findViewById(R.id.employeeDepartment)
        val startDate: TextView = itemView.findViewById(R.id.employeeStartDate)
        val endDate: TextView = itemView.findViewById(R.id.employeeEndDate)
        val reference: TextView = itemView.findViewById(R.id.employeeReference)
        val cisloZakazky: TextView = itemView.findViewById(R.id.employeeCisloZak)
    }
}