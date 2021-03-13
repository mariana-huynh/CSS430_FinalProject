// Mariana: deallocAllBlocks(), delete(), format(), sync()

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

        superblock.format(files);
        
        // everything reset: create a new FileTable and Directory
        directory = new Directory(superblock.totalInodes);
        filetable = new FileTable(directory);
        
        return true;
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
        boolean deallocated = false;
        
        // return false if ftEnt is null, or ftEnt inode is null, or if the inode is being used
        if (ftEnt == null || ftEnt.inode == null || inode.count > 1)
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

            superBlock.returnBlock(ftEnt.inode.indirect);
        }

        // write inode to disk
        ftEnt.inode.toDisk(ftEnt.iNumber);

        return true;
    }

    boolean delete(String filename)
    {
        boolean deleted = false;

        if (filename == null || (filename.compareTo("") == 0)
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
        else
        {

        }




    }

}
