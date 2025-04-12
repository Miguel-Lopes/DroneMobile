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

class MainActivity : AppCompatActivity() {

    private val droneIP = "192.168.4.1" // Replace with SITL IP or real drone later
    private val dronePort = 14550 // MAVLink port
    private lateinit var socket: DatagramSocket
    private lateinit var address: InetAddress

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // TODO: Create the activity_main.xml layout with buttons

        socket = DatagramSocket()
        address = InetAddress.getByName(droneIP)

        //Front end stuff
        val armBtn: Button = findViewById(R.id.armButton)
        val takeoffBtn: Button = findViewById(R.id.takeoffButton)
        val landBtn: Button = findViewById(R.id.landButton)
        val leftJoystick: Button = findViewById(R.id.leftJoystick)
        val rightJoystick: Button = findViewById(R.id.rightJoystick)

        armBtn.setOnClickListener {
            sendMavlinkCommand(MavlinkCommands.armCommand(true))
        }

        takeoffBtn.setOnClickListener {
            sendMavlinkCommand(MavlinkCommands.takeoffCommand(10f))
        }

        landBtn.setOnClickListener {
            sendMavlinkCommand(MavlinkCommands.landCommand())
        }
        leftJoystick.setOnClickListener {
            sendMavlinkCommand(MavlinkJoystick.leftStickToVelocityPacket()) //TODO add front end values
        }
        rightJoystick.setOnClickListener {
            sendMavlinkCommand(MavlinkJoystick.rightStickToVelocityPacket()) //TODO add front end values
        }
    }

    private fun sendMavlinkCommand(data: ByteArray) {
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
