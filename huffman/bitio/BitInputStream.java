package bitio;

import java.io.*;

/**
 * A BitInputStream is a buffered stream from where an arbitrary number
 * of bits can be read. (Since -1 is used as «end of stream», only
 * a maximum of 31 bits can be read in one read-operation.) It contains
 * some other input stream, which supplies an internal byte buffer with
 * data. Bytes are transferred from the byte buffer to a small bit buffer
 * (an int) from which the bits are read. Since BitInputStream is already
 * buffered, there is no need for a BufferedInputStream.
 *
 * In addition to be read, bits can be peeked and skipped. A peek and a read
 * behave in the same way except that a peek does not remove the bits from the
 * stream. An arbitrary number of bits can be skipped in one skip-operation.
 *
 * It is possible, subsequent to a read, to unread (push back) some or all of
 * the bits just read. It is always the most recently read bits that will be
 * unread (except for those bits already unread by another unread) to the front
 * of the stream. If for example the bits 0110010101011 were read
 * in a read-operation, then an unread of four bits will push 1011
 * back to the front of the stream. More than one unread is possible if the
 * "sum" does not exceed the number of bits read by the most recent read. Hence
 * another unread of for instance four bits in the example above, will be
 * allowed. The result is a push back of 1010. If more than 25
 * bits are read in one read-operation, then at least 25 of them can be unread,
 * but not necessarily all. The method unreadSize() returns the
 * number of bits that can be unread. An unread is illegal after a peek or a
 * skip.
 *
 * Normally it is possible to insert bits to the front of the stream. The method
 * insertSize() tells how many bits that can be inserted.
 * Subsequent to a read, it is always possible to insert as many bits as were
 * read (or a maximum of 25). After an insert the number of bits that can be
 * unread is reduced by the same amount. Let for instance the stream contain the
 * bits 0110010101011. If ten bits are read, that is the bits
 * 0110010101, then the stream will, after an insertion of the
 * five bits 11111, look like 11111011. A
 * subsequent unread of five bits, that is 10101, will turn the
 * stream into 1010111111011.
 *
 * It should be noticed that BitInputStream is not thread-safe.
 * None of the methods are synchronized.
 *
 * @author Ulf Uttersrud, Oslo and Akershus University College
 * @version 2.1   01.11.2011
 */

public class BitInputStream extends InputStream
{
  private static final int DEFAULT_BUFFER_SIZE = 4096;

  /**
   * The internal byte array where the bytes are stored.
   */
  private byte[] buf;

  /**
   * The number of valid bytes in the byte array buf. This is a number
   * in the range 0 through buf.length. Hence the valid part of the byte
   * array is buf[0] through buf[bufSize-1]. The byte array is filled
   * (and refilled) by reading bytes from the underlying input stream.
   */
  private int bufSize;

  /**
   * The current position in the byte buffer. This is the index of the next byte
   * to be fetched from the byte array buf. The index is always
   * in the range 0 through bufSize. An
   * int-variable bits is used as a bit buffer and the byte
   * buffer buf supplies this bit buffer with bits, one byte at a
   * time. If pos is less than bufSize, then
   * buf[pos] is the next byte to be fetched; if it is equal to
   * bufSize, the input stream must supply the byte buffer with
   * more bytes.
   */
  private int pos;

  /**
   * An int-variable used as an internal bit buffer.
   */
  private int bits;

  /**
   * The number of valid bits in the bit buffer. The value is always in the
   * range 0 through 32. It is the
   * bitSize rightmost bits of the variable bits
   * that are valid.
   */
  private int bitSize;

  /**
   * The maximal number of bits to unread. It is reset for each read, is reduced
   * after an unread and is set to 0 after a skip, a peek or a failed read.
   */
  private int unreadSize;

  /**
   * The underlying input stream.
   */
  private InputStream in;

  /**
   * Fills the byte buffer with data from the underlying stream. The number of
   * bytes may be less than the capacity of the byte buffer if there are too few
   * bytes left in the underlying stream. bufSize is set to the
   * number of bytes read or to -1 if the end of the stream will
   * be or has been reached.
   *
   * @return true at least one byte is read from the underlying
   *         stream and false else.
   * @exception IOException
   *              if the stream is closed.
   */
  private boolean fillByteBuffer() throws IOException
  {
    if (in == null)
      throw new IOException("The stream is closed!");

    if ((bufSize = in.read(buf, 0, buf.length)) == -1)
    {
      pos = -1;
      unreadSize = 0;
      return false;
    }
    pos = 0;
    return true;
  }

