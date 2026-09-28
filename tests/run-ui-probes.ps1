# リポジトリ直下から .\tests\run-ui-probes.ps1 で実行する。
# 検証中は範囲選択画面とミニウィンドウを短時間表示する。
$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Push-Location $projectRoot
try {
    $probeOutput = Join-Path $projectRoot 'target\ui-probes'
    New-Item -ItemType Directory -Path $probeOutput -Force | Out-Null
    & .\mvnw.cmd -o compile org.apache.maven.plugins:maven-dependency-plugin:3.7.0:build-classpath '-Dmdep.outputFile=target/ui-probes/classpath.txt'
    if ($LASTEXITCODE -ne 0) { throw 'Maven compile / dependency classpath failed' }

    $classpath = (Join-Path $projectRoot 'target\classes') + ';' + [IO.File]::ReadAllText((Join-Path $probeOutput 'classpath.txt')).Trim()
    $sources = @(
        'src\com\github\rebi0395\instantsnap\view\swing\dialog\RangeSelectionDialog.java',
        'src\com\github\rebi0395\instantsnap\view\swing\panel\RangeSelectionPanel.java',
        'src\com\github\rebi0395\instantsnap\view\swing\panel\MainWindowPanel.java',
        'tests\RangeSelectionRegressionProbe.java',
        'tests\MainWindowFocusProbe.java'
    )
    & javac --release 21 -encoding UTF-8 -cp $classpath -d $probeOutput @sources
    if ($LASTEXITCODE -ne 0) { throw 'UI regression probe compilation failed' }

    $runtimeClasspath = $probeOutput + ';' + $classpath
    & java -Xmx256m -cp $runtimeClasspath RangeSelectionRegressionProbe
    if ($LASTEXITCODE -ne 0) { throw 'Range selection regression probe failed' }
    & java -cp $runtimeClasspath MainWindowFocusProbe
    if ($LASTEXITCODE -ne 0) { throw 'Main window focus probe failed' }
} finally {
    Pop-Location
}
