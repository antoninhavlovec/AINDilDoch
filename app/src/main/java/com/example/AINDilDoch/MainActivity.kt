package com.example.AINDilDoch

import android.app.ActivityManager
import android.app.ProgressDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.launch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.ranges.coerceAtLeast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *Zprovoznění nového tabletu
 * Tablet
    * zapnout optimalizaci baterie
    * vypnout One Vision
 * Odemykání a zabezpčení > další nastavení zabezpečení > aplikace pro správ zařízení >AIN dílna >aktivovat

 * Povolení USB ladění: Na tabletu přejdi do "Nastavení" -> "O telefonu" (nebo "O zařízení") a několikrát klepni na "Číslo sestavení" (build number), dokud se nezobrazí hláška, že jsi vývojář. Pak přejdi do "Nastavení" -> "Systém" -> "Možnosti pro vývojáře" a zapni "Ladění USB" (USB debugging).
 * Připojení tabletu k počítači: Připoj tablet k počítači pomocí USB kabelu.
 * Kontrola připojení: Do terminálu zadej adb devices. Zobrazí se ti připojená zařízení.
 * Nastavení Device Owner: Použij ADB k nastavení tvé aplikace jako Device Owner. V terminálu nebo příkazovém řádku na počítači zadej:

 * adb shell dpm set-device-owner com.example.AINDilDoch/.YourDeviceAdminReceiver
 *
 * adb devices
 *
 * adb shell dpm remove-active-admin com.example.AINDilDoch/.YourDeviceAdminReceiver
 *
 * případně
 * adb -s HVA6AP0X shell am force-stop com.example.AINDilDoch
 *
 * naistalovat
 *  adb shell dpm set-device-owner com.example.AINDilDoch/.YourDeviceAdminReceiver
 *
 * Verze
 *
 *   * verze 20250307
 *   * verze 20250317
     *   upraven čašovač na načítání přehledu zaměstanců - fetchEmployeeOverview
     *   napojení na GIT
         *   1.git status (zkontrolovat stav)
         *   2.git add . (přidat soubory do staging area)
         *   3.git commit -m "20250612" (vytvořit commit)
         *   4.git branch -M main (Nastavit výchozí větev)
         *   5.git push -u origin main (nahrát na GitHub)
 *   * verze 20250415
 *   * přidán zobrazení času o délce zakázky v přehledu zakázek
 *   * verze 20250612
 *   upraven vzhled respektujicí světlé nebo tmavé téma v androidu
*/

class MainActivity : AppCompatActivity() {

    private lateinit var inputField: EditText
    private lateinit var nameField: TextView
    private lateinit var surnameField: TextView
    private lateinit var exp1Field: TextView
    private lateinit var exp2Field: TextView
    private lateinit var arrivalButton: Button
    private lateinit var obedButton: Button
    private lateinit var lekarButton: Button
    private lateinit var skoleniButton: Button
    private lateinit var prestavButton: Button
    private lateinit var settingsButton: Button
    private lateinit var typ_doch: TextView

    private var progressDialog: ProgressDialog? = null

    private var employeeNumber: String? = null
    private var typDoch: String? = null
    private var systemInputRecordNumber: Int? = null // Přidána proměnná pro nové pole
    private var podklad_doch: String? = null
    private var username: String? = "api"
    private var password: String? = "844a12ca08f959706a078f345067461a71c894151af7deb685d96675f9b0bda0378d6b6de12496aef5a96e4d88f7540374c615e6408e6ad4e2fc1102a1a87b9a187683504f20149b62ab7198615d906e15103160d0277f9d72775ff509ed82cfbebcf763e83dbdd9d3b0db78320313fa7c2cb09630452886c78683552b2c2951"
    var delay: Long = 0L // Výchozí hodnota: 30 sekund

    private lateinit var clockTextView: TextView
    private val client = OkHttpClient()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var fetchButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var countdownTextView: TextView
    private var countdownTimer: CountDownTimer? = null

    private lateinit var cisloZakazkyField: EditText
    private lateinit var pracovatButton: Button
    private lateinit var vedlejsiPraceButton: Button
    private lateinit var prehledZakazekRecyclerView: RecyclerView
    private var poznamka: String? ="Vytvořeno aplikací AIN-dílna"
    private var tenDigitCode: String? = null
    private var eightDigitCode: String? = null

