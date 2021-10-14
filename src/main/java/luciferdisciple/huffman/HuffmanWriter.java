package luciferdisciple.huffman;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Lucifer Disciple <piotr.momot420@gmail.com>
 */
public class HuffmanWriter {

    final private File inFile;
    final private HuffmanTree huffmanTree;

    public HuffmanWriter(File inFile)
            throws FileNotFoundException, IOException {
        this.inFile = inFile;
        this.huffmanTree = new HuffmanTree(inFile);
    }

    public Map<Byte, String> getPrefixCodes() {
        return this.huffmanTree.getPrefixCodes();
    }

    public void write(OutputStream dst) throws IOException {
        DataOutputStream dataStream = new DataOutputStream(dst);
        writeHeader(dataStream);
        writeEncodedFileContent(dataStream);
    }

    private void writeHeader(DataOutputStream dst) throws IOException {
        Map<Byte, Integer> freqMap = huffmanTree.frequencies;
        Set<Entry<Byte, Integer>> freqSet = freqMap.entrySet();
        dst.writeInt(freqSet.size());
        for (Entry<Byte, Integer> byteFrequency : freqSet) {
            dst.writeByte(byteFrequency.getKey());
            dst.writeInt(byteFrequency.getValue());
        }
    }

    private void writeEncodedFileContent(DataOutputStream outStream)
            throws FileNotFoundException, IOException {
        FileInputStream inStream = new FileInputStream(inFile);

        byte[] buffer = new byte[1024 * 16];
        StringBuilder binaryBuffer = new StringBuilder();
        Map<Byte, String> codes = huffmanTree.getPrefixCodes();

        try {
            for (int count = inStream.read(buffer); count > 0;
                    count = inStream.read(buffer)) {
                for (int i = 0; i < count; i++) {
                    binaryBuffer.append(codes.get(buffer[i]));
                    if (binaryBuffer.length() >= 8) {
                        int outByte = Integer.parseInt(
                                binaryBuffer.substring(0, 8),
                                2
                        );
                        outStream.writeByte(outByte);
                        binaryBuffer.delete(0, 8);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        } finally {
            inStream.close();
        }

        // add byte padding
        if (binaryBuffer.length() != 0) {
            while (binaryBuffer.length() < 8) {
                binaryBuffer.append("0");
            }
            int outByte = Integer.parseInt(
                    binaryBuffer.toString(),
                    2
            );
            outStream.writeByte(outByte);
        }
    }
}
