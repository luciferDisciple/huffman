/*
 * The MIT License
 *
 * Copyright 2021 Lucifer Disciple <piotr.momot420@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package luciferdisciple.huffman;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author Lucifer Disciple <piotr.momot420@gmail.com>
 */
public class Huffman {

    public static String usage =
      "Usage:  huffman (--compress|--decompress) output_file input_file\n"
    + "        huffman (--bit-stream|--prefix-codes) input_file\n"
    + "--bit-stream      Print the result of compressing the input file\n"
    + "                  as a sequence of ones and zeroes.\n"
    + "--prefix-codes    Print the mapping of bytes to binary prefix\n"
    + "                  codes.  The mapping is derived from the\n"
    + "                  huffman tree constructed from the bytes of\n"
    + "                  the input file.";

    /**
     * @param args command line arguments
     */
    public static void main(String[] args) {
        switch (args.length) {
            case 2:
                if ("--bit-stream".equals(args[0])) {
                    printBitStream(args[1]);
                } else if ("--prefix-codes".equals(args[0])) {
                    printPrefixEncoding(args[1]);
                } else {
                    System.err.println(usage);
                }
                break;
            case 3:
                if ("--compress".equals(args[0])) {
                    compress(args[1], args[2]);
                } else if ("--decompress".equals(args[0])) {
                    decompress(args[1], args[2]);
                } else {
                    System.err.println(usage);
                }
                break;
            default:
                System.err.println(usage);
                break;
        }

    }

    private static void printBitStream(String srcFilename) {
        File srcFile = new File(srcFilename);
        try (FileInputStream srcStream = new FileInputStream(srcFile);) {
            HuffmanTree huffmanTree = new HuffmanTree(srcFile);
            Map<Byte, String> prefixCodes = huffmanTree.getPrefixCodes();
            for (int srcByte = srcStream.read(); srcByte != -1;
                    srcByte = srcStream.read()) {
                System.out.print(
                        prefixCodes.get((byte) srcByte)
                );
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void printPrefixEncoding(String inputFilename) {
        try {
            File srcFile = new File(inputFilename);
            HuffmanTree huffmanTree = new HuffmanTree(srcFile);
            Map<Byte, String> prefixCodes = huffmanTree.getPrefixCodes();

            for (Map.Entry<Byte, String> code : prefixCodes.entrySet()) {
                String line;
                if (Character.isAlphabetic(code.getKey())) {
                    line = String.format(
                            "0x%02x '%c'  =>  %s",
                            code.getKey(),
                            code.getKey(),
                            code.getValue()
                    );
                } else if (code.getKey() == ' ') {
                    line = String.format(
                            "0x%02x SP   =>  %s",
                            code.getKey(), code.getValue()
                    );
                } else if (code.getKey() == 9) {
                    line = String.format(
                            "0x%02x TB   =>  %s",
                            code.getKey(), code.getValue()
                    );
                } else if (code.getKey() == 10) {
                    line = String.format(
                            "0x%02x LF   =>  %s",
                            code.getKey(), code.getValue()
                    );
                } else {
                    line = String.format(
                            "0x%02x       =>  %s",
                            code.getKey(), code.getValue()
                    );
                }
                System.err.println(line);
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void
            compress(String outputFilename, String inputFilename) {
        try (
                BufferedOutputStream outputStream
                = new BufferedOutputStream(
                        new FileOutputStream(outputFilename)
                );) {
            HuffmanWriter writer
                    = new HuffmanWriter(new File(inputFilename));
            writer.write(outputStream);
            System.err.println(
                    "Header size: " + getHeaderSize(writer)
            );
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void
            decompress(String outputFilename, String inputFilename) {
        try (
                BufferedOutputStream dstStream
                = new BufferedOutputStream(
                        new FileOutputStream(outputFilename)
                );) {
            HuffmanReader reader
                    = new HuffmanReader(new File(inputFilename));
            reader.readTo(dstStream);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static int
            getHeaderSize(HuffmanWriter huffmanWriter) {
        Map<Byte, String> prefixCodes
                = huffmanWriter.getPrefixCodes();
        return 4 + prefixCodes.size() * 5;
    }
}