  /**
   * Creates a BitInputStream with the specified size as the
   * length of the internal byte array and saves its other argument, the input
   * stream, for later use.
   *
   * @param in
   *          the underlying input stream.
   * @param size
   *          the length of the internal byte array.
   * @exception IllegalArgumentException
   *              if size <= 0.
   * @exception NullPointerException
   *              if the input stream is not defined.
   */
  public BitInputStream(InputStream in, int size)
  {
    if (in == null)
      throw new NullPointerException("The stream in is null");

    if (size <= 0)
      throw new IllegalArgumentException("A buffer size of " + size
          + " is illegal!");

    buf = new byte[size];
    this.in = in;
  }

  /**
   * Creates a BitInputStream and saves its argument, the input
   * stream, for later use. A default value is used as the length of the
   * internal byte array.
   *
   * @param in
   *          the underlying input stream.
   * @exception NullPointerException
   *              if the the input stream is not defined.
   */
  public BitInputStream(InputStream in)
  {
    this(in, DEFAULT_BUFFER_SIZE);
  }

  /**
   * Creates a BitInputStream from a file, with the filename as
   * argument and with the specified size as the length of the internal array.
   *
   * @param fileName
   *          the name of the file.
   * @param size
   *          the length of the internal byte buffer.
   * @exception FileNotFoundException
   *              if the file does not exist.
   * @exception IllegalArgumentException
   *              if size <= 0.
   */
  public BitInputStream(String fileName, int size) throws FileNotFoundException
  {
    this(new FileInputStream(fileName), size);
  }

  /**
   * Creates a BitInputStream from a file, with the filename as
   * argument. A default value is used as the length of the internal byte array.
   *
   * @param fileName
   *          the name of the file.
   * @exception FileNotFoundException
   *              if the file does not exist.
   */
  public BitInputStream(String fileName) throws FileNotFoundException
  {
    this(new FileInputStream(fileName));
  }

  /**
   * A factory method that creates a BitInputStream from a file,
   * with the filename as argument. A default value is used as the length of the
   * internal byte array.
   *
   * @param fileName
   *          the name of the file.
   * @exception FileNotFoundException
   *              if the file does not exist.
   */
  public static BitInputStream fromFile(String fileName)
      throws FileNotFoundException
  {
    return new BitInputStream(new FileInputStream(fileName));
  }

  /**
   * A factory method that creates a BitInputStream from a byte
   * array. Uses the length of the byte array as buffer length, or a default
   * value as buffer length if the length of the byte array is too large.
   *
   * @param b
   *          the byte array.
   * @exception NullPointerException
   *              if the byte array does not exist.
   */
  public static BitInputStream fromByteArray(byte[] b)
  {
    if (b == null)
      throw new NullPointerException("The byte array b is null!");
    int size = DEFAULT_BUFFER_SIZE;
    if (b.length < size)
      size = b.length;
    return new BitInputStream(new ByteArrayInputStream(b), size);
  }

  /**
   * Returns the number of bits that can be read from this input stream. That is
   * the sum of the number of bits left in the bit buffer, the bytes left in the
   * byte buffer times 8 and the result of calling the available
   * method of the underlying input stream times 8. The method is relatively
   * costly and should not be used for loop control.
   *
   * @return the number of available bits in the stream.
   * @exception IOException
   *              if an I/O error occurs.
   */
  public int available() throws IOException
  {
    if (in == null)
      throw new IOException("The stream is closed!");

    return bitSize + 8 * (bufSize - pos + in.available());
  }

