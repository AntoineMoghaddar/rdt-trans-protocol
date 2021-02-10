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

    @Override
    public void sender() {
        Logger.confirm("Sending...");

        ArrayList<Integer[]> alldata = new ArrayList<>();
        // read from the input file
        Integer[] fileContents = Utils.getFileContents(getFileID());
        HashMap<Integer, HashMap<Boolean, Integer[]>> pipelines = new HashMap<>();
        boolean stop = false;
        int pointer = 0;

        for (int i = 0; i < fileContents.length; i += DATASIZE) {
            alldata.add(Arrays.copyOfRange(fileContents, i, i + DATASIZE));
        }

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
    }

    @Override
    public Integer[] receiver() {
        return null;
    }
}
