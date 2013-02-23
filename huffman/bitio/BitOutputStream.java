package bitio;

import java.io.*;

/**
 * A BitOutputStream is a buffered stream designed to write an arbitrary
 * (up to limit of 32) number of bits. The bits are first written to an
 * internal bit buffer (an int) and then copied into an internal byte
 * buffer if the bit buffer is full. The bits in the byte buffer are
 * written to the underlying stream whenever the byte buffer reaches its
 * capacity, the BitOutputStream is closed, or is explicitly flushed.
 * Since BitOutputStream is already buffered, there is no need for
 * a BufferedOutputStream.
 *
 * It should be noticed that BitOutputStream is not thread-safe. None of
 * the methods are synchronized.
 *
 * @author Ulf Uttersrud, Oslo and Akershus University College
 * @version 2.1  01.11.2011
 */

public class BitOutputStream extends OutputStream
{
  private static final int DEFAULT_BUFFER_SIZE = 4096;

  /**
   * The internal byte array where the bytes are temporarily stored.
   */
  private byte buf[];

  /**
   * The size of the internal byte array.
   */
  private int bufSize;

  /**
   * The current position in the byte buffer. That is the position in
   * the byte array buf where the next byte will be written. The value
   * is always in the range 0 through buf.length and elements buf[0]
   * through buf[pos-1] contain valid bytes. The byte array is full,
   * and hence has to be emptied, if pos >= buf.length.
   */
  private int pos;

  /**
   * An int used as an internal bit buffer.
   */
  private int bits;

  /**
   * The number of valid (the rightmost) bits in the bit buffer.
   */
  private int bitSize;

  /**
   * The underlying output stream.
   */
  private OutputStream out;

  /**
   * Creates a BitOutputStream with the specified size as the length of
   * the internal byte array and saves its other argument, the output
   * stream, for later use.
   *
   * @param out
   *          the underlying output stream.
   * @param size
   *          the length of the byte buffer.
   * @exception IllegalArgumentException
   *              if size <= 0.
   * @exception NullPointerException
   *              if the output stream is not defined.
   */
  public BitOutputStream(OutputStream out, int size)
  {
    if (out == null) throw
      new NullPointerException("The stream out is null");

    if (size <= 0) throw
      new IllegalArgumentException("The size(" + size + ") <= 0");

    buf = new byte[bufSize = size];
    this.out = out;
  }

  /**
   * Creates a BitOutputStream and saves its argument, the output stream,
   * for later use. A default value DEFAULT_BUFFER_SIZE is used as
   * a buffer length.
   *
   * @param out
   *          the underlying output stream.
   * @exception NullPointerException
   *              if the the output stream is not defined.
   */
  public BitOutputStream(OutputStream out)
  {
    this(out, DEFAULT_BUFFER_SIZE);
  }

  /**
   * Creates a BitOutputStream with the specified file name and with
   * size as the length of the internal byte array.
   *
   * @param fileName
   *          the name of the file.
   * @exception FileNotFoundException
   *              if the file does not exist.
   * @exception IllegalArgumentException
   *              if size <= 0.
   */
  public BitOutputStream(String fileName, int size)
      throws FileNotFoundException
  {
    this(new FileOutputStream(fileName), size);
  }

  /**
   * Creates a BitOutputStream with the specified file name. A default
   * value DEFAULT_BUFFER_SIZE is used as the length of the internal
   * byte array.
   *
   * @param fileName
   *          the name of the file.
   * @exception FileNotFoundException
   *              if the file does not exist.
   */
  public BitOutputStream(String fileName) throws FileNotFoundException
  {
    this(new FileOutputStream(fileName), DEFAULT_BUFFER_SIZE);
  }

  /**
   * A factory method that creates and returns a BitOutputStream with
   * the specified file name and a default value as a buffer length.
   *
   * @param fileName
   *          the name of the file.
   * @exception FileNotFoundException
   *              if the file can not be created.
   */
  public static BitOutputStream toFile(String fileName)
      throws FileNotFoundException
  {
    return new BitOutputStream(new FileOutputStream(fileName));
  }

  /**
   * A factory method that creates and returns a BitOutputStream with
   * the specified filename. If the second argument append is true, then
   * bits will be written to the end of the file rather than the beginning.
   * A default value is used as a buffer length.
   *
   * @param fileName
   *          the name of the file.
   * @param append
   *          if true, then bits will be written to the end of the file rather
   *          than the beginning.
   * @exception FileNotFoundException
   *              if the file cannot be created or opened.
   */
  public static BitOutputStream toFile(String fileName, boolean append)
      throws FileNotFoundException
  {
    return new BitOutputStream(new FileOutputStream(fileName, append));
  }

  /** Flushes the internal byte buffer */
  private void flushBuffer() throws IOException
  {
    if (out == null)
      throw new IOException("The stream is closed!");

    if (pos > 0)
    {
      out.write(buf, 0, pos);
      pos = 0;
    }
  } // end flushBuffer

