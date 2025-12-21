package com.spectrum.phoenix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.database.FirebaseDatabase
import com.spectrum.phoenix.logic.migration.MigrationManager
import com.spectrum.phoenix.ui.navigation.AppNavigation
import com.spectrum.phoenix.ui.theme.PhoenixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Habilitar persistencia de datos offline de Firebase
        try {
            val database = FirebaseDatabase.getInstance()
            database.setPersistenceEnabled(true)
            
            // Sincronizar todos los nodos críticos para una experiencia Offline-First fluida
            database.getReference("Productos").keepSynced(true)
            database.getReference("Usuarios").keepSynced(true)
            database.getReference("Clientes").keepSynced(true)
            database.getReference("Ventas").keepSynced(true)
            database.getReference("Creditos").keepSynced(true)
            database.getReference("Logs").keepSynced(true)
            
        } catch (e: Exception) {
            // Ya estaba habilitada o error de instancia
        }

        // EJECUTAR MIGRACIÓN ÚNICA (Se puede eliminar después de la primera ejecución con éxito)
        MigrationManager(this).migrate()

        enableEdgeToEdge()
        setContent {
            PhoenixTheme {
                AppNavigation()
            }
        }
    }
}
