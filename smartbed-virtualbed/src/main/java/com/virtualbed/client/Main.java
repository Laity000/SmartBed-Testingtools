package com.virtualbed.client;



import com.virtualbed.stage.StageController;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


/**
 *
 * @Title: Main.java
 * @Description: TODO 程序入口
 * @author ZhangJing   https://github.com/Laity000/ChatRoom-JavaFX
 * @date 2017年5月17日 上午11:19:59
 *
 */
public class Main extends Application {

    public final static String BedUIID = "BedUI";
    public final static String BedUIRes = "fxml/BedUI.fxml";

   // public final static String ChatUIID = "ChatUI";
   // public final static String ChatUIRes = "fxml/ChatUI.fxml";

   // public final static String EmojiSelectorUIID = "EmojiSelectorUI";
   // public final static String EmojiSelectorUIRes = "fxml/EmojiSelectorUI.fxml";

    private StageController stageController;

	@Override
	public void start(Stage primaryStage) {
		try {
			//新建一个StageController控制器
	        stageController = new StageController();

	        //将主舞台交给控制器处理
	        stageController.addPrimaryStage(primaryStage);

	        //加载两个舞台，每个界面一个舞台
	        //stageController.loadStage(LoginUIID, LoginUIRes);
	        //stageController.loadStage(ChatUIID, ChatUIRes);
	        stageController.loadStage(BedUIID, BedUIRes, false, StageStyle.UNDECORATED);
	        //stageController.loadStage(ChatUIID, ChatUIRes, false, StageStyle.UNDECORATED);
	        //stageController.loadStage(EmojiSelectorUIID, EmojiSelectorUIRes, true, StageStyle.UNDECORATED);

	        //显示MainView舞台
	        stageController.showStage(BedUIID);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
