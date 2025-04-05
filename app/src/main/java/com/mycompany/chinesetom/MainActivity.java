package com.mycompany.chinesetom;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.content.Intent;
import android.os.Bundle;
import android.provider.DocumentsContract;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.net.URLEncoder;
import android.icu.util.*;
import android.widget.*;
import java.util.*;
import android.app.*;
import android.content.*;

public class MainActivity extends Activity {

    private TextView resultTextView;
    private EditText toConvert;
    private Button downloadButton;
	private String[] toConvertList;
	private TextView showLog;
	private String logText;
	private Spinner voiceTimbre;
	private List<String> timbres;
	private String selectedTimbre;
	private static final int REQUEST_CODE_DIR_SELECT = 1002;
	//private static final int REQUEST_CODE_FILE_SELECT = 1001;
	//private Uri selectedFileUri;
	private Button selectPath;
	
// 在类顶部添
	private String customDownloadPath = null; // 用户自定义的下载路径
    //private static final String BASE_DIR = Environment.getExternalStorageDirectory() + 
	//"/MikuriVoice/AutoDownloadTest/";
	private static final String BASE_DIR = 
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
    "/YoukuriVoice/";
	//private String filePath;
    private static final int STORAGE_PERMISSION_CODE = 1;
    private String lastConvertedText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initializeViews();
        setupButtonClickListener();
        checkStoragePermission();
		selectTimbre();
		//chooseDownloadDirectory();
		
		
    }
	
	/*
	private void chooseDownloadDirectory() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			// 使用系统的目录选择器（Android 5.0+）
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
			startActivityForResult(intent, REQUEST_CODE_DIR_SELECT);
		} else {
			// 旧版本Android可以使用文件选择器选择目录
			Toast.makeText(this, "请手动输入存储路径", Toast.LENGTH_SHORT).show();
		}
		
	}*/

    private void initializeViews() {
        resultTextView = findViewById(R.id.viewresult);
        toConvert = findViewById(R.id.toCvt);
        downloadButton = findViewById(R.id.downloadButton);
		selectPath=findViewById(R.id.pathCosing);
		showLog = findViewById(R.id.processLog);
		logText="";
		voiceTimbre = findViewById(R.id.timbre);
		timbres=new ArrayList<String>();
		timbres.add("f1e(chirno)");
		timbres.add("f1(reimu,marisa,etc)");
		
		
		
		ArrayAdapter<String> adapter=new ArrayAdapter<>(
		this,
		android.R.layout.simple_spinner_item,
		timbres
		);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		voiceTimbre.setAdapter(adapter);

        // 移除或隐藏Show按钮
        //Button showButton = findViewById(R.id.show);
        //showButton.setVisibility(View.GONE);
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		
		if (requestCode == REQUEST_CODE_DIR_SELECT && resultCode == RESULT_OK) {
			if (data != null) {
				Uri treeUri = data.getData();
				// 获取永久访问权限（Android 5.0+需要）
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					getContentResolver().takePersistableUriPermission(
						treeUri, 
						Intent.FLAG_GRANT_READ_URI_PERMISSION | 
						Intent.FLAG_GRANT_WRITE_URI_PERMISSION
					);
				}

				// 保存用户选择的目录
				customDownloadPath="/storage/emulated/0/"+DocumentsContract.getTreeDocumentId(
					Uri.parse(treeUri.toString())).toString().replace("primary:","");
				logText+="set dir: "+customDownloadPath+"\n";
				showLog.setText(logText);
				
				//Toast.makeText(this, "下载目录已设置", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	private void selectTimbre(){
		

		voiceTimbre.setOnItemSelectedListener(
			new AdapterView.OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedItem = parent.getItemAtPosition(position).toString();
					selectedTimbre=selectedItem;
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// 未选择任何项时的处理
					selectedTimbre="f1e";
					//select default timbre: chirno
				}
			}
		);
	}

    private void setupButtonClickListener() {
        downloadButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//logText="";
					String originalText = toConvert.getText().toString();
					toConvertList=originalText.replaceAll("\\n","").replaceAll("；",";").split(";");
					if(originalText.isEmpty()) {
						showError("输入文本不能为空!");
						return;
					}

					// 先执行文本转换，然后在转换成功后自动下载
					new TextConversionAndDownloadTask().execute(originalText);
				}
			});
		selectPath.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				openDirectoryChooser();
			}
		});
    }
	
	private void openDirectoryChooser() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
							Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
							Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
			startActivityForResult(intent, REQUEST_CODE_DIR_SELECT);

			// 临时保存原始文本
			//this.toConvertList = originalText.replaceAll("\\n","").replaceAll("；",";").split(";");
		} else {
			Toast.makeText(this, "Android 5.0以下版本请使用默认目录", Toast.LENGTH_SHORT).show();
		}
	}
	
	

    private class TextConversionAndDownloadTask extends AsyncTask<String, Void, String> {
        private Exception exception;

        @Override
        protected String doInBackground(String... params) {
            try {
                String originalText = "<begin>" + params[0] + "<end>";
                String encodedText = encodeText(originalText);
                String response = performHttpRequest(encodedText);
                String convertedText = processResponse(response);

                // 保存转换后的文本用于下载
                lastConvertedText = convertedText;
                return convertedText;
            } catch (Exception e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (exception != null) {
                showError("转换失败: " + exception.getMessage());
            } else if (result != null) {
                resultTextView.setText(result);
                // 转换成功后自动开始下载
				String[] TextList=lastConvertedText.replaceAll("；",";").split(";");
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
				
				for(int i=0;i<TextList.length;i++){
					new AudioDownloadTask().execute(TextList[i],timeStamp,toConvertList[i]);
				}
            } else {
                showError("未知错误");
            }
        }
    }
	

    private class AudioDownloadTask extends AsyncTask<String, Void, String> {
        private Exception exception;

        @Override
        protected String doInBackground(String... params) {
            String textToDownload = params[0];
            String encodedText = encodeText(textToDownload);
			
			//String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
			String timeStamp=params[1];
			//String[] TextList=encodedText.split(";");
			//for(String name:TextList){
			//www.yukumo.net/api/v2/aqtk10/koe.mp3?type=f1e&speed=80&volume=100&pitch=100&accent=100&lmd=61&fsc=148&kanji=
			//www.yukumo.net/api/v2/aqtk1/koe.mp3?type=f1&kanji=
			String fileUrl;
            if(selectedTimbre=="f1(reimu,marisa,etc)"){
				fileUrl="https://www.yukumo.net/api/v2/aqtk1/koe.mp3?type=f1&kanji="+encodedText;
			}
			else{
				fileUrl = "https://www.yukumo.net/api/v2/aqtk10/koe.mp3?type="+selectedTimbre.replaceAll("\\(.*?\\)","")+"&speed=80&volume=100&pitch=100&accent=100&lmd=61&fsc=148&kanji=" + encodedText;
			}
			
			
			
            // 使用应用专属目录
			
			File directory;
			if(customDownloadPath!=null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
				
				directory = new File(customDownloadPath);
				
			}
			else{
				directory = new File(BASE_DIR);
			}
            //File directory = new File(getExternalFilesDir(null), "AutoDownloadTest");
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    return null;
                }
            }

            
		
            String fileName = params[2] + selectedTimbre.replaceAll(".*?\\(","\\(") + "(" + timeStamp + ")" + ".mp3";
			//resultTextView.setText(toConvertList[0]);
            File outputFile = new File(directory, fileName);

            try {

				BufferedInputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
				FileOutputStream out = new FileOutputStream(outputFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

                return outputFile.getAbsolutePath();

            } catch (IOException e) {
                exception = e;
                return null;
            }
			}
			
			//}
        

        @Override
        protected void onPostExecute(String filePath) {
            if (exception != null) {
                logText+="下载失败: 字数过多，请注意断句，字数请勿超过50字" 
				+exception
				+"\n";
            } else if (filePath != null) {
                //Toast.makeText(MainActivity.this, 
				  //		   "语音文件已保存到: " + filePath, 
				  //		   Toast.LENGTH_LONG).show();
				logText+="语音文件已保存到: " + filePath + "\n";
				
            } else {
                showError("下载失败: 未知错误");
            }
			showLog.setText(logText);
        }
		
    }

    // 以下是原有的工具方法保持不变
    private String encodeText(String text) {
        try {
            return URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            throw new RuntimeException("编码失败", e);
        }
    }

    private String performHttpRequest(String encodedText) throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL("https://ltool.net/chinese-simplified-and-traditional-characters-pinyin-to-katakana-converter-in-english.php");
            String postData = "contents=" + encodedText + "&firstinput=OK&option=1&optionext=zenkaku";

            connection = (HttpURLConnection) url.openConnection();
            setupConnection(connection, postData);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP错误代码: " + responseCode);
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            return readResponse(reader);
        } finally {
            if (reader != null) reader.close();
            if (connection != null) connection.disconnect();
        }
    }

    private void setupConnection(HttpURLConnection connection, String postData) throws IOException {
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Java)");
        connection.setRequestProperty("Accept", "text/html");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(postData.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String readResponse(BufferedReader reader) throws IOException {
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        String psdStr = response.substring(56250);
        return psdStr;
    }

    private String processResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new RuntimeException("服务器返回空响应");
        }

        String content = response.substring(response.indexOf("&lt;begin&gt;")+13, response.indexOf("&lt;end&gt;"));
        content = content.replaceAll("\\s+", "")
			.replaceAll("<spanstyle=color:#aaaaaa>.*?</span>", "");
        return content;
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) 
				!= PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, 
										   @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && 
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "存储权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "需要存储权限才能保存文件", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showError(String message) {
        Toast.makeText(MainActivity.this, "错误: " + message, Toast.LENGTH_LONG).show();
        resultTextView.setText("操作失败");
    }
}
