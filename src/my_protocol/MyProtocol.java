package my_protocol;

import framework.IRDTProtocol;
import framework.Utils;
import Utils.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @version 10-07-2019
 * <p>
 * Copyright University of Twente, 2013-2019
 * <p>
 * *************************************************************************
 * Copyright notice                            *
 * *
 * This file may ONLY be distributed UNMODIFIED.              *
 * In particular, a correct solution to the challenge must NOT be posted  *
 * in public places, to preserve the learning effect for future students. *
 * *************************************************************************
 */
public class MyProtocol extends IRDTProtocol {

    // change the following as you wish:
    static final int HEADERSIZE = 1;   // number of header bytes in each packet
    static final int DATASIZE = 128;   // max. number of user data bytes in each packet
    static final int PIPESIZE = 3;
    static int pointer = 0;

    @Override
    public void sender() {

        Integer[] fileContents = Utils.getFileContents(getFileID());
        chunkArray(fileContents, 3);
//        getNetworkLayer().sendPacket(pkt);


        boolean stop;
        Integer[] acknowledgement = getNetworkLayer().receivePacket();
        if (acknowledgement != null) {

            // tell the user
            System.out.println("Received acknowledgement for packet" + acknowledgement[0]);
        } else {
            // wait ~10ms (or however long the OS makes us wait) before trying again
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                stop = true;
            }
        }

    }

    @Override
    public void TimeoutElapsed(Object tag) {
        int z = (Integer) tag;
        // handle expiration of the timeout:
        System.out.println("Timer expired with tag=" + z);
    }

    @Override
    public Integer[] receiver() {
        System.out.println("Receiving...");

        // create the array that will contain the file contents
        // note: we don't know yet how large the file will be, so the easiest (but not most efficient)
        //   is to reallocate the array every time we find out there's more data
        Integer[] fileContents = new Integer[0];

        // loop until we are done receiving the file
        boolean stop = false;
        while (!stop) {

            // try to receive a packet from the network layer
            Integer[] packet = getNetworkLayer().receivePacket();

            // if we indeed received a packet
            if (packet != null) {

                // tell the user
                System.out.println("Received packet, length=" + packet.length + "  first byte=" + packet[0]);

                // append the packet's data part (excluding the header) to the fileContents array, first making it larger
                int oldlength = fileContents.length;
                int datalen = packet.length - HEADERSIZE;
                fileContents = Arrays.copyOf(fileContents, oldlength + datalen);
                System.arraycopy(packet, HEADERSIZE, fileContents, oldlength, datalen);

                // and let's just hope the file is now complete
                stop = true;

            } else {
                // wait ~10ms (or however long the OS makes us wait) before trying again
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    stop = true;
                }
            }
        }

        // return the output file
        return fileContents;
    }


    public static Integer[][] chunkArray(Integer[] array, int chunkSize) {
        int numOfChunks = (int) Math.ceil((double) array.length / chunkSize);
        Integer[][] output = new Integer[numOfChunks][];

        for (int i = 0; i < numOfChunks; ++i) {
            int start = i * chunkSize;
            int length = Math.min(array.length - start, chunkSize);

            Integer[] temp = new Integer[length];
            System.arraycopy(array, start, temp, 0, length);
            output[i] = temp;
        }

        for (Integer[] i : output) {

            System.out.print("\n[");
            for (Integer j : i) {
                System.out.print(j + ",");
            }
            System.out.print("]");
        }
        return output;
    }
}
