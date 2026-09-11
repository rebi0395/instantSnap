package com.github.rebi0395.instantsnap.listener;

import com.github.rebi0395.instantsnap.model.enums.SnapRouteType;

/**
 * グローバルショートカットによる撮影要求を受け取るリスナーです。
 */
public interface SnapKeyEventListener {

	/**
	 * 撮影キーが押されたことを通知します。
	 *
	 * @param snapRouteType 撮影要求が発生した経路
	 */
	void requestSnap(SnapRouteType snapRouteType);

	/**
	 * 範囲選択キーが押されたことを通知します。
	 */
	void requestRangeSelection();
}
