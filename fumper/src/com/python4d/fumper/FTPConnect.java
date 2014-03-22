package com.python4d.fumper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

import com.badlogic.gdx.Gdx;

public class FTPConnect extends FTPClient {
	

	public String readfile(String filename,String key) {

		int buffersize = 1000;
		ByteArrayOutputStream fis;
		Hashtable<String, String> result = new Hashtable<String, String>();
		String[] linesfile;
		try {
			connect("ftpperso.free.fr", 21);
			Gdx.app.log("FTP/FTPConnect/Open=", getReplyString());
			int reply = getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				disconnect();
				Gdx.app.log("FTP/FTPConnect/Open=",
						"!FTP server refused connection!");
				return "!FTP server refused!";
			}
			boolean b = login("forumpanhard", "baco22");
			setFileType(FTP.ASCII_FILE_TYPE);
			enterLocalPassiveMode();
			fis = new ByteArrayOutputStream(buffersize);
			b = retrieveFile(filename, fis);
			if (!b)
				return "!FTP File refused!";
			// cf http://stackoverflow.com/questions/4539878/strange-string-split-n-behavior
			linesfile = fis.toString().split("[\\r\\n]+");
			if (linesfile.length>0)
				for (String i : linesfile) 
					result.put(i.split("=")[0], i.split("=")[1]);
			logout();
			if (result.get(key)==null)
				return new String("!No Data Available!");
			else
				return result.get(key);
		}
		catch (FTPConnectionClosedException e){
			e.printStackTrace();
			return "!FTP Service Not Available!";
		}
		catch (IOException e) {
			e.printStackTrace();
			return "!No Internet Access!";
		}

	}

	public String writefile(String filename, String[] ... keyvalue) {
		
		try {
			connect("ftpperso.free.fr", 21);
			Gdx.app.log("FTP/FTPConnect/Open=", getReplyString());
			int reply = getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				disconnect();
				Gdx.app.log("FTP/FTPConnect/Open=",
						"!FTP server refused connection.");
				return "!FTP server refused";
			}
			boolean b = login("forumpanhard", "baco22");
			setFileType(FTP.ASCII_FILE_TYPE);
			enterLocalPassiveMode();
			String data=new String();
			for (String[] i:keyvalue)
				data=data+new String(i[0]+"="+i[1]+"\n");
			InputStream in = new ByteArrayInputStream(data.getBytes());
			b = storeFile(filename, in);
			if (!b)
				return "!FTP File refused";
			logout();
			return "HighScore WEB writed !";
		}
		catch (FTPConnectionClosedException e){
			e.printStackTrace();
			return "!FTP Service Not Available";
		}
		catch (IOException e) {
			e.printStackTrace();
			return "!No InternetConnection";
		}

	}
}