// MARIANA HUYNH
// Holds all the FileTableEntrys (in a vector)
// The FileTable is shared by all threads?
// Allows a user to allocate and delete files within the OS
import java.util.*;

public class FileTable 
{   
    // inode.flag statuses
    private final static int UNUSED = 0;
    private final static int USED = 1;
    private final static int READ = 2;
    private final static int WRITE = 3;
    private final static int DELETE = 4;

    private Vector table;    // actual entity of this file table
    private Directory dir;   // the root dir

    // constructor
    public FileTable (Directory directory)
    {
        table = new Vector<FileTableEntry>();   // instanitate a file (structure) table 
        dir = directory;  // receive a reference to the Directory from the file system
    }

    // major public methods
    public synchronized FileTableEntry falloc (String filename, String mode)
    {
        short iNumber = -1;
        Inode inode = null;

        while (true)
        {
            System.out.println("Start of while(true) in falloc.");

            // allocate/retrieve and register the corresponding inode using dir
            iNumber = (filename.equals("/") ? 0 : dir.namei(filename));   

            System.out.println("iNumber in falloc: " + iNumber);

            // if file exists
            if (iNumber >= 0)
            {
                System.out.println("inside if iNumber >= 0 in falloc.");

                inode = new Inode(iNumber);

                System.out.println("After create new Inode in falloc.");

                if (mode.compareTo("r") == 0)
                {
                    System.out.println("Inside if(r) in falloc.");

                    System.out.println("flag inside falloc: " + inode.flag);

                    // // if flag == read
                    // // talk to charlie: Inode.java should have consts for flags? Or leave as is? Leave as is.
                    // if (inode.flag == READ)    
                    // {
                    //     System.out.println("Inside if (flag == READ) in falloc.");

                    //     break;   // no need to wait
                    // }

                    // // should this be here?
                    // try 
                    // {
                    //     wait();
                    // }
                    // catch (InterruptedException e)
                    // {}

                    if (inode.flag == UNUSED || inode.flag == USED)
                    {
                        System.out.println("inside if unused or used in falloc.");

                        inode.flag = USED;
                        break;
                    }
                    else
                    {
                        System.out.println("inside else unused or used in falloc.");

                        try 
                        {
                            wait();
                        }
                        catch (InterruptedException e)
                        {}

                        continue;
                    }
                }
                else
                {
                    if (inode.flag == DELETE)
                    {
                        iNumber = -1;
                        return null;
                    }

                    if (inode.flag == UNUSED || inode.flag == USED)
                    {
                        break;
                    }

                    if (inode.flag == WRITE)
                    {
                        inode.flag = READ;
                        break;
                    }

                    if (inode.flag == READ)
                    {
                        inode.flag = WRITE;
                        break;
                    }

                    try
                    {
                        wait();
                    }
                    catch (InterruptedException e)
                    {}
                }
            }
            else if (mode.compareTo("r") != 0)
            {
                System.out.println("got to else if in falloc.");

                iNumber = dir.ialloc(filename);
                inode = new Inode(iNumber);
                inode.flag = WRITE;
                break;
            }
            else  // the system want to read file that doesn't exist
            {
                return null;
            }
        }

        inode.count++;

        System.out.println("before toDisk in falloc.");
        inode.toDisk(iNumber);
        System.out.println("after toDisk in falloc.");

        FileTableEntry e = new FileTableEntry(inode, iNumber, mode);
        table.addElement(e);
        return e;
    }

    public synchronized boolean ffree (FileTableEntry e)
    {
        if (e == null)
        {
            return true;
        }

        if (table.removeElement(e))
        {
            // Inode inode = e.inode;
            Inode inode = new Inode(e.iNumber);

            if (inode.flag == READ || inode.flag == WRITE)
            {
                inode.flag = USED;
            }
            else
            {
                inode.flag = UNUSED;
            }

            inode.count--;
            inode.toDisk(e.iNumber);
            notify();
            e = null;
            return true;
        }

        return false;
    }

    public synchronized boolean fempty()
    {
        // return if table is empty
        // should be called before starting a format
        return table.isEmpty();   
    }
}
