import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.Callable;
import javax.swing.*;
import javax.swing.border.Border;
import com.github.rebi0395.instantsnap.common.MessageKeyConverter;
import com.github.rebi0395.instantsnap.model.bean.ConfigBean;
import com.github.rebi0395.instantsnap.model.enums.LanguageType;
import com.github.rebi0395.instantsnap.model.enums.ThemeType;
import com.github.rebi0395.instantsnap.view.swing.panel.MainWindowPanel;

/** 実際のSwingフォーカス移動と、標準テーマの枠の描画・余白を検証する。 */
public class MainWindowFocusProbe {
    private static JFrame frame;
    private static MainWindowPanel panel;
    private static ConfigBean config;

    private static void applyTheme() throws Exception {
        java.lang.reflect.Method method = MainWindowPanel.class.getDeclaredMethod("applyTheme", ThemeType.class);
        method.setAccessible(true);
        method.invoke(panel, config.getThemeType());
    }
    private static final String[] CONTROLS = {
        "languageComboBox", "settingsButton", "captureButton", "rangeSelectionButton",
        "snapAreaComboBox", "openSaveDirectoryButton", "notificationButton"
    };

    private static <T> T edt(Callable<T> task) throws Exception {
        java.util.concurrent.FutureTask<T> result = new java.util.concurrent.FutureTask<>(task);
        EventQueue.invokeAndWait(result);
        return result.get();
    }

    private static JComponent control(String name) throws Exception {
        Field field = MainWindowPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return (JComponent) field.get(panel);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void focus(Component component) throws Exception {
        edt(() -> { component.requestFocusInWindow(); return null; });
        for (int i = 0; i < 100; i++) {
            if (edt(component::hasFocus)) return;
            Thread.sleep(20);
        }
        throw new AssertionError("focus not received: " + component.getClass().getName());
    }

    private static int focusPixels(JComponent component) {
        BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();
        try {
            component.getBorder().paintBorder(component, graphics, 0, 0, image.getWidth(), image.getHeight());
        } finally {
            graphics.dispose();
        }
        int expected = ThemeType.DEFAULT.getThemePalette().getFocusColor().getRGB();
        int count = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            if (image.getRGB(x, 0) == expected) count++;
        }
        return count;
    }

    public static void main(String[] args) throws Exception {
        try {
            edt(() -> {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                config = new ConfigBean();
                config.setThemeType(ThemeType.DEFAULT);
                panel = new MainWindowPanel(null, new MessageKeyConverter(LanguageType.ENGLISH));
                // UIだけを検証し、設定保存や撮影処理を起動しない。
                JComboBox<?> snapArea = (JComboBox<?>) control("snapAreaComboBox");
                snapArea.removeActionListener(panel);
                JComboBox.class.getMethod("addItem", Object.class).invoke(snapArea, "Active window");
                control("openSaveDirectoryButton").setEnabled(true);
                applyTheme();
                frame = new JFrame("InstantSnap focus regression");
                frame.setContentPane(panel);
                frame.setAlwaysOnTop(true);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                frame.toFront();
                return null;
            });

            // Tabの巡回対象から、選択欄や小型アイコンが抜けていないことを確認する。
            edt(() -> {
                Set<Component> visited = Collections.newSetFromMap(new IdentityHashMap<>());
                FocusTraversalPolicy policy = frame.getFocusTraversalPolicy();
                Component first = policy.getFirstComponent(frame);
                Component current = first;
                for (int i = 0; i < 30 && current != null && visited.add(current); i++) {
                    current = policy.getComponentAfter(frame, current);
                }
                for (String name : CONTROLS) {
                    check(visited.contains(control(name)), "Tab traversal omits " + name);
                }
                return null;
            });

            for (String name : CONTROLS) {
                JComponent component = edt(() -> control(name));
                Insets unfocusedInsets = edt(() -> component.getBorder().getBorderInsets(component));
                focus(component);
                edt(() -> {
                    check(focusPixels(component) >= component.getWidth() - 2, "focus outline missing: " + name);
                    check(component.getBorder().getBorderInsets(component).equals(unfocusedInsets),
                            "focus changes insets: " + name);
                    Border standard = component instanceof JComboBox<?> ? UIManager.getBorder("ComboBox.border")
                            : UIManager.getBorder("Button.border");
                    if (name.equals("languageComboBox") || name.equals("snapAreaComboBox")
                            || name.equals("settingsButton") || name.equals("captureButton") || name.equals("rangeSelectionButton")) {
                        Insets expected = standard == null ? new Insets(0, 0, 0, 0) : standard.getBorderInsets(component);
                        check(unfocusedInsets.equals(expected), "standard border insets changed: " + name);
                    }
                    return null;
                });

                // 同じテーマを再適用してもフォーカス枠を維持する。
                edt(() -> { applyTheme(); return null; });
                edt(() -> {
                    check(component.hasFocus() && focusPixels(component) >= component.getWidth() - 2,
                            "theme reapplication loses focus outline: " + name);
                    return null;
                });

                if (component instanceof JComboBox<?> comboBox) {
                    edt(() -> {
                        comboBox.getActionMap().get("toggleComboPopup").actionPerformed(
                                new java.awt.event.ActionEvent(comboBox, 0, "enter"));
                        check(comboBox.isPopupVisible(), "Enter does not open combo popup");
                        comboBox.hidePopup();
                        return null;
                    });
                }
            }

            // テーマを往復しても標準部品と小型アイコンの枠が戻ることを確認する。
            JComponent language = edt(() -> control("languageComboBox"));
            focus(language);
            for (ThemeType custom : new ThemeType[]{ThemeType.DARK, ThemeType.STORYBOOK}) {
                edt(() -> {
                    config.setThemeType(custom);
                    applyTheme();
                    config.setThemeType(ThemeType.DEFAULT);
                    applyTheme();
                    check(language.hasFocus() && focusPixels(language) >= language.getWidth() - 2,
                            "theme round trip loses default focus outline");
                    return null;
                });
            }

            // 無効な部品はTabの巡回先から外れることも確認する。
            edt(() -> {
                panel.setSettingsEnabled(false);
                FocusTraversalPolicy policy = frame.getFocusTraversalPolicy();
                Set<Component> visited = Collections.newSetFromMap(new IdentityHashMap<>());
                Component current = policy.getFirstComponent(frame);
                for (int i = 0; i < 30 && current != null && visited.add(current); i++) {
                    current = policy.getComponentAfter(frame, current);
                }
                check(!visited.contains(control("languageComboBox")) && !visited.contains(control("snapAreaComboBox"))
                        && !visited.contains(control("notificationButton")), "disabled controls remain in Tab cycle");
                return null;
            });
            System.out.println("PASS: Tab traversal, actual focus, visible outlines, stable insets, theme reapplication, Enter popup and theme round trips");
        } finally {
            edt(() -> { if (frame != null) frame.dispose(); return null; });
        }
    }
}
