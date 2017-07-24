package sample;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class Controller implements Initializable{

    private final Node pcIcon =  new ImageView(new Image(getClass().getResourceAsStream("img/computer.png")));

    private final Image discIcon =  new Image(getClass().getResourceAsStream("img/disc.png"));

    private final Image folderIcon =  new Image(getClass().getResourceAsStream("img/folder.png"));

    private ArrayList<String> str_roots;

    private ObservableList<String> ofiles = FXCollections.observableArrayList();

    private ContextMenu treeContext = new ContextMenu();
    private ContextMenu listContext = new ContextMenu();


    @FXML
    TreeView<String> treeView;

    @FXML
    HBox parent;

    @FXML
    ListView<String> listView;





    @Override
    public void initialize(URL location, ResourceBundle resources) {

        str_roots = new ArrayList<String>();

        TreeItem<String> rootItem = new TreeItem<String> ("Мой компьютер",pcIcon);
        rootItem.setExpanded(true);

        File[] roots = File.listRoots();
        for (File file: roots) {
            if(file.canRead()){

                TreeItem<String> disc = new TreeItem<String> (file.getAbsolutePath(), new ImageView(discIcon));

                str_roots.add(file.getAbsolutePath()); //Список Дисков

                disc.setExpanded(true);

                rootItem.getChildren().add(disc);
            }
        }

        treeView.setRoot(rootItem);


        treeContext.getItems().addAll(new MenuItem("Новый каталог"), new MenuItem("Ленивая загрузка"));
        listContext.getItems().addAll(new MenuItem("Создать файл"), new MenuItem("Переименовать"), new MenuItem("Удалить"));

        //Новый каталог
        treeContext.getItems().get(0).setOnAction(event -> {

            File f = new File(treeView.getSelectionModel().getSelectedItem().getValue() + "\\New Folder");

                Boolean result = f.mkdir();
                if(result){
                    treeView.getSelectionModel().getSelectedItem().getChildren().add(new TreeItem<>(treeView.getSelectionModel().getSelectedItem().getValue() + "\\New Folder", new ImageView(new Image(getClass().getResourceAsStream("img/open_folder.png")))));
                }

        });

        //Ленивая загрузка
        treeContext.getItems().get(1).setOnAction(event -> {
            treeView.getSelectionModel().getSelectedItem().setGraphic(new ImageView(new Image(getClass().getResourceAsStream("img/lazyload.gif"))));
            (new IconHandler()).execute();

        });
        treeView.setContextMenu(treeContext);

        //Функции проводника

        //Добавить
        listContext.getItems().get(0).setOnAction(event -> {

            TextInputDialog dialog = new TextInputDialog("file");
            dialog.setTitle("Создание файла");
            dialog.setHeaderText("Какое имя задать файлу?");
            dialog.setContentText("Имя файла:");


            Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && !result.get().equals("")){

                String file = treeView.getSelectionModel().getSelectedItem().getValue();

                //Формирование пути
                file = (str_roots.contains(file)) ? file + result.get(): file + "\\" + result.get();


                File newFile = new File(file);

                try
                {
                    if(newFile.createNewFile()){

                        loadFilesAndFolders(); // реинициализация списка

                    } else throw new IOException();
                }
                catch(IOException ex){

                    alertThrower(Alert.AlertType.ERROR, "Ошибка", "Файл не может быть создан", "Проверьте наличие одноименного файла в директории.");
                    ex.printStackTrace();

                }

            }

        });

        //Переименование
        listContext.getItems().get(1).setOnAction(event -> {

            String fileName = listView.getSelectionModel().getSelectedItem();

            TextInputDialog dialog = new TextInputDialog(fileName);
            dialog.setTitle("Переименование файла");
            dialog.setHeaderText("Какое имя задать файлу?");
            dialog.setContentText("Новое имя файла:");


            Optional<String> result = dialog.showAndWait();

            if(result.isPresent()){
                String file = treeView.getSelectionModel().getSelectedItem().getValue();



                file = (str_roots.contains(file)) ? file + fileName: file + "\\" + fileName;
                File fileToRename = new File(file);

                String resultName = (str_roots.contains(file)) ? fileToRename.getParent() +  result.get(): fileToRename.getParent() + "\\" +  result.get();
                File newName = new File(resultName);

                if(fileToRename.renameTo(newName)){

                    loadFilesAndFolders();

                }else{
                    alertThrower(Alert.AlertType.ERROR, "Ошибка", "Ошибка переименования файла", "Файл не может быть переименован. Вероятно, в папке уже есть файл с таким именем.");
                }

            }

        });


        //Удаление
        listContext.getItems().get(2).setOnAction(event -> {
            String file = treeView.getSelectionModel().getSelectedItem().getValue();
            String fileName = listView.getSelectionModel().getSelectedItem();

            file = (str_roots.contains(file)) ? file + fileName: file + "\\" + fileName;

            File fileToRemove = new File(file);

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Подтвердите действие");
            alert.setHeaderText("Данная операция безвозвратно удалит выбранный файл");
            alert.setContentText("Вы действительно согласны?");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK){
                fileToRemove.delete();
                loadFilesAndFolders();
            }

        });

        listView.setContextMenu(listContext);

    }


    public void mouseClick(MouseEvent e) {
        if(e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2){
            loadFilesAndFolders();
        }

    }

    private class IconHandler extends SwingWorker<Void, Object> {
        @Override
        public Void doInBackground() {

            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void done() {

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();

                        loadFilesAndFolders();

                        if(item.getValue().matches("\\w:\\\\")){ //Если выбран диск
                            item.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("img/disc.png"))));
                        }else{
                            item.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("img/open_folder.png"))));
                        }
                    } catch (Exception ignore) {}
                }
            });

        }
    }


    private void loadFilesAndFolders(){
        TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();

        if(item.getValue().equals("Мой компьютер")){
            return;
        }

        if(!str_roots.contains(item.getValue())){ //Если пользователь кликнул не по диску
            if(item.isExpanded()){
                item.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("img/open_folder.png"))));
            }else{
                item.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("img/folder.png"))));
            }
        }


        ofiles.clear();

        File file = new File(item.getValue());

        File[] fileList = file.listFiles();
        try{
            for (File f: fileList  ) {

                if(f.isFile() ){

                    ofiles.add(f.getName());

                }

                if(f.isDirectory() && f.canRead() && f.canWrite()){


                    TreeItem<String> folder = new TreeItem<String> (f.getAbsolutePath(), new ImageView(folderIcon));

                    folder.setExpanded(true);


                    folder.expandedProperty().addListener(new ChangeListener<Boolean>() {
                        @Override
                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) { //Раскрытие/закрытие списка через иконку стрелки

                            BooleanProperty bb = (BooleanProperty) observable;
                            TreeItem t = (TreeItem) bb.getBean();

                            if(newValue){ //Папка открыта
                                t.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("img/open_folder.png"))));

                            }else{
                                t.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("img/folder.png"))));
                            }
                        }
                    });

                    //Предотвращение дублирования
                    boolean hasBrothers = false;
                    for (TreeItem<String> child : item.getChildren()) {
                        if(child.getValue().equals(folder.getValue())){
                            hasBrothers = true;
                            break;
                        }
                    }

                    if(!hasBrothers){
                        item.getChildren().add(folder);
                    }

                }

            }

            //Установка иконок файлам
            listView.setCellFactory(var -> new ListCell<String>() {

                private ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("img/unknown_file.png")));

                @Override
                public void updateItem(String name, boolean empty) {
                    super.updateItem(name, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {


                        int formatPosition = name.lastIndexOf(".");

                        if(formatPosition != -1){
                            String format = name.substring(formatPosition);

                            switch (format){
                                case ".java" : imageView.setImage(new Image(getClass().getResourceAsStream("img/java.png")));
                                    break;
                                case ".doc" : imageView.setImage(new Image(getClass().getResourceAsStream("img/doc.png")));
                                    break;
                                case ".txt" : imageView.setImage(new Image(getClass().getResourceAsStream("img/txt.png")));
                                    break;
                                case ".zip" : imageView.setImage(new Image(getClass().getResourceAsStream("img/zip.png")));
                                    break;
                                case ".mp3" : imageView.setImage(new Image(getClass().getResourceAsStream("img/mp3.png")));
                                    break;
                                case ".rar" : imageView.setImage(new Image(getClass().getResourceAsStream("img/rar.png")));
                                    break;
                                case ".jpg" : imageView.setImage(new Image(getClass().getResourceAsStream("img/jpg.png")));
                                    break;
                            }
                        }

                        setText(name);
                        setGraphic(imageView);
                    }
                }
            });

            listView.setItems(ofiles);

        }catch (Exception ex){

            alertThrower(Alert.AlertType.ERROR, "Ошибка", "Произошла ошибка", "Убедитесь в доступности диска или папки.");
            ex.printStackTrace();

        }
    }

    private void alertThrower(Alert.AlertType type, String title, String headerText, String cTest){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(cTest);

        alert.showAndWait();
    }



}



