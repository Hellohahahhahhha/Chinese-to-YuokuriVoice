package com.example.chinesetoy;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

//import com.github.stuxuhai.jpinyin.PinyinFormat;
//import com.github.stuxuhai.jpinyin.PinyinHelper;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.*;
//
//import com.github.promeg.pinyinhelper.Pinyin;
//import com.github.promeg.pinyinhelper.PinyinMapDict;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
//import android.os.Binder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.net.Uri;
import android.content.Intent;
import android.provider.Settings;


import android.provider.DocumentsContract;

import androidx.annotation.NonNull;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.Writer;
import java.io.FileWriter;
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
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import java.net.URLEncoder;

import android.widget.*;
import java.util.*;
import android.app.*;

//import java.io.*;
public class MainActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 100;  // 权限请求
    private static final int REQUEST_CODE_SAF = 101;         // SAF 文件夹选择

    private TextView resultTextView;
    private EditText toConvert;
    private Button downloadButton;
	private String[] toConvertList;
	private TextView showLog;
	private String logText;
	private Spinner voiceTimbre;
	private List<String> timbres=new ArrayList<>();
	private String selectedTimbre;
	private Spinner choosePreferencePath;
    private Map<String,String> sgMap;
    
    //this contains origin English
    private Map<Integer,String> insertText=new HashMap<>();
	
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
	private List<UserPaths> pths=new ArrayList<>();
    private UserPaths currentPath;
    private Map<String,String> pathMap=new HashMap<>();
	private static final int REQUEST_CODE_DIR_SELECT = 1002;
	//private static final int REQUEST_CODE_FILE_SELECT = 1001;
	//private Uri selectedFileUri;
	private Button selectPath;
    
    private Boolean isPermitted;

