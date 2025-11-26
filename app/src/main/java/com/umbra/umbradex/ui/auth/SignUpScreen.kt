package com.umbra.umbradex.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.umbra.umbradex.R
import com.umbra.umbradex.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    authState: AuthState,
    onSignUpClick: (String, String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val isLoading = authState is AuthState.Loading

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PurpleBackground,
                        PurpleSurface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = PurpleTertiary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "UmbraDex Logo",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 16.dp)
            )

            // Title
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineLarge,
                color = PurpleTertiary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Join the UmbraDex community",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon",
                        tint = PurpleTertiary
                    )
                },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurplePrimary,
                    unfocusedBorderColor = PurpleTertiary,
                    focusedLabelColor = PurplePrimary,
                    cursorColor = PurplePrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon",
                        tint = PurpleTertiary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible)
                                "Hide password"
                            else
                                "Show password",
                            tint = PurpleTertiary
                        )
                    }
                },
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = passwordError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurplePrimary,
                    unfocusedBorderColor = PurpleTertiary,
                    focusedLabelColor = PurplePrimary,
                    cursorColor = PurplePrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = null
                },
                label = { Text("Confirm Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Confirm Password Icon",
                        tint = PurpleTertiary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible)
                                "Hide password"
                            else
                                "Show password",
                            tint = PurpleTertiary
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                isError = confirmPasswordError != null,
                supportingText = confirmPasswordError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (validateSignUpInputs(
                                email, password, confirmPassword,
                                onEmailError = { emailError = it },
                                onPasswordError = { passwordError = it },
                                onConfirmPasswordError = { confirmPasswordError = it }
                            )) {
                            onSignUpClick(email, password)
                        }
                    }
                ),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurplePrimary,
                    unfocusedBorderColor = PurpleTertiary,
                    focusedLabelColor = PurplePrimary,
                    cursorColor = PurplePrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Error message
            if (authState is AuthState.Error) {
                Text(
                    text = authState.message,
                    color = ErrorColor,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Up Button
            Button(
                onClick = {
                    if (validateSignUpInputs(
                            email, password, confirmPassword,
                            onEmailError = { emailError = it },
                            onPasswordError = { passwordError = it },
                            onConfirmPasswordError = { confirmPasswordError = it }
                        )) {
                        onSignUpClick(email, password)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurplePrimary,
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign Up",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Terms text
            Text(
                text = "By signing up, you agree to our Terms & Privacy Policy",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

// Validation helper for sign up
private fun validateSignUpInputs(
    email: String,
    password: String,
    confirmPassword: String,
    onEmailError: (String?) -> Unit,
    onPasswordError: (String?) -> Unit,
    onConfirmPasswordError: (String?) -> Unit
): Boolean {
    var isValid = true

    // Email validation
    if (email.isBlank()) {
        onEmailError("Email is required")
        isValid = false
    } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onEmailError("Invalid email format")
        isValid = false
    } else {
        onEmailError(null)
    }

    // Password validation
    if (password.isBlank()) {
        onPasswordError("Password is required")
        isValid = false
    } else if (password.length < 6) {
        onPasswordError("Password must be at least 6 characters")
        isValid = false
    } else {
        onPasswordError(null)
    }

    // Confirm password validation
    if (confirmPassword.isBlank()) {
        onConfirmPasswordError("Please confirm your password")
        isValid = false
    } else if (password != confirmPassword) {
        onConfirmPasswordError("Passwords don't match")
        isValid = false
    } else {
        onConfirmPasswordError(null)
    }

    return isValid
}