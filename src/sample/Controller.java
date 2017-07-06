package sample;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    private final Node pcIcon =  new ImageView(new Image(getClass().getResourceAsStream("img/computer.png")));

    private final Image discIcon =  new Image(getClass().getResourceAsStream("img/disc.png"));

    private final Image folderIcon =  new Image(getClass().getResourceAsStream("img/folder.png"));
    private final Node openFolderIcon =  new ImageView(new Image(getClass().getResourceAsStream("img/open_folder.png")));

    private ArrayList<String> str_roots;

    @FXML
    TreeView<String> treeView;

    @FXML
    HBox parent;



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

    }


    public void mouseClick(MouseEvent e) {
        if(e.getClickCount() == 2){
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


            File file = new File(item.getValue());

            File[] fileList = file.listFiles();
            try{
                for (File f: fileList  ) {

                    if(f.isFile() ){
                        //System.out.println(f.getName());
                    }

                    if(f.isDirectory() && f.canRead() && f.canWrite()){
                        //System.out.println(f.getAbsolutePath());
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

                        item.getChildren().add(folder);
                    }

                }
            }catch (Exception ex){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Произошла ошибка");
                alert.setContentText("Убедитесь в доступности диска или папки.");

                alert.showAndWait();

                ex.printStackTrace();
            }


        }

    }


    public void mouseContext(Event event) {
    }
}