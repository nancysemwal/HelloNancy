package com.example.hellonancy

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hellonancy.ui.theme.HelloNancyTheme
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.CountDownTimer
import android.util.Log
import androidx.compose.foundation.layout.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val helloViewModel by viewModels<HelloViewModel>()
        super.onCreate(savedInstanceState)
        setContent {
            HelloNancyTheme {
                // A surface container using the 'background' color from the theme
                HelloScreen(helloViewModel)
            }
        }
    }
}

class HelloViewModel : ViewModel(){
    private val _name : MutableLiveData<String> = MutableLiveData("")
    val name = _name

    /*val pitchLiveData : LiveData<Int> get() = _pitch
    private val _pitch = MutableLiveData<Int>()
    private var count = 0*/

    /*val timer = object : CountDownTimer(10000, 1000){
        override fun onTick(millisUntilFinished: Long) {
            _pitch.value = millisUntilFinished
        }

        override fun onFinish() {
            _pitch.value = 0
        }
    }*/

    private val _pitch : MutableLiveData<Int> = MutableLiveData(0)
    val pitchLiveData = _pitch
    private var count = 0

    fun onNameChange(newName : String){
        _name.value = newName
    }

    fun updateCount(){
        Log.d("WATCH", "in onPitchChange")
        //_pitch.value = _pitch.value?.plus(1)
        _pitch.value = ++count
    }
}

@Composable
fun HelloScreen(helloViewModel: HelloViewModel){
    val name by helloViewModel.name.observeAsState("")
    val pitch by helloViewModel.pitchLiveData.observeAsState(0)
    /*var name : String by rememberSaveable{
        mutableStateOf("")
    }*/

    /*HelloContent(
        name = name,
        onNameChange = { helloViewModel.onNameChange(it) }
    )*/

    PitchContent(
        pitch = pitch,
        onClick = {helloViewModel.updateCount()})
}

@Composable
fun HelloContent(name: String, onNameChange: (String) -> Unit){
    Column(modifier = Modifier.padding(16.dp)){
        /*var name : String by rememberSaveable{
            mutableStateOf("")
        }*/
        Text(
            text = "Hello, $name!",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(8.dp)
        )
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = {Text("Name")}
        )
    }
}

@Composable
fun PitchContent(pitch: Int, onClick: () -> Unit = {}){
    Column() {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween){
            Text(
                text = "Pitch",
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(8.dp)
            )

            Text(
                text = pitch.toString(),
                style = MaterialTheme.typography.body1,
            )
        }
        Button(onClick = onClick)
            {
                    Text(text = "Update")
            }
    }
}

@Composable
fun Pitch(pitch: Int, onPitchChange: () -> Unit){
    Text(
        text = pitch.toString()
    )
}