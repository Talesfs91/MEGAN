/*
 * ReadAssembler.java Copyright (C) 2021. Daniel H. Huson
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
 *
 */

package megan.assembly;

import jloda.graph.*;
import jloda.util.*;
import megan.assembly.align.SimpleAligner4DNA;
import megan.core.Director;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * assembler for all reads assigned to a particular class
 * <p>
 * Daniel Huson, 5.2015
 */
public class ReadAssembler {
    private Graph overlapGraph;
    private NodeArray<String> node2ReadNameMap;
    private ReadData[] readId2ReadData;
    private Node[][] paths;
    private String label;
    private ArrayList<Pair<String, String>> contigs;
    private List<Integer>[] readId2ContainedReads;
    private final boolean verbose;

    /**
     * constructor
     */
    public ReadAssembler(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * build the overlap graph
     *
     * @param minOverlap
     * @param readData
     * @param progress
     * @throws IOException
     * @throws CanceledException
     */
    public void computeOverlapGraph(String label, int minOverlap, List<ReadData> readData, ProgressListener progress) throws IOException, CanceledException {
        this.label = label;
        final OverlapGraphBuilder overlapGraphBuilder = new OverlapGraphBuilder(minOverlap, verbose);
        overlapGraphBuilder.apply(readData, progress);
        overlapGraph = overlapGraphBuilder.getOverlapGraph();

        {
            if (verbose)
                System.err.print("Checking for cycles: ");
            final int edgesRemoved = DirectedCycleBreaker.apply(overlapGraph);
            if (verbose) {
                System.err.println(edgesRemoved + (edgesRemoved > 0 ? " removed" : ""));
            }
        }

        readId2ReadData = overlapGraphBuilder.getReadId2ReadData();
        node2ReadNameMap = overlapGraphBuilder.getNode2ReadNameMap();
        readId2ContainedReads = overlapGraphBuilder.getReadId2ContainedReads();

        final PathExtractor pathExtractor = new PathExtractor(overlapGraph, readId2ContainedReads);
        pathExtractor.apply(progress);
    }

    /**
     * show the overlap graph
     *
     * @param dir
     * @param progress
     * @throws CanceledException
     */
    public void showOverlapGraph(Director dir, ProgressListener progress) throws CanceledException {
        final OverlapGraphViewer overlapGraphViewer = new OverlapGraphViewer(dir, overlapGraph, node2ReadNameMap, paths);
        overlapGraphViewer.apply(progress);
    }


    /**
     * write the overlap graph
     *
     * @param w
     * @return
     * @throws IOException
     * @throws CanceledException
     */
    public Pair<Integer, Integer> writeOverlapGraph(Writer w) throws IOException, CanceledException {
        final NodeArray<String> names = new NodeArray<>(overlapGraph);
        final NodeArray<String> sequences = new NodeArray<>(overlapGraph);
        for (Node v = overlapGraph.getFirstNode(); v != null; v = v.getNext()) {
            ReadData readData = readId2ReadData[(Integer) v.getInfo()];
            sequences.put(v, readData.getSegment());
            names.put(v, readData.getName());
        }
        final Map<String, NodeArray<?>> label2nodes = new TreeMap<>();
        label2nodes.put("label", names);
        label2nodes.put("sequence", sequences);

        final EdgeArray<Integer> overlap = new EdgeArray<>(overlapGraph);
        for (Edge e = overlapGraph.getFirstEdge(); e != null; e = e.getNext()) {
            overlap.put(e, (Integer) e.getInfo());
        }
        final Map<String, EdgeArray<Integer>> label2edges = new TreeMap<>();
        label2edges.put("label", null);
        label2edges.put("overlap", overlap);

        // todo: fix
        System.err.println("NOT IMPLEMENTED");
        //GML.writeGML(overlapGraph,w, "Overlap graph generated by MEGAN6", true, label, 1, label2nodes, label2edges);

        return new Pair<>(this.overlapGraph.getNumberOfNodes(), this.overlapGraph.getNumberOfEdges());
    }

    /**
     * assemble all reads provided by the iterator using perfect overlaps of the given minimum length
     *
     * @param minReads
     * @param minCoverage
     * @param minLength
     * @param progress
     * @return number of contigs and singletons
     */
    public int computeContigs(int minReads, double minCoverage, int minLength, ProgressListener progress) throws IOException, CanceledException {
        final PathExtractor pathExtractor = new PathExtractor(overlapGraph, readId2ContainedReads);
        pathExtractor.apply(progress);
        paths = pathExtractor.getPaths();

        final ContigBuilder contigBuilder = new ContigBuilder(pathExtractor.getPaths(), readId2ContainedReads);
        contigBuilder.apply(readId2ReadData, minReads, minCoverage, minLength, progress);
        contigs = contigBuilder.getContigs();
        return contigBuilder.getCountContigs();
    }

    /**
     * write contigs
     *
     * @param w
     * @param progress
     * @throws CanceledException
     * @throws IOException
     */
    public void writeContigs(Writer w, ProgressListener progress) throws CanceledException, IOException {
        progress.setSubtask("Writing contigs");
        progress.setMaximum(contigs.size());
        progress.setProgress(0);
        for (Pair<String, String> pair : contigs) {
            w.write(pair.getFirst().trim());
            w.write("\n");
            w.write(pair.getSecond().trim());
            w.write("\n");
            progress.incrementProgress();
        }
        w.flush();
        if (progress instanceof ProgressPercentage)
            ((ProgressPercentage) progress).reportTaskCompleted();
    }

    /**
     * report contigs stats
     */
    public void reportContigStats() {
        if (contigs.size() == 0) {
            System.err.printf("Contigs:%,9d%n", 0);
        } else {
            final int[] sizes = new int[contigs.size()];
            int pos = 0;
            for (Pair<String, String> pair : contigs) {
                sizes[pos++] = pair.getSecond().length();
            }
            Arrays.sort(sizes);
            System.err.printf("Contigs:%,9d%n", sizes.length);
            System.err.printf("Min len:%,9d%n", sizes[0]);
            System.err.printf("Med len:%,9d%n", sizes[sizes.length / 2]);
            System.err.printf("Max len:%,9d%n", sizes[sizes.length - 1]);
        }
    }

    public ArrayList<Pair<String, String>> getContigs() {
        return contigs;
    }

    /**
     * computes all pairwise overlaps between contigs and then merges contigs
     *
     * @param maxNumberOfThreads
     * @param progress
     * @param minPercentIdentityToMergeContigs
     * @param minOverlap
     * @param contigs                          input list of contigs and output list of merged contigs
     * @return number of resulting
     * @throws CanceledException
     */
    public static int mergeOverlappingContigs(int maxNumberOfThreads, final ProgressListener progress, final float minPercentIdentityToMergeContigs, final int minOverlap, final ArrayList<Pair<String, String>> contigs, final boolean verbose) throws CanceledException {
        progress.setSubtask("Overlapping contigs");

        final ArrayList<Pair<String, String>> sortedContigs = new ArrayList<>(contigs.size());
        sortedContigs.addAll(contigs);
        sortedContigs.sort(Basic.getComparatorDecreasingLengthOfSecond());
        contigs.clear();

        final Graph overlapGraph = new Graph();
        final List<Integer>[] contigId2ContainedContigs = new List[sortedContigs.size()];
        final BitSet containedContigs = new BitSet();

        // main parallel computation:
        if (sortedContigs.size() > 0) {
            final int numberOfThreads = (Math.min(sortedContigs.size(), Math.min(Runtime.getRuntime().availableProcessors() - 1, maxNumberOfThreads)));
            final Single<Boolean> notCanceled = new Single<>(true);

            progress.setMaximum(sortedContigs.size() / numberOfThreads);
            progress.setProgress(0);

            final Map<Integer, Node> contig2Node = new HashMap<>();

            for (int i = 0; i < sortedContigs.size(); i++) {
                final Node v = overlapGraph.newNode(i);
                contig2Node.put(i, v);
            }

            final ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
            final CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
            try {
                for (int t = 0; t < numberOfThreads; t++) {
                    final int threadNumber = t;
                    service.submit(() -> {
                                try {
                                    final SimpleAligner4DNA simpleAlignerDNA = new SimpleAligner4DNA();
                                    simpleAlignerDNA.setMinPercentIdentity(minPercentIdentityToMergeContigs);
                                    final Single<Integer> overlap = new Single<>(0);

                                    for (int i = threadNumber; i < sortedContigs.size(); i += numberOfThreads) {
                                        final String iContig = sortedContigs.get(i).getSecond();
                                        final byte[] iBytes = iContig.getBytes();
                                        for (int j = 0; j < i; j++) {
                                            final byte[] jBytes = sortedContigs.get(j).getSecond().getBytes();

                                            if (iBytes.length > jBytes.length)
                                                throw new RuntimeException("Internal error: contig i is longer than contig j");

                                            final SimpleAligner4DNA.OverlapType overlapType = simpleAlignerDNA.getOverlap(iBytes, jBytes, overlap);

                                            // if contained or nearly contained, remove
                                            if (overlapType == SimpleAligner4DNA.OverlapType.QueryContainedInRef) {
                                                synchronized (contigId2ContainedContigs) {
                                                    List<Integer> contained = contigId2ContainedContigs[j];
                                                    if (contained == null) {
                                                        contained = new ArrayList<>();
                                                        contigId2ContainedContigs[j] = contained;
                                                    }
                                                    contained.add(i);
                                                    containedContigs.set(i);
                                                }
                                            } else if (overlapType == SimpleAligner4DNA.OverlapType.QuerySuffix2RefPrefix && overlap.get() >= minOverlap) {
                                                final Node v = contig2Node.get(i);
                                                final Node w = contig2Node.get(j);
                                                synchronized (overlapGraph) {
                                                    overlapGraph.newEdge(v, w, overlap.get());
                                                }
                                            } else if (overlapType == SimpleAligner4DNA.OverlapType.QueryPrefix2RefSuffix && overlap.get() >= minOverlap) {
                                                final Node v = contig2Node.get(i);
                                                final Node w = contig2Node.get(j);
                                                synchronized (overlapGraph) {
                                                    overlapGraph.newEdge(w, v, overlap.get());
                                                }
                                            }
                                        }
                                    }
                                    if (threadNumber == 0)
                                        progress.incrementProgress();
                                } catch (CanceledException e) {
                                    notCanceled.set(false);
                                    while (countDownLatch.getCount() > 0)
                                        countDownLatch.countDown();
                                } catch (Exception e) {
                                    Basic.caught(e);
                                } finally {
                                    countDownLatch.countDown();
                                }
                            }
                    );
                }
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    Basic.caught(e);
                }
            } finally {
                service.shutdownNow();
            }
        }

        if (verbose)
            System.err.printf("Contained contigs:%6d%n", containedContigs.cardinality());
        if (containedContigs.cardinality() > 0) // delete all contained contigs from graph
        {
            for (Node v : overlapGraph.nodes()) {
                if (containedContigs.get((Integer) v.getInfo())) {
                    overlapGraph.deleteNode(v);
                }
            }
        }

        {
            if (verbose)
                System.err.print("Checking for cycles: ");
            final int edgesRemoved = DirectedCycleBreaker.apply(overlapGraph);
            if (verbose) {
                System.err.println(edgesRemoved + (edgesRemoved > 0 ? " removed" : ""));
            }
        }

        if (verbose) {
            System.err.println(String.format("Contig graph nodes:%5d", overlapGraph.getNumberOfNodes()));
            System.err.println(String.format("Contig graph edges:%5d", overlapGraph.getNumberOfEdges()));
        }

        final PathExtractor pathExtractor = new PathExtractor(overlapGraph, contigId2ContainedContigs);
        pathExtractor.apply(progress);

        final Node[][] paths = pathExtractor.getPaths();
        final BitSet used = new BitSet();

        int countMergedContigs = 0;

        for (Node[] path : paths) {
            if (path.length == 1) {
                final Node current = path[0];
                if (!containedContigs.get((Integer) current.getInfo())) {
                    final int contigId = (Integer) current.getInfo(); // info is contig-Id (times -1, if reverse strand)
                    contigs.add(sortedContigs.get(contigId));
                }
            } else if (path.length > 1) {
                boolean verboseMerging = false;
                if (verboseMerging)
                    System.err.println("Merging " + path.length + " contigs...");

                final StringBuilder headerBuffer = new StringBuilder();
                final StringBuilder sequenceBuffer = new StringBuilder();

                Node prev = path[0];
                int contigId = Math.abs((Integer) prev.getInfo());

                if (used.get(contigId))
                    continue;
                else
                    used.set(contigId);

                Pair<String, String> prevContig = sortedContigs.get(contigId);

                headerBuffer.append("[").append(Basic.skipFirstWord(prevContig.getFirst()));
                sequenceBuffer.append(prevContig.getSecond());

                int length = prevContig.getSecond().length();

                for (int i = 1; i < path.length; i++) { // start at 1
                    final Node current = path[i];
                    contigId = (Integer) current.getInfo();
                    used.set(contigId);

                    final int overlap = (Integer) overlapGraph.getCommonEdge(prev, current).getInfo();

                    final Pair<String, String> currentContig = sortedContigs.get(contigId);
                    headerBuffer.append(" + (-").append(overlap).append(") + ").append(Basic.skipFirstWord(currentContig.getFirst()));

                    sequenceBuffer.append(currentContig.getSecond().substring(overlap));

                    length += currentContig.getSecond().length() - overlap;
                    prev = current;
                }
                headerBuffer.append("]");

                if (verboseMerging) {
                    System.err.println("Input contigs:");
                    for (int i = 0; i < path.length; i++) {
                        Node p = path[i];
                        System.err.println(sortedContigs.get((Integer) p.getInfo()));
                        if (i < path.length - 1) {
                            System.err.println("Overlap to next: " + overlapGraph.getCommonEdge(path[i], path[i + 1]).getInfo());
                        }
                    }
                }

                final Pair<String, String> pair = new Pair<>("length=" + length + " " + headerBuffer.toString(), sequenceBuffer.toString());
                contigs.add(pair);

                if (verboseMerging) {
                    System.err.println("Output contig:");
                    System.err.println(pair);
                }

                countMergedContigs++;
            }
        }

        for (Node current : pathExtractor.getSingletons()) {
            if (!containedContigs.get((Integer) current.getInfo())) {
                final int contigId = (Integer) current.getInfo(); // info is contig-Id (times -1, if reverse strand)
                contigs.add(sortedContigs.get(contigId));
            }
        }

        if (verbose)
            System.err.println(String.format("Merged contigs:   %6d", countMergedContigs));

        // sort and renumber contigs:
        contigs.sort(Basic.getComparatorDecreasingLengthOfSecond());
        int contigNumber = 1;
        for (Pair<String, String> contig : contigs) {
            contig.setFirst(String.format(">Contig-%06d %s", contigNumber++, (contig.getFirst().startsWith(">") ? Basic.skipFirstWord(contig.getFirst()) : contig.getFirst())));
        }
        return contigs.size();
    }
}
