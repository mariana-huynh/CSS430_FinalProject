public class Inode
{
    private final static int iNodeSize = 32;
    private final static int directSize = 11;

    public int length; //file size in bytes
    public short count; //# of file table entries pointing to this
    public short flag; // 0 = unused, 1 = used, ... 
    public short direct[] = new short[directSize]; // pointers to the first 12 blocks
    public short indirect;

    Inode()
    {
        length = 0;
        count = 0;
        flag = 1;
        for(int i = 0; i < directSize; i++)
        {
            direct[i] = -1;
        }
        indirect = -1;
    }

    Inode(short iNumber)
    {
        //retrieving inode from disk
        //first: get the block number where the inode is on the disk there are 16 inodes on the disk

        int blockNum = 1 + iNumber / 16;

        //Check the inode on the disk by reading from it
        byte[] nodeInfo = new byte[Disk.blockSize];
        SysLib.rawread(blockNum, nodeInfo);

        //Then write back it's contents to disk immediately
        //info to be written back/ updated length, count, flag,
        int offset = (iNumber % 16) * 32; //16 inodes per block and 32 bytes per block
        length = SysLib.bytes2int(nodeInfo, offset);
        offset += 4; //int is 4 bytes

        count = SysLib.bytes2short(nodeInfo, offset);
        offset += 2; //short is 2 bytes

        flag = SysLib.bytes2short(nodeInfo, offset);
        offset += 2; //short is 2 bytes

        for(int i = 0; i < directSize; i++)
        {
            //update direct
            direct[i] = SysLib.bytes2short(nodeInfo, offset);
            offset += 2;
        }

        //update indirect
        indirect = SysLib.bytes2short(nodeInfo, offset);
    }

    int toDisk(short iNumber)
    {
        //write from iNumber block to disk?
        //get the block number that corresponds the the iNumber
        int blockNum = 1 + iNumber / 16;

        //Hold Inode info
        byte[] nodeData = new byte[Disk.blockSize];
        int offset = (iNumber % 16) * 32;

        //get this inode data and convert to bytes
        SysLib.int2bytes(length, nodeData, offset);
        offset += 4; //int is 4 bytes

        SysLib.short2bytes(count, nodeData, offset);
        offset += 2; //short is 2 bytes

        SysLib.short2bytes(flag, nodeData, offset);
        offset += 2; //short is 2 bytes

        //convert direct
        for(int i = 0; i < directSize; i++)
        {
            //update direct
            SysLib.short2bytes(direct[i], nodeData, offset);
            offset += 2;
        }
        //covert indirect
        SysLib.short2bytes(indirect, nodeData, offset);

        //write to disk to the length of nodeData
        SysLib.rawwrite(blockNum, nodeData);

        return 0;
    }

    int getBlockNumber(short iNumber)
    {
        return (16 / iNumber) + 1;
    }

    int getSeekPtrBlock(int seek)
    {
        int block = seek / Disk.blockSize;
        //direct blocks = 11, point to data blocks
        if(block < directSize)
        {
            return direct[block];
        }
        else if(indirect != -1) //get blockNum at indirect
        {
            //scan the index block, indirect pointer
            byte[] blockData = new byte[Disk.blockSize];
            SysLib.rawread(indirect, blockData);
            int offset = (block - directSize);

            return SysLib.bytes2short(blockData, offset); //indirect is a short
        }
        else //blockNum is -1, empty at direct and indirect
        {
          return -1;
        }
    }

    //update the block, used with write in FS
    boolean setBlock(int freeBlock)
    {
        int blockNum = (length / Disk.blockSize) + 1;

        if(blockNum < directSize)
        {
            direct[blockNum] = (short) freeBlock;
            return true;
        }
        else if(indirect == -1) //indirect available
        {
            indirect = (short) freeBlock;
            return true;
        }
        else 
        {
            //scan the index block, indirect pointer
            byte[] blockData = new byte[Disk.blockSize];
            SysLib.rawread(indirect, blockData);

            int offset = 0;
            short indirBlockNum = SysLib.bytes2short(blockData, offset); //indirect is a short
            offset += 2; //short is 2 bytes

            //go till the next empty indirect block
            while (indirBlockNum != -1)
            {
                if(offset > Disk.blockSize) //if offset exceeds disk block size
                {
                  return false;
                }

                indirBlockNum = SysLib.bytes2short(blockData, offset);
                offset += 2;
            }

            //get the free block stopped at
            SysLib.short2bytes((short)freeBlock, blockData, offset);

            //update
            SysLib.rawwrite(indirect, blockData);
            return true;
        }
    }
}
