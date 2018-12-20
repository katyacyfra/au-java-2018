import org.json.simple.parser.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


public class TorrentTest {

    private Tracker tracker = new Tracker();
    private ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static ExecutorService executor = Executors.newFixedThreadPool(5);


    private String getMD5(String filePath) throws IOException, NoSuchAlgorithmException {
        try(FileInputStream inputStream = new FileInputStream(filePath))
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileChannel channel = inputStream.getChannel();
            ByteBuffer buff = ByteBuffer.allocate(2048);
            while (channel.read(buff) != -1) {
                buff.flip();
                md.update(buff);
                buff.clear();
            }
            byte[] hashValue = md.digest();
            return FileUtility.bytesToHex(hashValue);
        }
    }

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        Thread trackerThread = new Thread(()-> {

            try {
                tracker.main(null);
            } catch (IOException e) {
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        trackerThread.start();

    }


    @Test
    public void testTorrent() throws IOException, ParseException, InterruptedException, NoSuchAlgorithmException {
        Client client = new Client((short) 6881);
        outContent.reset();
        client.list();
        assertEquals(
                "Total 0 files:\n" +
                        "ID    Name    Size\n", outContent.toString());
        outContent.reset();

        File fi = new File("_info");
        assertTrue(fi.exists());

        client.upload("./example/test.txt");
        TimeUnit.MILLISECONDS.sleep(5);
        client.list();
        assertEquals(
                "Total 1 files:\n" +
                        "ID    Name    Size\n" +
                        "1    test.txt    7590\n", outContent.toString());
        outContent.reset();

        File ffi = new File("_fileInfo");
        assertTrue(ffi.exists());

        TimeUnit.SECONDS.sleep(70);//waiting for update
        client.source(1);
        assertEquals("127.0.0.1:6881\n", outContent.toString());
        outContent.reset();


        client.download(3);
        assertEquals("No such file!\n", outContent.toString());
        outContent.reset();

        File f = new File("test.txt");
        assertFalse(f.exists());
        String hash = getMD5("./example/test.txt");
        client.download(1);//downloading to root directory
        TimeUnit.MILLISECONDS.sleep(1);
        assertEquals("test.txt was successfully downloaded\n", outContent.toString());

        outContent.reset();

        assertTrue(f.exists());
        assertEquals(hash, getMD5("test.txt"));
        client.exit();

    }


    @After
    public void tearDown() throws IOException, InterruptedException {
        System.setOut(System.out);
        TimeUnit.SECONDS.sleep(1);
        tracker.exit();

        File inf = new File("_info");
        inf.delete();
        File finf = new File("_fileInfo");
        finf.delete();

        File downloaded = new File("test.txt");
        downloaded.delete();
    }
 }
