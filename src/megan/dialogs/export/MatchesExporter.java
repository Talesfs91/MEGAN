/*
 *  Copyright (C) 2019 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package megan.dialogs.export;

import jloda.util.BlastMode;
import jloda.util.CanceledException;
import jloda.util.ProgressListener;
import megan.data.IConnector;
import megan.data.IMatchBlock;
import megan.data.IReadBlock;
import megan.data.IReadBlockIterator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

/**
 * export all  matches to a file (or those associated with the set of selected taxa, if any selected)
 * Daniel Huson, 6.2010
 */
public class MatchesExporter {
    /**
     * export all matches in file
     *
     * @param connector
     * @param fileName
     * @param progressListener
     * @throws IOException
     */
    public static long exportAll(BlastMode blastMode, IConnector connector, String fileName, ProgressListener progressListener) throws IOException {
        progressListener.setTasks("Export", "Writing all matches");

        long countMatches = 0;
        try {
            try (BufferedWriter w = new BufferedWriter(new FileWriter(fileName));
                 IReadBlockIterator it = connector.getAllReadsIterator(0, 10000, true, true)) {
                w.write(blastMode.toString().toUpperCase() + " file generated by MEGAN6\n\n");
                progressListener.setMaximum(it.getMaximumProgress());
                progressListener.setProgress(0);
                while (it.hasNext()) {
                    countMatches += writeMatches(it.next(), w);
                    progressListener.setProgress(it.getProgress());
                }
            }
        } catch (CanceledException ex) {
            System.err.println("USER CANCELED");
        }
        return countMatches;
    }

    /**
     * export all matches for given set of classids in the given classification
     *
     * @param classification
     * @param classIds
     * @param connector
     * @param fileName
     * @param progressListener
     * @throws IOException
     */
    public static long export(String classification, Collection<Integer> classIds, BlastMode blastMode, IConnector connector, String fileName, ProgressListener progressListener) throws IOException {
        long countMatches = 0;
        try {
            progressListener.setTasks("Export", "Writing selected matches");

            try (BufferedWriter w = new BufferedWriter(new FileWriter(fileName))) {
                w.write(blastMode.toString().toUpperCase() + " file generated by MEGAN6\n\n");

                int maxProgress = 100000 * classIds.size();
                int currentProgress = 0;
                progressListener.setMaximum(maxProgress);
                progressListener.setProgress(currentProgress);
                int count = 0;
                for (Integer classId : classIds) {
                    count++;
                    currentProgress = 100000 * count;
                    try (IReadBlockIterator it = connector.getReadsIterator(classification, classId, 0, 10000, true, true)) {
                        long progressIncrement = 100000 / (it.getMaximumProgress() + 1);
                        while (it.hasNext()) {
                            countMatches += writeMatches(it.next(), w);
                            progressListener.setProgress(currentProgress);
                            currentProgress += progressIncrement;
                        }
                    }
                }
            }
        } catch (CanceledException ex) {
            System.err.println("USER CANCELED");
        }
        return countMatches;
    }

    /**
     * write all matches associated with the given read
     *
     * @param readBlock
     * @param w
     * @return number of matches written
     * @throws IOException
     */
    private static int writeMatches(IReadBlock readBlock, Writer w) throws IOException {
        int countMatches = 0;
        String readHeader = readBlock.getReadHeader();
        if (readHeader.startsWith(">"))
            readHeader = readHeader.substring(1);
        w.write("\nQuery=" + readHeader + "\n");

        final String readSequence = readBlock.getReadSequence();
        if (readSequence != null)
            w.write("\t(" + readSequence.length() + " letters)\n");
        w.write("\n");

        /*
        FastA fastA = new FastA(readBlock.getValueString(getReadFormat().getHeadItem()),
                readBlock.getValueString(getReadFormat().getSequenceItem()));
        fastA.write(w);
        */

        if (readBlock.getNumberOfAvailableMatchBlocks() == 0)
            w.write(" ***** No hits found ******\n");
        else {
            for (IMatchBlock matchBlock : readBlock.getMatchBlocks()) {
                w.write(matchBlock.getText() + "\n");
                countMatches++;
            }
        }
        return countMatches;
    }
}
