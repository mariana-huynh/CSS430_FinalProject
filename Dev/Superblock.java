// MARIANA HUYNH
// Disk 0 is the superblock
// It is used to describe:
//      Number of disk blocks
//      Number of inodes
//      Block number of the head block of the free list

public class SuperBlock 
{
    public final static int DEFAULT_INODES = 64; 

    public int totalBlocks;    // number of disk blocks
    public int inodeBlocks;    // number of inodes
    public int freeList;     // block number of the free list's head

    public SuperBlock (int disksize)
    {
        // read the superblock from the disk
        byte[] superBlock = new byte[Disk.blockSize];
        SysLib.rawread(0, superBlock);
        // converts num bytes in superBlock to an int
        // offset needs to be multiples of 4 as the num bytes in an int is 4
        totalBlocks = SysLib.bytes2int(superBlock, 0);   
        inodeBlocks = SysLib.bytes2int(superBlock, 4);
        freeList = SysLib.bytes2int(superBlock, 8);

        if (totalBlocks == disksize && inodeBlocks > 0 && freeList >= 2)
        {
            // disk contents are valid
            return;
        }
        else
        {
            // need to format disk
            totalBlocks = disksize;
            format(DEFAULT_INODES);
        }
    }

    // format()
    public void format(int numInodeBlocks) 
    {
        // reset disk
        // sync: write superblock to disk (the three values) -> call at end
        // erase current inodes and files and allocate new inodes

        byte[] blockData = new byte[Disk.blockSize];

        inodeBlocks = numInodeBlocks;

        for (int i = 0; i < inodeBlocks; i++)
        {
            Inode newInode = new Inode();
            newInode.flag = 0;  // UNUSED
            newInode.toDisk((short) i);
        }

        freeList = inodeBlocks / 16 + 1;

        // create new free blocks and write to disk
        for (int i = freeList; i < totalBlocks; i++)
        {
            SysLib.int2bytes(i + 1, blockData, 0);
            SysLib.rawwrite(i, blockData);
        }

        // sync the SuperBlock to the disk once formatted
        sync();
    }

    // sync()
    // write back totalBlocks, inodeBlocks, and freeList to disk
    public void sync()
    {
        // create new byte array (everything on disk is bytes)
        byte[] blockData = new byte[Disk.blockSize];

        SysLib.int2bytes(totalBlocks, blockData, 0);
        SysLib.int2bytes(inodeBlocks, blockData, 4);    // int = 4 bytes
        SysLib.int2bytes(freeList, blockData, 8);

        SysLib.rawwrite(0, blockData);
    }

    // getFreeBlock()
    // dequeue the top block from the free list
    public int getFreeBlock()
    {
        int freeBlocks = freeList;

        if (freeList < 0 || freeList > totalBlocks)
		{
			return -1;			
		}
    
        byte[] blockData = new byte[Disk.blockSize];
        SysLib.rawwrite(freeList, blockData);
        // get the next free block
        freeList = SysLib.bytes2int(blockData, 0);

        return freeBlocks;
    }

    // returnBlock(int blockNumber)
    // enqueue a given block to the end of the free list 
    public void returnBlock(int blockNumber)
    {
        if (blockNumber < 0 || blockNumber > totalBlocks)
        {
            return;
        }
        else
        {
            byte[] blockData = new byte[Disk.blockSize];

            SysLib.int2bytes(freeList, blockData, 0);
            SysLib.rawwrite(blockNumber, blockData);
            freeList = blockNumber;
        }
    }
}

