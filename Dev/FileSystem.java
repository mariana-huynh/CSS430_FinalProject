// Mariana: deallocAllBlocks(), delete(), format(), sync()

public class FileSystem
{
    private SuperBlock superblock;
    private Directory directory;
    private FileTable filetable;
    // private<Inode> nodes;

    public FileSystem(int diskBlocks)
    {
        // create superblock
        superblock = new SuperBlock(diskBlocks);

        //create directory, register "/" in directory entry 0
        directory = new Directory(superblock.inodeBlocks);

        //file table is created, and store directory in the file table
        filetable = new FileTable(directory);

        System.out.println("Before open() in FileSystem constructor.");
        FileTableEntry dirEnt = open("/", "r");
        System.out.println("After open() in FileSystem constructor.");


        int dirSize = fsize(dirEnt);
        if(dirSize > 0)
        {
            byte[] dirData = new byte[dirSize];
            read(dirEnt, dirData);
            directory.bytesToDirectory(dirData);
        }
        close(dirEnt);
    }

    boolean close (FileTableEntry ftEnt)
    {
        return filetable.ffree(ftEnt);
    }

    void sync()
    {
        FileTableEntry tempFTE = open("/", "w");   // open the fte of the current working directory
        byte[] dirData = directory.directoryToBytes();  // translate directory data to bytes
        write (tempFTE, dirData);   // write back to disk all data from directory
        close (tempFTE);   // close the fte when you are done
        superblock.sync();   
    }

    boolean format(int files)
    {
        // if the file table is in use
        if (!filetable.fempty())
        {
            return false;
        }

        System.out.println("before format() in FileSystem.");
        superblock.format(files);
        System.out.println("after format() in FileSystem.");
        
        // everything reset: create a new FileTable and Directory
        directory = new Directory(superblock.inodeBlocks);
        filetable = new FileTable(directory);
        
        return true;
    }

    FileTableEntry open(String filename, String mode)
    {
        System.out.println("Got to open() in FileSystem.java.");

        //falloc allocates a new file(structure) table entry for the file name
        FileTableEntry fileEntry = filetable.falloc(filename, mode);

        System.out.println("After falloc in open().");

        //SysLib.open() must return a negative number as an error value
        // if the file does not exist in the mode "r".
        
        if (fileEntry == null)
        {
            return null;
        }
        
        if(fileEntry.mode == "w")
        {
            deallocAllBlocks(fileEntry);
            // return null;
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
        // while (ftEnt.seekPtr < fsize(ftEnt) && buffSize > 0)
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
        System.out.println("top of write() in FileSystem.");

        //check if buffer is empty
        if(buffer == null || ftEnt == null)
        {
            System.out.println("inside of if buffer null or ftEnt null in write()");
            return -1;
        }
        //check for invalid mode
        if(ftEnt.mode.equals("r"))
        {
            System.out.println("inside of if mode == r in write()");
            return -1;
        }
        int leftToWrite;
        //int readTotal = 0;
        int writtenTotal = 0;
        int sizeWrite = 0;
        int buffSize = buffer.length;
        while(buffSize > 0)
        {
            // System.out.println("inside of while in write()");

            //get the block where the ftEnt seek Ptr is at
            //need the block number for rawwrite

            int blockNum = ftEnt.inode.getSeekPtrBlock(ftEnt.seekPtr); //get block number where seekPtr is at

            // System.out.println("adter getSeekPtrBlock() in write()");

            int availableBlock = superblock.getFreeBlock(); //get available block to write to

            // System.out.println("after getFreeBlock() in write()");

            ftEnt.inode.setBlock(availableBlock); //set available block

            // System.out.println("after setBlock() in write()");

            //read to figure out where to write to
            byte[] data = new byte[Disk.blockSize];
            SysLib.rawread(blockNum, data); //get data at current block
            int dataRead = ftEnt.seekPtr % Disk.blockSize; //get data read

            //find out how much more left to read
            int remainingBlocks = Disk.blockSize - dataRead; //remaining blocks to read
            
            sizeWrite = remainingBlocks - ftEnt.seekPtr; //remaining data left in file

            System.out.println("fsize(ftEnt): " + fsize(ftEnt));

            //if there is still blocks to be read, set left to read to remaining blocks
            if(remainingBlocks < buffSize)
            {
                sizeWrite = remainingBlocks;
            }
            //if there is left to read on the file
            else
            {
                sizeWrite = buffSize; //just set it to the remainder of file data
            }

            System.out.println("sizeWrite: " + sizeWrite);

            System.arraycopy(buffer, writtenTotal, data, dataRead, sizeWrite);
            SysLib.rawwrite(blockNum, data);

            writtenTotal += sizeWrite;//increment bytes written

            ftEnt.seekPtr += sizeWrite; //update seekPtr

            buffSize -= sizeWrite;
        }

        System.out.println("after while  in write()");

        return writtenTotal;
    }

    private boolean deallocAllBlocks(FileTableEntry ftEnt)
    {
        boolean deallocated = false;
        
        // return false if ftEnt is null, or ftEnt inode is null, or if the inode is being used
        if (ftEnt == null || ftEnt.inode == null || ftEnt.inode.count > 1)
        {
            return false;
        }

        // deallocate the direct blocks
        // inode.length = end of file?
        // each inode has 11 direct pointers and 1 indirect pointer
        for (short i = 0; i < 11; i++)
        {
            if (ftEnt.inode.direct[i] == -1)   // block location is invalid
            {
                continue;  
            }
            else
            {
                superblock.returnBlock(ftEnt.inode.direct[i]);
                ftEnt.inode.direct[i] = -1;
            }
        }

        // deallocate the indirect block
        byte[] blockData;

        if (ftEnt.inode.indirect == -1)
        {
            blockData = new byte[Disk.blockSize];
            SysLib.rawread(ftEnt.inode.indirect, blockData);
            ftEnt.inode.indirect = -1;
        }
        else
        {
            blockData = null; 
        }

        if (blockData != null)
        {
            int offset = 0;
            short block = SysLib.bytes2short(blockData, offset);
            while (block != -1)
            {
                superblock.returnBlock(block);
                block = SysLib.bytes2short(blockData, offset);
            }

            superblock.returnBlock(ftEnt.inode.indirect);
        }

        // write inode to disk
        ftEnt.inode.toDisk(ftEnt.iNumber);

        return true;
    }

    boolean delete(String filename)
    {
        boolean deleted = false;

        if (filename == null || (filename.compareTo("") == 0))
        {
            return deleted;
        }

        FileTableEntry tempFTE = open(filename, "w");
        if (tempFTE != null)
        {
            tempFTE.inode.flag = 4;  // 4 = delete
            directory.ifree(tempFTE.iNumber);
            filetable.ffree(tempFTE);
            deleted = true;
        }

        return deleted;
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

