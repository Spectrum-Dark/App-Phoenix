package com.spectrum.phoenix.ui.main.configuracion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spectrum.phoenix.logic.model.User
import com.spectrum.phoenix.logic.user.UsuariosViewModel
import com.spectrum.phoenix.ui.theme.FocusBlue

@Composable
fun UsuariosScreen(usuariosViewModel: UsuariosViewModel = viewModel()) {
    val users by usuariosViewModel.users.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Se ha eliminado el título como se solicitó
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(users) { user ->
                UserItem(user = user, onRoleChange = { newRole ->
                    usuariosViewModel.updateUserRole(user.userId, newRole)
                })
            }
        }
    }
}

@Composable
fun UserItem(user: User, onRoleChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("user", "admin")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(FocusBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = FocusBlue)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = user.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Surface(
                    color = if (user.role == "admin") FocusBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = user.role.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (user.role == "admin") FocusBlue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                Button(
                    onClick = { expanded = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FocusBlue.copy(alpha = 0.1f),
                        contentColor = FocusBlue
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ROL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                
                DropdownMenu(
                    expanded = expanded, 
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    roles.forEach { role ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    role.uppercase(), 
                                    fontWeight = if(user.role == role) FontWeight.Bold else FontWeight.Normal,
                                    color = if(user.role == role) FocusBlue else MaterialTheme.colorScheme.onSurface
                                ) 
                            },
                            onClick = {
                                onRoleChange(role)
                                expanded = false
                            },
                            leadingIcon = {
                                if (user.role == role) {
                                    Icon(Icons.Default.AdminPanelSettings, null, tint = FocusBlue, modifier = Modifier.size(18.dp))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
