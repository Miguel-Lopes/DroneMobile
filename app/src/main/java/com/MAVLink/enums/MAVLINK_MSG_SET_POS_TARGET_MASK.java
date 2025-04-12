package com.MAVLink.enums;

public class MAVLINK_MSG_SET_POS_TARGET_MASK {
    public static final int X = 1;         // Ignore position x
    public static final int Y = 2;         // Ignore position y
    public static final int Z = 4;         // Ignore position z
    public static final int VX = 8;        // Ignore velocity x
    public static final int VY = 16;       // Ignore velocity y
    public static final int VZ = 32;       // Ignore velocity z
    public static final int AFX = 64;      // Ignore acceleration x
    public static final int AFY = 128;     // Ignore acceleration y
    public static final int AFZ = 256;     // Ignore acceleration z
    public static final int YAW = 512;     // Ignore yaw
    public static final int YAW_RATE = 1024; // Ignore yaw rate
}
