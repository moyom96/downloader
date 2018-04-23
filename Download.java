/*
	@Author Moisés Montaño Copca - A01271656

	Copyright (C) 2018 Moisés Montaño Copca

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
*/
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Channels;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import javafx.concurrent.Task;

public class Download extends Task<Void>{
	private URL web_url;
	private String filename;
	private double downloaded;

	// Constructor
	public Download(String url, String filename){
		try{
			this.web_url = new URL(url);
			this.filename = filename;
			this.downloaded = 0.0;
		}catch(Exception e){
			System.out.println("Exception caught, please write a real URL: " + e);
		}
	}

	public void reset(){
		this.downloaded = 0.0;
		updateMessage("0 MB");
		updateProgress(0, 10);
	}

	/*
		run() method, first add and remove persons until we have reached the day limit,
		then remove each person that is inside, before closing.
	*/
	@Override
	public Void call(){
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try{
			System.setProperty("http.agent", "Chrome");
      updateProgress(5, 100);
	  	in = new BufferedInputStream(this.web_url.openStream());
      updateProgress(15, 100);
			fout = new FileOutputStream(this.filename);
      updateProgress(25, 100);
			final byte data[] = new byte[1024]; // An array of 1024 bytes to read the input (equals 1 KB)
      int count;
      double aux, total = 0.0; // Total will go from 0 to 60 (representing MB)
      while ((count = in.read(data, 0, 1024)) != -1 && !(Thread.currentThread().isInterrupted())) {
          fout.write(data, 0, count);
          aux = count/1024.0; // KB
          total += aux/1024.0; // MB
          this.downloaded += aux/1024.0;
          Downloader.increaseTotal(aux/1024.0);
          updateMessage(String.format("%.02f", this.downloaded) + " MB");
          total = (total > 60)? 60 : total;
          updateProgress(25 + (int)total, 100);
      }
      if(Thread.currentThread().isInterrupted()) {
        System.out.println("Interrupted " + this.filename);
        reset();
	  		if(in != null){
	  			in.close();
	  		}
	  		if(fout != null){
	      	fout.close();
	  		}
        return null;
      }
      updateProgress(85, 100);
      System.out.println("Finished downloading " + this.filename);
  	}catch(Exception ex){
  		System.out.println("Exception: " + ex);
  	}finally{
  		try{
	  		if(in != null){
	  			in.close();
	  		}
	  		if(fout != null){
	      	fout.close();
	  		}
  		}catch(Exception ioe){
  			System.out.println("Exception at finally: " + ioe);
  		}
  	}

    updateProgress(100,100);
  	return null;
	}
}