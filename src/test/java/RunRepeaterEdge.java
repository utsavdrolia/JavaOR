import edu.cmu.edgecache.objrec.rpc.RepeaterEdge;
import org.opencv.core.Core;

import java.io.IOException;

/**
 * Created by utsav on 2/5/16.
 */
public class RunRepeaterEdge
{
    public static void main(String args[]) throws IOException, InterruptedException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length == 2)
        {
            // FOr Repeater Edge
            String myaddress = args[0];
            String serverAdd= args[1];

            new RepeaterEdge(myaddress, serverAdd);

            while(System.in.available() == 0)
            {
                Thread.sleep(1000);
            }
        }
        else
        {
            System.out.println("2 Args required, provided " + args.length);
        }
        System.err.println("Exiting");
        System.exit(0);
    }
}