  /**
   * Reads the next 8 bits (a byte) from the stream. The bits are returned as
   * the 8 rightmost bits of an int; the rest (the leftmost) are
   * 0-bits. The value -1 is returned if there are less than 8
   * bits left in the stream. If a -1 is returned, then the
   * method available() will give the exact number (0 or more) of
   * bits left in the stream.
   *
   * @return the next 8 bits, or -1 if there are
   *         less than 8 bits left in the stream.
   * @exception IOException
   *              if an I/O error occurs.
   */
  public int read() throws IOException
  {
    unreadSize = 8; // the maximal number of bits to be unread

    if (bitSize < 8) // the bit buffer needs more bits
    {
      if (pos >= bufSize) // does the byte array need a refill?
      {
        if (fillByteBuffer() == false)
          return -1;
      }

      bits <<= 8; // makes room for 8 new bits
      bits |= (buf[pos++] & 0xff); // a byte (8 bits) is added

      return (bits >> bitSize) & 0xff; // 8 bits are returned

    } // end of if (bitSize < 8)

    bitSize -= 8;
    return (bits >> bitSize) & 0xff; // 8 bits are returned
  }

  /**
   * Returns the next bit in the stream. That is 0 if the next
   * bit is a 0-bit and 1 if the next bit is a
   * 1-bit. The value -1 is returned if there are
   * no more bits in the stream (the end of the stream will be or has been
   * reached).
   *
   * @return 0 or 1 depending on whether the next
   *         bit is a 0-bit or a 1-bit, and
   *         -1 if there are no more bits in the stream.
   *
   * @exception IOException
   *              if an I/O error occurs.
   */
  public int readBit() throws IOException
  {
    unreadSize = 1;

    if (bitSize <= 0) // the bit buffer is empty
    {
      if (pos >= bufSize) // does the byte array need a refill?
      {
        if (fillByteBuffer() == false)
          return -1;
      }
      bits = (buf[pos++] & 0xff); // 8 bits are added
      bitSize = 8; // bitSize is updated

    } // if (bitSize <= 0)

    bitSize--;
    return (bits >> bitSize) & 1;
  }

