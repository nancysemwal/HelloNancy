package com.example.hellonancy

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hellonancy.ui.theme.HelloNancyTheme
import com.felhr.usbserial.SerialInputStream
import com.felhr.usbserial.SerialOutputStream
import com.felhr.usbserial.UsbSerialDevice
import io.dronefleet.mavlink.*
import io.dronefleet.mavlink.common.*

class MainActivity : ComponentActivity() {


    val helloViewModel by viewModels<HelloViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        registerReceiver(usbReceiver, filter)
        setContent {
            HelloNancyTheme {
                MainScreen(usbManager = usbManager, helloViewModel = helloViewModel)
            }
        }
    }
    private val usbManager by lazy {
        getSystemService(Context.USB_SERVICE) as UsbManager
    }

    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    lateinit var usbConnection : UsbDeviceConnection
    lateinit var serialPort : UsbSerialDevice
    lateinit var mavlinkConn: MavlinkConnection
    private val usbReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.apply {
                            //call method to set up device communication
                            Log.d("SERVICE", "Success")
                            //helloviewmodel.openDevice
                            usbConnection = usbManager.openDevice(device)
                            helloViewModel.connectedStatus.value = "Connected"
                            serialPort = UsbSerialDevice.createUsbSerialDevice(device, usbConnection)

                            if(serialPort != null){
                                if(serialPort.syncOpen()){
                                    serialPort.setBaudRate(57600)
                                    var input : SerialInputStream = serialPort.inputStream
                                    var output : SerialOutputStream = serialPort.outputStream
                                    mavlinkConn = MavlinkConnection.create(input, output)
                                    var message : RequestDataStream = RequestDataStream.builder()
                                        .targetSystem(1)
                                        .targetComponent(0)
                                        .reqStreamId(0)
                                        .reqMessageRate(1)
                                        .startStop(1)
                                        .build()
                                    mavlinkConn.send2(255, 0, message)
                                    helloViewModel.connection.value = "Streams requested"
                                }
                            }
                        }
                    } else {
                        Log.d("SERVICE", "permission denied for device $device")
                        helloViewModel.connectedStatus.value = "Not granted"
                    }
                }
            }
        }
    }

}

@Composable
fun MainScreen(usbManager: UsbManager, helloViewModel: HelloViewModel){
    val connectedStatus by helloViewModel.connectedStatus.observeAsState("Not Connected")
    val connection by helloViewModel.connection.observeAsState("")
    val pitch by helloViewModel.pitch.observeAsState(0)
    /*val (connectedStatus, setConnectedStatus) = remember {
        mutableStateOf("Not Connected")
    }*/
    val deviceList: HashMap<String, UsbDevice> = usbManager.deviceList
    val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    val intent : Intent = Intent(ACTION_USB_PERMISSION)
    val context = LocalContext.current

    deviceList.values.forEach(){
        //Device(deviceName = it.deviceName)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent
        , 0)
        usbManager.requestPermission(it, pendingIntent)
    }
    Column() {
        Text(text = connectedStatus)
        Text(text = connection)
        Pitch(pitch = pitch as Float)
    }


}

class HelloViewModel : ViewModel(){
    private val _connectedStatus = MutableLiveData("Not Connected")
    var connectedStatus = _connectedStatus
    private val _connection = MutableLiveData("")
    var connection = _connection
    private val _name : MutableLiveData<String> = MutableLiveData("")
    val name = _name
    private val _pitch = MutableLiveData<Float>(0F)
    val pitch = _pitch

    /*val pitchLiveData : LiveData<Int> get() = _pitch
    private val _pitch = MutableLiveData<Int>()
    private var count = 0*/

    /*private val _pitch : MutableLiveData<Int> = MutableLiveData(0)
    val pitchLiveData = _pitch
    private var count = 0*/
    /*val timer = object : CountDownTimer(10000, 1000){
        override fun onTick(millisUntilFinished: Long) {
            Log.d("WATCH", "in onTick")
            _pitch.value = millisUntilFinished.toInt()
        }

        override fun onFinish() {
            Log.d("WATCH", "in onFinish")
            _pitch.value = 0
        }
    }.start()*/

    fun onNameChange(newName : String){
        _name.value = newName
    }

    fun updateCount(){
        Log.d("WATCH", "in onPitchChange")
        //_pitch.value = _pitch.value?.plus(1)
        //_pitch.value = ++count
    }
}

@Composable
fun HelloScreen(helloViewModel: HelloViewModel){
    val name by helloViewModel.name.observeAsState("")
    //val pitch by helloViewModel.pitchLiveData.observeAsState(0)

    /*var name : String by rememberSaveable{
        mutableStateOf("")
    }*/

    /*HelloContent(
        name = name,
        onNameChange = { helloViewModel.onNameChange(it) }
    )*/
    val ACTION_USB_PERMISSION: String = "com.android.example.USB_PERMISSION"
    val intent : Intent = Intent(ACTION_USB_PERMISSION)
    val context = LocalContext.current
    val usbManager:UsbManager? = context.getSystemService(Context.USB_SERVICE) as UsbManager?
    val deviceList = usbManager?.deviceList?.values
    if(deviceList != null){
        for(device in deviceList){
            val connection : UsbDeviceConnection = usbManager.openDevice(device)
            val serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection)
            val pendingIntent : PendingIntent = PendingIntent.getBroadcast(context, 0,
                intent, 0)
            usbManager.requestPermission(device, pendingIntent)
            Device(deviceName = connection.toString())
        }
    }

    /*PitchContent(
        pitch = pitch,
        onClick = {helloViewModel.updateCount()})*/
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
fun Pitch(pitch: Float){
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
                modifier = Modifier.padding(8.dp)
            )
        }
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
                modifier = Modifier.padding(8.dp)
            )
        }
        Button(onClick = onClick)
            {
                    Text(text = "Update")
            }
    }
}

@Composable
fun Device(deviceName: String){
    Column{
        Text(text = deviceName)
    }
}

@Composable
fun Pitch(pitch: Int, onPitchChange: () -> Unit){
    Text(
        text = pitch.toString()
    )
}