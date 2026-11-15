package com.example.actividad3da

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {

    var baseUrl by mutableStateOf("http://titan.cerez.dev/")
    var loggedUser by mutableStateOf<String?>(null)
    var municipios by mutableStateOf<List<Municipio>>(emptyList())
    var users by mutableStateOf<List<User>>(emptyList())
    var loading by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)

    private var api: TitanApi? = null

    private fun launchSafe(block: suspend () -> Unit) {
        viewModelScope.launch {
            loading = true
            try {
                block()
            } catch (e: Exception) {
                message = "Error: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    // ---------- Login (INSEGURO: compara la password en el cliente) ----------
    fun login(username: String, password: String) = launchSafe {
        api = ApiProvider.create(baseUrl)
        val found = api!!.getUsers().firstOrNull {
            it.username == username && it.password == password
        }
        if (found != null) {
            loggedUser = found.username
            message = null
            loadAll()
        } else {
            message = "Usuario o contraseña incorrectos"
        }
    }

    fun register(username: String, password: String, email: String) = launchSafe {
        api = ApiProvider.create(baseUrl)
        val r = api!!.addUser(User(username, password, email))
        message = if (r.isSuccessful) "Usuario creado, ya puedes iniciar sesión" else "Error ${r.code()}"
    }

    fun logout() {
        loggedUser = null
        municipios = emptyList()
        users = emptyList()
        message = null
    }

    // ---------- CRUD ----------
    private suspend fun loadAll() {
        municipios = api!!.getTables()
        users = api!!.getUsers()
    }

    fun refresh() = launchSafe { loadAll() }

    fun saveMunicipio(m: Municipio, isNew: Boolean) = launchSafe {
        val r = if (isNew) api!!.addTable(m) else api!!.updateTable(m)
        if (!r.isSuccessful) message = "Error ${r.code()}"
        loadAll()
    }

    fun deleteMunicipio(name: String) = launchSafe {
        api!!.deleteTable(name)
        loadAll()
    }

    fun saveUser(u: User, isNew: Boolean) = launchSafe {
        val r = if (isNew) api!!.addUser(u) else api!!.updateUser(u)
        if (!r.isSuccessful) message = "Error ${r.code()}"
        loadAll()
    }

    fun deleteUser(name: String) = launchSafe {
        api!!.deleteUser(name)
        loadAll()
    }
}
