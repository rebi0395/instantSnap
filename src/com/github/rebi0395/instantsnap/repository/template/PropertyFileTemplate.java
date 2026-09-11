package com.github.rebi0395.instantsnap.repository.template;

import com.github.rebi0395.instantsnap.model.enums.LanguageType;

/**
 * ユーザー設定ファイルの言語別テンプレートを提供するクラスです。
 * テンプレート内のコメントと項目順を維持したまま、PropertiesRepositoryへ渡します。
 */
public final class PropertyFileTemplate {

	/** インスタンス化を禁止します。 */
	private PropertyFileTemplate() {
	}

	/**
	 * 言語設定に対応したプロパティテンプレートを解決する
	 * @param languageType 言語設定(デフォルトは英語)
	 * @return プロパティファイルのテンプレート
	 */
	public static String resolve(LanguageType languageType) {
		switch (languageType) {
		case JAPANESE:
			return PROPERTY_TEMPLATE_JA;
		case SIMPLIFIED_CHINESE:
			return PROPERTY_TEMPLATE_ZH_CN;
		case KOREAN:
			return PROPERTY_TEMPLATE_KO;
		case FRENCH:
			return PROPERTY_TEMPLATE_FR;
		case SPANISH:
			return PROPERTY_TEMPLATE_ES;
		case RUSSIAN:
			return PROPERTY_TEMPLATE_RU;
		case GERMAN:
			return PROPERTY_TEMPLATE_DE;
		case BRAZILIAN_PORTUGUESE:
			return PROPERTY_TEMPLATE_PT_BR;

		case ENGLISH:
		default:
			return PROPERTY_TEMPLATE_EN;
		}
	}

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（日本語） */
	private static final String PROPERTY_TEMPLATE_JA = """
			# InstantSnap Settings

			# 言語を指定してください
			language=ja

			# テーマを指定してください
			# 0 -> デフォルト, 1 -> ダーク, 2 -> 絵本
			theme=0

			# ～～ 撮影設定 ～～
			# 撮影ショートカットキー
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# 範囲選択ショートカットキー
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# 範囲確定後に自動で撮影する
			autoSnapAfterRangeSelectionEnabled=true

			# 撮影後に範囲撮影モードを自動で終了する
			closeRangeSelectionAfterSnapEnabled=true

			# 撮影範囲の選択 デフォルトは 1
			# 1 -> 使用しているウィンドウを撮影, 2 -> カーソルのあるモニターを撮影
			# 3 -> メインモニターを撮影, 4 -> 全てのモニターを撮影
			snapArea=1

			# マウスカーソルを画像に含める
			includeCursorEnabled=false

			# InstantSnapのウィンドウを画像に含める
			includeSettingWindowEnabled=false

			# ～～ 出力設定 ～～
			# 画像を保存する（有効にする場合は"true"と記載してください）
			saveImageEnabled=true

			# クリップボードにコピーする（有効にする場合は"true"と記載してください）
			clipboardEnabled=true

			# 保存先パスを指定してください
			savePath=./screenshots/

			# ファイル名の先頭文字
			fileNamePrefix=

			# ファイル名につける日時フォーマット デフォルトは1
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# ファイルの拡張子の選択 png、jpg、bmp、gifのいずれかを記載してください(デフォルト：png)
			format=png

			# ～～ 撮影後設定 ～～
			# 撮影後に行う処理を選択
			# 1 -> 画像ファイルを表示する, 2 -> ペイントを開く,
			# 3 -> 一時保存ファイルに保存する, 0, 未記入等 -> 何もしない(デフォルト)
			afterSnapAction=0

			# 撮影時、通知を表示する（有効にする場合は"true"と記載してください）
			notificationEnabled=true

			# ～～ サウンド設定 ～～#
			# 通知音を有効にする（有効にする場合は"true"と記載してください）
			soundNotificationEnabled=true

			# 撮影音を鳴らして撮影する（有効にする場合は"true"と記載してください）
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（英語） */
	private static final String PROPERTY_TEMPLATE_EN = """
			# InstantSnap Settings

			# Please specify the language
			language=en