  /**
   * Writes one bit (i.e. the rightmost bit of the argument bit) to
   * the output stream.
   *
   * @param bit
   *          containing the bit
   * @exception IOException
   *              if an I/O error occurs.
   */
  public void writeBit(int bit) throws IOException
  {
    bits <<= 1;         // a bit can now be added
    bits |= (bit & 1);  // the last bit of the parameter bit is added
    bitSize++;          // bitSize is updated

    if (bitSize >= 8)   // a byte can be moved to the byte buffer
    {
      bitSize = 0;

      // the byte buffer is flushed if it is full
      if (pos >= bufSize) flushBuffer();

      buf[pos++] = (byte) bits;  // a byte is moved
    }
  } // end writeBit

  /**
   * Writes a 0-bit to the output stream.
   *
   * @exception IOException
   *              if an I/O error occurs.
   */
  public void write0Bit() throws IOException
  {
    bits <<= 1;        // adds a 0-bit
    bitSize++;         // bitSize is updated

    if (bitSize >= 8)  // a byte can be moved to the byte buffer
    {
      bitSize = 0;

      // the byte buffer is flushed if it is full
      if (pos >= bufSize) flushBuffer();

      buf[pos++] = (byte) bits;  // a byte is moved
    }
  } // end write0Bit

  /**
   * Writes a 1-bit to the output stream.
   *
   * @exception IOException
   *              if an I/O error occurs.
   */
  public void write1Bit() throws IOException
  {
    bits <<= 1;   // a bit can now be added
    bits |= 1;    // adds a 1-bit
    bitSize++;    // bitSize is updated

    if (bitSize >= 8)  // a byte can be moved to the byte buffer
    {
      bitSize = 0;

      // the byte buffer is flushed if it is full
      if (pos >= bufSize) flushBuffer();

      buf[pos++] = (byte) bits;  // a byte is moved
    }
  } // end write1Bit

  /**
   * Writes 8 bits (a byte) (i.e. the rightmost 8 bits of the argument b) to the
   * output stream.
   *
   * @param b
   *          containing the byte
   * @exception IOException
   *              if an I/O error occurs.
   */
  public void write(int b) throws IOException
  {
    bits <<= 8;          // 8 bits can now be added
    bits |= (b & 0xff);  // adds the 8 rightmost bits of b

    // the byte buffer is flushed if it is full
    if (pos >= bufSize) flushBuffer();

    buf[pos++] = (byte) (bits >> bitSize);  // a byte is moved

  } // end write

  /**
   * Writes the requested (rightmost) numberOfBits from the argument value to
   * the output stream. An IllegalArgumentException is thrown if the requested
   * number of bits is is not in the range [0,32]. No bits are written if
   * numberOfBits is equal to 0.
   *
   * @param value
   *          containing the bits
   * @param numberOfBits
   *          the (rightmost) number of bits from value to be written
   * @exception IOException
   *              if an I/O error occurs.
   * @exception IllegalArgumentException
   *              if numberOfBits is not in the range [0,32]
   */
  public void writeBits(int value, int numberOfBits) throws IOException
  {
    if (numberOfBits < 0)
      throw new IllegalArgumentException("Cannot write " + numberOfBits
          + " bits!");

    if (numberOfBits <= 25)   // the most common case
    {
      bits <<= numberOfBits;  // will not create overflow

      bits |= (value & ((1 << numberOfBits) - 1));  // the bits are added
      bitSize += numberOfBits;  // the bitsize is updated

      while (bitSize >= 8)
      {
        bitSize -= 8;

        // the byte buffer is flushed if it is full
        if (pos >= bufSize) flushBuffer();

        buf[pos++] = (byte) (bits >> bitSize);  // a byte is moved
      }
    }
    else if (numberOfBits <= 32)
    {
      int k = numberOfBits - 25;          // 1 <= k <= 7
      bits <<= 25;                        // 25 bits can now be added
      bits |= (value >> k) & 0x1ffffff;   // 25 bits are added
      bitSize += 25;                      // bitSize is updated

      // the bit buffer contains at least 25 bits,
      // 24 of them are moved to the byte buffer

      bitSize -= 8;
      if (pos >= bufSize) flushBuffer();
      buf[pos++] = (byte) (bits >> bitSize);

      bitSize -= 8;
      if (pos >= bufSize) flushBuffer();
      buf[pos++] = (byte) (bits >> bitSize);

      bitSize -= 8;
      if (pos >= bufSize) flushBuffer();
      buf[pos++] = (byte) (bits >> bitSize);

      bits <<= k;                         // k bits can now be added
      bits |= (value & ((1 << k) - 1));   // the rightmost k bits of value
      bitSize += k;                       // bitSize is updated

      if (bitSize >= 8)                   // 2 <= bitSize <= 15
      {
        bitSize -= 8;
        if (pos >= bufSize) flushBuffer();
        buf[pos++] = (byte) (bits >> bitSize);  // a byte is moved
      }
    }
    else
      throw new IllegalArgumentException("Cannot write " + numberOfBits
          + " bits!");

  } // end writeBits

