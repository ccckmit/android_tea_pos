package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.model.AppDatabase
import com.example.data.repository.OrderRepository
import com.example.ui.screens.PosScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PosViewModel

class MainActivity : ComponentActivity() {

  // Initialize Room database & repository locally
  private val database by lazy {
    Room.databaseBuilder(
      applicationContext,
      AppDatabase::class.java,
      "bubble_tea_pos_system.db"
    )
    .fallbackToDestructiveMigration()
    .build()
  }

  private val repository by lazy {
    OrderRepository(database.orderDao())
  }

  // Supply repository dependency to our POS ViewModel safely using Factories
  private val viewModel by lazy {
    ViewModelProvider(this, PosViewModel.Factory(repository))[PosViewModel::class.java]
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          PosScreen(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}

