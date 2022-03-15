package com.jonathanev.repasar;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.jonathanev.repasar.Fragments.Fragment_DialogListarGuias_popup;
import com.jonathanev.repasar.Fragments.Fragment_DialogNuevoArchivo_popu;
import com.jonathanev.repasar.databinding.ActivityMainBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // Array TEXTO donde guardaremos los nombres de los ficheros.
    ArrayList<String> item = new ArrayList<String>();

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Utilizamos un botón que es reutilizado, unicamente le cambiamos el texto.
        binding.btnAbrirGuiaEstudioHabilitado.setText("Abrir Guia");

        binding.btnNuevaGuiaEstudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Unicamente abrimos el dialogo y lo mostramos en la pantalla.
                Fragment_DialogNuevoArchivo_popu dialogo = new Fragment_DialogNuevoArchivo_popu();
                dialogo.show(getSupportFragmentManager(), "Fragment");
            }
        });

        binding.btnAbrirGuiaEstudioHabilitado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Defino la ruta donde busco los ficheros.
                @SuppressLint("SdCardPath") File file = new File("/data/data/com.jonathanev.repasar/files/");

                // Limpio el item por si se borra algun archivo no se quede guardado.
                item.clear();
                if (file.exists()){
                    // Creo el array de tipo File con el contenido de la carpeta.
                    File[] files = file.listFiles();

                    // Hacemos un ciclo por cada fichero para extraer el nombre de cada uno.
                    for (int i = 0; i < files.length; i++){
                        // Sacamos del array files el primer fichero.
                        File archivo = files[i];

                        // Guardamos el nombre del fichero en la lista item.
                        item.add(archivo.getName().replaceAll(".xml", ""));
                    }

                    // Teniendo todos los nombre de los archivos abrimos el dialogo.
                    Fragment_DialogListarGuias_popup dialogo = new Fragment_DialogListarGuias_popup();
                    dialogo.show(getSupportFragmentManager(), "Fragment");

                    // Creamos las preferencias y dentro de ellas guardamos el arreglo item
                    SharedPreferences preferencias = getSharedPreferences("nombres_guias", MODE_PRIVATE);
                    SharedPreferences.Editor editor;
                    editor = preferencias.edit();
                    Set<String> set = new HashSet<String>();
                    set.addAll(item);
                    editor.putStringSet("guias_estudio", set);
                    editor.commit();
                } else { // Si la carpeta no existe significa que aún no crea ninguna guia
                    Toast.makeText(getApplicationContext(),
                            "No tienes todos los archivos requeridos, crea una nueva guia para " +
                                    "comenzar a crear los paquetes.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}