    private val fetchEmployeeRunnable = Runnable {
        Log.d("MainActivity", "fetchEmployeeRunnable se spustil")
        // Spouštění v Coroutine
        lifecycleScope.launch {
            fetchEmployeeOverviewSafe()
        }
        startFetchingEmployeeOverview()//zavolání pro opakované spuštění
    }

    private val fetchEmployeeInterval: Long = 5 * 60 * 1000 // 5 minut v mi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputField = findViewById(R.id.inputField)
        nameField = findViewById(R.id.nameField)
        surnameField = findViewById(R.id.surnameField)
        exp1Field = findViewById(R.id.exp1Field)
        exp2Field = findViewById(R.id.exp2Field)
        arrivalButton = findViewById(R.id.arrivalButton)
        obedButton = findViewById(R.id.btn_obed)
        lekarButton = findViewById(R.id.btn_lekar)
        skoleniButton = findViewById(R.id.btn_skoleni)
        prestavButton = findViewById(R.id.btn_prestavka)
        settingsButton = findViewById(R.id.btn_settings)
        typ_doch = findViewById(R.id.textView10)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, calculateNumberOfColumns())
        //recyclerView.layoutManager = GridLayoutManager(this, 2) // Nastavení 2 sloupců

        clockTextView = findViewById(R.id.clockTextView)
        countdownTextView = findViewById(R.id.countdownTextView)

        // Spuštění hodin
        startClock()

        //fetchEmployeeOverview()

        lifecycleScope.launch {
            fetchEmployeeOverviewSafe()
        }
        // Spuštění opakovaného volání
        startFetchingEmployeeOverview()

        arrivalButton.setOnClickListener {
            if (employeeNumber != null && systemInputRecordNumber != null) {
                podklad_doch= "4350223"
                sendPostRequest( systemInputRecordNumber!!,employeeNumber!!, podklad_doch!!, poznamka!!)
                inputField.text.clear()
            } else {
                Toast.makeText(this, "Chybí potřebné údaje!", Toast.LENGTH_SHORT).show()
            }
        }

        // Inicializace TextWatcher
        setupInputFieldWatcher()

        //employeeOverviewList = findViewById(R.id.employeeOverviewList)
        fetchButton = findViewById(R.id.fetchButton)

        fetchButton.setOnClickListener {
            fetchEmployeeOverview()
        }

        cisloZakazkyField = findViewById(R.id.cisloZakazkyField)
        pracovatButton = findViewById(R.id.pracovatButton)
        vedlejsiPraceButton = findViewById(R.id.vedlejsiPraceButton)
        prehledZakazekRecyclerView = findViewById(R.id.prehledZakazekRecyclerView)

        updateButtonStates()

        cisloZakazkyField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Nevyžaduje žádnou akci
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Nevyžaduje žádnou akci
            }

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()

                // Ověření, zda je vstup kompletní
                if (input.length >= 8) { // Příklad: očekávaná délka NFC tagu
                    if (employeeNumber != null && systemInputRecordNumber != null) {
                        val input = cisloZakazkyField.text.toString() // Z hodnoty cisloZakazkyField lze vytvořit dynamiku
                        sendPostRequest(systemInputRecordNumber!!, employeeNumber!!, input,poznamka!!)
                    }
                    // Vymazání pole
                    cisloZakazkyField.text.clear()
                }
            }
        })

        findViewById<Button>(R.id.submitButton).setOnClickListener {
            val input = inputField.text.toString()

            if (input.isNotEmpty()) {
                sendGetRequest(input)
            } else {
                Toast.makeText(this, "Zadejte číslo!", Toast.LENGTH_SHORT).show()
            }
        }

        // Aktivace tlačítka "Pracovat" při vyplnění pole
        cisloZakazkyField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                pracovatButton.isEnabled = !s.isNullOrEmpty()
            }
        })

        // Logika tlačítka "Pracovat"
        pracovatButton.setOnClickListener {
            if (employeeNumber != null && systemInputRecordNumber != null) {
                val input = cisloZakazkyField.text.toString() // Z hodnoty cisloZakazkyField lze vytvořit dynamiku
                sendPostRequest(systemInputRecordNumber!!, employeeNumber!!, input,
                    poznamka!!)
            } else {
                Toast.makeText(this, "Chybí potřebné údaje!", Toast.LENGTH_SHORT).show()
            }
        }//

        // Logika tlačítka "Vedlejší práce/úklid"
        vedlejsiPraceButton.setOnClickListener {
            if (employeeNumber != null && systemInputRecordNumber != null) {
                val podklad_doch = "21816396"
                sendPostRequest(systemInputRecordNumber!!, employeeNumber!!, podklad_doch,
                    poznamka.toString()
                )
            } else {
                Toast.makeText(this, "Chybí potřebné údaje!", Toast.LENGTH_SHORT).show()
            }
        }

        // Logika tlačítka "Oběd"
        obedButton.setOnClickListener {
            if (employeeNumber != null && systemInputRecordNumber != null) {
                val podklad_doch = "4350224"
                sendPostRequest(systemInputRecordNumber!!, employeeNumber!!, podklad_doch,
                    poznamka.toString()
                )
            } else {
                Toast.makeText(this, "Chybí potřebné údaje!", Toast.LENGTH_SHORT).show()
            }
        }

        // Logika tlačítka "Lékař"
        lekarButton.setOnClickListener {
            if (employeeNumber != null && systemInputRecordNumber != null) {
                val podklad_doch = "4490151"
                sendPostRequest(systemInputRecordNumber!!, employeeNumber!!, podklad_doch,
                    poznamka.toString()
                )
            } else {
                Toast.makeText(this, "Chybí potřebné údaje!", Toast.LENGTH_SHORT).show()
            }
        }

        // Logika tlačítka "Školení"
        skoleniButton.setOnClickListener {
            if (employeeNumber != null && systemInputRecordNumber != null) {
                val podklad_doch = "4486460"
                sendPostRequest(systemInputRecordNumber!!, employeeNumber!!, podklad_doch,
                    poznamka.toString()
                )
            } else {
                Toast.makeText(this, "Chybí potřebné údaje!", Toast.LENGTH_SHORT).show()
            }
        }

        // Logika tlačítka "Přestávka   "
        prestavButton.setOnClickListener {
            if (employeeNumber != null && systemInputRecordNumber != null) {
                val podklad_doch = "4350225"
                sendPostRequest(systemInputRecordNumber!!, employeeNumber!!, podklad_doch,
                    poznamka.toString()
                )
            } else {
                Toast.makeText(this, "Chybí potřebné údaje!", Toast.LENGTH_SHORT).show()
            }
        }
        // Logika tlačítka "Nastaveni"
        settingsButton.setOnClickListener {
            val intent=Intent(this,NastaveniActivity::class.java)
            startActivity(intent)
        }

        // Nastavení listeneru pro dotyk na celou obrazovku
        val rootLayout = findViewById<ConstraintLayout>(R.id.linearLayout)

        rootLayout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                resetCountdown()
                rootLayout.performClick() // ✅ Přidáno pro odstranění varování
            }
            true
        }

        rootLayout.setOnClickListener {
            // Prázdná implementace, aby nebyla chyba performClick()
        }

        prehledZakazekRecyclerView.layoutManager = GridLayoutManager(this, 2) // Jednosloupcové zobrazení
        //        recyclerView.layoutManager = GridLayoutManager(this, calculateNumberOfColumns())

        prehledZakazekRecyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if (e.action == MotionEvent.ACTION_DOWN) {
                    resetCountdown()
                }
                return false // Povolit další zpracování dotyku (scroll)
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, YourDeviceAdminReceiver::class.java)