// 在类顶部添
	private String customDownloadPath = null; // 用户自定义的下载路径
    //private static final String BASE_DIR = Environment.getExternalStorageDirectory() + 
	//"/MikuriVoice/AutoDownloadTest/";
	private static final String BASE_DIR = 
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
    "/YoukuriVoice/";
	//private String filePath;
    //private static final int STORAGE_PERMISSION_CODE = 1;
    private String lastConvertedText = "";

    private PinyinToKanaMapper mapper = new PinyinToKanaMapper();
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

        //isPermitted=false;

        try {
            mapper.loadMappingFromAssets(this,"mapping.tsv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



        checkAndRequestManageStoragePermission(this);
        
        initializeViews();
        
        //setupInternalFolder();
        
        setupButtonClickListener();
        
		selectTimbre();
        
		
		//chooseDownloadDirectory();
        //showLogText(BASE_DIR,LOG);


    }
    
//    private void setupInternalFolder(){
//        File tmp=new File(getExternalFilesDir(null).toString()+"/convertedAudio/");
//        if(tmp.exists()){
//            showLogText(tmp.getAbsolutePath()+" detected.",LOG);
//        }
//
//    }

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
        logCount=0;
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
		choosePreferencePath =findViewById(R.id.pathChoices);
        
        
        //？！：×÷
        sgMap=new LinkedHashMap<>();
        sgMap.put("?","？ ");
        sgMap.put("!","！ ");
        sgMap.put(":","： ");
        sgMap.put("*","× ");
        sgMap.put("/","÷ ");
        sgMap.put(";","； ");
        sgMap.put("？","？ ");
        sgMap.put("！","！ ");
        sgMap.put("：","： ");
        sgMap.put("×","× ");
        sgMap.put("÷","÷ ");
        sgMap.put("；","； ");
        sgMap.put("，","， ");
        
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
                return new UserPaths("", up.getPath());
            }
            if(newPath.getPath().equals(up.getPath())){
                return new UserPaths(up.getName(), "");
            }
            showLogText("teted path "+up.getName()+": "+up.getPath()+"\n",LOG);
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
				showLogText("chose dir: "+pathToOperate,LOG);

				//Toast.makeText(this, "下载目录已设置", Toast.LENGTH_SHORT).show();
			}
		}
	}
    
    private static String batchReplace(String originalString, Map<String, String> replaceMap) {
        // 构建正则表达式，将Map中的键拼接成一个正则表达式
        StringBuilder regexBuilder = new StringBuilder();
        for (String key : replaceMap.keySet()) {
            if (regexBuilder.length() > 0) {
                regexBuilder.append("|");
            }
            // 对键进行转义，防止键中包含正则表达式的特殊字符
            regexBuilder.append(Pattern.quote(key));
        }
        String regex = regexBuilder.toString();

        // 创建Pattern和Matcher
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(originalString);

        // 使用StringBuilder来构建最终的替换结果
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            // 将上次匹配结束位置到本次匹配开始位置之间的内容添加到结果中
            result.append(originalString, lastEnd, matcher.start());

            // 获取匹配到的键，并从Map中获取对应的替换值
            String match = matcher.group();
            String replacement = replaceMap.get(match);

            // 将替换值添加到结果中
            result.append(replacement);

            // 更新上次匹配结束位置
            lastEnd = matcher.end();
        }

        // 将最后一次匹配结束位置到字符串末尾的内容添加到结果中
        result.append(originalString.substring(lastEnd));

        return result.toString();
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
    private int logCount;
    final private int ERROR=-1; 
    final private int LOG=1;
    final private int WARNING=101;
    final private int UNKNOWN_ERROR=0;
    private void showLogText(String msg,int TYPE){
        logCount++;
        String logType;
        switch(TYPE){
            case ERROR:
            logType=" -- ERROR --\n";
            break;
            case LOG:
            logType=" -- LOG --\n";
            break;
            case UNKNOWN_ERROR:
            logType=" -- UNKNOWN_ERROR --\n";
            break;
            case WARNING:
            logType="-- WARNING--\n";
            break;
            default:
            logType=" -- LOGH --\n";
            break;
        }
        logText=logCount+logType+msg+'\n'+logText;
        showLog.setText(logText);
        
    }
    
    private void deleteCurrentPath(){

        // UserPaths tmp=currentPath.copy();

        
        if(currentPath.getName().equals("default_path")){
            showError("请勿删除默认路径");
            return;
        }
        showLogText("正在删除 "+currentPath.getName()+"\n",LOG);
        new JSONFileManager().deleteUser(currentPath.getName());
        currentPath=pths.get(0);//选默认路径
        pathMap=toMap(pths);
        setupPathSpinner();
    }

    private void setupButtonClickListener() {
        deleteThisPath.setOnClickListener(v->{
            // 创建 AlertDialog.Builder 对象
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // 设置对话框标题和消息
            builder.setTitle("删除当前目录");
            builder.setMessage("你将删除当前目录");
            // 设置“是”按钮
            builder.setPositiveButton("删除", (dialog, which) -> {
                // 用户点击“是”按钮时的逻辑
                deleteCurrentPath();
            });
            // 设置“否”按钮
            builder.setNegativeButton("不删除", (dialog, which) -> {
                // 用户点击“否”按钮时的逻辑
                dialog.dismiss(); // 关闭对话框
            });
            // 显示对话框
            builder.show();
        });
        choosePreferencePath.setOnItemSelectedListener(
			new AdapterView.OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					String selectedItem = parent.getItemAtPosition(position).toString();
                    String pthstr;
                    if(selectedItem.isEmpty()){
                        if(customDownloadPath.isEmpty()){
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
                    currentPath= new UserPaths(pthstr, customDownloadPath);
                    showLogText("Selected path: "+customDownloadPath+"\n",LOG);
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
					String originalText = batchReplace(toConvert.getText().toString(),sgMap);
					toConvertList=originalText.split("；");
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
                if(pathNamer.getText().toString().isEmpty() || pathToOperate.isEmpty()){
                    Toast.makeText(MainActivity.this,"请输入路径名称",Toast.LENGTH_LONG).show();
                    return;
                }
                
                UserPaths newPath= new UserPaths(pathNamer.getText().toString(), pathToOperate);
                
                UserPaths checker=checkPathExistence(newPath);
                try{
                    if(checker==null){
                        new JSONFileManager().addUser(newPath);
                        currentPath=newPath;
                        pathMap=toMap(pths);
                        setupPathSpinner();
                        //??????????
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            choosePreferencePath.setTooltipText(currentPath.getName());
                        }
                        hidePathEditor();
                        showOptions();
                        showLogText("added path\n",LOG);
                        return;
                }
                if(checker.getPath().isEmpty()){
                    Toast.makeText(MainActivity.this,"Path has been set as "+checker.getName(),Toast.LENGTH_LONG).show();
                    return;
                }
                if(checker.getName().isEmpty()){
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
    private class PinyinToKanaMapper {
        private Map<String, String> pinyinToKana = new HashMap<>();

        public void loadMapping(String filePath) throws IOException {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(filePath), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\t");
                    if (parts.length == 2) {
                        pinyinToKana.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        }
        public void loadMappingFromAssets(Context context, String fileName) throws IOException {
            // 通过 AssetManager 打开文件
            try (InputStream is = context.getAssets().open(fileName);
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {

                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\t");
                    if (parts.length == 2) {
                        pinyinToKana.put(parts[0].trim(), parts[1].trim());
                    }
                }
            }
        }

        public String getKana(String pinyin) {
            return pinyinToKana.get(pinyin);
        }

        public boolean containsKey(String pinyin) {
            return pinyinToKana.containsKey(pinyin);
        }
    }

    private class TextConversionAndDownloadTask extends AsyncTask<String, Void, String> {
        private Exception exception;

        @Override
        protected String doInBackground(String... params) {
            try {
                //add
                //String convertedPinyin = PinyinHelper.convertToPinyinString(params[0]," ",PinyinFormat.WITHOUT_TONE);

                HanyuPinyinOutputFormat outputFormat = new HanyuPinyinOutputFormat();

                //set up mapper

                outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
                //outputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
                String convertedPinyin = PinyinHelper.toHanyuPinyinString(params[0].replaceAll("(?<=[\\u4e00-\\u9fa5])(?=[a-zA-Z])", " ")
                        .replaceAll("(?<=[a-zA-Z])(?=[\\u4e00-\\u9fa5])", " "),outputFormat," ");

                //"？","！","：","×","÷","；".replace("e^","ei")
                String[] pinyinArray = convertedPinyin.split(" ");
                StringBuilder kanaResult = new StringBuilder();

                for(String pinyin : pinyinArray){
                    if (pinyin.matches("[\\u4e00-\\u9fa5]")) {
                        kanaResult.append(pinyin);
                        continue;
                    }

                    String kana = mapper.getKana(pinyin);
                    if (kana != null) {
                        kanaResult.append(kana);
                    } else {
                        // 如果映射表中找不到，保留原拼音（或记录日志）
                        System.err.println("未找到映射: " + pinyin);
                        //showLogText();
                        kanaResult.append(pinyin);
                    }
                }



                //String originalText = "<begin>" + params[0] + "<end>";
                
                //String encodedText = encodeText(originalText);
                //String response = performHttpRequest(encodedText);
                //String convertedText = processResponse(response);

                // 保存转换后的文本用于下载
                lastConvertedText = kanaResult.toString();
                return lastConvertedText;
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
				directory = new File(BASE_DIR);//Download/Youkuri/
			}
            //File directory = new File(getExternalFilesDir(null), "AutoDownloadTest");
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    showLogText("应该不能出这个问题……",ERROR);
                    return null;
                }
            }



            String fileName = params[2] + selectedTimbre.replaceAll(".*?\\(","\\(") + "(" + timeStamp + ")" + ".mp3";
			//resultTextView.setText(toConvertList[0]);
            
            //isPermitted=false;
            
            if(isPermitted){
                File outputFile = new File(directory, fileName);
                try{
                    
                    BufferedInputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
				    FileOutputStream out = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                    return outputFile.getAbsolutePath();
                }
                catch(Exception e){
                    exception=e;
                    return null;
                }
            }else{
                
                File outputFile = new File(getExternalFilesDir(null).toString()+"/convertedAudio/",fileName);
                
                try{
                    BufferedInputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
				    FileOutputStream out = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                        
                    }
                    showLogText("Audio file saved to dir "+outputFile.getAbsolutePath()+"/"+fileName+ " due to failed to get permission to access interal storage",WARNING);
                    return outputFile.getAbsolutePath();
                }catch(Exception e){
                    exception=e;
                    //showLogText("Fuckwit! filed",ERROR);
                    return null;
                }
            }
		}

		//}


        @Override
        protected void onPostExecute(String filePath) {
            if (exception != null) {
                if(exception.toString().contains("Operation not permitted")){
                    if(isPermitted){
                        showLogText("请勿输入* / ? :等特殊符号",ERROR);
                    }
                    else{
                        showLogText("请授权访问存储空间",ERROR);
                        checkAndRequestManageStoragePermission(MainActivity.this);
                    }
                }
                else if(exception.toString().contains("https://")){
                    showLogText("下载失败: 字数过多，请注意断句，每段字数请勿超过50字",ERROR);
                }
                
            } else if (filePath != null) {
                //Toast.makeText(MainActivity.this, 
				//		   "语音文件已保存到: " + filePath, 
				//		   Toast.LENGTH_LONG).show();
				showLogText("语音文件已保存到: " + filePath,LOG);

            } else {
                showLogText(exception.toString(),UNKNOWN_ERROR);
            }
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

    private void checkAndRequestManageStoragePermission(AppCompatActivity activity) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){//api >=30
            if(Environment.isExternalStorageManager()){
                isPermitted = true;
            } else{
                requestManageStoragePermission(activity);
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//api >= 23
            // 检查是否已有权限
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 已经有权限
                isPermitted = true;
                //showLogText("PG",LOG);
            }else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // 向用户解释为什么需要权限
                    new AlertDialog.Builder(this)
                            .setTitle("需要存储权限")
                            .setMessage("应用需要存储权限来保存语音文件到您的设备")
                            .setPositiveButton("确定", (dialog, which) -> {
                                // 用户理解后请求权限
                                requestPermissions(
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        STORAGE_PERMISSION_CODE
                                );
                            })
                            .setNegativeButton("(请勿)取消", (dialog,which)->{requestPermissions(
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    STORAGE_PERMISSION_CODE
                            );})
                            .create()
                            .show();
                } else {
                    // 直接请求权限
                    requestPermissions(
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION_CODE
                    );
                }
                isPermitted = true;
            }
        }else {//api <23 no need
            isPermitted = true;
        }
    }

    private boolean abc=false;
    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestManageStoragePermission(AppCompatActivity activity){
        new AlertDialog.Builder(this)
                .setTitle("即将申请管理储存权限")
                .setMessage("为了正确保存下载文件，请授予软件管理设备全部文件的权限")
                .setPositiveButton("去设置", (dialog, which) -> {

                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    //intent.setData(Uri.parse("package:"+activity.getPackageName()));
                    activity.startActivity(intent);

                    isPermitted=true;
                })
                .setNegativeButton("取消", (dialog, which)->{
                    Toast.makeText(this, "权限申请被拒绝，将可能无法正常访问文件", Toast.LENGTH_SHORT).show();
                    //abc=false;
                })
                .create()
                .show();
    }
//    private void checkStoragePermission(){
//
//    }

//    private void checkStoragePermission() {
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            // 检查是否已有权限
//            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
//                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            // 已经有权限
//                isPermitted=true;
//                //showLogText("PG",LOG);
//
//            return;
//        }
//        //没有权限
//        // 检查是否需要显示权限请求说明
//        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//            // 向用户解释为什么需要权限
//            new AlertDialog.Builder(this)
//                .setTitle("需要存储权限")
//                .setMessage("应用需要存储权限来保存语音文件到您的设备")
//                .setPositiveButton("确定", (dialog, which) -> {
//                    // 用户理解后请求权限
//                    requestPermissions(
//                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        STORAGE_PERMISSION_CODE
//                    );
//                })
//                .setNegativeButton("(请勿)取消", (dialog,which)->{requestPermissions(
//                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                STORAGE_PERMISSION_CODE
//            );})
//                .create()
//                .show();
//        } else {
//            // 直接请求权限
//            requestPermissions(
//                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                STORAGE_PERMISSION_CODE
//            );
//        }
//    }
//     //Android 6.0 以下版本默认有权限
//}
/*
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
    }/*
    
    /*

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
    }*/
//    @Override
//public void onRequestPermissionsResult(int requestCode,
//                                     @NonNull String[] permissions,
//                                     @NonNull int[] grantResults) {
//    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//    if (requestCode == STORAGE_PERMISSION_CODE) {
//        if (grantResults.length > 0 &&
//            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            // 权限已授予
//                isPermitted=true;
//
//                //showLogText("wtf?",LOG);
//
//            Toast.makeText(this, "存储权限已授予", Toast.LENGTH_SHORT).show();
//        } else {
//            // 权限被拒绝
//                isPermitted=false;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                    // 用户勾选了"不再询问"，引导用户去设置中开启权限
//                    showPermissionDeniedDialog();
//                } else {
//                    // 只是简单拒绝，可以再次请求
//                    Toast.makeText(this, "需要存储权限才能保存文件", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//    }
//}


    // 【新增】实际测试 Download 目录是否能读写
    private void testDownloadAccess() {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadDir.exists() && downloadDir.canRead()) {
            isPermitted = true;
            Toast.makeText(this, "可以访问 Download 目录", Toast.LENGTH_SHORT).show();
        } else {
            // 在 Android 10 上，即使权限通过，这里也可能返回 false
            // 此时需要降级使用 MediaStore 或 SAF
            isPermitted = false;
            Toast.makeText(this, "传统方式无法访问，尝试使用系统选择器", Toast.LENGTH_SHORT).show();

            // 如果当前是 Android 10，建议调用 SAF 作为备选
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                openDownloadViaSAF();
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void openDownloadViaSAF() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        // 尝试直接定位到 Downloads（仅部分系统支持）
        Uri downloadsUri = Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, downloadsUri);
        startActivityForResult(intent, REQUEST_CODE_SAF);
    }
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                       @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == STORAGE_PERMISSION_CODE) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 权限授予后，必须实际测试
            testDownloadAccess();
        } else {
            // 用户拒绝权限，检查是否勾选了“不再询问”
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // 用户勾选了“不再询问”，引导去设置页
                    new AlertDialog.Builder(this)
                            .setTitle("权限被禁用")
                            .setMessage("您已拒绝存储权限且选择不再询问，请前往设置手动开启。")
                            .setPositiveButton("去设置", (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            })
                            .setNegativeButton("取消", null)
                            .create()
                            .show();
                } else {
                    Toast.makeText(this, "权限被拒绝，无法访问文件", Toast.LENGTH_SHORT).show();
                }
            }
            isPermitted = false;
        }
    }
}



