 // System maintained table shared among all user threads
// Each entry maintains a seek pointer and the inode number of a file
// Position of seek pointer dependent on access mode
// Also keeps track of next postion to read/write to

public class FileTableEntry 
{
    public int seekPtr;    // file seek pointer
    public final Inode inode;    // reference to its inode
    public final short iNumber;    // this inode number
    public int count;    // num threads sharing this entity
    public final String mode;    // "r", "w", "w+", or "a"

    // modes
    public static final String READ = "r";
    public static final String WRITE = "w";
    public static final String READ_WRITE = "w+";
    public static final String APPEND = "a";
    
    // constructor
    public FileTableEntry (Inode i, short inumber, String m)
    {
        seekPtr = 0;    // the seek pointer is set to the file top
        inode = i;
        iNumber = inumber;
        count = 1;    // at least on thread is using this entry
        // mode = m;    // once access mode is set, it never changes

        if (m.compareTo("r") == 0)
        {
            mode = READ;
        }
        else if (m.compareTo("w") == 0)
        {
            mode = WRITE;
        }
        else if (m.compareTo("w+") == 0)
        {
            mode = READ_WRITE;
        }
        else if (m.compareTo("a") == 0)
        {
            mode = APPEND;
            seekPtr = inode.length;    // seekPtr points to the end of file
        }
        else
        {
            mode = null;
        }
    }
}
