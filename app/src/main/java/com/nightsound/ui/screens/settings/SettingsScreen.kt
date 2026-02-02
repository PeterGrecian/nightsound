package com.nightsound.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val nightStartHour by viewModel.nightStartHour.collectAsStateWithLifecycle()
    val nightStartMinute by viewModel.nightStartMinute.collectAsStateWithLifecycle()
    val nightEndHour by viewModel.nightEndHour.collectAsStateWithLifecycle()
    val nightEndMinute by viewModel.nightEndMinute.collectAsStateWithLifecycle()
    val s3BucketName by viewModel.s3BucketName.collectAsStateWithLifecycle()
    val s3Region by viewModel.s3Region.collectAsStateWithLifecycle()
    val awsAccessKey by viewModel.awsAccessKey.collectAsStateWithLifecycle()
    val awsSecretKey by viewModel.awsSecretKey.collectAsStateWithLifecycle()

    var bucketNameInput by remember { mutableStateOf(s3BucketName) }
    var regionInput by remember { mutableStateOf(s3Region) }
    var accessKeyInput by remember { mutableStateOf(awsAccessKey) }
    var secretKeyInput by remember { mutableStateOf(awsSecretKey) }

    LaunchedEffect(s3BucketName) { bucketNameInput = s3BucketName }
    LaunchedEffect(s3Region) { regionInput = s3Region }
    LaunchedEffect(awsAccessKey) { accessKeyInput = awsAccessKey }
    LaunchedEffect(awsSecretKey) { secretKeyInput = awsSecretKey }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Night Time Section
            Text(
                text = "Recording Schedule",
                style = MaterialTheme.typography.titleLarge
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Night Start Time",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = String.format("%02d:%02d", nightStartHour, nightStartMinute),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Note: Time pickers require TimePicker API implementation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider()

                    Text(
                        text = "Night End Time",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = String.format("%02d:%02d", nightEndHour, nightEndMinute),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // S3 Configuration Section
            Text(
                text = "S3 Configuration",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = bucketNameInput,
                onValueChange = { bucketNameInput = it },
                label = { Text("S3 Bucket Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = regionInput,
                onValueChange = { regionInput = it },
                label = { Text("AWS Region") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("us-east-1") },
                singleLine = true
            )

            OutlinedTextField(
                value = accessKeyInput,
                onValueChange = { accessKeyInput = it },
                label = { Text("AWS Access Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            OutlinedTextField(
                value = secretKeyInput,
                onValueChange = { secretKeyInput = it },
                label = { Text("AWS Secret Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Button(
                onClick = {
                    viewModel.setS3BucketName(bucketNameInput)
                    viewModel.setS3Region(regionInput)
                    viewModel.setAwsCredentials(accessKeyInput, secretKeyInput)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save S3 Settings")
            }

            // Warning about credentials
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Security Note",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "For production use, consider using AWS Cognito or STS for temporary credentials instead of storing long-term credentials.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
