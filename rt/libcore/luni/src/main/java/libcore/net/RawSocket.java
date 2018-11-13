/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.net;

import dalvik.system.CloseGuard;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;
import libcore.io.IoBridge;

/**
 * This class allows raw L2 packets to be sent and received via the
 * specified network interface.  The receive-side implementation is
 * restricted to UDP packets for efficiency.
 *
 * @hide
 */
public class RawSocket implements Closeable {
    /**
     * Ethernet IP protocol type, part of the L2 header of IP packets.
     */
    public static final short ETH_P_IP = (short) 0x0800;

    /**
     * Ethernet ARP protocol type, part of the L2 header of ARP packets.
     */
    public static final short ETH_P_ARP = (short) 0x0806;

    private static native void create(FileDescriptor fd, short
            protocolType, String interfaceName)
            throws SocketException;
    private static native int sendPacket(FileDescriptor fd,
        String interfaceName, short protocolType, byte[] destMac, byte[] packet,
        int offset, int byteCount);
    private static native int recvPacket(FileDescriptor fd, byte[] packet,
        int offset, int byteCount, int destPort, int timeoutMillis);

    private final FileDescriptor fd;
    private final String mInterfaceName;
    private final short mProtocolType;
    private final CloseGuard guard = CloseGuard.get();

    /**
     * Creates a socket on the specified interface.
     */
    public RawSocket(String interfaceName, short protocolType)
        throws SocketException {
        mInterfaceName = interfaceName;
        mProtocolType = protocolType;
        fd = new FileDescriptor();
        create(fd, mProtocolType, mInterfaceName);
        guard.open("close");
    }

    /**
     * Reads a raw packet into the specified buffer, with the
     * specified timeout.  If the destPort is -1, then the IP
     * destination port is not verified, otherwise only packets
     * destined for the specified UDP port are returned.  Returns the
     * length actually read.  No indication of overflow is signaled.
     * The packet data will start at the IP header (EthernetII
     * dest/source/type headers are removed).
     */
    public int read(byte[] packet, int offset, int byteCount, int destPort,
        int timeoutMillis) {
        if (packet == null) {
            throw new NullPointerException("packet == null");
        }

        Arrays.checkOffsetAndCount(packet.length, offset, byteCount);

        if (destPort > 65535) {
            throw new IllegalArgumentException("Port out of range: "
                + destPort);
        }

        return recvPacket(fd, packet, offset, byteCount, destPort,
            timeoutMillis);
    }

    /**
     * Writes a raw packet to the desired interface.  A L2 header will
     * be added which includes the specified destination address, our
     * source MAC, and the specified protocol type.  The caller is responsible
     * for computing correct IP-header and payload checksums.
     */
    public int write(byte[] destMac, byte[] packet, int offset, int byteCount) {
        if (destMac == null) {
            throw new NullPointerException("destMac == null");
        }

        if (packet == null) {
            throw new NullPointerException("packet == null");
        }

        Arrays.checkOffsetAndCount(packet.length, offset, byteCount);

        if (destMac.length != 6) {
            throw new IllegalArgumentException("MAC length must be 6: "
                + destMac.length);
        }

        return sendPacket(fd, mInterfaceName, mProtocolType, destMac, packet,
            offset, byteCount);
    }

    /**
     * Closes the socket.  After this method is invoked, subsequent
     * read/write operations will fail.
     */
    public void close() throws IOException {
        guard.close();
        IoBridge.closeSocket(fd);
    }

    @Override protected void finalize() throws Throwable {
        try {
            if (guard != null) {
                guard.warnIfOpen();
            }
            close();
        } finally {
            super.finalize();
        }
    }
}
