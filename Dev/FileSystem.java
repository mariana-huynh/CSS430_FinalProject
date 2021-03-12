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
        if(buffer == null || ftEnt == null)
        {
            return -1;
        }
        //check for invalid mode
        if(ftEnt.mode.equals("w") || ftEnt.mode.equals("w+") || ftEnt.mode.equals("a"))
        {
            return -1;
        }
        int sizeRead = 0;
        int readTotal = 0;
        int buffSize = buffer.length;
        //If bytes remaining between the current seek pointer and the end of file are less than buffer.length
        while(ftEnt.seekPtr < buffer.length)
        {

            //need the block number for rawread
            byte[] data = new byte[Disk.blockSize]; // keep track of what was read

            //get the block where the ftEnt seek Ptr is at, should be set to 0 for read
            int blockNum = ftEnt.inode.getSeekPtrBlock(ftEnt.seekPtr);

            //reads up to buffer.size
            SysLib.rawread(blockNum, buffer);
            int dataRead = ftEnt.seekPtr % Disk.blockSize;

            //need to keep track of remaining blocks to be read from disk that will
            //where data will make up buffer
            int remainingBlocks = Disk.blockSize - dataRead; //remaining blocks to read
            int remainingData = fsize(ftEnt) - ftEnt.seekPtr; //remaining data left in file

            //if there is still blocks to be read, set left to read to remaining blocks
            if(remainingBlocks < buffSize)
            {
                sizeRead = remainingBlocks; //size left to read


            }
            //if there is left to read on the file
            else
            {
                sizeRead = remainingData; //just set it to the remainder of file data


            }
            //copy data read
            System.arraycopy(data, dataRead, buffer, readTotal, sizeRead);
            readTotal += sizeRead; //update what was currently read

            //update seekPtr
            ftEnt.seekPtr += sizeRead; //just read so can be updated

            buffSize -= sizeRead; //subtract what was read from buffer size



        }
        return readTotal;


    }
    int write(FileTableEntry ftEnt, byte[] buffer)
    {
        //check if buffer is empty
        if(buffer == null || ftEnt == null))
        {
            return -1;
        }
        //check for invalid mode
        if(ftEnt.mode.equals("r"))
        {
            return -1;
        }
        int leftToWrite;
        //int readTotal = 0;
        int writtenTotal = 0;
        int sizeWrite = 0;
        int buffSize = buffer.length;
        while(buffSize > 0)
        {
            //get the block where the ftEnt seek Ptr is at
            //need the block number for rawwrite


            int blockNum = ftEnt.inode.getSeekPtrBlock(ftEnt.seekPtr); //get block number where seekPtr is at
            int availableBlock = superblock.getFreeBlock(); //get available block to write to
            ftEnt.inode.setBlock(availableBlock); //set available block

            //read to figure out where to write to
            byte[] data = new byte[Disk.blockSize];
            SysLib.rawread(blockNum, data); //get data at current block
            int dataRead = ftEnt.seekPtr % Disk.blockSize; //get data read

            //find out how much more left to read
            int remainingBlocks = Disk.blockSize - dataRead; //remaining blocks to read
            int remainingData = fsize(ftEnt) - ftEnt.seekPtr; //remaining data left in file
            //if there is still blocks to be read, set left to read to remaining blocks
            if(remainingBlocks < buffSize)
            {
                sizeWrite = remainingBlocks;


            }
            //if there is left to read on the file
            else
            {
                sizeWrite = remainingData; //just set it to the remainder of file data


            }
            System.arraycopy(buffer, writtenTotal, data, dataRead, sizeWrite);
            SysLib.rawwrite(blockNum, data);

            writtenTotal += sizeWrite;//increment bytes written

            ftEnt.seekPtr += sizeWrite; //update seekPtr

            buffSize -= sizeWrite;


        }
        return writtenTotal;



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
        return ftEnt.seekPtr;




    }

}
