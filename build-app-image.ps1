<#
.SYNOPSIS
難読化したJARから、常駐向けのメモリ設定を含むWindowsアプリを生成します。
.PARAMETER JdkPath
JDK 21のディレクトリ。省略時はJAVA_HOMEを使用します。
.PARAMETER Destination
生成先ディレクトリ。既存のInstantSnapフォルダは削除・上書きしません。
#>
param(
    [string]$JdkPath = $env:JAVA_HOME,
    [string]$Destination = 'exe'
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($JdkPath)) {
    throw 'JAVA_HOMEを設定するか、-JdkPathでJDK 21のディレクトリを指定してください。'
}
$javaExecutable = Join-Path $JdkPath 'bin/java.exe'
$jpackageExecutable = Join-Path $JdkPath 'bin/jpackage.exe'
if (!(Test-Path -LiteralPath $javaExecutable) -or !(Test-Path -LiteralPath $jpackageExecutable)) {
    throw '指定したJDKにjava.exeまたはjpackage.exeがありません。'
}

$previousJavaHome = $env:JAVA_HOME
Push-Location $PSScriptRoot
try {
    if (Test-Path -LiteralPath (Join-Path $Destination 'InstantSnap')) {
        throw '生成先にInstantSnapが存在します。既存フォルダを退避するか、-Destinationで別の生成先を指定してください。'
    }

    # Mavenとjpackageで同じJDKを使う。終了時には元の環境変数へ戻す。
    $env:JAVA_HOME = $JdkPath
    & .\mvnw.cmd clean package '-DskipTests'
    if ($LASTEXITCODE -ne 0) {
        throw 'Mavenビルドに失敗しました。exeの生成は行いません。'
    }

    $packageArguments = @(
        '--input', 'target/jpackage-input',
        '--name', 'InstantSnap',
        '--main-jar', 'InstantSnap-0.0.1-SNAPSHOT-obfuscated.jar',
        '--main-class', 'com.github.rebi0395.instantsnap.app.Main',
        '--type', 'app-image',
        '--dest', $Destination,
        '--icon', 'resources/images/icon/Instantsnap.ico',
        # 512MBはヒープの上限であり、起動時に確保する量でもプロセス全体の上限でもない。
        '--java-options', '-Xms16m',
        '--java-options', '-Xmx512m',
        '--java-options', '-XX:+UseG1GC',
        # 撮影後に割り当てが止まっても、未使用領域を回収・縮小する機会を作る。
        # アプリの撮影処理からSystem.gc()を呼び出すことはしない。
        '--java-options', '-XX:G1PeriodicGCInterval=5000',
        '--java-options', '-XX:G1PeriodicGCSystemLoadThreshold=0',
        '--java-options', '-XX:MinHeapFreeRatio=5',
        '--java-options', '-XX:MaxHeapFreeRatio=15'
    )
    & $jpackageExecutable @packageArguments
    if ($LASTEXITCODE -ne 0) {
        throw 'jpackageによるexeの生成に失敗しました。'
    }
    Write-Output ('生成完了: ' + (Join-Path $Destination 'InstantSnap/InstantSnap.exe'))
} finally {
    $env:JAVA_HOME = $previousJavaHome
    Pop-Location
}
