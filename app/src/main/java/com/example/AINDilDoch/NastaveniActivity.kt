package com.example.AINDilDoch

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.AINDilDoch.R

class NastaveniActivity : AppCompatActivity() {

    val sharedPreferences by lazy { getSharedPreferences("MyAppPreferences", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nastaveni)

        val branchInput: EditText = findViewById(R.id.branchInput)
        val timeInput: EditText = findViewById(R.id.timeInput)
        val saveButton: Button = findViewById(R.id.saveButton)

        val buttonExit: Button = findViewById(R.id.button_exit_password)

        buttonExit.setOnClickListener {
            showExitPasswordDialog()
        }

        // Načtení uložených hodnot
        val savedTime = sharedPreferences.getString("time", "15") // Načítáme jako String
        timeInput.setText(savedTime)

        val savedBranch = sharedPreferences.getString("branch", "PA")
        branchInput.setText(savedBranch)

        // Uložení hodnot při stisknutí tlačítka
        saveButton.setOnClickListener {
            val branch = branchInput.text.toString()
            sharedPreferences.edit().putString("branch", branch).apply()

            val time = timeInput.text.toString().toIntOrNull()
            if (time == null || time <= 0) {
                timeInput.error = "Zadejte platné číslo větší než 0!"
                return@setOnClickListener
            }
            sharedPreferences.edit().putString("time", time.toString()).apply() // Uložení jako String

            // Ukončení aktivity
            finish()
        }
    }
    private fun showExitPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Zadejte heslo pro ukončení")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val password = input.text.toString()
            if (password == "tajneheslo") {  // Nastav si vlastní heslo!
                stopLockTask()  // Ukončení Lock Task Mode
                Toast.makeText(this, "Režim ukončen", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Nesprávné heslo", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Zrušit") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}