			# Please specify the theme
			# 0 -> Default, 1 -> Dark, 2 -> Storybook
			theme=0

			# ~~ Capture Settings ~~
			# Capture shortcut key
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# Range selection shortcut key
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# Capture automatically after selecting a range
			autoSnapAfterRangeSelectionEnabled=true

			# Automatically exit range capture mode after capturing
			closeRangeSelectionAfterSnapEnabled=true

			# Select the capture area
			# 1 -> Active window (default), 2 -> Active monitor
			# 3 -> Main monitor, 4 -> All monitors
			snapArea=1

			# Include the mouse cursor in the image
			includeCursorEnabled=false

			# Include the setting window in the image
			includeSettingWindowEnabled=false

			# ~~ Output Settings ~~
			# Save the image (set to "true" to enable)
			saveImageEnabled=true

			# Copy to clipboard (set to "true" to enable)
			clipboardEnabled=true

			# Please specify the save path
			savePath=./screenshots/

			# Prefix added to the beginning of the file name
			fileNamePrefix=

			# Date/time format added to the file name. Default is 1.
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# Select the file extension. Specify one of png, jpg, bmp, or gif. (default: png)
			format=png

			# ~~ Post-Capture Settings ~~
			# Select the action to perform after capturing
			# 1 -> Open the image file, 2 -> Open Paint,
			# 3 -> Save to a temporary file, 0, blank, etc. -> Do nothing (default)
			afterSnapAction=0

			# Show a notification when capturing (set to "true" to enable)
			notificationEnabled=true

			# ~~ Sound Settings ~~
			# Enable notification sound (set to "true" to enable)
			soundNotificationEnabled=true

			# Play a shutter sound when capturing (set to "true" to enable)
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（中国語・簡体字） */
	private static final String PROPERTY_TEMPLATE_ZH_CN = """
			# InstantSnap 设置

			# 指定语言
			language=zh-CN

			# 指定主题
			# 0 -> 默认, 1 -> 深色, 2 -> 绘本
			theme=0

			# ~~ 截图设置 ~~
			# 截图快捷键
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# 区域选择快捷键
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# 选定区域后自动截图
			autoSnapAfterRangeSelectionEnabled=true

			# 截图后自动退出区域模式
			closeRangeSelectionAfterSnapEnabled=true

			# 选择截图范围
			# 1 -> 当前窗口（默认）, 2 -> 指针所在屏幕
			# 3 -> 主屏幕, 4 -> 所有屏幕
			snapArea=1

			# 在图片中包含鼠标指针
			includeCursorEnabled=false

			# 在图片中包含 InstantSnap 窗口
			includeSettingWindowEnabled=false

			# ~~ 输出设置 ~~
			# 保存图片（设为 "true" 即启用）
			saveImageEnabled=true

			# 复制到剪贴板（设为 "true" 即启用）
			clipboardEnabled=true

			# 指定保存路径
			savePath=./screenshots/

			# 文件名前缀
			fileNamePrefix=

			# 文件名的日期时间格式，默认为 1
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# 文件扩展名：png、jpg、bmp 或 gif（默认：png）
			format=png

			# ~~ 截图后设置 ~~
			# 选择截图后的操作
			# 1 -> 打开图片, 2 -> 用画图打开,
			# 3 -> 保存到临时文件夹, 0 或留空 -> 无操作（默认）
			afterSnapAction=0

			# 截图时显示通知（设为 "true" 即启用）
			notificationEnabled=true

			# ~~ 声音设置 ~~
			# 启用通知音（设为 "true" 即启用）
			soundNotificationEnabled=true

			# 截图时播放快门声（设为 "true" 即启用）
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（韓国語） */
	private static final String PROPERTY_TEMPLATE_KO = """
			# InstantSnap 설정

			# 언어 지정
			language=ko

			# 테마 지정
			# 0 -> 기본, 1 -> 다크, 2 -> 동화책
			theme=0

			# ~~ 캡처 설정 ~~
			# 캡처 단축키
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# 영역 선택 단축키
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# 영역 선택 후 자동 캡처
			autoSnapAfterRangeSelectionEnabled=true

