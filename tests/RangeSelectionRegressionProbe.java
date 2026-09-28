import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import javax.swing.*;
import com.github.rebi0395.instantsnap.view.swing.dialog.RangeSelectionDialog;
import com.github.rebi0395.instantsnap.view.swing.panel.RangeSelectionPanel;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.common.MessageKeyConverter;
import com.github.rebi0395.instantsnap.listener.RangeSelectionListener;

/** モニター間の画面再利用、撮影座標、状態遷移、通知、終了時の解放を検証する。 */
public class RangeSelectionRegressionProbe {
    static class Events implements RangeSelectionListener {
        int shown, closed, updates;
        Rectangle area;
        public void onShown() { shown++; }
        public void onClosed() { closed++; }
        public void updateRangeArea(Rectangle area) { updates++; this.area = area; }
    }
    static Object field(Object obj, String name) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }
    static Object call(Object obj, String name, GraphicsDevice device) throws Exception {
        Method m = obj.getClass().getDeclaredMethod(name, GraphicsDevice.class);
        m.setAccessible(true);
        return m.invoke(obj, device);
    }
    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    static void click(RangeSelectionPanel panel, int x, int y) {
        panel.dispatchEvent(new MouseEvent(panel, MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(), InputEvent.BUTTON1_DOWN_MASK,
                x, y, 1, false, MouseEvent.BUTTON1));
    }
    public static void main(String[] args) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                RangeSelectionDialog mode = null;
                try {
                    GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
                    GraphicsDevice pointerScreen = MouseInfo.getPointerInfo().getDevice();
                    GraphicsDevice otherScreen = null;
                    for (GraphicsDevice screen : screens) {
                        if (!screen.equals(pointerScreen)) otherScreen = screen;
                    }
                    System.out.println("Detected monitors: " + screens.length);
                    ConfigBean config = new ConfigBean();
                    config.setIsAutoSnapAfterRangeSelectionEnabled(false);
                    Events events = new Events();
                    mode = new RangeSelectionDialog(config, new MessageKeyConverter(LanguageType.ENGLISH), events);
                    call(mode, "showRangeSelectionDialog", pointerScreen);
                    JDialog first = (JDialog) field(mode, "dialog");
                    Timer timer = (Timer) field(mode, "screenMonitorTimer");
                    check(first != null && first.isShowing(), "initial dialog visible");
                    check(events.shown == 1 && events.closed == 0, "initial mode notification");
                    check(timer != null && timer.isRunning(), "monitor timer started");

                    // 同じモニターへの要求では作り直さない。
                    call(mode, "changeSelectionScreen", pointerScreen);
                    check(field(mode, "dialog") == first, "same screen is a no-op");

                    // 作成失敗時に画面・タイマー・通知先の範囲を維持する。
                    GraphicsDevice broken = new GraphicsDevice() {
                        public int getType() { return TYPE_RASTER_SCREEN; }
                        public String getIDstring() { return "broken-test-device"; }
                        public GraphicsConfiguration[] getConfigurations() { return new GraphicsConfiguration[0]; }
                        public GraphicsConfiguration getDefaultConfiguration() { throw new IllegalStateException("expected test failure"); }
                    };
                    Rectangle retained = new Rectangle(11, 22, 33, 44);
                    events.area = retained;
                    call(mode, "changeSelectionScreen", broken);
                    check(field(mode, "dialog") == first && first.isShowing(), "failure retains old dialog");
                    check(field(mode, "screenMonitorTimer") == timer && timer.isRunning(), "failure retains timer");
                    check(events.area == retained && events.updates == 0, "failure retains capture area");

                    if (otherScreen != null) {
                        call(mode, "changeSelectionScreen", otherScreen);
                        JDialog next = (JDialog) field(mode, "dialog");
                        RangeSelectionPanel panel = (RangeSelectionPanel) field(mode, "selectionPanel");
                        Rectangle bounds = otherScreen.getDefaultConfiguration().getBounds();
                        check(next != first && next.isShowing() && !first.isShowing() && first.isDisplayable(), "dialog switched and old retained hidden");
                        check(next.getBounds().equals(bounds), "dialog uses target bounds");
                        check(field(panel, "screenArea").equals(bounds), "panel uses target origin");
                        check(field(mode, "screenMonitorTimer") == timer && timer.isRunning(), "switch reuses timer");
                        check(events.shown == 1 && events.closed == 0 && events.updates == 0 && events.area == retained,
                                "switch preserves notifications and capture area");

                        click(panel, 40, 50);
                        check(!panel.isWaiting(), "selection starts");
                        call(mode, "changeSelectionScreen", pointerScreen);
                        check(field(mode, "dialog") == next, "SELECTING blocks switch");
                        click(panel, 100, 120);
                        check(events.area.equals(new Rectangle(bounds.x + 40, bounds.y + 50, 60, 70)),
                                "selection converted to target screen coordinates");
                        call(mode, "changeSelectionScreen", pointerScreen);
                        check(field(mode, "dialog") == next, "CONFIRMED blocks switch");

                        mode.prepareNextSelection();
                        Method poll = mode.getClass().getDeclaredMethod("checkScreenChanged");
                        poll.setAccessible(true);
                        poll.invoke(mode);
                        check(field(mode, "screenDevice").equals(MouseInfo.getPointerInfo().getDevice()),
                                "pointer monitor polling switches after returning to WAITING");
                        check(!next.isShowing() && next.isDisplayable(), "polling hides previous dialog");
                        JDialog latest = (JDialog) field(mode, "dialog");
                        check(latest.getRootPane().getActionMap().get("advanceStateWithAction") != null,
                                "new dialog has Enter action");
                        check(latest.getRootPane().getActionMap().get("mouse370") != null,
                                "new dialog has arrow action");
                        check(latest == first, "returning to a monitor reuses its original dialog");
                        GraphicsDevice alternate = otherScreen;
                        java.util.IdentityHashMap<GraphicsDevice, JDialog> reused = new java.util.IdentityHashMap<>();
                        reused.put(pointerScreen, first);
                        reused.put(alternate, next);
                        for (int i = 0; i < 200; i++) {
                            GraphicsDevice target = i % 2 == 0 ? alternate : pointerScreen;
                            call(mode, "changeSelectionScreen", target);
                            check(field(mode, "dialog") == reused.get(target), "repeated switches must reuse each dialog");
                            check(((java.util.Map<?, ?>) field(mode, "selectionWindows")).size() == 2,
                                    "cached window count exceeds monitor count");
                            for (JDialog cached : reused.values()) {
                                check(cached.isShowing() == (cached == field(mode, "dialog")), "only target dialog visible");
                            }
                        }
                        System.out.println("PASS: 200 alternating switches reuse exactly two dialogs");
                        System.out.println("PASS: cross-monitor reuse, state guards, target coordinates, pointer polling");
                    } else {
                        System.out.println("SKIP: cross-monitor checks require two monitors");
                    }

                    Method restart = mode.getClass().getDeclaredMethod("startScreenMonitor");
                    restart.setAccessible(true);
                    restart.invoke(mode);
                    Timer replacement = (Timer) field(mode, "screenMonitorTimer");
                    check(!timer.isRunning() && replacement != timer && replacement.isRunning(), "restart stops old timer");
                    mode.close();
                    check(!replacement.isRunning() && field(mode, "screenMonitorTimer") == null, "close stops timer");
                    check(((java.util.Map<?, ?>) field(mode, "selectionWindows")).isEmpty(), "close clears all cached windows");
                    check(java.util.Arrays.stream(Window.getWindows()).noneMatch(Window::isDisplayable), "close disposes hidden windows too");
                    check(field(mode, "dialog") == null && field(mode, "selectionPanel") == null
                            && field(mode, "screenDevice") == null, "close clears references");
                    check(events.shown == 1 && events.closed == 1, "close notification only once");
                    mode.close();
                    check(events.closed == 1, "repeated close is harmless");

                    for (int i = 0; i < 3; i++) {
                        call(mode, "showRangeSelectionDialog", pointerScreen);
                        Timer openedTimer = (Timer) field(mode, "screenMonitorTimer");
                        mode.close();
                        check(!openedTimer.isRunning(), "reopen leaves no running timer");
                    }
                    check(events.shown == 4 && events.closed == 4, "repeated open/close notifications");
                    System.out.println("PASS: same-monitor no-op, failure recovery, notifications, timer restart and repeated open/close");
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                } finally {
                    if (mode != null) mode.close();
                }
            });
        } finally {
            EventQueue.invokeAndWait(() -> {
                for (Window window : Window.getWindows()) window.dispose();
            });
        }
    }
}
