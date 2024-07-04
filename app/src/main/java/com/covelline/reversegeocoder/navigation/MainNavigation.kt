package com.covelline.reversegeocoder.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.covelline.reversegeocoder.ui.screen.CurrentLocationListScreen
import com.covelline.reversegeocoder.ui.screen.CurrentLocationListViewModel
import com.covelline.reversegeocoder.ui.screen.LicensesScreen
import com.covelline.reversegeocoder.ui.screen.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable object CurrentLocationListScreenDestination

@Serializable object SettingsScreenDestination

@Serializable object LicensesScreenDestination

@Composable
fun MainNavigation() {
  val navController = rememberNavController()
  val currentLocationListViewModel: CurrentLocationListViewModel = viewModel()
  NavHost(navController = navController, startDestination = CurrentLocationListScreenDestination) {
    composable<CurrentLocationListScreenDestination> {
      CurrentLocationListScreen(
        viewModel = currentLocationListViewModel,
        navigateToSetting = { navController.navigate(SettingsScreenDestination) },
      )
    }
    composable<SettingsScreenDestination> {
      SettingsScreen(
        viewModel = currentLocationListViewModel,
        onNavigateToLicense = { navController.navigate(LicensesScreenDestination) },
        onBackClicked = {
          navController.popBackStack(
            route = CurrentLocationListScreenDestination,
            inclusive = false,
          )
        },
      )
    }
    composable<LicensesScreenDestination> {
      LicensesScreen(
        onBackClicked = {
          navController.popBackStack(route = SettingsScreenDestination, inclusive = false)
        }
      )
    }
  }
}