  /**
   * Reads the next numberOfBits bits (31 as a maximum) from the
   * stream. The bits are returned as the rightmost bits of an int; the rest
   * (the leftmost) are 0-bits. The value -1 is returned if there
   * are less than the requested number of bits in the stream. If a
   * -1 is returned, then the method available()
   * will give the exact number (0 or more) of bits left in the stream.
   * 

   * An IllegalArgumentException is thrown if the requested number of bits is
   * outside the interval [0,31].
   *
   * @param numberOfBits
   *          the requested number of bits to be read.
   * @return the requested bits, or -1 if there are less than the
   *         requested number of bits left in the stream.
   * @exception IOException
   *              if an I/O error occurs.
   * @exception IllegalArgumentException
   *              if numberOfBits is outside the interval [0,31]
   */
  public int readBits(int numberOfBits) throws IOException
  {
    if (numberOfBits < 0)
      throw new IllegalArgumentException("Cannot read " + numberOfBits
          + " bits!");

    // It might happen that numberOfBits is greater than
    // 31 and hence illegal. This is taken care of further down.

    // If the bit buffer (0 - 32 bits) does not contain enough bits,
    // a sufficient amount of bytes must be added to the buffer.
    // If numberOfBits does not exceed 25, than overflow
    // will not occur. However, if numberOfBits is
    // greater than 25, care must be taken. Suppose that the bit
    // buffer has just one bit left and that numberOfBits
    // is equal to 26. Then 3 extra bytes (1 + 24 bits) will not
    // be sufficient and 4 extra bytes (1 + 32 bits) will give
    // an overflow. A possible way to handle an overflow is to
    // make a copy of the bit buffer before new bytes are added.

    // The following code is quite long and contains segments of
    // identical code. But by keeping the code "in line", we get
    // somewhat better performance. The intension is also to have
    // as few operations as possible for each combination of
    // of values (bitSize and numberOfBits).

    // / 1. numberOfBits <= 25, the most common case /////////

    if (numberOfBits <= 25)
    {
      while (bitSize < numberOfBits) // will not create overflow
      {
        if (pos >= bufSize) // does the byte array need a refill?
        {
          if (fillByteBuffer() == false)
            return -1;
        }

        bits <<= 8; // makes room for 8 new bits
        bits |= (buf[pos++] & 0xff); // a byte (8 bits) is added
        bitSize += 8; // bitSize is updated

      } // end while

      bitSize -= numberOfBits; // bitSize is updated
      unreadSize = numberOfBits; // unreadSize is updated

      return (bits >> bitSize) & ~(-1 << numberOfBits);
    }

    // / 2. numberOfBits > 25 /////////////////////

    while (bitSize < 25) // will not create overflow
    {
      if (pos >= bufSize) // does the byte array need a refill?
      {
        if (fillByteBuffer() == false)
          return -1;
      }

      bits <<= 8; // makes room for 8 new bits
      bits |= (buf[pos++] & 0xff); // a byte (8 bits) is added
      bitSize += 8; // bitSize is updated
    } // end while

    // / 3. numberOfBits < bitSize /////////////////////

    if (numberOfBits < bitSize) // enough bits in the buffer!
    {
      bitSize -= numberOfBits; // bitSize is updated
      unreadSize = numberOfBits; // unreadSize is updated

      return (bits >> bitSize) & ~(-1 << numberOfBits);
    }

    // / 4. numberOfBits == bitSize /////////////////////

    if (numberOfBits == bitSize)
    {
      // To continue we need to be sure that numberOfBits is not
      // out of range, i.e. not equal to 32 or greater.

      if (numberOfBits > 31)
        throw new IllegalArgumentException("Cannot read " + numberOfBits
            + " bits!");

      bitSize = 0; // bitSize is updated
      unreadSize = numberOfBits; // unreadSize is updated

      return bits & ~(-1 << numberOfBits);
    }

    // / 5. numberOfBits > bitSize /////////////////////

    // The number of available bits in the bit buffer is
    // now greater than or equal to 25, but is less than
    // the requested number of bits. Hence an overflow
    // will occur if an extra byte is added. But more bits
    // are needed to satisfy the request. As a first step,
    // we make a copy of the bit buffer.

    int copy = bits & ~(-1 << bitSize); // a bit buffer copy

    if (pos >= bufSize) // does the byte array need a refill?
    {
      if (fillByteBuffer() == false)
        return -1;
    }

    // One byte must be added to the bit buffer. The overflow
    // leads to a loss of exactly bitSize - 24 bits.
    // But the lost bits are kept in the copy.

    bits <<= 8; // makes room for 8 new bits
    bits |= (buf[pos++] & 0xff); // a byte (8 bits) is added

    int diff = numberOfBits - bitSize;

    // at this point diff has to be in the interval [1,6]

    bitSize = 8 - diff; // bitSize is updated
    unreadSize = diff + 24; // unreadSize is updated

    // the bits from the copy and the necessary bits from the bit
    // buffer are combined in order to return a total amount of
    // numberOfBits bits. The bits that disappeared
    // from the bit buffer as a result of the overflow, cannot
    // be unread in a subsequent unread.

    return (copy << diff) | ((bits >> bitSize) & ~(-1 << diff));
  }

  /**
   * Reads the next 8 bits (a byte) from the stream without removing them. The
   * bits are returned as the 8 rightmost bits of an int; the
   * rest (the leftmost) are 0-bits. The value -1 is returned if
   * there are less than 8 bits left in the stream. If a -1 is
   * returned, then the method available() will give the exact
   * number (0 or more) of bits left in the stream.
   *
   * @return the next 8 bits, or -1 if there are
   *         less than 8 bits left in the stream.
   * @exception IOException
   *              if an I/O error occurs.
   */

  public int peek() throws IOException
  {
    unreadSize = 0;

    if (bitSize < 8) // the bit buffer needs more bits
    {
      if (pos >= bufSize) // does the byte array need a refill?
      {
        if (fillByteBuffer() == false)
          return -1;
      }

      bits <<= 8; // makes room for 8 new bits
      bits |= (buf[pos++] & 0xff); // a byte (8 bits) is added
      bitSize += 8; // bitSize is updated

    } // end of if (bitSize < 8)

    return (bits >> (bitSize - 8)) & 0xff; // 8 bits are returned
  }

