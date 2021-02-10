package my_protocol;

import Utils.Logger;
import framework.IRDTProtocol;
import framework.Utils;

import java.util.Arrays;

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
    static final int HEADERSIZE = 1;   // number of header bytes in each packet
    static final int PIPESIZE = 3;
    static int id = 0, quit;

    private Integer[][] sentItems;

    private void slidingWindow(Integer[] items) {
        Integer[][] packets = new Integer[items.length][];
        sentItems = new Integer[PIPESIZE][];

        //setup the correct packets for each individual item.
        for (int i = 0; i < items.length; i++) {
            packets[i] = setupPacket(id, items[i]);
            id++;
        }

        //Transmit in chunks of PIPESIZE length
        for (int i = 0; i < packets.length; i += PIPESIZE) {
            for (int j = 0; j < PIPESIZE - 1; j++) {
                if (packets[i + j] != null) {
                    transmit(packets[i + j]);
                    sentItems[j] = packets[i + j];
                } else {
                    Logger.err("Empty packet, end of line?");
                    quit = -1;
                }
            }

            //Check packets if one or many has failed, if so, retransmit
            int[] notAck = checkIncoming(sentItems);
            for (int k : notAck) {
                if (k != -1) {
                    switch (k) {
                        case 0 -> transmit(packets[i]);
                        case 1 -> transmit(packets[i + 1]);
                        case 2 -> transmit(packets[i + 2]);
                        default -> Logger.err("Out of bound, you cannot perform this action");
                    }
                }
            }
        }
    }

    private void transmit(Integer[] item) {
        getNetworkLayer().sendPacket(item);
    }

    private Integer[] setupPacket(int id, int item) {
        return new Integer[]{id, item};
    }

    // Wat er verwacht wordt: [0, -1, 2]
    // -1 = transmission succesvol
    // index = niet succesvol
    private int[] checkIncoming(Integer[][] sentItems) {
        int[] received = new int[PIPESIZE];

        //Checken of er een ACK binnen komt en welke
        for (int i = 0; i < PIPESIZE; i++) {
            Integer[] ack = getNetworkLayer().receivePacket();
            if (ack != null) {
                int newack = ack[0];
                for (int j = i; j < PIPESIZE; j++) {
                    if (newack == sentItems[j][0]) {
                        received[j] = -1;
                    }
                }
            } else {
                // wait ~10ms (or however long the OS makes us wait) before trying again
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    quit = -1;
                }
            }
        }
        for (int i = 0; i < PIPESIZE; i++) {
            if (received[i] == 0) {
                received[i] = i;
            }
        }
        return received;
    }


    private Integer[] checkIncomingtwo(Integer[] packet) {
        Integer[] fileContents = new Integer[0];

        for (int i = 0; i < PIPESIZE; i++) {
            if (packet != null) {

                // tell the user
                System.out.println("Received packet, length=" + packet.length + "  first byte=" + packet[0] + " Second byte= " + packet[1]);

                // append the packet's data part (excluding the header) to the fileContents array, first making it larger
                int oldlength = fileContents.length;
                int datalen = packet.length - HEADERSIZE;
                fileContents = Arrays.copyOf(fileContents, oldlength + datalen);
                System.arraycopy(packet, HEADERSIZE, fileContents, oldlength, datalen);

            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    quit = -1;
                    Logger.err(e.getMessage());
                }
            }
        }
        return fileContents;
    }


    @Override
    public void sender() {
        quit = 0;

        Integer[] fileContents = Utils.getFileContents(getFileID());
        if (fileContents != null) {
            while (quit != -1) {
                slidingWindow(fileContents);
            }
            Logger.err("quiting");
        }
    }

    @Override
    public Integer[] receiver() {
        System.out.println("Receiving...");
        Integer[] packet = getNetworkLayer().receivePacket();;
        while (packet == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            packet = getNetworkLayer().receivePacket();
        }
        return checkIncomingtwo(packet);
    }

    @Override
    public void TimeoutElapsed(Object tag) {

    }
}
