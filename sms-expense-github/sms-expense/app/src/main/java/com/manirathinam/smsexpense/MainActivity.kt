package com.manirathinam.smsexpense

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

 data class Tx(val date: Long, val amount: Double, val merchant: String, val direction: String, val raw: String)

class MainActivity : AppCompatActivity() {
    private lateinit var list: LinearLayout
    private val req = 77
    override fun onCreate(b: Bundle?) { super.onCreate(b); buildUi(); if (!hasSms()) ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS), req) else scan() }
    private fun hasSms() = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    private fun buildUi() {
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(28,24,28,24) }
        val title=TextView(this).apply { text="SMS Expense Tracker"; textSize=26f; setPadding(0,0,0,8) }
        val sub=TextView(this).apply { text="Local-only • detects likely payment SMS"; textSize=14f }
        val scan=Button(this).apply { text="Scan payment SMS"; setOnClickListener { if(hasSms()) scan() else ActivityCompat.requestPermissions(this@MainActivity,arrayOf(Manifest.permission.READ_SMS),req) } }
        list=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        root.addView(title); root.addView(sub); root.addView(scan); root.addView(list, LinearLayout.LayoutParams(-1,0,1f)); setContentView(root)
    }
    private fun scan() {
        list.removeAllViews(); val items=mutableListOf<Tx>(); val c:Cursor?=contentResolver.query(Uri.parse("content://sms/inbox"), arrayOf("date","body","address"), null,null,"date DESC")
        c?.use { while(it.moveToNext()) { val body=it.getString(1) ?: ""; parse(body,it.getLong(0))?.let{t->items.add(t)} } }
        val total=items.filter{it.direction=="DEBIT"}.sumOf{it.amount}
        val head=TextView(this).apply { text="Detected ${items.size} payments  •  Expenses ₹${String.format(Locale.US,"%.2f",total)}"; textSize=17f; setPadding(0,14,0,14) }; list.addView(head)
        if(items.isEmpty()) { list.addView(TextView(this).apply{text="No matching payment SMS found. Banks use different formats; we can tune the parser if you share a redacted example."; textSize=15f; setPadding(0,12,0,12)}); return }
        items.forEach { t -> val card=TextView(this).apply { text="${fmt(t.date)}\n${t.direction}: ₹${String.format(Locale.US,"%.2f",t.amount)}  •  ${t.merchant}\n${t.raw.take(140)}"; textSize=15f; setPadding(18,16,18,16); setBackgroundColor(0xFFF3F4F6.toInt()) }; val lp=LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,12); list.addView(card,lp) }
    }
    private fun fmt(ms:Long)=SimpleDateFormat("dd MMM yyyy, hh:mm a",Locale.getDefault()).format(Date(ms))
    private fun parse(s:String,date:Long):Tx? {
        val x=s.replace("₹","Rs.",true)
        val amount=Pattern.compile("(?:rs\\.?|inr|₹)\\s*([0-9][0-9,]*(?:\\.[0-9]{1,2})?)",Pattern.CASE_INSENSITIVE).matcher(x)
        if(!amount.find()) return null
        val value=amount.group(1).replace(",","").toDoubleOrNull() ?: return null
        val lower=s.lowercase(Locale.ROOT)
        val debit=Regex("\\b(debited|spent|paid|purchase|withdrawn|sent|transferred|payment of)\\b").containsMatchIn(lower)
        val credit=Regex("\\b(credited|received|refund|cashback|salary)\\b").containsMatchIn(lower)
        if(!debit && !credit) return null
        val dir=if(debit) "DEBIT" else "CREDIT"
        val merchant=Regex("(?:at|to|from|via)\\s+([A-Za-z0-9.&'_-]{2,40})",RegexOption.IGNORE_CASE).find(s)?.groupValues?.get(1) ?: "Unknown"
        return Tx(date,value,merchant,dir,s)
    }
}
