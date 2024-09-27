package com.adyen.android.assignment.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.State.CREATED
import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.Lifecycle.State.INITIALIZED
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import com.adyen.android.assignment.domain.usecase.RequestPermissionsLauncher
import org.koin.androidx.compose.koinViewModel


@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    permissionsUseCase: RequestPermissionsLauncher,
    viewModel: MainViewModel = koinViewModel()
) {

    val viewState = viewModel.viewState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        permissionsUseCase.invoke { permissionRequestResult ->
            when (permissionRequestResult) {
                RequestPermissionsLauncher.PermissionRequestResult.Denied,
                is RequestPermissionsLauncher.PermissionRequestResult.GrantedPartially -> {
                    viewModel.handleEvents(MainViewModel.Event.PermissionDenied) // assume we require all requested permissions
                }

                RequestPermissionsLauncher.PermissionRequestResult.Granted -> viewModel.handleEvents(
                    MainViewModel.Event.PermissionGranted
                )
            }
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.handleEvents(MainViewModel.Event.GoToBackground)
    }

    when (val state = viewState.value) {
        MainViewModel.ViewState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        is MainViewModel.ViewState.VenueList -> {
            Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
                TopAppBar(title = { Text("Venues") })
            }) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(items = state.data) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp)
                                .padding(4.dp),
                        ) {
                            Text(text = item.name)
                        }
                    }
                }
            }
        }

        MainViewModel.ViewState.PermissionDenied -> {
            Box {
                Text(
                    textAlign = TextAlign.Center,
                    text = "Permissions required"
                )
            }
        }
    }
}