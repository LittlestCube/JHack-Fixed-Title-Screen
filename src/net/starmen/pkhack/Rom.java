package net.starmen.pkhack;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Wrapper class for a ROM. Loads the ROM into memory and provides
 * <code>read()</code> and <code>write()</code> methods.
 * 
 * @author AnyoneEB
 */
//Made by AnyoneEB.
//Code released under the GPL - http://www.gnu.org/licenses/gpl.txt
public class Rom
{
    /** Size in bytes of a regular Earthbound ROM. */
    public final static long EB_ROM_SIZE_REGULAR = 3146240;

    /** Size in bytes of an expanded Earthbound ROM. */
    public final static long EB_ROM_SIZE_EXPANDED = 4194816;

    /**
     * Contains the loaded ROM. It is perfered that you don't access this
     * directly.
     * 
     * @see #write(int, int)
     * @see #read(int)
     */
    private byte[] rom;

    /** Path to the ROM. */
    protected File path; //path to the rom

    /**
     * Returns the default directory for saving and loading.
     * 
     * @return The default directory for saving and loading.
     */
    public static String getDefaultDir()
    {
        return JHack.main.getPrefs().getValue("defaultDir");
    }

    /**
     * Sets the default directory for saving and loading.
     * 
     * @param dir The default directory for saving and loading.
     */
    public static void setDefaultDir(String dir)
    {
        JHack.main.getPrefs().setValue("defaultDir", dir);
    }

    //    /**
    //     * True if ROM is expanded.
    //     */
    //    public boolean isExpanded;
    /**
     * True if ROM is a valid size. (Either exactly 3 Megabytes, or exactly 4
     * Megabytes)
     */
    public boolean isValid = true;

    /**
     * True if a ROM is loaded. Changing this is a very bad idea.
     */
    public boolean isLoaded = false;

    /**
     * Current "place" in ROM.
     * 
     * @see #seek(int)
     */
    private int seekOffset;

    //    /**
    //     * String indicating an unknown ROM type.
    //     *
    //     * @see #getRomType()
    //     */
    //    public final static String TYPE_UNKNOWN = "Unknown";
    //
    //    /**
    //     * String indicating an Earthbound ROM.
    //     *
    //     * @see #getRomType()
    //     */
    //    public final static String TYPE_EARTHBOUND = "Earthbound";
    //
    //    /**
    //     * String indicating a Secret of Mana ROM.
    //     *
    //     * @see #getRomType()
    //     */
    //    public final static String TYPE_SECRET_OF_MANA = "Secret of Mana";
    //
    //    /**
    //     * String indicating a Chrono Trigger ROM.
    //     *
    //     * @see #getRomType()
    //     */
    //    public final static String TYPE_CHRONO_TRIGGER = "Chrono Trigger";

    //    /**
    //     * Array of all ROM types for internal use.
    //     */
    //    protected final static String[] TYPES = new String[]{TYPE_UNKNOWN,
    //        TYPE_EARTHBOUND, TYPE_SECRET_OF_MANA, TYPE_CHRONO_TRIGGER};

    /**
     * Stores the type of ROM.
     */
    protected String romType;

    /**
     * Creates an Rom object and loads the <code>File rompath</code>.
     */
    public Rom(File rompath)
    {
        this.loadRom(rompath);
    }

    /**
     * Creates an Rom object. There will be no ROM loaded so calls to
     * <code>read()</code> or <code>write()</code> will give exceptions.
     */
    public Rom()
    {}

    /**
     * Returns the ROM type. Type being which game this ROM is. Should be one of
     * the ROM type constants.
     * 
     * @return what game loaded ROM is
     */
    public String getRomType()
    {
        return romType;
    }

    /**
     * Sets the ROM type. Type being which game this ROM is. Should be one of
     * the ROM type constants.
     * 
     * @param romType what game loaded ROM is
     */
    public void setRomType(String romType)
    {
        this.romType = romType;
        saveRomType();
    }

    /**
     * Loads the ROM from the location specificied by the <code>File</code>.
     * 
     * @param rompath Where the ROM to load is.
     * @return True if the ROM was successfully loaded.
     * @see #loadRom()
     */
    public boolean loadRom(File rompath)
    {
        setDefaultDir(rompath.getParent());
        //        if (rompath.length() == ROM_SIZE_REGULAR)
        //        {
        //            this.isExpanded = false;
        //        }
        //        else if (rompath.length() == ROM_SIZE_EXPANDED)
        //        {
        //            this.isExpanded = true;
        //        }
        //        else
        //        {
        //            this.isValid = false;
        //            //Not either size, assume bad rom
        //            int ques = JOptionPane
        //                .showConfirmDialog(
        //                    null,
        //                    "File is the wrong size. It may not be a ROM\nContinue anyways?",
        //                    "Error!", JOptionPane.YES_NO_OPTION);
        //            if (ques == JOptionPane.NO_OPTION) { return false; }
        //            if (Math.abs(rompath.length() - ROM_SIZE_REGULAR) < Math
        //                .abs(rompath.length() - ROM_SIZE_EXPANDED))
        //            //if the size is closer to $regSize
        //            {
        //                this.isExpanded = false;
        //            }
        //            else
        //            {
        //                this.isExpanded = true;
        //            }
        //        }

        try
        {
            readFromRom(rompath);
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error: File not loaded: File not found.");
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            System.err.println("Error: File not loaded: Could read file.");
            e.printStackTrace();
            return false;
        }

        this.path = rompath;

        System.out.println("Opened ROM: " + rompath.toString());
        System.out.println("Rom size is: " + rompath.length());

        this.isLoaded = true;

        //set rom type
        //first look for .romtype file
        if (!loadRomType())
        {
            //            //then look at certain bytes
            //            //Earthbound
            //            if (length() >= 0x300200
            //                && compare(0x1f005, new int[]{0xa9, 0x5e, 0xc0, 0x85, 0x12,
            //                    0xa9, 0xc4, 0x00}))
            //            {
            //                //1F005 - 1F00C = "Start New Game" text pointer
            //                //should be [a9 5e c0 85 12 a9 c4 00]
            //                setRomType(TYPE_EARTHBOUND);
            //            }
            //            //SOM
            //            else if (length() >= 0x200000
            //                && compare(0x230, new int[]{0x48, 0xab, 0xbd, 0x00, 0x00}))
            //            {
            //                //0x230 unknown
            //                setRomType(TYPE_SECRET_OF_MANA);
            //            }
            //            //CT
            //            else if (length() >= 0x400200
            //                && compare(0x270, new int[]{0x00, 0xc2, 0xe0, 0xff, 0x01, 0x30,
            //                    0x06, 0xa2}))
            //            {
            //                //0x270 unknown
            //                setRomType(TYPE_CHRONO_TRIGGER);
            //            }
            //            //all tests failed, unknown
            //            else
            //            {
            //                //unknown
            //                setRomType(TYPE_UNKNOWN);
            //            }
            setRomType(RomTypeFinder.getRomType(this));
        }

        return true;
    }

    /**
     * Attemps to load ROM type from .romtype file. Returns false if unable to
     * read that file. If this returns false, other methods must be used to
     * discover the ROM type.
     * 
     * @return false if .romtype meta data file not found.
     */
    protected boolean loadRomType()
    {
        try
        {
            FileReader in = new FileReader(getPath() + ".romtype");
            char[] c = new char[(int) new File(getPath() + ".romtype").length()];
            in.read(c);
            in.close();
            String type = new String(c);
            //            for (int i = 0; i < TYPES.length; i++)
            //            {
            //                if (type.equals(TYPES[i])) setRomType(TYPES[i]);
            //            }
            setRomType(type);
            return true;
        }
        catch (FileNotFoundException e)
        {}
        catch (IOException e)
        {}
        return false;
    }