  /**
   * Reads the next bit in the stream without removing it. The method returns 0
   * if the next bit is a 0-bit and 1 if the next bit is a 1-bit. The value -1
   * is returned if there are no more bits (the end of the stream will be or has
   * been reached).
   *
   * @return 0 or 1 depending on whether the next bit is a 0-bit or a 1-bit, and
   *         -1 if there are no more bits in the stream.
   * @exception IOException
   *              if an I/O error occurs.
   */
  public int peekBit() throws IOException
  {
    unreadSize = 0;

    if (bitSize <= 0) // the bit buffer needs more bits
    {
      if (pos >= bufSize) // does the byte array need a refill?
      {
        if (fillByteBuffer() == false)
          return -1;
      }
      bits = (buf[pos++] & 0xff); // 8 bits are added
      bitSize = 8; // bitSize is updated

    } // if (bitSize <= 0)

    return (bits >> (bitSize - 1)) & 1;
  }

  /**
   * Reads the next numberOfBits bits (31 as a maximum) from the
   * stream without removing them. The bits are returned as the rightmost bits
   * of an int; the rest (the leftmost) are 0-bits. The value -1
   * is returned if there are less than the requested number of bits in the
   * stream. If a -1 is returned, then the method
   * available() will give the exact number (0 or more) of bits
   * left in the stream.
   * 


   * An IllegalArgumentException is thrown if the requested number of bits is
   * outside the interval [0,31].
   *
   * @param numberOfBits
   *          the requested number of bits to be peeked.
   * @return the requested number of bits, or -1 if there are less than the
   *         requested number of bits left in the stream.
   * @exception IOException
   *              if an I/O error occurs.
   * @exception IllegalArgumentException
   *              if numberOfBits is outside the interval [0,31]
   */
  public int peekBits(int numberOfBits) throws IOException
  {
    if (numberOfBits < 0)
      throw new IllegalArgumentException("Cannot peek " + numberOfBits
          + " bits!");

    unreadSize = 0;

    // If the bit buffer (0 - 32 bits) does not contain enough bits,
    // a sufficient amount of bytes must be added to the buffer.
    // If numberOfBits does not exceed 25, than overflow
    // will not occur. However, if numberOfBits is
    // greater than 25, care must be taken. Suppose that the bit
    // buffer has just one bit left and that numberOfBits
    // is equal to 26. Then 3 extra bytes (1 + 24 bits) will not
    // be sufficient and 4 extra bytes (1 + 32 bits) will give
    // an overflow. If bitSize is 25 and
    // numberOfBits is 26, we pick instead the missing bit
    // from the byte buffer.

    // The following code is quite long and contains segments of
    // identical code. But by keeping the code "in line", we get
    // somewhat better performance. The intension is also to have
    // as few operations as possible for each combination of
    // of values (bitSize and numberOfBits).

    // / 1. numberOfBits <= 25, the most common case /////////

    if (numberOfBits <= 25)
    {
      while (bitSize < numberOfBits) // prevents overflow
      {
        if (pos >= bufSize) // does the byte array need a refill?
        {
          if (fillByteBuffer() == false)
            return -1;
        }

        bits <<= 8; // makes room for 8 new bits
        bits |= (buf[pos++] & 0xff); // a byte (8 bits) is added
        bitSize += 8; // bitSize is updated

      } // end while

      return (bits >> (bitSize - numberOfBits)) & ((1 << numberOfBits) - 1);
    }

    // / 2. numberOfBits > 25 /////////////////////

    while (bitSize < 25) // prevents overflow
    {
      if (pos >= bufSize) // does the byte array need a refill?
      {
        if (fillByteBuffer() == false)
          return -1;
      }

      bits <<= 8; // makes room for 8 new bits
      bits |= (buf[pos++] & 0xff); // a byte (8 bits) is added
      bitSize += 8; // bitSize is updated
    } // end while

    // / 3. numberOfBits < bitSize /////////////////////

    if (numberOfBits < bitSize) // enough bits in the buffer!
      return (bits >> (bitSize - numberOfBits)) & ((1 << numberOfBits) - 1);

    // / 4. numberOfBits == bitSize /////////////////////

    if (numberOfBits == bitSize)
    {
      // To continue we need to be sure that numberOfBits is not
      // out of range, i.e. not equal to 32 or greater.

      if (numberOfBits > 31)
        throw new IllegalArgumentException("Cannot peek " + numberOfBits
            + " bits!");

      return bits & ((1 << numberOfBits) - 1);
    }

    // / 5. numberOfBits > bitSize /////////////////////

    // The missing number of bits (i.e. numberOfBits - bitSize)
    // must be picked from the byte buffer.

    if (pos >= bufSize) // does the byte array need a refill?
    {
      if (fillByteBuffer() == false)
        return -1;
    }

    int diff = numberOfBits - bitSize; // the missing number of bits

    return ((bits << diff) | ((buf[pos] & 0xff) >> (8 - diff)))
        & ((1 << numberOfBits) - 1);
  }

