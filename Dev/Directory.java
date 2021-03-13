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
    //sets size of each file and it's name
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
        
        //the size of all fsize*4(4 bytes in an int) + fNames size * maxChars * 2(char is 2 bytes)
        int size = (fsize.length * 4) + (fNames.length * (maxChars * 2)); 
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

            String part = new String(fNames[j], 0, fsize[j]);
            byte[] nameData = part.getBytes();
            System.arraycopy(nameData, 0, data, offset, nameData.length);

            offset += maxChars * 2; // one byte in a char

        }
        return data;

    }

    public short ialloc(String filename)
    {
        //filename is the one of a file to be created
        //allocates a new inode number for this filename
        if(filename.length() > maxChars)
        {
            return -1;

        }
        for(int i = 0; i < fsize.length; i++)
        {
            if(fsize[i] == 0)
            {
                for(int j = 0; j < filename.length(); j++)
                {
                    fNames[i][j] = filename.charAt(j);
                }
                fsize[i] = filename.length();


            }
        }
        return 0;
    }

    public boolean ifree(short iNumber)
    {
        //deallocates this inumber(inode number)
        //the corresponding file will be deleted
        int size = fsize[iNumber];
        fsize[iNumber] = 0;
        for(int i = 0; i < size; i++)
        {
            fNames[iNumber][i] = 0;

        }
        return true;

    }

    public short namei(String filename)
    {
        //find the file name in fNames that matches filename and return the index?
        for(int i = 0; i < fsize.length; i++) // loop through each file size
        {
            if(filename.length() == fsize[i]) //same length
            {
                //get the fName string and see if it equals filename
                //sets string to the chars between where fname is to the length
                String fname = new String(fNames[i], 0, fsize[i]);
                if(fname.equals(filename))
                {
                    return (short)i;
                }

            }

        }
        return -1;

    }


}
