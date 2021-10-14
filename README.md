# Huffman

A Java CLI program for compressing files using Huffman encoding, written as an
assignment for a university course back in May 2019.

## Usage

```
$ java -jar target/Huffman-0.0.1-jar-with-dependencies.jar
Usage:  huffman (--compress|--decompress) output_file input_file
        huffman (--bit-stream|--prefix-codes) input_file
--bit-stream      Print the result of compressing the input file
                  as a sequence of ones and zeroes.
--prefix-codes    Print the mapping of bytes to binary prefix
                  codes.  The mapping is derived from the
                  huffman tree constructed from the bytes of
                  the input file.
```
