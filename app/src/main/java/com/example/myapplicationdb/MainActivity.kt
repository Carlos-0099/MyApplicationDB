package com.example.myapplicationdb

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : ComponentActivity() {

    private lateinit var editTextNombre: EditText
    private lateinit var btnAgregar: Button
    private lateinit var txtResultado: TextView

    private lateinit var db: FirebaseFirestore
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        editTextNombre = findViewById(R.id.editTextNombre)
        btnAgregar = findViewById(R.id.btnAgregar)
        txtResultado = findViewById(R.id.txtResultado)

        db = FirebaseFirestore.getInstance()

        // Inicial: mensaje de conexión
        txtResultado.text = "Esperando conexión..."

        // Acción del botón para agregar usuario
        btnAgregar.setOnClickListener {
            val nombre = editTextNombre.text.toString().trim()
            if (nombre.isEmpty()) {
                Toast.makeText(this, "Debe ingresar un nombre", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = hashMapOf(
                "nombre" to nombre,
                "timeStamp" to System.currentTimeMillis()
            )

            db.collection("usuarios")
                .add(data)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(
                        this,
                        "Registro agregado con ID ${documentReference.id}",
                        Toast.LENGTH_SHORT
                    ).show()
                    editTextNombre.text.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Escuchar cambios en tiempo real
        listener = db.collection("usuarios")
            .orderBy("timeStamp")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    txtResultado.text = "Error al cargar datos: ${e.message}"
                    return@addSnapshotListener
                }

                txtResultado.text = "Conectado\n\n"

                if (snapshots != null && !snapshots.isEmpty) {
                    val sb = StringBuilder()
                    sb.append("Conectado\n\n")
                    for (doc in snapshots.documents) {
                        val nombre = doc.getString("nombre") ?: "Sin nombre"
                        sb.append("• $nombre\n")
                    }
                    txtResultado.text = sb.toString()
                } else {
                    txtResultado.text = "Conectado\n\nNo hay usuarios registrados."
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove() // Detener la escucha al cerrar la app
    }
}
