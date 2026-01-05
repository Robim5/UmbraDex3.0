package com.umbra.umbradex

// Em: app/src/main/java/com/umbra/umbradex/MainViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umbra.umbradex.data.repository.UserRepository
import com.umbra.umbradex.utils.Resource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// A classe MainViewModel herda de ViewModel
class MainViewModel(
    // Instanciamos o repositório aqui. Numa app maior, usaríamos injeção de dependências.
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    // Este StateFlow irá guardar as cores do tema e partilhá-las com a UI.
    // Ele sobrevive a mudanças de configuração, como a rotação do ecrã.
    val themeColors: StateFlow<List<String>?> = userRepository.getUserProfile()
        .catch { e ->
            // Se houver erro (ex: utilizador não autenticado), emite um Resource.Error
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
        .map { result ->
            // Processamos o resultado que vem do repositório
            when (result) {
                is Resource.Success -> {
                    // Se for sucesso, tentamos fazer o parsing das cores
                    val themeStr = result.data?.equippedTheme
                    if (themeStr.isNullOrBlank() || themeStr == "[]") {
                        null // Retorna nulo se o tema estiver vazio
                    } else {
                        try {
                            // Lógica de parsing segura
                            val colors = themeStr.removeSurrounding("[", "]")
                                .replace("\"", "")
                                .split(",")
                                .map { it.trim() }
                                .filter { it.startsWith("#") }

                            // Garante que temos um tema válido (ex: 6 cores)
                            if (colors.size >= 6) colors else null
                        } catch (e: Exception) {
                            // Se o parsing falhar, retorna nulo para usar o tema padrão
                            null
                        }
                    }
                }
                // Em caso de erro ou loading, usamos o tema padrão (nulo)
                is Resource.Error, is Resource.Loading -> null
            }
        }
        .stateIn(
            scope = viewModelScope, // O scope do ViewModel
            started = SharingStarted.WhileSubscribed(5000), // Mantém o flow ativo por 5s
            initialValue = null // O valor inicial é nulo (tema padrão)
        )
}
