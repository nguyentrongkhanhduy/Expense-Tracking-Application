package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.services.CurrencyApiService
import com.example.myapplication.services.CurrencyResponse
import com.example.myapplication.services.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

const val apiKey = "5a19c8df99235bef3059833373dd97d9" //add your API key here from currencylayer.com

class CurrencyViewModel: ViewModel() {
    private val apiService = RetrofitClient.createService(CurrencyApiService::class.java, "https://apilayer.net/")

    private val _exchangeRates = MutableStateFlow<CurrencyResponse?>(null)
    val exchangeRates: StateFlow<CurrencyResponse?> = _exchangeRates

    fun getExchangeRate(source: String, target: String, onResult: (CurrencyResponse) -> Unit) {
        viewModelScope.launch {
            val response = apiService.getExchangeRates(
                accessKey = apiKey,
                currencies = target,
                source = source,
                format = 1
            )
            _exchangeRates.value = response
            onResult(response)
        }
    }

    fun getCurrencyShortForm(currency: String): String {
        return when (currency) {
            "US Dollar" -> "USD"
            "Canadian Dollar" -> "CAD"
            "Australian Dollar" -> "AUD"
            "Indian Rupee" -> "INR"
            "Euro" -> "EUR"
            "British Pound" -> "GBP"
            "Japanese Yen" -> "JPY"
            "Chinese Yuan" -> "CNY"
            "Swiss Franc" -> "CHF"
            "Swedish Krona" -> "SEK"
            "Hong Kong Dollar" -> "HKD"
            "Singapore Dollar" -> "SGD"
            "Russian Ruble" -> "RUB"
            else -> ""
        }
    }
}