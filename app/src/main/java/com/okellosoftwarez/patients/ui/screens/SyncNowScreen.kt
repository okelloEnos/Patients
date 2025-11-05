package com.okellosoftwarez.patients.ui.screens



import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.okellosoftwarez.patients.viewmodel.SyncNowViewModel
import kotlinx.coroutines.launch

@Composable
fun SyncNowScreen(
    viewModel: SyncNowViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val pendingCount by viewModel.pendingCount.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val message by viewModel.message.collectAsState()

//    val snackbarHostState = rememberSnackbarHostState()
    val coroutineScope = rememberCoroutineScope()

    // Show snackbar when message appears
    LaunchedEffect(message) {
        message?.let { msg ->
//            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        SnackbarHost(hostState = snackbarHostState)

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Offline Sync", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))
                Text(text = "Pending items: $pendingCount", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            viewModel.syncNow()
                        },
                        enabled = !isSyncing
                    ) {
                        Text(text = if (isSyncing) "Syncing..." else "Sync Now")
                    }

                    Spacer(Modifier.width(16.dp))

                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Tip: The app automatically retries failed uploads. Use 'Sync Now' to force immediate retry.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Manual refresh count button (small)
        Button(onClick = { viewModel.refreshPendingCount() }) {
            Text("Refresh Count")
        }
    }
}