			# 캡처 후 영역 모드 자동 종료
			closeRangeSelectionAfterSnapEnabled=true

			# 캡처 영역 선택
			# 1 -> 현재 창(기본), 2 -> 포인터가 있는 화면
			# 3 -> 주 화면, 4 -> 모든 화면
			snapArea=1

			# 이미지에 마우스 포인터 포함
			includeCursorEnabled=false

			# 이미지에 InstantSnap 창 포함
			includeSettingWindowEnabled=false

			# ~~ 출력 설정 ~~
			# 이미지 저장 (사용하려면 "true")
			saveImageEnabled=true

			# 클립보드에 복사 (사용하려면 "true")
			clipboardEnabled=true

			# 저장 경로 지정
			savePath=./screenshots/

			# 파일 이름 접두사
			fileNamePrefix=

			# 파일 이름의 날짜/시간 형식. 기본값은 1
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# 파일 확장자: png, jpg, bmp, gif 중 하나 (기본값: png)
			format=png

			# ~~ 캡처 후 설정 ~~
			# 캡처 후 동작 선택
			# 1 -> 이미지 열기, 2 -> 그림판에서 열기,
			# 3 -> 임시 폴더에 저장, 0 또는 빈 값 -> 없음(기본)
			afterSnapAction=0

			# 캡처 시 알림 표시 (사용하려면 "true")
			notificationEnabled=true

			# ~~ 소리 설정 ~~
			# 알림음 사용 (사용하려면 "true")
			soundNotificationEnabled=true

			# 캡처 시 셔터음 재생 (사용하려면 "true")
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（フランス語） */
	private static final String PROPERTY_TEMPLATE_FR = """
			# Paramètres InstantSnap

			# Langue
			language=fr

			# Thème
			# 0 -> Défaut, 1 -> Sombre, 2 -> Conte
			theme=0

			# ~~ Réglages de capture ~~
			# Raccourci de capture
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# Raccourci de sélection de zone
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# Capturer automatiquement la zone sélectionnée
			autoSnapAfterRangeSelectionEnabled=true

			# Quitter automatiquement le mode zone après capture
			closeRangeSelectionAfterSnapEnabled=true

			# Zone de capture
			# 1 -> Fenêtre active (défaut), 2 -> Écran du pointeur
			# 3 -> Écran principal, 4 -> Tous les écrans
			snapArea=1

			# Inclure le pointeur dans l’image
			includeCursorEnabled=false

			# Inclure les fenêtres InstantSnap dans l’image
			includeSettingWindowEnabled=false

			# ~~ Réglages de sortie ~~
			# Enregistrer l’image ("true" pour activer)
			saveImageEnabled=true

			# Copier dans le presse-papiers ("true" pour activer)
			clipboardEnabled=true

			# Dossier d’enregistrement
			savePath=./screenshots/

			# Préfixe du nom de fichier
			fileNamePrefix=

			# Format de date du nom de fichier. Valeur par défaut : 1
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# Extension : png, jpg, bmp ou gif (défaut : png)
			format=png

			# ~~ Après la capture ~~
			# Action après la capture
			# 1 -> Ouvrir l’image, 2 -> Ouvrir dans Paint,
			# 3 -> Dossier temporaire, 0 ou vide -> Aucune (défaut)
			afterSnapAction=0

			# Afficher une notification ("true" pour activer)
			notificationEnabled=true

			# ~~ Réglages du son ~~
			# Activer le son de notification ("true" pour activer)
			soundNotificationEnabled=true

			# Jouer le son de capture ("true" pour activer)
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（スペイン語） */
	private static final String PROPERTY_TEMPLATE_ES = """
			# Ajustes de InstantSnap

			# Idioma
			language=es

			# Tema
			# 0 -> Predeterminado, 1 -> Oscuro, 2 -> Cuento
			theme=0

			# ~~ Ajustes de captura ~~
			# Atajo de captura
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# Atajo de selección de área
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# Capturar automáticamente el área seleccionada
			autoSnapAfterRangeSelectionEnabled=true

			# Salir automáticamente del modo área tras capturar
			closeRangeSelectionAfterSnapEnabled=true

