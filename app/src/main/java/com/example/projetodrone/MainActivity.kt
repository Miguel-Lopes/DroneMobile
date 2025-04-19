package com.example.projetodrone

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

//MavLink imports
import com.MAVLink.common.msg_command_long
import com.MAVLink.common.msg_set_position_target_local_ned
import com.MAVLink.enums.MAV_CMD
import com.MAVLink.enums.MAV_FRAME
import com.MAVLink.enums.MAVLINK_MSG_SET_POS_TARGET_MASK //Criei eu
import com.MAVLink.MAVLinkPacket
import io.github.controlwear.virtual.joystick.android.JoystickView
import java.io.OutputStream
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private val droneIP = "10.0.2.2" // Replace with SITL IP or real drone later
    private val dronePort = 5763 // MAVLink port
    private lateinit var socket: Socket
    private lateinit var address: InetAddress
    private lateinit var outputStream: OutputStream

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // TODO: Create the activity_main.xml layout with buttons

        // Initialize TCP connection in a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                address = InetAddress.getByName(droneIP)
                socket = Socket(address, dronePort)
                outputStream = socket.getOutputStream()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Connected to drone", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Connection failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }

        //Front end stuff
        val btnArm: Button = findViewById(R.id.btnArm)
        val btnTakeOff: Button = findViewById(R.id.btnTakeOff)
        val btnLand: Button = findViewById(R.id.btnLand)
        val joystickLeft: JoystickView = findViewById(R.id.joystickLeft)
        val joystickRight: JoystickView = findViewById(R.id.joystickRight)

        btnArm.setOnClickListener {
            sendMavlinkCommand(MavlinkCommands.armCommand(true))
        }

        btnTakeOff.setOnClickListener {
            sendMavlinkCommand(MavlinkCommands.takeoffCommand(10f))
        }

        btnLand.setOnClickListener {
            sendMavlinkCommand(MavlinkCommands.landCommand())
        }
       /*leftJoystick.setOnClickListener {
            sendMavlinkCommand(MavlinkJoystick.leftStickToVelocityPacket()) //TODO add front end values
        }*/
        joystickLeft.setOnMoveListener { angle, strength ->
            // Do whatever you want with angle and strength
            Log.d("Joystick", "Angle: $angle, Strength: $strength")
        }
        /*rightJoystick.setOnClickListener {
            sendMavlinkCommand(MavlinkJoystick.rightStickToVelocityPacket()) //TODO add front end values
        }*/

        joystickRight.setOnMoveListener { angle, strength ->
            // Do whatever you want with angle and strength
            Log.d("Joystick", "Angle: $angle, Strength: $strength")
        }
    }

    /*private fun sendMavlinkCommand(data: ByteArray) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val packet = DatagramPacket(data, data.size, address, dronePort)
                socket.send(packet)
                Log.d("MAVLINK", "Command sent")
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Failed to send command", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }*/

    private fun sendMavlinkCommand(data: ByteArray) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (::outputStream.isInitialized) {
                    outputStream.write(data)
                    outputStream.flush()
                    Log.d("MAVLINK", "Command sent via TCP")
                } else {
                    Log.e("MavLink ERROR","Not connected to drone")
                }
            } catch (e: Exception) {
                Log.e("MavLink ERROR","Failed to send command")
                e.printStackTrace()

                // Attempt to reconnect
                try {
                    socket = Socket(address, dronePort)
                    outputStream = socket.getOutputStream()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (::socket.isInitialized) {
                socket.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

object MavlinkCommands {

    fun armCommand(arm: Boolean): ByteArray {
        val msg = msg_command_long()
        msg.target_system = 1
        msg.target_component = 1
        msg.command = MAV_CMD.MAV_CMD_COMPONENT_ARM_DISARM
        msg.confirmation = 0
        msg.param1 = if (arm) 1f else 0f
        msg.param2 = 21196f // magic value required by ArduPilot
        val packet: MAVLinkPacket = msg.pack()
        return packet.encodePacket()
    }

    fun takeoffCommand(altitude: Float): ByteArray {
        val msg = msg_command_long()
        msg.target_system = 1
        msg.target_component = 1
        msg.command = MAV_CMD.MAV_CMD_NAV_TAKEOFF
        msg.param5 = 0f // Latitude
        msg.param6 = 0f // Longitude
        msg.param7 = altitude
        val packet: MAVLinkPacket = msg.pack()
        return packet.encodePacket()
    }

    fun landCommand(): ByteArray {
        val msg = msg_command_long()
        msg.target_system = 1
        msg.target_component = 1
        msg.command = MAV_CMD.MAV_CMD_NAV_LAND
        val packet: MAVLinkPacket = msg.pack()
        return packet.encodePacket()
    }

}

object MavlinkJoystick {

    //DONT ALTER CONST'S TO CAPS
    private const val sysid = 255
    private const val compid = 190
    private const val targetSystem: Short = 1
    private const val targetComponent: Short = 1

    /**
     * LEFT STICK: Controls vertical velocity (throttle) and yaw rate
     */
    fun leftStickToVelocityPacket(throttle: Float, yawRate: Float): ByteArray {
        val msg = msg_set_position_target_local_ned()
        msg.target_system = targetSystem
        msg.target_component = targetComponent
        msg.coordinate_frame = MAV_FRAME.MAV_FRAME_LOCAL_NED.toShort()

        // Only use vertical velocity (vz) and yaw rate
        msg.type_mask = (
                1 or 2 or  // ignore pos x/y
                        4 or       // ignore pos z (redundant but okay)
                        8 or 16 or // ignore vx, vy
                        64 or 128 or 256 or // ignore acceleration
                        512        // ignore yaw
                )

        msg.vx = 0f
        msg.vy = 0f
        msg.vz = throttle    // NED: down is +, up is -
        msg.yaw_rate = yawRate

        msg.sysid = sysid
        msg.compid = compid
        msg.isMavlink2 = true

        return msg.pack().encodePacket()
    }

    /**
     * RIGHT STICK: Controls horizontal velocity (pitch = vx, roll = vy)
     */
    fun rightStickToVelocityPacket(pitch: Float, roll: Float): ByteArray {
        val msg = msg_set_position_target_local_ned()
        msg.target_system = targetSystem
        msg.target_component = targetComponent
        msg.coordinate_frame = MAV_FRAME.MAV_FRAME_LOCAL_NED.toShort()

        // Only use vx, vy
        msg.type_mask = (
                1 or 2 or 4 or  // ignore pos x/y/z
                        32 or           // ignore vz
                        64 or 128 or 256 or // ignore acceleration
                        512 or 1024     // ignore yaw and yaw rate
                )

        msg.vx = pitch  // forward/back
        msg.vy = roll   // left/right
        msg.vz = 0f     // up/down
        msg.yaw_rate = 0f //yaw i guess??

        msg.sysid = sysid
        msg.compid = compid
        msg.isMavlink2 = true

        return msg.pack().encodePacket()
    }
}
