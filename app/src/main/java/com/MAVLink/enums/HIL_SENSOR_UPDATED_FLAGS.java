/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

package com.MAVLink.enums;

/**
 * Flags in the HIL_SENSOR message indicate which fields have updated since the last message
 */
public class HIL_SENSOR_UPDATED_FLAGS {
   public static final int HIL_SENSOR_UPDATED_XACC = 1; /* The value in the xacc field has been updated | */
   public static final int HIL_SENSOR_UPDATED_YACC = 2; /* The value in the yacc field has been updated | */
   public static final int HIL_SENSOR_UPDATED_ZACC = 4; /* The value in the zacc field has been updated | */
   public static final int HIL_SENSOR_UPDATED_XGYRO = 8; /* The value in the xgyro field has been updated | */
   public static final int HIL_SENSOR_UPDATED_YGYRO = 16; /* The value in the ygyro field has been updated | */
   public static final int HIL_SENSOR_UPDATED_ZGYRO = 32; /* The value in the zgyro field has been updated | */
   public static final int HIL_SENSOR_UPDATED_XMAG = 64; /* The value in the xmag field has been updated | */
   public static final int HIL_SENSOR_UPDATED_YMAG = 128; /* The value in the ymag field has been updated | */
   public static final int HIL_SENSOR_UPDATED_ZMAG = 256; /* The value in the zmag field has been updated | */
   public static final int HIL_SENSOR_UPDATED_ABS_PRESSURE = 512; /* The value in the abs_pressure field has been updated | */
   public static final int HIL_SENSOR_UPDATED_DIFF_PRESSURE = 1024; /* The value in the diff_pressure field has been updated | */
   public static final int HIL_SENSOR_UPDATED_PRESSURE_ALT = 2048; /* The value in the pressure_alt field has been updated | */
   public static final int HIL_SENSOR_UPDATED_TEMPERATURE = 4096; /* The value in the temperature field has been updated | */
   public static final long HIL_SENSOR_UPDATED_RESET = 2147483648L; /* Full reset of attitude/position/velocities/etc was performed in sim (Bit 31). | */
   public static final long HIL_SENSOR_UPDATED_FLAGS_ENUM_END = 2147483649L; /*  | */
}