package com.pehenrii.packet.bridge.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a message packet.
 * <p>
 * This annotation should be used on classes that represent packets
 * to be sent or received in a messaging system.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * &#64;PacketInfo(name = "login", channel = "auth")
 * public class LoginPacket implements PacketInfo {
 *     // implementation
 * }
 * </pre>
 * </p>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PacketInfo {

    /**
     * The name of the packet.
     *
     * @return the packet name
     */
    String name();

    /**
     * The channel associated with the packet.
     * Default value is an empty string.
     *
     * @return the channel name
     */
    String channel() default "";
}
