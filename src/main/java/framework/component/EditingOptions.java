package framework.component;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Paint;

public class EditingOptions extends BorderPane {

	private GridPane grid;
	private Slider vidSlider;
	private Slider audioSlider;
	private Slider masterSlider;

	public EditingOptions() {
		grid = new GridPane();
		this.setBackground(new Background(new BackgroundFill(Paint.valueOf("White"), CornerRadii.EMPTY, Insets.EMPTY)));

		ColumnConstraints col1 = new ColumnConstraints();
		col1.setMinWidth(50);
		col1.setPrefWidth(100);
		col1.setMaxWidth(300);
		col1.setHgrow(Priority.ALWAYS);
		col1.setHalignment(HPos.CENTER);

		RowConstraints row1 = new RowConstraints();
		row1.setMinHeight(10);
		row1.setPrefHeight(30);
		row1.setVgrow(Priority.ALWAYS);

		RowConstraints row2 = new RowConstraints();
		row2.setMinHeight(10);
		row2.setPrefHeight(30);
		row2.setVgrow(Priority.ALWAYS);

		RowConstraints row3 = new RowConstraints();
		row3.setMinHeight(10);
		row3.setPrefHeight(30);
		row3.setVgrow(Priority.ALWAYS);

		grid.getColumnConstraints().addAll(col1);
		grid.getRowConstraints().addAll(row1, row2, row3);

		setCenter(grid);

		grid.add(new Label("Video"), 0, 0);
		grid.add(new Label("Audio"), 1, 0);
		grid.add(new Label("Master"), 2, 0);

		vidSlider = new Slider();
		vidSlider.setOrientation(Orientation.VERTICAL);
		vidSlider.setMax(1);
		vidSlider.setValue(1);

		audioSlider = new Slider();
		audioSlider.setOrientation(Orientation.VERTICAL);
		audioSlider.setMax(1);
		audioSlider.setValue(1);

		masterSlider = new Slider();
		masterSlider.setOrientation(Orientation.VERTICAL);
		masterSlider.setMax(1);
		masterSlider.setValue(1);

		grid.add(vidSlider, 0, 1);
		grid.add(audioSlider, 1, 1);
		grid.add(masterSlider, 2, 1);
	}

	public DoubleProperty getVideoProperty() {
		return vidSlider.valueProperty();
	}

	public DoubleProperty getAudioProperty() {
		return audioSlider.valueProperty();
	}

	public DoubleProperty getMasterProperty() {
		return masterSlider.valueProperty();
	}

}