    protected void saveRomType()
    {
        try
        {
            FileWriter out = new FileWriter(getPath() + ".romtype");
            out.write(getRomType());
            out.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Reads the ROM into memory, DO NOT CALL. This method is here to be
     * overridden by classes extending Rom.
     * 
     * @param rompath Path to load from.
     * @throws FileNotFoundException
     * @throws IOException
     */
    protected void readFromRom(File rompath) throws FileNotFoundException,
        IOException
    {
        this.rom = new byte[(int) rompath.length()];
        //byte[] b = new byte[rom.length];
        FileInputStream in = new FileInputStream(rompath);
        in.read(rom);
        //for (int i = 0; i < this.rom.length; i++)
        //{
        //	this.rom[i] = (b[i] & 255);
        //}
        in.close();
    }

    /**
     * Loads the ROM from a location selected by the user.
     * 
     * @return True if the ROM was successfully loaded.
     * @see #loadRom(File)
     */
    public boolean loadRom()
    {
        JFileChooser jfc = new JFileChooser(Rom.getDefaultDir());
        jfc.setFileFilter(new FileFilter()
        {

            public boolean accept(File f)
            {
                if ((f.getAbsolutePath().toLowerCase().endsWith(".smc")
                    || f.getAbsolutePath().toLowerCase().endsWith(".sfc")
                    || f.getAbsolutePath().toLowerCase().endsWith(".fig") || f
                    .isDirectory())
                    && f.exists())
                {
                    return true;
                }
                return false;
            }

            public String getDescription()
            {
                return "SNES ROMs (*.smc, *.sfc, *.fig)";
            }
        });
        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
        {
            return loadRom(jfc.getSelectedFile());
        }
        else
        {
            return false;
        }
    }

    /**
     * Saves the ROM to the location specificied by the <code>File</code>.
     * 
     * @param rompath Where to save the ROM to.
     * @return True if the ROM was successfully saved.
     * @see #saveRom()
     * @see #saveRomAs()
     */
    public boolean saveRom(File rompath)
    {
        if (!this.isLoaded) //don't try to save if nothing is loaded
        {
            return false;
        }
        this.path = rompath;
        setDefaultDir(rompath.getParent());

        try
        {
            FileOutputStream out = new FileOutputStream(rompath);
            out.write(this.rom);
            out.close();
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Error: File not saved: File not found.");
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            System.err.println("Error: File not saved: Could write file.");
            e.printStackTrace();
            return false;
        }
        System.out.println("Saved ROM: " + this.path.length() + " bytes");
        saveRomType();
        return true;
    }

    /**
     * Saves the ROM to the location {@link #loadRom(File)}was last called for.
     * Note that {@link #loadRom()}calls <code>loadRom(File)</code> with the
     * selected <code>File</code>.
     * 
     * @return True if the ROM was successfully saved.
     * @see #saveRomAs()
     * @see #saveRom(File)
     */
    public boolean saveRom()
    {
        return saveRom(this.path);
    }

    /**
     * Saves the ROM to a location selected by the user.
     * 
     * @return True if the ROM was successfully saved.
     * @see #saveRom(File)
     */
    public boolean saveRomAs()
    {
        if (!this.isLoaded) //don't try to save if nothing is loaded
        {
            return false;
        }
        JFileChooser jfc = new JFileChooser(Rom.getDefaultDir());
        jfc.setFileFilter(new FileFilter()
        {

            public boolean accept(File f)
            {
                if (f.getAbsolutePath().toLowerCase().endsWith(".smc")
                    || f.getAbsolutePath().toLowerCase().endsWith(".sfc")
                    || f.getAbsolutePath().toLowerCase().endsWith(".fig")
                    || f.isDirectory())
                {
                    return true;
                }
                return false;
            }

            public String getDescription()
            {
                return "SNES ROMs (*.smc, *.sfc, *.fig)";
            }
        });
        if (jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
        {
            return saveRom(jfc.getSelectedFile());
        }
        else
        {
            return false;
        }
    }

    /**
     * Writes <code>arg</code> at <code>offset</code> in the rom. This does
     * not actually write to the filesystem. {@link #saveRom(File)}writes to
     * the filesystem.
     * 
     * @param offset Where in the ROM to write the value. Counting starts at
     *            zero, as normal.
     * @param arg What to write at <code>offset</code>.
     * @see #write(int, int, int)
     * @see #write(int, int[])
     * @see #write(int, char)
     * @see #write(int, char[])
     */
    public void write(int offset, int arg) //main write method
    {
        if (offset > this.rom.length) //don't write past the end of the ROM
        {
            return;
        }

        this.rom[offset] = (byte) (arg & 255);
    }

    /**
     * Writes the specified length multibyte value <code>arg</code> at
     * <code>offset</code> in the rom. This does not actually write to the
     * filesystem. {@link #saveRom(File)}writes to the filesystem. This writes
     * a multibyte value in the standard reverse bytes format.
     * 
     * @param offset Where in the ROM to write the value. Counting starts at
     *            zero, as normal.
     * @param arg What to write at <code>offset</code>.
     * @param bytes How many bytes long this is.
     * @see #write(int, int[])
     * @see #write(int, char)
     * @see #write(int, char[])
     */
    public void write(int offset, int arg, int bytes)
    {
        for (int i = 0; i < bytes; i++)
        {
            this.write(offset + i, arg >> (i * 8));
        }
    }

    /**
     * Writes <code>arg</code> at <code>offset</code> in the rom. This does
     * not actually write to the filesystem. {@link #saveRom(File)}writes to
     * the filesystem.
     * 
     * @param offset Where in the ROM to write the value. Counting starts at
     *            zero, as normal.
     * @param arg What to write at <code>offset</code>.
     * @see #write(int, int)
     * @see #write(int, int[])
     * @see #write(int, char[])
     */
    public void write(int offset, char arg)
    {
        write(offset, (int) arg);
    }

    /**
     * Writes <code>arg</code> at <code>offset</code> in the rom. This
     * writes more than one byte to <code>offset</code>. The first byte is
     * written to <code>offset</code>, next to <code>offset</code>+ 1,
     * etc. This does not actually write to the filesystem.
     * {@link #saveRom(File)}writes to the filesystem.
     * 
     * @param offset Where in the ROM to write the value. Counting starts at
     *            zero, as normal.
     * @param arg What to write at <code>offset</code>.
     * @see #write(int, int)
     * @see #write(int, int[], int)
     * @see #write(int, char)
     * @see #write(int, char[])
     */
    public void write(int offset, int[] arg) //write a [multibyte] string to a
    // place
    {
        write(offset, arg, arg.length);
    }

    /**
     * Writes <code>arg</code> at <code>offset</code> in the rom. This
     * writes more than one byte to <code>offset</code>. The first byte is
     * written to <code>offset</code>, next to <code>offset</code>+ 1,
     * etc. This does not actually write to the filesystem.
     * {@link #saveRom(File)}writes to the filesystem.
     * 
     * @param offset Where in the ROM to write the value. Counting starts at
     *            zero, as normal.
     * @param arg What to write at <code>offset</code>.
     * @see #write(int, int)
     * @see #write(int, int[], int)
     * @see #write(int, char)
     * @see #write(int, char[])
     */
    public void write(int offset, byte[] arg) //write a [multibyte] string to
    // a place
    {
        write(offset, arg, arg.length);
    }

    /**
     * Writes <code>len</code> bytes of <code>arg</code> at
     * <code>offset</code> in the rom. This writes more than one byte to
     * <code>offset</code>. The first byte is written to <code>offset</code>,
     * next to <code>offset</code>+ 1, etc. This does not actually write to
     * the filesystem. {@link #saveRom(File)}writes to the filesystem.
     * 
     * @param offset Where in the ROM to write the value. Counting starts at
     *            zero, as normal.
     * @param arg What to write at <code>offset</code>.
     * @param len Number of bytes to write
     * @see #write(int, int)
     * @see #write(int, int[])
     */
    public void write(int offset, byte[] arg, int len) //write a [multibyte]
    // string to a place
    {
        //OK to use this instead of write()?
        System.arraycopy(arg, 0, rom, offset, len);
        //        for (int i = 0; i < len; i++)
        //        {
        //            if (!(offset + i > this.length()))
        //            //don't write past the end of the ROM
        //            {
        //                this.write(offset + i, arg[i]);
        //            }
        //            else
        //            { /***/
        //                //System.out.println("Error: attempted write past end of
        //                // ROM.");
        //            }
        //        }
    }

    /**
     * Writes arg at offset 0.
     * 
     * @param arg
     */
    public void writeFullRom(int[] arg)
    {
        write(0, arg);
    }

    /**
     * Writes arg at offset 0.
     * 
     * @param arg
     */
    public void writeFullRom(byte[] arg)
    {
        write(0, arg);
    }

    /**
     * Writes <code>len</code> bytes of <code>arg</code> at
     * <code>offset</code> in the rom. This writes more than one byte to
     * <code>offset</code>. The first byte is written to <code>offset</code>,
     * next to <code>offset</code>+ 1, etc. This does not actually write to
     * the filesystem. {@link #saveRom(File)}writes to the filesystem.
     * 
     * @param offset Where in the ROM to write the value. Counting starts at
     *            zero, as normal.
     * @param arg What to write at <code>offset</code>.
     * @param len Number of bytes to write
     * @see #write(int, int)
     * @see #write(int, char)
     * @see #write(int, char[])
     */
    public void write(int offset, int[] arg, int len) //write a [multibyte]
    // string to a place
    {
        for (int i = 0; i < len; i++)
        {
            if (!(offset + i > this.length()))
            //don't write past the end of the ROM
            {
                this.write(offset + i, arg[i]);
            }
            else
            {   /***/
                //System.out.println("Error: attempted write past end of
                // ROM.");
            }
        }
    }

    /**
     * Writes <code>len</code> multibyte indexes of <code>arg</code> at
     * <code>offset</code> in the rom. This writes
     * <code>len * bytes<code> bytes.
     * This does not actually write to the filesystem. {@link #saveRom(File)}
     * writes to the filesystem.
     * 
     * @param offset Where in the ROM to write the value. Counting starts at zero, as normal.
     * @param arg What to write at <code>offset</code>.
     * @param len Number of array indexes to go through
     * @param bytes How many bytes to write for each index
     * @see #write(int, int)
     * @see #write(int, int, int)
     * @see #write(int, int[], int)
     */
    public void write(int offset, int[] arg, int len, int bytes) //write a
    // [multibyte]
    // string to a
    // place
    {
        for (int i = 0; i < len; i++)
        {
            if (!(offset + (i * bytes) > this.length()))
            //don't write past the end of the ROM
            {
                this.write(offset + (i * bytes), arg[i], bytes);
            }
            else
            {   /***/
                //System.out.println("Error: attempted write past end of
                // ROM.");
            }
        }
    }

    /**
     * Writes <code>arg</code> at <code>offset</code> in the rom. This
     * writes more than one byte to <code>offset</code>. The first byte is
     * written to <code>offset</code>, next to <code>offset</code>+ 1,
     * etc. This does not actually write to the filesystem.
     * {@link #saveRom(File)}writes to the filesystem.
     * 
     * @param offset Where in the ROM to write the value. Counting starts at
     *            zero, as normal.
     * @param arg What to write at <code>offset</code>.
     * @see #write(int, int)
     * @see #write(int, int[])
     * @see #write(int, char)
     */
    public void write(int offset, char[] arg)
    {
        int[] newArg = new int[arg.length];
        for (int i = 0; i < arg.length; i++)
        {
            newArg[i] = (int) arg[i];
        }
        write(offset, newArg);
    }

    /**
     * Reads an <code>int</code> from <code>offset</code> in the rom.
     * 
     * @param offset Where to read from.
     * @return <code>int</code> at <code>offset</code>. If
     *         <code>offset &gt; the rom.length</code> then it is -1.
     */
    public int read(int offset)
    {
        if ((offset & 0x7fffffff) >= this.length()) //don't write past the end
        // of the ROM
        {
            //			System.out.println(
            //				"Attempted read past end of rom, (0x"
            //					+ Integer.toHexString(offset)
            //					+ ")");
            return -1;
        }
        return this.rom[offset] & 255;
    }

    /**
     * Reads a <code>byte</code> from <code>offset</code> in the rom.
     * 
     * @param offset Where to read from.
     * @return <code>byte</code> at <code>offset</code>. If
     *         <code>offset &gt; the rom.length</code> then it is -1.
     */
    public byte readByte(int offset)
    {
        return (byte) this.read(offset);
    }

    /**
     * Reads an <code>int[]</code> from <code>offset</code> in the rom.
     * 
     * @param offset Where to read from.
     * @param length Number of bytes to read.
     * @return <code>int[]</code> at <code>offset</code> with a length of
     *         <code>length</code>. If
     *         <code>offset &gt; the rom.length</code> then it is -1.
     */
    public int[] read(int offset, int length)
    {
        int[] returnValue = new int[length];
        for (int i = 0; i < length; i++)
        {
            returnValue[i] = this.read(offset + i);
        }
        return returnValue;
    }

    /**
     * Reads a <code>byte[]</code> from <code>offset</code> in the rom.
     * 
     * @param offset Where to read from.
     * @param length Number of bytes to read.
     * @return <code>byte[]</code> at <code>offset</code> with a length of
     *         <code>length</code>. If
     *         <code>offset &gt; the rom.length</code> then it is -1 or null
     *         may be returned.
     */
    public byte[] readByte(int offset, int length)
    {
        byte[] returnValue = new byte[length];
        try
        {
            //OK to not end up going to read function?
            System.arraycopy(rom, offset, returnValue, 0, length);
        }
        catch (IndexOutOfBoundsException e)
        {
            return null;
        }
        //        for (int i = 0; i < length; i++)
        //        {
        //            returnValue[i] = this.readByte(offset + i);
        //        }
        return returnValue;
    }

    /**
     * Reads an <code>int[]</code> of the entire ROM.
     * 
     * @return The entire ROM as a <code>int[]</code>
     */
    public int[] readFullRom()
    {
        return read(0, this.length());
    }

    /**
     * Reads a <code>byte[]</code> of the entire ROM.
     * 
     * @return The entire ROM as a <code>byte[]</code>
     */
    public byte[] readFullRomByteArr()
    {
        return readByte(0, this.length());
    }

    /**
     * Reads an <code>char</code> from <code>offset</code> in the rom.
     * 
     * @param offset Where to read from.
     * @return <code>char</code> at <code>offset</code>. If
     *         <code>offset<code> is past the end of the rom then it is -1.
     */
    public char readChar(int offset)
    {
        return (char) read(offset);
    }

    /**
     * Reads an <code>char[]</code> from <code>offset</code> in the rom.
     * 
     * @param offset Where to read from.
     * @param length Number of bytes to read.
     * @return <code>char[]</code> at <code>offset</code> with a length of
     *         <code>length</code>. If
     *         <code>offset<code> is past the end of the rom then it is -1.
     */
    public char[] readChar(int offset, int length) //read as a char[] instead
    // of int[]
    {
        char[] returnValue = new char[length];
        int[] in = read(offset, length);

        for (int i = 0; i < length; i++)
        {
            returnValue[i] = (char) in[i];
        }
        return returnValue;
    }

    /**
     * Reads a mulibyte number with the specified offset and length. Reverses
     * the byte order to get the correct value.
     * 
     * @param offset Where the number is.
     * @param len How many bytes long the number is.
     * @return Multibyte value as an int.
     */
    public int readMulti(int offset, int len)
    {
        int out = 0;
        for (int i = 0; i < len; i++)
        {
            out += this.read(offset + i) << (i * 8);
        }
        return out;
    }

    /**
     * Reads a SNES pointer from an ASM link. ASM links look like [A9 WW XX 85
     * 0E A9 YY ZZ] for a pointer to $ZZYYXXWW.
     * 
     * @param offset offset of ASM link to read
     * @return an SNES pointer
     */
    public int readAsmPointer(int offset)
    {
        int out = 0;
        if (read(offset++) != 0xA9)
            return -1;
        out |= read(offset++);
        out |= read(offset++) << 8;
        if (read(offset++) != 0x85)
            return -1;
        if (read(offset++) != 0x0E)
            return -1;
        if (read(offset++) != 0xA9)
            return -1;
        out |= read(offset++) << 16;
        out |= read(offset++) << 24;

        return out;
    }

    /**
     * Reads a regular pointer from an ASM link. ASM links look like [A9 WW XX
     * 85 0E A9 YY ZZ] for a pointer to $ZZYYXXWW.
     * 
     * @param offset offset of ASM link to read
     * @return an regular pointer
     */
    public int readRegAsmPointer(int offset)
    {
        return HackModule.toRegPointer(readAsmPointer(offset));
    }

    /**
     * Writes a SNES pointer to an ASM link. ASM links look like [A9 WW XX 85 0E
     * A9 YY ZZ] for a pointer to $ZZYYXXWW.
     * 
     * @param offset offset of ASM link to write
     * @param snesPointer an SNES pointer
     */
    public void writeAsmPointer(int offset, int snesPointer)
    {
        write(offset++, 0xA9);
        write(offset++, snesPointer & 0xFF);
        write(offset++, (snesPointer >> 8) & 0xFF);
        write(offset++, 0x85);
        write(offset++, 0x0E);
        write(offset++, 0xA9);
        write(offset++, (snesPointer >> 16) & 0xFF);
        write(offset++, (snesPointer >> 24) & 0xFF);
    }

    /**
     * Writes a regular pointer to an ASM link. ASM links look like [A9 WW XX 85
     * 0E A9 YY ZZ] for a pointer to $ZZYYXXWW.
     * 
     * @param offset offset of ASM link to write
     * @param regPointer an regular pointer
     */
    public void writeRegAsmPointer(int offset, int regPointer)
    {
        writeAsmPointer(offset, HackModule.toSnesPointer(regPointer));
    }

    //pallette
    /**
     * Reads an SNES format palette color from the specificed place in the rom.
     * This reads one color of a palette, which is two bytes long. SNES palettes
     * are made up of 16-bit little endian color entries. 5 bits each are used
     * for (from lowest order to highest order bits) red, green, and blue, and
     * one bit is left unused.
     * 
     * @param offset offset in the rom palette color is at; note that the byte
     *            at <code>offset</code> and the byte after will be read
     * @return a {@link Color}that is equivalent to the specified SNES color
     * @see #read(int)
     * @see #readByte(int, int)
     * @see #readPalette(int, Color[])
     * @see #readPalette(int, int)
     * @see #readPaletteSeek()
     * @see #readPaletteSeek(Color[])
     * @see #readPaletteSeek(int)
     * @see HackModule#readPalette(byte[], int)
     */
    public Color readPalette(int offset)
    {
        return HackModule.readPalette(readByte(offset, 2));
    }

    /**
     * Reads an SNES format palette from the specificed place in the rom. This
     * reads as many colors of the palette as the
     * <code>Color[]<code> array is long;
     * each color is two bytes long. SNES palettes
     * are made up of 16-bit little endian color entries. 5 bits each are used
     * for (from lowest order to highest order bits) red, green, and blue, and
     * one bit is left unused.
     * 
     * @param offset offset in the rom palette color is at; note
     *            that <code>c.length() * 2</code> bytes
     *            will be read
     * @param c <code>Color[]</code> to read {@link Color}'s into, which are equivalent to the SNES colors 
     * @see #read(int)
     * @see #readByte(int, int)
     * @see #readPalette(int)
     * @see #readPalette(int, int)
     * @see #readPaletteSeek()
     * @see #readPaletteSeek(Color[])
     * @see #readPaletteSeek(int)
     * @see HackModule#readPalette(byte[], int)
     * @see HackModule#readPalette(byte[], int, Color[])
     */
    public void readPalette(int offset, Color[] c)
    {
        HackModule.readPalette(readByte(offset, 2 * c.length), c);
    }

    /**
     * Reads an SNES format palette from the specificed place in the rom. This
     * reads <code>size</code> colors of the palette; each color is two bytes
     * long. SNES palettes are made up of 16-bit little endian color entries. 5
     * bits each are used for (from lowest order to highest order bits) red,
     * green, and blue, and one bit is left unused.
     * 
     * @param offset offset in the rom palette color is at; note that
     *            <code>size * 2</code> bytes will be read
     * @param size number of colors to read, <code>size * 2</code> equals the
     *            number of bytes to read
     * @return a <code>Color[]</code> of {@link Color}'s that are equivalent
     *         to the SNES colors
     * @see #read(int)
     * @see #readByte(int, int)
     * @see #readPalette(int)
     * @see #readPalette(int, Color[])
     * @see #readPaletteSeek()
     * @see #readPaletteSeek(Color[])
     * @see #readPaletteSeek(int)
     * @see HackModule#readPalette(byte[], int)
     * @see HackModule#readPalette(byte[], int, int)
     */
    public Color[] readPalette(int offset, int size)
    {
        Color[] c = new Color[size];
        readPalette(offset, c);
        return c;
    }

    /**
     * Reads an SNES format palette color from the seek offset in the rom. This
     * reads one color of a palette, which is two bytes long. SNES palettes are
     * made up of 16-bit little endian color entries. 5 bits each are used for
     * (from lowest order to highest order bits) red, green, and blue, and one
     * bit is left unused.
     * 
     * @return a {@link Color}that is equivalent to the specified SNES color
     * @see #seek(int)
     * @see #readSeek()
     * @see #readByteSeek(int)
     * @see #readPalette(int, Color[])
     * @see #readPalette(int, int)
     * @see #readPaletteSeek()
     * @see #readPaletteSeek(Color[])
     * @see #readPaletteSeek(int)
     * @see HackModule#readPalette(byte[], int)
     */
    public Color readPaletteSeek()
    {
        return HackModule.readPalette(readByteSeek(2));
    }

    /**
     * Reads an SNES format palette from the seek offset in the rom. This reads
     * as many colors of the palette as the <code>Color[]<code> array is long;
     * each color is two bytes long. SNES palettes
     * are made up of 16-bit little endian color entries. 5 bits each are used
     * for (from lowest order to highest order bits) red, green, and blue, and
     * one bit is left unused.
     * 
     * @param c <code>Color[]</code> to read {@link Color}'s into, which are equivalent to the SNES colors 
     * @see #seek(int)
     * @see #readSeek()
     * @see #readByteSeek(int)
     * @see #readPalette(int)
     * @see #readPalette(int, int)
     * @see #readPalette(int, Color[])
     * @see #readPaletteSeek()
     * @see #readPaletteSeek(int)
     * @see HackModule#readPalette(byte[], int)
     * @see HackModule#readPalette(byte[], int, Color[])
     */
    public void readPaletteSeek(Color[] c)
    {
        HackModule.readPalette(readByteSeek(2 * c.length), c);
    }

    /**
     * Reads an SNES format palette from the seek offset in the rom. This reads
     * <code>size</code> colors of the palette; each color is two bytes long.
     * SNES palettes are made up of 16-bit little endian color entries. 5 bits
     * each are used for (from lowest order to highest order bits) red, green,
     * and blue, and one bit is left unused.
     * 
     * @param size number of colors to read, <code>size * 2</code> equals the
     *            number of bytes to read
     * @return a <code>Color[]</code> of {@link Color}'s that are equivalent
     *         to the SNES colors
     * @see #seek(int)
     * @see #readSeek()
     * @see #readByteSeek(int)
     * @see #readPalette(int)
     * @see #readPalette(int, int)
     * @see #readPalette(int, Color[])
     * @see #readPaletteSeek()
     * @see #readPaletteSeek(Color[])
     * @see HackModule#readPalette(byte[], int)
     * @see HackModule#readPalette(byte[], int, int)
     */
    public Color[] readPaletteSeek(int size)
    {
        Color[] c = new Color[size];
        readPaletteSeek(c);
        return c;
    }

    /**
     * Writes an SNES format palette color to the specificed place in the rom.
     * This writes one color of a palette, which is two bytes long. SNES
     * palettes are made up of 16-bit little endian color entries. 5 bits each
     * are used for (from lowest order to highest order bits) red, green, and
     * blue, and one bit is left unused.
     * 
     * @param offset offset in rom palette color will be written at at; note
     *            that the byte at <code>offset</code> and the byte after will
     *            be written to
     * @param c {@link Color}to write as an SNES color; note that
     *            <code>Color</code> supports up to 8 bits per color component
     *            (red, green, blue) as well as an alpha channel and SNES colors
     *            have only 5 bits per color component and no alpha support
     * @see #write(int)
     * @see #write(int, int)
     * @see #readPalette(int)
     * @see #writePalette(int, Color[])
     * @see #writePaletteSeek(Color)
     * @see #writePaletteSeek(Color[])
     * @see HackModule#writePalette(byte[], int, Color)
     */
    public void writePalette(int offset, Color c)
    {
        write(offset, HackModule.writePalette(c));
    }

    /**
     * Writes an SNES format palette to the specificed place in the rom. This
     * writes all <code>Color</code>'s in <code>c</code> to a palette; each
     * color is two bytes long. SNES palettes are made up of 16-bit little
     * endian color entries. 5 bits each are used for (from lowest order to
     * highest order bits) red, green, and blue, and one bit is left unused.
     * 
     * @param offset offset in rom palette color will be written at at; note
     *            that <code>c.length() * 2</code> bytes will be written to
     * @param c {@link Color}[] to write as an SNES palette; note that
     *            <code>Color</code> supports up to 8 bits per color component
     *            (red, green, blue) as well as an alpha channel and SNES colors
     *            have only 5 bits per color component and no alpha support
     * @see #write(int)
     * @see #write(int, int)
     * @see #readPalette(int)
     * @see #writePalette(int, Color[])
     * @see #writePaletteSeek(Color)
     * @see #writePaletteSeek(Color[])
     * @see HackModule#writePalette(byte[], int, Color[])
     */
    public void writePalette(int offset, Color[] c)
    {
        write(offset, HackModule.writePalette(c));
    }

    /**
     * Writes an SNES format palette color to the seek offset in the rom. This
     * writes one color of a palette, which is two bytes long. SNES palettes are
     * made up of 16-bit little endian color entries. 5 bits each are used for
     * (from lowest order to highest order bits) red, green, and blue, and one
     * bit is left unused.
     * 
     * @param c {@link Color}to write as an SNES color; note that
     *            <code>Color</code> supports up to 8 bits per color component
     *            (red, green, blue) as well as an alpha channel and SNES colors
     *            have only 5 bits per color component and no alpha support
     * @see #seek(int)
     * @see #writeSeek()
     * @see #writeSeek(int)
     * @see #readPalette(int)
     * @see #writePalette(int, Color)
     * @see #writePalette(int, Color[])
     * @see #writePaletteSeek(Color[])
     * @see HackModule#writePalette(byte[], int, Color)
     */
    public void writePaletteSeek(Color c)
    {
        writeSeek(HackModule.writePalette(c));
    }

    /**
     * Writes an SNES format palette to the seek offset in the rom. This writes
     * all <code>Color</code>'s in <code>c</code> to a palette; each color
     * is two bytes long. SNES palettes are made up of 16-bit little endian
     * color entries. 5 bits each are used for (from lowest order to highest
     * order bits) red, green, and blue, and one bit is left unused.
     * 
     * @param c {@link Color}[] to write as an SNES palette; note that
     *            <code>Color</code> supports up to 8 bits per color component
     *            (red, green, blue) as well as an alpha channel and SNES colors
     *            have only 5 bits per color component and no alpha support
     * @see #seek(int)
     * @see #writeSeek()
     * @see #writeSeek(int)
     * @see #readPalette(int)
     * @see #writePalette(int, Color)
     * @see #writePalette(int, Color[])
     * @see #writePaletteSeek(Color)
     * @see HackModule#writePalette(byte[], int, Color[])
     */
    public void writePaletteSeek(Color[] c)
    {
        writeSeek(HackModule.writePalette(c));
    }

    //seeking read/write
    /**
     * Places marks current place in ROM as <code>offset</code>.
     * 
     * @param offset offset in ROM to seek to
     * @see #writeSeek(int)
     * @see #readSeek()
     */
    public void seek(int offset)
    {
        this.seekOffset = offset;
    }

    /**
     * Reads the <code>int</code> at
     * <code>seekOffset<code> and increments <code>seekOffset</code>.
     * 
     * @return <code>int</code> at <code>seekOffset</code>
     * @see #seek(int)
     * @see #readByteSeek()
     * @see #writeSeek(int)
     */
    public int readSeek()
    {
        return read(seekOffset++);
    }

    /**
     * Reads the <code>byte</code> at
     * <code>seekOffset<code> and increments <code>seekOffset</code>.
     * 
     * @return <code>byte</code> at <code>seekOffset</code>
     * @see #seek(int)
     * @see #readSeek()
     * @see #writeSeek(int)
     */
    public byte readByteSeek()
    {
        return readByte(seekOffset++);
    }

    /**
     * Writes <code>arg</code> at <code>seekOffset</code> in the rom and
     * increments <code>seekOffset</code>. This does not actually write to
     * the filesystem. {@link #saveRom(File)}writes to the filesystem.
     * 
     * @param arg What to write at <code>seekOffset</code>.
     * @see #seek(int)
     * @see #write(int, int)
     */
    public void writeSeek(int arg) //main write method
    {
        write(seekOffset++, arg);
    }

    /**
     * Writes the specified length multibyte value <code>arg</code> at
     * <code>seekOffset</code> in the rom and increments
     * <code>seekOffset</code>. This does not actually write to the
     * filesystem. {@link #saveRom(File)}writes to the filesystem. This writes
     * a multibyte value in the standard reverse bytes format.
     * 
     * @param arg What to write at <code>offset</code>.
     * @param bytes How many bytes long this is.
     * @see #seek(int)
     * @see #writeSeek(int)
     */
    public void writeSeek(int arg, int bytes)
    {
        for (int i = 0; i < bytes; i++)
        {
            this.writeSeek(arg >> (i * 8));
        }
    }

    /**
     * Writes <code>arg</code> at <code>seekOffset</code> in the rom and
     * increments <code>seekOffset</code>. This does not actually write to
     * the filesystem. {@link #saveRom(File)}writes to the filesystem.
     * 
     * @param arg What to write at <code>seekOffset</code>.
     * @see #seek(int)
     * @see #writeSeek(int)
     */
    public void writeSeek(char arg)
    {
        writeSeek((int) arg);
    }

    /**
     * Writes <code>arg</code> at <code>seekOffset</code> in the rom and
     * moves <code>seekOffset</code>.<code>seekOffset</code> will point to
     * the byte after the last byte written or the byte after the end of the
     * ROM. This writes more than one byte to <code>seekOffset</code>. The
     * first byte is written to <code>seekOffset</code>, next to
     * <code>seekOffset</code>+ 1, etc. This does not actually write to the
     * filesystem. {@link #saveRom(File)}writes to the filesystem.
     * 
     * @param arg What to write at <code>seekOffset</code>.
     * @see #seek(int)
     * @see #writeSeek(int)
     */
    public void writeSeek(int[] arg)
    {
        writeSeek(arg, arg.length);
    }

    /**
     * Writes <code>arg</code> at <code>seekOffset</code> in the rom and
     * moves <code>seekOffset</code>.<code>seekOffset</code> will point to
     * the byte after the last byte written or the byte after the end of the
     * ROM. This writes more than one byte to <code>seekOffset</code>. The
     * first byte is written to <code>seekOffset</code>, next to
     * <code>seekOffset</code>+ 1, etc. This does not actually write to the
     * filesystem. {@link #saveRom(File)}writes to the filesystem.
     * 
     * @param arg What to write at <code>seekOffset</code>.
     * @see #seek(int)
     * @see #writeSeek(int)
     */
    public void writeSeek(byte[] arg) //write a [multibyte] string to a place
    {
        writeSeek(arg, arg.length);
    }

    /**
     * Writes <code>len</code> bytes of <code>arg</code> at
     * <code>seekOffset</code> in the rom and moves <code>seekOffset</code>.
     * <code>seekOffset</code> will point to the byte after the last byte
     * written or the byte after the end of the ROM. This writes more than one
     * byte to <code>seekOffset</code>. The first byte is written to
     * <code>seekOffset</code>, next to <code>seekOffset</code>+ 1, etc.
     * This does not actually write to the filesystem. {@link #saveRom(File)}
     * writes to the filesystem.
     * 
     * @param arg What to write at <code>seekOffset</code>.
     * @param len Number of bytes to write
     * @see #seek(int)
     * @see #writeSeek(int)
     * @see #writeSeek(byte[])
     */
    public void writeSeek(byte[] arg, int len) //write a [multibyte] string to
    // a place
    {
        for (int i = 0; i < len; i++)
        {
            if (!(seekOffset > this.length()))
            //don't write past the end of the ROM
            {
                writeSeek(arg[i]);
            }
            else
            {   /***/
                //System.out.println("Error: attempted write past end of
                // ROM.");
            }
        }
    }

    /**
     * Writes <code>len</code> bytes of <code>arg</code> at
     * <code>seekOffset</code> in the rom and moves <code>seekOffset</code>.
     * <code>seekOffset</code> will point to the byte after the last byte
     * written or the byte after the end of the ROM. This writes more than one
     * byte to <code>seekOffset</code>. The first byte is written to
     * <code>seekOffset</code>, next to <code>seekOffset</code>+ 1, etc.
     * This does not actually write to the filesystem. {@link #saveRom(File)}
     * writes to the filesystem.
     * 
     * @param arg What to write at <code>seekOffset</code>.
     * @param len Number of bytes to write
     * @see #seek(int)
     * @see #writeSeek(int)
     * @see #writeSeek(byte[])
     */
    public void writeSeek(int[] arg, int len) //write a [multibyte] string to
    // a place
    {
        for (int i = 0; i < len; i++)
        {
            if (!(seekOffset > this.length()))
            //don't write past the end of the ROM
            {
                writeSeek(arg[i]);
            }
            else
            {   /***/
                //System.out.println("Error: attempted write past end of
                // ROM.");
            }
        }
    }

    /**
     * Writes <code>len</code> multibyte indexes of <code>arg</code> at
     * <code>seekOffset</code> in the rom and moves <code>seekOffset</code>.
     * <code>seekOffset</code> will point to the byte after the last byte
     * written or the byte after the end of the ROM. This writes
     * <code>len * bytes<code> bytes.
     * This does not actually write to the filesystem. {@link #saveRom(File)}
     * writes to the filesystem.
     * 
     * @param arg What to write at <code>seekOffset</code>.
     * @param len Number of array indexes to go through
     * @param bytes How many bytes to write for each index
     * @see #seek(int)
     * @see #writeSeek(int)
     * @see #writeSeek(int, int)
     */
    public void writeSeek(int[] arg, int len, int bytes) //write a [multibyte]
    // string to a place
    {
        for (int i = 0; i < len; i++)
        {
            if (!(seekOffset + (i * bytes) > this.length()))
            //don't write past the end of the ROM
            {
                this.writeSeek(arg[i], bytes);
            }
            else
            {   /***/
                //System.out.println("Error: attempted write past end of
                // ROM.");
            }
        }
    }

    /**
     * Writes <code>arg</code> at <code>seekOffset</code> in the rom and
     * moves <code>seekOffset</code>.<code>seekOffset</code> will point to
     * the byte after the last byte written or the byte after the end of the
     * ROM. This writes more than one byte to <code>seekOffset</code>. The
     * first byte is written to <code>seekOffset</code>, next to
     * <code>seekOffset</code>+ 1, etc. This does not actually write to the
     * filesystem. {@link #saveRom(File)}writes to the filesystem.
     * 
     * @param arg What to write at <code>seekOffset</code>.
     * @see #seek(int)
     * @see #writeSeek(int)
     */
    public void writeSeek(char[] arg)
    {
        writeSeek(arg, arg.length);
    }

    /**
     * Writes <code>len</code> bytes of <code>arg</code> at
     * <code>seekOffset</code> in the rom and moves <code>seekOffset</code>.
     * <code>seekOffset</code> will point to the byte after the last byte
     * written or the byte after the end of the ROM. This writes more than one
     * byte to <code>seekOffset</code>. The first byte is written to
     * <code>seekOffset</code>, next to <code>seekOffset</code>+ 1, etc.
     * This does not actually write to the filesystem. {@link #saveRom(File)}
     * writes to the filesystem.
     * 
     * @param arg What to write at <code>seekOffset</code>.
     * @param len Number of bytes to write
     * @see #seek(int)
     * @see #writeSeek(char)
     * @see #writeSeek(char[])
     */
    public void writeSeek(char[] arg, int len) //write a [multibyte] string to
    // a place
    {
        for (int i = 0; i < len; i++)
        {
            if (!(seekOffset > this.length()))
            //don't write past the end of the ROM
            {
                writeSeek(arg[i]);
            }
            else
            {   /***/
                //System.out.println("Error: attempted write past end of
                // ROM.");
            }
        }
    }

    /**
     * Reads an <code>int[]</code> from <code>seekOffset</code> in the rom
     * and moves <code>seekOffset</code>.<code>seekOffset</code> will
     * point to the byte after the last byte read. This may be past the end of
     * the ROM.
     * 
     * @param length Number of bytes to read.
     * @return <code>int[]</code> at <code>seekOffset</code> with a length
     *         of <code>length</code>. If
     *         <code>seekOffset &gt; the rom.length</code> then it is -1.
     * @see #seek(int)
     * @see #readSeek()
     */
    public int[] readSeek(int length)
    {
        int[] returnValue = new int[length];
        readSeek(returnValue, length);
        return returnValue;
    }

    /**
     * Reads an <code>int[]</code> from <code>seekOffset</code> in the rom
     * and moves <code>seekOffset</code>.<code>seekOffset</code> will
     * point to the byte after the last byte read. This may be past the end of
     * the ROM.
     * 
     * @param target <code>int[]</code> to read into
     * @param length Number of bytes to read.
     * @see #seek(int)
     * @see #readSeek()
     */
    public void readSeek(int[] target, int length)
    {
        for (int i = 0; i < length; i++)
        {
            target[i] = this.readSeek();
        }
    }

    /**
     * Reads an <code>int[]</code> from <code>seekOffset</code> in the rom
     * and moves <code>seekOffset</code>.<code>seekOffset</code> will
     * point to the byte after the last byte read. This may be past the end of
     * the ROM.
     * 
     * @param target <code>int[]</code> to read into
     * @see #seek(int)
     * @see #readSeek()
     * @see #readSeek(int[], int)
     */
    public void readSeek(int[] target)
    {
        readSeek(target, target.length);
    }

    /**
     * Reads <code>length</code> multibyte indexes into an <code>int[]</code>
     * from offset <code>seekOffset</code> in the rom and moves
     * <code>seekOffset</code>.<code>seekOffset</code> will point to the
     * byte after the last byte read. This may be past the end of the ROM.
     * 
     * @param length Number of indexes to read.
     * @param bytes Number of bytes per index
     * @return <code>int[]</code> at <code>seekOffset</code> with a length
     *         of <code>length</code>. If
     *         <code>seekOffset &gt; the rom.length</code> then it is -1.
     * @see #seek(int)
     * @see #readSeek()
     * @see #readMultiSeek(int)
     * @see #readMultiSeek(int[], int)
     * @see #readMultiSeek(int[], int, int)
     */
    public int[] readMultiSeek(int length, int bytes)
    {
        int[] returnValue = new int[length];
        readMultiSeek(returnValue, length, bytes);
        return returnValue;
    }

    /**
     * Reads <code>length</code> multibyte indexes into <code>target</code>
     * from offset <code>seekOffset</code> in the rom and moves
     * <code>seekOffset</code>.<code>seekOffset</code> will point to the
     * byte after the last byte read. This may be past the end of the ROM.
     * 
     * @param target <code>int[]</code> to read into
     * @param length Number of indexes to read.
     * @param bytes Number of bytes per index
     * @return <code>int[]</code> at <code>seekOffset</code> with a length
     *         of <code>length</code>. If
     *         <code>seekOffset &gt; the rom.length</code> then it is -1.
     * @see #seek(int)
     * @see #readSeek()
     * @see #readMultiSeek(int)
     * @see #readMultiSeek(int, int)
     * @see #readMultiSeek(int[], int)
     */
    public void readMultiSeek(int[] target, int length, int bytes)
    {
        for (int i = 0; i < length; i++)
        {
            target[i] = this.readMultiSeek(bytes);
        }
    }

    /**
     * Reads <code>target.length</code> multibyte indexes into
     * <code>target</code> from offset <code>seekOffset</code> in the rom
     * and moves <code>seekOffset</code>.<code>seekOffset</code> will
     * point to the byte after the last byte read. This may be past the end of
     * the ROM.
     * 
     * @param target <code>int[]</code> to read into
     * @param bytes Number of bytes per index
     * @return <code>int[]</code> at <code>seekOffset</code> with a length
     *         of <code>target.length</code>. If
     *         <code>seekOffset &gt; the rom.length</code> then it is -1.
     * @see #seek(int)
     * @see #readSeek()
     * @see #readMultiSeek(int)
     * @see #readMultiSeek(int, int)
     * @see #readMultiSeek(int[], int, int)
     */
    public void readMultiSeek(int[] target, int bytes)
    {
        readMultiSeek(target, target.length, bytes);
    }

    /**
     * Reads a <code>byte[]</code> from <code>offset</code> in the rom and
     * moves <code>seekOffset</code>.<code>seekOffset</code> will point to
     * the byte after the last byte read. This may be past the end of the ROM. *
     * 
     * @param length Number of bytes to read.
     * @return <code>byte[]</code> at <code>seekOffset</code> with a length
     *         of <code>length</code>. If
     *         <code>seekOffset &gt; the rom.length</code> then it is -1.
     */
    public byte[] readByteSeek(int length)
    {
        byte[] returnValue = new byte[length];
        for (int i = 0; i < length; i++)
        {
            returnValue[i] = readByteSeek();
        }
        return returnValue;
    }

    /**
     * Reads an <code>char</code> from <code>seekOffset</code> in the rom
     * and increments <code>seekOffset</code>.
     * 
     * @return <code>char</code> at <code>seekOffset</code>. If
     *         <code>seekOffset<code> is past the end of the rom then it is -1.
     */
    public char readCharSeek()
    {
        return (char) readSeek();
    }

    /**
     * Reads an <code>char[]</code> from <code>seekOffset</code> in the rom
     * and moves <code>seekOffset</code>.<code>seekOffset</code> will
     * point to the byte after the last byte read. This may be past the end of
     * the ROM.
     * 
     * @param length Number of bytes to read.
     * @return <code>char[]</code> at <code>seekOffset</code> with a length
     *         of <code>length</code>. If
     *         <code>seekOffset &gt; the rom.length</code> then it is -1.
     */
    public char[] readCharSeek(int length)
    {
        char[] returnValue = new char[length];
        for (int i = 0; i < length; i++)
        {
            returnValue[i] = readCharSeek();
        }
        return returnValue;
    }

    /**
     * Reads a mulibyte number with the specified length from
     * <code>seekOffset</code> in the rom and moves <code>seekOffset</code>.
     * <code>seekOffset</code> will point to the byte after the last byte
     * read. This may be past the end of the ROM. Reverses the byte order to get
     * the correct value.
     * 
     * @param len How many bytes long the number is.
     * @return Multibyte value as an int.
     */
    public int readMultiSeek(int len)
    {
        int out = 0;
        for (int i = 0; i < len; i++)
        {
            out += readSeek() << (i * 8);
        }
        return out;
    }

    /**
     * Writes the specified range from an orginal ROM into this ROM.
     * 
     * @param offset range to start reseting to orginal
     * @param len number of bytes to reset to orginal
     * @param orgRom orginal ROM to read from
     * @see net.starmen.pkhack.eb.ResetButton
     */
    public void resetArea(int offset, int len, Rom orgRom)
    {
        //only works if neither is direct file IO
        if (!this.isDirectFileIO() && !orgRom.isDirectFileIO())
            System.arraycopy(orgRom.rom, offset, rom, offset, len);
        //otherwise, use normal methods to read/write
        else
            write(offset, orgRom.readByte(offset, len));
    }

    /**
     * Checks if an <code>int[]</code> matches a specified part of the ROM.
     * 
     * @param offset Where in the rom to compare to.
     * @param values What to compare to.
     * @param len How many bytes to compare.
     * @return True if the same, false if different.
     */
    public boolean compare(int offset, int[] values, int len)
    {
        for (int i = 0; i < len; i++)
            if (this.read(offset + i) != values[i])
                return false;
        return true;
    }

    /**
     * Searches for an <code>int[]</code> in the ROM.
     * 
     * @param offset offset to start search at
     * @param values values to search for
     * @param len how many bytes of values to look at
     * @return offset <code>values</code> was found at in the ROM or -1 on
     *         failure.
     */
    public int find(int offset, int[] values, int len)
    {
        for (int i = offset; i < rom.length; i++)
        {
            if (compare(i, values, len))
                return i;
        }
        return -1;
    }

    /**
     * Checks if an <code>int[]</code> matches a specified part of the ROM.
     * 
     * @param offset Where in the rom to compare to.
     * @param values What to compare to.
     * @return True if the same, false if different.
     */
    public boolean compare(int offset, int[] values)
    {
        return compare(offset, values, values.length);
    }

    /**
     * Checks if an <code>byte[]</code> matches a specified part of the ROM.
     * 
     * @param offset Where in the rom to compare to.
     * @param values What to compare to.
     * @param len How many bytes to compare.
     * @return True if the same, false if different.
     */
    public boolean compare(int offset, byte[] values, int len)
    {
        for (int i = 0; i < len; i++)
            if (this.readByte(offset + i) != values[i])
                return false;
        return true;
    }

    /**
     * Checks if an <code>byte[]</code> matches a specified part of the ROM.
     * 
     * @param offset Where in the rom to compare to.
     * @param values What to compare to.
     * @return True if the same, false if different.
     */
    public boolean compare(int offset, byte[] values)
    {
        return compare(offset, values, values.length);
    }

    /**
     * Searches for an <code>byte[]</code> in the ROM.
     * 
     * @param offset offset to start search at
     * @param values values to search for
     * @param len how many bytes of values to look at
     * @return offset <code>values</code> was found at in the ROM or -1 on
     *         failure.
     */
    public int find(int offset, byte[] values, int len)
    {
        for (int i = offset; i < rom.length; i++)
        {
            if (compare(i, values, len))
                return i;
        }
        return -1;
    }

    /**
     * Searches for an <code>byte[]</code> in the ROM.
     * 
     * @param offset offset to start search at
     * @param values values to search for
     * @return offset <code>values</code> was found at in the ROM or -1 on
     *         failure.
     * @see #find(int, byte[], int)
     */
    public int find(int offset, byte[] values)
    {
        return find(offset, values, values.length);
    }

    /**
     * Expands an Earthbound ROM. Will fail if ROM is already expanded or is not
     * Earthbound.
     * 
     * @return True if succesful, false if ROM already expanded or not
     *         Earthbound.
     */
    public boolean expand()
    {
        if ((!getRomType().equals("Earthbound")) || length() == 0x400200)
        {
            return false;
        }

        byte[] out = new byte[this.length() + (4096 * 256)];
        for (int i = 0; i < this.length(); i++)
        {
            out[i] = (byte) read(i);
        }
        for (int j = 0; j < 4096; j++)
        {
            for (int i = 0; i < 255; i++)
            {
                out[((j * 256) + i) + this.length()] = 0;
            }
            out[((j * 256) + 255) + this.length()] = 2;
        }

        rom = out;
        //        isExpanded = true;

        return true;
    }

    //class info functions
    /**
     * Returns a description of this class.
     * 
     * @return A short (one-line) description of this class.
     */
    public static String getDescription() //Return one line description of
    // class
    {
        return "Earthbound ROM wrapper class";
    }

    /**
     * Returns the version of this class as a <code>String</code>. Can have
     * any number of numbers and dots ex. "0.3.3.5".
     * 
     * @return The version of this class.
     */
    public static String getVersion() //Return version as a string that may
    // have more than one decimal point (.)
    {
        return "0.7";
    }

    /**
     * Returns the credits for this class.
     * 
     * @return The credits for this class.
     */
    public static String getCredits() //Return who made it
    {
        return "Written by AnyoneEB\n"
            + "Inspiration for faster file i/o from Cabbage\n"
            + "Idea for direct file IO mode from EBisumaru";
    }

    /**
     * Returns the path of the loaded file as a String. This is changed whenever
     * the ROM is loaded or saved to a different location.
     * 
     * @return The path of the loaded file.
     */
    public String getPath()
    {
        return path.toString();
    }

    /**
     * Returns the path of the loaded file as a File. This is changed whenever
     * the ROM is loaded or saved to a different location.
     * 
     * @return The path of the loaded file.
     */
    public File getFilePath()
    {
        return new File(getPath());
    }

    /**
     * Returns the number of bytes in the ROM.
     * 
     * @return int
     */
    public int length()
    {
        return rom.length;
    }

    /**
     * Returns whether this Rom object writes directly to the filesystem.
     * 
     * @return boolean
     */
    public boolean isDirectFileIO()
    {
        return false;
    }

    /**
     * Returns an {@link IPSFile}object containing the differences between this
     * ROM and the specified ROM. The IPSFile will only contain differences
     * between the byte offsets <code>start</code> and <code>end</code>.
     * 
     * @see #createIPS(Rom)
     * @param orgRom ROM to base patch off of.
     * @param start Byte offset to start looking for differences.
     * @param end Byte offset to stop looking for differences.
     * @return IPSFile
     */
    public IPSFile createIPS(Rom orgRom, int start, int end)
    {
        return IPSFile.createIPS(this.rom, orgRom.rom, start, end);
    }

    /**
     * Returns an {@link IPSFile}object containing the differences between this
     * ROM and the specified ROM.
     * 
     * @see #createIPS(Rom, int, int)
     * @param orgRom ROM to base patch off of.
     * @return IPSFile
     */
    public IPSFile createIPS(Rom orgRom)
    {
        return this.createIPS(orgRom, 0, this.length());
    }

    /**
     * Applies the specified patch to this ROM.
     * 
     * @param ips IPSFile object to get patch from.
     * @return Returns true if successful, false if fails because ROM is not
     *         expanded.
     */
    public boolean apply(IPSFile ips)
    {
        try
        {
            ips.apply(this.rom);
            return true;
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return false;
        }
    }

    /**
     * Unapplies the specified patch from this ROM.
     * 
     * @param ips IPSFile object to get patch from.
     * @param orgRom <code>Rom</code> to read orginal bytes from
     * @return Returns true if successful, false if fails because ROM is not
     *         expanded.
     */
    public boolean unapply(IPSFile ips, Rom orgRom)
    {
        try
        {
            ips.unapply(this.rom, orgRom.rom);
            return true;
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return false;
        }
    }

    /**
     * Checks if the specified patch has been applied to this ROM.
     * 
     * @param ips IPSFile object to get patch from.
     * @return True if applying this patch would have no effect
     */
    public boolean check(IPSFile ips)
    {
        return ips.check(rom);
    }
}