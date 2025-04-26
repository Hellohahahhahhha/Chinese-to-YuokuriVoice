package com.example.chinesetoy;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.view.LayoutInflater;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
//import android.os.Binder;
import android.os.Bundle;
import android.view.View;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.content.Intent;
import android.graphics.PixelFormat;


import android.provider.DocumentsContract;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.lang.reflect.Type;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.Writer;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.net.URLEncoder;
import android.icu.util.*;
import android.widget.*;
import java.util.*;
import android.app.*;
import android.content.*;
import kotlin.*;
//import java.io.*;
public class MainActivity extends AppCompatActivity {

    private TextView resultTextView;
    private EditText toConvert;
    private Button downloadButton;
	private String[] toConvertList;
	private TextView showLog;
	private String logText;
	private Spinner voiceTimbre;
	private List<String> timbres;
	private String selectedTimbre;
	private Spinner choosePerferencePath;
	
    //new 
    private TextView selectedPathByUser;
    private String pathToOperate;
    private Button pathChooser;
    private Button pcrOperator;
    private Button submitRst;
    private Button cancelIpt;
    
    // private Button showEditPath;
    private Button deleteThisPath;
    //private Button editPathName;
    //private Button cancelEditPth;
    private EditText pathNamer;
	private List<UserPaths> pths;
    private UserPaths currentPath;
    private Map<String,String> pathMap;
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
/*
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        checkStoragePermission();
        initializeViews();
        setupButtonClickListener();
        
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
        pathToOperate="";
        selectedPathByUser=findViewById(R.id.selectedPath);
        pathChooser = findViewById(R.id.pathCosing);
        submitRst=findViewById(R.id.submitRst);
        cancelIpt=findViewById(R.id.cancelIpt);
        pathNamer = findViewById(R.id.deitNewPathName);
        pcrOperator=findViewById(R.id.enChoosePath);
        resultTextView = findViewById(R.id.viewresult);
        toConvert = findViewById(R.id.toCvt);
        downloadButton = findViewById(R.id.downloadButton);
		selectPath=findViewById(R.id.pathCosing);
		showLog = findViewById(R.id.processLog);
        deleteThisPath=findViewById(R.id.deleteThisPath);
		choosePerferencePath=findViewById(R.id.pathChoices);
		logText="";
		voiceTimbre = findViewById(R.id.timbre);
		//binding.fab.setOnClickListener(v ->
        //  Toast.makeText(MainActivity.this, "Replace with your action", Toast.LENGTH_SHORT).show()
        //);
		timbres=new ArrayList<String>();
		timbres.add("f1e(chirno)");
		timbres.add("f1(reimu,marisa,etc)");
		
        pths=new JSONFileManager().getAllUsers();
        pathMap=toMap(pths);
        //as the first item of path.json is set to be "default_path"
        currentPath=pths.get(0);
        
		setupPathSpinner();
        
        hidePathEditor();
        showOptions();
		//pths=new ArrayList<>();
		//pths.add(new UserPaths("default_"));



		ArrayAdapter<String> adapter=new ArrayAdapter<>(
			this,
			android.R.layout.simple_spinner_item,
			timbres
		);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		voiceTimbre.setAdapter(adapter);

        // 移除或隐藏Show按钮
        //
    }
    private Map<String,String> toMap(List<UserPaths> paths){
        Map<String,String> ApathMap=new HashMap();
        for(UserPaths up:paths){
            ApathMap.put(up.getName(),up.getPath());
        }
        return ApathMap;
    }
    private UserPaths checkPathExistence(UserPaths newPath){
        for(UserPaths up:pths){
            if(newPath.getName().equals(up.getName())){
                return new UserPaths("",up.getPath());
            }
            if(newPath.getPath().equals(up.getPath())){
                return new UserPaths(up.getName(),"");
            }
            showLogText("teted path "+up.getName()+": "+up.getPath()+"\n");
        }
        return null;
    }
    private void hideOptions(){
        
        pcrOperator.setVisibility(View.GONE);
    }
    private void hidePathEditor(){
        submitRst.setVisibility(View.GONE);
        cancelIpt.setVisibility(View.GONE);
        selectedPathByUser.setVisibility(View.GONE);
        pathToOperate="";
        selectedPathByUser.setText("");
        pathNamer.setText("");
        pathChooser.setVisibility(View.GONE);
        pathNamer.setVisibility(View.GONE);
    }
    private void showOptions(){
        pcrOperator.setVisibility(View.VISIBLE);
    }
    private void showPathEditor(){
        submitRst.setVisibility(View.VISIBLE);
        cancelIpt.setVisibility(View.VISIBLE);
        selectedPathByUser.setVisibility(View.VISIBLE);
        pathChooser.setVisibility(View.VISIBLE);
        pathNamer.setVisibility(View.VISIBLE);
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
                pathToOperate="/storage/emulated/0/"+DocumentsContract.getTreeDocumentId(
					Uri.parse(treeUri.toString())).toString().replace("primary:","");
                String temp="..."+pathToOperate.substring(pathToOperate.length()-15);
                selectedPathByUser.setText(temp);
				logText+="chose dir: "+pathToOperate+"\n";
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
    private void showLogText(String msg){
        logText+=msg;
        showLog.setText(logText);
    }
    
    private void deleteCurrentPath(){
        UserPaths tmp=currentPath.copy();
        
        if(currentPath.getName().equals("default_path")){
            showError("Cannot delete default path");
            return;
        }
        showLogText("deleting path:"+currentPath.getName()+"\n");
        new JSONFileManager().deleteUser(currentPath.getName());
        currentPath=pths.get(0);
        pathMap=toMap(pths);
        setupPathSpinner();
        //showLogText("path: "+tmp.getName()+"---"+tmp.getPath()+" deleted\n");
    }

    private void setupButtonClickListener() {
        deleteThisPath.setOnClickListener(v->{
            // 创建 AlertDialog.Builder 对象
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // 设置对话框标题和消息
            builder.setTitle("Think twice");
            builder.setMessage("You are deleting current path");

            // 设置“是”按钮
            builder.setPositiveButton("Delete", (dialog, which) -> {
                // 用户点击“是”按钮时的逻辑
                deleteCurrentPath();
            });

            // 设置“否”按钮
            builder.setNegativeButton("Cancel", (dialog, which) -> {
                // 用户点击“否”按钮时的逻辑
                dialog.dismiss(); // 关闭对话框
            });

            // 显示对话框
            builder.show();
        });
        choosePerferencePath.setOnItemSelectedListener(
			new AdapterView.OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedItem = parent.getItemAtPosition(position).toString();
                    String pthstr;
                    if(selectedItem.equals("")){
                        if(customDownloadPath.equals("")){
                            //when opening the app
                            customDownloadPath=pths.get(0).getPath();
                            pthstr="default_path";
                        }
                        else{
                            return;
                        }
                    }
                    else{
                        customDownloadPath=pathMap.get(selectedItem);
                        pthstr=selectedItem;
                    }
                    currentPath=new UserPaths(pthstr,customDownloadPath);
                    showLogText("Selected path: "+customDownloadPath+"\n");
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// 未选择任何项时的处理
					
					//select default timbre: chirno
				}
			}
		);
        downloadButton.setOnClickListener(v->{
					//logText="";
					String originalText = toConvert.getText().toString();
					toConvertList=originalText.replaceAll("\\n","").replaceAll("；",";").split(";");
					if(originalText.isEmpty()) {
						showError("输入文本不能为空!");
						return;
					}

					// 先执行文本转换，然后在转换成功后自动下载
					new TextConversionAndDownloadTask().execute(originalText);
		    });
        pcrOperator.setOnClickListener(v->{
            hideOptions();
            showPathEditor();
        });
        cancelIpt.setOnClickListener(v->{
            hidePathEditor();
            showOptions();
        });
        submitRst.setOnClickListener(v->{
                if(pathNamer.getText().toString()=="" || pathToOperate==""){
                    Toast.makeText(MainActivity.this,"invalid input",Toast.LENGTH_LONG).show();
                    return;
                }
                
                UserPaths newPath=new UserPaths(pathNamer.getText().toString(),pathToOperate);
                
                UserPaths checker=checkPathExistence(newPath);
                try{
                    if(checker==null){
                        new JSONFileManager().addUser(newPath);
                        currentPath=newPath;
                        pathMap=toMap(pths);
                        setupPathSpinner();
                        choosePerferencePath.setTooltipText(currentPath.getName());
                        hidePathEditor();
                        showOptions();
                        showLogText("added path\n");
                        return;
                }
                if(checker.getPath()==""){
                    Toast.makeText(MainActivity.this,"Path has been set as "+checker.getName(),Toast.LENGTH_LONG).show();
                    return;
                }
                if(checker.getName()==""){
                    Toast.makeText(MainActivity.this,"Name has been occupied by "+checker.getPath(),Toast.LENGTH_LONG).show();
                    return;
                }}catch(Exception e){showError(e.toString());}
                //use addUser to update pths List
                
        });
        selectPath.setOnClickListener(v->openDirectoryChooser());
        
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


	//Text Conversion
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
                showError("未知错误"+exception.toString());
            }
        }
    }

	//Audio downloading
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
                showError("下载失败: 未知错误"+exception.toString());
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
	
	private class UserPaths{
		private String name;
		private String path;
		
		public UserPaths(){}
		public UserPaths(String n,String p){
			name=n;
			path=p;
		}
		public void setName(String n){
			name=n;
		}
		public void setPath(String p){
			path=p;
		}
		public String getName(){
			return name;
		}
		public String getPath(){
			return path;
		}
		public void clear(){
			name="";
			path="";
		}
		public boolean isInvalid(){
			if(name==""||path==""){
				return true;
			}
			else{
				return false;
			}
		}
        public UserPaths copy(){
            return new UserPaths(this.name,this.path);
        }
	}
	
	private void setupPathSpinner(){
		//pths = new ProcessData().getPathList();
		ArrayList<String> nmeList=new ArrayList<>();
		List<UserPaths> defaultPaths=new ArrayList<>();
		defaultPaths.add(new UserPaths("default_path",BASE_DIR));
		//has set 'pths' just now
		for(UserPaths up:pths){
			nmeList.add(up.getName());
		}
		ArrayAdapter<String> adapter=new ArrayAdapter<>(
			this,
			android.R.layout.simple_spinner_item,
			nmeList
		);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		choosePerferencePath.setAdapter(adapter);
	}
	
	

	

	private class JSONFileManager {
		private String FILE_PATH = "/path.json"; // JSON 文件路径
		private Gson gson;

		public JSONFileManager() {
			this.gson = new GsonBuilder().setPrettyPrinting().create();
            File testFile=new File(getExternalFilesDir(null).toString()+FILE_PATH);
            if(!testFile.exists()){
                try{
                    testFile.createNewFile();
                    List<UserPaths> defaultPaths=new ArrayList<>();
                    defaultPaths.add(new UserPaths("default_path",BASE_DIR));
                    defaultPaths.add(new UserPaths("original_default_path",BASE_DIR));
                    String  json=gson.toJson(defaultPaths);
                    saveUsers(defaultPaths);
                }catch(Exception e){
                    showError(e.toString());
                }
            }
		}

		// 添加用户
		public void addUser(UserPaths user) {
			List<UserPaths> users = loadUsers();
            
			users.add(user);
            pths=users;
			saveUsers(users);
		}

		// 删除用户
		public void deleteUser(String name) {
			List<UserPaths> users = loadUsers();
			for(int i=0;i<users.size();i++){
				if(users.get(i).getName().equals(name)){
					users.remove(i);
                    showLogText("path: "+name+" deleted\n");
					break;
				}
			}
            pths=users;
           // currentPath=pths.get(0);
			saveUsers(users);
            
		}

		// 修改用户
		public void updateUser(String oriName,String newName, String newPath) {
			List<UserPaths> users = loadUsers();
			for (UserPaths user : users) {
				if (user.getName().equals(oriName)) {
					user.setName(newName);
					user.setPath(newPath);
					break;
				}
			}
            pths=users;
			saveUsers(users);
		}

		// 查询所有用户
		public List<UserPaths> getAllUsers() {
			return loadUsers();
		}

		// 查询单个用户
		public UserPaths getUserByName(String userName) {
			List<UserPaths> users = loadUsers();
			for (UserPaths user : users) {
				if (user.getName() == userName) {
					return user;
				}
			}
			return null;
		}

		// 加载用户列表
		private List<UserPaths> loadUsers() {
			try(Reader reader = new FileReader(getExternalFilesDir(null).toString()+FILE_PATH)){
				Type userListType = new TypeToken<List<UserPaths>>() {}.getType();
				return gson.fromJson(reader, userListType);
			} catch (IOException e) {
				e.printStackTrace();
                showError(e.toString());
				return new ArrayList<>();
			}
		}

		// 保存用户列表
		private void saveUsers(List<UserPaths> users) {
			try(Writer writer = new FileWriter(getExternalFilesDir(null).toString()+FILE_PATH)){
				String jsonText=gson.toJson(users);
                writer.write(jsonText);
				//writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/*
	public class User {
		//private int id;
		private String name;
		private String path;

		// 构造方法
		public User(String name, String p) {
			this.name = name;
			this.path = p;
		}

		// Get&Set
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPath() {
			return this.path;
		}

		public void setPath(String p) {
			this.path = p;
		}
	}
	*/
}