// Povolení Lock Task Mode
        if (dpm.isDeviceOwnerApp(packageName)) {
            val packages = arrayOf(packageName)
            dpm.setLockTaskPackages(componentName, packages)

            // Spuštění režimu zamknutí
            startLockTask()
        } else {
            Toast.makeText(this, "App is not a device owner!", Toast.LENGTH_SHORT).show()
        }

        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_LOCKED) {
            Log.d("LockTask", "App is locked!")
        } else {
            Log.d("LockTask", "App is NOT locked!")
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Připojení menu_main.xml k hlavní aktivitě
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                // Spuštění aktivity NastaveniActivity
                val intent = Intent(this, NastaveniActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

/*---------------------------------------------------------- Přehled docházky ------------------------------------------------------------*/
    private fun sendGetRequest(input: String, retryCount: Int = 0) {
        val url =
            "http://ains-aps:5001/api/v1/Generic/browse/474?Id=12578&FilterId=4091&FilterParams=identifikace='$input'&Skip=0&Top=100"
        val request = username?.let {
            password?.let { it1 ->
                Credentials.basic(
                    it,
                    it1
                )
            }
        }?.let {
            Request.Builder()
                .url(url)
                .addHeader(
                    "Authorization", it
                )
                .build()
        }

        showProgressDialog("sendGetRequest", "Odesílání dat...", "Čekejte prosím")

        if (request != null) {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("GET_REQUEST", "Chyba připojení: ${e.message}", e)
                    hideProgressDialog("sendGetRequest")

                    if (retryCount < 3) {
                        Log.i("GET_REQUEST", "Retrying... ($retryCount/3)")
                        sendGetRequest(input, retryCount + 1)
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this@MainActivity,
                                "Chyba při odesílání GET požadavku po 3 pokusech!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val jsonResponse = response.body?.string()
                    hideProgressDialog("sendGetRequest")

                    if (response.isSuccessful && jsonResponse != null) {
                        val jsonObject = JSONObject(jsonResponse)
                        val dataArray = jsonObject.optJSONArray("data")

                        if (dataArray != null && dataArray.length() > 0) {
                            val data = dataArray.getJSONObject(0)

                            val name = data.optString("zamestnanci_jmeno", "")
                            val surname = data.optString("zamestnanci_prijmeni", "")
                            val exp1 = formatDate(data.optString("zamestnanci_Exp712847",null.toString()))
                            val exp2 = formatDate(data.optString("zamestnanci_Exp62249",null.toString()))
                            employeeNumber = data.optString("elektronicke_karty_Exp6", null.toString())
                            systemInputRecordNumber = data.optInt("elektronicke_karty_Exp843223", 0)
                            typDoch = data.optString("elektronicke_karty_Exp828580", null.toString())
                            val wholename = name +" "+ surname

                            // Lokální kopie employeeNumber
                            val localEmployeeNumber = employeeNumber

                            updateButtonStates()

                            runOnUiThread {
                                nameField.text = wholename//name
                                //surnameField.text = surname
                                exp1Field.text = exp1 ?: ""
                                exp2Field.text = exp2 ?: ""
                                typ_doch.text = typDoch ?: ""

                                fetchEmployeeOverview()
                                // Spuštění odpočtu
                                startCountdown(delay*1000)

                                // Přesun kurzoru do pole cisloZakazkyField
                                //cisloZakazkyField.requestFocus()
                                inputField.requestFocus()
                            }
                            if (localEmployeeNumber != null) {
                                sendPostRequestZakazky(localEmployeeNumber)
                            }
                            //clearFieldsAfterDelay(delay*1000)

                        } else {
                            runOnUiThread {
                                showAlertDialog("Zaměstanec nenalezen", "Zadaný zaměstanec nebyl nalezen.")
                                clearFieldsAfterDelay(0)
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Chybná odpověď ze serveru!", Toast.LENGTH_SHORT).show()
                        }
                        Log.e("GET_REQUEST", "Server odpověď kód: ${response.code}")
                    }
                }
            })
        }
    }

    /*---------------------------------------------------------- Zápis docházky ------------------------------------------------------------*/

    private fun sendPostRequest(systemInputRecordNumber: Int, employeeNumber: String, podklad: String, poznamka: String ) {
        val url =
            "http://ains-aps:5001/api/v1/Generic/Run/10144?FolderId=20946&ReturnLogDetail=Always"

        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        val branch = sharedPreferences.getString("branch", "PA") // Výchozí hodnota je "PA"

        val parametersArray = JSONArray().put(
            JSONObject()
                .put("uzivatel", employeeNumber.toInt())
                .put("podklad", podklad)
                .put("poznamka", "$poznamka, pobočka: $branch"))

        val requestBody = JSONObject()
            .put("parameters", parametersArray)
            .toString()
            .toRequestBody("application/json".toMediaType())

        Log.d("POST_REQUEST", "URL: $url")
        Log.d("POST_REQUEST", "Body: $requestBody")

        showProgressDialog("sendPostRequest", "Odesílání dat...", "Čekejte prosím")

        val request = username?.let {
            password?.let { it1 ->
                Credentials.basic(
                    it,
                    it1
                )
            }
        }?.let {
            Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader(
                    "Authorization", it
                )
                .build()
        }

        if (request != null) {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("POST_REQUEST", "Chyba připojení: ${e.message}", e)
                    hideProgressDialog("sendPostRequest")
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Chyba při odesílání POST požadavku!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    hideProgressDialog("sendPostRequest")
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@MainActivity,
                                "Příchod/Odchod byl úspěšně zaregistrován!",
                                Toast.LENGTH_SHORT
                            ).show()
                            fetchEmployeeOverview()
                            // Přesun kurzoru zpět do pole inputField
                            inputField.requestFocus()

                        } else {
                            Toast.makeText(this@MainActivity, "Chybná odpověď POST požadavku!", Toast.LENGTH_SHORT).show()
                            Log.e("POST_REQUEST", "Chybná odpověď POST požadavku: "+response.toString())
                        }
                    }
                    clearFieldsAfterDelay(0)
                }
            })
        }
    }

    /*---------------------------------------------------------- Přehled zakázek ------------------------------------------------------------*/

    private fun sendPostRequestZakazky(employeeNumber: String) {
        val url = "http://ains-aps:5001/api/v1/Generic/browse/20476?Id=12584&FilterId=4603&FilterParams=zamestnanec=$employeeNumber&Skip=0&Top=100"

        val request = Request.Builder()
            .url(url)
            .addHeader(
                "Authorization", Credentials.basic(username!!, password!!)
            )
            .build()

        showProgressDialog("sendPostRequestZakazky", "Načítání zakázek...", "Čekejte prosím")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GET_ZAKAZKY", "Chyba připojení: ${e.message}", e)
                hideProgressDialog("sendPostRequestZakazky")
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Chyba při načítání zakázek!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                hideProgressDialog("sendPostRequestZakazky")
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val jsonResponse = JSONObject(responseBody.string())
                        val dataArray = jsonResponse.optJSONArray("data") ?: JSONArray()
                        val zakazky = mutableListOf<Zakazka>()

                        for (i in 0 until dataArray.length()) {
                            val zakazka = dataArray.getJSONObject(i)
                            zakazky.add(
                                Zakazka(
                                    formatDate(zakazka.optString("ino_planovani_datum_od", "")) ?: "",
                                    formatDate(zakazka.optString("ino_planovani_datum_do", "")) ?: "",
                                    zakazka.optString("subjekty_reference_subjektu", ""),
                                    zakazka.optString("subjekty_nazev_subjektu", "")
                                )
                            )
                        }

                        runOnUiThread {
                            val adapter = ZakazkyAdapter(zakazky)
                            prehledZakazekRecyclerView.adapter = adapter
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Chybná odpověď serveru!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    /*---------------------------------------------------------- Přehled zaměstnanců ------------------------------------------------------------*/
    private fun fetchEmployeeOverview() {
        showProgressDialog("fetchEmployeeOverview", "Načítání přehledu zaměstnanců...", "Čekejte prosím")

        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        val branch = sharedPreferences.getString("branch", "PA") // Výchozí hodnota je "PA"
        val url = "http://ains-aps:5001/api/v1/Generic/browse/20476?Id=12600&FilterId=4596&FilterParams=mistnost='$branch'&Skip=0&Top=100"

        // Logování obsahu GET požadavku
        Log.d("GET_REQUEST-fetchEmployeeOverview", "URL: $url")

        val request = username?.let { password?.let { it1 -> Credentials.basic(it, it1) } }?.let {
            Request.Builder()
                .url(url)
                .addHeader(
                    "Authorization",
                    it
                )
                .build()
        }

        if (request != null) {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("FETCH_EMPLOYEES", "Chyba připojení: ${e.message}", e)
                    hideProgressDialog("fetchEmployeeOverview")
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Chyba při načítání přehledu zaměstnanců!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    hideProgressDialog("fetchEmployeeOverview")
                    if (response.isSuccessful) {
                        response.body?.let { responseBody ->
                            val jsonResponse = JSONObject(responseBody.string())

                            Log.d("GET_REQUEST", "Response: $jsonResponse") // Logování odpovědi
                            val dataArray = jsonResponse.optJSONArray("data") ?: JSONArray()
                            val employees = mutableListOf<Employee>()
/*                            for (i in 0 until dataArray.length()) {
                                val employee = dataArray.getJSONObject(i)
                                val startDateString = employee.optString("ino_planovani_datum_od", null)
                                val endDateString = employee.optString("ino_planovani_datum_do", null)
                                employees.add(
                                    Employee(
                                        (employee.optString("subjekty_nazev_subjektu", "")).toString(),
                                        (employee.optString("subjekty_nazev_subjektu_0001", "")).toString(),//dochazka
                                        ((formatDate(employee.optString("ino_planovani_datum_od", null)) ?: "")).toString(),
                                        ((formatDate(employee.optString("ino_planovani_datum_do", null)) ?: "")).toString(),
                                        (employee.optString("subjekty_reference_subjektu", "")).toString(),//pobočka,
                                         (employee.optString("subjekty_reference_subjektu_0001", "")).toString()//cislo zakazky
                                    )
                            }*/
                            for (i in 0 until dataArray.length()) {
                                val employeeData = dataArray.getJSONObject(i)
                                Log.d("fetchEmployeeOverview", "Raw employee data: ${employeeData.toString(2)}") // Pretty-print JSON
                                val startDateString = employeeData.optString("ino_planovani_datum_od", null)
                                val endDateString = employeeData.optString("ino_planovani_datum_do", null)
                                Log.d("fetchEmployeeOverview", "startDateString: $startDateString, endDateString: $endDateString")
                                val employee = Employee(
                                    (employeeData.optString("subjekty_nazev_subjektu", "")).toString(),
                                    (employeeData.optString("subjekty_nazev_subjektu_0001", "")).toString(),
                                    ((formatDate(startDateString) ?: "")).toString(),
                                    ((formatDate(endDateString) ?: "")).toString(),
                                    (employeeData.optString("subjekty_reference_subjektu", "")).toString(),
                                    (employeeData.optString("subjekty_reference_subjektu_0001", "")).toString()
                                )
                                employee.duration = calculateDuration(startDateString, endDateString) // Výpočet a uložení trvání
                                employees.add(employee)
                            }

                            runOnUiThread {
                                val adapter = EmployeeAdapter(employees) // Používáme adaptér, který je v samostatném souboru
                                recyclerView.adapter = adapter
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                this@MainActivity,
                                "Chybná odpověď ze serveru: ${response.code}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
        }
    }

    /*----------------------------------------------------------  Konec ------------------------------------------------------------*/

    private fun calculateDuration(startDateString: String?, endDateString: String?): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return try {
            if (startDateString == null) {
                return ""
            }

            val startDate = tryParseDate(startDateString, dateFormat) ?: return "" // Použijeme pomocnou funkci
            val endDate = endDateString?.let { tryParseDate(it, dateFormat) } ?: Date()

            val durationMillis = endDate.time - startDate.time
            val hours = durationMillis / (1000 * 60 * 60)
            val minutes = (durationMillis % (1000 * 60 * 60)) / (1000 * 60)
            val durationString = String.format("%02d:%02d", hours, minutes)
            Log.d("calculateDuration", "Duration for $startDateString - $endDateString: $durationString")
            durationString
        } catch (e: Exception) {
            Log.e("calculateDuration", "Chyba při výpočtu trvání: ${e.message}", e)
            ""
        }
    }

    // Pomocná funkce pro bezpečné parsování data
    private fun tryParseDate(dateString: String, dateFormat: SimpleDateFormat): Date? {
        return try {
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            Log.w("tryParseDate", "Invalid date format: $dateString")
            null
        }
    }

    private fun formatDate(input: String?): String? {
        if (input == null) return null
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            //val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(input)
            outputFormat.format(date)
        } catch (e: Exception) {
            null
        }
    }

    private var progressDialogMap = mutableMapOf<String, ProgressDialog>()

    private fun showProgressDialog(key: String, title: String, message: String) {
        runOnUiThread {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle(title)
            progressDialog.setMessage(message)
            progressDialog.setCancelable(false)
            progressDialog.show()

            // Uložení dialogu do mapy podle klíče
            progressDialogMap[key] = progressDialog
        }
    }

    private fun hideProgressDialog(key: String) {
        runOnUiThread {
            progressDialogMap[key]?.dismiss()
            progressDialogMap.remove(key)
        }
    }


    private fun showAlertDialog(title: String, message: String) {
        runOnUiThread {
            val dialog = android.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .create()
            dialog.show()
        }
    }

    private fun clearFieldsAfterDelay(delay: Long) {
        handler.postDelayed({
            nameField.text = ""
            surnameField.text = ""
            exp1Field.text = ""
            exp2Field.text = ""
            employeeNumber = null
            countdownTimer?.cancel() // Zrušení odpočtu při ukončení aktivity
            countdownTextView.text = ""
            podklad_doch = null
            typ_doch.text = ""

            // Vymazání dat z RecyclerView pro zakázky
            (prehledZakazekRecyclerView.adapter as? ZakazkyAdapter)?.clearData()

            // Vymazání vstupního pole pro číslo zakázky
            inputField.text.clear()

            // Vymazání vstupního pole pro číslo zakázky
            cisloZakazkyField.text.clear()

            // Přesun kurzoru zpět do pole inputField
            inputField.requestFocus()
            //showKeyboard(inputField)
            tenDigitCode=null
            eightDigitCode=null
            updateButtonStates()
        }, delay)
    }

    private fun calculateNumberOfColumns(): Int {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val columnWidth = resources.getDimensionPixelSize(R.dimen.column_width) // Nastavit požadovanou šířku sloupce

        // Počet sloupců bude závislý na šířce displeje a požadované šířce sloupce
        return (screenWidth / columnWidth).toInt().coerceAtLeast(1) // Ensure at least 1 column
    }
    private fun startClock() {
        // Formát času
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        // Aktualizace každou sekundu
        val runnable = object : Runnable {
            override fun run() {
                // Získání aktuálního času
                val currentTime = timeFormat.format(Date())
                // Aktualizace TextView
                clockTextView.text = currentTime
                // Opětovné spuštění za 1 sekundu
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }
    override fun onDestroy() {
        super.onDestroy()
        // Zastavení handleru při ukončení aktivity
        handler.removeCallbacksAndMessages(null)
        countdownTimer?.cancel() // Zrušení odpočtu při ukončení aktivity
        stopFetchingEmployeeOverview()
    }

    private fun startCountdown(delay: Long) {

        countdownTimer?.cancel() // Zrušení případného předchozího odpočtu

        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)

        if (sharedPreferences.contains("time")) {
            try {
                val oldTime = sharedPreferences.getInt("time", 10) // Načíst jako Int, pokud existuje
                sharedPreferences.edit()
                    .remove("time") // Smazat starou hodnotu
                    .putString("time", oldTime.toString()) // Uložit jako String
                    .apply()
            } catch (e: ClassCastException) {
                // Pokud není Int, ignorovat chybu
            }
        }

        // Načtení hodnoty delay ze SharedPreferences jako String
        val savedTimeString = sharedPreferences.getString("time", "10") // Načítáme jako String
        this@MainActivity.delay = savedTimeString?.toIntOrNull()?.toLong()
            ?: (10L)// Bezpečný převod na Long s výchozí hodnotou 10

        countdownTimer = object : CountDownTimer(delay, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                //countdownTextView.text = "Označ příchod/odchod do: ${secondsRemaining} s"
                countdownTextView.text = "Pokračuj do: ${secondsRemaining}"
            }

            override fun onFinish() {
                //countdownTextView.text = ""
                clearFieldsAfterDelay(0)
            }
        }.start()
    }

    private fun showKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // Proměnné pro uložení 10místného a 8místného kódu

    private var inputHandler = Handler(Looper.getMainLooper()) // Handler pro časovač
    private var inputRunnable: Runnable? = null // Uchování instance Runnable pro opakované volání

    private fun setupInputFieldWatcher() {
        inputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()

                // Zrušení předchozího spuštěného časovače
                inputRunnable?.let { inputHandler.removeCallbacks(it) }

                // Nastavení nového časovače na 500 ms
                inputRunnable = Runnable {
                    processInput(input)
                }
                inputHandler.postDelayed(inputRunnable!!, 500) // Zpoždění 500 ms
            }
        })
    }

    private fun processInput(input: String) {
        when {
            // 10místný kód
            input.length == 10 && input.all { it.isDigit() } -> {
                tenDigitCode = input
                sendGetRequest(input)
                inputField.text.clear()
            }
            // 8místný kód (po 10místném)
            input.length >= 8 /*&& input.all { it.isDigit() } */-> {
                if (tenDigitCode != null) {
                    eightDigitCode = input
                    if (systemInputRecordNumber != null && employeeNumber != null) {
                        sendPostRequest(
                            systemInputRecordNumber!!,
                            employeeNumber!!,
                            input,
                            poznamka!!
                        )
                        Log.d("POST_REQUEST", "Odesílám POST s: systemInputRecordNumber=$systemInputRecordNumber, employeeNumber=$employeeNumber, podklad=$input, poznamka=$poznamka")

                        inputField.text.clear()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Chybí potřebné údaje pro POST požadavek! xxxx",
                            Toast.LENGTH_SHORT
                        ).show()
                        clearFieldsAfterDelay(0)
                    }
                } else {
                    showAlertDialog("Pozor","Nejprve musí být zadán zaměstnanec a potom zakázka")

                    //inputField.text.clear()
                    clearFieldsAfterDelay(0)
                }
            }
            // Chybná délka
            input.length > 8 && input.length != 10 -> {
                Toast.makeText(
                    this@MainActivity,
                    "Neplatná délka vstupu!",
                    Toast.LENGTH_SHORT
                ).show()

                showAlertDialog("Pozor", "Nejedná se o zaměstnance ani zakázku.")

                //inputField.text.clear()
                clearFieldsAfterDelay(0)
            }
        }
    }
    private fun updateButtonStates() {
        runOnUiThread {
            vedlejsiPraceButton.isEnabled = employeeNumber != null
            arrivalButton.isEnabled = employeeNumber != null
            obedButton.isEnabled = employeeNumber != null
            lekarButton.isEnabled = employeeNumber != null
            skoleniButton.isEnabled = employeeNumber != null
            prestavButton.isEnabled = employeeNumber != null
        }
    }
    // Funkce pro bezpečné volání fetchEmployeeOverview
    private suspend fun fetchEmployeeOverviewSafe() {
        Log.d("MainActivity", "fetchEmployeeOverviewSafe() se spustil")
        withContext(Dispatchers.IO) {
            try {
                Log.d("MainActivity", "fetchEmployeeOverview() se volá")
                fetchEmployeeOverview() // Volání síťové operace
                Log.d("MainActivity", "fetchEmployeeOverview() úspěšně dokončeno")
            } catch (e: Exception) {
                // Obsluha chyb na vedlejším vlákně
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Chyba při načítání dat", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // Funkce pro opakované spouštění
    private fun startFetchingEmployeeOverview() {
        //handler.removeCallbacks(fetchEmployeeRunnable)
        handler.postDelayed(fetchEmployeeRunnable, fetchEmployeeInterval)
    }

    private fun stopFetchingEmployeeOverview() {
        handler.removeCallbacks(fetchEmployeeRunnable)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            resetCountdown()
        }
        return super.onTouchEvent(event)
    }

    override fun onResume() {
        super.onResume()

//        // KONTROLA LOCK TASK MODE (doplněný kód)
//        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        if (activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
//            Toast.makeText(this, "Not in LockTaskMode, exiting", Toast.LENGTH_SHORT).show()
//            finish()
//            return // Ukončení metody, pokud nejsme v Lock Task Mode
//        }
        startCountdown(delay * 1000) // Restart odpočtu při návratu do aplikace
    }


    private fun resetCountdown() {
        startCountdown(delay * 1000) // Restartuje odpočet
    }

    override fun onStart() {
        super.onStart()

        // Zkontroluj, jestli aplikace není už uzamčená
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        if (activityManager.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_NONE) {
            startLockTask() // Uzamkne aplikaci
        }
    }

}

