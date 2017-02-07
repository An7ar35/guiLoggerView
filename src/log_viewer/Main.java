package log_viewer;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;

import eadjlib.logger.Log_Levels;
import eadjlib.logger.Log_TimeStamp;
import eadjlib.logger.Logger;
import eadjlib.logger.outputs.Log_Window_Interface;

/**
 * Main entry to the application
 * Extends 'Application' for the JavaFX stuff to work and
 * implements Log_Window_Interface for the logger injection
 */
public class Main extends Application implements Log_Window_Interface {
    private ListView<TextFlow> logMsgList = new ListView<>();
    private ObservableList<TextFlow> logData = FXCollections.observableArrayList();
    //Declare the log for the current class
    private final Logger log = Logger.getLoggerInstance(Main.class.getName());

    /**
     * Sets up the window scene
     * @param scene Scene to setup
     */
    private void setupScene(Scene scene) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.autosize();
        grid.setVgap(5);
        grid.setHgap(5);
        grid.add(logMsgList, 0, 0);
        scene.setRoot(grid);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        grid.prefHeightProperty().bind(scene.heightProperty());
        grid.prefWidthProperty().bind(scene.widthProperty());
        logMsgList.setId("LogMsgView");
        addAutoScroll(logMsgList);

        //Binding size to the grid
        logMsgList.prefWidthProperty().bind(grid.prefWidthProperty());
        logMsgList.prefHeightProperty().bind(grid.prefHeightProperty());
        logMsgList.prefWidthProperty().bind(grid.widthProperty());
        logMsgList.prefHeightProperty().bind(grid.heightProperty());
    }

    /**
     * Start GUI
     * @param primaryStage Primary stage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("log_viewer.fxml"));
        primaryStage.setTitle("Logger View");
        Scene scene = new Scene(root, 900, 500);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.sizeToScene();

        logMsgList.setItems(logData);
        log.connectView(this); //Connect up the view with the Logger
        setupScene(scene);

        primaryStage.show();

        //Example test log
        for (int i = 0; i < 2000; i++) {
            log.log_Error("Error message example.");
            log.log_Warning( "Warning message example." );
            log.log( "Normal log message example.");
            log.log_Debug( "Debug message example.");
            log.log_Trace("Trace message example.");
        }
        log.log_Fatal("A fatal message example!");
        log.log_Exception( new Exception( "some exception.."));

    }

    /**
     * Adds an auto-scroll
     *
     * @param view ListView object
     */
    private static <S> void addAutoScroll(final ListView<S> view) {
        if (view == null) {
            throw new NullPointerException();
        }

        view.getItems().addListener((ListChangeListener<S>) (c -> {
            c.next();
            final int size = view.getItems().size();
            if (size > 0) {
                view.scrollTo(size - 1);
            }
        }));
    }

    /**
     * Entry to the program
     *
     * @param args Program arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Append a log message to the list
     *
     * @param origin_name Class name of the message
     * @param log_level   Log level
     * @param log_number  Log number
     * @param time_stamp  Time stamp
     * @param objects     Message
     */
    @Override
    public void append(String origin_name, int log_level, Long log_number, Log_TimeStamp time_stamp, Object... objects) {
        String msg = "";
        for (Object o : objects) {
            msg += o;
        }

        Text number = new Text(String.format("[%7d]", log_number));
        Text date = new Text(time_stamp.getDate());
        Text time = new Text(time_stamp.getTime());
        Text level = new Text(Log_Levels.txtLevels[log_level]);
        Text origin = new Text(origin_name);
        Text message = new Text(msg);

        number.setFill(Color.BLUE);
        switch (log_level) {
            case Log_Levels.FATAL:
                level.setFill(Color.CRIMSON);
                break;
            case Log_Levels.ERROR:
                level.setFill(Color.RED);
                break;
            case Log_Levels.WARNING:
                level.setFill(Color.ORANGE);
                break;
            case Log_Levels.MSG:
                break;
            case Log_Levels.DEBUG:
                level.setFill(Color.GREEN);
                break;
            case Log_Levels.TRACE:
                level.setFill(Color.GRAY);
                break;

        }

        origin.setFill(Color.PURPLE);
        TextFlow flow = new TextFlow(
                number, new Text(" "),
                date, new Text(" - "),
                time, new Text(" "),
                level, new Text(" + ["),
                origin, new Text("] "),
                message
        );

        flow.getStyleClass().add("log-msg");
        logData.add(flow);
    }

    /**
     * Appends log exception message to the list
     *
     * @param origin_name Class name of the message
     * @param time_stamp  Time stamp
     * @param log_number  Log number
     * @param e           Exception raised
     */
    @Override
    public void append(String origin_name, Log_TimeStamp time_stamp, Long log_number, Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        Text number = new Text(String.format("[%7d]", log_number));
        Text date = new Text(time_stamp.getDate());
        Text time = new Text(time_stamp.getTime());
        Text origin = new Text(origin_name);
        Text exceptionLevel = new Text( "|EXCEPTION|" );
        Text exceptionTrace = new Text(sw.toString());

        number.setFill(Color.BLUE);
        exceptionLevel.setFill(Color.RED);
        origin.setFill(Color.PURPLE);

        TextFlow flow = new TextFlow(
                number, new Text(" "),
                date, new Text(" - "),
                time, new Text(" "),
                exceptionLevel, new Text( " + ["),
                origin, new Text("] \n\t"),
                exceptionTrace
        );

        flow.getStyleClass().add("log-msg");
        logData.add(flow);
    }
}
