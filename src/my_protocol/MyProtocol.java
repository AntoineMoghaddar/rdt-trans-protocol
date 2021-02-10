package my_protocol;

import framework.IRDTProtocol;
import framework.Utils;
import Utils.Logger;

import java.util.*;

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
    static final int PIPESIZE = 4;
    static int LAR = 0;

    @Override
    public void sender() {


        Integer[] fileContents = Utils.getFileContents(getFileID());
        HashMap<Integer, Integer> data = new HashMap<>();

        if (fileContents != null) {
            for (int i = 0; i < fileContents.length; i++) {
                data.put(i, fileContents[i]);
            }
        }

        ArrayList<Integer> sent = new ArrayList<>();
        ArrayList<Integer> acked = new ArrayList<>();

        //Send first 4
        Iterator it = data.entrySet().iterator();
        int index = 0;
        while (it.hasNext() && index < PIPESIZE) {
            Map.Entry pair = (Map.Entry) it.next();
            getNetworkLayer().sendPacket(new Integer[]{(Integer) pair.getKey(), (Integer) pair.getValue()});
            sent.add((Integer) pair.getKey());
            index++;
        }

        nxtloop:
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            getNetworkLayer().sendPacket(new Integer[]{(Integer) pair.getKey(), (Integer) pair.getValue()});
            sent.add((Integer) pair.getKey());
            index++;


            while (acked.size() != fileContents.length) {
                Integer[] acknowledgement = getNetworkLayer().receivePacket();

                if (acknowledgement != null) {
                    Logger.confirm(acknowledgement[0]);
                    // tell the user
                    System.out.println("Received acknowledgement for packet with header: " + acknowledgement[0]);
                    acked.add(acknowledgement[0]);
                    LAR = acknowledgement[0];
                } else {
                    // wait ~10ms (or however long the OS makes us wait) before trying again
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Logger.err("Interrupted; Cause: " + e.getMessage());
                    }
                }

                if (LAR >= sent.get(sent.size() - 1)) {
                    continue nxtloop;
                }
            }
        }

//
//        Integer[] fileContents = Utils.getFileContents(getFileID());
//        HashMap<Boolean, Integer[]> chunk = new HashMap<>();
//        acked = new ArrayList<>();
//
//        for (Integer[] datachunk : chunkArray(fileContents, 5)) {
//            chunk.put(false, datachunk);
//        }
//
//        Iterator it = chunk.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry) it.next();
//            getNetworkLayer().sendPacket((Integer[]) pair.getValue());
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//

        //TODO: Wat krijgen we terug, hoe zetten we dit op done
//        checkChunk()
        // write something random into the header byte

        // copy databytes from the input file into data part of the packet, i.e., after the header
//        System.arraycopy(fileContents, filePointer, pkt, HEADERSIZE, datalen);
//        getNetworkLayer().sendPacket(pkt);


//

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
//
        for (Integer[] i : output) {

            System.out.print("\n[");
            for (Integer j : i) {
                System.out.print(j + ",");
            }
            System.out.print("]");
        }
        return output;
    }
//
//    private boolean checkChunk(Integer[] chunk) {
//        boolean isAcked = false;
//        for (Integer i : chunk) {
//            for (Integer j : acked) {
//                if (j.equals(i)) {
//                    isAcked = true;
//                }
//            }
//        }
//        return isAcked;
//    }
}
