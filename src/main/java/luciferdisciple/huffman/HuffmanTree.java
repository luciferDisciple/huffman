package luciferdisciple.huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 *
 * @author Lucifer Disciple <piotr.momot420@gmail.com>
 */
public class HuffmanTree {

    final public Map<Byte, Integer> frequencies;
    final private Node root;

    public HuffmanTree(File inFile) throws IOException {
        this.frequencies = new HashMap<>();
        byte[] buffer = new byte[1024 * 16];

        try (FileInputStream inStream = new FileInputStream(inFile);) {

            for (int count = inStream.read(buffer); count > 0;
                    count = inStream.read(buffer)) {
                for (int i = 0; i < count; i++) {
                    byte key = buffer[i];
                    int value = frequencies.containsKey(key)
                            ? frequencies.get(key) : 0;
                    value++;
                    frequencies.put(key, value);
                }
            }
        } catch (IOException ex) {
            throw ex;
        }

        this.root = buildTree();
    }

    public HuffmanTree(Map<Byte, Integer> frequencies) {
        this.frequencies = frequencies;
        this.root = buildTree();
    }

    private Node buildTree() {
        PriorityQueue<Node> nodeQueue
                = new PriorityQueue<>(new NodeComparator());

        for (Map.Entry<Byte, Integer> frequency
                : frequencies.entrySet()) {
            Node leaf
                    = new Node(frequency.getKey(), frequency.getValue());
            nodeQueue.add(leaf);
        }

        while (nodeQueue.size() > 1) {
            Node n = new Node(nodeQueue.poll(), nodeQueue.poll());
            nodeQueue.add(n);
        }
        return nodeQueue.poll();
    }

    public HashMap<Byte, String> getPrefixCodes() {
        HashMap<Byte, String> result = new HashMap<>();
        this.root.getPrefixes(result, "");
        return result;
    }

    public int originalBytesCount() {
        return this.root.getFrequency();
    }

    public void
            decode(OutputStream dstStream, InputStream huffmanStream)
            throws IOException {
        Deque<Integer> bitQueue = new ArrayDeque<>();
        Node treePointer = this.root;
        for (int i = 0; i < originalBytesCount();) {
            if (bitQueue.isEmpty()) {
                enqueueBits(bitQueue, huffmanStream.read());
            }
            while (!bitQueue.isEmpty()) {
                if (bitQueue.poll() == 0) {
                    treePointer = treePointer.left;
                } else {
                    treePointer = treePointer.right;
                }

                if (treePointer.isLeaf) {
                    dstStream.write(treePointer.ch);
                    treePointer = this.root;
                    i++;
                    break;
                }
            }
        }
    }

    private void enqueueBits(Deque<Integer> bitQueue, int srcByte) {
        bitQueue.add((srcByte & 0x80) >> 7);
        bitQueue.add((srcByte & 0x40) >> 6);
        bitQueue.add((srcByte & 0x20) >> 5);
        bitQueue.add((srcByte & 0x10) >> 4);
        bitQueue.add((srcByte & 0x08) >> 3);
        bitQueue.add((srcByte & 0x04) >> 2);
        bitQueue.add((srcByte & 0x02) >> 1);
        bitQueue.add(srcByte & 0x01);
    }

    private class Node {

        final public boolean isLeaf;
        public byte ch;
        private int frequency;
        public Node left;
        public Node right;

        public Node(byte ch, int frequency) {
            this.isLeaf = true;
            this.ch = ch;
            this.frequency = frequency;
        }

        public Node(Node left, Node right) {
            this.isLeaf = false;
            this.left = left;
            this.right = right;
        }

        public int getFrequency() {
            if (this.isLeaf) {
                return frequency;
            }

            return left.getFrequency() + right.getFrequency();
        }

        public void getPrefixes(Map<Byte, String> dst, String prefix) {
            if (isLeaf) {
                dst.put(ch, prefix);
            } else {
                this.left.getPrefixes(dst, prefix + "0");
                this.right.getPrefixes(dst, prefix + "1");
            }
        }
    }

    private class NodeComparator implements Comparator<Node> {

        @Override
        public int compare(Node o1, Node o2) {
            return o1.getFrequency() - o2.getFrequency();
        }

    }
}