			# Área de captura
			# 1 -> Ventana activa (predeterminado), 2 -> Pantalla del puntero
			# 3 -> Pantalla principal, 4 -> Todas las pantallas
			snapArea=1

			# Incluir el puntero en la imagen
			includeCursorEnabled=false

			# Incluir las ventanas de InstantSnap en la imagen
			includeSettingWindowEnabled=false

			# ~~ Ajustes de salida ~~
			# Guardar la imagen ("true" para activar)
			saveImageEnabled=true

			# Copiar al portapapeles ("true" para activar)
			clipboardEnabled=true

			# Carpeta de destino
			savePath=./screenshots/

			# Prefijo del nombre de archivo
			fileNamePrefix=

			# Formato de fecha del nombre. Valor predeterminado: 1
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# Extensión: png, jpg, bmp o gif (predeterminado: png)
			format=png

			# ~~ Después de capturar ~~
			# Acción después de capturar
			# 1 -> Abrir la imagen, 2 -> Abrir en Paint,
			# 3 -> Carpeta temporal, 0 o vacío -> Ninguna (predeterminado)
			afterSnapAction=0

			# Mostrar una notificación ("true" para activar)
			notificationEnabled=true

			# ~~ Ajustes de sonido ~~
			# Activar sonido de notificación ("true" para activar)
			soundNotificationEnabled=true

			# Reproducir sonido de captura ("true" para activar)
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（ロシア語） */
	private static final String PROPERTY_TEMPLATE_RU = """
			# Настройки InstantSnap

			# Язык
			language=ru

			# Тема
			# 0 -> Обычная, 1 -> Тёмная, 2 -> Сказка
			theme=0

			# ~~ Настройки снимка ~~
			# Горячая клавиша снимка
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# Горячая клавиша выбора области
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# Автоматически снимать выбранную область
			autoSnapAfterRangeSelectionEnabled=true

			# Автоматически закрывать режим области после снимка
			closeRangeSelectionAfterSnapEnabled=true

			# Область снимка
			# 1 -> Активное окно (по умолчанию), 2 -> Экран с указателем
			# 3 -> Основной экран, 4 -> Все экраны
			snapArea=1

			# Включить указатель мыши в изображение
			includeCursorEnabled=false

			# Включить окна InstantSnap в изображение
			includeSettingWindowEnabled=false

			# ~~ Настройки вывода ~~
			# Сохранять изображение ("true" — включено)
			saveImageEnabled=true

			# Копировать в буфер обмена ("true" — включено)
			clipboardEnabled=true

			# Папка сохранения
			savePath=./screenshots/

			# Префикс имени файла
			fileNamePrefix=

			# Формат даты в имени файла. По умолчанию: 1
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# Расширение: png, jpg, bmp или gif (по умолчанию: png)
			format=png

			# ~~ После снимка ~~
			# Действие после снимка
			# 1 -> Открыть изображение, 2 -> Открыть в Paint,
			# 3 -> Во временную папку, 0 или пусто -> Нет (по умолчанию)
			afterSnapAction=0

			# Показывать уведомление ("true" — включено)
			notificationEnabled=true

			# ~~ Настройки звука ~~
			# Включить звук уведомления ("true" — включено)
			soundNotificationEnabled=true

			# Воспроизводить звук снимка ("true" — включено)
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（ドイツ語） */
	private static final String PROPERTY_TEMPLATE_DE = """
			# InstantSnap-Einstellungen

			# Sprache
			language=de

			# Design
			# 0 -> Standard, 1 -> Dunkel, 2 -> Bilderbuch
			theme=0

			# ~~ Aufnahme-Einstellungen ~~
			# Tastenkürzel für Aufnahme
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# Tastenkürzel für Bereichsauswahl
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# Gewählten Bereich automatisch aufnehmen
			autoSnapAfterRangeSelectionEnabled=true

			# Bereichsmodus nach der Aufnahme automatisch schließen
			closeRangeSelectionAfterSnapEnabled=true

			# Aufnahmebereich
			# 1 -> Aktives Fenster (Standard), 2 -> Bildschirm am Mauszeiger
			# 3 -> Hauptbildschirm, 4 -> Alle Bildschirme
			snapArea=1

