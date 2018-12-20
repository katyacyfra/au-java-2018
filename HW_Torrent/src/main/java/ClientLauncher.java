import asg.cliche.ShellFactory;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class ClientLauncher {

    public static void main(String [] arg) throws IOException, ParseException {
        if (arg.length !=1) {
            System.out.println("Please, specify client port number");
            return;
        }
            short port = Short.parseShort(arg[0]);
            ShellFactory.createConsoleShell("torrent", "", new Client(port))
                    .commandLoop();

    }

}