  /**
   * Writes the significant binary digits (bits) of <code>value</code>
   * to the output stream. All 32 digits are written if <code>value</code>
   * is negative. If value is equal to 0, then a 0-digit is written. This
   * coincides with the "digits/bits" returned by the method
   * <code>Integer.toBinaryString(value)</code>.
   *
   * @param value
   *          containing the bits
   * @exception IOException
   *              if an I/O error occurs.
   */
  public void writeBits(int value) throws IOException
  {
    // must find the number of significant bits of value

    int signifcantBits = 31, v = value;

    if (v >>> 16 == 0) { signifcantBits -= 16; v <<= 16; }
    if (v >>> 24 == 0) { signifcantBits -=  8; v <<=  8; }
    if (v >>> 28 == 0) { signifcantBits -=  4; v <<=  4; }
    if (v >>> 30 == 0) { signifcantBits -=  2; v <<=  2; }

    signifcantBits += v >>> 31;

    bitSize += signifcantBits;

    if (bitSize <= 32)
    {
      bits <<= signifcantBits;  // will not create overflow
      bits |= value;            // the signifcantBits are added

      while (bitSize >= 8)
      {
        bitSize -= 8;
        if (pos >= bufSize) flushBuffer();
        buf[pos++] = (byte) (bits >> bitSize);
      }
    }
    else
    {
      int k = bitSize - 32;
      bits <<= signifcantBits - k;
      bits |= value >>> k;

      if (pos >= bufSize) flushBuffer();
      buf[pos++] = (byte) (bits >> 24);

      if (pos >= bufSize) flushBuffer();
      buf[pos++] = (byte) (bits >> 16);

      if (pos >= bufSize) flushBuffer();
      buf[pos++] = (byte) (bits >> 8);

      if (pos >= bufSize) flushBuffer();
      buf[pos++] = (byte) bits;

      bits = value;
      bitSize = k;
    }

  } // end writeBits


  /**
   * Flushes this buffered output stream. This forces any buffered output
   * bits to be written to the underlying output stream. If the number of
   * buffered output bits is not a multiple of 8, then a sufficient number
   * of 0-bits are added.
   *
   * @exception IOException
   *              if an I/O error occurs.
   */
  public void flush() throws IOException
  {
    if (bitSize > 0)
    {
      // the byte buffer is written to the ouput stream if it is full
      if (pos >= bufSize) flushBuffer();

      // 0-bits are added to create a full byte
      buf[pos++] = (byte) (bits <<= (8 - bitSize));
      bitSize = 0;
    }

    flushBuffer();
    out.flush();

  } // end flush()

  /**
   * Returns the number of bits missing in order to fill the last byte.
   * If the method is called previous to a close (or a flush), the return
   * value tells how many 0-bits are shifted into the last byte.
   *
   * @return the number of bits missing in order to fill the last byte.
   */
  public int missingBits() throws IOException
  {
    if (out == null)
      throw new IOException("The stream is closed!");
    return bitSize == 0 ? 0 : 8 - bitSize;
  }

  /**
   * Ensures that the close method of this bit output stream is called when
   * there are no more references to it.
   *
   * @exception IOException
   *              if an I/O error occurs.
   */
  protected void finalize() throws IOException
  {
    if (out != null)
      close();
  }

  /**
   * Closes the underlying output stream and releases any system resources
   * associated with the stream. The internal buffers are emptied (the method
   * flush() is called) before the underlying stream is closed. Any method call
   * subsequent to a close will result in an IOException with the message "The
   * stream is closed!".
   *
   * @exception IOException
   *              if an I/O error occurs.
   */
  public void close() throws IOException
  {
    // a second call to close will have no effect
    if (out == null) return;
    flush();
    out.close();

    // The variables out and buf are set to null, pos and bufSize to -1
    // and bitSize to 8. This will prevent any method call subsequent to
    // a close. Such an effort will result in an IOException with
    // the message "The stream is closed!".
    out = null;
    buf = null;
    pos = bufSize = -1;
    bitSize = 8;

  } // end close()

  /**
   * Converts a byte array to a string of '0'- and '1'-characters.
   * An example: the byte array {(byte)-1,(byte)15} is converted
   * to the string "11111111 00001111". Suppose a ByteArrayOutputStream
   * is used as the underlying stream. Then we can easily observe
   * how the write methods behave.
   *
   * @param b the byte array
   * @return the byte array turned into a string of
   *         characters ('0' or '1')
   */
  public static String toBitString(byte[] b)
  {
    StringBuilder s = new StringBuilder();

    String[] fourBits =
    {"0000","0001","0010","0011","0100","0101","0110","0111",
     "1000","1001","1010","1011","1100","1101","1110","1111"};

    for (int c : b)
    {
      s.append(fourBits[(c & 255) >> 4]);
      s.append(fourBits[c & 15]);
      s.append(' ');
    }

    return s.toString();
  }

}