			# Mauszeiger in das Bild aufnehmen
			includeCursorEnabled=false

			# InstantSnap-Fenster in das Bild aufnehmen
			includeSettingWindowEnabled=false

			# ~~ Ausgabe-Einstellungen ~~
			# Bild speichern (zum Aktivieren "true")
			saveImageEnabled=true

			# In Zwischenablage kopieren (zum Aktivieren "true")
			clipboardEnabled=true

			# Speicherpfad
			savePath=./screenshots/

			# Präfix des Dateinamens
			fileNamePrefix=

			# Datumsformat im Dateinamen. Standard: 1
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# Dateiendung: png, jpg, bmp oder gif (Standard: png)
			format=png

			# ~~ Nach der Aufnahme ~~
			# Aktion nach der Aufnahme
			# 1 -> Bild öffnen, 2 -> In Paint öffnen,
			# 3 -> Im Temp-Ordner speichern, 0 oder leer -> Keine (Standard)
			afterSnapAction=0

			# Benachrichtigung anzeigen (zum Aktivieren "true")
			notificationEnabled=true

			# ~~ Ton-Einstellungen ~~
			# Hinweiston aktivieren (zum Aktivieren "true")
			soundNotificationEnabled=true

			# Aufnahmeton abspielen (zum Aktivieren "true")
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

	/** 初期化時の文章をソース側で保持 改行コードはosごとに最適化（ポルトガル語・ブラジル） */
	private static final String PROPERTY_TEMPLATE_PT_BR = """
			# Configurações do InstantSnap

			# Idioma
			language=pt-BR

			# Tema
			# 0 -> Padrão, 1 -> Escuro, 2 -> Livro infantil
			theme=0

			# ~~ Ajustes de captura ~~
			# Atalho de captura
			snapKey=3639
			snapIsUseAlt=false
			snapIsUseCtrl=false
			snapIsUseShift=true

			# Atalho de seleção de área
			selectionKey=68
			selectionIsUseAlt=false
			selectionIsUseCtrl=false
			selectionIsUseShift=false

			# Capturar automaticamente a área selecionada
			autoSnapAfterRangeSelectionEnabled=true

			# Sair automaticamente do modo de área após capturar
			closeRangeSelectionAfterSnapEnabled=true

			# Área de captura
			# 1 -> Janela ativa (padrão), 2 -> Tela do ponteiro
			# 3 -> Tela principal, 4 -> Todas as telas
			snapArea=1

			# Incluir o ponteiro na imagem
			includeCursorEnabled=false

			# Incluir as janelas do InstantSnap na imagem
			includeSettingWindowEnabled=false

			# ~~ Ajustes de saída ~~
			# Salvar a imagem (use "true" para ativar)
			saveImageEnabled=true

			# Copiar para a área de transferência (use "true" para ativar)
			clipboardEnabled=true

			# Pasta de destino
			savePath=./screenshots/

			# Prefixo do nome do arquivo
			fileNamePrefix=

			# Formato de data no nome. Padrão: 1
			# 1 -> yyyyMMdd.HHmmss.SSS, 2 -> yyyyMMdd.HHmmss
			# 3 -> yyyyMMdd_HHmmss_SSS, 4 -> yyyyMMdd_HHmmss
			fileNameDateFormat=1

			# Extensão: png, jpg, bmp ou gif (padrão: png)
			format=png

			# ~~ Após capturar ~~
			# Ação após a captura
			# 1 -> Abrir imagem, 2 -> Abrir no Paint,
			# 3 -> Pasta temporária, 0 ou vazio -> Nenhuma (padrão)
			afterSnapAction=0

			# Mostrar notificação (use "true" para ativar)
			notificationEnabled=true

			# ~~ Ajustes de som ~~
			# Ativar som de notificação (use "true" para ativar)
			soundNotificationEnabled=true

			# Reproduzir som de captura (use "true" para ativar)
			soundSnapEnabled=true

			""".replace("\r\n", "\n").replace("\n", System.lineSeparator());

}