  /**
   * Skips over and discards the next byte (8 bits) in the input stream. A skip
   * may (if end of stream will be reached) end up skipping over fewer than 8
   * bits. However, the method will always return the actual number of bits
   * skipped (0 if end of stream has been reached).
   *
   * @return the actual number of bits skipped.
   * @exception IOException
   *              if an I/O error occurs.
   */
  public int skip() throws IOException
  {
    unreadSize = 0; // no unread subsequent to a skip

    if (bitSize < 8) // more bits to the bit buffer
    {
      if (pos >= bufSize) // does the byte array need a refill?
      {
        if (fillByteBuffer() == false)
        {
          int skipSize = bitSize;
          bitSize = 0;
          return skipSize; // equal to the number of bits skipped
        }
      }

      bits <<= 8; // make room for 8 bits
      bits |= (buf[pos++] & 0xff); // a byte (8 bits) is added

      return 8; // 8 bits are skipped

    } // end of if (bitSize < 8)

    bitSize -= 8;
    return 8; // 8 bits are skipped
  }

  /**
   * This method is inherited from the superclass InputStream,
   * but is overridden since it will not work properly
   * in these circumstances.
   */

  public long skip(long n) throws IOException
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Skips over and discard the next bit in the input stream. Returns either 1
   * or 0 (0 if end of stream will be or has been reached).
   *
   * @return the number of bits skipped (1 or 0).
   * @exception IOException
   *              if an I/O error occurs.
   */
  public int skipBit() throws IOException
  {
    unreadSize = 0; // // no unread subsequent to a skip

    if (bitSize <= 0) // the bit buffer is empty
    {
      if (pos >= bufSize) // does the byte array need a refill?
      {
        if (fillByteBuffer() == false)
          return 0;
      }

      bits = buf[pos++]; // a byte (8 bits) is added
      bitSize = 8; // bitSize is updated

    } // end if

    bitSize--;
    return 1; // one bit is skipped
  }

  /**
   * Skips over and discards a requested number of bits in the input stream. A
   * skip may (if end of stream will be reached) end up skipping over fewer than
   * the requested number. However, the method will always return the actual
   * number of bits skipped (0 if end of stream has been reached). If
   * numberOfBits is negative, no bits are skipped.
   *
   * @param numberOfBits
   *          the number of bits to be skipped.
   * @return the actual number of bits skipped.
   * @exception IOException
   *              if an I/O error occurs.
   */
  public int skipBits(int numberOfBits) throws IOException
  {
    if (numberOfBits <= 0)
      return 0; // no skipping

    unreadSize = 0; // no unread subsequent to a skip

    if (numberOfBits <= bitSize) // skip within current bit buffer?
    {
      bitSize -= numberOfBits;
      return numberOfBits;
    }

    int diff = numberOfBits - bitSize;

    if (diff <= (bufSize - pos) << 3) // skip within current byte buffer?
    {
      pos += diff >> 3;

      if ((diff & 7) != 0)
      {
        bits = buf[pos++]; // a byte (8 bits) is added
        bitSize = 8 - (diff & 7); // the new bitSize
      }
      else
        bitSize = 0;

      return numberOfBits;
    }

    int skipSize = bitSize + ((bufSize - pos) << 3);

    while (skipSize < numberOfBits)
    {
      if (fillByteBuffer() == false)
      {
        bitSize = 0; // end of stream
        return skipSize;
      }

      skipSize += bufSize << 3;
    }

    diff = numberOfBits - skipSize + (bufSize << 3);

    pos += diff >> 3;

    if ((diff & 7) != 0)
    {
      bits = buf[pos++]; // a byte (8 bits) is added
      bitSize = 8 - (diff & 7); // the new bitSize
    }
    else
      bitSize = 0;

    return numberOfBits;
  }

