
import java.nio.charset.StandardCharsets;

public class Directory
{
    private static int maxChars = 30; // 60 bytes

    //Directory entries
    private int fsize[]; //each element stores a different file size
    private char fNames[][]; //each element stores a different file name

    public Directory(int maxInumber)
    {
        fsize = new int[maxInumber]; //maxInumber = max files
        for(int i = 0; i < maxInumber; i++)
        {
            fsize[i] = 0; //all file size initialized to 0
        }
        fNames = new char[maxInumber][maxChars];
        String root = "/"; //entry(inode) 0 is "/"
        fsize[0] = root.length();
        root.getChars(0, fsize[0],fNames[0],0 ); //fNames[0] includes "/"

    }
    public int bytesToDirectory(byte data[])
    {
        //assumes data[] received directory information from disk
        //initializes the Directory instance with this data[]


        int offset = 0;
        //convert byte array
        for(int i = 0; i < fsize.length; i++)
        {
            //use the SysLib bytesToInt
            SysLib.bytes2int(data, offset);
            offset += 4;

        }
        //populate the directory
        for(int j = 0; j < fNames.length; j++)
        {
            //each file name is 60 bytes
            //gets the name
            String fName = new String(data, offset, maxChars * 2);
            //get the characters
            fName.getChars(0, fsize[j], fNames[j], 0);
            //update the offset

            offset += maxChars*2;

        }



        return 0;

    }

    public byte[] directoryToBytes()
    {
        //converts and return Directory information into a plain byte array
        //this byte array will be written back to disk
        //only meaningful directory information should be converted into bytes

        int size = (fsize.length * 4) + (fNames.length * 60); //the size of all files + the size of each file name(60 bytes max);
        byte[] data = new byte[size]; //would be the size of whole directory byte array
        // data is the resulting array
        // i is the value
        //offset is index where to start to store byte
        //loop that goes through each file size

        //Convert the file data
        int offset = 0;
        for(int i = 0; i < fsize.length; i++)
        {
            int num = fsize[i];
            SysLib.int2bytes(num, data, offset);
            offset += 4; //shift over 4 bytes because num is converted into 4 bytes
        }


        //convert file names
        for(int j = 0; j < fNames.length; j++)
        {
            String part = fNames[j].toString();
            byte[] nameData = part.getBytes();
            System.arraycopy(nameData, 0, data, offset, nameData.length);

            offset += 1; // one byte in a char

        }
        return data;

    }

    public short ialloc(String filename)
    {
        //filename is the one of a file to be created
        //allocates a new inode number for this filename
    }

    public boolean ifree(short iNumber)
    {
        //deallocates this inumber(inode number)
        //the corresponding file will be deleted
    }

    public short namei(String filename)
    {

    }




}
