/*
	Downloader
	@Author Moisés Montaño Copca - A01271656
	Final project for Programming Languages class

	Copyright (C) 2018  Moisés Montaño Copca

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

	Example links to download: 
	http://www.mcmbuzz.com/wp-content/uploads/2014/07/025Pikachu_OS_anime_4.png
	http://www.islamicity.com/multimedia/radio/ch152/MalikQuranTransaltion.pdf
	https://free4kwallpapers.com/no-watermarks/originals/2018/04/15/an-edit-of-a-thumbnail-wallpaper.jpg
	http://downloads.4ksamples.com/downloads/SES.Astra.UHD.Test.2.2160p.UHDTV.HEVC.x265-LiebeIst.mkv
	http://downloads.4ksamples.com/downloads/sample-Elysium.2013.2160p.mkv
	http://downloads.4ksamples.com/downloads/SKYFALL%204K%20(Ultra%20HD)%20(4ksamples.com).mp4
*/
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.ProgressBar;

import java.util.LinkedList;
import java.util.Arrays;

/*
	Class Downloader
	It extends the Application class to have a graphic interface
	and has the main method, which launches the app
*/
public class Downloader extends Application{

	Scene scene;
	GridPane grid;
	Text title;
	static Text total;
	Label total_label;
	static double downloaded;
	Button start, stop, add_link;
	LinkedList<Label> file_number, file_size;
	LinkedList<TextField> url_field, filename;
	LinkedList<ProgressBar> progressBar;
	LinkedList<Download> downloads;
	LinkedList<Thread> threads;
	int link_counter;

	// Main method
	public static void main(String[] args){
		launch(args);
	}

	@Override
    public void start(Stage primaryStage) {
    	link_counter = 0;
			primaryStage.setTitle("Downloader");
      
			grid = new GridPane();
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(25, 25, 25, 25));

			scene = new Scene(grid, 700, 300);
			primaryStage.setScene(scene);

			start = new Button("Start Download");
			stop = new Button("Cancel");
			stop.setDisable(true);
			add_link = new Button("+");
			title = new Text("DOWNLOADER");
			downloaded = 0.0;
			total = new Text("0 MB");
			total_label = new Label("Total: ");

			url_field = new LinkedList<TextField>();
			filename = new LinkedList<TextField>();
			file_number = new LinkedList<Label>();
			file_size = new LinkedList<Label>();
			progressBar = new LinkedList<ProgressBar>();
			downloads = new LinkedList<Download>();
			threads = new LinkedList<Thread>();

			file_number.push(new Label("File " + (link_counter + 1)) );
			file_size.push(new Label("0 MB") );
			url_field.push(new TextField("URL"));
			filename.push(new TextField("./downloads/filename.pdf"));
			progressBar.push(new ProgressBar(0));
			link_counter++;

			HBox row = new HBox();
			row.setSpacing(10);
			row.getChildren().addAll(file_number.get(0), url_field.get(0), filename.get(0), progressBar.get(0), file_size.get(0));

			VBox links_container = new VBox();
			links_container.setSpacing(10);
			links_container.getChildren().add(row);

			// Order on the grid
			grid.add(title, 0, 0, 2, 1);
			grid.add(links_container, 0, 2);
			grid.add(add_link, 0, 3);
			grid.add(total_label, 1, 4);
			grid.add(total, 2, 4);
			grid.add(start, 2, 5);
			grid.add(stop, 3, 5);

			// Button to add another link for download
			add_link.setOnAction(new EventHandler<ActionEvent>(){
				@Override
				public void handle(ActionEvent e){
					file_number.push(new Label("File " + (link_counter + 1)) );
					file_size.push(new Label("0 MB") );
					url_field.push(new TextField("URL"));
					filename.push(new TextField("./downloads/filename.pdf"));
					progressBar.push(new ProgressBar(0));
					link_counter++;
					if(link_counter == 25){ // Max links is 25
						add_link.setDisable(true);
					}
					HBox new_row = new HBox();
					new_row.setSpacing(10);
					new_row.getChildren().addAll(file_number.get(0), url_field.get(0), filename.get(0), progressBar.get(0), file_size.get(0));
					links_container.getChildren().add(new_row);
				}
			});

			// Add event handlers on each button (pause and start)
			stop.setOnAction(new EventHandler<ActionEvent>(){
				@Override
				public void handle(ActionEvent e){
					// Stop threads
					stop.setDisable(true);
					start.setDisable(false);
					for(int i = 0; i < link_counter; i++){
						threads.get(i).interrupt();
						try{
							threads.get(i).join();
						}catch(Exception ex){
							System.out.println(ex);
						}
				  	progressBar.get(i).progressProperty().unbind();
	          file_size.get(i).textProperty().unbind();
						threads.remove(i);
						downloads.remove(i);
					}
				}
			});

			
			start.setOnAction(new EventHandler<ActionEvent>(){
			  @Override
			  public void handle(ActionEvent e) {
			  	// Start download threads and block textfields
			  	stop.setDisable(false);
			  	start.setDisable(true);
			  	for(int i = 0; i < link_counter; i++){	
				  	downloads.add(new Download(url_field.get(i).getText(), filename.get(i).getText()) );

				  	progressBar.get(i).progressProperty().unbind();
	      		progressBar.get(i).progressProperty().bind(downloads.get(i).progressProperty());
	      		// Bind file_size label to messageProperty
	          file_size.get(i).textProperty().unbind();
	          file_size.get(i).textProperty().bind(downloads.get(i).messageProperty());
				  	threads.push(new Thread(downloads.get(i)));
				  	threads.get(0).start();
			  	}
		    }
			});
			
      primaryStage.show();
  }

  public synchronized static void increaseTotal(double c){
  	Platform.runLater(new Runnable(){
			@Override
			public void run(){
		  	downloaded += c;
		  	total.setText(String.format("%.02f", downloaded) + " MB");
			}
		});
  }

}