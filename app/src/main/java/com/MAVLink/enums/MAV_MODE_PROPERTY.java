/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

package com.MAVLink.enums;

/**
 * Mode properties.
      
 */
public class MAV_MODE_PROPERTY {
   public static final int MAV_MODE_PROPERTY_ADVANCED = 1; /* If set, this mode is an advanced mode.
          For example a rate-controlled manual mode might be advanced, whereas a position-controlled manual mode is not.
          A GCS can optionally use this flag to configure the UI for its intended users.
         | */
   public static final int MAV_MODE_PROPERTY_NOT_USER_SELECTABLE = 2; /* If set, this mode should not be added to the list of selectable modes.
          The mode might still be selected by the FC directly (for example as part of a failsafe).
         | */
   public static final int MAV_MODE_PROPERTY_AUTO_MODE = 4; /* If set, this mode is automatically controlled (it may use but does not require a manual controller).
          If unset the mode is a assumed to require user input (be a manual mode).
         | */
   public static final int MAV_MODE_PROPERTY_ENUM_END = 5; /*  | */
}