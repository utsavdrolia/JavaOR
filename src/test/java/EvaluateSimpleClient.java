import edu.cmu.edgecache.objrec.rpc.ObjRecClient;
import org.opencv.core.Core;

import java.io.IOException;

/**
 * Created by utsav on 2/5/16.
 */
public class EvaluateSimpleClient
{
    public static void main(String args[]) throws IOException, InterruptedException
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        if (args.length == 3)
        {
            String queryList = args[0];
            String resultspath = args[1];
            String serverAdd = args[2];

            ObjRecClient objRecClient = new ObjRecClient(serverAdd);

            Util.evaluate(objRecClient, queryList, resultspath);
        }
        else
        {
            System.out.println("3 Args required, only provided " + args.length);
        }
        System.exit(0);
    }
}
