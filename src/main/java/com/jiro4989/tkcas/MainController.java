package com.jiro4989.tkcas;

import static com.jiro4989.tkcas.util.Texts.*;

import com.jiro4989.tkcas.config.ConfigStage;
import com.jiro4989.tkcas.menubar.TrimmingSelector;
import com.jiro4989.tkcas.stage.MyFileChooser;
import com.jiro4989.tkcas.standard.Standards;
import com.jiro4989.tkcas.util.DialogUtils;
import com.jiro4989.tkcas.util.MyProperties;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainController {

  // 画像描画戦略
  private DrawImageStrategy strategy;

  // 画像の規格
  private Standards walkStandard;
  private Standards sideViewStandard;

  // 設定変更画面
  private Optional<ConfigStage> configStageOpt = Optional.empty();

  // ファイルの更新時間監視
  private static FileObserver fileObserver;

  // 設定ファイル
  private final MyProperties preferences = new MyProperties(PREFERENCES_FILE);

  // 最近開いたファイル
  private final MyProperties walkLog = new MyProperties(LOG_FILE_WALK);
  private final MyProperties sideViewLog = new MyProperties(LOG_FILE_SIDE_VIEW);

  // @FXML private PositionsFlowPane positionsFlowPane;

  // 初期化

  @FXML
  private void initialize() {

    preferences.load();

    // TODO 一時的な設定
    String walk = WALK_PREST;
    String sideView = SIDE_VIEW_PREST;

    walkStandard = WalkGraphicsStrategy.createStandard(walk);
    sideViewStandard = SideViewStrategy.createStandard(sideView);
  }

  // イベントメソッド

  @FXML
  private void rootOnDragOver(DragEvent e) {

    Dragboard board = e.getDragboard();
    if (board.hasFiles()) {

      e.acceptTransferModes(TransferMode.COPY);
    }
  }

  @FXML
  private void rootOnDragDropped(DragEvent e) {

    Dragboard board = e.getDragboard();
    if (board.hasFiles()) {

      Pattern p = Pattern.compile("^.*\\.((?i)png)");
      board
          .getFiles()
          .stream()
          .filter(f -> p.matcher(f.getName()).matches())
          .forEach(
              file -> {
                // myMenuBar.setDisables(false);

                String result = DialogUtils.showChoiseDialog();
                if (result.equals("w")) {

                  TrimmingSelector ts = new TrimmingSelector(file, walkStandard);
                  ts.showAndWait();

                  drawWalkImage(file);
                  // myMenuBar.addRecentWalkFile(file);
                  return;
                }
                drawSideViewImage(file);
                // myMenuBar.addRecentSideViewFile(file);
              });
    }
  }

  // メソッド

  public void updateImages(File file) {
    strategy.drawImage(file);
  }

  public void drawWalkImage(File file) {

    if (fileObserver != null) fileObserver.stop();

    fileObserver = new FileObserver(200, file, this);
    strategy = new WalkGraphicsStrategy(this);

    // positionsFlowPane.drawWalkImage(file.getPath(), walkStandard);

    configStageOpt.ifPresent(
        cs -> {
          cs.applyZoomRate();
          cs.applyAnimationSpeed();
        });
  }

  public void drawSideViewImage(File file) {

    if (fileObserver != null) fileObserver.stop();

    fileObserver = new FileObserver(200, file, this);
    strategy = new SideViewStrategy(this);

    // positionsFlowPane.drawSideViewImage(file.getPath(), sideViewStandard);

    configStageOpt.ifPresent(
        cs -> {
          cs.applyZoomRate();
          cs.applyAnimationSpeed();
        });
  }

  public void clearImages() {
    // positionsFlowPane.clearImages();
  }

  public void showConfigStage() {

    configStageOpt.ifPresent(
        c -> {
          if (c.isShowing()) c.hide();
          else c.show();
          getStage().requestFocus();
        });
  }

  public void updateZoomRate(double zoom) {

    // positionsFlowPane.updateZoomRate(zoom);
  }

  public void updateZoomRate(ScrollEvent e) {

    configStageOpt.ifPresent(
        cs -> {
          cs.changeZoomRate(e);
        });
  }

  public void updateAnimationSpeed(int duration) {

    // positionsFlowPane.updateAnimationSpeed(duration);
  }

  public void changeAlwaysOnTop() {

    Stage stage = getStage();
    boolean alwaysOnTop = getStage().isAlwaysOnTop();
    stage.setAlwaysOnTop(!alwaysOnTop);
    preferences.setProperty(KEY_ALWAYS_ON_TOP, "" + alwaysOnTop);
  }

  public void showPreviousImage() {
    // positionsFlowPane.showPreviousImage();
  }

  public void showNextImage() {
    // positionsFlowPane.showNextImage();
  }

  public void zoomDownImages() {
    configStageOpt.ifPresent(cs -> cs.zoomDown());
  }

  public void zoomUpImages() {
    configStageOpt.ifPresent(cs -> cs.zoomUp());
  }

  public void durationDown() {
    configStageOpt.ifPresent(cs -> cs.durationDown());
  }

  public void durationUp() {
    configStageOpt.ifPresent(cs -> cs.durationUp());
  }

  public void showOneImage() {
    // positionsFlowPane.switchViewerMode();
  }

  public void closeRequest() {

    // 最近開いた画像を保存
    // List<String> openedWalkFiles = myMenuBar.getRecentOpenedWalkFiles();
    // List<String> openedSideViewFiles = myMenuBar.getRecentOpenedSideViewFiles();
    // storeRecentFile(walkLog, openedWalkFiles);
    // storeRecentFile(sideViewLog, openedSideViewFiles);

    configStageOpt.ifPresent(
        cs -> {
          double rate = cs.getZoomRate();
          double duration = cs.getDuration();
          preferences.setProperty(KEY_ZOOM_RATE, "" + rate);
          preferences.setProperty(KEY_DURATION, "" + duration);
        });

    boolean alwaysOnTop = getStage().isAlwaysOnTop();
    preferences.setProperty(KEY_ALWAYS_ON_TOP, "" + alwaysOnTop);
    preferences.store();

    // Main.mainMp.setProperty(myMenuBar);
    Main.mainMp.store();
  }

  // package methods

  void resizeConfigStage() {

    configStageOpt.ifPresent(
        c -> {
          // c.resize(positionsFlowPane);
        });
  }

  // private methods

  private Stage getStage() {
    return null;
    // return (Stage) positionsFlowPane.getScene().getWindow();
  }

  private void setConfigs() {

    configStageOpt.ifPresent(
        cs -> {
          String r = preferences.getProperty(KEY_ZOOM_RATE).orElse(DEFAULT_VALUE_ZOOM_RATE);
          String d = preferences.getProperty(KEY_DURATION).orElse(DEFAULT_VALUE_DURATION);
          double rate = Double.parseDouble(r);
          double duration = Double.parseDouble(d);

          cs.setZoomRate(rate);
          cs.setDuration(duration);
        });
  }

  // Getter

  public Standards getWalkStandards() {
    return walkStandard;
  }

  // Setter

  public void setFontSize(String fontSize) {

    // VBox root = (VBox) positionsFlowPane.getScene().lookup("#root");
    // root.setStyle("-fx-font-size:" + fontSize + "pt;");
    preferences.setProperty(KEY_FONT_SIZE, fontSize);
  }

  public void setLanguages(String languages) {

    preferences.setProperty(KEY_LANGS, languages);
  }

  public void setWalkStandard(File file) {

    // myMenuBar.setCurrentWalkPreset(file);

    String path = file.getPath();
    walkStandard = WalkGraphicsStrategy.createStandard(path);

    preferences.setProperty(KEY_WALK_PRESET, path);

    if (fileObserver != null) fileObserver.stop();
    clearImages();
  }

  public void setSideViewStandard(File file) {

    // myMenuBar.setCurrentSideViewPreset(file);

    String path = file.getPath();
    sideViewStandard = SideViewStrategy.createStandard(path);

    preferences.setProperty(KEY_SIDE_VIEW_PRESET, path);

    if (fileObserver != null) fileObserver.stop();
    clearImages();
  }

  void setConfigStageInstance() {
    // ConfigStage cs = new ConfigStage(positionsFlowPane, this);
    // configStageOpt = Optional.ofNullable(cs);
    setConfigs();
  }

  void setInitAlwaysOnTop() {

    // String a = preferences.getProperty(KEY_ALWAYS_ON_TOP).orElse("false");
    // boolean alwaysOnTop = Boolean.valueOf(a);
    // getStage().setAlwaysOnTop(alwaysOnTop);
    // myMenuBar.setAlwaysOnTop(alwaysOnTop);
  }

  private MyFileChooser imagefileChooser;
  private MyFileChooser walkPresetFileChooser;
  private MyFileChooser sideViewPresetFileChooser;

  // ファイル
  @FXML private Menu fileMenu;
  @FXML private MenuItem openCharaChipMenuItem;
  @FXML private MenuItem openSideViewMenuItem;
  @FXML private Menu openWalkRecentMenu;
  @FXML private Menu openSideViewRecentMenu;
  @FXML private MenuItem closeMenuItem;
  @FXML private MenuItem currentWalkPresetMenuItem;
  @FXML private MenuItem currentSideViewPresetMenuItem;
  @FXML private MenuItem walkPresetMenuItem;
  @FXML private MenuItem sideViewPresetMenuItem;
  @FXML private MenuItem editWalkPresetMenuItem;
  @FXML private MenuItem editSideViewPresetMenuItem;
  @FXML private MenuItem preferencesMenuItem;
  @FXML private MenuItem quitMenuItem;
  @FXML private MenuItem forcedTerminateMenuItem;

  // 表示
  @FXML private Menu displayMenu;
  @FXML private MenuItem previousMenuItem;
  @FXML private MenuItem nextMenuItem;
  @FXML private MenuItem zoomDownMenuItem;
  @FXML private MenuItem zoomUpMenuItem;
  @FXML private MenuItem durationDownMenuItem;
  @FXML private MenuItem durationUpMenuItem;
  @FXML private MenuItem onlyMenuItem;
  @FXML private CheckMenuItem alwaysOnTopMenuItem;

  // 言語変更メニュー
  @FXML private Menu langsMenu;
  @FXML private ToggleGroup langGroup;
  @FXML private RadioMenuItem jpRadioMenuItem;
  @FXML private RadioMenuItem usRadioMenuItem;

  // コンストラクタ

  // public MyMenuBar() {

  //   imagefileChooser = new MyFileChooser.Builder("Image Files", "*.png").build();
  //   walkPresetFileChooser =
  //       new MyFileChooser.Builder("Preset Files", "*.preset").initDir(WALK_PREST_DIR).build();
  //   sideViewPresetFileChooser =
  //       new MyFileChooser.Builder("Preset Files",
  // "*.preset").initDir(SIDE_VIEW_PREST_DIR).build();

  //   URL location = getClass().getResource("my_menu_bar.fxml");
  //   ResourceBundle resources =
  //       ResourceBundle.getBundle(
  //           "com.jiro4989.tkcas.menubar.dict",
  //           Locale.getDefault(),
  //           ResourceBundleWithUtf8.UTF8_ENCODING_CONTROL);
  //   FXMLLoader loader = new FXMLLoader(location, resources);

  //   loader.setRoot(this);
  //   loader.setController(this);

  //   try {
  //     loader.load();

  //     fontSize8RadioMenuItem.setOnAction(e -> setFontSize(fontSize8RadioMenuItem));
  //     fontSize9RadioMenuItem.setOnAction(e -> setFontSize(fontSize9RadioMenuItem));
  //     fontSize10RadioMenuItem.setOnAction(e -> setFontSize(fontSize10RadioMenuItem));
  //     fontSize11RadioMenuItem.setOnAction(e -> setFontSize(fontSize11RadioMenuItem));
  //     fontSize12RadioMenuItem.setOnAction(e -> setFontSize(fontSize12RadioMenuItem));

  //     changeSelectedFontMenuItem();

  //   } catch (IOException e) {
  //     e.printStackTrace();
  //   }
  // }

  // FXMLイベント
  // ファイルメニュー
  @FXML
  private void openCharaChipMenuItemOnAction() {

    imagefileChooser
        .openFile()
        .ifPresent(
            file -> {
              // Standards std = mainController.getWalkStandards();
              // TrimmingSelector ts = new TrimmingSelector(file, std);
              // ts.showAndWait();

              // setDisables(false);
              // mainController.drawWalkImage(file);

              // String path = file.getPath();
              // openWalkRecentMenu.getItems().add(createMenuItemHasWalkAction(path));
            });
  }

  @FXML
  private void openSideViewMenuItemOnAction() {
    imagefileChooser
        .openFile()
        .ifPresent(
            file -> {
              // setDisables(false);
              // mainController.drawSideViewImage(file);

              // String path = file.getPath();
              // openSideViewRecentMenu.getItems().add(createMenuItemHasSideViewAction(path));
            });
  }

  @FXML
  private void openWalkRecentMenuItemOnAction() {
    imagefileChooser
        .openFile()
        .ifPresent(
            file -> {
              // // FIXME NullPointerexception
              // mainController.drawWalkImage(file);
            });
  }

  @FXML
  private void openSideViewRecentMenuItemOnAction() {
    imagefileChooser
        .openFile()
        .ifPresent(
            file -> {
              // // FIXME NullPointerexception
              // mainController.drawWalkImage(file);
            });
  }

  @FXML
  private void closeMenuItemOnAction() {
    // mainController.clearImages();
    // setDisables(true);
  }

  @FXML
  private void walkPresetMenuItemOnAction() {
    walkPresetFileChooser
        .openFile()
        .ifPresent(
            file -> {
              // mainController.setWalkStandard(file);
              // setCurrentWalkPreset(file);
            });
  }

  @FXML
  private void sideViewPresetMenuItemOnAction() {
    sideViewPresetFileChooser
        .openFile()
        .ifPresent(
            file -> {
              // mainController.setSideViewStandard(file);
              // setCurrentSideViewPreset(file);
            });
  }

  @FXML
  private void editWalkPresetMenuItemOnAction() {
    walkPresetFileChooser
        .saveFile()
        .ifPresent(
            file -> {
              // PresetEditor editor = new PresetEditor(file);
              // editor.showAndWait();
              // mainController.setWalkStandard(file);
              // setCurrentWalkPreset(file);
            });
  }

  @FXML
  private void editSideViewPresetMenuItemOnAction() {
    sideViewPresetFileChooser
        .saveFile()
        .ifPresent(
            file -> {
              // SideViewEditor editor = new SideViewEditor(file);
              // editor.showAndWait();
              // mainController.setSideViewStandard(file);
              // setCurrentSideViewPreset(file);
            });
  }

  @FXML
  private void preferencesMenuItemOnAction() {
    // mainController.showConfigStage();
  }

  @FXML
  private void quitMenuItemOnAction() {
    // mainController.closeRequest();
    // Platform.exit();
  }

  @FXML
  private void forcedTerminateMenuItemOnAction() {
    DialogUtils.showForcedTerminationDialog();
  }
  // 表示メニュー
  @FXML
  private void alwaysOnTopMenuItemOnAction() {
    // mainController.changeAlwaysOnTop();
  }

  @FXML
  private void previousMenuItemOnAction() {
    // mainController.showPreviousImage();
  }

  @FXML
  private void nextMenuItemOnAction() {
    // mainController.showNextImage();
  }

  @FXML
  private void zoomDownMenuItemOnAction() {

    // mainController.zoomDownImages();
  }

  @FXML
  private void zoomUpMenuItemOnAction() {

    // mainController.zoomUpImages();
  }

  @FXML
  private void durationDownMenuItemOnAction() {
    // mainController.durationDown();
  }

  @FXML
  private void durationUpMenuItemOnAction() {
    // mainController.durationUp();
  }

  @FXML
  private void onlyMenuItemOnAction() {

    // mainController.showOneImage();
  }
  // 言語メニュー
  @FXML
  private void jpRadioMenuItemOnAction() {

    // DialogUtils.showLanguageDialog();
    // String langs = Locale.JAPAN.getLanguage();
    // mainController.setLanguages(langs);
  }

  @FXML
  private void usRadioMenuItemOnAction() {

    // DialogUtils.showLanguageDialog();
    // String langs = Locale.ENGLISH.getLanguage();
    // mainController.setLanguages(langs);
  }

  // private メソッド

  private void setPresetText(MenuItem item, String text) {

    String current = item.getText();
    String top = current.split(":")[0];
    String newText = top + ": " + text;
    item.setText(newText);
  }

  private MenuItem createMenuItemHasWalkAction(String path) {

    MenuItem item = new MenuItem(path);
    item.setOnAction(
        e -> {
          File file = new File(item.getText());

          if (file.exists()) {

            // Standards std = mainController.getWalkStandards();
            // TrimmingSelector ts = new TrimmingSelector(file, std);
            // ts.showAndWait();

            // setDisables(false);
            // mainController.drawWalkImage(file);
          }
        });

    return item;
  }

  private MenuItem createMenuItemHasSideViewAction(String path) {

    MenuItem item = new MenuItem(path);
    item.setOnAction(
        e -> {
          // setDisables(false);
          // File file = new File(item.getText());
          // if (file.exists()) mainController.drawSideViewImage(file);
        });

    return item;
  }
  // Getter
  public List<String> getRecentOpenedWalkFiles() {

    return openWalkRecentMenu
        .getItems()
        .stream()
        .map(item -> item.getText())
        .collect(Collectors.toList());
  }

  public List<String> getRecentOpenedSideViewFiles() {

    return openSideViewRecentMenu
        .getItems()
        .stream()
        .map(item -> item.getText())
        .collect(Collectors.toList());
  }
  // Setter
  public void setRecentWalkFilePaths(List<String> paths) {
    paths
        .stream()
        .distinct()
        .filter(p -> new File(p).exists())
        .map(this::createMenuItemHasWalkAction)
        .forEach(
            item -> {
              openWalkRecentMenu.getItems().add(item);
            });
  }

  public void setRecentSideViewFilePaths(List<String> paths) {
    paths
        .stream()
        .distinct()
        .filter(p -> new File(p).exists())
        .map(this::createMenuItemHasSideViewAction)
        .forEach(
            item -> {
              openSideViewRecentMenu.getItems().add(item);
            });
  }

  public void setAlwaysOnTop(boolean alwaysOnTop) {
    alwaysOnTopMenuItem.setSelected(alwaysOnTop);
  }

  public void setCurrentWalkPreset(String text) {
    setPresetText(currentWalkPresetMenuItem, text);
  }

  public void setCurrentWalkPreset(File file) {
    setPresetText(currentWalkPresetMenuItem, file.getName());
  }

  public void setCurrentSideViewPreset(String text) {
    setPresetText(currentSideViewPresetMenuItem, text);
  }

  public void setCurrentSideViewPreset(File file) {
    setPresetText(currentSideViewPresetMenuItem, file.getName());
  }

  public void setDisables(boolean disable) {

    onlyMenuItem.setDisable(disable);
    previousMenuItem.setDisable(disable);
    nextMenuItem.setDisable(disable);
    zoomDownMenuItem.setDisable(disable);
    zoomUpMenuItem.setDisable(disable);
    durationDownMenuItem.setDisable(disable);
    durationUpMenuItem.setDisable(disable);
    closeMenuItem.setDisable(disable);
  }

  public void addRecentWalkFile(File file) {
    MenuItem item = createMenuItemHasWalkAction(file.getPath());
    openWalkRecentMenu.getItems().add(item);
  }

  public void addRecentSideViewFile(File file) {
    MenuItem item = createMenuItemHasSideViewAction(file.getPath());
    openSideViewRecentMenu.getItems().add(item);
  }
}
