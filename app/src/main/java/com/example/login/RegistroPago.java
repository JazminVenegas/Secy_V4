package com.example.login;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
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

public class RegistroPago extends AppCompatActivity {
    private EditText vecino, domicilio, fechapago;
    private ListView listapago;
    private Spinner medioPago;
    String[]mdpago = {"Efectivo","Debito","Credito"};
    private FirebaseFirestore dbpago;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registropago);

        // Inicializar Firestore
        dbpago = FirebaseFirestore.getInstance();

        // Inicializar los componentes de la interfaz
        vecino = findViewById(R.id.vecino);
        domicilio = findViewById(R.id.domicilio);
        fechapago = findViewById(R.id.fechapago);
        listapago = findViewById(R.id.listapago);
        medioPago = findViewById(R.id.mediopago);

        // Configurar el Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mdpago);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        medioPago.setAdapter(adapter);

        // Cargar la lista desde Firestore
        CargaListaFirestorePagos();
    }


    public void enviarDatosFirestorePagos(View view) {
        String vecin = vecino.getText().toString();
        String domi = domicilio.getText().toString();
        String fechapag = fechapago.getText().toString();
        String mdpago = medioPago.getSelectedItem().toString();

        // Validar que no estén vacíos
        if (vecin.isEmpty() || domi.isEmpty() || fechapag.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return; // Detener el flujo si los datos están incompletos
        }

        // Crear un objeto Map con los datos
        Map<String, Object> alerta = new HashMap<>();
        alerta.put("vecino", vecin);
        alerta.put("domicilio", domi);
        alerta.put("fechapago", fechapag);
        alerta.put("mdpago",mdpago);

        // Guardar los datos en Firestore
        dbpago.collection("ingresopagos")
                .document(vecin) // Nombre del documento basado en el campo "vecino"
                .set(alerta) // Enviar el Map completo
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegistroPago.this, "Datos enviados correctamente", Toast.LENGTH_SHORT).show();
                    // Recargar la lista después de enviar los datos
                    CargaListaFirestorePagos();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegistroPago.this, "Error al enviar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void CargarListaPagos(View view) {
        CargaListaFirestorePagos();
    }

    public void CargaListaFirestorePagos() {
        dbpago.collection("ingresopagos")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> listaAlertas = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String linea = "|| " + document.getString("vecino") + " || " +
                                        document.getString("domicilio") + " || " +
                                        document.getString("fechapago");
                                listaAlertas.add(linea);
                            }

                            // Configurar el adaptador solo si hay datos
                            ArrayAdapter<String> adaptador = new ArrayAdapter<>(
                                    RegistroPago.this,
                                    R.layout.list_item, // Archivo de diseño personalizado
                                    listaAlertas
                            );
                            listapago.setAdapter(adaptador); // Establecer adaptador en el ListView
                        } else {
                            Log.e("CargaListaFirestorePagos", "Error al obtener datos del Firestore", task.getException());
                        }
                    }
                });
    }
}

