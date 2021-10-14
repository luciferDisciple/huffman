package luciferdisciple.huffman;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Lucifer Disciple <piotr.momot420@gmail.com>
 */
public class HuffmanReader {

    final static private int HEADER_ENTRY_SIZE = 5;

    final private File huffmanFile;
    final private HuffmanTree huffmanTree;

    public HuffmanReader(File huffmanFile)
            throws FileNotFoundException, IOException {
        this.huffmanFile = huffmanFile;
        Map<Byte, Integer> symbolsFrequencies = readHeader();
        this.huffmanTree = new HuffmanTree(symbolsFrequencies);
    }

    private Map<Byte, Integer> readHeader() throws IOException {
        Map<Byte, Integer> symbolsFrequencies = new HashMap<>();
        try (
                DataInputStream dataStream
                = new DataInputStream(
                        new FileInputStream(this.huffmanFile)
                );) {
            int symbolCount = dataStream.readInt();
            for (int i = 0; i < symbolCount; i++) {
                byte symbol = dataStream.readByte();
                int frequency = dataStream.readInt();
                symbolsFrequencies.put(symbol, frequency);
            }
        } catch (IOException ex) {
            throw ex;
        }
        return symbolsFrequencies;
    }

    public void readTo(OutputStream dstStream)
            throws IOException {
        try (
                DataInputStream huffmanStream
                = new DataInputStream(
                        new FileInputStream(this.huffmanFile)
                );) {
            int symbolCount = huffmanStream.readInt();
            huffmanStream.skip(symbolCount * HEADER_ENTRY_SIZE);
            huffmanTree.decode(dstStream, huffmanStream);
        } catch (IOException ex) {
            throw ex;
        }
    }
}
