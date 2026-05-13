package instantsnap.logic;

import static instantsnap.util.AppConstant.*;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import instantsnap.core.Option;

/**
 * ホットキーを監視し、撮影処理を呼び出すクラス。
 * JNativeHookライブラリを使用。
 */
public class KeyManager implements NativeKeyListener {

    private Option option;
    private ScreenShotExecutor executor;

    /**
     * コンストラクタ
     * @param option ユーザー設定値
     */
    public KeyManager(Option option) {
        this.option = option;
        this.executor = new ScreenShotExecutor();
        activate();
    }

    /**
     * キー監視を開始する
     */
    public void activate() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
            System.out.println("キー監視開始");
        } catch (NativeHookException e) {
            System.err.println("Could not register native hook: " + e.getMessage());
        }
    }

    /**
     * キー監視を停止する
     */
    public void deactivate() {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    /**
     * キーが押下されたときのイベントメソッド
     * 設定したトリガーキーを押下で、撮影処理を呼び出す
     */
    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        // PrintScreenキーが押下時
        if (e.getKeyCode() == NativeKeyEvent.VC_PRINTSCREEN) {
        	// ビット演算でshiftキーの状態ビットだけを抜き出し判定する
        	boolean isShiftPressed = (e.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0;
        	// shiftキーが押されているかで撮影可否を判定する
        	if ((PRINTSCREEN.equals(option.getTrigger()) && !isShiftPressed) || 
        		(SHIFT_PRINTSCREEN.equals(option.getTrigger()) && isShiftPressed)) {
        		// 別スレッドで撮影実行
	    		new Thread(() -> {
	    		    executor.execute(option);
	    		    System.out.println("撮影に成功しました");
	    		    deactivate();
	    		}).start();
        	}
        }
    }
}