  /**
   * Unreads (pushes back) the most recently read bit (except for the bit or
   * bits already unread by some other unread) to the front of the stream. Two
   * or more subsequent calls to an unread are allowed if their "sum" does not
   * exceed the number of bits read by the most recent read. The method
   * unreadSize tells at each time how many more bits that can be
   * unread. No bits can be unread after a peek or a skip. An
   * IllegalStateException is thrown if there are no more bits to unread.
   *
   * @exception IllegalStateException
   *              if there are no more bits to unread.
   * @exception IOException
   *              if an I/O error occurs.
   */
  public void unreadBit() throws IOException
  {
    if (in == null)
      throw new IOException("The stream is closed!");

    if (unreadSize <= 0)
      throw new IllegalStateException("No bits to unread!");

    unreadSize--;
    bitSize++;
  }

  /**
   * Unreads (pushes back) as many as possible of the most recently read bits
   * (except for the bits already unread by some other unread) to the front of
   * the stream. Nothing happens if there are no bits to unread.
   *
   * @exception IOException
   *              if an I/O error occurs.
   */
  public void unreadBits() throws IOException
  {
    if (in == null)
      throw new IOException("The stream is closed!");

    bitSize += unreadSize;
    unreadSize = 0;
  }

  /**
   * Unreads (pushes back) the requested number of the most recently read bits.
   * After a read all or some (the most recently) of the read bits can be unread
   * (pushed back) to the front of the stream. If less than or equal to 25 bits
   * were read, than all of them can be unread. If more than 25 bits were read,
   * than at least 25 of them, may be more, can be unread. Two or more
   * subsequent calls to an unread are allowed if their "sum" does not exceed
   * the number of bits read by the most recent read. An IllegalStateException
   * is thrown if there are fewer then the requested number of bits that can be
   * unread.
   * 


   * The method unreadSize() tells at each time how many more
   * bits that can be unread. No bits can be unread after a peek or a skip.
   *
   * @param numberOfBits
   *          the requested number of bits to be unread.
   * @exception IllegalArgumentException
   *              if there are fewer than the requested number of bits to be
   *              unread.
   * @exception IOException
   *              if an I/O error occurs.
   */
  public void unreadBits(int numberOfBits) throws IOException
  {
    if (in == null)
      throw new IOException("The stream is closed!");

    if (numberOfBits < 0 || numberOfBits > unreadSize)
      throw new IllegalArgumentException("Illegal number of bits!");

    unreadSize -= numberOfBits;
    bitSize += numberOfBits;
  }

  /**
   * Returns the maximal number of bits that can be unread to the stream at that
   * moment. This number is reset to the number of bits read for each call to
   * readBit(), readBits() and read(). However, if more than 25 bits were read,
   * it might happen that this maximal number is set reset to 25.
   *
   * @return at each time the maximal number of bits that can be unread to the
   *         stream
   * @exception IOException
   *              if an I/O error occurs.
   */
  public int unreadSize() throws IOException
  {
    if (in == null)
      throw new IOException("The stream is closed!");

    return unreadSize;
  }

  /**
   * Inserts the last bit of bit into the front of the stream. An
   * IllegalStateException is thrown if no more bits can be inserted. The method
   * insertSize() reports the number of bits that can be inserted. If a bit is
   * inserted, the number of bits that can be unread is reduced by one.
   *
   * @param bit
   *          containing the bit
   * @exception IllegalStateException
   *              if no more bits can be inserted.
   * @exception IOException
   *              if an I/O error occurs.
   */
  public void insertBit(int bit) throws IOException
  {
    if (in == null)
      throw new IOException("The stream is closed!");

    if (bitSize == 32)
      throw new IllegalStateException("No bits can be inserted!");

    bits = ((bits & (-1 << bitSize)) << 1) | ((bit & 1) << bitSize)
        | (bits & ((1 << bitSize) - 1));

    if (unreadSize > 0)
      unreadSize--;
    bitSize++;
  }

