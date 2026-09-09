# instantSnap
A simple screenshot tool for Java.

## Windows版のビルド（JDK 21）

プロジェクト直下で次を実行します。難読化JARのビルドとexe化をまとめて行います。

```powershell
.\build-app-image.ps1 -JdkPath 'C:\pleiades\2026-03\java\21'
```

生成先は `exe/InstantSnap/InstantSnap.exe` です。配布時は `InstantSnap` フォルダ全体が必要です。
既存の生成物は自動削除しません。退避するか、`-Destination target/app-image` などで別の生成先を指定してください。

exeは1個です。設定ボタンでは同じexeを `--settings` 付きで別プロセス起動します。
設定画面を閉じても常駐側の撮影監視は継続します。同梱ランタイムに `javaw.exe` がなくても動作する構成です。
設定画面の子プロセスの出力は、作業ディレクトリの `logs/settings-process.log` に保存します（起動ごとに上書き）。

### メモリ設定

ビルドスクリプトは、以下をexeのJVM起動オプションに設定します。
Eclipseから起動する場合も、実行構成の「VM引数」に同じ値を指定できます。
Mavenの「ゴール」欄ではありません。

```text
-Xms16m -Xmx512m -XX:+UseG1GC -XX:G1PeriodicGCInterval=5000 -XX:G1PeriodicGCSystemLoadThreshold=0 -XX:MinHeapFreeRatio=5 -XX:MaxHeapFreeRatio=15
```

従来どおり手動でjpackageを実行する場合は、各オプションを `--java-options "-Xms32m"` のように追加してください。
JARをビルドし直すだけでは既存exeの起動設定は変わらないため、exeも再生成してください。

- 初期ヒープは16MB、最大ヒープは512MB。画像処理時には必要に応じて拡張します。
- G1 GCに5秒間隔の回収機会を設け、撮影後の待機中にも未使用ヒープを縮小できるようにします。追加のGC処理は発生します。
- 512MBはJavaヒープの上限です。タスクマネージャーのプロセス全体の使用量を512MB以下に保証するものではありません。
- Windowsではクリップボードへの同期転送後にJava側の画像参照を解放します。ただし、貼り付け用の最新画像はOS側に残り、4K画像の転送中には画素だけでも約32MiBと変換用の作業領域が必要です。
- 高解像度や複数画面をまとめて撮影する場合は、512MBでは不足する可能性があります。実際の撮影条件で確認してください。

### 確認した範囲と再現試験

`tests/MemoryProbe.java` は4K画像を10枚PNG保存し、最新の1枚だけを保持する比較試験です。
実際の画面やOSのクリップボードは変更しません。通知・Windowsの画像変換・ネイティブメモリは測定対象外です。

```powershell
javac -d target tests/MemoryProbe.java
java -cp target MemoryProbe 3840 2160
java -Xms16m -Xmx512m -XX:+UseG1GC -XX:G1PeriodicGCInterval=5000 -XX:G1PeriodicGCSystemLoadThreshold=0 -XX:MinHeapFreeRatio=5 -XX:MaxHeapFreeRatio=15 -cp target MemoryProbe 3840 2160
```

開発環境のJDK 21で、最後の保存から25秒後の確保済みヒープは標準設定250MiBに対して上記設定42MiBでした。
これはプロセス全体の使用量ではなく、exeで報告された500MBの再現・解消を保証する結果ではありません。
再生成したexeで同じ条件の撮影後、30秒ほど待った時点の使用量も確認してください。
