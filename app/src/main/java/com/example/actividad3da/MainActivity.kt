package com.example.actividad3da

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) { App() }
            }
        }
    }
}

@Composable
fun App(vm: AppViewModel = viewModel()) {
    if (vm.loggedUser == null) LoginScreen(vm) else HomeScreen(vm)
}

// ====================== LOGIN ======================
@Composable
fun LoginScreen(vm: AppViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Front", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            vm.baseUrl, { vm.baseUrl = it }, label = { Text("URL del servidor") },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            username, { username = it }, label = { Text("Usuario") },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            password, { password = it }, label = { Text("Contraseña") },
            singleLine = true, visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            email, { email = it }, label = { Text("Email (solo para registrarse)") },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.login(username, password) }, enabled = !vm.loading) {
                Text("Iniciar sesión")
            }
            OutlinedButton(onClick = { vm.register(username, password, email) }, enabled = !vm.loading) {
                Text("Registrarse")
            }
        }
        if (vm.loading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 12.dp))
        vm.message?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

// ====================== HOME ======================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: AppViewModel) {
    var tab by remember { mutableIntStateOf(0) }

    var showM by remember { mutableStateOf(false) }
    var editM by remember { mutableStateOf<Municipio?>(null) }
    var showU by remember { mutableStateOf(false) }
    var editU by remember { mutableStateOf<User?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hola, ${vm.loggedUser}") },
                actions = {
                    TextButton(onClick = { vm.refresh() }) { Text("Recargar") }
                    TextButton(onClick = { vm.logout() }) { Text("Salir") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (tab == 0) { editM = null; showM = true } else { editU = null; showU = true }
            }) { Text("+") }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Municipios") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Usuarios") })
            }
            if (vm.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            vm.message?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
            }

            if (tab == 0) {
                LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(vm.municipios, key = { it.name }) { m ->
                        ItemCard(
                            title = m.name,
                            subtitle = "Población: ${m.population} | Temp: ${m.temperature}°C",
                            onEdit = { editM = m; showM = true },
                            onDelete = { vm.deleteMunicipio(m.name) }
                        )
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(vm.users, key = { it.username }) { u ->
                        ItemCard(
                            title = u.username,
                            subtitle = u.email,
                            onEdit = { editU = u; showU = true },
                            onDelete = { vm.deleteUser(u.username) }
                        )
                    }
                }
            }
        }
    }

    if (showM) MunicipioDialog(
        initial = editM,
        onDismiss = { showM = false },
        onSave = { vm.saveMunicipio(it, isNew = editM == null); showM = false }
    )
    if (showU) UserDialog(
        initial = editU,
        onDismiss = { showU = false },
        onSave = { vm.saveUser(it, isNew = editU == null); showU = false }
    )
}

@Composable
fun ItemCard(title: String, subtitle: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = onEdit) { Text("Editar") }
            TextButton(onClick = onDelete) { Text("Borrar") }
        }
    }
}

// ====================== DIALOGOS ======================
@Composable
fun MunicipioDialog(initial: Municipio?, onDismiss: () -> Unit, onSave: (Municipio) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var pop by remember { mutableStateOf(initial?.population?.toString() ?: "") }
    var temp by remember { mutableStateOf(initial?.temperature?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nuevo municipio" else "Editar municipio") },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") },
                    singleLine = true, enabled = initial == null) // el nombre es la clave
                OutlinedTextField(pop, { pop = it }, label = { Text("Población") }, singleLine = true)
                OutlinedTextField(temp, { temp = it }, label = { Text("Temperatura") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val p = pop.toIntOrNull()
                val t = temp.toDoubleOrNull()
                if (name.isNotBlank() && p != null && t != null) onSave(Municipio(name.trim(), p, t))
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun UserDialog(initial: User?, onDismiss: () -> Unit, onSave: (User) -> Unit) {
    var username by remember { mutableStateOf(initial?.username ?: "") }
    var password by remember { mutableStateOf(initial?.password ?: "") }
    var email by remember { mutableStateOf(initial?.email ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nuevo usuario" else "Editar usuario") },
        text = {
            Column {
                OutlinedTextField(username, { username = it }, label = { Text("Usuario") },
                    singleLine = true, enabled = initial == null)
                OutlinedTextField(password, { password = it }, label = { Text("Contraseña") }, singleLine = true)
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (username.isNotBlank()) onSave(User(username.trim(), password, email))
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
