public class FileSystem
{
    private SuperBlock superblock;
    private Directory directory;
    private FileTable filetable;
    private<Inode> nodes;

    public FileSystem(int diskBlocks)
    {
        //create directory, register "/" in directory entry 0
        directory = new Directory(superblock.inodeBlocks);

        //file table is created, and store directory in the file table
        filetable = new FileTable(directory);

        FileTableEntry dirEnt = open("/", "r");
        int dirSize = fsize(dirEnt);
        if(dirSize > 0)
        {
            byte[] dirData = new byte[dirSize];
            read(dirEnt, dirData);
            directory.bytes2directory(dirData);
        }
        close(dirEnt);
    }
    void sync()
    {

    }
    boolean format(int files)
    {

    }
    FileTableEntry open(String filename, String mode)
    {
        //falloc allocates a new file(structure) table entry for the file name
        FileTableEntry fileEntry = filetable.falloc(filename, mode);

        //SysLib.open() must return a negative number as an error value
        // if the file does not exist in the mode "r".
        if(fileEntry.mode != "r")
        {
            return null;
        }

        return fileEntry; //return file descriptor


    }
    int fsize(FileTableEntry ftEnt)
    {
        //Inode holds file size in bytes
        return ftEnt.inode.length;


    }
    /*
    Reads up to buffer.length bytes from the file indicated by the file descriptor fd,
    starting at the position currently pointed to by the seek pointer. If bytes remaining between
    the current seek pointer and the end of file are less than buffer.length:
        SysLib.read reads as many bytes as possible and puts them into the beginning of buffer.
        It increments the seek pointer by the number of bytes to have been read.
        The return value is the number of bytes that have been read, or a negative value upon an error
     */
    int read(FileTableEntry ftEnt, byte[] buffer)
    {
        //check if buffer is empty
        if(buffer == null)
        {
            return -1;
        }
        //check for invalid mode
        if(ftEnt.mode.equals("w") || ftEnt.mode.equals("w+") || ftEnt.mode.equals("a"))
        {
            return -1;
        }
        //If bytes remaining between the current seek pointer and the end of file are less than buffer.length
        while(ftEnt.seekPtr < buffer.length)
        {

            //get the block where the ftEnt seek Ptr is at
            //need the block number for rawread

            //blockNumber = block number where ftEnt seekPtr is at
            //SysLib.rawread(blockNum, buffer);
            //int dataRead = ftEnt.seekPtr
            //int blocksLeft
            //int remainingData
            //increment the number of of bytes that have been read


            //

        }




    }
    int write(FileTableEntry ftEnt, byte[] buffer)
    {
        //check if buffer is empty
        if(buffer == null)
        {
            return -1;
        }
        //check for invalid mode
        if(ftEnt.mode.equals("r"))
        {
            return -1;
        }
        while(buffer.length > 0)
        {
            //get the block where the ftEnt seek Ptr is at
            //need the block number for rawwrite

            //blockNumber = block number where ftEnt seekPtr is at
            //SysLib.rawwrite(blockNumber, buffer);
            //int dataWritten = ftEnt.seekPtr
            //int blocksLeft
            //int remainingData


            //increment the number of of bytes that have been read

        }


    }
    private boolean deallocAllBlocks(FileTableEntry ftEnt)
    {

    }
    boolean delete(String filename)
    {

    }

    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;

    int seek(FileTableEntry ftEnt, int offset, int whence)
    {

        // the file's seek pointer is set to offset bytes from the beginning of the file
        if(whence == SEEK_SET)
        {
            ftEnt.seekPtr = offset;

        }
        else if(whence == SEEK_CUR)
        {
            ftEnt.seekPtr += offset;

        }
        else if(whence == SEEK_END)
        {

            ftEnt.seekPtr =  fsize(ftEnt) + offset;

        }
        else
        {

        }




    }

}
