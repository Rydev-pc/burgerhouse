package com.burgerhouse.app.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burgerhouse.app.R
import com.burgerhouse.app.ui.util.Validation
import com.burgerhouse.app.viewmodel.AuthViewModel

/**
 * Tela de Login / Cadastro. Campos: nome, e-mail, telefone e endereço.
 * Valida o formulário e salva a sessão via DataStore.
 */
@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Mensagens de erro de validação
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }

    val sessionActive by authViewModel.sessionActive.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    // Quando o usuário existente carregar, pré-preenche os campos
    val user by authViewModel.user.collectAsState()
    LaunchedEffect(user) {
        if (user != null && name.isBlank()) {
            name = user!!.name
            email = user!!.email
            phone = user!!.phone
            address = user!!.address
        }
    }

    // Navega quando a sessão é ativada
    LaunchedEffect(sessionActive) {
        if (sessionActive) onLoggedIn()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "🍔", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; nameError = null },
            label = { Text(stringResource(R.string.login_name)) },
            isError = nameError != null,
            supportingText = { nameError?.let { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; emailError = null },
            label = { Text(stringResource(R.string.login_email)) },
            isError = emailError != null,
            supportingText = { emailError?.let { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it; phoneError = null },
            label = { Text(stringResource(R.string.login_phone)) },
            isError = phoneError != null,
            supportingText = { phoneError?.let { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it; addressError = null },
            label = { Text(stringResource(R.string.login_address)) },
            isError = addressError != null,
            supportingText = { addressError?.let { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Validação antes de prosseguir
                nameError = if (!Validation.isNotBlank(name)) "Informe seu nome." else null
                emailError = when {
                    email.isBlank() -> "Informe seu e-mail."
                    !Validation.isValidEmail(email) -> "E-mail inválido."
                    else -> null
                }
                phoneError = when {
                    phone.isBlank() -> "Informe seu telefone."
                    !Validation.isValidPhone(phone) -> "Telefone inválido."
                    else -> null
                }
                addressError = if (!Validation.isNotBlank(address)) "Informe o endereço." else null

                if (nameError == null && emailError == null &&
                    phoneError == null && addressError == null
                ) {
                    authViewModel.login(name, email, phone, address)
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.login_action))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
