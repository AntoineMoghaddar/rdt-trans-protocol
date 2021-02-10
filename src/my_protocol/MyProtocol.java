package my_protocol;

import framework.IRDTProtocol;
import framework.Utils;

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

    @Override
    public void sender() {
        System.out.println("Sending...");

        ArrayList<Integer[]> alldata = new ArrayList<>();
        // read from the input file
        Integer[] fileContents = Utils.getFileContents(getFileID());
        HashMap<Integer, HashMap<Boolean, Integer[]>> pipelines = new HashMap<>();
        boolean stop = false;
        int pointer = 0;

        for (int i = 0; i < fileContents.length; i += DATASIZE) {
            alldata.add(Arrays.copyOfRange(fileContents, i, i + DATASIZE));
        }
//
//        // keep track of where we are in the data
//        int filePointer = 0;
//
//        // create a new packet of appropriate size
//        int datalen = Math.min(DATASIZE, fileContents.length - filePointer);
//        Integer[] pkt = new Integer[HEADERSIZE + datalen];
//        // write something random into the header byte
//        pkt[0] = 123;
//        // copy databytes from the input file into data part of the packet, i.e., after the header
//        System.arraycopy(fileContents, filePointer, pkt, HEADERSIZE, datalen);
//
//        // send the packet to the network layer
//        getNetworkLayer().sendPacket(pkt);
//        System.out.println("Sent one packet with header=" + pkt[0]);
//
//        // schedule a timer for 1000 ms into the future, just to show how that works:
//        framework.Utils.Timeout.SetTimeout(1000, this, 28);

        // and loop and sleep; you may use this loop to check for incoming acks...

        loop:
        while (!stop) {
            for (int i = 0; i < PIPESIZE; i++) {
                HashMap<Boolean, Integer[]> tmp = new HashMap<>();
                tmp.put(false, alldata.get(i + pointer));
                pipelines.put((i + pointer), tmp);
                pointer++;
            }

            for (int i = 0; i < PIPESIZE; i++) {
                Integer[] pkt = new Integer[HEADERSIZE + DATASIZE];
                getNetworkLayer().sendPacket(pkt);
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
}
