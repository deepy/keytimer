import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KeyTimer implements NativeKeyListener{
    private int lastKey;
    private String lastKeyString;
    private long lastKeyStroke;
    private Connection conn;

    private PreparedStatement insert;

    public Connection getConnection() {
        try {
            //Connection con = DriverManager.getConnection("jdbc:h2:mem:");
            Connection con = DriverManager.getConnection("jdbc:h2:~/timings", "sa", "");
            return con;
        } catch (Exception e) {
            return null;
        }
    }

    public void nativeKeyPressed(NativeKeyEvent e) {

        if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
            try {
                GlobalScreen.unregisterNativeHook();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        long currentKeyStroke = System.currentTimeMillis();
        int currentKeyCode = e.getKeyCode();
        String currentKeyString = NativeKeyEvent.getKeyText(currentKeyCode);
        long diff = currentKeyStroke - lastKeyStroke;
        if (lastKey != 0 && lastKey != 15 && lastKey != 56 && diff < 200) {
            System.out.println(lastKeyString+ ":"+currentKeyString+" - "+(currentKeyStroke-lastKeyStroke));
            try {
                insert.setString(1, lastKeyString);
                insert.setString(2, currentKeyString);
                insert.setLong(3, diff);
                insert.execute();
            } catch (Exception ex) {}
        }
        lastKey = e.getKeyCode();
        lastKeyString = NativeKeyEvent.getKeyText(lastKey);
        lastKeyStroke = System.currentTimeMillis();
    }

    public void setup() throws Exception {
        conn = getConnection();
        conn.prepareCall("CREATE TABLE IF NOT EXISTS timings (id bigint auto_increment, key1 char(1), key2 char(2), diff int)").execute();
        insert = conn.prepareStatement("INSERT INTO timings (key1, key2, diff) values(?, ?, ?)");
    }

    public void nativeKeyTyped(NativeKeyEvent e) {}

    public static void main(String[] args) throws Exception {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        Handler[] handlers = Logger.getLogger("").getHandlers();
        for (int i = 0; i < handlers.length; i++) {
            handlers[i].setLevel(Level.OFF);
        }

        try {
            GlobalScreen.registerNativeHook();
        }
        catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());

            System.exit(1);
        }

        KeyTimer timer = new KeyTimer();
        timer.setup();

        GlobalScreen.addNativeKeyListener(timer);
    }
}
