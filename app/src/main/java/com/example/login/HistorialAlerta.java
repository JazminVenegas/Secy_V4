package com.example.login;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistorialAlerta extends AppCompatActivity {
    private EditText vecino, tipAlerta, fechaAlerta;
    private ListView lista; // Aquí estará la lista
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historialalerta);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar los componentes de la interfaz
        vecino = findViewById(R.id.vecino);
        tipAlerta = findViewById(R.id.tipAlerta);
        fechaAlerta = findViewById(R.id.fechaAlerta);
        lista = findViewById(R.id.lista); // Inicializar el ListView

        // Cargar la lista desde Firestore
        CargaListaFirestore();
    }

    public void enviarDatosFirestore(View view) {
        String vecin = vecino.getText().toString();
        String tipAlert = tipAlerta.getText().toString();
        String fechaAlert = fechaAlerta.getText().toString();

        // Validar que no estén vacíos
        if (vecin.isEmpty() || tipAlert.isEmpty() || fechaAlert.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return; // Detener el flujo si los datos están incompletos
        }

        // Crear un objeto Map con los datos
        Map<String, Object> alerta = new HashMap<>();
        alerta.put("vecino", vecin);
        alerta.put("tipoAlerta", tipAlert);
        alerta.put("fechaAlerta", fechaAlert);

        // Guardar los datos en Firestore
        db.collection("seguridad")
                .document(vecin) // Nombre del documento basado en el campo "vecino"
                .set(alerta) // Enviar el Map completo
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(HistorialAlerta.this, "Datos enviados correctamente", Toast.LENGTH_SHORT).show();
                    // Recargar la lista después de enviar los datos
                    CargaListaFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HistorialAlerta.this, "Error al enviar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void CargarLista(View view) {
        CargaListaFirestore();
    }

    public void CargaListaFirestore() {
        db.collection("seguridad")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> listaAlertas = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String linea = "|| " + document.getString("vecino") + " || " +
                                        document.getString("tipoAlerta") + " || " +
                                        document.getString("fechaAlerta");
                                listaAlertas.add(linea);
                            }

                            // Configurar el adaptador solo si hay datos
                            ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                                    HistorialAlerta.this,
                                    R.layout.list_item, // Archivo de diseño personalizado
                                    listaAlertas
                            );
                            lista.setAdapter(adaptador); // Establecer adaptador en el ListView
                        } else {
                            Log.e("CargaListaFirestore", "Error al obtener datos del Firestore", task.getException());
                        }
                    }
                });
    }
}