private void showPermissionDeniedDialog() {
        isPermitted=false;
    new AlertDialog.Builder(this)
        .setTitle("权限被永久拒绝")
        .setMessage("您已拒绝存储权限并选择不再询问。如需使用完整功能，请在设置中手动授予权限")
        .setPositiveButton("去设置", (dialog, which) -> {
            // 跳转到应用设置页面
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        })
        .setNegativeButton("取消", null)
        .create()
        .show();

}
    

    private void showError(String message) {
        showLogText(message,ERROR);
        resultTextView.setText("操作失败");
    }
	
	private static class UserPaths{
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
            return new UserPaths(this.name, this.path);
        }
	}
	
	private void setupPathSpinner(){
		//pths = new ProcessData().getPathList();
		ArrayList<String> nmeList=new ArrayList<>();
		//List<UserPaths> defaultPaths=new ArrayList<>();
        UserPaths tmpPath= new UserPaths("default_path", BASE_DIR);
		//defaultPaths.add(tmpPath);
		//has set 'pths' just now

        if(pths !=null && !pths.isEmpty()){
            for(UserPaths up:pths){
                if(up!=null && up.getName()!=null){
                    nmeList.add(up.getName());
                }

            }
        }else{
            nmeList.add(tmpPath.getName());
        }

		ArrayAdapter<String> adapter=new ArrayAdapter<>(
			this,
			android.R.layout.simple_spinner_item,
			nmeList
		);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		choosePreferencePath.setAdapter(adapter);
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
                    defaultPaths.add(new UserPaths("default_path", BASE_DIR));
                    defaultPaths.add(new UserPaths("original_default_path", BASE_DIR));
                    String  json=gson.toJson(defaultPaths);
                    saveUsers(defaultPaths);
                }catch(Exception e){
                    showError(e.toString());
                }
            }else{
                List<UserPaths> userPaths=loadUsers();

                while(userPaths.remove(null)){}

                //check if-is valid
                for(UserPaths up:userPaths) {
                    //remove all Invalid items or wrong paths
                    if (up != null && up.isInvalid()) {
                        userPaths.remove(up);
                    }
                }
                saveUsers(userPaths);
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
                    showLogText("path: "+name+" deleted\n",LOG);
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
                JsonArray jsonArray= JsonParser.parseReader(reader).getAsJsonArray();
                List<UserPaths> userPaths = new ArrayList<>();
                for(int i = 0; i < jsonArray.size(); i++){
                    UserPaths item = gson.fromJson(jsonArray.get(i),UserPaths.class);
                    if(item!=null){
                        userPaths.add(item);
                    }
                }
                return userPaths;

				//Type userListType = new TypeToken<List<UserPaths>>() {}.getType();
				//return gson.fromJson(reader, userListType);
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