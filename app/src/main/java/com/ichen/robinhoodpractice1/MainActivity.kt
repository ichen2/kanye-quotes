package com.ichen.robinhoodpractice1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : AppCompatActivity() {

    val quoteText : TextView by lazy { findViewById(R.id.quote_text) }
    val quoteButton : Button by lazy { findViewById(R.id.quote_button) }
    val viewModel : MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        observeData()
        quoteButton.setOnClickListener {
            viewModel.getQuote()
        }
    }

    fun observeData() {
        viewModel.quoteState.observe(this) { state ->
            when(state) {
                RequestState.NOT_STARTED -> quoteText.text = ""
                RequestState.LOADING -> quoteText.text = "Loading..."
                RequestState.FAILURE -> quoteText.text = "Failed to get quote"
                else -> {}
            }
        }
        viewModel.quote.observe(this) { quote ->
            if(quote != null) quoteText.text = quote
        }
    }
}

class MainViewModel : ViewModel() {
    val quoteState = MutableLiveData<RequestState>().apply { value = RequestState.NOT_STARTED }
    val quote = MutableLiveData<String?>().apply { value = null}
    val repository = MainRepository()

    fun getQuote() {
        quoteState.value = RequestState.LOADING
        repository.getQuote { result ->
            if(result == null) {
                quoteState.value = RequestState.FAILURE
            } else {
                quoteState.value = RequestState.SUCCESS
            }
            quote.value = if(result==null) result else result + "\n- Kanye West"
        }
    }
}

class MainRepository {
    val retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
    val service = retrofit.create(KanyeService::class.java)
    val call = service.getResponse()

    fun getQuote(onResult: (String?) -> Unit) {
        call.clone().enqueue(object : Callback<KanyeResponse> {
            override fun onResponse(
                call: Call<KanyeResponse>,
                response: Response<KanyeResponse>
            ) {
                onResult(response.body()?.quote)
            }

            override fun onFailure(call: Call<KanyeResponse>, t: Throwable) {
                onResult(null)
            }
        })
    }
}

const val BASE_URL = "https://api.kanye.rest/"
interface KanyeService {
    @GET(".")
    fun getResponse(): Call<KanyeResponse>
}

enum class RequestState {
    NOT_STARTED,
    LOADING,
    SUCCESS,
    FAILURE
}

data class KanyeResponse(val quote: String)