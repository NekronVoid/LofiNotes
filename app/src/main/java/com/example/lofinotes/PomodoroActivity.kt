package com.example.lofinotes

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class PomodoroActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var btnStartPause: Button
    private lateinit var btnReset: Button
    private lateinit var etStudyTime: EditText
    private lateinit var etBreakTime: EditText
    private lateinit var spnMusic: Spinner
    private lateinit var bottomNav: BottomNavigationView

    private var countDownTimer: CountDownTimer? = null
    private var tiempoRestanteMilli: Long = 25 * 60 * 1000
    private var timerCorriendo: Boolean = false
    private var esTiempoDeEstudio: Boolean = true

    // Reproductor de música ambiental
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pomodoro)

        tvTimer = findViewById(R.id.tvTimer)
        btnStartPause = findViewById(R.id.btnStartPause)
        btnReset = findViewById(R.id.btnReset)
        etStudyTime = findViewById(R.id.etStudyTime)
        etBreakTime = findViewById(R.id.etBreakTime)
        spnMusic = findViewById(R.id.spnMusic)
        bottomNav = findViewById(R.id.bottomNavigation)

        actualizarTextoTimer()

        btnStartPause.setOnClickListener {
            if (timerCorriendo) {
                pausarTimer()
            } else {
                iniciarTimer()
            }
        }

        btnReset.setOnClickListener {
            reiniciarTimer()
        }

        configurarMusica()
        configurarNavegacion()
    }

    private fun iniciarTimer() {
        // Si el timer no estaba corriendo y estaba en el inicio, tomamos los valores de los EditText
        if (!timerCorriendo && (tiempoRestanteMilli == 25L * 60 * 1000 || tiempoRestanteMilli == 5L * 60 * 1000)) {
            val minutos = if (esTiempoDeEstudio) {
                etStudyTime.text.toString().toLongOrNull() ?: 25
            } else {
                etBreakTime.text.toString().toLongOrNull() ?: 5
            }
            tiempoRestanteMilli = minutos * 60 * 1000
        }

        countDownTimer = object : CountDownTimer(tiempoRestanteMilli, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tiempoRestanteMilli = millisUntilFinished
                actualizarTextoTimer()
            }

            override fun onFinish() {
                timerCorriendo = false
                esTiempoDeEstudio = !esTiempoDeEstudio
                
                val mensaje = if (esTiempoDeEstudio) "¡A estudiar!" else "¡Tiempo de descanso!"
                Toast.makeText(this@PomodoroActivity, mensaje, Toast.LENGTH_SHORT).show()
                
                btnStartPause.text = "[ EMPEZAR ]"
                
                // Cargar el siguiente tiempo automáticamente
                val proximoMinuto = if (esTiempoDeEstudio) {
                    etStudyTime.text.toString().toLongOrNull() ?: 25
                } else {
                    etBreakTime.text.toString().toLongOrNull() ?: 5
                }
                tiempoRestanteMilli = proximoMinuto * 60 * 1000
                actualizarTextoTimer()
            }
        }.start()

        timerCorriendo = true
        btnStartPause.text = "[ PAUSAR ]"
    }

    private fun pausarTimer() {
        countDownTimer?.cancel()
        timerCorriendo = false
        btnStartPause.text = "[ REANUDAR ]"
    }

    private fun reiniciarTimer() {
        countDownTimer?.cancel()
        timerCorriendo = false
        val minutos = etStudyTime.text.toString().toLongOrNull() ?: 25
        tiempoRestanteMilli = minutos * 60 * 1000
        esTiempoDeEstudio = true
        actualizarTextoTimer()
        btnStartPause.text = "[ EMPEZAR ]"
    }

    private fun actualizarTextoTimer() {
        val minutos = (tiempoRestanteMilli / 1000) / 60
        val segundos = (tiempoRestanteMilli / 1000) % 60
        val formatoTiempo = String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos)
        tvTimer.text = formatoTiempo
    }

    private fun configurarMusica() {
        spnMusic.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                detenerMusica()
                val opcion = parent?.getItemAtPosition(position).toString()
                
                if (opcion != "Silencio") {
                    reproducirMusicaAmbiental(opcion)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun reproducirMusicaAmbiental(tipo: String) {
        val resId = when (tipo) {
            "Lluvia" -> resources.getIdentifier("lluvia", "raw", packageName)
            "Bosque" -> resources.getIdentifier("bosque", "raw", packageName)
            "Cafetería" -> resources.getIdentifier("cafeteria", "raw", packageName)
            "Olas del Mar" -> resources.getIdentifier("olas", "raw", packageName)
            else -> 0
        }

        if (resId != 0) {
            try {
                mediaPlayer = MediaPlayer.create(this, resId)
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
            } catch (e: Exception) {
                Toast.makeText(this, "Asegúrate de añadir el archivo $tipo en res/raw", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Archivo de sonido no encontrado en res/raw", Toast.LENGTH_SHORT).show()
        }
    }

    private fun detenerMusica() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun configurarNavegacion() {
        bottomNav.selectedItemId = R.id.nav_pomodoro
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_notes -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_pomodoro -> true
                else -> false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detenerMusica()
    }
}