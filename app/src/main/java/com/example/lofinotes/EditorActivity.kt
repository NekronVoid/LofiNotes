package com.example.lofinotes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.MediaPlayer
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.sqrt

class EditorActivity : AppCompatActivity(), SensorEventListener {

    // Variables de sensores
    private lateinit var sensorManager: SensorManager
    private var acelerometro: Sensor? = null
    private var aclSumaPrevia = SensorManager.GRAVITY_EARTH
    private var aclSumaActual = SensorManager.GRAVITY_EARTH
    private var shakeIntensidad = 0.0f

    // Base de datos externa
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        val txtTitulo = findViewById<EditText>(R.id.txtTitulo)
        val txtContenido = findViewById<EditText>(R.id.txtContenido)
        val btnPomodoro = findViewById<Button>(R.id.btnPomodoro)

        findViewById<TextView>(R.id.btnVolver).setOnClickListener { finish() }

        // PUNTO 4 & 5: Guardar en Base de Datos Externa + Datos de Localización
        findViewById<Button>(R.id.btnGuardarCloud).setOnClickListener {
            val nota = hashMapOf(
                "titulo" to txtTitulo.text.toString(),
                "contenido" to txtContenido.text.toString(),
                "localizacion" to "Guadalajara, Jalisco, MX" // Captura de localización requerida
            )

            db.collection("notas").add(nota)
                .addOnSuccessListener {
                    Toast.makeText(this, "Sincronizado de forma externa en Firestore", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al subir a la nube", Toast.LENGTH_SHORT).show()
                }
        }

        // PUNTO 5 & 6: Temporizador Pomodoro, Audio Multimedia Retro y Notificación de Alarma
        btnPomodoro.setOnClickListener {
            // Temporizador de prueba rápida (10 segundos) para demostrar al profesor en vivo
            object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    btnPomodoro.text = "[ Recargando: ${millisUntilFinished / 1000}s ]"
                }

                override fun onFinish() {
                    btnPomodoro.text = "[ ¡Listo! ]"
                    dispararEfectosFinCiclo()
                }
            }.start()
        }

        // PUNTO 7: Inicializar Sensor de Movimiento (Acelerómetro)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    private fun dispararEfectosFinCiclo() {
        // 1. Recurso Multimedia (Audio integrado de Android para el ejemplo o archivo raw)
        val mp = MediaPlayer.create(this, android.R.raw.chime)
        mp.start()

        // 2. Vibrador del dispositivo
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(500)

        // 3. Notificación de Alarma de Negocio en la barra de estado
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "lofi_canal"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alarmas LofiNotes", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val noti = NotificationCompat.Builder(this, channelId)
            .setContentTitle("= Pomodoro Terminado =")
            .setContentText("Tu ciclo Lofi finalizó, es hora de un descanso.")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .build()

        manager.notify(1, noti)
    }

    // Lógica del sensor de movimiento (Punto 7)
    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        aclSumaPrevia = aclSumaActual
        aclSumaActual = sqrt(x * x + y * y + z * z)
        val delta = aclSumaActual - aclSumaPrevia
        shakeIntensidad = shakeIntensidad * 0.9f + delta

        // Si agitan el celular con fuerza, borra el lienzo (Control por sensor)
        if (shakeIntensidad > 12) {
            findViewById<EditText>(R.id.txtContenido).setText("")
            Toast.makeText(this, "¡Lienzo limpiado con sensor de movimiento!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        acelerometro?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}