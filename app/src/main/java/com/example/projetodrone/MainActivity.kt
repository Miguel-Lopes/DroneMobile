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
import com.MAVLink.enums.MAV_CMD
import com.MAVLink.Messages.MAVLinkPacket


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

        val armBtn: Button = findViewById(R.id.armButton)
        val takeoffBtn: Button = findViewById(R.id.takeoffButton)
        val landBtn: Button = findViewById(R.id.landButton)

        armBtn.setOnClickListener {
            sendMavlinkCommand(MavlinkCommands.armCommand(true))
        }

        takeoffBtn.setOnClickListener {
            sendMavlinkCommand(MavlinkCommands.takeoffCommand(10f))
        }

        landBtn.setOnClickListener {
            sendMavlinkCommand(MavlinkCommands.landCommand())
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