  /**
   * Inserts a 0-bit into the front of the stream. An IllegalStateException is
   * thrown if no more bits can be inserted. The method insertSize() reports the
   * number of bits that can be inserted. If a bit is inserted, the number of
   * bits that can be unread is reduced by one.
   *
   * @exception IllegalStateException
   *              if no more bits can be inserted.
   * @exception IOException
   *              if an I/O error occurs.
   */
  public void insert0Bit() throws IOException
  {
    if (in == null)
      throw new IOException("The stream is closed!");

    if (bitSize == 32)
      throw new IllegalStateException("No bits can be inserted!");

    bits = ((bits & (-1 << bitSize)) << 1) | (bits & ((1 << bitSize) - 1));

    if (unreadSize > 0)
      unreadSize--;
    bitSize++;
  }

  /**
   * Inserts a 1-bit into the front of the stream. An IllegalStateException is
   * thrown if no more bits can be inserted. The method insertSize() reports the
   * number of bits that can be inserted. If a bit is inserted, the number of
   * bits that can be unread is reduced by one.
   *
   * @exception IllegalStateException
   *              if no more bits can be inserted.
   * @exception IOException
   *              if an I/O error occurs.
   */
  public void insert1Bit() throws IOException
  {
    if (in == null)
      throw new IOException("The stream is closed!");

    if (bitSize == 32)
      throw new IllegalStateException("No bits can be inserted!");

    bits = ((bits & (-1 << bitSize)) << 1) | (1 << bitSize)
        | (bits & ((1 << bitSize) - 1));

    if (unreadSize > 0)
      unreadSize--;
    bitSize++;
  }

  /**
   * Inserts the rightmost numberOfBits of value
   * into the front of the stream. An IllegalArgumentException is thrown if
   * numberOfBits is negative or to large. An insert subsequent
   * to a read can insert as many bits as were read. However if more then 25
   * bits were read, the maximum insert size might be only 25. The method
   * insertSize() reports the number of bits that can be
   * inserted. If bits are inserted, then the number of bits that can be unread
   * is reduced by the same amount.
   *
   * @param numberOfBits
   *          the requested number of bits to be inserted.
   * @param value
   *          the argument from which the bits are taken
   * @exception IllegalArgumentException
   *              if the requested number of bits is negative or to large.
   * @exception IOException
   *              if an I/O error occurs.
   */
  public void insertBits(int value, int numberOfBits) throws IOException
  {
    if (in == null)
      throw new IOException("The stream is closed!");

    if (numberOfBits < 0 || numberOfBits > insertSize())
      throw new IllegalArgumentException("numberOfBits too large!");

    if (numberOfBits == 32) // bitSize = 0
    {
      bits = value;
    }
    else
    {
      bits = (bits & ((1 << bitSize) - 1))
          | ((bits & (~((1 << bitSize) - 1))) << numberOfBits)
          | ((value & ((1 << numberOfBits) - 1)) << bitSize);
    }
    unreadSize -= numberOfBits;

    if (unreadSize < 0)
      unreadSize = 0;
    bitSize += numberOfBits;
  }

  /**
   * Returns the maximal number of bits that can be inserted into the stream at
   * that moment. This number is in the range [0,32]. Especially an insert
   * subsequent to a read (or a skip), can always insert as many bits as were
   * read.
   *
   * @return at each time the maximal number of bits that can be inserted into
   *         the stream.
   * @exception IOException
   *              if an I/O error occurs.
   */
  public int insertSize() throws IOException
  {
    if (in == null)
      throw new IOException("The stream is closed!");

    return 32 - bitSize;
  }

  /**
   * Ensures that the close method of this bit input stream is
   * called when there are no more references to it.
   *
   * @exception IOException
   *              if an I/O error occurs.
   */
  protected void finalize() throws IOException
  {
    if (in != null)
      close();
  }

  /**
   * Closes the underlying input stream and releases any system resources
   * associated with the stream. Any method call subsequent to a close will
   * result in an IOException with the message "The stream is closed!".
   *
   * @exception IOException
   *              if an I/O error occurs.
   */
  public void close() throws IOException
  {
    // a second call to close will have no effect
    if (in == null)
      return;
    in.close();

    // The instance variables in and buf are set to null.
    // This will prevent any method call subsequent to a close.
    // Such an effort will result in an IOException with
    // the message "The stream is closed!".

    in = null;
    buf = null;
    pos = -1;
    bufSize = -1;
    bitSize = -1;
  }

}