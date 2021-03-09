
public class Inode
{
    private final static int iNodeSize = 32;
    private final static int directSize = 11;

    public int length; //file size in bytes
    public short count; //# of file table entries pointing to this
    public short flag; // 0 = unused, 1 = used
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

        int blockNum = (16 / iNumber) + 1;

        //Check the inode on the disk by reading from it
        byte[] nodeInfo = new byte[Disk.blockSize]; // get info from disk
        SysLib.rawread(blockNum, nodeInfo);
        //Then write back it's contents to disk immediately
        int offset = iNumber * 32 //16 inodes per block and 32 bytes per block
        length = SysLib.bytes2int(nodeInfo, offset);

        //count =
        //flag =

        for(int i = 0; i < directSize; i++)
        {
            //update direct
        }
        //update indirect

        //info to be written back/ updated length, count, flag,

    }
    int toDisk(short iNumber)
    {

    }



}
